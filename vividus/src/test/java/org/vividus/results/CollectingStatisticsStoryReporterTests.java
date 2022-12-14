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

package org.vividus.results;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.inOrder;
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
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.jbehave.core.failures.BeforeOrAfterFailed;
import org.jbehave.core.failures.UUIDExceptionWrapper;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Step;
import org.jbehave.core.model.Story;
import org.jbehave.core.reporters.StoryReporter;
import org.jbehave.core.steps.StepCollector.Stage;
import org.jbehave.core.steps.StepCreator.PendingStep;
import org.jbehave.core.steps.StepCreator.StepExecutionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.context.ReportControlContext;
import org.vividus.context.RunContext;
import org.vividus.model.RunningScenario;
import org.vividus.model.RunningStory;
import org.vividus.results.model.ExecutableEntity;
import org.vividus.results.model.ExitCode;
import org.vividus.results.model.Failure;
import org.vividus.softassert.event.AssertionFailedEvent;
import org.vividus.softassert.exception.VerificationError;
import org.vividus.softassert.issue.KnownIssueIdentifier;
import org.vividus.softassert.issue.KnownIssueType;
import org.vividus.softassert.model.KnownIssue;
import org.vividus.softassert.model.SoftAssertionError;
import org.vividus.testcontext.SimpleTestContext;
import org.vividus.testcontext.ThreadedTestContext;
import org.vividus.util.json.JsonUtils;

@ExtendWith({ MockitoExtension.class, TestLoggerFactoryExtension.class })
@SuppressWarnings({ "MultipleStringLiterals", "MultipleStringLiteralsExtended", "PMD.AvoidDuplicateLiterals"})
class CollectingStatisticsStoryReporterTests
{
    private static final String STEP_AS_STRING = "step";
    private static final Step STEP = new Step(StepExecutionType.EXECUTABLE, STEP_AS_STRING);
    private static final String ASSERTION_STEP = "assertionPassed";

    private static final TestLogger LOGGER = TestLoggerFactory.getTestLogger(CollectingStatisticsStoryReporter.class);

    @Mock private RunContext runContext;
    @Mock private StoryReporter nextStoryReporter;
    private final ReportControlContext reportControlContext = new ReportControlContext(new SimpleTestContext());

    private CollectingStatisticsStoryReporter reporter;

    private final Story givenStory = mockStory("GivenStory");
    private final Story story = mockStory("Story");
    private final Scenario scenario = new Scenario();

    @BeforeEach
    void beforeEach()
    {
        reportControlContext.enableReporting();
    }

    private void initReporter(boolean collectFailures, File statisticsFolder)
    {
        reporter = new CollectingStatisticsStoryReporter(collectFailures, statisticsFolder, reportControlContext,
                runContext, new ThreadedTestContext(), new JsonUtils());
        reporter.setNext(nextStoryReporter);
    }

    @Test
    void testScenarioExcluded(@TempDir Path tempDirectory)
    {
        initReporter(false, tempDirectory.toFile());

        reporter.scenarioExcluded(null, null);
        assertEquals(ExitCode.FAILED, reporter.calculateExitCode());
        verify(nextStoryReporter).scenarioExcluded(null, null);
    }

    static Stream<Arguments> stepTypes()
    {
        var knownIssueError = createKnownIssueError(false);
        var potentiallyKnownIssueError = createKnownIssueError(true);
        var assertionError = new UUIDExceptionWrapper(new AssertionError());
        var verificationError = createVerificationError(new SoftAssertionError(null));
        var ioException = new IOException();

        return Stream.of(
            arguments(ExitCode.PASSED, (BiConsumer<StoryReporter, String>) StoryReporter::successful, STEP_AS_STRING),
            arguments(ExitCode.FAILED, (BiConsumer<StoryReporter, String>) StoryReporter::ignorable, STEP_AS_STRING),
            arguments(ExitCode.FAILED, (BiConsumer<StoryReporter, String>) StoryReporter::notPerformed, STEP_AS_STRING),
            arguments(ExitCode.KNOWN_ISSUES, (BiConsumer<StoryReporter, String>)
                    (reporter, step) -> reporter.failed(step, knownIssueError),
                    STEP_AS_STRING
            ),
            arguments(ExitCode.FAILED, (BiConsumer<StoryReporter, PendingStep>) StoryReporter::pending,
                    new PendingStep(STEP_AS_STRING, null)
            ),
            arguments(ExitCode.FAILED, (BiConsumer<StoryReporter, String>)
                    (reporter, step) -> reporter.failed(step, potentiallyKnownIssueError),
                    STEP_AS_STRING
            ),
            arguments(ExitCode.FAILED, (BiConsumer<StoryReporter, String>)
                    (reporter, step) -> reporter.failed(step, assertionError),
                    STEP_AS_STRING
            ),
            arguments(ExitCode.FAILED, (BiConsumer<StoryReporter, String>)
                    (reporter, step) -> reporter.failed(step, verificationError),
                    STEP_AS_STRING
            ),
            arguments(ExitCode.FAILED, (BiConsumer<StoryReporter, String>)
                    (reporter, step) -> reporter.failed(step, ioException),
                    STEP_AS_STRING
            )
        );
    }

