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

package org.vividus.bdd.report.allure;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
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
import java.util.stream.Stream;

import com.google.common.collect.Maps;

import org.jbehave.core.failures.BeforeOrAfterFailed;
import org.jbehave.core.failures.UUIDExceptionWrapper;
import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.model.GivenStories;
import org.jbehave.core.model.Meta;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Story;
import org.jbehave.core.model.StoryDuration;
import org.jbehave.core.reporters.StoryReporter;
import org.jbehave.core.steps.StepCollector.Stage;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.FieldSetter;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.bdd.batch.BatchExecutionConfiguration;
import org.vividus.bdd.batch.BatchStorage;
import org.vividus.bdd.context.BddRunContext;
import org.vividus.bdd.context.IBddRunContext;
import org.vividus.bdd.model.RunningScenario;
import org.vividus.bdd.model.RunningStory;
import org.vividus.bdd.report.allure.AllureStoryReporter.LinkedQueueItem;
import org.vividus.bdd.report.allure.adapter.IVerificationErrorAdapter;
import org.vividus.bdd.report.allure.model.ScenarioExecutionStage;
import org.vividus.bdd.report.allure.model.StatusPriority;
import org.vividus.bdd.report.allure.model.StoryExecutionStage;
import org.vividus.softassert.exception.VerificationError;
import org.vividus.softassert.model.KnownIssue;
import org.vividus.testcontext.SimpleTestContext;
import org.vividus.testcontext.TestContext;

