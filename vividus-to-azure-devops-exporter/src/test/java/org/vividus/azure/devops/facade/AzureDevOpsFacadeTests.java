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

package org.vividus.azure.devops.facade;

import static com.github.valfirst.slf4jtest.LoggingEvent.info;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.azure.devops.client.AzureDevOpsClient;
import org.vividus.azure.devops.client.model.AddOperation;
import org.vividus.azure.devops.client.model.Entity;
import org.vividus.azure.devops.client.model.ShallowReference;
import org.vividus.azure.devops.client.model.TestPoint;
import org.vividus.azure.devops.client.model.TestResult;
import org.vividus.azure.devops.client.model.TestRun;
import org.vividus.azure.devops.client.model.WorkItem;
import org.vividus.azure.devops.configuration.AzureDevOpsExporterOptions;
import org.vividus.azure.devops.configuration.SectionMapping;
import org.vividus.azure.devops.facade.model.ScenarioPart;
import org.vividus.model.jbehave.Scenario;
import org.vividus.model.jbehave.Step;
import org.vividus.output.SyntaxException;
import org.vividus.util.DateUtils;

@ExtendWith({ MockitoExtension.class, TestLoggerFactoryExtension.class })
class AzureDevOpsFacadeTests
{
    private static final ZoneOffset OFFSET = ZoneId.systemDefault().getRules().getOffset(Instant.now());
    private static final OffsetDateTime START = OffsetDateTime.of(1977, 5, 25, 0, 0, 0, 0, OFFSET);
    private static final OffsetDateTime END = OffsetDateTime.of(1993, 4, 16, 0, 0, 0, 0, OFFSET);

    private static final String MANUAL_STEP_PREFIX = "Step: ";
    private static final String MANUAL_RESULT_PREFIX = "Result: ";
    private static final String SUITE_TITLE = "suite-title";
    private static final String TEST_TITLE = "test-title";
    private static final String WHEN_STEP = "When I perform action";
    private static final String THEN_STEP = "Then I perform verification";
    private static final String STEPS = "<steps id=\"0\" last=\"2\"><step id=\"2\" type=\"ActionStep\"><description/>"
            + "<parameterizedString isformatted=\"true\">" + WHEN_STEP + "</parameterizedString><parameterizedString "
            + "isformatted=\"true\"/></step></steps>";
    private static final String PROJECT = "project";
    private static final String AREA = "area";
    private static final String MANUAL_STEPS_DATA = "<steps id=\"0\" last=\"4\"><step id=\"2\" type=\"ActionStep\"><de"
            + "scription/><parameterizedString isformatted=\"true\">When I perform action</parameterizedString><parame"
            + "terizedString isformatted=\"true\">Then I perform verification</parameterizedString></step><step id=\"3"
            + "\" type=\"ActionStep\"><description/><parameterizedString isformatted=\"true\">When I perform action</p"
            + "arameterizedString><parameterizedString isformatted=\"true\"/></step><step id=\"4\" type=\"ActionStep\""
            + "><description/><parameterizedString isformatted=\"true\">When I perform action</parameterizedString><pa"
            + "rameterizedString isformatted=\"true\">Then I perform verification</parameterizedString></step></steps>";
    private static final String AUTOMATED_STEPS_DESC = "<div>Step: When I perform action</div><div>Result: Then I perfo"
            + "rm verification</div><div>Step: When I perform action</div><div>Step: When I perform action</div><div>Re"
            + "sult: Then I perform verification</div>";
    private static final Integer TEST_CASE_ID = 123;
    private static final String RUN_NAME = "run-name";
    private static final Integer TEST_PLAN_ID = 345;

    private AzureDevOpsExporterOptions options;

    @Captor private ArgumentCaptor<List<AddOperation>> operationsCaptor;
    @Mock private AzureDevOpsClient client;
    private AzureDevOpsFacade facade;

    private final TestLogger logger = TestLoggerFactory.getTestLogger(AzureDevOpsFacade.class);

    @BeforeEach
    void init()
    {
        this.options = new AzureDevOpsExporterOptions();
        options.setProject(PROJECT);
        options.setArea(AREA);
        this.facade = new AzureDevOpsFacade(client, options, new DateUtils(ZoneId.systemDefault()));
    }

