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

import static com.github.valfirst.slf4jtest.LoggingEvent.info;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.apache.commons.lang3.function.FailableSupplier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.context.VariableContext;
import org.vividus.softassert.ISoftAssert;
import org.vividus.testcontext.TestContext;
import org.vividus.util.wait.DurationBasedWaiter;
import org.vividus.variable.VariableScope;

import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClientBuilder;
import software.amazon.awssdk.services.cloudwatchlogs.model.FilterLogEventsRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.FilteredLogEvent;
import software.amazon.awssdk.services.cloudwatchlogs.paginators.FilterLogEventsIterable;

@ExtendWith({ MockitoExtension.class, TestLoggerFactoryExtension.class })
@SuppressWarnings({ "PMD.CloseResource", "PMD.CouplingBetweenObjects" })
class CloudWatchLogsStepsTests
{
    private static final TestLogger LOGGER = TestLoggerFactory.getTestLogger(CloudWatchLogsSteps.class);

    private static final String LOG_GROUP_NAME = "vividus-log-group";
    private static final String LOG_STREAM_NAME = "vividus-log-stream";
    private static final String FILTER_PATTERN = "ERROR";
    private static final String EMPTY_FILTER_PATTERN = "";
    private static final String EVENT_ID = "event-id";
    private static final String MESSAGE = "The connection is refused";
    private static final long TIMESTAMP = 1_700_000_000_000L;
    private static final String VARIABLE_NAME = "var-name";
    private static final Instant START_TIME = Instant.ofEpochMilli(1_699_999_000_000L);
    private static final Duration TIMEOUT = Duration.ofMinutes(1);
    private static final Duration POLLING_TIMEOUT = Duration.ofSeconds(5);
    private static final Set<VariableScope> SCOPES = Set.of(VariableScope.SCENARIO);
    private static final String FETCHED_EVENTS_LOG =
            "{} event(s) are fetched from the CloudWatch log group '{}' and log stream '{}'";

    @Mock private TestContext testContext;
    @Mock private VariableContext variableContext;
    @Mock private ISoftAssert softAssert;

    @Test
    void shouldStartListeningForLogEvents()
    {
        CloudWatchLogsSteps steps = new CloudWatchLogsSteps(testContext, variableContext, softAssert);
        Instant beforeCall = Instant.now();

        steps.startListeningForLogEvents();

        ArgumentCaptor<Object> keyCaptor = ArgumentCaptor.forClass(Object.class);
        ArgumentCaptor<Instant> startTimeCaptor = ArgumentCaptor.forClass(Instant.class);
        verify(testContext).put(keyCaptor.capture(), startTimeCaptor.capture());
        Instant capturedStartTime = startTimeCaptor.getValue();
        assertThat(capturedStartTime, greaterThanOrEqualTo(beforeCall));
        assertThat(LOGGER.getLoggingEvents(),
                equalTo(List.of(info("Started listening for the CloudWatch log events at {}", capturedStartTime))));
    }

    @Test
    void shouldFailWaitingWhenListeningHasNotBeenStarted()
    {
        CloudWatchLogsSteps steps = new CloudWatchLogsSteps(testContext, variableContext, softAssert);
        when(testContext.get(any(), eq(Instant.class))).thenReturn(null);

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> steps.waitForLogEvents(TIMEOUT, POLLING_TIMEOUT, FILTER_PATTERN, LOG_GROUP_NAME,
                        LOG_STREAM_NAME, SCOPES, VARIABLE_NAME));

