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

package org.vividus.report.allure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.jbehave.core.failures.UUIDExceptionWrapper;
import org.jbehave.core.model.Lifecycle.ExecutionType;
import org.jbehave.core.model.Meta;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Step;
import org.jbehave.core.model.Story;
import org.jbehave.core.reporters.NullStoryReporter;
import org.jbehave.core.steps.StepCollector.Stage;
import org.jbehave.core.steps.StepCreator.StepExecutionType;
import org.jbehave.core.steps.Timing;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.vividus.ReportControlStoryReporter;
import org.vividus.batch.BatchConfiguration;
import org.vividus.batch.BatchStorage;
import org.vividus.context.ReportControlContext;
import org.vividus.context.RunTestContext;
import org.vividus.model.RunningScenario;
import org.vividus.model.RunningStory;
import org.vividus.report.allure.adapter.IVerificationErrorAdapter;
import org.vividus.testcontext.SimpleTestContext;
import org.vividus.testcontext.TestContext;

import io.qameta.allure.AllureLifecycle;
import io.qameta.allure.AllureResultsWriter;
import io.qameta.allure.model.Status;
import io.qameta.allure.model.StepResult;
import io.qameta.allure.model.TestResult;

class AllureStoryReporterScenarioFlowTests
{
    private static final String BATCH_KEY = "batch-1";
    private static final String MAIN_STEP = "Then `1` is equal to `1`";
    private static final String LIFECYCLE_STEP = "Given I initialize scenario variable `var` with value `value`";
    private static final String CONTAINER_STEP = "When I execute steps:";
    private static final String SUB_STEP = "Then `2` is equal to `2`";
    private static final String FIRST_ITERATION_LOG = "Saving a value '0' into the step variable 'iterationVariable'";
    private static final String SECOND_ITERATION_LOG = "Saving a value '1' into the step variable 'iterationVariable'";
    private static final String LOG_ENTRY = "log entry";
    private static final Runnable NO_STEPS = () -> { };

    private final TestContext testContext = new SimpleTestContext();
    private final ReportControlContext reportControlContext = new ReportControlContext(testContext);
    private final AllureResultsWriter resultsWriter = mock(AllureResultsWriter.class);
    private final Scenario scenario = new Scenario("Scenario", Meta.EMPTY);
    private final Story story = new Story("story/Story.story", null, Meta.EMPTY, null, List.of(scenario));

    private AllureStoryReporter allureStoryReporter;
    private ReportControlStoryReporter reporter;

    @BeforeEach
    void beforeEach() throws IllegalAccessException
    {
        RunTestContext runContext = new RunTestContext();
        runContext.setTestContext(testContext);
        runContext.startRun();
        RunningScenario runningScenario = new RunningScenario();
        runningScenario.setScenario(scenario);
        RunningStory runningStory = new RunningStory();
        runningStory.setStory(story);
        runningStory.setNotExcluded(true);
        runningStory.setRunningScenario(runningScenario);
        runContext.putRunningStory(runningStory, false);
        reportControlContext.enableReporting();

        BatchConfiguration batchConfiguration = new BatchConfiguration();
        batchConfiguration.setName(BATCH_KEY);
        BatchStorage batchStorage = mock(BatchStorage.class);
        when(batchStorage.getBatchConfiguration(BATCH_KEY)).thenReturn(batchConfiguration);

        allureStoryReporter = new AllureStoryReporter(false, reportControlContext, runContext,
                mock(IAllureReportGenerator.class), batchStorage, testContext, new AllureRunContext(testContext),
                mock(IVerificationErrorAdapter.class));
        FieldUtils.writeField(allureStoryReporter, "lifecycle", new AllureLifecycle(resultsWriter), true);
        allureStoryReporter.setNext(new NullStoryReporter());
        reporter = new ReportControlStoryReporter(reportControlContext);
        reporter.setNext(allureStoryReporter);
    }

    @Test
    void shouldReportScenarioWhenLifecycleBeforeStepLogsBetweenItsSubSteps()
    {
        performScenario(NO_STEPS, () -> {
            reporter.beforeStep(createStep(CONTAINER_STEP));
            addLogStep(FIRST_ITERATION_LOG);
            performStep(SUB_STEP);
            addLogStep(SECOND_ITERATION_LOG);
            performStep(SUB_STEP);
            reporter.successful(CONTAINER_STEP);
        }, () -> performStep(MAIN_STEP), NO_STEPS, NO_STEPS);

        TestResult testResult = getWrittenTestResult();
        assertEquals(Status.PASSED, testResult.getStatus());
        List<StepResult> steps = testResult.getSteps();
        assertStepNames(steps, CONTAINER_STEP, MAIN_STEP);
        assertStepNames(steps.get(0).getSteps(), FIRST_ITERATION_LOG, SUB_STEP, SECOND_ITERATION_LOG, SUB_STEP);
    }