    @Test
    void shouldCreateTestCaseWithAutomatedStepsInStepsSection() throws IOException, SyntaxException
    {
        SectionMapping mapping = new SectionMapping();
        mapping.setSteps(ScenarioPart.AUTOMATED);
        options.setSectionMapping(mapping);
        when(client.createTestCase(operationsCaptor.capture())).thenReturn(createWorkItem());
        facade.createTestCase(SUITE_TITLE, createScenario(List.of(createStep(WHEN_STEP))));
        assertOperations(5, ops -> assertAll(
            () -> assertAreaPath(ops.get(0)),
            () -> assertTitle(ops.get(1)),
            () -> assertSteps(ops.get(2), STEPS),
            () -> assertAutomatedTestName(ops.get(3)),
            () -> assertAutomatedTestType(ops.get(4))
        ));
        verifyCreateTestCaseLog();
    }

    @Test
    void shouldCreateTestCaseWithAutomatedStepsStartingWithCommentInStepsSection() throws IOException, SyntaxException
    {
        SectionMapping mapping = new SectionMapping();
        mapping.setSteps(ScenarioPart.AUTOMATED);
        options.setSectionMapping(mapping);
        when(client.createTestCase(operationsCaptor.capture())).thenReturn(createWorkItem());
        facade.createTestCase(SUITE_TITLE, createScenario(List.of(
            createManualStep("Just a comment"),
            createStep(WHEN_STEP)
        )));
        String data = "<steps id=\"0\" last=\"3\"><step id=\"2\" type=\"ActionStep\"><description/><parameterizedString"
                + " isformatted=\"true\">!-- Just a comment</parameterizedString><parameterizedString isformatted=\"tru"
                + "e\"/></step><step id=\"3\" type=\"ActionStep\"><description/><parameterizedString isformatted=\"true"
                + "\">When I perform action</parameterizedString><parameterizedString isformatted=\"true\"/></step></st"
                + "eps>";
        assertOperations(5, ops -> assertAll(
            () -> assertAreaPath(ops.get(0)),
            () -> assertTitle(ops.get(1)),
            () -> assertSteps(ops.get(2), data),
            () -> assertAutomatedTestName(ops.get(3)),
            () -> assertAutomatedTestType(ops.get(4))
        ));
        verifyCreateTestCaseLog();
    }

    @Test
    void shouldCreateTestCaseWithManualStepsInStepsSection() throws IOException, SyntaxException
    {
        SectionMapping mapping = new SectionMapping();
        mapping.setSteps(ScenarioPart.MANUAL);
        options.setSectionMapping(mapping);
        when(client.createTestCase(operationsCaptor.capture())).thenReturn(createWorkItem());
        facade.createTestCase(SUITE_TITLE, createScenario(List.of(
            createManualStep(MANUAL_STEP_PREFIX + WHEN_STEP),
            createManualStep(MANUAL_RESULT_PREFIX + THEN_STEP),
            createManualStep(MANUAL_STEP_PREFIX + WHEN_STEP),
            createManualStep(MANUAL_STEP_PREFIX + WHEN_STEP),
            createManualStep(MANUAL_RESULT_PREFIX + THEN_STEP)
        )));
        assertOperations(3, ops -> assertAll(
            () -> assertAreaPath(ops.get(0)),
            () -> assertTitle(ops.get(1)),
            () -> assertSteps(ops.get(2), MANUAL_STEPS_DATA)
        ));
        verifyCreateTestCaseLog();
    }

    @Test
    void shouldCreateTestCaseWithManualStepsInSummarySection() throws IOException, SyntaxException
    {
        SectionMapping mapping = new SectionMapping();
        mapping.setSteps(ScenarioPart.AUTOMATED);
        options.setSectionMapping(mapping);
        when(client.createTestCase(operationsCaptor.capture())).thenReturn(createWorkItem());
        facade.createTestCase(SUITE_TITLE, createScenario(List.of(
            createManualStep(MANUAL_STEP_PREFIX + WHEN_STEP),
            createManualStep(MANUAL_RESULT_PREFIX + THEN_STEP),
            createManualStep(MANUAL_STEP_PREFIX + WHEN_STEP),
            createManualStep(MANUAL_STEP_PREFIX + WHEN_STEP),
            createManualStep(MANUAL_RESULT_PREFIX + THEN_STEP)
        )));
        assertOperations(3, ops -> assertAll(
            () -> assertAreaPath(ops.get(0)),
            () -> assertTitle(ops.get(1)),
            () -> assertDescription(ops.get(2), AUTOMATED_STEPS_DESC)
        ));
        verifyCreateTestCaseLog();
    }

