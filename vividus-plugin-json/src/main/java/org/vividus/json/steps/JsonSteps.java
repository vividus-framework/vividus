/*
 * Copyright 2019-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.vividus.json.steps;

import static net.javacrumbs.jsonunit.JsonMatchers.jsonEquals;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.regex.Pattern;

import com.jayway.jsonpath.PathNotFoundException;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.math.NumberUtils;
import org.hamcrest.Matcher;
import org.jbehave.core.annotations.Alias;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.jbehave.core.steps.ParameterConverters.FluentEnumConverter;
import org.vividus.context.VariableContext;
import org.vividus.json.JsonContext;
import org.vividus.json.JsonDiffMatcher;
import org.vividus.reporter.event.IAttachmentPublisher;
import org.vividus.softassert.ISoftAssert;
import org.vividus.steps.ComparisonRule;
import org.vividus.steps.StringComparisonRule;
import org.vividus.steps.SubSteps;
import org.vividus.util.json.JsonPathUtils;
import org.vividus.util.json.JsonUtils;
import org.vividus.variable.VariableScope;

import net.javacrumbs.jsonunit.core.Option;

public class JsonSteps
{
    private static final String ASSERTION_BOUNDS = String.join("|", Set.of(
            "Different (?:value|keys) found",
            "Array \"[^\"]*\" has (different|invalid)"
    ));
    private static final char LF = '\n';
    private static final Pattern DIFFERENCES_PATTERN = Pattern.compile(
            "(?=(?:" + ASSERTION_BOUNDS + ")).+?(?=(?:" + ASSERTION_BOUNDS + "|$))",
            Pattern.DOTALL);

    private static final String IS_EQUAL_TO = "IS_EQUAL_TO";
    private static final String IS_NOT_EQUAL_TO = "IS_NOT_EQUAL_TO";
    private static final String INVALID_COMPARISON_RULE_MESSAGE = "Unable to compare actual JSON element value '%s' "
            + "against expected value '%s' using comparison rule '%s'";

    private final FluentEnumConverter fluentEnumConverter;
    private final JsonContext jsonContext;
    private final VariableContext variableContext;
    private final JsonUtils jsonUtils;
    private final ISoftAssert softAssert;
    private final IAttachmentPublisher attachmentPublisher;
    private final Map<String, Matcher<Object>> customJsonMatchers;

    public JsonSteps(FluentEnumConverter fluentEnumConverter, JsonContext jsonContext, VariableContext variableContext,
            JsonUtils jsonUtils, ISoftAssert softAssert, IAttachmentPublisher attachmentPublisher,
                     Map<String, Matcher<Object>> customJsonMatchers)
    {
        this.fluentEnumConverter = fluentEnumConverter;
        this.jsonContext = jsonContext;
        this.variableContext = variableContext;
        this.jsonUtils = jsonUtils;
        this.softAssert = softAssert;
        this.attachmentPublisher = attachmentPublisher;
        this.customJsonMatchers = customJsonMatchers;
    }

    /**
     * Saves a value of JSON element found in the given JSON into the variable with the specified name and scope.
     *
     * @param json         The JSON used to find JSON element value.
     * @param jsonPath     The JSON path used to find JSON element value.
     * @param scopes       The set (comma separated list of scopes e.g.: STORY, NEXT_BATCHES) of the variable
     *                     scopes.<br>
     *                     <i>Available scopes:</i>
     *                     <ul>
     *                     <li><b>STEP</b> - the variable will be available only within the step,
     *                     <li><b>SCENARIO</b> - the variable will be available only within the scenario,
     *                     <li><b>STORY</b> - the variable will be available within the whole story,
     *                     <li><b>NEXT_BATCHES</b> - the variable will be available starting from next batch
     *                     </ul>
     * @param variableName The name of the variable to save the found JSON element value.
     */
    @When("I save JSON element value from `$json` by JSON path `$jsonPath` to $scopes variable `$variableName`")
    public void saveJsonValueToVariable(String json, String jsonPath, Set<VariableScope> scopes, String variableName)
    {
        getDataByJsonPathSafely(json, jsonPath, true).map(jsonByPath -> {
            if (jsonByPath.isEmpty())
            {
                return String.valueOf((String) null);
            }
            Object jsonValue = jsonByPath.get();
            if (jsonValue instanceof String || jsonValue instanceof Boolean || jsonValue instanceof Number)
            {
                return String.valueOf(jsonValue);
            }
            reportUnexpectedValueType(jsonPath, jsonValue);
            return null;
        }).ifPresent(actualData -> variableContext.putVariable(scopes, variableName, actualData));
    }

    /**
     * Saves a JSON element found in the given JSON into the variable with the specified name and scope.
     *
     * @param json         The JSON used to find JSON element.
     * @param jsonPath     The JSON path used to find JSON element.
     * @param scopes       The set (comma separated list of scopes e.g.: STORY, NEXT_BATCHES) of the variable
     *                     scopes.<br>
     *                     <i>Available scopes:</i>
     *                     <ul>
     *                     <li><b>STEP</b> - the variable will be available only within the step,
     *                     <li><b>SCENARIO</b> - the variable will be available only within the scenario,
     *                     <li><b>STORY</b> - the variable will be available within the whole story,
     *                     <li><b>NEXT_BATCHES</b> - the variable will be available starting from next batch
     *                     </ul>
     * @param variableName The name of the variable to save the found JSON element.
     */
    @When("I save JSON element from `$json` by JSON path `$jsonPath` to $scopes variable `$variableName`")
    public void saveJsonElementToVariable(String json, String jsonPath, Set<VariableScope> scopes, String variableName)
    {
        getJsonElementByJsonPath(json, jsonPath, null)
                .ifPresent(actualData -> variableContext.putVariable(scopes, variableName, actualData));
    }

    /**
     * Converts JSON element into the variable with the specified name and scope.
     * JSON fields will be available via their names.<br>
     * For example:<br>
     * <b>JSON:</b><br>
     * <pre>
     * {
     *   "book":[
     *     {
     *       "author":"Karl Marx",
     *       "title":"Das Kapital"
     *     }
     *   ]
     * }
     * </pre>
     * <br>
     * <b>Reference to a variable:</b>
     * <pre><b>${json.books[0].title}</b></pre>
     *
     * @param json         The JSON used to find JSON element.
     * @param scopes       The set (comma separated list of scopes e.g.: STORY, NEXT_BATCHES) of the variable
     *                     scopes.<br>
     *                     <i>Available scopes:</i>
     *                     <ul>
     *                     <li><b>STEP</b> - the variable will be available only within the step,
     *                     <li><b>SCENARIO</b> - the variable will be available only within the scenario,
     *                     <li><b>STORY</b> - the variable will be available within the whole story,
     *                     <li><b>NEXT_BATCHES</b> - the variable will be available starting from next batch
     *                     </ul>
     * @param variableName The name of the variable to save the found JSON element.
     */
    @When("I convert JSON `$json` to $scopes variable `$variableName`")
    public void convertJsonToVariable(String json, Set<VariableScope> scopes, String variableName)
    {
        variableContext.putVariable(scopes, variableName, jsonUtils.toObject(json, Object.class));
    }

    /**
     * Saves the number of elements found in the JSON by JSON path into the variable with the
     * specified name and scope.
     *
     * @param json         The JSON used to find JSON elements.
     * @param jsonPath     The JSON path used to find JSON elements.
     * @param scopes       The set (comma separated list of scopes e.g.: STORY, NEXT_BATCHES) of the variable
     *                     scopes.<br>
     *                     <i>Available scopes:</i>
     *                     <ul>
     *                     <li><b>STEP</b> - the variable will be available only within the step,
     *                     <li><b>SCENARIO</b> - the variable will be available only within the scenario,
     *                     <li><b>STORY</b> - the variable will be available within the whole story,
     *                     <li><b>NEXT_BATCHES</b> - the variable will be available starting from next batch
     *                     </ul>
     * @param variableName The name of the variable to save the number of elements found in the JSON.
     */
    @When("I save number of elements from `$json` found by JSON path `$jsonPath` to $scopes variable `$variableName`")
    public void saveElementsNumberByJsonPath(String json, String jsonPath, Set<VariableScope> scopes,
            String variableName)
    {
        variableContext.putVariable(scopes, variableName, getElementsNumber(json, jsonPath));
    }

    /**
     * Executes steps against all elements found by JSON path in the JSON data. The actions performed by the step are:
     * <ul>
     * <li>searches for elements using JSON path;</li>
     * <li>checks the elements number matches comparison rule;</li>
     * <li>passes if the comparison rule matches and the elements number is 0;</li>
     * <li>otherwise switches JSON context to each found element and executes all steps (no steps will be executed in
     * case of comparison rule mismatch);</li>
     * <li>restores previous JSON context.</li>
     * </ul>
     * Usage example:
     * <pre>
     * When I find equal to `1` JSON elements from `{"parent_id":"","elements":[{"name": "1"},{"name": "2"}]}` by
     * `$.[?(@.parent_id=="")]` and for each element do
     * |step                                                                              |
     * |Then number of JSON elements from `${json-context}` by JSON path `$..name` is = 2 |
     * </pre>
     *
     * @param comparisonRule The rule to match the number of elements. The supported rules:
     *                       <ul>
     *                       <li>less than (&lt;)</li>
     *                       <li>less than or equal to (&lt;=)</li>
     *                       <li>greater than (&gt;)</li>
     *                       <li>greater than or equal to (&gt;=)</li>
     *                       <li>equal to (=)</li>
     *                       <li>not equal to (!=)</li>
     *                       </ul>
     * @param elementsNumber The expected number of elements.
     * @param json           The JSON used to find JSON elements.
     * @param jsonPath       The JSON path used to find JSON elements.
     * @param stepsToExecute ExamplesTable with steps to execute for each found JSON element.
     */
    @When("I find $comparisonRule `$elementsNumber` JSON elements from `$json` by `$jsonPath` and for each element do"
            + "$stepsToExecute")
    @Alias("I find $comparisonRule '$elementsNumber' JSON elements from '$json' by '$jsonPath' and for each element "
            + "do$stepsToExecute")
    public void executeStepsForFoundJsonElements(ComparisonRule comparisonRule, int elementsNumber, String json,
            String jsonPath, SubSteps stepsToExecute)
    {
        executeStepsForFoundJsonElements(json, jsonPath, comparisonRule, elementsNumber, stepsToExecute, () -> false);
    }

    /**
     * Executes steps against all elements found by JSON path in the JSON data until the variable is not set or its
     * value corresponds to the expected one. The actions performed by the step are:
     * <ul>
     * <li>searches for elements using JSON path;</li>
     * <li>checks the elements number matches comparison rule;</li>
     * <li>passes if the comparison rule matches and the elements number is 0;</li>
     * <li>otherwise switches JSON context to each found element and executes all steps until the variable is not set
     * or mismatches the expected value (no steps will be executed in case of comparison rule mismatch);</li>
     * <li>restores previous JSON context;</li>
     * <li>fails if the variable has never been set during the iterations execution.</li>
     * </ul>
     * Usage example:
     * <pre>
     * When I find &gt; `1` JSON elements in `${json}` by `$.store.book` and until variable `title` matches `M.+`
     * for each element I do:
     * |step|
     * |When I save JSON element value from `${json-context}` by JSON path `$.title` to scenario variable `title`|
     * </pre>
     *
     * @param comparisonRule  The rule to match the number of elements. The supported rules:
     *                        <ul>
     *                        <li>less than (&lt;)</li>
     *                        <li>less than or equal to (&lt;=)</li>
     *                        <li>greater than (&gt;)</li>
     *                        <li>greater than or equal to (&gt;=)</li>
     *                        <li>equal to (=)</li>
     *                        <li>not equal to (!=)</li>
     *                        </ul>
     * @param elementsNumber  The expected number of elements.
     * @param json            The JSON used to find JSON elements.
     * @param jsonPath        The JSON path used to find JSON elements.
     * @param variableName    The name of the variable to validate.
     * @param variableMatcher The rule to match the variable value. The supported rules:
     *                        <ul>
     *                        <li>is equal to</li>
     *                        <li>contains</li>
     *                        <li>does not contain</li>
     *                        <li>matches</li>
     *                        </ul>
     * @param expectedValue   The expected value of the variable.
     * @param stepsToExecute  ExamplesTable with steps to execute for each found JSON element.
     */
    @When("I find $comparisonRule `$elementsNumber` JSON elements in `$json` by `$jsonPath` and until variable "
            + "`$variableName` $variableMatcher `$expectedValue` for each element I do:$stepsToExecute")
    @Alias("I find $comparisonRule '$elementsNumber' JSON elements in '$json' by '$jsonPath' and until variable "
            + "'$variableName' $variableMatcher '$expectedValue' for each element I do:$stepsToExecute")
    public void executeStepsForFoundJsonElementsExpectingVariable(ComparisonRule comparisonRule, int elementsNumber,
            String json, String jsonPath, String variableName, StringComparisonRule variableMatcher,
            String expectedValue, SubSteps stepsToExecute)
    {
        Matcher<String> matcher = variableMatcher.createMatcher(expectedValue);
        executeStepsForFoundJsonElements(json, jsonPath, comparisonRule, elementsNumber, stepsToExecute, () -> {
            Object variable = variableContext.getVariable(variableName);
            return variable != null && matcher.matches(variable.toString());
        });
        Object variable = variableContext.getVariable(variableName);
        if (variable == null)
        {
            softAssert.recordFailedAssertion(String.format("Variable `%s` was not initialized", variableName));
        }
    }

    private void executeStepsForFoundJsonElements(String json, String jsonPath, ComparisonRule comparisonRule,
            int elementsNumber, SubSteps stepsToExecute, BooleanSupplier breakCondition)
    {
        Optional<List<?>> jsonElements = getDataByJsonPathSafely(json, jsonPath, false).map(jsonByPath ->
                jsonByPath.map(jsonValue -> jsonValue instanceof List ? (List<?>) jsonValue : List.of(jsonValue))
                        .orElseGet(() -> Collections.singletonList(null))
        );
        int count = jsonElements.map(List::size).orElse(0);
        if (assertJsonElementsNumber(jsonPath, count, comparisonRule, elementsNumber) && count != 0)
        {
            String currentJsonContext = jsonContext.getJsonContext();
            try
            {
                for (Object jsonElement : jsonElements.get())
                {
                    if (breakCondition.getAsBoolean())
                    {
                        break;
                    }
                    jsonContext.putJsonContext(jsonUtils.toJson(jsonElement));
                    stepsToExecute.execute(Optional.empty());
                }
            }
            finally
            {
                jsonContext.putJsonContext(currentJsonContext);
            }
        }
    }

    /**
     * Validates if the given JSON contains the expected JSON element value by the specified JSON path
     *
     * @param json           The JSON used to find the actual JSON element value.
     * @param jsonPath       The JSON path used to find the actual JSON element value.
     * @param comparisonRule The comparison rule to match JSON element value depending on the
     *                       <a href="https://www.json.org/json-en.html">element type</a>:
     *                       <ul>
     *                           <li>for <code>string</code> - string comparison rules are applicable: "is equal to",
     *                           "contains", "does not contain" or "matches",
     *                           <li>for <code>number</code> - regular comparison rules are applicable:
     *                           <ul>
     *                              <li>less than (&lt;)</li>
     *                              <li>less than or equal to (&lt;=)</li>
     *                              <li>greater than (&gt;)</li>
     *                              <li>greater than or equal to (&gt;=)</li>
     *                              <li>equal to (=)</li>
     *                              <li>not equal to (!=)</li>
     *                          </ul>
     *                          <li>for <code>boolean</code> - only single rule <code>IS_EQUAL_TO</code>
     *                          (readable form: <code>is equal to</code>) is allowed,</li>
     *                          <li>for <code>null</code> - only two rules <code>IS_EQUAL_TO</code> and
     *                          <code>IS_NOT_EQUAL_TO</code> (readable forms: <code>is equal to</code> and
     *                          <code>is not equal to</code>) are allowed</li>
     *                          <li><code>array</code> and <code>object</code> are complex types and must be validated
     *                          using another steps dedicated for JSON elements.</li>
     *                       </ul>
     * @param expectedData   The expected JSON element value to compare against.
     */
    @Then("JSON element value from `$json` by JSON path `$jsonPath` $comparisonRule `$expectedValue`")
    public void assertValueByJsonPath(String json, String jsonPath, String comparisonRule, Object expectedData)
    {
        getDataByJsonPathSafely(json, jsonPath, true).ifPresent(jsonByPath -> {
            Object jsonValue = jsonByPath.orElse(null);
            String normalizedComparisonRule = normalizeToEnumConstant(comparisonRule);
            if (jsonValue == null || expectedData == null)
            {
                Matcher<Object> matcher;
                switch (normalizedComparisonRule)
                {
                    case IS_EQUAL_TO:
                        matcher = equalTo(expectedData);
                        break;
                    case IS_NOT_EQUAL_TO:
                        matcher = not(equalTo(expectedData));
                        break;
                    default:
                        throw new IllegalArgumentException(
                                String.format(INVALID_COMPARISON_RULE_MESSAGE, jsonValue, expectedData,
                                        comparisonRule));
                }
                assertThat(jsonPath, jsonValue, matcher);
            }
            else if (jsonValue instanceof Boolean)
            {
                Validate.isTrue(IS_EQUAL_TO.equals(normalizedComparisonRule), INVALID_COMPARISON_RULE_MESSAGE,
                        jsonValue, expectedData, comparisonRule);
                assertThat(jsonPath, (Boolean) jsonValue, equalTo(Boolean.valueOf(String.valueOf(expectedData))));
            }
            else if (jsonValue instanceof String)
            {
                StringComparisonRule rule = convertToEnum(normalizedComparisonRule, StringComparisonRule.class);
                assertThat(jsonPath, (String) jsonValue, rule.createMatcher(String.valueOf(expectedData)));
            }
            else if (jsonValue instanceof Number)
            {
                ComparisonRule rule = convertToEnum(StringUtils.removeStart(normalizedComparisonRule, "IS_"),
                        ComparisonRule.class);
                BigDecimal actualNumber = new BigDecimal(jsonValue.toString());
                BigDecimal expectedNumber = NumberUtils.createBigDecimal(String.valueOf(expectedData));
                assertThat(jsonPath, actualNumber, rule.getComparisonRule(expectedNumber));
            }
            else
            {
                reportUnexpectedValueType(jsonPath, jsonValue);
            }
        });
    }

    /**
     * Validates if the given JSON contains the expected JSON element matching the comparison rule by the specified
     * JSON path.
     *
     * @param json         The JSON used to find the actual JSON element.
     * @param jsonPath     The JSON path used to find the actual JSON element.
     * @param expectedJson The expected JSON element to compare against.
     * @param options      The set of JSON comparison options. The available options are: TREATING_NULL_AS_ABSENT,
     *                     IGNORING_ARRAY_ORDER, IGNORING_EXTRA_FIELDS, IGNORING_EXTRA_ARRAY_ITEMS, IGNORING_VALUES.
     */
    @Then("JSON element from `$json` by JSON path `$jsonPath` is equal to `$expectedJson`$options")
    public void assertElementByJsonPath(String json, String jsonPath, String expectedJson, Set<Option> options)
    {
        getJsonElementByJsonPath(json, jsonPath, expectedJson).ifPresent(actualData -> {
            JsonDiffMatcher jsonMatcher = new JsonDiffMatcher(attachmentPublisher, expectedJson)
                    .withTolerance(BigDecimal.ZERO);
            for (Option option : options)
            {
                jsonMatcher = jsonMatcher.when(option);
            }
            customJsonMatchers.forEach(jsonMatcher::withMatcher);

            jsonMatcher.matches(actualData);
            String differences = jsonMatcher.getDifferences();

            if (differences == null)
            {
                softAssert.assertThat(String.format("Data by JSON path: %s is equal to '%s'", jsonPath, expectedJson),
                        actualData, jsonMatcher);
                return;
            }

            StringBuilder matched = new StringBuilder("JSON documents are different:").append(LF);
            java.util.regex.Matcher matcher = DIFFERENCES_PATTERN.matcher(differences);
            while (matcher.find())
            {
                String assertion = matcher.group().strip();
                matched.append(assertion).append(LF);
                softAssert.recordFailedAssertion(assertion);
            }
            String matchedDiff = matched.toString();
            Validate.isTrue(matchedDiff.equals(differences),
                    "Unable to match all JSON diff entries from the diff text.%nExpected diff to match:%n%s%nActual "
                            + "matched diff:%n%s%n",
                    differences, matchedDiff);
        });
    }

    /**
     * Compares the number of elements found in the given json by JSON path with the expected number.
     *
     * @param json           The JSON used to find the actual JSON elements.
     * @param jsonPath       The JSON path used to find the JSON elements.
     * @param comparisonRule The rule to match the number of JSON elements. The supported rules:
     *                       <ul>
     *                       <li>less than (&lt;)</li>
     *                       <li>less than or equal to (&lt;=)</li>
     *                       <li>greater than (&gt;)</li>
     *                       <li>greater than or equal to (&gt;=)</li>
     *                       <li>equal to (=)</li>
     *                       <li>not equal to (!=)</li>
     *                       </ul>
     * @param elementsNumber The expected number of JSON elements.
     */
    @Then("number of JSON elements from `$json` by JSON path `$jsonPath` is $comparisonRule $elementsNumber")
    public void assertNumberOfJsonElements(String json, String jsonPath, ComparisonRule comparisonRule,
            int elementsNumber)
    {
        int actualNumber = getElementsNumber(json, jsonPath);
        assertJsonElementsNumber(jsonPath, actualNumber, comparisonRule, elementsNumber);
    }

    @SuppressWarnings("unchecked")
    private Optional<String> getJsonElementByJsonPath(String json, String jsonPath, String expectedData)
    {
        return getDataByJsonPathSafely(json, jsonPath, true).map(
                jsonByPath -> jsonByPath.map(jsonValue -> {
                    if (jsonValue instanceof List && expectedData != null)
                    {
                        List<Object> values = (List<Object>) jsonValue;
                        if (values.size() == 1)
                        {
                            String actualJsonElement = jsonUtils.toJson(values.get(0));
                            if (jsonEquals(expectedData).matches(actualJsonElement))
                            {
                                return actualJsonElement;
                            }
                        }
                    }
                    return jsonUtils.toJson(jsonValue);
                }).orElseGet(() -> jsonUtils.toJson(null))
        );
    }

    @SuppressWarnings("unchecked")
    private <T extends Enum<?>> T convertToEnum(String comparisonRule, Class<T> enumClass)
    {
        return (T) fluentEnumConverter.convertValue(comparisonRule, enumClass);
    }

    private String normalizeToEnumConstant(String comparisonRule)
    {
        return comparisonRule.trim().replaceAll("\\W", "_").toUpperCase();
    }

    private <T> void assertThat(String jsonPath, T actual, Matcher<? super T> matcher)
    {
        softAssert.assertThat(String.format("Value of JSON element found by JSON path '%s'", jsonPath), actual,
                matcher);
    }

    @SuppressWarnings("rawtypes")
    public int getElementsNumber(String json, String jsonPath)
    {
        return getDataByJsonPathSafely(json, jsonPath, false).map(
                jsonByPath -> jsonByPath.filter(List.class::isInstance).map(List.class::cast).map(List::size).orElse(1)
        ).orElse(0);
    }

    private <T> Optional<Optional<T>> getDataByJsonPathSafely(String json, String jsonPath, boolean recordFail)
    {
        try
        {
            return Optional.of(Optional.ofNullable(JsonPathUtils.getData(json, jsonPath)));
        }
        catch (PathNotFoundException e)
        {
            if (recordFail)
            {
                softAssert.recordFailedAssertion(e);
            }
            return Optional.empty();
        }
    }

    private boolean reportUnexpectedValueType(String jsonPath, Object jsonValue)
    {
        String actualType = jsonValue instanceof List ? "array" : "object";
        return softAssert.recordFailedAssertion(String.format(
                "Value of JSON element found by JSON path '%s' must be either null, or boolean, or string, or number,"
                        + " but found %s",
                jsonPath, actualType));
    }

    private boolean assertJsonElementsNumber(String jsonPath, int actualNumber, ComparisonRule comparisonRule,
            int expectedElementsNumber)
    {
        return softAssert.assertThat("The number of JSON elements by JSON path: " + jsonPath, actualNumber,
                comparisonRule.getComparisonRule(expectedElementsNumber));
    }
}