    private static UUIDExceptionWrapper createKnownIssueError(boolean potentiallyKnown)
    {
        var identifier = new KnownIssueIdentifier();
        identifier.setType(KnownIssueType.AUTOMATION);
        var knownIssue = new KnownIssue(null, identifier, potentiallyKnown);

        var knownIssueSoftAssertionError = new SoftAssertionError(null);
        knownIssueSoftAssertionError.setKnownIssue(knownIssue);
        return createVerificationError(knownIssueSoftAssertionError);
    }

    private static UUIDExceptionWrapper createVerificationError(SoftAssertionError softAssertionError)
    {
        return new UUIDExceptionWrapper(new VerificationError(null, List.of(softAssertionError)));
    }

    @ParameterizedTest
    @MethodSource("stepTypes")
    <T> void shouldRecordStepStatus(ExitCode expectedStatus, BiConsumer<StoryReporter, T> test, T input,
            @TempDir Path tempDirectory)
    {
        when(runContext.isRunInProgress()).thenReturn(true);
        initReporter(false, tempDirectory.toFile());

        reporter.beforeStory(story, false);
        reporter.beforeScenario(scenario);
        reporter.beforeStep(STEP);
        test.accept(reporter, input);
        reporter.afterScenario(null);
        reporter.afterStory(false);

        assertEquals(expectedStatus, reporter.calculateExitCode());

        var ordered = inOrder(nextStoryReporter);
        ordered.verify(nextStoryReporter).beforeStory(story, false);
        ordered.verify(nextStoryReporter).beforeScenario(scenario);
        ordered.verify(nextStoryReporter).beforeStep(STEP);
        test.accept(ordered.verify(nextStoryReporter), input);
        ordered.verify(nextStoryReporter).afterScenario(null);
        ordered.verify(nextStoryReporter).afterStory(false);
        ordered.verifyNoMoreInteractions();
    }

    @Test
    void shouldSkipReportingForBeforeOrAfterStoriesSteps(@TempDir Path tempDirectory)
    {
        initReporter(false, tempDirectory.toFile());

        reportControlContext.disableReporting();

        reporter.beforeStoriesSteps(Stage.BEFORE);
        reporter.beforeStep(STEP);
        reporter.successful(STEP_AS_STRING);
        reporter.afterStoriesSteps(Stage.BEFORE);
        assertEquals(ExitCode.FAILED, reporter.calculateExitCode());
        var ordered = inOrder(nextStoryReporter);
        ordered.verify(nextStoryReporter).beforeStoriesSteps(Stage.BEFORE);
        ordered.verify(nextStoryReporter).beforeStep(STEP);
        ordered.verify(nextStoryReporter).successful(STEP_AS_STRING);
        ordered.verify(nextStoryReporter).afterStoriesSteps(Stage.BEFORE);
    }

    @Test
    void shouldEnableReportingBackAfterBeforeOrAfterStoriesSteps(@TempDir Path tempDirectory)
    {
        initReporter(false, tempDirectory.toFile());

        reportControlContext.disableReporting();
        reporter.beforeStoriesSteps(Stage.BEFORE);
        reporter.afterStoriesSteps(Stage.BEFORE);
        reporter.beforeStoriesSteps(Stage.AFTER);
        reporter.afterStoriesSteps(Stage.AFTER);

        reportControlContext.enableReporting();
        reporter.beforeStory(story, false);
        reporter.beforeScenario(scenario);
        reporter.beforeStep(STEP);
        reporter.successful(STEP_AS_STRING);
        reporter.afterScenario(null);
        reporter.afterStory(false);

        assertEquals(ExitCode.PASSED, reporter.calculateExitCode());
        var ordered = inOrder(nextStoryReporter);
        ordered.verify(nextStoryReporter).beforeStoriesSteps(Stage.AFTER);
        ordered.verify(nextStoryReporter).afterStoriesSteps(Stage.AFTER);
        ordered.verify(nextStoryReporter).beforeStep(STEP);
        ordered.verify(nextStoryReporter).successful(STEP_AS_STRING);
        ordered.verify(nextStoryReporter).afterScenario(null);
        ordered.verify(nextStoryReporter).afterStory(false);
        ordered.verifyNoMoreInteractions();
    }