    @Test
    void shouldCreateTestCaseWithManualStepsInSummarySectionAndAutomatedStepsInStepsSection()
            throws IOException, SyntaxException
    {
        SectionMapping mapping = new SectionMapping();
        mapping.setSteps(ScenarioPart.AUTOMATED);
        options.setSectionMapping(mapping);
        when(client.createTestCase(operationsCaptor.capture())).thenReturn(createWorkItem());
        facade.createTestCase(SUITE_TITLE, createScenario(List.of(
            createManualStep(MANUAL_STEP_PREFIX + WHEN_STEP),
            createManualStep(MANUAL_RESULT_PREFIX + THEN_STEP),
            createManualStep(MANUAL_STEP_PREFIX + WHEN_STEP),
            createManualStep(MANUAL_STEP_PREFIX + WHEN_STEP),
            createManualStep(MANUAL_RESULT_PREFIX + THEN_STEP),
            createStep(WHEN_STEP)
        )));
        assertOperations(6, ops -> assertAll(
            () -> assertAreaPath(ops.get(0)),
            () -> assertTitle(ops.get(1)),
            () -> assertDescription(ops.get(2), AUTOMATED_STEPS_DESC),
            () -> assertSteps(ops.get(3), STEPS),
            () -> assertAutomatedTestName(ops.get(4)),
            () -> assertAutomatedTestType(ops.get(5))
        ));
        verifyCreateTestCaseLog();
    }

    @Test
    void shouldCreateTestCaseWithManualStepsInStepsSectionAndAutomatedStepsInSummarySection()
            throws IOException, SyntaxException
    {
        SectionMapping mapping = new SectionMapping();
        mapping.setSteps(ScenarioPart.MANUAL);
        options.setSectionMapping(mapping);
        when(client.createTestCase(operationsCaptor.capture())).thenReturn(createWorkItem());
        facade.createTestCase(SUITE_TITLE, createScenario(List.of(
            createManualStep(MANUAL_STEP_PREFIX + WHEN_STEP),
            createManualStep(MANUAL_RESULT_PREFIX + THEN_STEP),
            createManualStep(MANUAL_STEP_PREFIX + WHEN_STEP),
            createManualStep(MANUAL_STEP_PREFIX + WHEN_STEP),
            createManualStep(MANUAL_RESULT_PREFIX + THEN_STEP),
            createStep(WHEN_STEP)
        )));
        String summary = "<div>When I perform action</div>";
        assertOperations(6, ops -> assertAll(
            () -> assertAreaPath(ops.get(0)),
            () -> assertTitle(ops.get(1)),
            () -> assertSteps(ops.get(2), MANUAL_STEPS_DATA),
            () -> assertDescription(ops.get(3), summary),
            () -> assertAutomatedTestName(ops.get(4)),
            () -> assertAutomatedTestType(ops.get(5))
        ));
        verifyCreateTestCaseLog();
    }

    @CsvSource({
        "area, project\\area",
        ", project"
    })
    @ParameterizedTest
    void shouldUpdateTestCase(String area, String path) throws IOException, SyntaxException
    {
        SectionMapping mapping = new SectionMapping();
        mapping.setSteps(ScenarioPart.AUTOMATED);
        options.setSectionMapping(mapping);
        options.setArea(area);
        facade.updateTestCase(TEST_CASE_ID, SUITE_TITLE, createScenario(List.of(createStep(WHEN_STEP))));
        verify(client).updateTestCase(eq(TEST_CASE_ID), operationsCaptor.capture());
        assertOperations(5, ops -> assertAll(
            () -> assertAreaPath(ops.get(0), path),
            () -> assertTitle(ops.get(1)),
            () -> assertSteps(ops.get(2), STEPS),
            () -> assertAutomatedTestName(ops.get(3)),
            () -> assertAutomatedTestType(ops.get(4))
        ));
        assertThat(logger.getLoggingEvents(), is(List.of(
            info("Updating Test Case with ID {}", TEST_CASE_ID),
            info("Test Case with ID {} has been updated", TEST_CASE_ID)
        )));
    }

