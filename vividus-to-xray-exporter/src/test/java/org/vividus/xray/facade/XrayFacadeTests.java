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

package org.vividus.xray.facade;

import static com.github.valfirst.slf4jtest.LoggingEvent.info;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.function.Supplier;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.jira.JiraClient;
import org.vividus.jira.JiraFacade;
import org.vividus.output.ManualTestStep;
import org.vividus.xray.databind.AbstractTestCaseSerializer;
import org.vividus.xray.databind.CucumberTestCaseSerializer;
import org.vividus.xray.databind.ManualTestCaseSerializer;
import org.vividus.xray.facade.XrayFacade.NonEditableIssueStatusException;
import org.vividus.xray.model.AbstractTestCase;
import org.vividus.xray.model.CucumberTestCase;
import org.vividus.xray.model.ManualTestCase;
import org.vividus.xray.model.TestCaseType;
import org.vividus.xray.model.TestExecution;
import org.vividus.xray.model.TestExecutionItem;
import org.vividus.xray.model.TestExecutionItemStatus;

@ExtendWith({ MockitoExtension.class, TestLoggerFactoryExtension.class })
class XrayFacadeTests
{
    private static final String ISSUE_KEY = "TEST-0";
    private static final String ISSUE_ID = "issue id";
    private static final String BODY = "{}";
    private static final String OPEN_STATUS = "Open";
    private static final String MANUAL_TYPE = "Manual";
    private static final String CUCUMBER_TYPE = "Cucumber";
    private static final String CREATE_RESPONSE = "{\"key\" : \"" + ISSUE_ID + "\"}";

    @Mock private ManualTestCaseSerializer manualTestSerializer;
    @Mock private CucumberTestCaseSerializer cucumberTestSerializer;
    @Mock private JiraFacade jiraFacade;
    @Mock private JiraClient jiraClient;
    private XrayFacade xrayFacade;

    private final TestLogger logger = TestLoggerFactory.getTestLogger(XrayFacade.class);

    @AfterEach
    void afterEach()
    {
        verifyNoMoreInteractions(jiraFacade, jiraClient);
    }

    @Test
    void shouldCreateTestsLink() throws IOException
    {
        initializeFacade(List.of());
        String requirementId = "requirement id";
        String linkType = "Tests";

        xrayFacade.createTestsLink(ISSUE_ID, requirementId);

        verify(jiraFacade).createIssueLink(ISSUE_ID, requirementId, linkType);
        assertThat(logger.getLoggingEvents(),
                is(List.of(info("Create '{}' link from {} to {}", linkType, ISSUE_ID, requirementId))));
    }

    @Test
    void shouldUpdateManualTestCase() throws IOException, NonEditableIssueStatusException
    {
        initializeFacade(List.of(OPEN_STATUS));
        ManualTestCase testCase = createManualTestCase();
        mockSerialization(manualTestSerializer, testCase);

        when(jiraFacade.getIssueStatus(ISSUE_ID)).thenReturn(OPEN_STATUS);

        xrayFacade.updateTestCase(ISSUE_ID, testCase);

        verify(jiraFacade).updateIssue(ISSUE_ID, BODY);
        verifyUpdateLogs(MANUAL_TYPE);
    }

    @Test
    void shouldUpdateCucumberTestCase() throws IOException, NonEditableIssueStatusException
    {
        initializeFacade(List.of(OPEN_STATUS));
        CucumberTestCase testCase = createCucumberTestCase();
        mockSerialization(cucumberTestSerializer, testCase);

        when(jiraFacade.getIssueStatus(ISSUE_ID)).thenReturn(OPEN_STATUS);

        xrayFacade.updateTestCase(ISSUE_ID, testCase);

        verify(jiraFacade).updateIssue(ISSUE_ID, BODY);
        verifyUpdateLogs(CUCUMBER_TYPE);
    }

    private void verifyUpdateLogs(String type)
    {
        assertThat(logger.getLoggingEvents(), is(List.of(
            info("Updating {} Test Case with ID {}: {}", type, ISSUE_ID, BODY),
            info("{} Test with key {} has been updated", type, ISSUE_ID))));
    }

    @Test
    void shouldUpdateTestCaseNotEditableStatus() throws IOException
    {
        initializeFacade(List.of(OPEN_STATUS));
        String closedStatus = "Closed";
        when(jiraFacade.getIssueStatus(ISSUE_ID)).thenReturn(closedStatus);

        NonEditableIssueStatusException exception = assertThrows(NonEditableIssueStatusException.class,
            () -> xrayFacade.updateTestCase(ISSUE_ID, createManualTestCase()));
        assertEquals("Issue " + ISSUE_ID + " is in non-editable '" + closedStatus + "' status", exception.getMessage());
        assertThat(logger.getLoggingEvents(), is(List.of()));
    }

