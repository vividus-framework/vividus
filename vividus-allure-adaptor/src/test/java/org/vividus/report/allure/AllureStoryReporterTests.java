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

package org.vividus.report.allure;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Consumer;

import com.google.common.collect.Maps;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.jbehave.core.failures.BeforeOrAfterFailed;
import org.jbehave.core.failures.UUIDExceptionWrapper;
import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.model.GivenStories;
import org.jbehave.core.model.Lifecycle;
import org.jbehave.core.model.Lifecycle.ExecutionType;
import org.jbehave.core.model.Meta;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Step;
import org.jbehave.core.model.Story;
import org.jbehave.core.model.StoryDuration;
import org.jbehave.core.reporters.StoryReporter;
import org.jbehave.core.steps.StepCollector.Stage;
import org.jbehave.core.steps.StepCreator.PendingStep;
import org.jbehave.core.steps.StepCreator.StepExecutionType;
import org.jbehave.core.steps.Timing;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.batch.BatchConfiguration;
import org.vividus.batch.BatchStorage;
import org.vividus.context.ReportControlContext;
import org.vividus.context.RunTestContext;
import org.vividus.model.RunningScenario;
import org.vividus.model.RunningStory;
import org.vividus.report.allure.AllureStoryReporter.LinkedQueueItem;
import org.vividus.report.allure.adapter.IVerificationErrorAdapter;
import org.vividus.report.allure.model.ScenarioExecutionStage;
import org.vividus.report.allure.model.StoryExecutionStage;
import org.vividus.reporter.event.LinkPublishEvent;
import org.vividus.softassert.event.AssertionFailedEvent;
import org.vividus.softassert.exception.VerificationError;
import org.vividus.softassert.issue.KnownIssueIdentifier;
import org.vividus.softassert.model.KnownIssue;
import org.vividus.softassert.model.SoftAssertionError;
import org.vividus.testcontext.SimpleTestContext;
import org.vividus.testcontext.TestContext;

import io.qameta.allure.AllureLifecycle;
import io.qameta.allure.entity.LabelName;
import io.qameta.allure.model.Label;
import io.qameta.allure.model.Link;
import io.qameta.allure.model.Parameter;
import io.qameta.allure.model.Status;
import io.qameta.allure.model.StatusDetails;
import io.qameta.allure.model.StepResult;
import io.qameta.allure.model.TestResult;
import io.qameta.allure.util.ResultsUtils;

@SuppressWarnings("MethodCount")
@ExtendWith(MockitoExtension.class)
class AllureStoryReporterTests
{
    private static final String STORY_TIMEOUT_MESSAGE = "Story timed out after 0s";
    private static final String LOG_ENTRY = "logEntry";
    private static final String DASH = "-";
    private static final String BATCH_NAME = "my-batch";
    private static final String BASE_STORY = "BaseStory";
    private static final String GIVEN_STORY = "GivenStory";
    private static final String SCENARIO = "Scenario";
    private static final String GIVEN_STEP = "Given step";
    private static final String SCENARIO_UID_PATTERN = "%s[%d]";
    private static final String SCENARIO_UID = "scenarioUid";
    private static final long THREAD_ID = Thread.currentThread().getId();
    private static final String STEP_UID = SCENARIO_UID + DASH + THREAD_ID;
    private static final String SUB_STEP_UID = STEP_UID + DASH + THREAD_ID;
    private static final String CURRENT_STEP_KEY = "allureCurrentLinkedStep";
    private static final String STORY_NAME = "name";
    private static final String EXPECTED_SCENARIO_TEST_CASE_ID = "testScenarioTestCaseId";
    private static final String EXPECTED_SCENARIO_REQUIREMENT_ID = "testScenarioRequirementId";

    private static final String ALLURE_LINK_ISSUE_PROPERTY = "allure.link.issue.pattern";
    private static final String ALLURE_LINK_TMS_PROPERTY = "allure.link.tms.pattern";
    private static final String ALLURE_LINK_ISSUE_DEV_PROPERTY = "allure.link.issue.dev.pattern";

    private static final String ISSUE_LINK_PREFIX = "https://issue/";
    private static final String TMS_LINK_PREFIX = "https://tms/";
    private static final String ISSUE_LINK_DEV_PREFIX = "https://vividus.dev/";

    private static final String LIFECYCLE_BEFORE_STORY = "Lifecycle: Before story";
    private static final String LIFECYCLE_AFTER_STORY = "Lifecycle: After story";
    private static final String COMMENT_STEP = "!-- Comment";

    private static final String TEST_CASE_ID = "testCaseId";
    private static final String REQUIREMENT_ID = "requirementId";

    @Captor private ArgumentCaptor<TestResult> testResultCaptor;
    @Mock private IAllureReportGenerator allureReportGenerator;
    @Mock private BatchStorage batchStorage;
    @Spy private TestContext testContext = new SimpleTestContext();
    @Mock private IAllureRunContext allureRunContext;
    @Mock private IVerificationErrorAdapter verificationErrorAdapter;
    @Mock private StoryReporter next;
    @Mock private AllureLifecycle allureLifecycle;
    @Mock private ReportControlContext reportControlContext;

    private LinkedQueueItem<String> linkedQueueItem;
    private String scenarioUid;
    private RunTestContext runTestContext;

    private AllureStoryReporter allureStoryReporter;

    @BeforeAll
    static void beforeAll()
    {
        String placeholder = "{}";
        System.setProperty(ALLURE_LINK_ISSUE_PROPERTY, ISSUE_LINK_PREFIX + placeholder);
        System.setProperty(ALLURE_LINK_TMS_PROPERTY, TMS_LINK_PREFIX + placeholder);
        System.setProperty(ALLURE_LINK_ISSUE_DEV_PROPERTY, ISSUE_LINK_DEV_PREFIX + placeholder);
    }

    @AfterAll
    static void afterAll()
    {
        System.clearProperty(ALLURE_LINK_ISSUE_PROPERTY);
        System.clearProperty(ALLURE_LINK_TMS_PROPERTY);
        System.clearProperty(ALLURE_LINK_ISSUE_DEV_PROPERTY);
    }

    @BeforeEach
    void beforeEach() throws IllegalAccessException
    {
        RunTestContext context = new RunTestContext();
        context.setTestContext(testContext);
        runTestContext = spy(context);
        allureStoryReporter = new AllureStoryReporter(reportControlContext, runTestContext, allureReportGenerator,
                batchStorage, testContext, allureRunContext, verificationErrorAdapter);
        FieldUtils.writeField(allureStoryReporter, "lifecycle", allureLifecycle, true);
        linkedQueueItem = new LinkedQueueItem<>(SCENARIO_UID);
        allureStoryReporter.setNext(next);
    }

    @Test
    void testNonFirstExampleEventHandling()
    {
        lenient().when(testContext.get(CURRENT_STEP_KEY))
                .thenReturn(null)
                .thenReturn(linkedQueueItem)
                .thenReturn(linkedQueueItem)
                .thenReturn(linkedQueueItem)
                .thenReturn(null);
        when(allureRunContext.getCurrentStoryLabels()).thenReturn(new ArrayList<>());
        Entry<String, String> tableRow = testExampleHandling(1);
        verify(allureLifecycle).updateStep(eq(null), argThat(updater -> {
            StepResult stepResult = new StepResult();
            updater.accept(stepResult);
            assertParameters(tableRow, stepResult.getParameters());
            return true;
        }));
    }

