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

package org.vividus.azure.devops.facade;

import static com.github.valfirst.slf4jtest.LoggingEvent.info;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.azure.devops.client.AzureDevOpsClient;
import org.vividus.azure.devops.client.model.AddOperation;
import org.vividus.azure.devops.configuration.AzureDevOpsExporterOptions;
import org.vividus.azure.devops.configuration.SectionMapping;
import org.vividus.azure.devops.facade.model.ScenarioPart;
import org.vividus.model.jbehave.Scenario;
import org.vividus.model.jbehave.Step;
import org.vividus.output.SyntaxException;

@ExtendWith({ MockitoExtension.class, TestLoggerFactoryExtension.class })
class AzureDevOpsFacadeTests
{
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
    private static final String CREATE_RESPONSE = "{\"id\": 1}";
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
        this.facade = new AzureDevOpsFacade(client, options);
    }

    @Test
    void shouldCreateTestCaseWithAutomatedStepsInStepsSection() throws IOException, SyntaxException
    {
        SectionMapping mapping = new SectionMapping();
        mapping.setSteps(ScenarioPart.AUTOMATED);
        options.setSectionMapping(mapping);
        when(client.createTestCase(operationsCaptor.capture())).thenReturn(CREATE_RESPONSE);
        facade.createTestCase(SUITE_TITLE, createScenario(List.of(createStep(WHEN_STEP))));
        assertOperations(3, ops -> assertAll(
            () -> assertAreaPath(ops.get(0)),
            () -> assertTitle(ops.get(1)),
            () -> assertSteps(ops.get(2), STEPS)
        ));
        verifyCreateTestCaseLog();
    }

    @Test
    void shouldCreateTestCaseWithAutomatedStepsStartingWithCommentInStepsSection() throws IOException, SyntaxException
    {
        SectionMapping mapping = new SectionMapping();
        mapping.setSteps(ScenarioPart.AUTOMATED);
        options.setSectionMapping(mapping);
        when(client.createTestCase(operationsCaptor.capture())).thenReturn(CREATE_RESPONSE);
        facade.createTestCase(SUITE_TITLE, createScenario(List.of(
            createManualStep("Just a comment"),
            createStep(WHEN_STEP)
        )));
        String data = "<steps id=\"0\" last=\"3\"><step id=\"2\" type=\"ActionStep\"><description/><parameterizedString"
                + " isformatted=\"true\">!-- Just a comment</parameterizedString><parameterizedString isformatted=\"tru"
                + "e\"/></step><step id=\"3\" type=\"ActionStep\"><description/><parameterizedString isformatted=\"true"
                + "\">When I perform action</parameterizedString><parameterizedString isformatted=\"true\"/></step></st"
                + "eps>";
        assertOperations(3, ops -> assertAll(
            () -> assertAreaPath(ops.get(0)),
            () -> assertTitle(ops.get(1)),
            () -> assertSteps(ops.get(2), data)
        ));
        verifyCreateTestCaseLog();
    }

    @Test
    void shouldCreateTestCaseWithManualStepsInStepsSection() throws IOException, SyntaxException
    {
        SectionMapping mapping = new SectionMapping();
        mapping.setSteps(ScenarioPart.MANUAL);
        options.setSectionMapping(mapping);
        when(client.createTestCase(operationsCaptor.capture())).thenReturn(CREATE_RESPONSE);
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
        when(client.createTestCase(operationsCaptor.capture())).thenReturn(CREATE_RESPONSE);
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
        when(client.createTestCase(operationsCaptor.capture())).thenReturn(CREATE_RESPONSE);
        facade.createTestCase(SUITE_TITLE, createScenario(List.of(
            createManualStep(MANUAL_STEP_PREFIX + WHEN_STEP),
            createManualStep(MANUAL_RESULT_PREFIX + THEN_STEP),
            createManualStep(MANUAL_STEP_PREFIX + WHEN_STEP),
            createManualStep(MANUAL_STEP_PREFIX + WHEN_STEP),
            createManualStep(MANUAL_RESULT_PREFIX + THEN_STEP),
            createStep(WHEN_STEP)
        )));
        assertOperations(4, ops -> assertAll(
            () -> assertAreaPath(ops.get(0)),
            () -> assertTitle(ops.get(1)),
            () -> assertDescription(ops.get(2), AUTOMATED_STEPS_DESC),
            () -> assertSteps(ops.get(3), STEPS)
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
        when(client.createTestCase(operationsCaptor.capture())).thenReturn(CREATE_RESPONSE);
        facade.createTestCase(SUITE_TITLE, createScenario(List.of(
            createManualStep(MANUAL_STEP_PREFIX + WHEN_STEP),
            createManualStep(MANUAL_RESULT_PREFIX + THEN_STEP),
            createManualStep(MANUAL_STEP_PREFIX + WHEN_STEP),
            createManualStep(MANUAL_STEP_PREFIX + WHEN_STEP),
            createManualStep(MANUAL_RESULT_PREFIX + THEN_STEP),
            createStep(WHEN_STEP)
        )));
        String summary = "<div>When I perform action</div>";
        assertOperations(4, ops -> assertAll(
            () -> assertAreaPath(ops.get(0)),
            () -> assertTitle(ops.get(1)),
            () -> assertSteps(ops.get(2), MANUAL_STEPS_DATA),
            () -> assertDescription(ops.get(3), summary)
        ));
        verifyCreateTestCaseLog();
    }

    @Test
    void shouldUpdateTestCase() throws IOException, SyntaxException
    {
        SectionMapping mapping = new SectionMapping();
        mapping.setSteps(ScenarioPart.AUTOMATED);
        options.setSectionMapping(mapping);
        String testCaseId = "test-case-id";
        facade.updateTestCase(testCaseId, SUITE_TITLE, createScenario(List.of(createStep(WHEN_STEP))));
        verify(client).updateTestCase(eq(testCaseId), operationsCaptor.capture());
        assertOperations(3, ops -> assertAll(
            () -> assertAreaPath(ops.get(0)),
            () -> assertTitle(ops.get(1)),
            () -> assertSteps(ops.get(2), STEPS)
        ));
        assertThat(logger.getLoggingEvents(), is(List.of(
            info("Updating Test Case with ID {}", testCaseId),
            info("Test Case with ID {} has been updated", testCaseId)
        )));
    }

    private void verifyCreateTestCaseLog()
    {
        assertThat(logger.getLoggingEvents(), is(List.of(
            info("Creating Test Case"),
            info("Test Case with ID {} has been created", 1)
        )));
    }

    private void assertOperations(int size, Consumer<List<AddOperation>> operationsVerifier)
    {
        List<AddOperation> operations = operationsCaptor.getValue();
        assertThat(operations, hasSize(size));
        operationsVerifier.accept(operations);
    }

    private void assertAreaPath(AddOperation operation)
    {
        assertOperation(operation, "/fields/System.AreaPath", PROJECT + '\\' + AREA);
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
        scenario.setSteps(steps);
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
}
