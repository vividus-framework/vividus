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

package org.vividus.zephyr.facade;

import static com.github.valfirst.slf4jtest.LoggingEvent.info;
import static java.lang.System.lineSeparator;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.jira.JiraClient;
import org.vividus.jira.JiraClientProvider;
import org.vividus.jira.JiraConfigurationException;
import org.vividus.jira.JiraFacade;
import org.vividus.jira.model.Project;
import org.vividus.jira.model.Version;
import org.vividus.model.jbehave.Examples;
import org.vividus.model.jbehave.Parameters;
import org.vividus.model.jbehave.Scenario;
import org.vividus.model.jbehave.Step;
import org.vividus.model.jbehave.Story;
import org.vividus.zephyr.configuration.ZephyrConfiguration;
import org.vividus.zephyr.configuration.ZephyrExporterConfiguration;
import org.vividus.zephyr.configuration.ZephyrExporterProperties;
import org.vividus.zephyr.databind.TestCaseSerializer;
import org.vividus.zephyr.model.CucumberTestStep;
import org.vividus.zephyr.model.TestCaseStatus;
import org.vividus.zephyr.model.ZephyrTestCase;

@ExtendWith({MockitoExtension.class, TestLoggerFactoryExtension.class})
class ZephyrFacadeTests
{
    private static final String ZAPI_ENDPOINT = "/rest/zapi/latest/";
    private static final String GET_CYCLE_ID_ENDPOINT = ZAPI_ENDPOINT + "cycle?projectId=%s&versionId=%s";
    private static final String GET_FOLDER_ID_ENDPOINT = ZAPI_ENDPOINT +  "cycle/%s/folders?projectId=%s&versionId=%s";
    private static final String GET_STATUSES_ENDPOINT = ZAPI_ENDPOINT + "util/testExecutionStatus";
    private static final String GET_EXECUTION_ID_ENDPOINT = ZAPI_ENDPOINT + "execution?issueId=111";
    private static final String TEST_STEP_ENDPOINT = ZAPI_ENDPOINT + "teststep/%s";
    private static final String GET_CYCLE_ID_RESPONSE = "{\"11113\":{\"name\":\"test\"},\"recordsCount\":1}";
    private static final String GET_FOLDER_ID_RESPONSE = "[{\"folderId\":11114,\"folderName\":\"test\"}]";
    private static final String GET_EXECUTION_ID_RESPONSE = "{\"issueId\": 111,\"executions\": [{\"id\": 1001,"
            + "\"cycleName\": \"test\",\"folderName\": \"test\",\"versionName\": \"test\"},{\"id\": 1002,"
            + "\"cycleName\": \"test 2\",\"folderName\": \"test 2\",\"versionName\": \"test 2\"}]}";
    private static final String GET_STATUSES_ID_RESPONSE = "[{\"id\": 1, \"name\": \"test\"}]";
    private static final String PROJECT_ID = "11111";
    private static final String VERSION_ID = "11112";
    private static final String CYCLE_ID = "11113";
    private static final String FOLDER_ID = "11114";
    private static final String ISSUE_ID = "111";
    private static final String TEST = "test";
    private static final String BODY = "{}";
    private static final String CREATE_RESPONSE = "{\"key\" : \"" + ISSUE_ID + "\"}";
    private static final String STEP_VALUE = "When I perform action ";
    private static final String NAME = "name ";
    private static final String VALUE = "value ";
    private static final String TEST_STEP_CREATED = "{} test step has been created";

    @Mock private JiraFacade jiraFacade;
    @Mock private JiraClient client;
    @Mock private JiraClientProvider jiraClientProvider;
    @Mock private ZephyrExporterConfiguration zephyrExporterConfiguration;
    @Mock private TestCaseSerializer testCaseSerializer;
    @Mock private ZephyrExporterProperties zephyrExporterProperties;
    @InjectMocks private ZephyrFacade zephyrFacade;

    private final TestLogger logger = TestLoggerFactory.getTestLogger(ZephyrFacade.class);