    @ParameterizedTest
    @CsvSource({
        "successful, successful, successful, Passed",
        "successful, failed, successful, Failed",
        "successful, successful, failed, Failed",
        "failed, successful, successful, Failed"
    })
    void shouldCreateTestRun(String systemBeforeStepOutcome, String stepOutcome, String systemAfterStepOutcome,
            String resultOutcome) throws IOException
    {
        options.getTestRun().setTestPlanId(TEST_PLAN_ID);
        options.getTestRun().setName(RUN_NAME);

        ArgumentCaptor<TestRun> testRunCaptor = ArgumentCaptor.forClass(TestRun.class);
        Entity testRunEntity = new Entity();
        testRunEntity.setId(923);
        when(client.createTestRun(testRunCaptor.capture())).thenReturn(testRunEntity);

        when(client.queryTestPoints(Set.of(TEST_CASE_ID))).thenReturn(List.of(createTestPoint(911)));

        when(client.getWorkItem(TEST_CASE_ID)).thenReturn(createWorkItem());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<TestResult>> testResultsCaptor = ArgumentCaptor.forClass(List.class);
        doNothing().when(client).addTestResults(eq(923), testResultsCaptor.capture());

        Step successfulStep = createStep(WHEN_STEP);
        successfulStep.setOutcome(stepOutcome);
        Scenario scenario = createScenario(List.of(successfulStep));
        Step systemBeforeStep = createStep(WHEN_STEP);
        systemBeforeStep.setOutcome(systemBeforeStepOutcome);
        scenario.setBeforeSystemScenarioSteps(List.of(systemBeforeStep));
        Step systemAfterStep = createStep(WHEN_STEP);
        systemAfterStep.setOutcome(systemAfterStepOutcome);
        scenario.setAfterSystemScenarioSteps(List.of(systemAfterStep));

        facade.createTestRun(Map.of(TEST_CASE_ID, scenario));

        TestRun testRun = testRunCaptor.getValue();
        assertEquals(RUN_NAME, testRun.getName());
        assertTrue(testRun.isAutomated());
        assertEquals(TEST_PLAN_ID, Integer.valueOf(testRun.getPlan().getId()));

        TestResult testResult = testResultsCaptor.getValue().get(0);
        assertEquals("Completed", testResult.getState());
        assertEquals(TEST_TITLE, testResult.getTestCaseTitle());
        assertEquals(resultOutcome, testResult.getOutcome());
        assertEquals("40", testResult.getTestPoint().getId());
        assertEquals(25, testResult.getRevision());
        assertEquals(START, testResult.getStartedDate());
        assertEquals(END, testResult.getCompletedDate());

        assertThat(logger.getLoggingEvents(), is(List.of(
            info("Creating Test Run"),
            info("Test Run with ID {} has been created", 923)
        )));
    }