    @Test
    void shouldReportBrokenScenarioWhenLifecycleBeforeStepFailsAfterItsSubSteps()
    {
        UUIDExceptionWrapper failure = new UUIDExceptionWrapper(new IllegalStateException("broken"));
        performScenario(NO_STEPS, () -> {
            reporter.beforeStep(createStep(CONTAINER_STEP));
            reporter.beforeStep(createStep(SUB_STEP));
            reporter.failed(SUB_STEP, failure);
            reporter.failed(CONTAINER_STEP, failure);
        }, () -> {
            reporter.beforeStep(createStep(MAIN_STEP));
            reporter.notPerformed(MAIN_STEP);
        }, NO_STEPS, NO_STEPS);

        TestResult testResult = getWrittenTestResult();
        assertEquals(Status.BROKEN, testResult.getStatus());
        List<StepResult> steps = testResult.getSteps();
        assertStepNames(steps, CONTAINER_STEP, MAIN_STEP);
        StepResult containerStep = steps.get(0);
        assertEquals(Status.BROKEN, containerStep.getStatus());
        assertStepNames(containerStep.getSteps(), SUB_STEP);
    }

    @Test
    void shouldReportLogsOfSystemBeforeScenarioStepsInSeparateStep()
    {
        performScenario(() -> addLogStep(LOG_ENTRY), () -> performStep(LIFECYCLE_STEP), () -> performStep(MAIN_STEP),
                NO_STEPS, NO_STEPS);

        List<StepResult> steps = getWrittenTestResult().getSteps();
        assertStepNames(steps, "@BeforeScenario", LIFECYCLE_STEP, MAIN_STEP);
        assertStepNames(steps.get(0).getSteps(), LOG_ENTRY);
    }

    @Test
    void shouldReportLogsOfSystemAfterScenarioStepsInSeparateStep()
    {
        performScenario(NO_STEPS, NO_STEPS, () -> performStep(MAIN_STEP), () -> performStep(LIFECYCLE_STEP),
                () -> addLogStep(LOG_ENTRY));

        List<StepResult> steps = getWrittenTestResult().getSteps();
        assertStepNames(steps, MAIN_STEP, LIFECYCLE_STEP, "@AfterScenario");
        assertStepNames(steps.get(2).getSteps(), LOG_ENTRY);
    }

    private void addLogStep(String logEntry)
    {
        allureStoryReporter.addLogStep("INFO", logEntry);
    }

    private void performScenario(Runnable systemBeforeSteps, Runnable userBeforeSteps, Runnable scenarioSteps,
            Runnable userAfterSteps, Runnable systemAfterSteps)
    {
        reporter.beforeStory(story, false);
        reporter.beforeScenario(scenario);
        performStage(Stage.BEFORE, ExecutionType.SYSTEM, systemBeforeSteps);
        performStage(Stage.BEFORE, ExecutionType.USER, userBeforeSteps);
        performStage(null, null, scenarioSteps);
        performStage(Stage.AFTER, ExecutionType.USER, userAfterSteps);
        performStage(Stage.AFTER, ExecutionType.SYSTEM, systemAfterSteps);
        reporter.afterScenario(new Timing());
        reporter.afterStory(false);
    }

    private void performStage(Stage stage, ExecutionType type, Runnable steps)
    {
        reporter.beforeScenarioSteps(stage, type);
        steps.run();
        reporter.afterScenarioSteps(stage, type);
    }

    private void performStep(String step)
    {
        reporter.beforeStep(createStep(step));
        reporter.successful(step);
    }

    private static Step createStep(String step)
    {
        return new Step(StepExecutionType.EXECUTABLE, step);
    }

    private TestResult getWrittenTestResult()
    {
        ArgumentCaptor<TestResult> testResultCaptor = ArgumentCaptor.forClass(TestResult.class);
        verify(resultsWriter).write(testResultCaptor.capture());
        return testResultCaptor.getValue();
    }

    private static void assertStepNames(List<StepResult> steps, String... expectedNames)
    {
        assertEquals(List.of(expectedNames), steps.stream().map(StepResult::getName).toList());
    }
}
