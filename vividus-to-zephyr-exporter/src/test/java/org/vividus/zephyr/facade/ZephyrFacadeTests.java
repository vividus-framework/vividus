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

package org.vividus.zephyr.facade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;

import org.junit.jupiter.api.BeforeEach;
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
import org.vividus.zephyr.configuration.ZephyrConfiguration;
import org.vividus.zephyr.configuration.ZephyrExporterConfiguration;
import org.vividus.zephyr.configuration.ZephyrExporterProperties;
import org.vividus.zephyr.model.TestCaseStatus;

@ExtendWith(MockitoExtension.class)
class ZephyrFacadeTests
{
    private static final String ZAPI_ENDPOINT = "/rest/zapi/latest/";
    private static final String GET_CYCLE_ID_ENDPOINT = ZAPI_ENDPOINT + "cycle?projectId=%s&versionId=%s";
    private static final String GET_FOLDER_ID_ENDPOINT = ZAPI_ENDPOINT +  "cycle/%s/folders?projectId=%s&versionId=%s";
    private static final String GET_STATUSES_ENDPOINT = ZAPI_ENDPOINT + "util/testExecutionStatus";
    private static final String GET_EXECUTION_ID_ENDPOINT = ZAPI_ENDPOINT + "execution?issueId=111";
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

    @Mock private JiraFacade jiraFacade;
    @Mock private JiraClient client;
    @Mock private JiraClientProvider jiraClientProvider;
    @Mock private ZephyrExporterConfiguration zephyrExporterConfiguration;
    @InjectMocks private ZephyrFacade zephyrFacade;

    @BeforeEach
    void init()
    {
        zephyrFacade = new ZephyrFacade(jiraFacade, jiraClientProvider, zephyrExporterConfiguration,
                new ZephyrExporterProperties());
    }

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
}
