/*
 * Copyright 2019-2020 the original author or authors.
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

package org.vividus.bdd.steps.api;

import static java.lang.String.format;
import static java.lang.String.join;
import static net.javacrumbs.jsonunit.JsonMatchers.jsonEquals;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.jayway.jsonpath.InvalidJsonException;
import com.jayway.jsonpath.PathNotFoundException;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.vividus.bdd.context.IBddVariableContext;
import org.vividus.bdd.diff.JsonDiffMatcher;
import org.vividus.bdd.steps.ComparisonRule;
import org.vividus.bdd.steps.SubSteps;
import org.vividus.bdd.variable.VariableScope;
import org.vividus.http.HttpMethod;
import org.vividus.http.HttpRequestExecutor;
import org.vividus.http.HttpTestContext;
import org.vividus.http.client.HttpResponse;
import org.vividus.reporter.event.IAttachmentPublisher;
import org.vividus.softassert.ISoftAssert;
import org.vividus.util.json.JsonPathUtils;
import org.vividus.util.json.JsonUtils;
import org.vividus.util.wait.DurationBasedWaiter;
import org.vividus.util.wait.RetryTimesBasedWaiter;
import org.vividus.util.wait.WaitMode;
import org.vividus.util.wait.Waiter;

import net.javacrumbs.jsonunit.core.internal.Options;

public class JsonResponseValidationSteps
{
    private static final Set<String> ASSERTION_BOUNDS = Set.of(
            "Different (?:value|keys) found",
            "Array \"[^\"]*\" has different"
            );
    private static final String PIPE = "|";
    private static final char LF = '\n';
    private static final Pattern DIFFERENCES_PATTERN = Pattern.compile(
            "(?=(?:" + join(PIPE, ASSERTION_BOUNDS) + ")).+?(?=(?:" + join(PIPE, ASSERTION_BOUNDS) + "|$))",
            Pattern.DOTALL);

    private final HttpTestContext httpTestContext;
    private final IBddVariableContext bddVariableContext;
    private final IAttachmentPublisher attachmentPublisher;
    private final HttpRequestExecutor httpRequestExecutor;
    private final JsonUtils jsonUtils;

    private ISoftAssert softAssert;

    public JsonResponseValidationSteps(HttpTestContext httpTestContext, IBddVariableContext bddVariableContext,
            IAttachmentPublisher attachmentPublisher, HttpRequestExecutor httpRequestExecutor, JsonUtils jsonUtils)
    {
        this.httpTestContext = httpTestContext;
        this.bddVariableContext = bddVariableContext;
        this.attachmentPublisher = attachmentPublisher;
        this.httpRequestExecutor = httpRequestExecutor;
        this.jsonUtils = jsonUtils;
    }

    /**
     * Checks if JSON contains the expected data by given JSON path
     * @param jsonPath JSON path
     * @param expectedData expected value of element by JSON path
     * @param options JSON comparison options. Available options: TREATING_NULL_AS_ABSENT, IGNORING_ARRAY_ORDER,
     * IGNORING_EXTRA_FIELDS, IGNORING_EXTRA_ARRAY_ITEMS, IGNORING_VALUES
     * @return true JSON contains the expected data by given JSON path, otherwise - false
     */
    @Then("a JSON element by the JSON path '$jsonPath' is equal to '$expectedData'$options")
    public boolean isDataByJsonPathEqual(String jsonPath, String expectedData, Options options)
    {
        return isDataByJsonPathFromJsonEqual(getActualJson(), jsonPath, expectedData, options);
    }

    /**
     * Checks if supplied JSON contains the expected data by given JSON path
     * @param json JSON data
     * @param jsonPath JSON path
     * @param expectedData expected value of element by JSON path
     * @param options JSON comparison options. Available options: TREATING_NULL_AS_ABSENT, IGNORING_ARRAY_ORDER,
     * IGNORING_EXTRA_FIELDS, IGNORING_EXTRA_ARRAY_ITEMS, IGNORING_VALUES
     * @return true JSON contains the expected data by given JSON path, otherwise - false
     */
    @Then("a JSON element from '$json' by the JSON path '$jsonPath' is equal to '$expectedData'$options")
    public boolean isDataByJsonPathFromJsonEqual(String json, String jsonPath, String expectedData, Options options)
    {
        return getDataByJsonPath(json, jsonPath, expectedData).map(match(jsonPath, expectedData, options))
                .orElse(Boolean.FALSE).booleanValue();
    }

    private Function<String, Boolean> match(String jsonPath, String expectedData, Options options)
    {
        return actualData ->
        {
            JsonDiffMatcher jsonMatcher = new JsonDiffMatcher(attachmentPublisher, expectedData).withOptions(options)
                    .withTolerance(BigDecimal.ZERO);
            jsonMatcher.matches(actualData);
            String differences = jsonMatcher.getDifferences();

            if (differences == null)
            {
                return softAssert.assertThat(format("Data by JSON path: %s is equal to '%s'", jsonPath, expectedData),
                        actualData, jsonMatcher);
            }

            StringBuilder matched = new StringBuilder("JSON documents are different:").append(LF);
            Matcher matcher = DIFFERENCES_PATTERN.matcher(differences);
            while (matcher.find())
            {
                String assertion = matcher.group().strip();
                matched.append(assertion).append(LF);
                softAssert.recordFailedAssertion(assertion);
            }
            String matchedDiff = matched.toString();
            Validate.isTrue(matchedDiff.equals(differences),
                    "Unable to match all JSON diff entries from the diff text."
                    + "%nExpected diff to match:%n%s%nActual matched diff:%n%s%n", differences, matchedDiff);
            return false;
        };
    }

    /**
     * Compares the number of elements found in JSON by JSON path with the expected number.
     * @param jsonPath JSON path
     * @param comparisonRule The rule to compare values
     * (<i>Possible values:<b> less than, less than or equal to, greater than, greater than or equal to,
     * equal to</b></i>)
     * @param elementsNumber The expected number of elements
     * @return true - the number of found elements is as expected; false - the actual number is not as expected or the
     * specified JSON path was not found
     */
    @Then("the number of JSON elements by the JSON path '$jsonPath' is $comparisonRule $elementsNumber")
    public boolean doesJsonPathElementsMatchRule(String jsonPath, ComparisonRule comparisonRule, int elementsNumber)
    {
        return doesJsonPathElementsFromJsonMatchRule(getActualJson(), jsonPath, comparisonRule, elementsNumber);
    }

    /**
     * Compares the number of elements found in the given json by JSON path with the expected number.
     * @param json A json string to find elements in it
     * @param jsonPath JSON path
     * @param comparisonRule The rule to compare values
     * (<i>Possible values:<b> less than, less than or equal to, greater than, greater than or equal to,
     * equal to</b></i>)
     * @param elementsNumber The expected number of elements
     * @return true - the number of found elements is as expected; false - the actual number is not as expected or the
     * specified JSON path was not found
     */
    @Then("number of JSON elements from `$json` by JSON path `$jsonPath` is $comparisonRule $elementsNumber")
    public boolean doesJsonPathElementsFromJsonMatchRule(String json, String jsonPath, ComparisonRule comparisonRule,
            int elementsNumber)
    {
        int actualNumber = getElementsNumber(json, jsonPath);
        return assertJsonElementsNumber(jsonPath, actualNumber, comparisonRule, elementsNumber);
    }

    /**
     * Saves value extracted from JSON context or HTTP response with given scope into the variable with specified name
     * <p>
     * <b>Actions performed at this step:</b>
     * </p>
     * <ul>
     * <li>Extracts value by jsonPath from JSON context or HTTP response</li>
     * <li>Saves value extracted into the variable with specified name</li>
     * </ul>
     * @param jsonPath json path
     * @param scopes The set (comma separated list of scopes e.g.: STORY, NEXT_BATCHES) of variable's scope<br>
     * <i>Available scopes:</i>
     * <ul>
     * <li><b>STEP</b> - the variable will be available only within the step,
     * <li><b>SCENARIO</b> - the variable will be available only within the scenario,
     * <li><b>STORY</b> - the variable will be available within the whole story,
     * <li><b>NEXT_BATCHES</b> - the variable will be available starting from next batch
     * </ul>
     * @param variableName variable name
     */
    @When("I save JSON element from context by JSON path `$jsonPath` to $scopes variable `$variableName`")
    public void saveJsonElementFromContextToVariable(String jsonPath, Set<VariableScope> scopes, String variableName)
    {
        saveJsonElementToVariable(getActualJson(), jsonPath, scopes, variableName);
    }

    /**
     * Saves value extracted by the JSON path from the given json with the given scope into the variable with specified
     * name
     * <p>
     * <b>Actions performed at this step:</b>
     * </p>
     * <ul>
     * <li>Extracts value by jsonPath from the given json</li>
     * <li>Saves value extracted from HTTP response with given scope into the variable with specified name</li>
     * </ul>
     * @param json A json string to extract value from
     * @param jsonPath A JSON path
     * @param scopes The set (comma separated list of scopes e.g.: STORY, NEXT_BATCHES) of variable's scope<br>
     * <i>Available scopes:</i>
     * <ul>
     * <li><b>STEP</b> - the variable will be available only within the step,
     * <li><b>SCENARIO</b> - the variable will be available only within the scenario,
     * <li><b>STORY</b> - the variable will be available within the whole story,
     * <li><b>NEXT_BATCHES</b> - the variable will be available starting from next batch
     * </ul>
     * @param variableName A variable name
     */
    @When("I save a JSON element from '$json' by JSON path '$jsonPath' to $scopes variable '$variableName'")
    public void saveJsonElementToVariable(String json, String jsonPath, Set<VariableScope> scopes, String variableName)
    {
        getDataByJsonPath(json, jsonPath, null)
                .ifPresent(actualData -> bddVariableContext.putVariable(scopes, variableName, actualData));
    }

    /**
     * Saves the found by json path elements' quantity to the variable with
     * specified name and scope.
     * @param jsonPath A JSON path
     * @param scopes The set (comma separated list of scopes e.g.: STORY, NEXT_BATCHES) of variable's scope<br>
     * <i>Available scopes:</i>
     * <ul>
     * <li><b>STEP</b> - the variable will be available only within the step,
     * <li><b>SCENARIO</b> - the variable will be available only within the scenario,
     * <li><b>STORY</b> - the variable will be available within the whole story,
     * <li><b>NEXT_BATCHES</b> - the variable will be available starting from next batch
     * </ul>
     * @param variableName A variable name
     */
    @When("I set the number of elements found by the JSON path '$jsonPath' to the $scopes variable '$variableName'")
    public void saveElementsNumberByJsonPath(String jsonPath, Set<VariableScope> scopes, String variableName)
    {
        bddVariableContext.putVariable(scopes, variableName, getElementsNumber(getActualJson(), jsonPath));
    }

    /**
     * Waits until GET request to resource retrieves response body that contains specific JSON path
     * for a specified amount of time.
     * <p>
     * <b>Actions performed at this step:</b>
     * </p>
     * <ul>
     * <li>Executes HTTP GET request to the specified resource</li>
     * <li>Checks if response body contains an element by JSON path</li>
     * <li>Sleeps during calculated part of specified duration</li>
     * <li>Repeats previous actions if element was not found and seconds timeout not expired</li>
     * </ul>
     * @param jsonPath A JSON path
     * @param resourceUrl Resource URL
     * @param duration Time duration to wait
     * @param retryTimes How many times request will be retried; duration/retryTimes=timeout between requests
     * @throws IOException If an input or output exception occurred
     * @deprecated Use <i>When I wait for presence of element by `$jsonPath` for `$duration` duration
     * retrying $retryTimes times$stepsToExecute</i>
     */
    @Deprecated(since = "0.2.0", forRemoval = true)
    @When("I wait for presence of element by '$jsonPath' in HTTP GET response from '$resourceUrl'"
            + " for '$duration' duration retrying $retryTimes times")
    public void waitForJsonFieldAppearance(String jsonPath, String resourceUrl, Duration duration,
            int retryTimes) throws IOException
    {
        httpRequestExecutor.executeHttpRequest(HttpMethod.GET, resourceUrl, Optional.empty(),
            response -> isJsonElementSearchCompleted(response, jsonPath), new WaitMode(duration, retryTimes));
        assertJsonElementExists(jsonPath);
    }

    /**
     * Waits for a specified amount of time until HTTP response body contains an element by the specified JSON path.
     * <p>
     * <b>Actions performed:</b>
     * </p>
     * <ul>
     * <li>Execute sub-steps</li>
     * <li>Check if HTTP response is present and response body contains an element by JSON path</li>
     * <li>Stop step execution if HTTP response is not present or JSON element is found, otherwise
     * sleep for the calculated part of specified duration and repeat actions from the start</li>
     * </ul>
     * @param jsonPath JSON path of element to find
     * @param duration Full duration of time to wait
     * @param retryTimes Number of attempts (duration/retryTimes is a sleep timeout between sub-steps execution)
     * @param stepsToExecute Steps to execute at each wait iteration
     */
    @When("I wait for presence of element by `$jsonPath` for `$duration` duration retrying $retryTimes times"
            + "$stepsToExecute")
    public void waitForJsonElement(String jsonPath, Duration duration, int retryTimes, SubSteps stepsToExecute)
    {
        waitForJsonElement(new DurationBasedWaiter(new WaitMode(duration, retryTimes)), jsonPath, stepsToExecute);
    }

    /**
     * Execute the provided sub-steps until the HTTP response body contains an element by the specified JSON path or
     * until the maximum number of retries is reached. The maximum duration of the step execution is not limited.
     * <p>
     * <b>The actions performed:</b>
     * </p>
     * <ul>
     * <li>execute sub-steps</li>
     * <li>wait for the polling interval</li>
     * <li>if the required JSON element exists or the maximum number of retries is reached, then execution stops,
     * otherwise the step actions are repeated</li>
     * </ul>
     * @param jsonPath the JSON path of the element to find
     * @param pollingInterval the duration to wait between retries
     * @param retryTimes the maximum number of the retries
     * @param stepsToExecute the sub-steps to execute at each iteration
     */
    @When("I wait for presence of element by `$jsonPath` with `$pollingInterval` polling interval "
            + "retrying $retryTimes times$stepsToExecute")
    public void waitForJsonElementWithPollingInterval(String jsonPath, Duration pollingInterval,
                                                      int retryTimes, SubSteps stepsToExecute)
    {
        waitForJsonElement(new RetryTimesBasedWaiter(pollingInterval, retryTimes), jsonPath, stepsToExecute);
    }

    private void waitForJsonElement(Waiter waiter, String jsonPath, SubSteps stepsToExecute)
    {
        waiter.wait(
            () -> stepsToExecute.execute(Optional.empty()),
            () -> isJsonElementSearchCompleted(httpTestContext.getResponse(), jsonPath)
        );
        assertJsonElementExists(jsonPath);
    }

    private boolean isJsonElementSearchCompleted(HttpResponse response, String jsonPath)
    {
        if (response == null)
        {
            return true;
        }
        String responseBody = response.getResponseBodyAsString();
        try
        {
            // Empty response may be in case of HTTP "204 NO CONTENT"
            return StringUtils.isNotEmpty(responseBody) && getElementsNumber(responseBody, jsonPath) > 0;
        }
        catch (InvalidJsonException ignored)
        {
            return false;
        }
    }

    private void assertJsonElementExists(String jsonPath)
    {
        HttpResponse response = httpTestContext.getResponse();
        if (response != null)
        {
            if (response.getResponseBody() != null)
            {
                doesJsonPathElementsMatchRule(jsonPath, ComparisonRule.GREATER_THAN, 0);
            }
            else
            {
                softAssert.recordFailedAssertion("HTTP response body is not present");
            }
        }
    }

    /**
     * Step designed to perform steps against all elements found by JSON path in current json context or response
     * <b>if</b> they are matching comparison rule.
     * Actions performed by step:
     * <ul>
     * <li>Searches for elements using JSON path</li>
     * <li>Checks that elements quantity matches comparison rule and elements number</li>
     * <li>For each element switches JSON context and performs all steps. No steps will be performed
     * in case of comparison rule mismatch</li>
     * <li>Restores previously set context</li>
     * </ul>
     * <br> Usage example:
     * <code>
     * <br>When I find equal to '1' JSON elements by '$.[?(@.parent_target_id=="")]' and for each element do
     * <br>|step|
     * <br>|Then the number of JSON elements by the JSON path '$..name' is = 3|
     * </code>
     * @param comparisonRule use to check elements quantity
     * @param elementsNumber The expected number of elements
     * @param jsonPath A JSON path
     * @param stepsToExecute examples table with steps to execute for each found elements
     */
    @SuppressWarnings("MagicNumber")
    @When(value = "I find $comparisonRule '$elementsNumber' JSON elements by '$jsonPath' and for each element do"
            + "$stepsToExecute", priority = 6)
    public void performAllStepsForJsonIfFound(ComparisonRule comparisonRule, int elementsNumber, String jsonPath,
            SubSteps stepsToExecute)
    {
        performAllStepsForProvidedJsonIfFound(comparisonRule, elementsNumber, getActualJson(), jsonPath,
                stepsToExecute);
    }

    /**
    * Step designed to perform steps against all elements found by JSON path in provided json
    * <b>if</b> they are matching comparison rule.
    * Actions performed by step:
    * <ul>
    * <li>Searches for elements using JSON path</li>
    * <li>Checks that elements quantity matches comparison rule and elements number</li>
    * <li>For each element switches JSON context and performs all steps. No steps will be performed
    * in case of comparison rule mismatch</li>
    * <li>Restores previously set context</li>
    * </ul>
    * <br> Usage example:
    * <code>
    * <br>When I find equal to `1` JSON elements from `{"parent_id":"","elements":[{"name": "1"},{"name": "2"}]}`
    *       by `$.[?(@.parent_id=="")]` and for each element do
    * <br>|step|
    * <br>|Then the number of JSON elements by the JSON path '$..name' is = 2|
    * </code>
    * @param comparisonRule use to check elements quantity
    * @param elementsNumber The expected number of elements
    * @param json A JSON element
    * @param jsonPath A JSON path
    * @param stepsToExecute examples table with steps to execute for each found elements
    */
    @When("I find $comparisonRule `$elementsNumber` JSON elements from `$json` by `$jsonPath` and for each element do"
            + "$stepsToExecute")
    public void performAllStepsForProvidedJsonIfFound(ComparisonRule comparisonRule, int elementsNumber, String json,
            String jsonPath, SubSteps stepsToExecute)
    {
        Optional<List<?>> jsonElements = getElements(json, jsonPath);
        if (assertJsonElementsNumber(jsonPath, countElementsNumber(jsonElements), comparisonRule, elementsNumber))
        {
            String jsonContext = getActualJson();
            jsonElements.get().stream().map(jsonUtils::toJson).forEach(jsonElement ->
            {
                httpTestContext.putJsonContext(jsonElement);
                stepsToExecute.execute(Optional.empty());
            });
            httpTestContext.putJsonContext(jsonContext);
        }
    }

    private Optional<String> getDataByJsonPath(String json, String jsonPath, String expectedData)
    {
        return getDataByJsonPathSafely(json, jsonPath).map(
            jsonByPath -> unwrapCollection(jsonByPath, expectedData)
            .orElseGet(() -> jsonUtils.toJson(jsonByPath.orElse(null))));
    }

    private <T> Optional<Optional<T>> getDataByJsonPathSafely(String json, String jsonPath)
    {
        return getDataByJsonPathSafely(json, jsonPath, true);
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

    private Optional<String> unwrapCollection(Optional<Object> jsonByPath, String expectedData)
    {
        Optional<String> actualJsonByPath = Optional.empty();
        if (jsonByPath.isEmpty() || !(jsonByPath.get() instanceof List) || expectedData == null)
        {
            return actualJsonByPath;
        }
        @SuppressWarnings("unchecked")
        List<Object> values = (List<Object>) jsonByPath.get();
        if (values.size() == 1)
        {
            String actualJsonElement = jsonUtils.toJson(values.get(0));
            if (jsonEquals(expectedData).matches(actualJsonElement))
            {
                return Optional.of(actualJsonElement);
            }
        }
        return actualJsonByPath;
    }

    private int getElementsNumber(String json, String jsonPath)
    {
        Optional<List<?>> elements = getElements(json, jsonPath);
        return countElementsNumber(elements);
    }

    private Optional<List<?>> getElements(String json, String jsonPath)
    {
        Optional<Optional<Object>> jsonObject = getDataByJsonPathSafely(json, jsonPath, false);
        return jsonObject.map(e -> e.map(value -> value instanceof List ? (List<?>) value : List.of(value))
                .orElseGet(() -> Collections.singletonList(null)));
    }

    private static int countElementsNumber(Optional<List<?>> elements)
    {
        return elements.map(List::size).orElse(0).intValue();
    }

    private boolean assertJsonElementsNumber(String jsonPath, int actualNumber, ComparisonRule comparisonRule,
            int expectedElementsNumber)
    {
        return softAssert.assertThat("The number of JSON elements by JSON path: " + jsonPath, actualNumber,
                comparisonRule.getComparisonRule(expectedElementsNumber));
    }

    private String getActualJson()
    {
        return httpTestContext.getJsonContext();
    }

    public void setSoftAssert(ISoftAssert softAssert)
    {
        this.softAssert = softAssert;
    }
}
