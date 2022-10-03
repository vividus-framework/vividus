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

package org.vividus.steps.api;

import java.time.Duration;
import java.util.Optional;

import com.jayway.jsonpath.InvalidJsonException;

import org.apache.commons.lang3.StringUtils;
import org.jbehave.core.annotations.When;
import org.vividus.http.HttpTestContext;
import org.vividus.http.client.HttpResponse;
import org.vividus.json.steps.JsonSteps;
import org.vividus.softassert.ISoftAssert;
import org.vividus.steps.ComparisonRule;
import org.vividus.steps.SubSteps;
import org.vividus.util.wait.DurationBasedWaiter;
import org.vividus.util.wait.RetryTimesBasedWaiter;
import org.vividus.util.wait.WaitMode;
import org.vividus.util.wait.Waiter;

public class JsonResponseValidationSteps
{
    private final HttpTestContext httpTestContext;
    private final ISoftAssert softAssert;
    private final JsonSteps jsonSteps;

    public JsonResponseValidationSteps(HttpTestContext httpTestContext, JsonSteps jsonSteps, ISoftAssert softAssert)
    {
        this.httpTestContext = httpTestContext;
        this.softAssert = softAssert;
        this.jsonSteps = jsonSteps;
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
    @When("I wait for presence of element by `$jsonPath` with `$pollingInterval` polling interval retrying $retryTimes "
            + "times$stepsToExecute")
    public void waitForJsonElementWithPollingInterval(String jsonPath, Duration pollingInterval, int retryTimes,
            SubSteps stepsToExecute)
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
            return StringUtils.isNotEmpty(responseBody) && jsonSteps.getElementsNumber(responseBody, jsonPath) > 0;
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
                jsonSteps.assertNumberOfJsonElements(response.getResponseBodyAsString(), jsonPath,
                        ComparisonRule.GREATER_THAN, 0);
            }
            else
            {
                softAssert.recordFailedAssertion("HTTP response body is not present");
            }
        }
    }
}