    @Test
    void testFirstExampleEventHandling()
    {
        lenient().when(testContext.get(CURRENT_STEP_KEY))
                .thenReturn(null)
                .thenReturn(null)
                .thenReturn(null)
                .thenReturn(linkedQueueItem);
        Entry<String, String> tableRow = testExampleHandling(0);
        verify(allureLifecycle).updateTestCase(eq(linkedQueueItem.getValue()), argThat(updater -> {
            TestResult testResult = new TestResult();
            updater.accept(testResult);
            assertParameters(tableRow, testResult.getParameters());
            return true;
        }));
    }

    @ParameterizedTest
    @CsvSource({
            "BEFORE, BEFORE_STEPS",
            "AFTER,  AFTER_STEPS"
    })
    void shouldSetExecutionStageInBeforeScenarioSteps(Stage stage, ScenarioExecutionStage scenarioExecutionStage)
    {
        allureStoryReporter.beforeScenarioSteps(stage, ExecutionType.SYSTEM);
        InOrder ordered = inOrder(allureRunContext, next);
        ordered.verify(allureRunContext).setScenarioExecutionStage(scenarioExecutionStage);
        ordered.verify(next).beforeScenarioSteps(stage, ExecutionType.SYSTEM);
    }

    @Test
    void shouldDoNothingInBeforeScenarioStepsIfStageIsUnknown()
    {
        allureStoryReporter.beforeScenarioSteps(null, ExecutionType.USER);
        verify(next).beforeScenarioSteps(null, ExecutionType.USER);
        verifyNoInteractions(allureRunContext);
    }

