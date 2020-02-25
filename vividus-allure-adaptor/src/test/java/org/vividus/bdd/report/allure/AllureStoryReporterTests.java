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
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
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
import org.vividus.reporter.event.SubStepsPublishingFinishEvent;
import org.vividus.reporter.event.SubStepsPublishingStartEvent;
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
    private static final String SCENARIO = "Scenario";
    private static final String GIVEN_STEP = "Given step";
    private static final String SCENARIO_UID_PATTERN = "%s[%d]";
    private static final String SCENARIO_UID = "scenarioUid";
    private static final long THREAD_ID = Thread.currentThread().getId();
    private static final String STEP_UID = SCENARIO_UID + DASH + THREAD_ID;
    private static final String SUB_STEP_UID = STEP_UID + DASH + THREAD_ID;
    private static final String CURRENT_STEP_KEY = "allureCurrentLinkedStep";
    private static final String STORY_NAME = "name";
    private static final String EXPECTED_SCENARIO_TEST_CASE_GROUP = "testScenarioTestCaseGroup";
    private static final String EXPECTED_SCENARIO_TEST_CASE_ID = "testScenarioTestCaseId";
    private static final String EXPECTED_SCENARIO_REQUIREMENT_ID = "testScenarioRequirementId";
    private static final String ALLURE_LINK_ISSUE_PROPERTY = "allure.link.issue.pattern";
    private static final String ISSUE_LINK_PREFIX = "https://example.org/";

    private static final String LIFECYCLE_BEFORE_STORY = "Lifecycle: Before story";
    private static final String LIFECYCLE_AFTER_STORY = "Lifecycle: After story";

    private static final String TEST_CASE_ID = "testCaseId";
    private static final String TEST_CASE_GROUP = "testCaseGroup";
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
        System.setProperty(ALLURE_LINK_ISSUE_PROPERTY, ISSUE_LINK_PREFIX + "{}");
    }

    @AfterAll
    static void afterAll()
    {
        System.clearProperty(ALLURE_LINK_ISSUE_PROPERTY);
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

    @Test
    void testBeforeStoryDefault()
    {
        boolean givenStory = false;
        List<Label> storyLabels = mockNewStoryLabels(givenStory);
        String batchKey = mockRunningBatchName();
        testBeforeStory("", givenStory, false);
        verifyNoInteractions(allureReportGenerator);
        assertEquals(7, storyLabels.size());
        assertLabel(LabelName.PARENT_SUITE.value(), batchKey, storyLabels.get(6));
    }

    @Test
    void testBeforeStoryDefaultDryRun()
    {
        when(bddRunContext.isDryRun()).thenReturn(true);
        boolean givenStory = false;
        List<Label> storyLabels = mockNewStoryLabels(givenStory);
        String batchKey = mockRunningBatchName();
        testBeforeStory("", givenStory, false);
        verifyNoInteractions(allureReportGenerator);
        assertEquals(7, storyLabels.size());
        assertLabel(LabelName.PARENT_SUITE.value(), batchKey, storyLabels.get(6));
    }

    @Test
    void testBeforeStoryDefaultGroup()
    {
        boolean givenStory = false;
        List<Label> storyLabels = mockNewStoryLabels(givenStory);
        String batchKey = mockRunningBatchName();
        testBeforeStory("", givenStory, true);
        verifyNoInteractions(allureReportGenerator);
        assertEquals(7, storyLabels.size());
        assertLabel(LabelName.PARENT_SUITE.value(), batchKey, storyLabels.get(6));
    }

    @Test
    void testBeforeStoryDefaultGivenStory()
    {
        testBeforeStory("", true, false);
        verifyNoInteractions(allureReportGenerator);
    }

    @Test
    void testBeforeStoryScenarioGivenStory()
    {
        Story story = mockRunningStory(false, true);
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
        when(bddRunContext.isDryRun()).thenReturn(true);
        Story story = mockRunningStory(false, true);
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
        testBeforeStory("BeforeStories", false, false);
        verify(allureReportGenerator).start();
    }

    @Test
    void testAfterStories()
    {
        testBeforeStory("AfterStories", false, false);
        verify(allureReportGenerator).end();
    }

    @Test
    void testBeforeStorySameNames()
    {
        List<Label> story1Labels = new ArrayList<>();
        List<Label> story2Labels = new ArrayList<>();
        startStoriesWithSameName(story1Labels, story2Labels);
        assertStoryLabels(story1Labels, STORY_NAME);
        assertStoryLabels(story2Labels, STORY_NAME);
    }

    private void assertStoryLabels(List<Label> storyLabels, String suite)
    {
        assertEquals(1,
                storyLabels.stream().filter(
                    label -> LabelName.SUITE.value().equals(label.getName()) && suite.equals(label.getValue()))
                    .count());
    }

    private void testBeforeStory(String storyName, boolean givenStory, boolean storyWithGroup)
    {
        Story story = mockRunningStory(storyWithGroup, true);
        story.namedAs(storyName);
        allureStoryReporter.beforeStory(story, givenStory);
        verify(next).beforeStory(story, givenStory);
    }

    private List<Label> mockNewStoryLabels(boolean givenStory)
    {
        List<Label> storyLabels = new LinkedList<>();
        when(allureRunContext.createNewStoryLabels(givenStory)).thenReturn(storyLabels);
        return storyLabels;
    }

    private String mockRunningBatchName()
    {
        String batchKey = "batch-1";
        when(bddRunContext.getRunningBatchKey()).thenReturn(batchKey);
        String batchName = "my-batch";
        BatchExecutionConfiguration config = new BatchExecutionConfiguration();
        config.setName(batchName);
        when(batchStorage.getBatchExecutionConfiguration(batchKey)).thenReturn(config);
        return batchName;
    }

    private void assertLabel(String expectedName, String expectedValue, Label actual)
    {
        assertEquals(expectedName, actual.getName());
        assertEquals(expectedValue, actual.getValue());
    }

    @Test
    void testBeforeScenario()
    {
        Story story = mockRunningStory(false, true);
        boolean givenStory = false;
        mockNewStoryLabels(givenStory);
        mockRunningBatchName();
        mockScenarioUid(true);
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
        Story story = mockRunningStory(false, true);
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
    void testBeforeScenarioNoTier()
    {
        Story story = mockRunningStory(false, false);
        boolean givenStory = false;
        mockNewStoryLabels(givenStory);
        mockRunningBatchName();
        mockScenarioUid(true);
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
        startStoriesWithSameName(story1Labels, story2Labels);
        when(allureRunContext.getCurrentStoryLabels()).thenReturn(story1Labels);
        when(allureRunContext.getCurrentStoryLabels()).thenReturn(story2Labels);
        mockScenarioUid(true);
        allureStoryReporter.beforeScenario(new Scenario("Scenario 1", Meta.EMPTY));
        verify(allureLifecycle).scheduleTestCase(testResultCaptor.capture());
        assertTestResultLabel(LabelName.SUITE, STORY_NAME);
    }

    @Test
    void testBeforeScenarioStoriesWithSameNamesGivenStory()
    {
        Story story = mockRunningStory(false, true);
        story.namedAs("given.story");
        List<Label> givenStoryLabels = List.of(
                new Label().setName(LabelName.PARENT_SUITE.value()).setValue(STORY_NAME));
        when(allureRunContext.getCurrentStoryLabels()).thenReturn(givenStoryLabels);
        mockScenarioUid(true);
        allureStoryReporter.beforeScenario(story.getScenarios().get(0));
        verify(allureLifecycle).scheduleTestCase(testResultCaptor.capture());
        assertTestResultLabel(LabelName.PARENT_SUITE, STORY_NAME);
    }

    @Test
    void testBeforeScenarioGivenStoriesHasAnchors()
    {
        GivenStories givenStories = mock(GivenStories.class);
        Scenario scenario = new Scenario(SCENARIO, new Meta(getScenarioMeta(true)), givenStories, ExamplesTable.EMPTY,
                List.of());
        RunningScenario runningScenario = getRunningScenario(scenario, 0);
        Story story = new Story(null, null, new Meta(getStoryMeta()), null, List.of(scenario));
        RunningStory runningStory = getRunningStory(story, runningScenario);
        when(bddRunContext.getRunningStory()).thenReturn(runningStory);
        when(allureRunContext.getCurrentStoryLabels()).thenReturn(new ArrayList<>());
        mockScenarioUid(true);
        allureStoryReporter.beforeScenario(scenario);
        verify(allureRunContext).setScenarioExecutionStage(ScenarioExecutionStage.BEFORE_STEPS);
        verify(next).beforeScenario(scenario);
        verify(allureLifecycle).scheduleTestCase(testResultCaptor.capture());
        verify(allureLifecycle).startTestCase(scenarioUid);
    }

    @Test
    void beforeScenarioShouldStopLifecycleBeforeStorySteps()
    {
        BddRunContext bddRunContext = setupContext();

        when(allureRunContext.getStoryExecutionStage()).thenReturn(StoryExecutionStage.LIFECYCLE_BEFORE_STORY_STEPS);
        when(allureRunContext.getScenarioExecutionStage()).thenReturn(ScenarioExecutionStage.IN_PROGRESS);
        String currentStepId = "lifecycle-before-story-step";
        String currentScenarioId = LIFECYCLE_BEFORE_STORY;
        testContext.put(CURRENT_STEP_KEY, new LinkedQueueItem<>(currentScenarioId).attachItem(currentStepId));

        RunningScenario runningScenario = putEmptyRunningScenario(bddRunContext);

        allureStoryReporter.beforeScenario(runningScenario.getScenario());

        InOrder ordered = inOrder(allureLifecycle, allureRunContext, next);
        verifyScenarioStop(ordered, currentScenarioId, currentStepId);
        ordered.verify(allureLifecycle).scheduleTestCase(testResultCaptor.capture());
        ordered.verify(allureLifecycle).startTestCase(runningScenario.getUuid());
        ordered.verify(allureRunContext).setStoryExecutionStage(StoryExecutionStage.BEFORE_SCENARIO);
        ordered.verify(allureRunContext).setScenarioExecutionStage(ScenarioExecutionStage.BEFORE_STEPS);
        ordered.verify(next).beforeScenario(runningScenario.getScenario());
        verifyNoMoreInteractions(next, allureLifecycle);
        assertEquals(runningScenario.getUuid(), testContext.get(CURRENT_STEP_KEY, LinkedQueueItem.class).getValue());
        assertEquals(runningScenario.getUuid(), testResultCaptor.getValue().getUuid());
    }

    private void assertTestResultLabel(LabelName labelName, String labelValue)
    {
        List<Label> labels = testResultCaptor.getValue().getLabels();
        Optional<Label> label = labels.stream().filter(l -> labelName.value().equals(l.getName())).findFirst();
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
        when(allureRunContext.getStoryExecutionStage()).thenReturn(StoryExecutionStage.BEFORE_SCENARIO);
        mockScenarioUid(false);
        allureStoryReporter.beforeStep(GIVEN_STEP);
        verify(next).beforeStep(GIVEN_STEP);
        verify(allureLifecycle).startStep(eq(SCENARIO_UID), eq(STEP_UID), any(StepResult.class));
    }

    @Test
    void beforeStepShouldStartLifecycleBeforeStorySteps()
    {
        BddRunContext bddRunContext = setupContext();

        when(allureRunContext.getStoryExecutionStage()).thenReturn(null);
        testContext.put(CURRENT_STEP_KEY, null);

        RunningStory runningStory = new RunningStory();
        runningStory.setStory(new Story(List.of()));
        bddRunContext.putRunningStory(runningStory, false);

        String step = "before-story-step";
        allureStoryReporter.beforeStep(step);

        InOrder ordered = inOrder(allureLifecycle, next);
        ordered.verify(allureLifecycle).scheduleTestCase(testResultCaptor.capture());
        TestResult testResult = testResultCaptor.getValue();
        assertEquals(LIFECYCLE_BEFORE_STORY, testResult.getName());
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
    void beforeStepShouldStartLifecycleAfterStorySteps()
    {
        BddRunContext bddRunContext = setupContext();

        when(allureRunContext.getStoryExecutionStage()).thenReturn(StoryExecutionStage.AFTER_SCENARIO);
        testContext.put(CURRENT_STEP_KEY, null);

        RunningStory runningStory = new RunningStory();
        runningStory.setStory(new Story(List.of()));
        bddRunContext.putRunningStory(runningStory, false);

        String step = "after-story-step";
        allureStoryReporter.beforeStep(step);

        InOrder ordered = inOrder(allureLifecycle, next);
        ordered.verify(allureLifecycle).scheduleTestCase(testResultCaptor.capture());
        TestResult testResult = testResultCaptor.getValue();
        assertEquals(LIFECYCLE_AFTER_STORY, testResult.getName());
        ordered.verify(allureLifecycle).startTestCase(testResult.getUuid());
        ArgumentCaptor<StepResult> stepResultCaptor = ArgumentCaptor.forClass(StepResult.class);
        ordered.verify(allureLifecycle).startStep(eq(testResult.getUuid()), eq(testResult.getUuid() + DASH + THREAD_ID),
                stepResultCaptor.capture());
        assertEquals(step, stepResultCaptor.getValue().getName());
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
        verifyScenarioStop(ordered, currentScenarioId, currentStepId);
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
        Story story = mockRunningStory(false, true);
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
        Story story = mockRunningStory(false, true);
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
        mockNewStoryLabels(givenStory);
        mockRunningBatchName();
        Story story = mockRunningStory(new Properties(), putTestCaseMetaProperties(getScenarioMeta(false),
                EXPECTED_SCENARIO_TEST_CASE_GROUP, EXPECTED_SCENARIO_TEST_CASE_ID, EXPECTED_SCENARIO_REQUIREMENT_ID),
                List.of());
        allureStoryReporter.beforeStory(story, givenStory);
        allureStoryReporter.beforeScenario(story.getScenarios().get(0));
        verify(allureLifecycle).scheduleTestCase(testResultCaptor.capture());
        TestResult captured = testResultCaptor.getValue();
        List<Label> labels = captured.getLabels();
        Optional<Label> label = labels.stream().filter(l -> TEST_CASE_ID.equals(l.getName())).findFirst();
        assertTrue(label.isPresent());
        assertEquals(EXPECTED_SCENARIO_TEST_CASE_ID, label.get().getValue());
        label = labels.stream().filter(l -> TEST_CASE_GROUP.equals(l.getName())).findFirst();
        assertTrue(label.isPresent());
        assertEquals(EXPECTED_SCENARIO_TEST_CASE_GROUP, label.get().getValue());
        label = labels.stream().filter(l -> REQUIREMENT_ID.equals(l.getName())).findFirst();
        assertTrue(label.isPresent());
        assertEquals(EXPECTED_SCENARIO_REQUIREMENT_ID, label.get().getValue());
        List<Link> links = captured.getLinks();
        assertFalse(links.isEmpty());
        assertEquals(ISSUE_LINK_PREFIX + EXPECTED_SCENARIO_TEST_CASE_ID, links.get(0).getUrl());
        assertEquals(ISSUE_LINK_PREFIX + EXPECTED_SCENARIO_TEST_CASE_ID, links.get(0).getUrl());
        assertEquals(StatusPriority.getLowest().getStatusModel(), captured.getStatus());
    }

    @Test
    void testAddTestCaseInfoIdenticalValuesOnStoreAndScenarioLevel()
    {
        mockScenarioUid(true);
        boolean givenStory = false;
        mockNewStoryLabels(givenStory);
        mockRunningBatchName();
        Story story = mockRunningStory(
                putTestCaseMetaProperties(new Properties(), EXPECTED_SCENARIO_TEST_CASE_GROUP,
                        EXPECTED_SCENARIO_TEST_CASE_ID, EXPECTED_SCENARIO_REQUIREMENT_ID),
                putTestCaseMetaProperties(getScenarioMeta(false), EXPECTED_SCENARIO_TEST_CASE_GROUP,
                        EXPECTED_SCENARIO_TEST_CASE_ID, EXPECTED_SCENARIO_REQUIREMENT_ID),
                List.of());
        allureStoryReporter.beforeStory(story, givenStory);
        allureStoryReporter.beforeScenario(story.getScenarios().get(0));
        verify(allureLifecycle).scheduleTestCase(testResultCaptor.capture());
        List<Label> labels = testResultCaptor.getValue().getLabels();
        assertEquals(1, labels.stream().filter(l -> TEST_CASE_ID.equals(l.getName())).count());
        assertEquals(1, labels.stream().filter(l -> TEST_CASE_GROUP.equals(l.getName())).count());
        assertEquals(1, labels.stream().filter(l -> REQUIREMENT_ID.equals(l.getName())).count());
    }

    @Test
    void testFireSubStepsPublishingStartEvent()
    {
        mockStepUid();
        allureStoryReporter.onSubStepsPublishingStart(new SubStepsPublishingStartEvent());
        verify(allureLifecycle).startStep(eq(STEP_UID), eq(SUB_STEP_UID), any(StepResult.class));
        verify(allureRunContext).setScenarioExecutionStage(ScenarioExecutionStage.IN_PROGRESS);
    }

    @Test
    void testFireSubStepsPublishingFinishEvent()
    {
        mockStepUid();
        allureStoryReporter.onSubStepsPublishingFinish(new SubStepsPublishingFinishEvent());
        verify(allureLifecycle, times(2)).updateStep(eq(STEP_UID), anyStepResultConsumer());
        verify(allureLifecycle).stopStep(STEP_UID);
        verify(allureLifecycle, never()).updateTestCase(eq(SCENARIO_UID),
                anyTestResultConsumer());
    }

    @Test
    void testFireSubStepsPublishingFinishEventWithError()
    {
        mockStepUid();
        SubStepsPublishingFinishEvent event = new SubStepsPublishingFinishEvent();
        VerificationError verificationError = mock(VerificationError.class);
        event.setSubStepThrowable(verificationError);
        allureStoryReporter.onSubStepsPublishingFinish(event);
        verify(allureLifecycle, times(2)).updateStep(eq(STEP_UID), anyStepResultConsumer());
        verify(allureLifecycle).stopStep(STEP_UID);
        verify(allureLifecycle).updateTestCase(eq(SCENARIO_UID), anyTestResultConsumer());
    }

    @ParameterizedTest
    @MethodSource("updateStepStatus")
    void testUpdateStepStatus(Status initialStepStatus, Status statusUpdate, int updatedTimes)
    {
        mockStepUid();
        StepResult stepResult = mock(StepResult.class);
        Mockito.lenient().doNothing().when(allureLifecycle).updateStep(eq(STEP_UID), argThat(update ->
        {
            update.accept(stepResult);
            return true;
        }));
        Mockito.lenient().when(stepResult.getStatus()).thenReturn(initialStepStatus);
        allureStoryReporter.updateStepStatus(statusUpdate);
        verify(stepResult, times(updatedTimes)).setStatus(statusUpdate);
    }

    private void testFailed(Throwable throwable)
    {
        mockStepUid();
        allureStoryReporter.failed(GIVEN_STEP, throwable);
        verify(next).failed(GIVEN_STEP, throwable);
    }

    private Story mockRunningStory(boolean storyWithGroup, boolean tierProperty)
    {
        Properties scenarioMeta = getScenarioMeta(tierProperty);
        Properties storyMeta = getStoryMeta();
        if (storyWithGroup)
        {
            storyMeta.setProperty("group", "storyGroup");
        }
        return mockRunningStory(storyMeta, scenarioMeta, List.of());
    }

    private Story mockRunningStory(Properties storyMeta, Properties scenarioMeta, List<String> steps)
    {
        RunningStory runningStory = createRunningStory(storyMeta, scenarioMeta, steps, null);
        Mockito.lenient().when(bddRunContext.getRunningStory()).thenReturn(runningStory);
        return runningStory.getStory();
    }

    private RunningStory createRunningStory(Properties storyMeta, Properties scenarioMeta, List<String> steps,
            String storyPath)
    {
        Scenario scenario = new Scenario(SCENARIO, new Meta(scenarioMeta), GivenStories.EMPTY, ExamplesTable.EMPTY,
                steps);
        RunningScenario runningScenario = getRunningScenario(scenario, 0);
        Story story = new Story(storyPath, null, new Meta(storyMeta), null, List.of(scenario));
        return getRunningStory(story, runningScenario);
    }

    private RunningStory getRunningStory(Story story, RunningScenario runningScenario)
    {
        RunningStory runningStory = new RunningStory();
        runningStory.setStory(story);
        runningStory.setAllowed(true);
        runningStory.setRunningScenario(runningScenario);
        return runningStory;
    }

    private Properties getScenarioMeta(boolean tierProperty)
    {
        Properties scenarioMeta = new Properties();
        scenarioMeta.setProperty("scenarioMetaKey", "scenarioMetaValue");
        if (tierProperty)
        {
            scenarioMeta.setProperty("testTier", "1");
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

    private Properties putTestCaseMetaProperties(final Properties meta, String testCaseGroup, String testCaseId,
            String requirementId)
    {
        meta.setProperty(TEST_CASE_GROUP, testCaseGroup);
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
        Mockito.lenient().when(allureRunContext.getStoryExecutionStage())
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
        boolean givenStory = false;
        mockNewStoryLabels(givenStory);
        mockRunningBatchName();
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

    private void verifyScenarioStop(InOrder ordered, String currentScenarioId, String currentStepId)
    {
        ordered.verify(allureLifecycle).updateStep(eq(currentStepId), anyStepResultConsumer());
        ordered.verify(allureLifecycle).stopStep(currentStepId);
        ordered.verify(allureLifecycle).updateTestCase(eq(currentScenarioId), anyTestResultConsumer());
        ordered.verify(allureRunContext).setScenarioExecutionStage(ScenarioExecutionStage.AFTER_STEPS);
        ordered.verify(allureLifecycle).stopTestCase(currentScenarioId);
        ordered.verify(allureLifecycle).writeTestCase(currentScenarioId);
        ordered.verify(allureRunContext).resetScenarioExecutionStage();
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
