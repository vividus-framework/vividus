/*
 * Copyright 2019-2022 the original author or authors.
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

import static org.apache.commons.lang3.Validate.isTrue;
import static org.hamcrest.Matchers.equalTo;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.jayway.jsonpath.PathNotFoundException;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.hamcrest.Matcher;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.jbehave.core.steps.ParameterConverters.FluentEnumConverter;
import org.vividus.context.VariableContext;
import org.vividus.json.JsonContext;
import org.vividus.softassert.ISoftAssert;
import org.vividus.steps.ComparisonRule;
import org.vividus.steps.StringComparisonRule;
import org.vividus.util.json.JsonPathUtils;
import org.vividus.util.json.JsonUtils;
import org.vividus.variable.VariableScope;

public class JsonSteps
{
    private final FluentEnumConverter fluentEnumConverter;
    private final JsonContext jsonContext;
    private final VariableContext variableContext;
    private ISoftAssert softAssert;
    private JsonUtils jsonUtils;

    public JsonSteps(FluentEnumConverter fluentEnumConverter, JsonContext jsonContext, VariableContext variableContext,
            JsonUtils jsonUtils)
    {
        this.fluentEnumConverter = fluentEnumConverter;
        this.jsonContext = jsonContext;
        this.variableContext = variableContext;
        this.jsonUtils = jsonUtils;
    }

    /**
     * Saves a value of JSON element found in JSON context into the variable with specified name and scope
     *
     * @param jsonPath     The JSON path used to find JSON element value.
     * @param scopes       The set (comma separated list of scopes e.g.: STORY, NEXT_BATCHES) of variable's scope<br>
     *                     <i>Available scopes:</i>
     *                     <ul>
     *                     <li><b>STEP</b> - the variable will be available only within the step,
     *                     <li><b>SCENARIO</b> - the variable will be available only within the scenario,
     *                     <li><b>STORY</b> - the variable will be available within the whole story,
     *                     <li><b>NEXT_BATCHES</b> - the variable will be available starting from next batch
     *                     </ul>
     * @param variableName The name of the variable to save the found JSON element value.
     */
    @When("I save JSON element value from context by JSON path `$jsonPath` to $scopes variable `$variableName`")
    public void saveJsonValueFromContextToVariable(String jsonPath, Set<VariableScope> scopes, String variableName)
    {
        saveJsonValueToVariable(getActualJson(), jsonPath, scopes, variableName);
    }

    /**
     * Saves a value of JSON element found in the given JSON into the variable with specified name and scope
     *
     * @param json         The JSON used to find JSON element value.
     * @param jsonPath     The JSON path used to find JSON element value.
     * @param scopes       The set (comma separated list of scopes e.g.: STORY, NEXT_BATCHES) of variable's scope<br>
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
        getDataByJsonPathSafely(json, jsonPath, true).map(
                    jsonByPath -> {
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
                    }
                )
                .ifPresent(actualData -> variableContext.putVariable(scopes, variableName, actualData));
    }

    /**
     * Converts a JSON into the variable with specified name and scope.
     * JSON fields will be available via their names.<br/>
     * For example:<br/>
     * <b>JSON:</b><br/>
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
     * <br/>
     * <b>Reference to a variable:</b>
     * <pre><b>${json.books[0].title}</b></pre>
     * @param json         The JSON used to find JSON element value.
     * @param scopes       The set (comma separated list of scopes e.g.: STORY, NEXT_BATCHES) of variable's scope<br>
     *                     <i>Available scopes:</i>
     *                     <ul>
     *                     <li><b>STEP</b> - the variable will be available only within the step,
     *                     <li><b>SCENARIO</b> - the variable will be available only within the scenario,
     *                     <li><b>STORY</b> - the variable will be available within the whole story,
     *                     <li><b>NEXT_BATCHES</b> - the variable will be available starting from next batch
     *                     </ul>
     * @param variableName The name of the variable to save the found JSON element value.
     */
    @When("I convert JSON `$json` to $scopes variable `$variableName`")
    public void convertJsonToVariable(String json, Set<VariableScope> scopes, String variableName)
    {
        variableContext.putVariable(scopes, variableName, jsonUtils.toObject(json, Object.class));
    }

    /**
     * Converts a JSON into the variable with specified name and scope.
     * JSON fields will be available via their names.<br/>
     * For example:<br/>
     * <b>JSON:</b><br/>
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
     * <br/>
     * <b>Reference to a variable:</b>
     * <pre><b>${json.books[0].title}</b></pre>
     * @param scopes       The set (comma separated list of scopes e.g.: STORY, NEXT_BATCHES) of variable's scope<br>
     *                     <i>Available scopes:</i>
     *                     <ul>
     *                     <li><b>STEP</b> - the variable will be available only within the step,
     *                     <li><b>SCENARIO</b> - the variable will be available only within the scenario,
     *                     <li><b>STORY</b> - the variable will be available within the whole story,
     *                     <li><b>NEXT_BATCHES</b> - the variable will be available starting from next batch
     *                     </ul>
     * @param variableName The name of the variable to save the found JSON element value.
     */
    @When("I convert JSON from context to $scopes variable `$variableName`")
    public void convertJsonFromContextToVariable(Set<VariableScope> scopes, String variableName)
    {
        convertJsonToVariable(getActualJson(), scopes, variableName);
    }

    /**
     * Validates if the JSON context contains the expected JSON element value by the specified JSON path
     *
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
     *                          <li>for <code>boolean</code> and <code>null</code>-s - only single rule
     *                          <code>IS_EQUAL_TO</code> (readable form: <code>is equal to</code>) is allowed</li>
     *                          <li><code>array</code> and <code>object</code> are complex types and must be validated
     *                          using another steps dedicated for JSON elements.</li>
     *                       </ul>
     * @param expectedData   The expected JSON element value to compare against.
     * @return true if JSON contains the expected JSON element value by the specified JSON path, otherwise - false
     */
    @Then("JSON element value from context by JSON path `$jsonPath` $comparisonRule `$expectedValue`")
    public boolean isValueByJsonPathFromContextEqual(String jsonPath, String comparisonRule, Object expectedData)
    {
        return isValueByJsonPathEqual(getActualJson(), jsonPath, comparisonRule, expectedData);
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
     *                          <li>for <code>boolean</code> and <code>null</code>-s - only single rule
     *                          <code>IS_EQUAL_TO</code> (readable form: <code>is equal to</code>) is allowed</li>
     *                          <li><code>array</code> and <code>object</code> are complex types and must be validated
     *                          using another steps dedicated for JSON elements.</li>
     *                       </ul>
     * @param expectedData   The expected JSON element value to compare against.
     * @return true if JSON contains the expected JSON element value by the specified JSON path, otherwise - false
     */
    @Then("JSON element value from `$json` by JSON path `$jsonPath` $comparisonRule `$expectedValue`")
    public boolean isValueByJsonPathEqual(String json, String jsonPath, String comparisonRule, Object expectedData)
    {
        Optional<Optional<Object>> jsonData = getDataByJsonPathSafely(json, jsonPath, true);
        if (jsonData.isEmpty())
        {
            return false;
        }

        Optional<Object> jsonByPath = jsonData.get();
        if (jsonByPath.isEmpty())
        {
            validateEqualsComparison(comparisonRule, null, expectedData);
            return assertThat(jsonPath, null, equalTo(expectedData));
        }
        Object jsonValue = jsonByPath.get();
        if (jsonValue instanceof Boolean)
        {
            validateEqualsComparison(comparisonRule, jsonValue, expectedData);
            return assertThat(jsonPath, (Boolean) jsonValue, equalTo(Boolean.valueOf(String.valueOf(expectedData))));
        }
        if (jsonValue instanceof String)
        {
            StringComparisonRule rule = convertToEnum(normalizeToEnumConstant(comparisonRule),
                    StringComparisonRule.class);
            return assertThat(jsonPath, (String) jsonValue, rule.createMatcher(String.valueOf(expectedData)));
        }
        if (jsonValue instanceof Number)
        {
            ComparisonRule rule = convertToEnum(StringUtils.removeStart(normalizeToEnumConstant(comparisonRule), "IS_"),
                    ComparisonRule.class);
            BigDecimal actualNumber = new BigDecimal(jsonValue.toString());
            BigDecimal expectedNumber = NumberUtils.createBigDecimal(String.valueOf(expectedData));
            return assertThat(jsonPath, actualNumber, rule.getComparisonRule(expectedNumber));
        }
        return reportUnexpectedValueType(jsonPath, jsonValue);
    }

    @SuppressWarnings("unchecked")
    private <T extends Enum<?>> T convertToEnum(String comparisonRule, Class<T> enumClass)
    {
        return (T) fluentEnumConverter.convertValue(comparisonRule, enumClass);
    }

    private void validateEqualsComparison(String comparisonRule, Object actualJsonValue, Object expectedJsonValue)
    {
        isTrue("IS_EQUAL_TO".equals(normalizeToEnumConstant(comparisonRule)),
                "Unable to compare actual JSON element value '" + actualJsonValue + "' against expected value '"
                        + expectedJsonValue + "' using comparison rule '" + comparisonRule + "'");
    }

    private String normalizeToEnumConstant(String comparisonRule)
    {
        return comparisonRule.trim().replaceAll("\\W", "_").toUpperCase();
    }

    private <T> boolean assertThat(String jsonPath, T actual, Matcher<? super T> matcher)
    {
        return softAssert.assertThat(String.format("Value of JSON element found by JSON path '%s'", jsonPath), actual,
                matcher);
    }

    public <T> Optional<Optional<T>> getDataByJsonPathSafely(String json, String jsonPath, boolean recordFail)
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

    private String getActualJson()
    {
        return jsonContext.getJsonContext();
    }

    public void setSoftAssert(ISoftAssert softAssert)
    {
        this.softAssert = softAssert;
    }
}
