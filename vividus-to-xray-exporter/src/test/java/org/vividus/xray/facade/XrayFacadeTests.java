/*
 * Copyright 2019-2023 the original author or authors.
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

import static com.github.valfirst.slf4jtest.LoggingEvent.error;
import static com.github.valfirst.slf4jtest.LoggingEvent.info;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Supplier;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.apache.commons.io.FilenameUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.jira.JiraClient;
import org.vividus.jira.JiraClientProvider;
import org.vividus.jira.JiraConfigurationException;
import org.vividus.jira.JiraFacade;
import org.vividus.jira.model.Attachment;
import org.vividus.jira.model.IssueLink;
import org.vividus.jira.model.JiraEntity;
import org.vividus.output.ManualTestStep;
import org.vividus.util.zip.ZipUtils;
import org.vividus.xray.databind.AbstractTestCaseSerializer;
import org.vividus.xray.databind.CucumberTestCaseSerializer;
import org.vividus.xray.databind.ManualTestCaseSerializer;
import org.vividus.xray.facade.XrayFacade.NonEditableIssueStatusException;
import org.vividus.xray.model.AbstractTestCase;
import org.vividus.xray.model.CucumberTestCase;
import org.vividus.xray.model.ManualTestCase;
import org.vividus.xray.model.TestCaseType;
import org.vividus.xray.model.TestExecution;
import org.vividus.xray.model.TestExecutionInfo;
import org.vividus.xray.model.TestExecutionItem;
import org.vividus.xray.model.TestExecutionItemStatus;

@ExtendWith({ MockitoExtension.class, TestLoggerFactoryExtension.class })
class XrayFacadeTests
{
    private static final String TEST_EXECUTION_UPDATED_LOG = "Test Execution with key {} has been updated";
    private static final String UPDATE_TEST_EXECUTION_LOG = "Updating Test Execution with ID {}: {}";
    private static final String ISSUE_KEY = "TEST-0";
    private static final String LINK_TYPE = "Tests";
    private static final String ISSUE_ID = "issue id";
    private static final String REQUIREMENT_ID = "requirement id";
    private static final String BODY = "{}";
    private static final String OPEN_STATUS = "Open";
    private static final String MANUAL_TYPE = "Manual";
    private static final String CUCUMBER_TYPE = "Cucumber";
    private static final String CREATE_RESPONSE = "{\"key\" : \"" + ISSUE_ID + "\"}";
    private static final String EXECUTION_IMPORT_ENDPOINT = "/rest/raven/1.0/import/execution";
    private static final String FILES_ATTACH_MESSAGE = "Successfully attached files and folders at {} to test "
            + "execution with key {}";
    private static final String TEST_EXECUTION_REQUEST = "{\"testExecutionKey\":\"TEST-0\",\"tests\":[]}";

    @Mock private ManualTestCaseSerializer manualTestSerializer;
    @Mock private CucumberTestCaseSerializer cucumberTestSerializer;
    @Mock private JiraFacade jiraFacade;
    @Mock private JiraClient jiraClient;
    @Mock private JiraClientProvider jiraClientProvider;
    private XrayFacade xrayFacade;

    private final TestLogger logger = TestLoggerFactory.getTestLogger(XrayFacade.class);

    @AfterEach
    void afterEach()
    {
        verifyNoMoreInteractions(jiraFacade, jiraClient);
    }

    @Test
    void shouldCreateTestsLink() throws IOException, JiraConfigurationException
    {
        initializeFacade(List.of());
        JiraEntity jiraEntity = new JiraEntity();
        jiraEntity.setIssueLinks(List.of(
            new IssueLink(LINK_TYPE, null, "another requirement id"),
            new IssueLink("another link type", null, REQUIREMENT_ID)
        ));
        when(jiraFacade.getIssue(ISSUE_ID)).thenReturn(jiraEntity);

        xrayFacade.createTestsLink(ISSUE_ID, REQUIREMENT_ID);

        verify(jiraFacade).createIssueLink(ISSUE_ID, REQUIREMENT_ID, LINK_TYPE);
        assertThat(logger.getLoggingEvents(),
                is(List.of(info("Create '{}' link from {} to {}", LINK_TYPE, ISSUE_ID, REQUIREMENT_ID))));
    }

    @Test
    void shouldNotCreateNewLinkIfItExists() throws IOException, JiraConfigurationException
    {
        initializeFacade(List.of());
        JiraEntity jiraEntity = new JiraEntity();
        jiraEntity.setIssueLinks(List.of(new IssueLink(LINK_TYPE, null, REQUIREMENT_ID)));
        when(jiraFacade.getIssue(ISSUE_ID)).thenReturn(jiraEntity);

        xrayFacade.createTestsLink(ISSUE_ID, REQUIREMENT_ID);

        assertThat(logger.getLoggingEvents(), is(List.of(
                info("Skipping create of {} {} {} link as it already exists", ISSUE_ID, LINK_TYPE, REQUIREMENT_ID))));
        verifyNoMoreInteractions(jiraFacade);
    }

    @Test
    void shouldUpdateManualTestCase() throws IOException, NonEditableIssueStatusException, JiraConfigurationException
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
    void shouldUpdateCucumberTestCase() throws IOException, NonEditableIssueStatusException, JiraConfigurationException
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
    void shouldUpdateTestCaseNotEditableStatus() throws IOException, JiraConfigurationException
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
    void shouldCreateManualTestCase() throws IOException, JiraConfigurationException
    {
        initializeFacade(List.of());
        ManualTestCase testCase = createManualTestCase();
        mockSerialization(manualTestSerializer, testCase);
        when(jiraFacade.createIssue(BODY, Optional.empty())).thenReturn(CREATE_RESPONSE);

        xrayFacade.createTestCase(testCase);

        verifyCreateLogs(MANUAL_TYPE);
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldUpdateTestExecution(@TempDir Path directory) throws IOException, JiraConfigurationException
    {
        initializeFacade(List.of());
        TestExecution testExecution = new TestExecution();
        testExecution.setTestExecutionKey(ISSUE_KEY);
        testExecution.setTests(List.of(
            createTestExecutionItem("test-1", TestExecutionItemStatus.PASS, null),
            createTestExecutionItem("test-2", TestExecutionItemStatus.FAIL, List.of(TestExecutionItemStatus.PASS,
                    TestExecutionItemStatus.FAIL))
        ));
        when(jiraClientProvider.getByIssueKey(ISSUE_KEY)).thenReturn(jiraClient);

        Path regularFile = createRegularFile(directory);
        xrayFacade.importTestExecution(testExecution, List.of(regularFile, directory));

        String body = "{\"testExecutionKey\":\"TEST-0\",\"tests\":[{\"testKey\":\"test-1\",\"status\":\"PASS\"},"
                + "{\"testKey\":\"test-2\",\"status\":\"FAIL\",\"examples\":[\"PASS\",\"FAIL\"]}]}";
        verify(jiraClient).executePost(EXECUTION_IMPORT_ENDPOINT, body);
        assertThat(logger.getLoggingEvents(), is(List.of(
            info(UPDATE_TEST_EXECUTION_LOG, ISSUE_KEY, body),
            info(FILES_ATTACH_MESSAGE, List.of(regularFile, directory), ISSUE_KEY),
            info(TEST_EXECUTION_UPDATED_LOG, ISSUE_KEY)
        )));

        ArgumentCaptor<List<Attachment>> attachmentsCaptor = ArgumentCaptor.forClass(List.class);
        String regularFileName = FilenameUtils.getName(regularFile.toString());
        verify(jiraFacade).addAttachments(eq(ISSUE_KEY), attachmentsCaptor.capture());
        List<Attachment> attachments = attachmentsCaptor.getValue();
        assertThat(attachments, hasSize(2));
        Attachment regularFileAttachment = attachments.get(0);
        assertEquals(regularFileName, regularFileAttachment.getName());
        assertArrayEquals(Files.readAllBytes(regularFile), regularFileAttachment.getBody());
        Attachment directoryAttachment = attachments.get(1);
        assertEquals(directory.getFileName() + ".zip", directoryAttachment.getName());
        Map<String, byte[]> entries = ZipUtils.readZipEntriesFromBytes(directoryAttachment.getBody());
        assertThat(entries, aMapWithSize(1));
        Entry<String, byte[]> entry = entries.entrySet().iterator().next();
        assertEquals(regularFileName, entry.getKey());
        assertArrayEquals(Files.readAllBytes(regularFile), entry.getValue());
    }

    private Path createRegularFile(Path root) throws IOException
    {
        Path filePath = root.resolve("data.txt");
        Files.createFile(filePath);
        Files.write(filePath, new byte[] { 0, 1, 0, 1 });
        return filePath;
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldCreateTestExecution(@TempDir Path directory) throws JiraConfigurationException, IOException
    {
        initializeFacade(List.of());
        TestExecution testExecution = new TestExecution();
        TestExecutionInfo info = new TestExecutionInfo();
        info.setSummary("summary");
        testExecution.setInfo(info);
        testExecution.setTests(List.of(
            createTestExecutionItem("test-one", TestExecutionItemStatus.PASS, null)
        ));
        when(jiraClientProvider.getByJiraConfigurationKey(Optional.empty())).thenReturn(jiraClient);
        String body = "{\"info\":{\"summary\":\"summary\"},\"tests\":[{\"testKey\":\"test-one\",\"status\":\"PASS\"}]}";
        when(jiraClient.executePost(EXECUTION_IMPORT_ENDPOINT, body)).thenReturn("{\"testExecIssue\":{\"id\":\"01101\""
                + ",\"key\":\"TEST-0\",\"self\":\"https://jira.com/rest/api/2/issue/01101\"}}");

        Path regularFile = createRegularFile(directory);
        xrayFacade.importTestExecution(testExecution, List.of(regularFile));

        verify(jiraClient).executePost(EXECUTION_IMPORT_ENDPOINT, body);
        assertThat(logger.getLoggingEvents(), is(List.of(
            info("Creating Test Execution: {}", body),
            info(FILES_ATTACH_MESSAGE, List.of(regularFile), ISSUE_KEY),
            info("Test Execution with key {} has been created", ISSUE_KEY))));
        ArgumentCaptor<List<Attachment>> attachmentsCaptor = ArgumentCaptor.forClass(List.class);
        verify(jiraFacade).addAttachments(eq(ISSUE_KEY), attachmentsCaptor.capture());
        List<Attachment> attachments = attachmentsCaptor.getValue();
        assertThat(attachments, hasSize(1));
        Attachment regularFileAttachment = attachments.get(0);
        assertEquals(FilenameUtils.getName(regularFile.toString()), regularFileAttachment.getName());
        assertArrayEquals(Files.readAllBytes(regularFile), regularFileAttachment.getBody());
    }

    @Test
    void shouldLogErrorIfAttachmentsUploadIsFailed(@TempDir Path directory)
            throws JiraConfigurationException, IOException
    {
        createRegularFile(directory);
        initializeFacade(List.of());
        TestExecution testExecution = new TestExecution();
        testExecution.setTestExecutionKey(ISSUE_KEY);
        testExecution.setTests(List.of());
        when(jiraClientProvider.getByIssueKey(ISSUE_KEY)).thenReturn(jiraClient);

        IOException thrown = mock(IOException.class);
        doThrow(thrown).when(jiraFacade).addAttachments(eq(ISSUE_KEY), any());

        xrayFacade.importTestExecution(testExecution, List.of(directory));

        verify(jiraClient).executePost(EXECUTION_IMPORT_ENDPOINT, TEST_EXECUTION_REQUEST);
        assertThat(logger.getLoggingEvents(), is(List.of(
            info(UPDATE_TEST_EXECUTION_LOG, ISSUE_KEY, TEST_EXECUTION_REQUEST),
            error(thrown, "Failed to attach files and folders at {} to test execution with key {}", List.of(directory),
                    ISSUE_KEY),
            info(TEST_EXECUTION_UPDATED_LOG, ISSUE_KEY)
        )));
    }

    @Test
    void shouldNotAttachAnythingIfAttachmentsAreEmpty() throws JiraConfigurationException, IOException
    {
        initializeFacade(List.of());
        TestExecution testExecution = new TestExecution();
        testExecution.setTestExecutionKey(ISSUE_KEY);
        testExecution.setTests(List.of());
        when(jiraClientProvider.getByIssueKey(ISSUE_KEY)).thenReturn(jiraClient);

        xrayFacade.importTestExecution(testExecution, List.of());

        verify(jiraClient).executePost(EXECUTION_IMPORT_ENDPOINT, TEST_EXECUTION_REQUEST);
        assertThat(logger.getLoggingEvents(), is(List.of(
            info(UPDATE_TEST_EXECUTION_LOG, ISSUE_KEY, TEST_EXECUTION_REQUEST),
            info(TEST_EXECUTION_UPDATED_LOG, ISSUE_KEY)
        )));
        verify(jiraFacade, times(0)).addAttachments(any(), any());
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
    void shouldAddTestCasesToTestSet() throws IOException, JiraConfigurationException
    {
        initializeFacade(List.of());
        when(jiraClientProvider.getByIssueKey(ISSUE_KEY)).thenReturn(jiraClient);
        xrayFacade.updateTestSet(ISSUE_KEY, List.of(ISSUE_ID, ISSUE_ID));
        verify(jiraClient).executePost("/rest/raven/1.0/api/testset/" + ISSUE_KEY + "/test",
            "{\"add\":[\"issue id\",\"issue id\"]}");
        assertThat(logger.getLoggingEvents(), is(List.of(
            info("Add {} test cases to Test Set with ID {}", ISSUE_ID + ", " + ISSUE_ID, ISSUE_KEY)
        )));
    }

    @Test
    void shouldCreateCucumberTestCase() throws IOException, JiraConfigurationException
    {
        initializeFacade(List.of());
        CucumberTestCase testCase = createCucumberTestCase();
        mockSerialization(cucumberTestSerializer, testCase);
        when(jiraFacade.createIssue(BODY, Optional.empty())).thenReturn(CREATE_RESPONSE);

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
        xrayFacade = new XrayFacade(Optional.empty(), editableStatuses, jiraFacade, jiraClientProvider,
                manualTestSerializer, cucumberTestSerializer);
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
