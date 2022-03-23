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

package org.vividus;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.github.valfirst.slf4jtest.LoggingEvent;
import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;
import com.google.common.eventbus.EventBus;

import org.jbehave.core.failures.BeforeOrAfterFailed;
import org.jbehave.core.failures.UUIDExceptionWrapper;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Step;
import org.jbehave.core.model.Story;
import org.jbehave.core.steps.StepCollector.Stage;
import org.jbehave.core.steps.StepCreator.StepExecutionType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.context.ReportControlContext;
import org.vividus.context.RunContext;
import org.vividus.model.Failure;
import org.vividus.model.NodeType;
import org.vividus.model.RunningScenario;
import org.vividus.model.RunningStory;
import org.vividus.model.Statistic;
import org.vividus.softassert.event.AssertionFailedEvent;
import org.vividus.softassert.exception.VerificationError;
import org.vividus.softassert.model.KnownIssue;
import org.vividus.softassert.model.SoftAssertionError;
import org.vividus.testcontext.SimpleTestContext;
import org.vividus.testcontext.ThreadedTestContext;
import org.vividus.util.json.JsonUtils;

@ExtendWith({ MockitoExtension.class, TestLoggerFactoryExtension.class })
@SuppressWarnings({ "MultipleStringLiterals", "MultipleStringLiteralsExtended", "PMD.AvoidDuplicateLiterals"})
class StatisticsStoryReporterTests
{
    private static final String STEP_AS_STRING = "step";
    private static final Step STEP = new Step(StepExecutionType.EXECUTABLE, STEP_AS_STRING);
    private static final String ASSERTION_STEP = "assertionPassed";

    private static final TestLogger LOGGER = TestLoggerFactory.getTestLogger(StatisticsStoryReporter.class);

    @Mock private EventBus eventBus;
    @Mock private RunContext runContext;
    private final ReportControlContext reportControlContext = new ReportControlContext(new SimpleTestContext());

    private StatisticsStoryReporter reporter;

    private final Story givenStory = mockStory("GivenStory");
    private final Story story = mockStory("Story");
    private final Scenario scenario = new Scenario();

    @BeforeEach
    void init()
    {
        reporter = new StatisticsStoryReporter(reportControlContext, runContext, eventBus, new ThreadedTestContext(),
                new JsonUtils());
        reporter.init();

        reportControlContext.enableReporting();
        when(runContext.isRunCompleted()).thenReturn(false);
    }

    @Test
    void shouldSaveStatistics(@TempDir Path tempDirectory) throws IOException
    {
        reporter.setStatisticsFolder(tempDirectory.toFile());
        reporterFlowProvider();

        String output = readStatistics(tempDirectory);
        String expected = "{\n"
                + "  \"STORY\" : {\n"
                + "    \"total\" : 1,\n"
                + "    \"passed\" : 0,\n"
                + "    \"failed\" : 0,\n"
                + "    \"broken\" : 1,\n"
                + "    \"skipped\" : 0,\n"
                + "    \"pending\" : 0,\n"
                + "    \"knownIssue\" : 0\n"
                + "  },\n"
                + "  \"SCENARIO\" : {\n"
                + "    \"total\" : 10,\n"
                + "    \"passed\" : 3,\n"
                + "    \"failed\" : 1,\n"
                + "    \"broken\" : 1,\n"
                + "    \"skipped\" : 2,\n"
                + "    \"pending\" : 2,\n"
                + "    \"knownIssue\" : 1\n"
                + "  },\n"
                + "  \"STEP\" : {\n"
                + "    \"total\" : 17,\n"
                + "    \"passed\" : 11,\n"
                + "    \"failed\" : 1,\n"
                + "    \"broken\" : 1,\n"
                + "    \"skipped\" : 2,\n"
                + "    \"pending\" : 1,\n"
                + "    \"knownIssue\" : 1\n"
                + "  },\n"
                + "  \"GIVEN_STORY\" : {\n"
                + "    \"total\" : 5,\n"
                + "    \"passed\" : 2,\n"
                + "    \"failed\" : 0,\n"
                + "    \"broken\" : 0,\n"
                + "    \"skipped\" : 1,\n"
                + "    \"pending\" : 2,\n"
                + "    \"knownIssue\" : 0\n"
                + "  }\n"
                + "}";
        assertEquals(expected, output);
        verify(runContext, times(34)).isRunCompleted();
        verifyNoMoreInteractions(runContext);
    }

