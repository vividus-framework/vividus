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

package org.vividus.newrelic;

import static com.github.valfirst.slf4jtest.LoggingEvent.warn;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import com.github.valfirst.slf4jtest.LoggingEvent;
import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.jbehave.core.embedder.StoryControls;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.batch.BatchConfiguration;
import org.vividus.context.RunContext;
import org.vividus.model.RunningBatch;
import org.vividus.model.RunningStory;
import org.vividus.newrelic.model.NewRelicEvent;
import org.vividus.newrelic.model.RunPhase;
import org.vividus.newrelic.model.Status;
import org.vividus.newrelic.model.VividusRunEvent;
import org.vividus.newrelic.model.VividusStoryEvent;
import org.vividus.util.property.IPropertyParser;

@ExtendWith({ MockitoExtension.class, TestLoggerFactoryExtension.class })
class NewRelicStepsTests
{
    private static final TestLogger LOGGER = TestLoggerFactory.getTestLogger(NewRelicSteps.class);
    private static final String ATTRIBUTE_PREFIX = "newrelic.event.attribute.";
    private static final String EVENT_TYPE = "eventType";
    private static final String RUN_ID = "runId";
    private static final String STORY_NAME = "storyName";
    private static final String PHASE = "phase";
    private static final String STATUS = "status";
    private static final String ENVIRONMENT = "environment";
    private static final String STAGING = "staging";
    private static final String DROPPED = "dropped";
    private static final String RUN_EVENT = "VividusRun";
    private static final String TEAM_NAME = "team_name";
    private static final String TEAM_WITH_LEADING_DIGIT = "1team";
    private static final String HYPHENATED_TEAM_NAME = "team-name";
    private static final String LONG_ATTRIBUTE_NAME = "a".repeat(256);
    private static final String QA = "qa";
    private static final String STORY_EVENT = "VividusStory";
    private static final String LOGIN = "Login";
    private static final String BATCH_KEY = "batch-2";
    private static final int BATCH_INDEX = 2;
    private static final String BATCH_INDEX_ATTRIBUTE = "batchIndex";
    private static final String PROVIDED_RUN_ID = "build-42";
    private static final String BLANK_RUN_ID = " ";
    private static final String RESERVED_ATTRIBUTE =
            "Skipping New Relic custom attribute '{}' because its name is reserved";
    private static final String INVALID_ATTRIBUTE =
            "Skipping New Relic custom attribute '{}' because its name violates New Relic attribute rules";

    @Mock private NewRelicEventPublisher publisher;
    @Mock private RunContext runContext;
    @Mock private IPropertyParser propertyParser;
    @Mock private StoryControls storyControls;
    @Mock private RunningStory runningStory;
    @Mock private RunningStory givenRunningStory;
    @Captor private ArgumentCaptor<NewRelicEvent> eventCaptor;
    private NewRelicSteps steps;

    @BeforeEach
    void setUp()
    {
        when(propertyParser.getPropertyValuesByPrefix(ATTRIBUTE_PREFIX)).thenReturn(Map.of());
        steps = createSteps(false, null);
    }

    private NewRelicSteps createSteps(boolean enabled, String runId)
    {
        return createSteps(enabled, runId, Duration.ofMinutes(1));
    }

    private NewRelicSteps createSteps(boolean enabled, String runId, Duration publishTimeout)
    {
        return new NewRelicSteps(publisher, runContext, propertyParser, storyControls, enabled, runId, publishTimeout);
    }

    @Test
    void shouldNotSendWhenDisabled()
    {
        steps.beforeStories();
        steps.afterStory();
        steps.afterGivenStory();
        steps.afterStories();

        verifyNoInteractions(publisher);
    }

    @Test
    void shouldNotSendRunEventWhenDryRun()
    {
        when(storyControls.dryRun()).thenReturn(true);
        steps = createSteps(true, null);

        steps.beforeStories();
        steps.afterStory();
        steps.afterGivenStory();
        steps.afterStories();

        verifyNoInteractions(publisher);
    }

    @Test
    void shouldSendRunEndAfterStories()
    {
        steps = createSteps(true, PROVIDED_RUN_ID);

        steps.beforeStories();
        steps.afterStories();

        verify(publisher, times(2)).postEvent(eventCaptor.capture());
        VividusRunEvent startEvent = assertInstanceOf(VividusRunEvent.class, eventCaptor.getAllValues().get(0));
        VividusRunEvent endEvent = assertInstanceOf(VividusRunEvent.class, eventCaptor.getAllValues().get(1));
        assertEquals(RunPhase.STARTED, startEvent.getPhase());
        assertNull(startEvent.getStatus());
        assertEquals(RunPhase.FINISHED, endEvent.getPhase());
        assertEquals(Status.PASSED, endEvent.getStatus());
        assertEquals(RUN_EVENT, endEvent.getEventType());
        assertEquals(PROVIDED_RUN_ID, startEvent.getRunId());
        assertEquals(PROVIDED_RUN_ID, endEvent.getRunId());
    }