    @Test
    void shouldChangeStatusAccordingToPriorities(@TempDir Path tempDirectory)
    {
        initReporter(false, tempDirectory.toFile());

        reporter.beforeStory(story, false);
        reporter.beforeScenario(scenario);
        reporter.beforeStep(STEP);
        reporter.successful(STEP_AS_STRING);
        assertEquals(ExitCode.PASSED, reporter.calculateExitCode());
        reporter.beforeStep(STEP);
        reporter.failed(STEP_AS_STRING, null);
        assertEquals(ExitCode.FAILED, reporter.calculateExitCode());
        reporter.beforeStep(STEP);
        reporter.successful(STEP_AS_STRING);
        reporter.afterScenario(null);
        reporter.afterStory(false);
        assertEquals(ExitCode.FAILED, reporter.calculateExitCode());
    }

    @Test
    void shouldChangeKnownIssuesOnlyStatusToPendingStatus(@TempDir Path tempDirectory)
    {
        when(runContext.isRunInProgress()).thenReturn(true);
        initReporter(false, tempDirectory.toFile());

        reporter.beforeStory(story, false);
        reporter.beforeScenario(scenario);
        reporter.beforeStep(STEP);
        var throwable = createKnownIssueError(false);
        reporter.failed(STEP_AS_STRING, throwable);
        assertEquals(ExitCode.KNOWN_ISSUES, reporter.calculateExitCode());
        reporter.beforeStep(STEP);
        reporter.pending(new PendingStep(STEP_AS_STRING, null));
        reporter.afterScenario(null);
        reporter.afterStory(false);
        assertEquals(ExitCode.FAILED, reporter.calculateExitCode());
    }

    @Test
    void shouldSaveStatistics(@TempDir Path tempDirectory) throws IOException
    {
        initReporter(false, tempDirectory.toFile());
        reporterFlowProvider();

        var output = readStatistics(tempDirectory);
        var expected = "{\n"
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
        verify(runContext, times(34)).isRunInProgress();
        verifyNoMoreInteractions(runContext);
    }

    @Test
    void shouldReturnStatistics(@TempDir File tempDirectory)
    {
        initReporter(false, tempDirectory);
        reporterFlowProvider();

        var output = reporter.getStatistics();
        assertAll(
            () -> assertEquals(1, output.get(ExecutableEntity.STORY).getTotal()),
            () -> assertEquals(10, output.get(ExecutableEntity.SCENARIO).getTotal()),
            () -> assertEquals(17, output.get(ExecutableEntity.STEP).getTotal()),
            () -> assertEquals(5, output.get(ExecutableEntity.GIVEN_STORY).getTotal())
        );
        verify(runContext, times(34)).isRunInProgress();
        verifyNoMoreInteractions(runContext);
    }

    @Test
    void shouldReturnDuration(@TempDir File tempDirectory)
    {
        initReporter(false, tempDirectory);
        reporterFlowProvider();

        var duration = reporter.getDuration();

        assertThat(duration.toNanos(), greaterThan(0L));
    }

    @Test
    void shouldLogMessageInCaseOfIOException(@TempDir File tempDirectory)
    {
        when(runContext.isRunInProgress()).thenReturn(true);
        initReporter(false, tempDirectory);
        try (MockedStatic<Files> files = mockStatic(Files.class))
        {
            files.when(() -> Files.createDirectories(tempDirectory.toPath())).thenThrow(new IOException());
            reporterFlowProvider();
            var events = LOGGER.getLoggingEvents();
            assertThat(events, hasSize(1));
            var event = events.get(0);
            assertEquals(String.format("Unable to write statistics.json into folder: %s", tempDirectory),
                    event.getFormattedMessage());
            assertThat(event.getThrowable().get(), instanceOf(IOException.class));
            verify(runContext, times(34)).isRunInProgress();
            verifyNoMoreInteractions(runContext);
        }
    }

    private String readStatistics(Path tempDirectory) throws IOException
    {
        return Files.readString(tempDirectory.resolve("statistics.json")).replaceAll("\\r", "");
    }