    @Test
    void shouldReturnStatistics(@TempDir Path tempDirectory)
    {
        reporter.setStatisticsFolder(tempDirectory.toFile());
        reporterFlowProvider();

        Map<NodeType, Statistic> output = StatisticsStoryReporter.getStatistics();
        Assertions.assertAll(
            () -> assertEquals(1, output.get(NodeType.STORY).getTotal()),
            () -> assertEquals(10, output.get(NodeType.SCENARIO).getTotal()),
            () -> assertEquals(17, output.get(NodeType.STEP).getTotal()),
            () -> assertEquals(5, output.get(NodeType.GIVEN_STORY).getTotal()));
        verify(runContext, times(34)).isRunCompleted();
        verifyNoMoreInteractions(runContext);
    }

    @Test
    void shouldLogMessageInCaseOfIOException(@TempDir File tempDir)
    {
        try (MockedStatic<Files> files = mockStatic(Files.class))
        {
            files.when(() -> Files.createDirectories(tempDir.toPath())).thenThrow(new IOException());
            reporter.setStatisticsFolder(tempDir);
            reporterFlowProvider();
            List<LoggingEvent> events = LOGGER.getLoggingEvents();
            assertThat(events, hasSize(1));
            LoggingEvent event = events.get(0);
            assertEquals(String.format("Unable to write statistics.json into folder: %s", tempDir),
                    event.getFormattedMessage());
            assertThat(event.getThrowable().get(), instanceOf(IOException.class));
            verify(runContext, times(34)).isRunCompleted();
            verifyNoMoreInteractions(runContext);
        }
    }

    private String readStatistics(Path tempDirectory) throws IOException
    {
        return Files.readString(tempDirectory.resolve("statistics.json")).replaceAll("\\r", "");
    }

    private void reporterFlowProvider()
    {
        reporter.beforeStoriesSteps(Stage.BEFORE);
        reporter.afterStoriesSteps(Stage.BEFORE);

        reporter.beforeStory(story, false);

        reporter.beforeStory(givenStory, true);

        reporter.beforeStory(givenStory, true);
        reporter.beforeScenario(scenario);
        reportStep(reporter, () -> reporter.successful(STEP_AS_STRING));
        reportStep(reporter, () -> reporter.successful(STEP_AS_STRING));
        reporter.afterScenario(null);
        reporter.afterStory(true);

        reporter.beforeScenario(scenario);
        reporter.beforeStory(givenStory, true);
        reporter.beforeScenario(scenario);
        reportStep(reporter, () -> reporter.successful(STEP_AS_STRING));
        reportStep(reporter, () -> reporter.pending(STEP_AS_STRING));
        reporter.afterScenario(null);
        reporter.afterStory(true);
        reportStep(reporter, () -> reporter.successful(STEP_AS_STRING));
        reportStep(reporter, () -> reporter.successful(STEP_AS_STRING));
        reporter.afterScenario(null);

        reporter.afterStory(true);

        reporter.beforeScenario(scenario);

        // examples start

        reporter.beforeStory(givenStory, true);
        reporter.beforeScenario(scenario);
        reporter.beforeStep(new Step(StepExecutionType.COMMENT, STEP_AS_STRING));
        reporter.comment(STEP_AS_STRING);
        reporter.afterScenario(null);
        reporter.afterStory(true);
        reportStep(reporter, () -> reporter.successful(STEP_AS_STRING));
        reportStep(reporter, () -> reporter.successful(STEP_AS_STRING));

        reporter.beforeStory(givenStory, true);
        reporter.beforeScenario(scenario);
        reportStep(reporter, () -> reporter.ignorable(STEP_AS_STRING));
        reportStep(reporter, () -> reporter.notPerformed(STEP_AS_STRING));
        reporter.afterScenario(null);
        reporter.afterStory(true);
        reporter.beforeStep(STEP);
        reporter.onAssertionFailure(mockFailed());
        reporter.successful(STEP_AS_STRING);
        reportStep(reporter, () -> reporter.successful(STEP_AS_STRING));

        reportControlContext.disableReporting();
        reporter.failed(ASSERTION_STEP, null);
        reportControlContext.enableReporting();

        // examples end

        reporter.afterScenario(null);

        // sub steps

        reporter.beforeScenario(scenario);
        reportStep(reporter, () -> reporter.successful(STEP_AS_STRING));
        reporter.beforeStep(STEP);
        reportStep(reporter, () -> reporter.successful(STEP_AS_STRING));
        reporter.beforeStep(STEP);
        reporter.onAssertionFailure(mockKnownIssue());
        reporter.successful(STEP_AS_STRING);
        reporter.successful(STEP_AS_STRING);

        reportControlContext.disableReporting();
        reporter.failed(ASSERTION_STEP, null);
        reportControlContext.enableReporting();

        reporter.afterScenario(null);

        // broken

        reporter.beforeScenario(scenario);
        reportStep(reporter, () -> reporter.successful(STEP_AS_STRING));
        reportStep(reporter, () -> reporter.failed(STEP_AS_STRING, new Throwable()));
        reporter.afterScenario(null);

        //skipped
        Scenario skippedScenario = mock(Scenario.class);
        when(skippedScenario.getSteps()).thenReturn(List.of("When I one", "Then I two"));
        reporter.beforeScenario(skippedScenario);
        reporter.afterScenario(null);

        // Empty scenario
        Scenario emptyScenario = new Scenario();
        reporter.beforeScenario(emptyScenario);
        reporter.afterScenario(null);

        reporter.afterStory(false);
        reporter.beforeStoriesSteps(Stage.AFTER);
        reporter.afterStoriesSteps(Stage.AFTER);
    }

