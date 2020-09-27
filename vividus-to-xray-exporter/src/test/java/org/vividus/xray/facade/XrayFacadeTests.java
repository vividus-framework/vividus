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

package org.vividus.xray.facade;

import static com.github.valfirst.slf4jtest.LoggingEvent.info;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.jira.JiraClient;
import org.vividus.jira.JiraFacade;
import org.vividus.xray.databind.ManualTestCaseSerializer;
import org.vividus.xray.facade.XrayFacade.NonEditableIssueStatusException;
import org.vividus.xray.model.ManualTestCase;
import org.vividus.xray.model.ManualTestStep;

@ExtendWith({ MockitoExtension.class, TestLoggerFactoryExtension.class })
class XrayFacadeTests
{
    private static final String ISSUE_ID = "issue id";
    private static final String BODY = "{}";
    private static final String PROJECT_KEY = "project key";
    private static final String OPEN_STATUS = "Open";
    private static final String ASSIGNEE = "test-assignee";

    @Captor private ArgumentCaptor<ManualTestCase> manualTestCaseCaptor;

    @Mock private ManualTestCaseSerializer manualTestSerializer;
    @Mock private JiraFacade jiraFacade;
    @Mock private JiraClient jiraClient;
    @Mock private ManualTestStep manualTestStep;
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
    void shouldUpdateTestCase() throws IOException, NonEditableIssueStatusException
    {
        initializeFacade(List.of(OPEN_STATUS));
        mockSerialization();
        TestCaseParameters parameters = createParameters();

        when(jiraFacade.getIssueStatus(ISSUE_ID)).thenReturn(OPEN_STATUS);

        xrayFacade.updateTestCase(ISSUE_ID, parameters);

        verify(jiraFacade).updateIssue(ISSUE_ID, BODY);
        assertThat(logger.getLoggingEvents(), is(List.of(
            info("Updating Test Case with ID {}: {}", ISSUE_ID, BODY),
            info("Test with key {} has been updated", ISSUE_ID))));
        verifyManualTestCase(parameters);
    }

    @Test
    void shouldUpdateTestCaseNotEditableStatus() throws IOException, NonEditableIssueStatusException
    {
        initializeFacade(List.of(OPEN_STATUS));
        TestCaseParameters parameters = createParameters();

        String closedStatus = "Closed";
        when(jiraFacade.getIssueStatus(ISSUE_ID)).thenReturn(closedStatus);

        NonEditableIssueStatusException exception = assertThrows(NonEditableIssueStatusException.class,
            () -> xrayFacade.updateTestCase(ISSUE_ID, parameters));
        assertEquals("Issue " + ISSUE_ID + " is in non-editable '" + closedStatus + "' status", exception.getMessage());
        assertThat(logger.getLoggingEvents(), is(List.of()));
    }

    @Test
    void shouldCreateTestCase() throws IOException
    {
        mockSerialization();
        initializeFacade(List.of());
        TestCaseParameters parameters = createParameters();
        when(jiraFacade.createIssue(BODY)).thenReturn("{\"key\" : \"" + ISSUE_ID + "\"}");

        xrayFacade.createTestCase(parameters);

        assertThat(logger.getLoggingEvents(), is(List.of(
                info("Creating Test Case: {}", BODY),
                info("Test with key {} has been created", ISSUE_ID))));
        verifyManualTestCase(parameters);
    }

    @Test
    void shouldAddTestCasesToTestExecution() throws IOException
    {
        initializeFacade(List.of());
        String testExecutionKey = "TEST-0";
        xrayFacade.updateTestExecution(testExecutionKey, List.of(ISSUE_ID, ISSUE_ID));
        verify(jiraClient).executePost("/rest/raven/1.0/api/testexec/" + testExecutionKey + "/test",
                "{\"add\":[\"issue id\",\"issue id\"]}");
        assertThat(logger.getLoggingEvents(), is(List.of(
                info("Add {} test cases to {} test execution", ISSUE_ID + ", " + ISSUE_ID, testExecutionKey)
        )));
    }

    private void verifyManualTestCase(TestCaseParameters parameters)
    {
        ManualTestCase manualTestCase = manualTestCaseCaptor.getValue();
        assertEquals(PROJECT_KEY, manualTestCase.getProjectKey());
        assertEquals(ASSIGNEE, manualTestCase.getAssignee());
        assertEquals(parameters.getLabels(), manualTestCase.getLabels());
        assertEquals(parameters.getComponents(), manualTestCase.getComponents());
        assertEquals(parameters.getSummary(), manualTestCase.getSummary());
        assertEquals(List.of(manualTestStep), manualTestCase.getManualTestSteps());
    }

    private TestCaseParameters createParameters()
    {
        TestCaseParameters parameters = new TestCaseParameters();
        parameters.setSummary("scenarioTitle");
        parameters.setSteps(List.of(manualTestStep));
        parameters.setLabels(new LinkedHashSet<>(List.of("label")));
        parameters.setComponents(new LinkedHashSet<>(List.of("component")));
        return parameters;
    }

    private void initializeFacade(List<String> editableStatuses)
    {
        xrayFacade = new XrayFacade(PROJECT_KEY, ASSIGNEE, editableStatuses, jiraFacade, jiraClient,
                manualTestSerializer);
    }

    private void mockSerialization() throws IOException
    {
        doAnswer(a ->
        {
            JsonGenerator generator = a.getArgument(1, JsonGenerator.class);
            generator.writeStartObject();
            generator.writeEndObject();
            return null;
        }).when(manualTestSerializer).serialize(manualTestCaseCaptor.capture(), any(JsonGenerator.class),
                any(SerializerProvider.class));
    }
}