import io.qameta.allure.AllureLifecycle;
import io.qameta.allure.entity.LabelName;
import io.qameta.allure.model.Label;
import io.qameta.allure.model.Link;
import io.qameta.allure.model.Parameter;
import io.qameta.allure.model.Status;
import io.qameta.allure.model.StepResult;
import io.qameta.allure.model.TestResult;

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

    private static final String ISSUE_LINK_PREFIX = "https://issue/";
    private static final String TMS_LINK_PREFIX = "https://tms/";

    private static final String LIFECYCLE_BEFORE_STORY = "Lifecycle: Before story";
    private static final String LIFECYCLE_AFTER_STORY = "Lifecycle: After story";

    private static final String TEST_CASE_ID = "testCaseId";
    private static final String REQUIREMENT_ID = "requirementId";

    @Captor
    private ArgumentCaptor<TestResult> testResultCaptor;

    @Mock
    private IAllureReportGenerator allureReportGenerator;

    @Mock
    private IBddRunContext bddRunContext;

    @Mock
    private BatchStorage batchStorage;

    @Mock
    private TestContext testContext;

    @Mock
    private IAllureRunContext allureRunContext;

    @Mock
    private IVerificationErrorAdapter verificationErrorAdapter;

    @Mock
    private StoryReporter next;

    @Mock
    private AllureLifecycle allureLifecycle;

    private LinkedQueueItem<String> linkedQueueItem;
    private String scenarioUid;

    @InjectMocks
    private AllureStoryReporter allureStoryReporter;

    static Stream<Arguments> updateStepStatus()
    {
        return Stream.of(Arguments.of(null, Status.PASSED, 0),
                Arguments.of(Status.PASSED, null, 1),
                Arguments.of(null, Status.FAILED, 1),
                Arguments.of(Status.PASSED, Status.FAILED, 1),
                Arguments.of(Status.FAILED, Status.PASSED, 0));
    }

    @BeforeAll
    static void beforeAll()
    {
        String placeholder = "{}";
        System.setProperty(ALLURE_LINK_ISSUE_PROPERTY, ISSUE_LINK_PREFIX + placeholder);
        System.setProperty(ALLURE_LINK_TMS_PROPERTY, TMS_LINK_PREFIX + placeholder);
    }

    @AfterAll
    static void afterAll()
    {
        System.clearProperty(ALLURE_LINK_ISSUE_PROPERTY);
        System.clearProperty(ALLURE_LINK_TMS_PROPERTY);
    }

    @BeforeEach
    void beforeEach() throws NoSuchFieldException
    {
        FieldSetter.setField(allureStoryReporter, allureStoryReporter.getClass().
                getDeclaredField("lifecycle"), allureLifecycle);
        linkedQueueItem = new LinkedQueueItem<>(SCENARIO_UID);
        allureStoryReporter.setNext(next);
    }

    @Test
    void testNonFirstExampleEventHandling()
    {
        when(testContext.get(CURRENT_STEP_KEY))
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
        when(testContext.get(CURRENT_STEP_KEY))
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
    @ValueSource(booleans = {true, false})
    void testBeforeStoryDefault(boolean dryRun)
    {
        when(bddRunContext.isDryRun()).thenReturn(dryRun);
        List<Label> storyLabels = mockStoryStart(false);
        testBeforeStory(BASE_STORY, false);
        verifyNoInteractions(allureReportGenerator);
        assertEquals(6, storyLabels.size());
        assertLabel(LabelName.PARENT_SUITE, BATCH_NAME, storyLabels.get(0));
        assertLabel(LabelName.SUITE, BASE_STORY, storyLabels.get(1));
        assertLabel(LabelName.STORY, BASE_STORY, storyLabels.get(2));
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
        assertLabel(LabelName.PARENT_SUITE, BATCH_NAME, storyLabels.get(0));
        assertLabel(LabelName.SUITE, BASE_STORY, storyLabels.get(1));
        assertLabel(LabelName.SUB_SUITE, "Given Story: " + GIVEN_STORY, storyLabels.get(2));
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
        when(bddRunContext.isDryRun()).thenReturn(true);
        Story story = mockRunningStoryWithSeverity(true).getStory();
        story.namedAs("");
        allureStoryReporter.beforeStory(story, true);
        verify(next).beforeStory(story, true);
        verifyNoInteractions(allureReportGenerator);
    }

    @Test
    void testBeforeNotAllowedRunningStory()
    {
        boolean givenStory = true;
        Story story = new Story();
        mockRunningStory(false);
        allureStoryReporter.beforeStory(story, givenStory);
        verify(next).beforeStory(story, givenStory);
        verifyNoInteractions(allureReportGenerator, allureRunContext, allureLifecycle);
    }

    @Test
    void testBeforeStories()
    {
        testBeforeStory("BeforeStories", false);
        verify(allureReportGenerator).start();
    }

    @Test
    void testAfterStories()
    {
        testBeforeStory("AfterStories", false);
        verify(allureReportGenerator).end();
    }

    @Test
    void testBeforeStorySameNames()
    {
        List<Label> story1Labels = new ArrayList<>();
        List<Label> story2Labels = new ArrayList<>();
        startStoriesWithSameName(story1Labels, story2Labels);
        assertLabel(LabelName.SUITE, STORY_NAME, story1Labels.get(1));
        assertLabel(LabelName.SUITE, STORY_NAME, story2Labels.get(1));
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
        when(bddRunContext.getRunningBatchKey()).thenReturn(batchKey);
    }

    private String mockBatchExecutionConfiguration()
    {
        String batchKey = "batch-1";
        BatchExecutionConfiguration config = new BatchExecutionConfiguration();
        config.setName(BATCH_NAME);
        when(batchStorage.getBatchExecutionConfiguration(batchKey)).thenReturn(config);
        return batchKey;
    }

    @Test
    void testBeforeScenario()
    {
        RunningStory runningStory = mockRunningStoryWithSeverity(true);
        Story story = runningStory.getStory();
        boolean givenStory = false;
        mockStoryStart(givenStory);
        mockScenarioUid(true);
        when(bddRunContext.getStoriesChain()).thenReturn(new LinkedList<>(List.of(runningStory)));
        allureStoryReporter.beforeStory(story, givenStory);
        Scenario scenario = story.getScenarios().get(0);
        allureStoryReporter.beforeScenario(scenario);
        verify(allureRunContext).setScenarioExecutionStage(ScenarioExecutionStage.BEFORE_STEPS);
        verify(next).beforeScenario(scenario);
        verify(allureLifecycle).scheduleTestCase(testResultCaptor.capture());
        verify(allureLifecycle).startTestCase(scenarioUid);
        assertTestResultLabel(LabelName.SEVERITY, "critical");
    }

    @Test
    void testBeforeScenarioInGivenStoryScenarioLevel()
    {
        Story story = mockRunningStoryWithSeverity(true).getStory();
        mockScenarioUid(false);
        Scenario scenario = story.getScenarios().get(0);
        allureStoryReporter.beforeScenario(scenario);
        verify(allureRunContext).setScenarioExecutionStage(ScenarioExecutionStage.BEFORE_STEPS);
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
        when(bddRunContext.getStoriesChain()).thenReturn(new LinkedList<>(List.of(runningStory)));
        allureStoryReporter.beforeStory(story, givenStory);
        Scenario scenario = story.getScenarios().get(0);
        allureStoryReporter.beforeScenario(scenario);
        verify(allureRunContext).setScenarioExecutionStage(ScenarioExecutionStage.BEFORE_STEPS);
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
        List<RunningStory> runningStories = startStoriesWithSameName(story1Labels, story2Labels);
        when(allureRunContext.getCurrentStoryLabels()).thenReturn(story1Labels).thenReturn(story2Labels);
        when(bddRunContext.getStoriesChain()).thenReturn(new LinkedList<>(List.of(runningStories.get(0))))
                .thenReturn(new LinkedList<>(List.of(runningStories.get(1))));
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
        lenient().when(bddRunContext.getRunningStory()).thenReturn(runningGivenStory);
        Story givenStory = runningGivenStory.getStory();
        List<Label> givenStoryLabels = List.of(
                new Label().setName(LabelName.PARENT_SUITE.value()).setValue(STORY_NAME));
        when(allureRunContext.getCurrentStoryLabels()).thenReturn(givenStoryLabels);
        LinkedList<RunningStory> storiesChain = new LinkedList<>(
                List.of(runningGivenStory, mockRunningStoryWithSeverity(true)));
        when(bddRunContext.getStoriesChain()).thenReturn(storiesChain);
        mockScenarioUid(true);
        allureStoryReporter.beforeScenario(givenStory.getScenarios().get(0));
        verify(allureLifecycle).scheduleTestCase(testResultCaptor.capture());
        assertTestResultLabel(LabelName.PARENT_SUITE, STORY_NAME);
        TestResult captured = testResultCaptor.getValue();
        assertEquals("[batch: my-batch][stories-chain: name > given story][scenario: Scenario]",
                captured.getHistoryId());
    }

    @Test
    void afterBeforeStoryStepsShouldStopLifecycleBeforeStorySteps()
    {
        setupContext();

        when(allureRunContext.getStoryExecutionStage()).thenReturn(StoryExecutionStage.LIFECYCLE_BEFORE_STORY_STEPS);
        when(allureRunContext.getScenarioExecutionStage()).thenReturn(ScenarioExecutionStage.IN_PROGRESS);
        String currentStepId = "lifecycle-before-story-step";
        String currentScenarioId = LIFECYCLE_BEFORE_STORY;
        testContext.put(CURRENT_STEP_KEY, new LinkedQueueItem<>(currentScenarioId).attachItem(currentStepId));

        allureStoryReporter.afterStorySteps(Stage.BEFORE);

        InOrder ordered = inOrder(allureLifecycle, allureRunContext, next);
        ordered.verify(next).afterStorySteps(Stage.BEFORE);
        ordered.verify(allureLifecycle).updateStep(eq(currentStepId), anyStepResultConsumer());
        ordered.verify(allureLifecycle).stopStep(currentStepId);
        ordered.verify(allureLifecycle).updateTestCase(eq(currentScenarioId), anyTestResultConsumer());
        ordered.verify(allureRunContext).setScenarioExecutionStage(ScenarioExecutionStage.AFTER_STEPS);
        ordered.verify(allureLifecycle).stopTestCase(currentScenarioId);
        ordered.verify(allureLifecycle).writeTestCase(currentScenarioId);
        ordered.verify(allureRunContext).resetScenarioExecutionStage();
        verifyNoMoreInteractions(next, allureLifecycle);
        assertNull(testContext.get(CURRENT_STEP_KEY, LinkedQueueItem.class));
    }

    @Test
    void afterEmptyBeforeStorySteps()
    {
        when(allureRunContext.getStoryExecutionStage()).thenReturn(null);
        allureStoryReporter.afterStorySteps(Stage.BEFORE);
        InOrder ordered = inOrder(allureLifecycle, allureRunContext, next);
        ordered.verify(next).afterStorySteps(Stage.BEFORE);
        ordered.verify(allureRunContext).getStoryExecutionStage();
        ordered.verifyNoMoreInteractions();
    }

    @Test
    void afterAfterStorySteps()
    {
        allureStoryReporter.afterStorySteps(Stage.AFTER);
        InOrder ordered = inOrder(allureLifecycle, allureRunContext, next);
        ordered.verify(next).afterStorySteps(Stage.AFTER);
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
    private List<RunningStory> startStoriesWithSameName(List<Label> story1Labels, List<Label> story2Labels)
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
        when(bddRunContext.getRunningStory()).thenReturn(runningStory1, runningStory2);
        when(allureRunContext.createNewStoryLabels(false)).thenReturn(story1Labels, story2Labels);
        mockRunningBatchName();
        allureStoryReporter.beforeStory(story1, false);
        allureStoryReporter.beforeStory(story2, false);
        return List.of(runningStory1, runningStory2);
    }

    @Test
    void testBeforeStep()
    {
        mockScenarioUid(false);
        allureStoryReporter.beforeStep(GIVEN_STEP);
        verify(next).beforeStep(GIVEN_STEP);
        verify(allureLifecycle).startStep(eq(SCENARIO_UID), eq(STEP_UID), any(StepResult.class));
    }

    @Test
    void beforeStepShouldStartLifecycleBeforeStorySteps()
    {
        beforeStepShouldStartLifecycleStorySteps(null, "before-story-step", LIFECYCLE_BEFORE_STORY);
    }

    @Test
    void beforeStepShouldStartLifecycleAfterStorySteps()
    {
        beforeStepShouldStartLifecycleStorySteps(StoryExecutionStage.AFTER_SCENARIO, "after-story-step",
                LIFECYCLE_AFTER_STORY);
    }

    private void beforeStepShouldStartLifecycleStorySteps(StoryExecutionStage storyExecutionStage, String step,
            String testCaseName)
    {
        BddRunContext bddRunContext = setupContext();
        mockBatchExecutionConfiguration();

        when(allureRunContext.getStoryExecutionStage()).thenReturn(storyExecutionStage);
        testContext.put(CURRENT_STEP_KEY, null);

        Story story = new Story(STORY_NAME, null, new Meta(getStoryMeta()), null, List.of());
        RunningStory runningStory = getRunningStory(story, null);
        bddRunContext.putRunningStory(runningStory, false);

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
        assertEquals(step, capturedStepResult.getName());
        assertEquals(StatusPriority.getLowest().getStatusModel(), capturedStepResult.getStatus());
        ordered.verify(next).beforeStep(step);
        verifyNoMoreInteractions(next, allureLifecycle);
    }

    @Test
    void testSuccessful()
    {
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
    void testAfterNotAllowedRunningStory()
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
        BddRunContext bddRunContext = setupContext();

        when(allureRunContext.getStoryExecutionStage()).thenReturn(StoryExecutionStage.LIFECYCLE_AFTER_STORY_STEPS);
        when(allureRunContext.getScenarioExecutionStage()).thenReturn(ScenarioExecutionStage.IN_PROGRESS);
        String currentStepId = "lifecycle-after-story-step";
        String currentScenarioId = LIFECYCLE_AFTER_STORY;
        testContext.put(CURRENT_STEP_KEY, new LinkedQueueItem<>(currentScenarioId).attachItem(currentStepId));

        boolean givenStory = false;
        RunningStory runningStory = new RunningStory();
        runningStory.setAllowed(true);
        bddRunContext.putRunningStory(runningStory, givenStory);
        allureStoryReporter.afterStory(givenStory);
        InOrder ordered = inOrder(next, allureRunContext, allureLifecycle);
        ordered.verify(next).afterStory(givenStory);
        ordered.verify(allureLifecycle).updateStep(eq(currentStepId), anyStepResultConsumer());
        ordered.verify(allureLifecycle).stopStep(currentStepId);
        ordered.verify(allureLifecycle).updateTestCase(eq(currentScenarioId), anyTestResultConsumer());
        ordered.verify(allureRunContext).setScenarioExecutionStage(ScenarioExecutionStage.AFTER_STEPS);
        ordered.verify(allureLifecycle).stopTestCase(currentScenarioId);
        ordered.verify(allureLifecycle).writeTestCase(currentScenarioId);
        ordered.verify(allureRunContext).resetScenarioExecutionStage();
        ordered.verify(allureRunContext).resetCurrentStoryLabels(givenStory);
        verifyNoMoreInteractions(next, allureLifecycle);
        assertNull(testContext.get(CURRENT_STEP_KEY));
    }

    @Test
    void testAfterScenario()
    {
        mockScenarioUid(false);
        allureStoryReporter.afterScenario();
        verify(next).afterScenario();
        verify(allureLifecycle).stopTestCase(SCENARIO_UID);
        verify(allureLifecycle).writeTestCase(SCENARIO_UID);
        verify(allureRunContext).resetScenarioExecutionStage();
        verify(testContext).put(CURRENT_STEP_KEY, null);
    }

    @Test
    void testAfterScenarioGivenStoryScenarioLevel()
    {
        mockStepUid();
        allureStoryReporter.afterScenario();
        verify(next).afterScenario();
        verify(allureLifecycle).stopStep(STEP_UID);
        verify(allureLifecycle, never()).stopTestCase(SCENARIO_UID);
        verify(allureLifecycle, never()).writeTestCase(SCENARIO_UID);
    }

    @Test
    void testAddLogStep()
    {
        mockStepUid();
        mockAddLogStep(null);
        verify(allureRunContext, never()).setScenarioExecutionStage(ScenarioExecutionStage.IN_PROGRESS);
        verify(allureLifecycle).startStep(eq(STEP_UID), anyString(),
                argThat(s -> LOG_ENTRY.equals(s.getName())));
        verify(allureLifecycle).stopStep(anyString());
    }

    @Test
    void testAddLogBeforeStep()
    {
        mockStepUid();
        mockAddLogStep(ScenarioExecutionStage.BEFORE_STEPS);
        verify(allureRunContext).setScenarioExecutionStage(ScenarioExecutionStage.IN_PROGRESS);
        verify(allureLifecycle).startStep(eq(STEP_UID), eq(SUB_STEP_UID),
                argThat(s -> "@BeforeScenario".equals(s.getName())));
        verify(allureLifecycle).startStep(eq(STEP_UID), anyString(),
                argThat(s -> LOG_ENTRY.equals(s.getName())));
        verify(allureLifecycle).stopStep(anyString());
    }

    @Test
    void testAddLogAfterStepNotRoot()
    {
        mockStepUid();
        mockAddLogStep(ScenarioExecutionStage.AFTER_STEPS);
        verify(allureLifecycle).startStep(eq(STEP_UID), anyString(),
                argThat(s -> LOG_ENTRY.equals(s.getName())));
        verify(allureLifecycle).stopStep(anyString());
        verify(allureRunContext, never()).setScenarioExecutionStage(ScenarioExecutionStage.IN_PROGRESS);
    }

    @Test
    void testAddLogAfterStepRoot()
    {
        mockScenarioUid(false);
        when(allureRunContext.getScenarioExecutionStage()).thenReturn(ScenarioExecutionStage.AFTER_STEPS);
        when(allureRunContext.getStoryExecutionStage()).thenReturn(StoryExecutionStage.BEFORE_SCENARIO);
        allureStoryReporter.addLogStep("DEBUG", LOG_ENTRY);
        verify(allureRunContext).setScenarioExecutionStage(ScenarioExecutionStage.IN_PROGRESS);
        verify(allureLifecycle).startStep(eq(SCENARIO_UID), eq(STEP_UID),
                argThat(s -> "@AfterScenario".equals(s.getName())));
        verify(allureLifecycle).startStep(eq(SCENARIO_UID), anyString(),
                argThat(s -> LOG_ENTRY.equals(s.getName())));
        verify(allureLifecycle).stopStep(anyString());
    }

    @Test
    void testAddLogStepIsNull()
    {
        mockScenarioUid(true);
        mockAddLogStep(ScenarioExecutionStage.IN_PROGRESS);
        verify(testContext, never()).put(ScenarioExecutionStage.class, ScenarioExecutionStage.IN_PROGRESS);
        verifyNoInteractions(allureLifecycle);
    }

    @Test
    void testAddLogStepInProgress()
    {
        mockStepUid();
        mockAddLogStep(ScenarioExecutionStage.IN_PROGRESS);
        verify(testContext, never()).put(ScenarioExecutionStage.class, ScenarioExecutionStage.IN_PROGRESS);
        verify(allureLifecycle).startStep(eq(STEP_UID), anyString(),
                argThat(s -> LOG_ENTRY.equals(s.getName())));
        verify(allureLifecycle).stopStep(anyString());
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
        verify(allureRunContext).resetScenarioExecutionStage();
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
        verify(allureRunContext).resetScenarioExecutionStage();
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
        verify(allureLifecycle).startStep(eq(STEP_UID), eq(SUB_STEP_UID), any(StepResult.class));
        verify(allureLifecycle).updateStep(eq(STEP_UID), anyStepResultConsumer());
        verify(allureLifecycle).stopStep(STEP_UID);
        verify(allureLifecycle).updateTestCase(eq(SCENARIO_UID), anyTestResultConsumer());
    }

    @Test
    void testComment()
    {
        String step = "!-- Comment";
        mockStepUid();
        allureStoryReporter.comment(step);
        verify(testContext, never()).get(ScenarioExecutionStage.class, ScenarioExecutionStage.class);
        verify(testContext, never()).put(eq(ScenarioExecutionStage.class), any(ScenarioExecutionStage.class));
        verify(next).comment(step);
        verify(allureLifecycle).startStep(eq(STEP_UID), eq(SUB_STEP_UID), any(StepResult.class));
        verify(allureLifecycle).updateStep(eq(STEP_UID), anyStepResultConsumer());
        verify(allureLifecycle).stopStep(STEP_UID);
        verify(allureLifecycle, never()).updateTestCase(eq(SCENARIO_UID), anyTestResultConsumer());
    }

    @Test
    void testPending()
    {
        mockStepUid();
        allureStoryReporter.pending(GIVEN_STEP);
        verify(next).pending(GIVEN_STEP);
        verify(allureLifecycle).startStep(eq(STEP_UID), eq(SUB_STEP_UID), any(StepResult.class));
        verify(allureLifecycle).updateStep(eq(STEP_UID), anyStepResultConsumer());
        verify(allureLifecycle).updateTestCase(eq(SCENARIO_UID), anyTestResultConsumer());
    }

    @Test
    void testNotPerformed()
    {
        mockStepUid();
        allureStoryReporter.notPerformed(GIVEN_STEP);
        verify(next).notPerformed(GIVEN_STEP);
        verify(allureLifecycle).startStep(eq(STEP_UID), eq(SUB_STEP_UID), any(StepResult.class));
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
    void testFailedCreateBeforeScenarioStep()
    {
        when(allureRunContext.getScenarioExecutionStage()).thenReturn(ScenarioExecutionStage.BEFORE_STEPS);
        testFailed(new UUIDExceptionWrapper());
        verify(allureLifecycle, times(2)).updateStep(eq(STEP_UID), anyStepResultConsumer());
        verify(allureLifecycle).startStep(eq(STEP_UID), eq(SUB_STEP_UID), any(StepResult.class));
        verify(allureRunContext).setScenarioExecutionStage(ScenarioExecutionStage.IN_PROGRESS);
        verify(allureLifecycle).stopStep(STEP_UID);
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
        mockScenarioUid(true);
        boolean givenStory = false;
        mockStoryStart(givenStory);
        Properties scenarioMeta = putTestCaseMetaProperties(getScenarioMeta(false), EXPECTED_SCENARIO_TEST_CASE_ID,
                EXPECTED_SCENARIO_REQUIREMENT_ID);
        Scenario scenario1 = createScenario(scenarioMeta, List.of());
        Scenario scenario2 = createScenario(scenarioMeta, List.of());
        RunningScenario runningScenario = getRunningScenario(scenario1, 0);
        Story story = new Story(STORY_NAME, null, new Meta(new Properties()), null, List.of(scenario1, scenario2));
        RunningStory runningStory = getRunningStory(story, runningScenario);
        when(bddRunContext.getRunningStory()).thenReturn(runningStory);
        when(bddRunContext.getStoriesChain()).thenReturn(new LinkedList<>(List.of(runningStory)));
        allureStoryReporter.beforeStory(story, givenStory);
        allureStoryReporter.beforeScenario(story.getScenarios().get(0));
        verify(allureLifecycle).scheduleTestCase(testResultCaptor.capture());
        TestResult captured = testResultCaptor.getValue();
        assertEquals("[batch: my-batch][stories-chain: name][scenario: Scenario-0]", captured.getHistoryId());
        assertTestResultLabel(TEST_CASE_ID, EXPECTED_SCENARIO_TEST_CASE_ID);
        assertTestResultLabel(REQUIREMENT_ID, EXPECTED_SCENARIO_REQUIREMENT_ID);
        List<Link> links = captured.getLinks();
        assertFalse(links.isEmpty());
        assertEquals(TMS_LINK_PREFIX + EXPECTED_SCENARIO_TEST_CASE_ID, links.get(0).getUrl());
        assertEquals(StatusPriority.getLowest().getStatusModel(), captured.getStatus());
    }

    @Test
    void testAddTestCaseInfoIdenticalValuesOnStoreAndScenarioLevel()
    {
        mockScenarioUid(true);
        boolean givenStory = false;
        mockStoryStart(givenStory);
        RunningStory runningStory = mockRunningStory(
                putTestCaseMetaProperties(new Properties(), EXPECTED_SCENARIO_TEST_CASE_ID,
                        EXPECTED_SCENARIO_REQUIREMENT_ID),
                putTestCaseMetaProperties(getScenarioMeta(false), EXPECTED_SCENARIO_TEST_CASE_ID,
                        EXPECTED_SCENARIO_REQUIREMENT_ID), List.of());
        Story story = runningStory.getStory();
        when(bddRunContext.getStoriesChain()).thenReturn(new LinkedList<>(List.of(runningStory)));
        allureStoryReporter.beforeStory(story, givenStory);
        allureStoryReporter.beforeScenario(story.getScenarios().get(0));
        verify(allureLifecycle).scheduleTestCase(testResultCaptor.capture());
        List<Label> labels = testResultCaptor.getValue().getLabels();
        assertEquals(1, labels.stream().filter(l -> TEST_CASE_ID.equals(l.getName())).count());
        assertEquals(1, labels.stream().filter(l -> REQUIREMENT_ID.equals(l.getName())).count());
    }

    @ParameterizedTest
    @MethodSource("updateStepStatus")
    void testUpdateStepStatus(Status initialStepStatus, Status statusUpdate, int updatedTimes)
    {
        mockStepUid();
        StepResult stepResult = mock(StepResult.class);
        lenient().doNothing().when(allureLifecycle).updateStep(eq(STEP_UID), argThat(update ->
        {
            update.accept(stepResult);
            return true;
        }));
        lenient().when(stepResult.getStatus()).thenReturn(initialStepStatus);
        allureStoryReporter.updateStepStatus(statusUpdate);
        verify(stepResult, times(updatedTimes)).setStatus(statusUpdate);
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
        when(bddRunContext.getRunningStory()).thenReturn(runningStory);
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
        return new Scenario(SCENARIO, new Meta(scenarioMeta), GivenStories.EMPTY, ExamplesTable.EMPTY, steps);
    }

    private RunningStory getRunningStory(Story story, RunningScenario runningScenario)
    {
        RunningStory runningStory = new RunningStory();
        runningStory.setStory(story);
        runningStory.setAllowed(true);
        runningStory.setRunningScenario(runningScenario);
        return runningStory;
    }

    private Properties getScenarioMeta(boolean useSeverity)
    {
        Properties scenarioMeta = new Properties();
        scenarioMeta.setProperty("scenarioMetaKey", "scenarioMetaValue");
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
        scenarioUid = String.format(SCENARIO_UID_PATTERN, runningScenario.getUuid(), scenarioRowIndex);
        return runningScenario;
    }

    private Properties getStoryMeta()
    {
        Properties storyMeta = new Properties();
        storyMeta.setProperty("storyMetaKey", "storyMetaValue");
        return storyMeta;
    }

    private Properties putTestCaseMetaProperties(final Properties meta, String testCaseId, String requirementId)
    {
        meta.setProperty(TEST_CASE_ID, testCaseId);
        meta.setProperty(REQUIREMENT_ID, requirementId);
        return meta;
    }

    private void mockScenarioUid(boolean nullOnFirstAccess)
    {
        if (nullOnFirstAccess)
        {
            when(testContext.get(CURRENT_STEP_KEY)).thenReturn(null).thenReturn(null).thenReturn(linkedQueueItem);
        }
        else
        {
            when(testContext.get(CURRENT_STEP_KEY)).thenReturn(linkedQueueItem);
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
        scenarioUid = String.format(SCENARIO_UID_PATTERN, runningScenario.getUuid(), scenarioRowIndex);
        Properties storyMeta = getStoryMeta();
        Story story = new Story(null, null, new Meta(storyMeta), null, List.of(scenario));
        RunningStory runningStory = getRunningStory(story, runningScenario);

        String key = "key";
        String value = "value";
        Map<String, String> tableRow = new HashMap<>();
        tableRow.put(key, value);
        tableRow.putAll(Maps.fromProperties(scenarioMeta));
        tableRow.putAll(Maps.fromProperties(storyMeta));

        when(bddRunContext.getRunningStory()).thenReturn(runningStory);
        when(bddRunContext.getStoriesChain()).thenReturn(new LinkedList<>(List.of(runningStory)));
        boolean givenStory = false;
        mockStoryStart(givenStory);
        allureStoryReporter.beforeStory(story, givenStory);
        allureStoryReporter.example(tableRow, scenarioRowIndex);
        verify(allureRunContext).setScenarioExecutionStage(ScenarioExecutionStage.BEFORE_STEPS);
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

    private BddRunContext setupContext()
    {
        BddRunContext bddRunContext = new BddRunContext();
        testContext = new SimpleTestContext();
        bddRunContext.setTestContext(testContext);
        allureStoryReporter.setBddRunContext(bddRunContext);
        allureStoryReporter.setTestContext(testContext);
        return bddRunContext;
    }

    private static RunningScenario putEmptyRunningScenario(BddRunContext bddRunContext)
    {
        Scenario scenario = new Scenario();
        RunningScenario runningScenario = new RunningScenario();
        runningScenario.setScenario(scenario);
        Story story = new Story(List.of(scenario));
        RunningStory runningStory = new RunningStory();
        runningStory.setStory(story);
        runningStory.setRunningScenario(runningScenario);
        bddRunContext.putRunningStory(runningStory, false);
        return runningScenario;
    }

    private void mockRunningStory(boolean allowed)
    {
        RunningStory runningStory = new RunningStory();
        runningStory.setAllowed(allowed);
        when(bddRunContext.getRunningStory()).thenReturn(runningStory);
    }

    private static Consumer<StepResult> anyStepResultConsumer()
    {
        return argThat(consumer ->
        {
            consumer.accept(new StepResult());
            return true;
        });
    }

    private static Consumer<TestResult> anyTestResultConsumer()
    {
        return argThat(consumer ->
        {
            consumer.accept(new TestResult());
            return true;
        });
    }
}