    private Story mockStory(String path)
    {
        Story story = mock(Story.class);
        when(story.getPath()).thenReturn(path);
        return story;
    }

    private void reportStep(StatisticsStoryReporter reporter, Runnable runnable)
    {
        reporter.beforeStep(STEP);
        runnable.run();
    }

    private AssertionFailedEvent mockFailed()
    {
        AssertionFailedEvent event = mock(AssertionFailedEvent.class);
        SoftAssertionError error = mock(SoftAssertionError.class);
        when(event.getSoftAssertionError()).thenReturn(error);
        when(error.isKnownIssue()).thenReturn(false);
        return event;
    }

    private AssertionFailedEvent mockKnownIssue()
    {
        AssertionFailedEvent event = mock(AssertionFailedEvent.class);
        SoftAssertionError error = mock(SoftAssertionError.class);
        KnownIssue issue = mock(KnownIssue.class);
        when(event.getSoftAssertionError()).thenReturn(error);
        when(error.isKnownIssue()).thenReturn(true);
        when(error.getKnownIssue()).thenReturn(issue);
        when(issue.isFixed()).thenReturn(false);
        return event;
    }

    @Test
    void beforeAndAfterStep(@TempDir Path tempDirectory) throws IOException
    {
        reporter.setStatisticsFolder(tempDirectory.toFile());
        reporter.beforeStoriesSteps(Stage.BEFORE);
        reporter.afterStoriesSteps(Stage.BEFORE);

        reporter.beforeStory(story, false);
        reporter.beforeScenario(scenario);
        reportStep(reporter, () -> reporter.successful(STEP_AS_STRING));
        reportStep(reporter, () -> reporter.successful(STEP_AS_STRING));
        reportStep(reporter, () -> reporter.successful(STEP_AS_STRING));
        reportStep(reporter, () -> reporter.successful(STEP_AS_STRING));
        reporter.beforeStep(STEP);
        reporter.onAssertionFailure(mockFailed());
        reporter.successful(STEP_AS_STRING);
        reportStep(reporter, () -> reporter.successful(STEP_AS_STRING));
        reporter.afterScenario(null);
        reporter.afterStory(false);
        reporter.beforeStoriesSteps(Stage.AFTER);
        reporter.afterStoriesSteps(Stage.AFTER);

        String statistic = readStatistics(tempDirectory);
        String expected = "{\n"
                + "  \"STORY\" : {\n"
                + "    \"total\" : 1,\n"
                + "    \"passed\" : 0,\n"
                + "    \"failed\" : 1,\n"
                + "    \"broken\" : 0,\n"
                + "    \"skipped\" : 0,\n"
                + "    \"pending\" : 0,\n"
                + "    \"knownIssue\" : 0\n"
                + "  },\n"
                + "  \"SCENARIO\" : {\n"
                + "    \"total\" : 1,\n"
                + "    \"passed\" : 0,\n"
                + "    \"failed\" : 1,\n"
                + "    \"broken\" : 0,\n"
                + "    \"skipped\" : 0,\n"
                + "    \"pending\" : 0,\n"
                + "    \"knownIssue\" : 0\n"
                + "  },\n"
                + "  \"STEP\" : {\n"
                + "    \"total\" : 6,\n"
                + "    \"passed\" : 5,\n"
                + "    \"failed\" : 1,\n"
                + "    \"broken\" : 0,\n"
                + "    \"skipped\" : 0,\n"
                + "    \"pending\" : 0,\n"
                + "    \"knownIssue\" : 0\n"
                + "  },\n"
                + "  \"GIVEN_STORY\" : {\n"
                + "    \"total\" : 0,\n"
                + "    \"passed\" : 0,\n"
                + "    \"failed\" : 0,\n"
                + "    \"broken\" : 0,\n"
                + "    \"skipped\" : 0,\n"
                + "    \"pending\" : 0,\n"
                + "    \"knownIssue\" : 0\n"
                + "  }\n"
                + "}";
        assertEquals(expected, statistic);
        verify(runContext, times(12)).isRunCompleted();
        verifyNoMoreInteractions(runContext);
    }