    @SuppressWarnings({ "MethodLength", "PMD.ExcessiveMethodLength", "PMD.NcssCount" })
    private void reporterFlowProvider()
    {
        when(runContext.isRunInProgress()).thenReturn(true);

        reporter.beforeStoriesSteps(Stage.BEFORE);
        reporter.afterStoriesSteps(Stage.BEFORE);

        reporter.beforeStory(story, false);

        reporter.beforeStory(givenStory, true);

        reporter.beforeStory(givenStory, true);
        reporter.beforeScenario(scenario);
        reportStep(reporter, () -> reporter.successful(STEP_AS_STRING));
        assertEquals(ExitCode.PASSED, reporter.calculateExitCode());
        reportStep(reporter, () -> reporter.successful(STEP_AS_STRING));
        assertEquals(ExitCode.PASSED, reporter.calculateExitCode());
        reporter.afterScenario(null);
        reporter.afterStory(true);

        reporter.beforeScenario(scenario);
        reporter.beforeStory(givenStory, true);
        reporter.beforeScenario(scenario);
        reportStep(reporter, () -> reporter.successful(STEP_AS_STRING));
        PendingStep pendingStep = new PendingStep(STEP_AS_STRING, null);
        reportStep(reporter, () -> reporter.pending(pendingStep));
        assertEquals(ExitCode.FAILED, reporter.calculateExitCode());
        reporter.afterScenario(null);
        reporter.afterStory(true);
        reportStep(reporter, () -> reporter.successful(STEP_AS_STRING));
        assertEquals(ExitCode.FAILED, reporter.calculateExitCode());
        reportStep(reporter, () -> reporter.successful(STEP_AS_STRING));
        assertEquals(ExitCode.FAILED, reporter.calculateExitCode());
        reporter.afterScenario(null);

        reporter.afterStory(true);

        reporter.beforeScenario(scenario);

        // examples start

        reporter.beforeStory(givenStory, true);
        reporter.beforeScenario(scenario);
        var comment = new Step(StepExecutionType.COMMENT, STEP_AS_STRING);
        reporter.beforeStep(comment);
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
        var failure = new Throwable();
        reportStep(reporter, () -> reporter.failed(STEP_AS_STRING, failure));
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

        verify(nextStoryReporter).beforeStoriesSteps(Stage.BEFORE);
        verify(nextStoryReporter).afterStoriesSteps(Stage.BEFORE);
        verify(nextStoryReporter).beforeStory(story, false);
        verify(nextStoryReporter, times(5)).beforeStory(givenStory, true);
        verify(nextStoryReporter, times(8)).beforeScenario(scenario);
        verify(nextStoryReporter, times(18)).beforeStep(STEP);
        verify(nextStoryReporter, times(14)).successful(STEP_AS_STRING);
        verify(nextStoryReporter, times(10)).afterScenario(null);
        verify(nextStoryReporter, times(5)).afterStory(true);
        verify(nextStoryReporter).pending(pendingStep);
        verify(nextStoryReporter).beforeStep(comment);
        verify(nextStoryReporter).comment(STEP_AS_STRING);
        verify(nextStoryReporter).ignorable(STEP_AS_STRING);
        verify(nextStoryReporter).notPerformed(STEP_AS_STRING);
        verify(nextStoryReporter, times(2)).failed(ASSERTION_STEP, null);
        verify(nextStoryReporter).failed(STEP_AS_STRING, failure);
        verify(nextStoryReporter).beforeScenario(skippedScenario);
        verify(nextStoryReporter).beforeScenario(emptyScenario);
        verify(nextStoryReporter).afterStory(false);
        verify(nextStoryReporter).beforeStoriesSteps(Stage.AFTER);
        verify(nextStoryReporter).afterStoriesSteps(Stage.AFTER);
    }

    private Story mockStory(String path)
    {
        var story = mock(Story.class);
        when(story.getPath()).thenReturn(path);
        return story;
    }

    private void reportStep(CollectingStatisticsStoryReporter reporter, Runnable runnable)
    {
        reporter.beforeStep(STEP);
        runnable.run();
    }

    private AssertionFailedEvent mockFailed()
    {
        var event = mock(AssertionFailedEvent.class);
        var error = mock(SoftAssertionError.class);
        when(event.getSoftAssertionError()).thenReturn(error);
        when(error.isNotFixedKnownIssue()).thenReturn(false);
        return event;
    }

    private AssertionFailedEvent mockKnownIssue()
    {
        var event = mock(AssertionFailedEvent.class);
        var error = mock(SoftAssertionError.class);
        when(event.getSoftAssertionError()).thenReturn(error);
        when(error.isNotFixedKnownIssue()).thenReturn(true);
        return event;
    }