    @Test
    void shouldFailIfTestCaseIsMappedToSeveralSuites() throws IOException
    {
        options.getTestRun().setTestPlanId(TEST_PLAN_ID);
        options.getTestRun().setName(RUN_NAME);

        when(client.queryTestPoints(Set.of(TEST_CASE_ID))).thenReturn(List.of(
            createTestPoint(103),
            createTestPoint(101)
        ));

        Map<Integer, Scenario> scenarios = Map.of(TEST_CASE_ID, createScenario(List.of()));
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> facade.createTestRun(scenarios));
        assertEquals("The test case with id 123 is attached to more than one test suite (103,101) in 345 test plan",
                thrown.getMessage());
    }

    @Test
    void shouldFailIfTestPlanIdIsNotSet()
    {
        options.getTestRun().setName(RUN_NAME);
        Map<Integer, Scenario> scenarios = Map.of();
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
            () -> facade.createTestRun(scenarios));
        assertEquals("The 'azure-devops-exporter.test-run.test-plan-id' property is mandatory to create a test run",
                thrown.getMessage());
    }

    @Test
    void shouldFailIfRunNameIsNotSet()
    {
        Map<Integer, Scenario> scenarios = Map.of();
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
            () -> facade.createTestRun(scenarios));
        assertEquals("The 'azure-devops-exporter.test-run.name' property is mandatory to create a test run",
                thrown.getMessage());
    }

    @Test
    void shouldFailIfThereIsNoTestPointForTestCase() throws IOException
    {
        options.getTestRun().setTestPlanId(TEST_PLAN_ID);
        options.getTestRun().setName(RUN_NAME);

        when(client.queryTestPoints(Set.of(TEST_CASE_ID))).thenReturn(List.of());

        Map<Integer, Scenario> scenarios = Map.of(TEST_CASE_ID, createScenario(List.of()));

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
            () -> facade.createTestRun(scenarios));
        assertEquals("Unable to find test point for test case with id " + TEST_CASE_ID + " in " + TEST_PLAN_ID
                + " test plan", thrown.getMessage());
    }

    private void verifyCreateTestCaseLog()
    {
        assertThat(logger.getLoggingEvents(), is(List.of(
            info("Creating Test Case"),
            info("Test Case with ID {} has been created", TEST_CASE_ID)
        )));
    }

    private void assertOperations(int size, Consumer<List<AddOperation>> operationsVerifier)
    {
        List<AddOperation> operations = operationsCaptor.getValue();
        assertThat(operations, hasSize(size));
        operationsVerifier.accept(operations);
    }

    private void assertAutomatedTestName(AddOperation operation)
    {
        assertOperation(operation, "/fields/Microsoft.VSTS.TCM.AutomatedTestName", SUITE_TITLE + "." + TEST_TITLE);
    }

    private void assertAutomatedTestType(AddOperation operation)
    {
        assertOperation(operation, "/fields/Microsoft.VSTS.TCM.AutomatedTestType", "VIVIDUS");
    }

    private void assertAreaPath(AddOperation operation)
    {
        assertAreaPath(operation, PROJECT + '\\' + AREA);
    }

    private void assertAreaPath(AddOperation operation, String value)
    {
        assertOperation(operation, "/fields/System.AreaPath", value);
    }

    private void assertTitle(AddOperation operation)
    {
        assertOperation(operation, "/fields/System.Title", TEST_TITLE);
    }

    private void assertSteps(AddOperation operation, String steps)
    {
        assertOperation(operation, "/fields/Microsoft.VSTS.TCM.Steps", steps);
    }

    private void assertDescription(AddOperation operation, String steps)
    {
        assertOperation(operation, "/fields/System.Description", steps);
    }

    private void assertOperation(AddOperation operation, String path, String value)
    {
        assertAll(
            () -> assertEquals(path, operation.getPath()),
            () -> assertEquals(value, operation.getValue())
        );
    }

    private Scenario createScenario(List<Step> steps)
    {
        Scenario scenario = new Scenario();
        scenario.setTitle(TEST_TITLE);
        scenario.setBeforeUserScenarioSteps(List.of());
        scenario.setSteps(steps);
        scenario.setAfterUserScenarioSteps(List.of());
        scenario.setStart(START.toInstant().toEpochMilli());
        scenario.setEnd(END.toInstant().toEpochMilli());
        return scenario;
    }

    private static Step createStep(String value)
    {
        Step step = new Step();
        step.setValue(value);
        return step;
    }

    private static Step createManualStep(String value)
    {
        Step step = new Step();
        step.setValue("!-- " + value);
        step.setOutcome("comment");
        return step;
    }

    private static WorkItem createWorkItem()
    {
        WorkItem workItem = new WorkItem();
        workItem.setId(TEST_CASE_ID);
        workItem.setRev(25);
        return workItem;
    }

    private static TestPoint createTestPoint(Integer suiteId)
    {
        TestPoint testPoint = new TestPoint();
        testPoint.setId(40);
        testPoint.setTestCase(new ShallowReference(TEST_CASE_ID.toString()));
        testPoint.setTestPlan(new ShallowReference(TEST_PLAN_ID.toString()));
        testPoint.setSuite(new ShallowReference(suiteId.toString()));
        return testPoint;
    }
}
