/*
 * Copyright 2019-2021 the original author or authors.
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

package org.vividus.bdd;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verifyNoInteractions;
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
import org.vividus.bdd.context.IBddRunContext;
import org.vividus.bdd.model.Failure;
import org.vividus.bdd.model.NodeType;
import org.vividus.bdd.model.RunningScenario;
import org.vividus.bdd.model.RunningStory;
import org.vividus.bdd.model.Statistic;
import org.vividus.softassert.event.AssertionFailedEvent;
import org.vividus.softassert.model.KnownIssue;
import org.vividus.softassert.model.SoftAssertionError;
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

    @Mock
    private EventBus eventBus;

    @Mock
    private IBddRunContext bddRunContext;

    private StatisticsStoryReporter reporter;

    private final Story givenStory = mockStory("GivenStory");
    private final Story story = mockStory("Story");
    private final Scenario scenario = new Scenario();

    @BeforeEach
    void init()
    {
        reporter = new StatisticsStoryReporter(eventBus, new ThreadedTestContext(), new JsonUtils(),
                bddRunContext);
        reporter.init();
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
                + "    \"total\" : 18,\n"
                + "    \"passed\" : 12,\n"
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
        verifyNoInteractions(bddRunContext);
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
            () -> assertEquals(18, output.get(NodeType.STEP).getTotal()),
            () -> assertEquals(5, output.get(NodeType.GIVEN_STORY).getTotal()));
        verifyNoInteractions(bddRunContext);
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
            verifyNoInteractions(bddRunContext);
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
        reporter.failed(ASSERTION_STEP, null);

        // examples end

        reporter.afterScenario(null);

        // sub steps

        reporter.beforeScenario(scenario);
        reportStep(reporter, () -> reporter.successful(STEP_AS_STRING));
        reporter.beforeStep(STEP);
        reportStep(reporter, () -> reporter.successful(STEP_AS_STRING));
        reportStep(reporter, () -> reporter.successful(STEP_AS_STRING));
        reporter.onAssertionFailure(mockKnownIssue());
        reporter.successful(STEP_AS_STRING);
        reporter.failed(ASSERTION_STEP, null);
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
        verifyNoInteractions(bddRunContext);
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
        verifyNoInteractions(bddRunContext);
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
        verifyNoInteractions(bddRunContext);
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
        when(bddRunContext.getRunningStory()).thenReturn(runningStory);
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
        String causeMessage = "You're illegal";
        when(assertion.getError()).thenReturn(error);
        when(error.getMessage()).thenReturn(causeMessage);
        reporter.onAssertionFailure(event);
        IllegalArgumentException exception = new IllegalArgumentException(causeMessage);
        reporter.failed("step", new IllegalArgumentException(exception));
        reporter.failed("verifyIfAssertionsPassed", null);
        reporter.failed("some other step", error);

        List<Failure> failures = StatisticsStoryReporter.getFailures();
        assertThat(failures, hasSize(2));
        Failure fail = failures.get(0);
        Failure broken = failures.get(1);
        Assertions.assertAll(
                () -> assertEquals(storyName, fail.getStory()),
                () -> assertEquals(scenarioTitle, fail.getScenario()),
                () -> assertEquals(step, fail.getStep()),
                () -> assertEquals(causeMessage, fail.getMessage()),
                () -> assertEquals(storyName, broken.getStory()),
                () -> assertEquals(scenarioTitle, broken.getScenario()),
                () -> assertEquals(step, broken.getStep()),
                () -> assertEquals("java.lang.IllegalArgumentException: You're illegal", broken.getMessage()));
    }
}