    @Test
    void beforeAndAfterStep(@TempDir Path tempDirectory) throws IOException
    {
        when(runContext.isRunInProgress()).thenReturn(true);
        initReporter(false, tempDirectory.toFile());
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

        var statistic = readStatistics(tempDirectory);
        var expected = "{\n"
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
        verify(runContext, times(12)).isRunInProgress();
        verifyNoMoreInteractions(runContext);
    }

    @Test
    void beforeAndAfterScenario(@TempDir Path tempDirectory) throws IOException
    {
        when(runContext.isRunInProgress()).thenReturn(true);
        initReporter(false, tempDirectory.toFile());
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
        var statistic = readStatistics(tempDirectory);
        var expected = "{\n"
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
        verify(runContext, times(8)).isRunInProgress();
        verifyNoMoreInteractions(runContext);
    }

    @Test
    void beforeAndAfterStory(@TempDir Path tempDirectory) throws IOException
    {
        when(runContext.isRunInProgress()).thenReturn(true);
        initReporter(false, tempDirectory.toFile());
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

        var statistic = readStatistics(tempDirectory);
        var expected = "{\n"
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
        verify(runContext, times(8)).isRunInProgress();
        verifyNoMoreInteractions(runContext);
    }

    @Test
    @SuppressWarnings("PMD.NcssCount")
    void shouldProvideRecordedFailures()
    {
        when(runContext.isRunInProgress()).thenReturn(true);
        initReporter(true, null);
        reporter.beforeStory(story, false);
        assertTrue(reporter.getFailures().isPresent());

        var runningStory = mock(RunningStory.class);
        var step = "step1";
        when(runningStory.getRunningSteps()).thenReturn(new LinkedList<>(List.of(step, "step2")));
        when(runContext.getRunningStory()).thenReturn(runningStory);
        var storyName = "storyName";
        when(runningStory.getName()).thenReturn(storyName);
        var runningScenario = mock(RunningScenario.class);
        when(runningStory.getRunningScenario()).thenReturn(runningScenario);
        var scenarioTitle = "scenarioTitle";
        when(runningScenario.getTitle()).thenReturn(scenarioTitle);

        var event = mock(AssertionFailedEvent.class);
        var assertion = mock(SoftAssertionError.class);
        when(event.getSoftAssertionError()).thenReturn(assertion);
        var error = mock(AssertionError.class);
        var argumentExceptionCauseMessage = "You're illegal";
        when(assertion.getError()).thenReturn(error);
        when(error.getMessage()).thenReturn(argumentExceptionCauseMessage);
        reporter.beforeStep(STEP);
        reporter.onAssertionFailure(event);
        var exception = new IllegalArgumentException(argumentExceptionCauseMessage);
        reporter.failed("step", new IllegalArgumentException(exception));
        var verificationErrorWrapped = new UUIDExceptionWrapper(
                new BeforeOrAfterFailed(new VerificationError("message", List.of())));
        var illegalArgumentExceptionWrapped = new UUIDExceptionWrapper(
                new BeforeOrAfterFailed(new IllegalArgumentException(argumentExceptionCauseMessage)));
        reporter.beforeStep(STEP);
        reporter.failed("verifyIfAssertionsPassed", verificationErrorWrapped);
        reporter.beforeStep(STEP);
        reporter.failed("some other step", error);
        reporter.beforeStep(STEP);
        reporter.failed("Step with incorrect argument", illegalArgumentExceptionWrapped);

        var optionalFailures = reporter.getFailures();
        assertTrue(optionalFailures.isPresent());
        var failures = optionalFailures.get();
        assertThat(failures, hasSize(3));
        var fail = failures.get(0);
        var broken = failures.get(1);
        var assertionFail = failures.get(1);

        assertFailure(fail, storyName, scenarioTitle, step, argumentExceptionCauseMessage);
        assertFailure(broken, storyName, scenarioTitle, step, "java.lang.IllegalArgumentException: You're illegal");
        assertFailure(assertionFail, storyName, scenarioTitle, step,
                "java.lang.IllegalArgumentException: " + argumentExceptionCauseMessage);
    }

    private static void assertFailure(Failure failure, String story, String scenario, String step, String cause)
    {
        assertAll(
            () -> assertEquals(story, failure.getStory()),
            () -> assertEquals(scenario, failure.getScenario()),
            () -> assertEquals(step, failure.getStep()),
            () -> assertEquals(cause, failure.getMessage())
        );
    }
}