    @Test
    void shouldResetExecutionStageInAfterScenarioSteps()
    {
        allureStoryReporter.afterScenarioSteps(Stage.AFTER, ExecutionType.SYSTEM);
        InOrder ordered = inOrder(allureRunContext, next);
        ordered.verify(next).afterScenarioSteps(Stage.AFTER, ExecutionType.SYSTEM);
        ordered.verify(allureRunContext).resetScenarioExecutionStage();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testBeforeStoryDefault(boolean dryRun)
    {
        when(runTestContext.isDryRun()).thenReturn(dryRun);
        List<Label> storyLabels = mockStoryStart(false);
        testBeforeStory(BASE_STORY, false);
        verifyNoInteractions(allureReportGenerator);
        assertEquals(6, storyLabels.size());
        assertLabel(LabelName.PARENT_SUITE, BATCH_NAME, storyLabels.get(4));
        assertLabel(LabelName.SUITE, BASE_STORY, storyLabels.get(5));
        assertLabel(LabelName.STORY, BASE_STORY, storyLabels.get(3));
    }

    @Test
    void testBeforeStoryDefaultGivenStory()
    {
        when(allureRunContext.getRootStoryLabels()).thenReturn(
                List.of(new Label().setName(LabelName.SUITE.value()).setValue(BASE_STORY)));
        List<Label> storyLabels = mockStoryStart(true);
        testBeforeStory(GIVEN_STORY, true);
        verifyNoInteractions(allureReportGenerator);
        assertEquals(7, storyLabels.size());
        assertLabel(LabelName.PARENT_SUITE, BATCH_NAME, storyLabels.get(4));
        assertLabel(LabelName.SUITE, BASE_STORY, storyLabels.get(5));
        assertLabel(LabelName.SUB_SUITE, "Given Story: " + GIVEN_STORY, storyLabels.get(6));
        assertLabel(LabelName.STORY, GIVEN_STORY, storyLabels.get(3));
    }

    @Test
    void testBeforeStoryScenarioGivenStory()
    {
        Story story = mockRunningStoryWithSeverity(true).getStory();
        story.namedAs("");
        mockScenarioUid(false);
        allureStoryReporter.beforeStory(story, true);
        verify(next).beforeStory(story, true);
        verifyNoInteractions(allureReportGenerator);
        verify(allureLifecycle).startStep(eq(SCENARIO_UID), eq(STEP_UID), any(StepResult.class));
    }

    @Test
    void testBeforeStoryScenarioGivenStoryDryRun()
    {
        mockRunningBatchName();
        when(allureRunContext.getRootStoryLabels()).thenReturn(
                List.of(new Label().setName(LabelName.SUITE.value()).setValue(BASE_STORY)));
        when(runTestContext.isDryRun()).thenReturn(true);
        Story story = mockRunningStoryWithSeverity(true).getStory();
        story.namedAs("");
        allureStoryReporter.beforeStory(story, true);
        verify(next).beforeStory(story, true);
        verifyNoInteractions(allureReportGenerator);
    }

    @Test
    void testBeforeExcludedRunningStory()
    {
        boolean givenStory = true;
        Story story = new Story();
        mockRunningStory(false);
        allureStoryReporter.beforeStory(story, givenStory);
        verify(next).beforeStory(story, givenStory);
        verifyNoInteractions(allureReportGenerator, allureRunContext, allureLifecycle);
    }

    @Test
    void testBeforeStoriesWithBeforeStage()
    {
        allureStoryReporter.beforeStoriesSteps(Stage.BEFORE);
        verify(next).beforeStoriesSteps(Stage.BEFORE);
        verify(allureReportGenerator).start();
    }

    @Test
    void testBeforeStoriesWithAfterStage()
    {
        allureStoryReporter.beforeStoriesSteps(Stage.AFTER);
        verify(next).beforeStoriesSteps(Stage.AFTER);
        verifyNoInteractions(allureReportGenerator);
    }

    @Test
    void testAfterStoriesWithBeforeStage()
    {
        allureStoryReporter.afterStoriesSteps(Stage.BEFORE);
        verify(next).afterStoriesSteps(Stage.BEFORE);
        verifyNoInteractions(allureReportGenerator);
    }

    @Test
    void testAfterStoriesWithAfterStage()
    {
        allureStoryReporter.afterStoriesSteps(Stage.AFTER);
        verify(next).afterStoriesSteps(Stage.AFTER);
        verify(allureReportGenerator).end();
    }

    @Test
    void testBeforeStorySameNames()
    {
        List<Label> story1Labels = new ArrayList<>();
        List<Label> story2Labels = new ArrayList<>();
        startStoriesWithSameName(story1Labels, story2Labels);
        assertLabel(LabelName.SUITE, STORY_NAME, story1Labels.get(5));
        assertLabel(LabelName.SUITE, STORY_NAME, story2Labels.get(5));
    }

    private void assertLabel(LabelName expectedName, String expectedValue, Label actual)
    {
        assertAll(
            () -> assertEquals(expectedName.value(), actual.getName()),
            () -> assertEquals(expectedValue, actual.getValue())
        );
    }

    private void testBeforeStory(String storyName, boolean givenStory)
    {
        Story story = mockRunningStoryWithSeverity(true).getStory();
        story.namedAs(storyName);
        allureStoryReporter.beforeStory(story, givenStory);
        verify(next).beforeStory(story, givenStory);
    }

    private List<Label> mockStoryStart(boolean givenStory)
    {
        mockRunningBatchName();
        List<Label> storyLabels = new LinkedList<>();
        when(allureRunContext.createNewStoryLabels(givenStory)).thenReturn(storyLabels);
        return storyLabels;
    }

    private void mockRunningBatchName()
    {
        String batchKey = mockBatchExecutionConfiguration();
        runTestContext.putRunningBatch(batchKey);
    }

    private String mockBatchExecutionConfiguration()
    {
        String batchKey = "batch-1";
        BatchConfiguration config = new BatchConfiguration();
        config.setName(BATCH_NAME);
        when(batchStorage.getBatchConfiguration(batchKey)).thenReturn(config);
        return batchKey;
    }

    @Test
    void testBeforeScenario()
    {
        RunningStory runningStory = mockRunningStoryWithSeverity(true);
        Scenario scenario = createScenario("New Scenario", getScenarioMeta(true),
                List.of());
        runningStory.setRunningScenario(getRunningScenario(scenario, 0));
        runningStory.setRunningScenario(getRunningScenario(scenario, 0));
        Story story = runningStory.getStory();
        boolean givenStory = false;
        mockStoryStart(givenStory);
        mockScenarioUid(true);
        allureStoryReporter.beforeStory(story, givenStory);
        allureStoryReporter.beforeScenario(scenario);
        verify(next).beforeScenario(scenario);
        verify(allureLifecycle).scheduleTestCase(testResultCaptor.capture());
        verify(allureLifecycle).startTestCase(scenarioUid);
        assertTestResultLabel(LabelName.SEVERITY, "critical");
        assertEquals("[batch: my-batch][stories-chain: name][scenario: New Scenario-1]",
                testResultCaptor.getValue().getHistoryId());
    }

    @Test
    void testBeforeScenarioInGivenStoryScenarioLevel()
    {
        Story story = mockRunningStoryWithSeverity(true).getStory();
        mockScenarioUid(false);
        Scenario scenario = story.getScenarios().get(0);
        allureStoryReporter.beforeScenario(scenario);
        verify(next).beforeScenario(scenario);
        verify(allureLifecycle).startStep(eq(SCENARIO_UID), eq(STEP_UID), any(StepResult.class));
        verify(allureLifecycle, never()).scheduleTestCase(testResultCaptor.capture());
        verify(allureLifecycle, never()).startTestCase(scenarioUid);
    }

    @Test
    void testBeforeScenarioNoSeverity()
    {
        RunningStory runningStory = mockRunningStoryWithSeverity(false);
        Story story = runningStory.getStory();
        boolean givenStory = false;
        mockStoryStart(givenStory);
        mockScenarioUid(true);
        allureStoryReporter.beforeStory(story, givenStory);
        Scenario scenario = story.getScenarios().get(0);
        allureStoryReporter.beforeScenario(scenario);
        verify(next).beforeScenario(scenario);
        verify(allureLifecycle).scheduleTestCase(testResultCaptor.capture());
        verify(allureLifecycle).startTestCase(scenarioUid);
        List<Label> labels = testResultCaptor.getValue().getLabels();
        Optional<Label> label = labels.stream().filter(l -> LabelName.SEVERITY.value().equals(l.getName())).findFirst();
        assertFalse(label.isPresent());
    }

    @Test
    void testBeforeScenarioStoriesWithSameNames()
    {
        List<Label> story1Labels = new ArrayList<>();
        List<Label> story2Labels = new ArrayList<>();
        startStoriesWithSameName(story1Labels, story2Labels);
        when(allureRunContext.getCurrentStoryLabels()).thenReturn(story1Labels).thenReturn(story2Labels);
        mockScenarioUid(true);
        allureStoryReporter.beforeScenario(new Scenario("Scenario 1", Meta.EMPTY));
        verify(allureLifecycle).scheduleTestCase(testResultCaptor.capture());
        assertTestResultLabel(LabelName.SUITE, STORY_NAME);
    }

    @Test
    void testBeforeScenarioStoriesWithSameNamesGivenStory()
    {
        mockRunningBatchName();
        Properties scenarioMeta = getScenarioMeta(true);
        Properties storyMeta = getStoryMeta();
        RunningStory runningGivenStory = createRunningStory(storyMeta, scenarioMeta, List.of(), "given story");
        runTestContext.putRunningStory(runningGivenStory, true);
        runTestContext.putRunningStory(mockRunningStoryWithSeverity(true), false);
        Story givenStory = runningGivenStory.getStory();
        List<Label> givenStoryLabels = List.of(
                new Label().setName(LabelName.PARENT_SUITE.value()).setValue(STORY_NAME));
        when(allureRunContext.getCurrentStoryLabels()).thenReturn(givenStoryLabels);
        mockScenarioUid(true);
        allureStoryReporter.beforeScenario(givenStory.getScenarios().get(0));
        verify(allureLifecycle).scheduleTestCase(testResultCaptor.capture());
        assertTestResultLabel(LabelName.PARENT_SUITE, STORY_NAME);
        TestResult captured = testResultCaptor.getValue();
        assertEquals("[batch: my-batch][stories-chain: name > given story][scenario: Scenario-0]",
                captured.getHistoryId());
    }

    @Test
    void afterBeforeStoryStepsShouldStopLifecycleBeforeStorySteps()
    {
        when(allureRunContext.getStoryExecutionStage()).thenReturn(StoryExecutionStage.LIFECYCLE_BEFORE_STORY_STEPS);
        when(allureRunContext.isStepInProgress()).thenReturn(true);
        String currentStepId = "lifecycle-before-story-step";
        String currentScenarioId = LIFECYCLE_BEFORE_STORY;
        testContext.put(CURRENT_STEP_KEY, new LinkedQueueItem<>(currentScenarioId).attachItem(currentStepId));

        allureStoryReporter.afterStorySteps(Stage.BEFORE, ExecutionType.USER);

        InOrder ordered = inOrder(allureLifecycle, allureRunContext, next);
        ordered.verify(next).afterStorySteps(Stage.BEFORE, ExecutionType.USER);
        ordered.verify(allureLifecycle).updateStep(eq(currentStepId), anyStepResultConsumer());
        ordered.verify(allureLifecycle).stopStep(currentStepId);
        ordered.verify(allureLifecycle).updateTestCase(eq(currentScenarioId), anyTestResultConsumer());
        ordered.verify(allureLifecycle).stopTestCase(currentScenarioId);
        ordered.verify(allureLifecycle).writeTestCase(currentScenarioId);
        verifyNoMoreInteractions(next, allureLifecycle);
        assertNull(testContext.get(CURRENT_STEP_KEY, LinkedQueueItem.class));
    }

    @Test
    void afterEmptyBeforeStorySteps()
    {
        when(allureRunContext.getStoryExecutionStage()).thenReturn(null);
        allureStoryReporter.afterStorySteps(Stage.BEFORE, ExecutionType.USER);
        InOrder ordered = inOrder(allureLifecycle, allureRunContext, next);
        ordered.verify(next).afterStorySteps(Stage.BEFORE, ExecutionType.USER);
        ordered.verify(allureRunContext).getStoryExecutionStage();
        ordered.verifyNoMoreInteractions();
    }

    @Test
    void afterAfterStorySteps()
    {
        allureStoryReporter.afterStorySteps(Stage.AFTER, ExecutionType.USER);
        InOrder ordered = inOrder(allureLifecycle, allureRunContext, next);
        ordered.verify(next).afterStorySteps(Stage.AFTER, ExecutionType.USER);
        ordered.verifyNoMoreInteractions();
    }

    private void assertTestResultLabel(LabelName labelName, String labelValue)
    {
        assertTestResultLabel(labelName.value(), labelValue);
    }

    private void assertTestResultLabel(String labelName, String labelValue)
    {
        List<Label> labels = testResultCaptor.getValue().getLabels();
        Optional<Label> label = labels.stream().filter(l -> labelName.equals(l.getName())).findFirst();
        assertTrue(label.isPresent());
        assertEquals(labelValue, label.get().getValue());
    }

    @SuppressWarnings("unchecked")
    private void startStoriesWithSameName(List<Label> story1Labels, List<Label> story2Labels)
    {
        String storyName = "name.story";
        RunningStory runningStory1 = createRunningStory(new Properties(), new Properties(), List.of(),
                "/story/path1/" + storyName);
        Story story1 = runningStory1.getStory();
        story1.namedAs(storyName);
        RunningStory runningStory2 = createRunningStory(new Properties(), new Properties(), List.of(),
                "/story/path2/" + storyName);
        Story story2 = runningStory2.getStory();
        story2.namedAs(storyName);
        runTestContext.putRunningStory(runningStory1, false);
        runTestContext.putRunningStory(runningStory2, false);
        when(allureRunContext.createNewStoryLabels(false)).thenReturn(story1Labels, story2Labels);
        mockRunningBatchName();
        allureStoryReporter.beforeStory(story1, false);
        allureStoryReporter.beforeStory(story2, false);
    }

    @Test
    void testBeforeStep()
    {
        mockEnableReporting();
        mockScenarioUid(false);
        Step givenStep = createStep(GIVEN_STEP);
        allureStoryReporter.beforeStep(givenStep);
        verify(next).beforeStep(givenStep);
        verify(allureLifecycle).startStep(eq(SCENARIO_UID), eq(STEP_UID), any(StepResult.class));
    }

    @Test
    void beforeStepShouldStartLifecycleBeforeStorySteps()
    {
        mockEnableReporting();
        beforeStepShouldStartLifecycleStorySteps(null, "before-story-step", LIFECYCLE_BEFORE_STORY);
    }

    @Test
    void beforeStepShouldStartLifecycleAfterStorySteps()
    {
        mockEnableReporting();
        beforeStepShouldStartLifecycleStorySteps(StoryExecutionStage.AFTER_SCENARIO, "after-story-step",
                LIFECYCLE_AFTER_STORY);
    }

    private void beforeStepShouldStartLifecycleStorySteps(StoryExecutionStage storyExecutionStage, String stepAsString,
            String testCaseName)
    {
        mockBatchExecutionConfiguration();

        when(allureRunContext.getStoryExecutionStage()).thenReturn(storyExecutionStage);
        testContext.put(CURRENT_STEP_KEY, null);

        Story story = new Story(STORY_NAME, null, new Meta(getStoryMeta()), null, List.of());
        RunningStory runningStory = getRunningStory(story, null);
        runTestContext.putRunningStory(runningStory, false);

        Step step = createStep(stepAsString);
        allureStoryReporter.beforeStep(step);

        InOrder ordered = inOrder(allureLifecycle, next);
        ordered.verify(allureLifecycle).scheduleTestCase(testResultCaptor.capture());
        TestResult testResult = testResultCaptor.getValue();
        assertEquals(testCaseName, testResult.getName());
        assertEquals(String.format("[batch: my-batch][stories-chain: name][scenario: %s]", testCaseName),
                testResult.getHistoryId());
        ordered.verify(allureLifecycle).startTestCase(testResult.getUuid());
        ArgumentCaptor<StepResult> stepResultCaptor = ArgumentCaptor.forClass(StepResult.class);
        ordered.verify(allureLifecycle).startStep(eq(testResult.getUuid()), eq(testResult.getUuid() + DASH + THREAD_ID),
                stepResultCaptor.capture());
        StepResult capturedStepResult = stepResultCaptor.getValue();
        assertEquals(stepAsString, capturedStepResult.getName());
        assertEquals(Status.PASSED, capturedStepResult.getStatus());
        ordered.verify(next).beforeStep(step);
        verifyNoMoreInteractions(next, allureLifecycle);
    }

    private static Step createStep(String stepAsString)
    {
        return new Step(StepExecutionType.EXECUTABLE, stepAsString);
    }

    @Test
    void testSuccessful()
    {
        mockEnableReporting();
        mockStepUid();
        allureStoryReporter.successful(GIVEN_STEP);
        verify(next).successful(GIVEN_STEP);
        verify(allureLifecycle, times(2)).updateStep(eq(STEP_UID), anyStepResultConsumer());
        verify(allureLifecycle).stopStep(STEP_UID);
        verify(allureLifecycle).updateTestCase(eq(SCENARIO_UID), anyTestResultConsumer());
    }

    @Test
    void testAfterStory()
    {
        boolean givenStory = false;
        mockRunningStory(true);
        allureStoryReporter.afterStory(givenStory);
        InOrder ordered = inOrder(next, allureRunContext);
        ordered.verify(next).afterStory(givenStory);
        ordered.verify(allureRunContext).resetCurrentStoryLabels(givenStory);
        verifyNoInteractions(allureLifecycle);
    }

    @Test
    void testAfterExcludedRunningStory()
    {
        boolean givenStory = true;
        mockRunningStory(false);
        allureStoryReporter.afterStory(givenStory);
        verify(next).afterStory(givenStory);
        verifyNoInteractions(allureRunContext, allureLifecycle);
    }

    @Test
    void testAfterGivenStoryScenarioLevel()
    {
        mockScenarioUid(false);
        boolean givenStory = true;
        mockRunningStory(true);
        allureStoryReporter.afterStory(givenStory);
        InOrder ordered = inOrder(next, allureLifecycle, allureRunContext);
        ordered.verify(next).afterStory(givenStory);
        ordered.verify(allureLifecycle).stopStep(SCENARIO_UID);
        ordered.verify(allureRunContext).resetCurrentStoryLabels(givenStory);
    }

    @Test
    void afterStoryShouldStopLifecycleAfterStorySteps()
    {
        when(allureRunContext.getStoryExecutionStage()).thenReturn(StoryExecutionStage.LIFECYCLE_AFTER_STORY_STEPS);
        when(allureRunContext.isStepInProgress()).thenReturn(true);
        String currentStepId = "lifecycle-after-story-step";
        String currentScenarioId = LIFECYCLE_AFTER_STORY;
        testContext.put(CURRENT_STEP_KEY, new LinkedQueueItem<>(currentScenarioId).attachItem(currentStepId));

        boolean givenStory = false;
        RunningStory runningStory = new RunningStory();
        runningStory.setNotExcluded(true);
        runTestContext.putRunningStory(runningStory, givenStory);
        allureStoryReporter.afterStory(givenStory);
        InOrder ordered = inOrder(next, allureRunContext, allureLifecycle);
        ordered.verify(next).afterStory(givenStory);
        ordered.verify(allureLifecycle).updateStep(eq(currentStepId), anyStepResultConsumer());
        ordered.verify(allureLifecycle).stopStep(currentStepId);
        ordered.verify(allureLifecycle).updateTestCase(eq(currentScenarioId), anyTestResultConsumer());
        ordered.verify(allureLifecycle).stopTestCase(currentScenarioId);
        ordered.verify(allureLifecycle).writeTestCase(currentScenarioId);
        ordered.verify(allureRunContext).resetCurrentStoryLabels(givenStory);
        verifyNoMoreInteractions(next, allureLifecycle);
        assertNull(testContext.get(CURRENT_STEP_KEY));
    }

    @Test
    void testAfterScenario()
    {
        mockScenarioUid(false);
        Timing timing = mock(Timing.class);
        allureStoryReporter.afterScenario(timing);
        verify(next).afterScenario(timing);
        verify(allureLifecycle).stopTestCase(SCENARIO_UID);
        verify(allureLifecycle).writeTestCase(SCENARIO_UID);
        verify(testContext).put(CURRENT_STEP_KEY, null);
    }

    @Test
    void testAfterScenarioGivenStoryScenarioLevel()
    {
        mockStepUid();
        Timing timing = mock(Timing.class);
        allureStoryReporter.afterScenario(timing);
        verify(next).afterScenario(timing);
        verify(allureLifecycle).stopStep(STEP_UID);
        verify(allureLifecycle, never()).stopTestCase(SCENARIO_UID);
        verify(allureLifecycle, never()).writeTestCase(SCENARIO_UID);
    }

    @Test
    void testAddLogStep()
    {
        mockStepUid();
        mockAddLogStep(null);
        verify(allureLifecycle).startStep(eq(STEP_UID), anyString(),
                argThat(s -> LOG_ENTRY.equals(s.getName())));
        verify(allureLifecycle).stopStep(anyString());
    }

    @Test
    void testAddLogBeforeAnyScenarioStepIsStarted()
    {
        mockStepUid();
        when(allureRunContext.isStepInProgress()).thenReturn(false);
        mockAddLogStep(ScenarioExecutionStage.BEFORE_STEPS);
        verify(allureLifecycle).startStep(eq(STEP_UID), eq(SUB_STEP_UID),
                argThat(s -> "@BeforeScenario".equals(s.getName())));
        verify(allureRunContext).startStep();
        verify(allureLifecycle).startStep(eq(STEP_UID), anyString(), argThat(s -> LOG_ENTRY.equals(s.getName())));
        verify(allureLifecycle).stopStep(anyString());
        verifyNoMoreInteractions(allureLifecycle);
    }

    @Test
    void testAddLogInBeforeScenarioStep()
    {
        mockStepUid();
        when(allureRunContext.isStepInProgress()).thenReturn(true);
        allureStoryReporter.addLogStep("INFO", LOG_ENTRY);
        verify(allureLifecycle).startStep(eq(STEP_UID), anyString(), argThat(s -> LOG_ENTRY.equals(s.getName())));
        verify(allureLifecycle).stopStep(anyString());
        verifyNoMoreInteractions(allureLifecycle);
    }

    @Test
    void testAddLogAfterStepNotRoot()
    {
        mockStepUid();
        mockAddLogStep(ScenarioExecutionStage.AFTER_STEPS);
        verify(allureLifecycle).startStep(eq(STEP_UID), anyString(),
                argThat(s -> LOG_ENTRY.equals(s.getName())));
        verify(allureLifecycle).stopStep(anyString());
        verify(allureRunContext, never()).startStep();
    }

    @Test
    void testAddLogAfterAllScenarioStepsAreFinished()
    {
        mockScenarioUid(false);
        when(allureRunContext.isStepInProgress()).thenReturn(false);
        when(allureRunContext.getScenarioExecutionStage()).thenReturn(ScenarioExecutionStage.AFTER_STEPS);
        when(allureRunContext.getStoryExecutionStage()).thenReturn(StoryExecutionStage.BEFORE_SCENARIO);
        allureStoryReporter.addLogStep("DEBUG", LOG_ENTRY);
        verify(allureRunContext).startStep();
        verify(allureLifecycle).startStep(eq(SCENARIO_UID), eq(STEP_UID),
                argThat(s -> "@AfterScenario".equals(s.getName())));
        verify(allureLifecycle).startStep(eq(SCENARIO_UID), anyString(), argThat(s -> LOG_ENTRY.equals(s.getName())));
        verify(allureLifecycle).stopStep(anyString());
        verifyNoMoreInteractions(allureLifecycle);
    }

    @Test
    void testAddLogInAfterScenarioStep()
    {
        mockScenarioUid(false);
        when(allureRunContext.isStepInProgress()).thenReturn(true);
        allureStoryReporter.addLogStep("ERROR", LOG_ENTRY);
        verify(allureLifecycle).startStep(eq(SCENARIO_UID), anyString(), argThat(s -> LOG_ENTRY.equals(s.getName())));
        verify(allureLifecycle).stopStep(anyString());
        verifyNoMoreInteractions(allureLifecycle);
    }

    @Test
    void testAddLogStepIsNull()
    {
        mockScenarioUid(true);
        mockAddLogStep(null);
        verify(allureRunContext, never()).startStep();
        verifyNoInteractions(allureLifecycle);
    }

    @Test
    void testAddLogStepInProgress()
    {
        mockStepUid();
        mockAddLogStep(null);
        verify(allureLifecycle).startStep(eq(STEP_UID), anyString(),
                argThat(s -> LOG_ENTRY.equals(s.getName())));
        verify(allureLifecycle).stopStep(anyString());
        verify(allureRunContext, never()).startStep();
    }

    private void mockAddLogStep(ScenarioExecutionStage status)
    {
        when(allureRunContext.getScenarioExecutionStage()).thenReturn(status);
        allureStoryReporter.addLogStep("logLevel", LOG_ENTRY);
    }

    @Test
    void testStoryCancelledNoRunningStep()
    {
        mockScenarioUid(false);
        Story story = createRunningStory(true).getStory();
        StoryDuration storyDuration = new StoryDuration(0);
        allureStoryReporter.storyCancelled(story, storyDuration);
        verify(allureLifecycle, never()).stopStep(STEP_UID);
        verify(allureLifecycle).stopTestCase(SCENARIO_UID);
        verify(allureLifecycle).writeTestCase(SCENARIO_UID);
        verify(allureLifecycle).updateTestCase(eq(SCENARIO_UID), storyCancelConsumerMatcher());
    }

    @Test
    void testStoryCancelledStopTestCaseAndStep()
    {
        linkedQueueItem = linkedQueueItem.attachItem(STEP_UID);
        when(testContext.get(CURRENT_STEP_KEY)).thenReturn(linkedQueueItem).thenReturn(linkedQueueItem)
                .thenReturn(linkedQueueItem).thenReturn(new LinkedQueueItem<>(SCENARIO_UID));
        Story story = createRunningStory(true).getStory();
        StoryDuration storyDuration = new StoryDuration(0);
        allureStoryReporter.storyCancelled(story, storyDuration);
        verify(allureLifecycle).stopStep(STEP_UID);
        verify(allureLifecycle).stopTestCase(SCENARIO_UID);
        verify(allureLifecycle).writeTestCase(SCENARIO_UID);
        verify(allureLifecycle).updateTestCase(eq(SCENARIO_UID), storyCancelConsumerMatcher());
    }

    private Consumer<TestResult> storyCancelConsumerMatcher()
    {
        return argThat(c -> {
            TestResult testResult = new TestResult();
            c.accept(testResult);
            return testResult.getStatus().equals(Status.BROKEN)
                    && testResult.getStatusDetails().getMessage().equals(STORY_TIMEOUT_MESSAGE);
        });
    }

    @Test
    void testIgnorable()
    {
        mockStepUid();
        allureStoryReporter.ignorable(GIVEN_STEP);
        verify(next).ignorable(GIVEN_STEP);
        verify(allureLifecycle).updateStep(eq(STEP_UID), anyStepResultConsumer());
        verify(allureLifecycle).stopStep(STEP_UID);
        verify(allureLifecycle).updateTestCase(eq(SCENARIO_UID), anyTestResultConsumer());
    }

    @Test
    void testComment()
    {
        mockStepUid();
        allureStoryReporter.comment(COMMENT_STEP);
        verify(testContext, never()).get(ScenarioExecutionStage.class, ScenarioExecutionStage.class);
        verify(testContext, never()).put(eq(ScenarioExecutionStage.class), any(ScenarioExecutionStage.class));
        verify(next).comment(COMMENT_STEP);
        verify(allureLifecycle).updateStep(eq(STEP_UID), anyStepResultConsumer());
        verify(allureLifecycle).stopStep(STEP_UID);
        verify(allureLifecycle, never()).updateTestCase(eq(SCENARIO_UID), anyTestResultConsumer());
    }

    @Test
    void testCommentDoesNotAffectTestCaseStatusIfOtherStepsArePresent()
    {
        mockEnableReporting();
        RunningStory runningStory = mockRunningStoryWithSeverity(true);
        Scenario scenario = createScenario(SCENARIO, getScenarioMeta(true),
                List.of());
        runningStory.setRunningScenario(getRunningScenario(scenario, 0));
        runningStory.setRunningScenario(getRunningScenario(scenario, 0));
        Story story = runningStory.getStory();
        boolean givenStory = false;
        mockStoryStart(givenStory);
        mockScenarioUid(true);
        allureStoryReporter.beforeStory(story, givenStory);
        allureStoryReporter.beforeScenario(scenario);

        mockStepUid();
        allureStoryReporter.successful(GIVEN_STEP);
        verify(allureLifecycle, times(2)).updateStep(eq(STEP_UID), anyStepResultConsumer());
        verify(allureLifecycle, times(1)).updateTestCase(eq(SCENARIO_UID), anyTestResultConsumer());

        allureStoryReporter.beforeScenario(scenario);
        String commentStepUID = STEP_UID + "_comment";
        linkedQueueItem = linkedQueueItem.attachItem(commentStepUID);
        mockScenarioUid(false);
        allureStoryReporter.comment(COMMENT_STEP);
        verify(allureLifecycle, times(1)).updateStep(eq(commentStepUID), anyStepResultConsumer());
        verify(allureLifecycle, times(1)).updateTestCase(eq(SCENARIO_UID), anyTestResultConsumer());
    }

    @Test
    void testPending()
    {
        mockStepUid();
        PendingStep pendingStep = new PendingStep(GIVEN_STEP, null);
        allureStoryReporter.pending(pendingStep);
        verify(next).pending(pendingStep);
        verify(allureLifecycle).updateStep(eq(STEP_UID), anyStepResultConsumer());
        verify(allureLifecycle).updateTestCase(eq(SCENARIO_UID), argThat(consumer -> {
            consumer.accept(
                    new TestResult().setStatusDetails(new StatusDetails().setMessage("The step is commented")));
            return true;
        }));
    }

    @Test
    void testNotPerformed()
    {
        mockStepUid();
        allureStoryReporter.notPerformed(GIVEN_STEP);
        verify(next).notPerformed(GIVEN_STEP);
        verify(allureLifecycle).updateStep(eq(STEP_UID), anyStepResultConsumer());
        verify(allureLifecycle).stopStep(STEP_UID);
        verify(allureLifecycle).updateTestCase(eq(SCENARIO_UID), anyTestResultConsumer());
    }

    @Test
    void testFailed()
    {
        testFailed(new UUIDExceptionWrapper());
        verify(allureLifecycle, times(2)).updateStep(eq(STEP_UID), anyStepResultConsumer());
        verify(allureLifecycle).stopStep(STEP_UID);
        verify(allureLifecycle).updateTestCase(eq(SCENARIO_UID), anyTestResultConsumer());
    }

    @Test
    void shouldReportSystemAfterStoryStepInCaseOfFailure()
    {
        Story story = new Story();
        RunningStory runningStory = new RunningStory();
        runningStory.setStory(story);
        runTestContext.putRunningStory(runningStory, false);
        mockRunningBatchName();
        when(allureRunContext.isStepInProgress()).thenReturn(false);
        when(allureRunContext.getStoryExecutionStage()).thenReturn(StoryExecutionStage.SYSTEM_AFTER_STORY_STEPS);
        var throwable = new IllegalStateException();

        allureStoryReporter.failed(GIVEN_STEP, throwable);

        var ordered = Mockito.inOrder(next, allureLifecycle, allureRunContext);
        ordered.verify(next).failed(GIVEN_STEP, throwable);
        ordered.verify(allureLifecycle).scheduleTestCase(
            argThat(tr -> "System After Story step: Given step".equals(tr.getName())));
        var testCaseUuidCaptor = ArgumentCaptor.forClass(String.class);
        ordered.verify(allureLifecycle).startTestCase(testCaseUuidCaptor.capture());
        var testCaseUuid = testCaseUuidCaptor.getValue();
        ordered.verify(allureRunContext).setStoryExecutionStage(StoryExecutionStage.SYSTEM_AFTER_STORY_STEPS);
        var stepUuidCaptor = ArgumentCaptor.forClass(String.class);
        ordered.verify(allureLifecycle).startStep(eq(testCaseUuid), stepUuidCaptor.capture(),
                argThat(r -> Status.PASSED == r.getStatus()));
        ordered.verify(allureRunContext).startStep();
        ordered.verify(allureLifecycle).stopStep(stepUuidCaptor.getValue());
        ordered.verify(allureRunContext).stopStep();
        ordered.verify(allureLifecycle).stopTestCase(testCaseUuid);
        ordered.verify(allureLifecycle).writeTestCase(testCaseUuid);
    }

    @Test
    void testFailedCreateBeforeScenarioStep()
    {
        when(allureRunContext.getScenarioExecutionStage()).thenReturn(ScenarioExecutionStage.BEFORE_STEPS);
        testFailed(new UUIDExceptionWrapper());
        verify(allureLifecycle, times(2)).updateStep(eq(STEP_UID), anyStepResultConsumer());
        verify(allureLifecycle).startStep(eq(STEP_UID), eq(SUB_STEP_UID), any(StepResult.class));
        verify(allureRunContext).startStep();
        verify(allureLifecycle).stopStep(STEP_UID);
        verify(allureRunContext).stopStep();
        verify(allureLifecycle).updateTestCase(eq(SCENARIO_UID), anyTestResultConsumer());
    }

    @Test
    void testFailedBeforeOrAfterFailed()
    {
        Throwable throwable = new UUIDExceptionWrapper();
        testFailed(throwable.initCause(new BeforeOrAfterFailed(throwable)));
        verify(allureLifecycle).updateStep(eq(STEP_UID), anyStepResultConsumer());
        verify(allureLifecycle).stopStep(STEP_UID);
        verify(allureLifecycle).updateTestCase(eq(SCENARIO_UID), anyTestResultConsumer());
    }

    @Test
    void testFailedVerificationError()
    {
        VerificationError verificationError = mock(VerificationError.class);
        KnownIssue knownIssue = mock(KnownIssue.class);
        when(verificationErrorAdapter.adapt(verificationError)).thenReturn(verificationError);
        when(verificationError.getKnownIssues()).thenReturn(Collections.singleton(knownIssue));
        testFailed(verificationError);
        verify(verificationErrorAdapter).adapt(verificationError);
        verify(allureLifecycle, times(2)).updateStep(eq(STEP_UID), anyStepResultConsumer());
        verify(allureLifecycle).stopStep(STEP_UID);
        verify(allureLifecycle, times(2)).updateTestCase(eq(SCENARIO_UID),
                anyTestResultConsumer());
    }

    @Test
    void testAddTestCaseInfo()
    {
        putEmptyRunningScenario(runTestContext);

        mockScenarioUid(true);
        boolean givenStory = false;
        mockStoryStart(givenStory);
        Properties scenarioMeta = getScenarioMeta(false);
        putDefaultTestCaseMetaProperties(scenarioMeta);
        scenarioMeta.put("issueId.dev", "VVD-1");
        scenarioMeta.put("check-override", "scenario");
        Scenario scenario1 = createScenario(scenarioMeta, List.of());
        Scenario scenario2 = createScenario(scenarioMeta, List.of());
        RunningScenario runningScenario = getRunningScenario(scenario1, 0);
        Meta storyMeta = new Meta(List.of("check-override story"));
        Story story = new Story(STORY_NAME, null, storyMeta, null, List.of(scenario1, scenario2));
        RunningStory runningStory = getRunningStory(story, runningScenario);
        runTestContext.putRunningStory(runningStory, false);
        allureStoryReporter.beforeStory(story, givenStory);
        allureStoryReporter.beforeScenario(story.getScenarios().get(0));
        verify(allureLifecycle).scheduleTestCase(testResultCaptor.capture());
        TestResult captured = testResultCaptor.getValue();
        assertEquals("[batch: my-batch][stories-chain: name][scenario: Scenario-0]", captured.getHistoryId());
        assertTestResultLabel(TEST_CASE_ID, EXPECTED_SCENARIO_TEST_CASE_ID);
        assertTestResultLabel(REQUIREMENT_ID, EXPECTED_SCENARIO_REQUIREMENT_ID);
        List<Link> links = captured.getLinks();
        assertThat(links, hasSize(3));
        assertEquals(TMS_LINK_PREFIX + EXPECTED_SCENARIO_TEST_CASE_ID, links.get(0).getUrl());
        Link issue = links.get(1);
        assertEquals("https://vividus.dev/VVD-1", issue.getUrl());
        assertEquals("issue", issue.getType());
        assertEquals(Status.PASSED, captured.getStatus());

        List<Label> labels = captured.getLabels();
        assertEquals(6, labels.size());

        assertEquals(ResultsUtils.createTagLabel("@check-override scenario"), labels.get(3));
        assertEquals(ResultsUtils.createTagLabel("@scenario-meta-1d"), labels.get(4));
        assertEquals(ResultsUtils.createTagLabel("@scenario-meta-2d scenario-meta-value"), labels.get(5));
    }

    @Test
    void testAddTestCaseInfoIdenticalValuesOnStoreAndScenarioLevel()
    {
        mockScenarioUid(true);
        boolean givenStory = false;
        mockStoryStart(givenStory);
        RunningStory runningStory = mockRunningStory(
                putDefaultTestCaseMetaProperties(new Properties()),
                putDefaultTestCaseMetaProperties(getScenarioMeta(false)), List.of());
        Story story = runningStory.getStory();
        allureStoryReporter.beforeStory(story, givenStory);
        allureStoryReporter.beforeScenario(story.getScenarios().get(0));
        verify(allureLifecycle).scheduleTestCase(testResultCaptor.capture());
        List<Label> labels = testResultCaptor.getValue().getLabels();
        assertEquals(1, labels.stream().filter(l -> TEST_CASE_ID.equals(l.getName())).count());
        assertEquals(1, labels.stream().filter(l -> REQUIREMENT_ID.equals(l.getName())).count());
    }

    @Test
    void shouldAddPublishedLink()
    {
        String linkName = "link-name";
        String linkUrl = "link-url";
        LinkPublishEvent event = new LinkPublishEvent(linkName, linkUrl);

        List<Link> links = new ArrayList<>();
        TestResult testResult = mock(TestResult.class);
        when(testResult.getLinks()).thenReturn(links);
        doAnswer(a ->
        {
            Consumer<TestResult> consumer = a.getArgument(1);
            consumer.accept(testResult);
            return null;
        }).when(allureLifecycle).updateTestCase(eq(SCENARIO_UID), any());

        mockScenarioUid(false);

        allureStoryReporter.onLinkPublish(event);
        allureStoryReporter.onLinkPublish(event);

        assertThat(links, hasSize(1));
        Link link = links.get(0);
        assertEquals(linkName, link.getName());
        assertEquals(linkUrl, link.getUrl());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "PASSED")
    void testOnAssertionFailure(Status initialStepStatus)
    {
        StepResult stepResult = mockCurrentStep(initialStepStatus);
        AssertionFailedEvent assertionFailedEvent = mockSoftAssertionError(null);
        allureStoryReporter.onAssertionFailure(assertionFailedEvent);
        verify(stepResult).setStatus(Status.FAILED);
    }

    @Test
    void testOnAssertionFailureKnownIssue()
    {
        StepResult stepResult = mockCurrentStep(Status.PASSED);
        AssertionFailedEvent assertionFailedEvent = mockSoftAssertionError(
                new KnownIssue(null, new KnownIssueIdentifier(), false));
        allureStoryReporter.onAssertionFailure(assertionFailedEvent);
        verify(stepResult).setStatus(null);
    }

    @Test
    void testOnAssertionFailureKnownIssueFixed()
    {
        StepResult stepResult = mockCurrentStep(Status.PASSED);
        KnownIssue knownIssue = new KnownIssue(null, new KnownIssueIdentifier(), false);
        knownIssue.setStatus(Optional.of("Closed"));
        knownIssue.setResolution(Optional.of("Fixed"));
        AssertionFailedEvent assertionFailedEvent = mockSoftAssertionError(knownIssue);
        allureStoryReporter.onAssertionFailure(assertionFailedEvent);
        verify(stepResult).setStatus(Status.FAILED);
    }

    @ParameterizedTest
    @CsvSource({"BEFORE, SYSTEM, 0", "BEFORE, USER, 0", "AFTER, SYSTEM, 1", "AFTER, USER, 0"})
    void shouldDetectSystemAfterStorySteps(Stage stage, Lifecycle.ExecutionType type, int stageSet)
    {
        allureStoryReporter.beforeStorySteps(stage, type);
        var ordered = Mockito.inOrder(next, allureRunContext);
        ordered.verify(allureRunContext, times(stageSet))
            .setStoryExecutionStage(StoryExecutionStage.SYSTEM_AFTER_STORY_STEPS);
        ordered.verify(next).beforeStorySteps(stage, type);
    }

    private StepResult mockCurrentStep(Status initialStepStatus)
    {
        mockStepUid();
        StepResult stepResult = mock(StepResult.class);
        lenient().doNothing().when(allureLifecycle).updateStep(eq(STEP_UID), argThat(update -> {
            update.accept(stepResult);
            return true;
        }));
        lenient().when(stepResult.getStatus()).thenReturn(initialStepStatus);
        return stepResult;
    }

    private AssertionFailedEvent mockSoftAssertionError(KnownIssue knownIssue)
    {
        SoftAssertionError softAssertionError = new SoftAssertionError(null);
        softAssertionError.setKnownIssue(knownIssue);
        AssertionFailedEvent assertionFailedEvent = mock(AssertionFailedEvent.class);
        when(assertionFailedEvent.getSoftAssertionError()).thenReturn(softAssertionError);
        return assertionFailedEvent;
    }

    private void testFailed(Throwable throwable)
    {
        mockStepUid();
        allureStoryReporter.failed(GIVEN_STEP, throwable);
        verify(next).failed(GIVEN_STEP, throwable);
    }

    private RunningStory mockRunningStoryWithSeverity(boolean useSeverity)
    {
        Properties scenarioMeta = getScenarioMeta(useSeverity);
        Properties storyMeta = getStoryMeta();
        return mockRunningStory(storyMeta, scenarioMeta, List.of());
    }

    private RunningStory mockRunningStory(Properties storyMeta, Properties scenarioMeta, List<String> steps)
    {
        RunningStory runningStory = createRunningStory(storyMeta, scenarioMeta, steps, STORY_NAME);
        runTestContext.putRunningStory(runningStory, false);
        return runningStory;
    }

    private RunningStory createRunningStory(boolean useSeverity)
    {
        Properties scenarioMeta = getScenarioMeta(useSeverity);
        Properties storyMeta = getStoryMeta();
        return createRunningStory(storyMeta, scenarioMeta, List.of(), STORY_NAME);
    }

    private RunningStory createRunningStory(Properties storyMeta, Properties scenarioMeta, List<String> steps,
            String storyPath)
    {
        Scenario scenario = createScenario(scenarioMeta, steps);
        RunningScenario runningScenario = getRunningScenario(scenario, 0);
        Story story = new Story(storyPath, null, new Meta(storyMeta), null, List.of(scenario));
        return getRunningStory(story, runningScenario);
    }

    private Scenario createScenario(Properties scenarioMeta, List<String> steps)
    {
        return createScenario(SCENARIO, scenarioMeta, steps);
    }

    private Scenario createScenario(String name, Properties scenarioMeta, List<String> steps)
    {
        return new Scenario(name, new Meta(scenarioMeta), GivenStories.EMPTY, ExamplesTable.EMPTY, steps);
    }

    private RunningStory getRunningStory(Story story, RunningScenario runningScenario)
    {
        RunningStory runningStory = new RunningStory();
        runningStory.setStory(story);
        runningStory.setNotExcluded(true);
        runningStory.setRunningScenario(runningScenario);
        return runningStory;
    }

    private Properties getScenarioMeta(boolean useSeverity)
    {
        Properties scenarioMeta = new Properties();
        scenarioMeta.setProperty("scenario-meta-1d", "");
        scenarioMeta.setProperty("scenario-meta-2d", "scenario-meta-value");
        if (useSeverity)
        {
            scenarioMeta.setProperty("severity", "2");
        }
        return scenarioMeta;
    }

    private RunningScenario getRunningScenario(Scenario scenario, int scenarioRowIndex)
    {
        RunningScenario runningScenario = new RunningScenario();
        runningScenario.setScenario(scenario);
        runningScenario.setIndex(scenarioRowIndex);
        scenarioUid = String.format(SCENARIO_UID_PATTERN, scenario.getId(), scenarioRowIndex);
        return runningScenario;
    }

    private Properties getStoryMeta()
    {
        Properties storyMeta = new Properties();
        storyMeta.setProperty("storyMetaKey", "storyMetaValue");
        return storyMeta;
    }

    private Properties putDefaultTestCaseMetaProperties(final Properties meta)
    {
        meta.setProperty(TEST_CASE_ID, EXPECTED_SCENARIO_TEST_CASE_ID);
        meta.setProperty(REQUIREMENT_ID, EXPECTED_SCENARIO_REQUIREMENT_ID);
        return meta;
    }

    private void mockScenarioUid(boolean nullOnFirstAccess)
    {
        if (nullOnFirstAccess)
        {
            lenient().when(testContext.get(CURRENT_STEP_KEY)).thenReturn(null).thenReturn(null)
                    .thenReturn(linkedQueueItem);
        }
        else
        {
            lenient().when(testContext.get(CURRENT_STEP_KEY)).thenReturn(linkedQueueItem);
        }
        lenient().when(allureRunContext.getStoryExecutionStage())
                 .thenReturn(StoryExecutionStage.BEFORE_SCENARIO);
    }

    private void mockStepUid()
    {
        linkedQueueItem = linkedQueueItem.attachItem(STEP_UID);
        mockScenarioUid(false);
    }

    private Entry<String, String> testExampleHandling(int scenarioRowIndex)
    {
        Properties scenarioMeta = getScenarioMeta(true);
        Scenario scenario = new Scenario(SCENARIO, new Meta(scenarioMeta));
        RunningScenario runningScenario = getRunningScenario(scenario, scenarioRowIndex);
        scenarioUid = String.format(SCENARIO_UID_PATTERN, scenario.getId(), scenarioRowIndex);
        Properties storyMeta = getStoryMeta();
        Story story = new Story(null, null, new Meta(storyMeta), null, List.of(scenario));
        RunningStory runningStory = getRunningStory(story, runningScenario);

        String key = "key";
        String value = "value";
        Map<String, String> tableRow = new HashMap<>();
        tableRow.put(key, value);
        tableRow.putAll(Maps.fromProperties(scenarioMeta));
        tableRow.putAll(Maps.fromProperties(storyMeta));

        boolean givenStory = false;
        runTestContext.putRunningStory(runningStory, givenStory);
        mockStoryStart(givenStory);
        allureStoryReporter.beforeStory(story, givenStory);
        allureStoryReporter.example(tableRow, scenarioRowIndex);
        verify(next).example(tableRow, scenarioRowIndex);
        verify(allureLifecycle).scheduleTestCase(any(TestResult.class));
        verify(allureLifecycle).startTestCase(scenarioUid);
        return Map.entry(key, value);
    }

    private void assertParameters(Entry<String, String> tableRow, List<Parameter> parameters)
    {
        assertEquals(1, parameters.size());
        Parameter actual = parameters.get(0);
        assertAll(
            () -> assertEquals(tableRow.getKey(), actual.getName()),
            () -> assertEquals(tableRow.getValue(), actual.getValue())
        );
    }

    private static RunningScenario putEmptyRunningScenario(RunTestContext runTestContext)
    {
        Scenario scenario = new Scenario();
        RunningScenario runningScenario = new RunningScenario();
        runningScenario.setScenario(scenario);
        Story story = new Story(List.of(scenario));
        RunningStory runningStory = new RunningStory();
        runningStory.setStory(story);
        runningStory.setRunningScenario(runningScenario);
        runTestContext.putRunningStory(runningStory, false);
        return runningScenario;
    }

    private void mockRunningStory(boolean notExcluded)
    {
        RunningStory runningStory = new RunningStory();
        runningStory.setNotExcluded(notExcluded);
        runTestContext.putRunningStory(runningStory, false);
    }

    private void mockEnableReporting()
    {
        when(reportControlContext.isReportingEnabled()).thenReturn(true);
        when(runTestContext.isRunInProgress()).thenReturn(true);
    }

    private static Consumer<StepResult> anyStepResultConsumer()
    {
        return argThat(consumer ->
        {
            consumer.accept(new StepResult().setStatus(Status.PASSED));
            return true;
        });
    }

    private static Consumer<TestResult> anyTestResultConsumer()
    {
        return argThat(consumer ->
        {
            consumer.accept(
                    new TestResult().setStatusDetails(new StatusDetails().setMessage("The step is not implemented")));
            return true;
        });
    }
}