    @Test
    void shouldCreateManualTestCase() throws IOException
    {
        initializeFacade(List.of());
        ManualTestCase testCase = createManualTestCase();
        mockSerialization(manualTestSerializer, testCase);
        when(jiraFacade.createIssue(BODY)).thenReturn(CREATE_RESPONSE);

        xrayFacade.createTestCase(testCase);

        verifyCreateLogs(MANUAL_TYPE);
    }

    @Test
    void shouldAddTestCasesToTestExecution() throws IOException
    {
        initializeFacade(List.of());
        TestExecution testExecution = new TestExecution();
        testExecution.setTestExecutionKey(ISSUE_KEY);
        testExecution.setTests(List.of(
            createTestExecutionItem("test-1", TestExecutionItemStatus.PASS, null),
            createTestExecutionItem("test-2", TestExecutionItemStatus.FAIL, List.of(TestExecutionItemStatus.PASS,
                    TestExecutionItemStatus.FAIL))
        ));
        xrayFacade.updateTestExecution(testExecution);
        String body = "{\"testExecutionKey\":\"TEST-0\",\"tests\":[{\"testKey\":\"test-1\",\"status\":\"PASS\"},"
                + "{\"testKey\":\"test-2\",\"status\":\"FAIL\",\"examples\":[\"PASS\",\"FAIL\"]}]}";
        verify(jiraClient).executePost("/rest/raven/1.0/import/execution", body);
        assertThat(logger.getLoggingEvents(), is(List.of(
            info("Updating Test Execution with ID {}: {}", ISSUE_KEY, body),
            info("Test Execution with key {} has been updated", ISSUE_KEY))));
    }

    private static TestExecutionItem createTestExecutionItem(String key, TestExecutionItemStatus status,
            List<TestExecutionItemStatus> statusess)
    {
        TestExecutionItem test = new TestExecutionItem();
        test.setTestKey(key);
        test.setStatus(status);
        test.setExamples(statusess);
        return test;
    }

    @Test
    void shouldAddTestCasesToTestSet() throws IOException
    {
        initializeFacade(List.of());
        xrayFacade.updateTestSet(ISSUE_KEY, List.of(ISSUE_ID, ISSUE_ID));
        verify(jiraClient).executePost("/rest/raven/1.0/api/testset/" + ISSUE_KEY + "/test",
            "{\"add\":[\"issue id\",\"issue id\"]}");
        assertThat(logger.getLoggingEvents(), is(List.of(
            info("Add {} test cases to Test Set with ID {}", ISSUE_ID + ", " + ISSUE_ID, ISSUE_KEY)
        )));
    }

    @Test
    void shouldCreateCucumberTestCase() throws IOException
    {
        initializeFacade(List.of());
        CucumberTestCase testCase = createCucumberTestCase();
        mockSerialization(cucumberTestSerializer, testCase);
        when(jiraFacade.createIssue(BODY)).thenReturn(CREATE_RESPONSE);

        xrayFacade.createTestCase(testCase);

        verifyCreateLogs(CUCUMBER_TYPE);
    }

    private void verifyCreateLogs(String type)
    {
        assertThat(logger.getLoggingEvents(), is(List.of(
            info("Creating {} Test Case: {}", type, BODY),
            info("{} Test with key {} has been created", type, ISSUE_ID))));
    }

    private ManualTestCase createManualTestCase()
    {
        ManualTestCase testCase = createTestCase(TestCaseType.MANUAL, ManualTestCase::new);
        ManualTestStep manualTestStep = mock(ManualTestStep.class);
        testCase.setManualTestSteps(List.of(manualTestStep));
        return testCase;
    }

    private CucumberTestCase createCucumberTestCase()
    {
        CucumberTestCase testCase = createTestCase(TestCaseType.CUCUMBER,
                CucumberTestCase::new);
        testCase.setScenarioType("scenario-type");
        testCase.setScenario("scenario");
        return testCase;
    }

    @SuppressWarnings("unchecked")
    private <T extends AbstractTestCase> T createTestCase(TestCaseType type, Supplier<T> factory)
    {
        AbstractTestCase testCase = factory.get();
        testCase.setType(type.getValue());
        testCase.setSummary("scenarioTitle");
        testCase.setLabels(new LinkedHashSet<>(List.of("label")));
        testCase.setComponents(new LinkedHashSet<>(List.of("component")));
        return (T) testCase;
    }

    private void initializeFacade(List<String> editableStatuses)
    {
        xrayFacade = new XrayFacade(editableStatuses, jiraFacade, jiraClient, manualTestSerializer,
                cucumberTestSerializer);
    }

    private <T extends AbstractTestCase> void mockSerialization(AbstractTestCaseSerializer<T> serializer,
            T testCase) throws IOException
    {
        doAnswer(a ->
        {
            JsonGenerator generator = a.getArgument(1, JsonGenerator.class);
            generator.writeStartObject();
            generator.writeEndObject();
            return null;
        }).when(serializer).serialize(eq(testCase), any(JsonGenerator.class), any(SerializerProvider.class));
    }
}