    @Test
    void shouldMarkFinishedRunFailedWhenStoryFailed()
    {
        stubReporting(true);

        steps.beforeStories();
        steps.afterStory();
        steps.afterStories();

        verify(publisher, times(3)).postEvent(eventCaptor.capture());
        VividusRunEvent finishedRun = eventCaptor.getAllValues().stream()
                .filter(VividusRunEvent.class::isInstance)
                .map(VividusRunEvent.class::cast)
                .filter(event -> event.getPhase() == RunPhase.FINISHED)
                .findFirst()
                .orElseThrow();
        assertEquals(Status.FAILED, finishedRun.getStatus());
    }

    @Test
    void shouldSendRunEventBeforeStories()
    {
        when(propertyParser.getPropertyValuesByPrefix(ATTRIBUTE_PREFIX)).thenReturn(customAttributes());
        steps = createSteps(true, null);

        steps.beforeStories();

        verify(publisher).postEvent(eventCaptor.capture());
        VividusRunEvent event = assertInstanceOf(VividusRunEvent.class, eventCaptor.getValue());
        assertEquals(RUN_EVENT, event.getEventType());
        assertEquals(RunPhase.STARTED, event.getPhase());
        assertNull(event.getStatus());
        assertInstanceOf(String.class, event.getRunId());
        assertEquals(Map.of(ENVIRONMENT, STAGING, TEAM_NAME, QA, TEAM_WITH_LEADING_DIGIT, DROPPED),
                event.getAttributes());
        assertEquals(List.of(warn(INVALID_ATTRIBUTE, LONG_ATTRIBUTE_NAME),
                warn(RESERVED_ATTRIBUTE, BATCH_INDEX_ATTRIBUTE), warn(RESERVED_ATTRIBUTE, EVENT_TYPE),
                warn(RESERVED_ATTRIBUTE, PHASE), warn(RESERVED_ATTRIBUTE, RUN_ID), warn(RESERVED_ATTRIBUTE, STATUS),
                warn(RESERVED_ATTRIBUTE, STORY_NAME), warn(INVALID_ATTRIBUTE, HYPHENATED_TEAM_NAME)),
                LOGGER.getLoggingEvents());
    }

    @Test
    void shouldUseProvidedRunId()
    {
        steps = createSteps(true, PROVIDED_RUN_ID);

        steps.beforeStories();

        verify(publisher).postEvent(eventCaptor.capture());
        assertEquals(PROVIDED_RUN_ID, eventCaptor.getValue().getRunId());
    }

    @Test
    void shouldGenerateRunIdWhenProvidedValueIsBlank()
    {
        steps = createSteps(true, BLANK_RUN_ID);

        steps.beforeStories();

        verify(publisher).postEvent(eventCaptor.capture());
        assertNotEquals(BLANK_RUN_ID, eventCaptor.getValue().getRunId());
    }

    static Stream<Arguments> storyStatuses()
    {
        return Stream.of(
                arguments(true, Status.FAILED),
                arguments(false, Status.PASSED)
        );
    }

    @ParameterizedTest
    @MethodSource("storyStatuses")
    void shouldSendStoryStatus(boolean failed, Status expectedStatus)
    {
        stubReporting(failed);

        steps.beforeStories();
        steps.afterStory();
        steps.afterStories();

        verify(publisher, times(3)).postEvent(eventCaptor.capture());
        assertStoryEvent(eventCaptor.getAllValues().get(0), eventCaptor.getAllValues().get(1), expectedStatus);
    }

    @Test
    void shouldNotSendAfterStoriesWhenDisabled()
    {
        steps.afterStories();

        verifyNoInteractions(publisher);
    }

    @Test
    void shouldWaitForStoryEventsInAfterStories() throws InterruptedException
    {
        CountDownLatch postStarted = new CountDownLatch(1);
        CountDownLatch allowPost = new CountDownLatch(1);
        doAnswer(invocation -> {
            if (invocation.getArgument(0) instanceof VividusStoryEvent)
            {
                postStarted.countDown();
                assertTrue(allowPost.await(10, TimeUnit.SECONDS));
            }
            return null;
        }).when(publisher).postEvent(any());
        stubReporting(false);

        steps.beforeStories();
        steps.afterStory();
        assertTrue(postStarted.await(10, TimeUnit.SECONDS));

        CountDownLatch afterStoriesReturned = new CountDownLatch(1);
        Thread awaitThread = new Thread(() -> {
            steps.afterStories();
            afterStoriesReturned.countDown();
        });
        try
        {
            awaitThread.start();
            assertFalse(afterStoriesReturned.await(300, TimeUnit.MILLISECONDS));
            allowPost.countDown();
            assertTrue(afterStoriesReturned.await(10, TimeUnit.SECONDS));
        }
        finally
        {
            allowPost.countDown();
        }
    }