    @Test
    void testCreateExecution() throws IOException, JiraConfigurationException
    {
        when(jiraClientProvider.getByJiraConfigurationKey(Optional.empty())).thenReturn(client);
        String execution = "{\"cycleId\": \"11113\",\"issueId\": \"11115\",\"projectId\": \"11111\","
                + "\"versionId\": \"11112\",\"folderId\": 11114}";
        when(client.executePost(ZAPI_ENDPOINT + "execution/", execution)).thenReturn(
                "{\"11116\": {\"id\": 11116,\"executionStatus\": \"-1\"}}");
        assertEquals(11_116, zephyrFacade.createExecution(execution));
    }

    @Test
    void testUpdateExecutionStatus() throws IOException, JiraConfigurationException
    {
        when(jiraClientProvider.getByJiraConfigurationKey(Optional.empty())).thenReturn(client);
        String executionBody = "{\"status\": \"1\"}";
        zephyrFacade.updateExecutionStatus(11_116, executionBody);
        verify(client).executePut(String.format(ZAPI_ENDPOINT + "execution/%s/execute", "11116"), executionBody);
    }

    @Test
    void testFindVersionIdDoesNotExist() throws IOException, JiraConfigurationException
    {
        when(zephyrExporterConfiguration.getProjectKey()).thenReturn(TEST);
        when(zephyrExporterConfiguration.getVersionName()).thenReturn(TEST);
        Version version = new Version();
        version.setId(VERSION_ID);
        version.setName("test1");
        Project project = new Project();
        project.setId("11110");
        project.setVersions(List.of(version));
        when(jiraFacade.getProject(TEST)).thenReturn(project);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                zephyrFacade::prepareConfiguration);
        assertEquals("Version with name 'test' does not exist", exception.getMessage());
    }

    @Test
    void testFindCycleIdDoesNotExist() throws IOException, JiraConfigurationException
    {
        when(jiraClientProvider.getByJiraConfigurationKey(Optional.empty())).thenReturn(client);
        when(zephyrExporterConfiguration.getProjectKey()).thenReturn(TEST);
        when(zephyrExporterConfiguration.getVersionName()).thenReturn(TEST);
        when(zephyrExporterConfiguration.getCycleName()).thenReturn(TEST);
        mockJiraProjectRetrieve();
        when(client.executeGet(String.format(GET_CYCLE_ID_ENDPOINT, PROJECT_ID, VERSION_ID))).
                thenReturn("{\"-1\":{\"name\":\"test1\"},\"recordsCount\":1}");
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                zephyrFacade::prepareConfiguration);
        assertEquals("Cycle with name 'test' does not exist", exception.getMessage());
    }

    @Test
    void testFindFolderIdDoesNotExist() throws IOException, JiraConfigurationException
    {
        when(jiraClientProvider.getByJiraConfigurationKey(Optional.empty())).thenReturn(client);
        when(zephyrExporterConfiguration.getProjectKey()).thenReturn(TEST);
        when(zephyrExporterConfiguration.getVersionName()).thenReturn(TEST);
        when(zephyrExporterConfiguration.getCycleName()).thenReturn(TEST);
        when(zephyrExporterConfiguration.getFolderName()).thenReturn(TEST);
        mockJiraProjectRetrieve();
        when(client.executeGet(String.format(GET_CYCLE_ID_ENDPOINT, PROJECT_ID, VERSION_ID)))
            .thenReturn(GET_CYCLE_ID_RESPONSE);
        when(client.executeGet(String.format(GET_FOLDER_ID_ENDPOINT, CYCLE_ID, PROJECT_ID, VERSION_ID)))
            .thenReturn("[{\"folderId\":0,\"folderName\":\"test1\"}]");
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                zephyrFacade::prepareConfiguration);
        assertEquals("Folder with name 'test' does not exist", exception.getMessage());
    }

    @Test
    void testGetExecutionStatusesDoNotExist() throws IOException, JiraConfigurationException
    {
        when(jiraClientProvider.getByJiraConfigurationKey(Optional.empty())).thenReturn(client);
        setConfiguration();
        mockJiraProjectRetrieve();
        when(client.executeGet(String.format(GET_CYCLE_ID_ENDPOINT, PROJECT_ID, VERSION_ID)))
            .thenReturn(GET_CYCLE_ID_RESPONSE);
        when(client.executeGet(String.format(GET_FOLDER_ID_ENDPOINT, CYCLE_ID, PROJECT_ID, VERSION_ID)))
            .thenReturn(GET_FOLDER_ID_RESPONSE);
        when(client.executeGet(GET_STATUSES_ENDPOINT)).thenReturn("[{\"id\": 1, \"name\": \"PASSED\"}]");
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                zephyrFacade::prepareConfiguration);
        assertEquals("Status 'test' does not exist", exception.getMessage());
    }

    @Test
    void testPrepareConfiguration() throws IOException, JiraConfigurationException
    {
        when(jiraClientProvider.getByJiraConfigurationKey(Optional.empty())).thenReturn(client);
        setConfiguration();
        mockJiraProjectRetrieve();
        when(client.executeGet(String.format(GET_CYCLE_ID_ENDPOINT, PROJECT_ID, VERSION_ID)))
            .thenReturn(GET_CYCLE_ID_RESPONSE);
        when(client.executeGet(String.format(GET_FOLDER_ID_ENDPOINT, CYCLE_ID, PROJECT_ID, VERSION_ID)))
                .thenReturn(GET_FOLDER_ID_RESPONSE);
        when(client.executeGet(GET_STATUSES_ENDPOINT)).thenReturn(GET_STATUSES_ID_RESPONSE);
        ZephyrConfiguration actualConfiguration = zephyrFacade.prepareConfiguration();
        assertEquals(PROJECT_ID, actualConfiguration.getProjectId());
        assertEquals(VERSION_ID, actualConfiguration.getVersionId());
        assertEquals(CYCLE_ID, actualConfiguration.getCycleId());
        assertEquals(FOLDER_ID, actualConfiguration.getFolderId());
        assertEquals(1, actualConfiguration.getTestStatusPerZephyrIdMapping().size());
        assertEquals(1, actualConfiguration.getTestStatusPerZephyrIdMapping().get(TestCaseStatus.PASSED));
    }

    @Test
    void testPrepareConfigurationWithoutFolder() throws IOException, JiraConfigurationException
    {
        when(jiraClientProvider.getByJiraConfigurationKey(Optional.empty())).thenReturn(client);
        when(zephyrExporterConfiguration.getProjectKey()).thenReturn(TEST);
        when(zephyrExporterConfiguration.getVersionName()).thenReturn(TEST);
        when(zephyrExporterConfiguration.getCycleName()).thenReturn(TEST);
        when(zephyrExporterConfiguration.getFolderName()).thenReturn("");
        Map<TestCaseStatus, String> statuses = new EnumMap<>(TestCaseStatus.class);
        statuses.put(TestCaseStatus.PASSED, TEST);
        when(zephyrExporterConfiguration.getStatuses()).thenReturn(statuses);
        mockJiraProjectRetrieve();
        when(client.executeGet(String.format(GET_CYCLE_ID_ENDPOINT, PROJECT_ID, VERSION_ID)))
            .thenReturn(GET_CYCLE_ID_RESPONSE);
        when(client.executeGet(GET_STATUSES_ENDPOINT)).thenReturn(GET_STATUSES_ID_RESPONSE);
        ZephyrConfiguration actualConfiguration = zephyrFacade.prepareConfiguration();
        assertEquals(PROJECT_ID, actualConfiguration.getProjectId());
        assertEquals(VERSION_ID, actualConfiguration.getVersionId());
        assertEquals(CYCLE_ID, actualConfiguration.getCycleId());
        assertNull(actualConfiguration.getFolderId());
        assertEquals(1, actualConfiguration.getTestStatusPerZephyrIdMapping().size());
        assertEquals(1, actualConfiguration.getTestStatusPerZephyrIdMapping().get(TestCaseStatus.PASSED));
    }

    @Test
    void testFindExecutionId() throws IOException, JiraConfigurationException
    {
        when(jiraClientProvider.getByJiraConfigurationKey(Optional.empty())).thenReturn(client);
        when(zephyrExporterConfiguration.getVersionName()).thenReturn(TEST);
        when(zephyrExporterConfiguration.getCycleName()).thenReturn(TEST);
        when(zephyrExporterConfiguration.getFolderName()).thenReturn(TEST);
        when(client.executeGet(GET_EXECUTION_ID_ENDPOINT)).thenReturn(GET_EXECUTION_ID_RESPONSE);
        assertEquals(OptionalInt.of(1001), zephyrFacade.findExecutionId(ISSUE_ID));
    }

    @Test
    void testFindExecutionIdWithoutFolder() throws IOException, JiraConfigurationException
    {
        when(jiraClientProvider.getByJiraConfigurationKey(Optional.empty())).thenReturn(client);
        when(zephyrExporterConfiguration.getVersionName()).thenReturn(TEST);
        when(zephyrExporterConfiguration.getCycleName()).thenReturn(TEST);
        when(zephyrExporterConfiguration.getFolderName()).thenReturn("");
        when(client.executeGet(GET_EXECUTION_ID_ENDPOINT)).thenReturn("{\"issueId\": 111,\"executions\":"
                + "[{\"id\": 1003,\"cycleName\": \"test\",\"versionName\": \"test\"}]}");
        assertEquals(OptionalInt.of(1003), zephyrFacade.findExecutionId(ISSUE_ID));
    }

    @Test
    void testExecutionIdNotFound() throws IOException, JiraConfigurationException
    {
        when(jiraClientProvider.getByJiraConfigurationKey(Optional.empty())).thenReturn(client);
        when(zephyrExporterConfiguration.getVersionName()).thenReturn(TEST);
        when(zephyrExporterConfiguration.getCycleName()).thenReturn(TEST);
        when(zephyrExporterConfiguration.getFolderName()).thenReturn("test2");
        when(client.executeGet(GET_EXECUTION_ID_ENDPOINT)).thenReturn(GET_EXECUTION_ID_RESPONSE);
        assertEquals(OptionalInt.empty(), zephyrFacade.findExecutionId(ISSUE_ID));
    }

    @Test
    void testUpdateTestCase() throws IOException, JiraConfigurationException
    {
        ZephyrTestCase test = createZephyrTestCase();
        mockSerialization(test);
        when(jiraFacade.updateIssue(ISSUE_ID, BODY)).thenReturn(BODY);

        zephyrFacade.updateTestCase(ISSUE_ID, test);

        assertThat(logger.getLoggingEvents(), is(List.of(
                info("Updating Test Case with ID {}: {}", ISSUE_ID, BODY),
                info("Test with key {} has been updated", ISSUE_ID))));
    }

    @Test
    void testCreateNewTestCase() throws IOException, JiraConfigurationException
    {
        ZephyrTestCase test = createZephyrTestCase();
        mockSerialization(test);
        when(jiraFacade.createIssue(BODY, Optional.empty())).thenReturn(CREATE_RESPONSE);
        zephyrFacade.createTestCase(test);

        assertThat(logger.getLoggingEvents(), is(List.of(
                info("Creating Test Case: {}", BODY),
                info("Test with key {} has been created", ISSUE_ID))));
    }

    @Test
    void testCreateTestStepsScenario() throws JiraConfigurationException, IOException
    {
        Scenario scenario = new Scenario();
        List<Step> steps = List.of(
                createStep(STEP_VALUE + 1),
                createStep(STEP_VALUE + 2),
                createStep(STEP_VALUE + 3)
        );
        scenario.setSteps(steps);

        when(jiraClientProvider.getByJiraConfigurationKey(Optional.empty())).thenReturn(client);

        zephyrFacade.createTestSteps(scenario, ISSUE_ID);

        verify(client).executePost(String.format(TEST_STEP_ENDPOINT, ISSUE_ID),
                "{\"step\":\"When I perform action 1\"}");
        verify(client).executePost(String.format(TEST_STEP_ENDPOINT, ISSUE_ID),
                "{\"step\":\"When I perform action 2\"}");
        verify(client).executePost(String.format(TEST_STEP_ENDPOINT, ISSUE_ID),
                "{\"step\":\"When I perform action 3\"}");
        assertThat(logger.getLoggingEvents(), is(List.of(
                info(TEST_STEP_CREATED, 1),
                info(TEST_STEP_CREATED, 2),
                info(TEST_STEP_CREATED, 3))));
        verifyNoMoreInteractions(client);
    }

    @Test
    void testCreateTestStepsWithExamples() throws JiraConfigurationException, IOException
    {
        Scenario scenario = new Scenario();
        Examples examples = new Examples();
        Parameters parameters = new Parameters();
        parameters.setNames(Arrays.asList(NAME + 1, NAME + 2));
        parameters.setValues(Arrays.asList(Arrays.asList(VALUE + 1, VALUE + 2), Arrays.asList(VALUE + 3, VALUE + 4)));
        examples.setParameters(parameters);
        scenario.setExamples(examples);

        when(jiraClientProvider.getByJiraConfigurationKey(Optional.empty())).thenReturn(client);

        zephyrFacade.createTestSteps(scenario, ISSUE_ID);

        String table = "|name 1|name 2|" + lineSeparator()
                + "|value 1|value 2|" + lineSeparator()
                + "|value 3|value 4|" + lineSeparator();

        ObjectMapper objectMapper = new ObjectMapper();
        table = objectMapper.writeValueAsString(table);

        String body = "{\"step\":\"Examples:\",\"data\":" + table + "}";

        verify(client).executePost(String.format(TEST_STEP_ENDPOINT, ISSUE_ID), body);
    }

    @Test
    void testCreateTestStepsStory() throws JiraConfigurationException, IOException
    {
        Story story = new Story();
        List<Scenario> scenarios = List.of(
                createScenario(NAME + 1),
                createScenario(NAME + 2),
                createScenario(NAME + 3)
        );
        story.setScenarios(scenarios);

        when(jiraClientProvider.getByJiraConfigurationKey(Optional.empty())).thenReturn(client);

        zephyrFacade.createTestSteps(story, ISSUE_ID);

        verify(client).executePost(String.format(TEST_STEP_ENDPOINT, ISSUE_ID),
                "{\"step\":\"name 1\"}");
        verify(client).executePost(String.format(TEST_STEP_ENDPOINT, ISSUE_ID),
                "{\"step\":\"name 2\"}");
        verify(client).executePost(String.format(TEST_STEP_ENDPOINT, ISSUE_ID),
                "{\"step\":\"name 3\"}");
        assertThat(logger.getLoggingEvents(), is(List.of(
                info(TEST_STEP_CREATED, 1),
                info(TEST_STEP_CREATED, 2),
                info(TEST_STEP_CREATED, 3))));
        verifyNoMoreInteractions(client);
    }

    private void mockJiraProjectRetrieve() throws IOException, JiraConfigurationException
    {
        Version version = new Version();
        version.setId(VERSION_ID);
        version.setName(TEST);
        Project project = new Project();
        project.setId(PROJECT_ID);
        project.setVersions(List.of(version));
        when(jiraFacade.getProject(zephyrExporterConfiguration.getProjectKey()))
            .thenReturn(project);
    }

    private void setConfiguration()
    {
        when(zephyrExporterConfiguration.getProjectKey()).thenReturn(TEST);
        when(zephyrExporterConfiguration.getVersionName()).thenReturn(TEST);
        when(zephyrExporterConfiguration.getCycleName()).thenReturn(TEST);
        when(zephyrExporterConfiguration.getFolderName()).thenReturn(TEST);
        Map<TestCaseStatus, String> statuses = new EnumMap<>(TestCaseStatus.class);
        statuses.put(TestCaseStatus.PASSED, TEST);
        when(zephyrExporterConfiguration.getStatuses()).thenReturn(statuses);
    }

    private ZephyrTestCase createZephyrTestCase()
    {
        ZephyrTestCase test = new ZephyrTestCase();
        test.setLabels(new LinkedHashSet<>(List.of("label")));
        test.setComponents(new LinkedHashSet<>(List.of("component")));
        test.setTestSteps(List.of(new CucumberTestStep("testStep 1"), new CucumberTestStep("testStep 2")));
        return test;
    }

    private void mockSerialization(ZephyrTestCase test) throws IOException
    {
        doAnswer(a ->
        {
            JsonGenerator generator = a.getArgument(1, JsonGenerator.class);
            generator.writeStartObject();
            generator.writeEndObject();
            return null;
        }).when(testCaseSerializer).serialize(eq(test), any(JsonGenerator.class), any(SerializerProvider.class));
    }

    private Step createStep(String value)
    {
        Step step = new Step();
        step.setValue(value);
        return step;
    }

    private Scenario createScenario(String title)
    {
        Scenario scenario = new Scenario();
        scenario.setTitle(title);
        return scenario;
    }
}