    @Test
    void beforeAndAfterScenario(@TempDir Path tempDirectory) throws IOException
    {
        reporter.setStatisticsFolder(tempDirectory.toFile());
        reporter.beforeStoriesSteps(Stage.BEFORE);
        reporter.afterStoriesSteps(Stage.BEFORE);

        reporter.beforeStory(story, false);
        reporter.beforeScenario(scenario);
        reportStep(reporter, () -> reporter.successful(STEP_AS_STRING));
        reportStep(reporter, () -> reporter.successful(STEP_AS_STRING));
        reporter.beforeStep(STEP);
        reporter.onAssertionFailure(mockFailed());
        reporter.successful(STEP_AS_STRING);
        reportStep(reporter, () -> reporter.successful(STEP_AS_STRING));
        reporter.afterScenario(null);
        reporter.afterStory(false);
        reporter.beforeStoriesSteps(Stage.AFTER);
        reporter.afterStoriesSteps(Stage.AFTER);
        String statistic = readStatistics(tempDirectory);
        String expected = "{\n"
                + "  \"STORY\" : {\n"
                + "    \"total\" : 1,\n"
                + "    \"passed\" : 0,\n"
                + "    \"failed\" : 1,\n"
                + "    \"broken\" : 0,\n"
                + "    \"skipped\" : 0,\n"
                + "    \"pending\" : 0,\n"
                + "    \"knownIssue\" : 0\n"
                + "  },\n"
                + "  \"SCENARIO\" : {\n"
                + "    \"total\" : 1,\n"
                + "    \"passed\" : 0,\n"
                + "    \"failed\" : 1,\n"
                + "    \"broken\" : 0,\n"
                + "    \"skipped\" : 0,\n"
                + "    \"pending\" : 0,\n"
                + "    \"knownIssue\" : 0\n"
                + "  },\n"
                + "  \"STEP\" : {\n"
                + "    \"total\" : 4,\n"
                + "    \"passed\" : 3,\n"
                + "    \"failed\" : 1,\n"
                + "    \"broken\" : 0,\n"
                + "    \"skipped\" : 0,\n"
                + "    \"pending\" : 0,\n"
                + "    \"knownIssue\" : 0\n"
                + "  },\n"
                + "  \"GIVEN_STORY\" : {\n"
                + "    \"total\" : 0,\n"
                + "    \"passed\" : 0,\n"
                + "    \"failed\" : 0,\n"
                + "    \"broken\" : 0,\n"
                + "    \"skipped\" : 0,\n"
                + "    \"pending\" : 0,\n"
                + "    \"knownIssue\" : 0\n"
                + "  }\n"
                + "}";
        assertEquals(expected, statistic);
        verify(runContext, times(8)).isRunCompleted();
        verifyNoMoreInteractions(runContext);
    }

    @Test
    void beforeAndAfterStory(@TempDir Path tempDirectory) throws IOException
    {
        reporter.setStatisticsFolder(tempDirectory.toFile());
        reporter.beforeStoriesSteps(Stage.BEFORE);
        reporter.afterStoriesSteps(Stage.BEFORE);

        reporter.beforeStory(story, false);
        reportStep(reporter, () -> reporter.failed(STEP_AS_STRING, new Throwable()));
        reporter.beforeScenario(scenario);
        reportStep(reporter, () -> reporter.successful(STEP_AS_STRING));
        reporter.beforeStep(STEP);
        reporter.onAssertionFailure(mockFailed());
        reporter.successful(STEP_AS_STRING);
        reporter.afterScenario(null);
        reportStep(reporter, () -> reporter.successful(STEP_AS_STRING));
        reporter.afterStory(false);
        reporter.beforeStoriesSteps(Stage.AFTER);
        reporter.afterStoriesSteps(Stage.AFTER);

        String statistic = readStatistics(tempDirectory);
        String expected = "{\n"
                + "  \"STORY\" : {\n"
                + "    \"total\" : 1,\n"
                + "    \"passed\" : 0,\n"
                + "    \"failed\" : 0,\n"
                + "    \"broken\" : 1,\n"
                + "    \"skipped\" : 0,\n"
                + "    \"pending\" : 0,\n"
                + "    \"knownIssue\" : 0\n"
                + "  },\n"
                + "  \"SCENARIO\" : {\n"
                + "    \"total\" : 1,\n"
                + "    \"passed\" : 0,\n"
                + "    \"failed\" : 1,\n"
                + "    \"broken\" : 0,\n"
                + "    \"skipped\" : 0,\n"
                + "    \"pending\" : 0,\n"
                + "    \"knownIssue\" : 0\n"
                + "  },\n"
                + "  \"STEP\" : {\n"
                + "    \"total\" : 4,\n"
                + "    \"passed\" : 2,\n"
                + "    \"failed\" : 1,\n"
                + "    \"broken\" : 1,\n"
                + "    \"skipped\" : 0,\n"
                + "    \"pending\" : 0,\n"
                + "    \"knownIssue\" : 0\n"
                + "  },\n"
                + "  \"GIVEN_STORY\" : {\n"
                + "    \"total\" : 0,\n"
                + "    \"passed\" : 0,\n"
                + "    \"failed\" : 0,\n"
                + "    \"broken\" : 0,\n"
                + "    \"skipped\" : 0,\n"
                + "    \"pending\" : 0,\n"
                + "    \"knownIssue\" : 0\n"
                + "  }\n"
                + "}";
        assertEquals(expected, statistic);
        verify(runContext, times(8)).isRunCompleted();
        verifyNoMoreInteractions(runContext);
    }