        assertEquals("The listening for the CloudWatch log events has not been started, use step `When I start"
                + " listening for CloudWatch log events` first", exception.getMessage());
        verifyNoInteractions(variableContext, softAssert);
    }

    @Test
    void shouldWaitForLogEventsMatchingFilterPattern()
    {
        runTest((logsClient, steps) ->
        {
            ArgumentCaptor<FilterLogEventsRequest> requestCaptor = mockEvents(logsClient, event());

            steps.waitForLogEvents(TIMEOUT, POLLING_TIMEOUT, FILTER_PATTERN, LOG_GROUP_NAME, LOG_STREAM_NAME,
                    SCOPES, VARIABLE_NAME);

            FilterLogEventsRequest request = requestCaptor.getValue();
            assertAll(
                    () -> assertEquals(LOG_GROUP_NAME, request.logGroupName()),
                    () -> assertEquals(List.of(LOG_STREAM_NAME), request.logStreamNames()),
                    () -> assertEquals(FILTER_PATTERN, request.filterPattern()),
                    () -> assertEquals(Boolean.FALSE, request.startFromHead()),
                    () -> assertEquals(START_TIME.toEpochMilli(), request.startTime()));
            verify(variableContext).putVariable(SCOPES, VARIABLE_NAME, List.of(expectedEvent()));
            verifyNoInteractions(softAssert);
            assertThat(LOGGER.getLoggingEvents(),
                    equalTo(List.of(info(FETCHED_EVENTS_LOG, 1, LOG_GROUP_NAME, LOG_STREAM_NAME))));
        });
    }

    @Test
    void shouldNotApplyFilterPatternWhenItIsEmpty()
    {
        runTest((logsClient, steps) ->
        {
            ArgumentCaptor<FilterLogEventsRequest> requestCaptor = mockEvents(logsClient, event());

            steps.waitForLogEvents(TIMEOUT, POLLING_TIMEOUT, EMPTY_FILTER_PATTERN, LOG_GROUP_NAME, LOG_STREAM_NAME,
                    SCOPES, VARIABLE_NAME);

            assertNull(requestCaptor.getValue().filterPattern());
            verify(variableContext).putVariable(SCOPES, VARIABLE_NAME, List.of(expectedEvent()));
        });
    }

    @Test
    void shouldRecordFailedAssertionWhenNoLogEventsAppear()
    {
        runTest((logsClient, steps) ->
        {
            mockEvents(logsClient);

            steps.waitForLogEvents(TIMEOUT, POLLING_TIMEOUT, FILTER_PATTERN, LOG_GROUP_NAME, LOG_STREAM_NAME,
                    SCOPES, VARIABLE_NAME);

            verify(softAssert).recordFailedAssertion("No events matching `ERROR` pattern appeared in the"
                    + " CloudWatch log group 'vividus-log-group' and log stream 'vividus-log-stream' within PT1M");
            verifyNoInteractions(variableContext);
            assertThat(LOGGER.getLoggingEvents(),
                    equalTo(List.of(info(FETCHED_EVENTS_LOG, 0, LOG_GROUP_NAME, LOG_STREAM_NAME))));
        });
    }

    private static ArgumentCaptor<FilterLogEventsRequest> mockEvents(CloudWatchLogsClient logsClient,
            FilteredLogEvent... events)
    {
        ArgumentCaptor<FilterLogEventsRequest> requestCaptor = ArgumentCaptor.forClass(FilterLogEventsRequest.class);
        FilterLogEventsIterable pages = mock();
        SdkIterable<FilteredLogEvent> iterable = List.of(events)::iterator;
        when(pages.events()).thenReturn(iterable);
        when(logsClient.filterLogEventsPaginator(requestCaptor.capture())).thenReturn(pages);
        return requestCaptor;
    }

    private static FilteredLogEvent event()
    {
        return FilteredLogEvent.builder()
                .eventId(EVENT_ID)
                .logStreamName(LOG_STREAM_NAME)
                .timestamp(TIMESTAMP)
                .message(MESSAGE)
                .build();
    }

    private static Map<String, Object> expectedEvent()
    {
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("eventId", EVENT_ID);
        event.put("timestamp", Instant.ofEpochMilli(TIMESTAMP));
        event.put("message", MESSAGE);
        return event;
    }

    @SuppressWarnings("unchecked")
    private static MockedConstruction<DurationBasedWaiter> mockWaiter(List<List<?>> constructorArguments)
    {
        return mockConstruction(DurationBasedWaiter.class, (waiter, context) ->
        {
            constructorArguments.add(context.arguments());
            when(waiter.wait(any(FailableSupplier.class), any(Predicate.class))).thenAnswer(invocation ->
            {
                FailableSupplier<Object, Exception> valueProvider = invocation.getArgument(0);
                Predicate<Object> stopCondition = invocation.getArgument(1);
                Object value = valueProvider.get();
                stopCondition.test(value);
                return value;
            });
        });
    }

    private void runTest(CloudWatchTest test)
    {
        List<List<?>> waiterArguments = new ArrayList<>();
        try (var clientStatic = mockStatic(CloudWatchLogsClient.class);
                MockedConstruction<DurationBasedWaiter> waiter = mockWaiter(waiterArguments))
        {
            CloudWatchLogsClientBuilder builder = mock();
            CloudWatchLogsClient logsClient = mock();
            clientStatic.when(CloudWatchLogsClient::builder).thenReturn(builder);
            when(builder.build()).thenReturn(logsClient);
            when(testContext.get(any(), eq(Instant.class))).thenReturn(START_TIME);

            CloudWatchLogsSteps steps = new CloudWatchLogsSteps(testContext, variableContext, softAssert);

            test.run(logsClient, steps);

            assertEquals(1, waiter.constructed().size());
            assertEquals(List.of(TIMEOUT, POLLING_TIMEOUT), waiterArguments.get(0));
            verify(logsClient).close();
        }
    }

    @FunctionalInterface
    private interface CloudWatchTest
    {
        void run(CloudWatchLogsClient logsClient, CloudWatchLogsSteps steps);
    }
}
