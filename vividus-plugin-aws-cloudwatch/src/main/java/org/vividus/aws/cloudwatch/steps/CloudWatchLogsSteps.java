/*
 * Copyright 2019-2026 the original author or authors.
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

package org.vividus.aws.cloudwatch.steps;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.Validate;
import org.jbehave.core.annotations.When;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.context.VariableContext;
import org.vividus.softassert.ISoftAssert;
import org.vividus.testcontext.TestContext;
import org.vividus.util.wait.DurationBasedWaiter;
import org.vividus.variable.VariableScope;

import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.FilterLogEventsRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.FilteredLogEvent;

public class CloudWatchLogsSteps
{
    private static final Logger LOGGER = LoggerFactory.getLogger(CloudWatchLogsSteps.class);
    private static final Object LISTENING_START_TIME_KEY = CloudWatchLogsSteps.class;

    private final TestContext testContext;
    private final VariableContext variableContext;
    private final ISoftAssert softAssert;

    public CloudWatchLogsSteps(TestContext testContext, VariableContext variableContext, ISoftAssert softAssert)
    {
        this.testContext = testContext;
        this.variableContext = variableContext;
        this.softAssert = softAssert;
    }

    /**
     * Starts listening for the Amazon CloudWatch log events by capturing the current moment in time. The captured
     * moment is later used as the start of the time range by the step
     * <code>When I wait `$duration` with `$pollingDuration` polling until at least one event matching `$pattern`
     * pattern appears in CloudWatch log group `$logGroupName` and log stream `$logStreamName` and save them to
     * $scopes variable `$variableName`</code>.
     * <p>
     * The step is useful when the moment the events of interest may start appearing is not known upfront, e.g. the
     * events are triggered by some business actions performed after this step.
     * </p>
     */
    @When("I start listening for CloudWatch log events")
    public void startListeningForLogEvents()
    {
        Instant now = Instant.now();
        testContext.put(LISTENING_START_TIME_KEY, now);
        LOGGER.info("Started listening for the CloudWatch log events at {}", now);
    }

    /**
     * Polls the specified log stream of the specified Amazon CloudWatch log group until at least one log event
     * matching the filter pattern is found since the listening was started or the timeout expires, and then saves
     * all the found events to the variable. The step is failed if no matching event appears within the timeout.
     * <p>
     * The listening is started by the step <code>When I start listening for CloudWatch log events</code>, which
     * must be called before this step. The time range grows on each polling attempt from the moment the listening
     * was started up to the current moment. This handles the Amazon CloudWatch ingestion delay: the events produced
     * right before the step execution are picked up as soon as they become available.
     * </p>
     * The events are matched against the filter pattern described at
     * <a href="https://docs.aws.amazon.com/AmazonCloudWatch/latest/logs/FilterAndPatternSyntax.html">
     * Filter and pattern syntax</a>.
     *
     * @param duration        The maximum time to wait for the events, in ISO-8601 duration format.
     * @param pollingDuration The interval between the polling attempts, in ISO-8601 duration format.
     * @param pattern         The filter pattern to match the events against, if it's empty any event since the
     *                        listening was started is matched.
     * @param logGroupName    The name of the Amazon CloudWatch log group to fetch the events from.
     * @param logStreamName   The name of the Amazon CloudWatch log stream to fetch the events from.
     * @param scopes          The set (comma separated list of scopes e.g.: STORY, NEXT_BATCHES) of the variable's
     *                        scope<br>
     *                        <i>Available scopes:</i>
     *                        <ul>
     *                        <li><b>STEP</b> - the variable will be available only within the step,
     *                        <li><b>SCENARIO</b> - the variable will be available only within the scenario,
     *                        <li><b>STORY</b> - the variable will be available within the whole story,
     *                        <li><b>NEXT_BATCHES</b> - the variable will be available starting from next batch
     *                        </ul>
     * @param variableName    The name of the variable to store the events. The events are sorted from the newest to
     *                        the oldest and are accessible via zero-based index and the event key, e.g.
     *                        <code>${my-var[0].message}</code> returns the message of the most recent found event.
     *                        The available keys are: <code>eventId</code>, <code>timestamp</code>,
     *                        <code>message</code>.
     */
    @When("I wait `$duration` with `$pollingDuration` polling until at least one event matching `$pattern` pattern"
            + " appears in CloudWatch log group `$logGroupName` and log stream `$logStreamName` and save them to"
            + " $scopes variable `$variableName`")
    @SuppressWarnings("PMD.UseObjectForClearerAPI")
    public void waitForLogEvents(Duration duration, Duration pollingDuration, String pattern, String logGroupName,
            String logStreamName, Set<VariableScope> scopes, String variableName)
    {
        Instant startTime = testContext.get(LISTENING_START_TIME_KEY, Instant.class);
        Validate.validState(startTime != null, "The listening for the CloudWatch log events has not been started,"
                + " use step `When I start listening for CloudWatch log events` first");

        try (CloudWatchLogsClient logsClient = CloudWatchLogsClient.builder().build())
        {
            List<FilteredLogEvent> events = new DurationBasedWaiter(duration, pollingDuration).wait(
                    () -> filterLogEvents(logsClient, logGroupName, logStreamName, pattern, startTime),
                    found -> !found.isEmpty());

            if (events.isEmpty())
            {
                softAssert.recordFailedAssertion(
                        ("No events matching `%s` pattern appeared in the CloudWatch log group '%s' and log stream"
                                + " '%s' within %s").formatted(pattern, logGroupName, logStreamName, duration));
            }
            else
            {
                variableContext.putVariable(scopes, variableName,
                        events.stream().map(CloudWatchLogsSteps::asMap).toList());
            }
        }
    }

    private List<FilteredLogEvent> filterLogEvents(CloudWatchLogsClient logsClient, String logGroupName,
            String logStreamName, String pattern, Instant startTime)
    {
        FilterLogEventsRequest.Builder request = FilterLogEventsRequest.builder()
                .logGroupName(logGroupName)
                .logStreamNames(logStreamName)
                .startTime(startTime.toEpochMilli())
                .endTime(Instant.now().toEpochMilli())
                .startFromHead(false);
        if (!pattern.isEmpty())
        {
            request.filterPattern(pattern);
        }

        List<FilteredLogEvent> found = logsClient.filterLogEventsPaginator(request.build()).events().stream()
                .toList();

        LOGGER.atInfo()
                .addArgument(found::size)
                .addArgument(logGroupName)
                .addArgument(logStreamName)
                .log("{} event(s) are fetched from the CloudWatch log group '{}' and log stream '{}'");
        return found;
    }

    private static Map<String, Object> asMap(FilteredLogEvent event)
    {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("eventId", event.eventId());
        data.put("timestamp", Instant.ofEpochMilli(event.timestamp()));
        data.put("message", event.message());
        return data;
    }
}