    @Test
    void shouldStopWaitingForStoryEventsAfterTimeout()
    {
        CountDownLatch release = new CountDownLatch(1);
        doAnswer(invocation -> {
            if (invocation.getArgument(0) instanceof VividusStoryEvent)
            {
                release.await(10, TimeUnit.SECONDS);
            }
            return null;
        }).when(publisher).postEvent(any());
        Duration timeout = Duration.ofMillis(200);
        stubReporting(false, timeout);

        try
        {
            steps.beforeStories();
            steps.afterStory();
            steps.afterStories();
        }
        finally
        {
            release.countDown();
        }

        LoggingEvent loggingEvent = LOGGER.getLoggingEvents().get(0);
        assertEquals("Timed out after {} waiting for New Relic story events", loggingEvent.getMessage());
        assertEquals(List.of(timeout), loggingEvent.getArguments());
        assertInstanceOf(TimeoutException.class, loggingEvent.getThrowable().orElseThrow());
    }

    @Test
    void shouldLogWhenWaitingForStoryEventsIsInterrupted() throws InterruptedException
    {
        CountDownLatch postStarted = new CountDownLatch(1);
        CountDownLatch allowPost = new CountDownLatch(1);
        doAnswer(invocation -> {
            if (invocation.getArgument(0) instanceof VividusStoryEvent)
            {
                postStarted.countDown();
                assertTrue(allowPost.await(10, TimeUnit.SECONDS));
            }
            return null;
        }).when(publisher).postEvent(any());
        stubReporting(false);

        steps.beforeStories();
        steps.afterStory();
        assertTrue(postStarted.await(10, TimeUnit.SECONDS));

        Thread testThread = Thread.currentThread();
        AtomicBoolean watch = new AtomicBoolean(true);
        Thread interrupter = new Thread(() -> {
            while (watch.get() && testThread.getState() != Thread.State.TIMED_WAITING)
            {
                Thread.onSpinWait();
            }
            if (watch.get())
            {
                testThread.interrupt();
            }
        });
        interrupter.start();
        try
        {
            steps.afterStories();
        }
        finally
        {
            watch.set(false);
            allowPost.countDown();
        }

        assertTrue(Thread.interrupted());
        interrupter.join(TimeUnit.SECONDS.toMillis(10));
        assertFalse(interrupter.isAlive());
        LoggingEvent loggingEvent = LOGGER.getLoggingEvents().get(0);
        assertEquals("Interrupted while waiting for New Relic story events", loggingEvent.getMessage());
        assertInstanceOf(InterruptedException.class, loggingEvent.getThrowable().orElseThrow());
    }

    @Test
    void shouldLogWhenStoryEventPublishingFails()
    {
        RuntimeException failure = new RuntimeException("post failed");
        doAnswer(invocation -> {
            if (invocation.getArgument(0) instanceof VividusStoryEvent)
            {
                throw failure;
            }
            return null;
        }).when(publisher).postEvent(any());
        stubReporting(false);

        steps.beforeStories();
        steps.afterStory();
        steps.afterStories();

        LoggingEvent loggingEvent = LOGGER.getLoggingEvents().get(0);
        assertEquals("Unable to send New Relic story events", loggingEvent.getMessage());
        ExecutionException executionException = assertInstanceOf(ExecutionException.class,
                loggingEvent.getThrowable().orElseThrow());
        assertEquals(failure, executionException.getCause());
    }

    private void stubReporting(boolean failed)
    {
        stubReporting(failed, Duration.ofMinutes(1));
    }

    private void stubReporting(boolean failed, Duration publishTimeout)
    {
        when(propertyParser.getPropertyValuesByPrefix(ATTRIBUTE_PREFIX)).thenReturn(Map.of(ENVIRONMENT, STAGING));
        steps = createSteps(true, null, publishTimeout);
        when(runContext.getRootRunningStory()).thenReturn(runningStory);
        when(runningStory.isNotExcluded()).thenReturn(true);
        when(runningStory.isFailed()).thenReturn(failed);
        when(runningStory.getName()).thenReturn(LOGIN);
        when(runContext.getRunningBatch()).thenReturn(new RunningBatch(BATCH_KEY, new BatchConfiguration()));
    }