    @Test
    void shouldProvideRecordedFailures()
    {
        reporter.beforeStory(story, false);
        assertNull(StatisticsStoryReporter.getFailures());
        reporter.setCollectFailures(true);
        reporter.init();

        RunningStory runningStory = mock(RunningStory.class);
        String step = "step1";
        when(runningStory.getRunningSteps()).thenReturn(new LinkedList<>(List.of(step, "step2")));
        when(runContext.getRunningStory()).thenReturn(runningStory);
        String storyName = "storyName";
        when(runningStory.getName()).thenReturn(storyName);
        RunningScenario runningScenario = mock(RunningScenario.class);
        when(runningStory.getRunningScenario()).thenReturn(runningScenario);
        String scenarioTitle = "scenarioTitle";
        when(runningScenario.getTitle()).thenReturn(scenarioTitle);

        AssertionFailedEvent event = mock(AssertionFailedEvent.class);
        SoftAssertionError assertion = mock(SoftAssertionError.class);
        when(event.getSoftAssertionError()).thenReturn(assertion);
        AssertionError error = mock(AssertionError.class);
        String argumentExceptionCauseMessage = "You're illegal";
        when(assertion.getError()).thenReturn(error);
        when(error.getMessage()).thenReturn(argumentExceptionCauseMessage);
        reporter.beforeStep(STEP);
        reporter.onAssertionFailure(event);
        IllegalArgumentException exception = new IllegalArgumentException(argumentExceptionCauseMessage);
        reporter.failed("step", new IllegalArgumentException(exception));
        UUIDExceptionWrapper verificationErrorWrapped = new UUIDExceptionWrapper(
                new BeforeOrAfterFailed(new VerificationError("message", List.of())));
        UUIDExceptionWrapper illegalArgumentExceptionWrapped = new UUIDExceptionWrapper(
                new BeforeOrAfterFailed(new IllegalArgumentException(argumentExceptionCauseMessage)));
        reporter.beforeStep(STEP);
        reporter.failed("verifyIfAssertionsPassed", verificationErrorWrapped);
        reporter.beforeStep(STEP);
        reporter.failed("some other step", error);
        reporter.beforeStep(STEP);
        reporter.failed("Step with incorrect argument", illegalArgumentExceptionWrapped);

        List<Failure> failures = StatisticsStoryReporter.getFailures();
        failures.forEach(s -> System.out.println(s.getMessage()));
        assertThat(failures, hasSize(3));
        Failure fail = failures.get(0);
        Failure broken = failures.get(1);
        Failure assertionFail = failures.get(1);

        assertFailure(fail, storyName, scenarioTitle, step, argumentExceptionCauseMessage);
        assertFailure(broken, storyName, scenarioTitle, step, "java.lang.IllegalArgumentException: You're illegal");
        assertFailure(assertionFail, storyName, scenarioTitle, step,
                "java.lang.IllegalArgumentException: " + argumentExceptionCauseMessage);
    }

    private static void assertFailure(Failure failure, String story, String scenario, String step, String cause)
    {
        Assertions.assertAll(
            () -> assertEquals(story, failure.getStory()),
            () -> assertEquals(scenario, failure.getScenario()),
            () -> assertEquals(step, failure.getStep()),
            () -> assertEquals(cause, failure.getMessage())
        );
    }
}