    private static void assertStoryEvent(NewRelicEvent runEvent, NewRelicEvent storyEvent, Status expectedStatus)
    {
        VividusRunEvent run = assertInstanceOf(VividusRunEvent.class, runEvent);
        VividusStoryEvent story = assertInstanceOf(VividusStoryEvent.class, storyEvent);
        assertEquals(RUN_EVENT, run.getEventType());
        assertEquals(STORY_EVENT, story.getEventType());
        assertEquals(run.getRunId(), story.getRunId());
        assertEquals(LOGIN, story.getStoryName());
        assertEquals(BATCH_INDEX, story.getBatchIndex());
        assertEquals(expectedStatus, story.getStatus());
        assertEquals(Map.of(ENVIRONMENT, STAGING), story.getAttributes());
    }

    @Test
    void shouldNotSendEventForGivenStory()
    {
        startRun();
        when(runContext.getRunningStory()).thenReturn(givenRunningStory);

        steps.afterGivenStory();

        verify(publisher).postEvent(any());
    }

    @Test
    void shouldNotSendEventForExcludedStory()
    {
        when(propertyParser.getPropertyValuesByPrefix(ATTRIBUTE_PREFIX)).thenReturn(Map.of());
        steps = createSteps(true, null);
        when(runContext.getRootRunningStory()).thenReturn(runningStory);

        steps.beforeStories();
        steps.afterStory();
        steps.afterStories();

        verify(publisher, times(2)).postEvent(eventCaptor.capture());
        assertTrue(eventCaptor.getAllValues().stream().allMatch(VividusRunEvent.class::isInstance));
        VividusRunEvent finishedRun = assertInstanceOf(VividusRunEvent.class, eventCaptor.getAllValues().get(1));
        assertEquals(RunPhase.FINISHED, finishedRun.getPhase());
        assertEquals(Status.PASSED, finishedRun.getStatus());
    }

    @Test
    void shouldMarkRootAndRunFailedWhenGivenStoryFailed()
    {
        when(propertyParser.getPropertyValuesByPrefix(ATTRIBUTE_PREFIX)).thenReturn(Map.of(ENVIRONMENT, STAGING));
        steps = createSteps(true, null);
        when(runContext.getRunningStory()).thenReturn(givenRunningStory);
        when(givenRunningStory.isFailed()).thenReturn(true);
        when(runContext.getRootRunningStory()).thenReturn(runningStory);
        when(runningStory.isNotExcluded()).thenReturn(true);
        when(runningStory.isFailed()).thenReturn(false);
        when(runningStory.getName()).thenReturn(LOGIN);
        when(runContext.getRunningBatch()).thenReturn(new RunningBatch(BATCH_KEY, new BatchConfiguration()));

        steps.beforeStories();
        steps.afterGivenStory();
        steps.afterStory();
        steps.afterStories();

        verify(publisher, times(3)).postEvent(eventCaptor.capture());
        VividusStoryEvent story = eventCaptor.getAllValues().stream()
                .filter(VividusStoryEvent.class::isInstance)
                .map(VividusStoryEvent.class::cast)
                .findFirst()
                .orElseThrow();
        VividusRunEvent finishedRun = eventCaptor.getAllValues().stream()
                .filter(VividusRunEvent.class::isInstance)
                .map(VividusRunEvent.class::cast)
                .filter(event -> event.getPhase() == RunPhase.FINISHED)
                .findFirst()
                .orElseThrow();
        assertEquals(LOGIN, story.getStoryName());
        assertEquals(Status.FAILED, story.getStatus());
        assertEquals(Status.FAILED, finishedRun.getStatus());
        verify(publisher, times(2)).postEvent(any(VividusRunEvent.class));
    }

    private void startRun()
    {
        steps = createSteps(true, null);
        steps.beforeStories();
    }

    private static Map<String, String> customAttributes()
    {
        Map<String, String> attributes = new HashMap<>();
        attributes.put(LONG_ATTRIBUTE_NAME, DROPPED);
        attributes.put(ENVIRONMENT, STAGING);
        attributes.put(TEAM_NAME, QA);
        attributes.put(TEAM_WITH_LEADING_DIGIT, DROPPED);
        attributes.put(HYPHENATED_TEAM_NAME, DROPPED);
        attributes.put(EVENT_TYPE, DROPPED);
        attributes.put(RUN_ID, DROPPED);
        attributes.put(STORY_NAME, DROPPED);
        attributes.put(BATCH_INDEX_ATTRIBUTE, DROPPED);
        attributes.put(PHASE, DROPPED);
        attributes.put(STATUS, DROPPED);
        attributes.put("empty", null);
        return attributes;
    }
}
