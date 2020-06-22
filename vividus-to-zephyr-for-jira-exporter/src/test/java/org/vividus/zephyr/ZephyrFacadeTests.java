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

package org.vividus.zephyr;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.jira.JiraClient;
import org.vividus.jira.JiraFacade;
import org.vividus.jira.model.Project;
import org.vividus.jira.model.Version;

@ExtendWith(MockitoExtension.class)
class ZephyrFacadeTests
{
    private static final String ZAPI_ENDPOINT = "/rest/zapi/latest/";
    private static final String GET_CYCLE_ID_ENDPOINT = ZAPI_ENDPOINT + "cycle?projectId=%s&versionId=%s";
    private static final String GET_FOLDER_ID_ENDPOINT = ZAPI_ENDPOINT +  "cycle/%s/folders?projectId=%s&versionId=%s";
    private static final String CREATE_EXECUTION_ENDPOINT = ZAPI_ENDPOINT + "execution/";
    private static final String UPDATE_EXECUTION_STATUS_ENDPOINT = ZAPI_ENDPOINT + "execution/%s/execute";
    private static final String GET_CYCLE_ID_RESPONSE = "{\"11113\":{\"name\":\"test\"},\"recordsCount\":1}";
    private static final String PROJECT_ID = "11111";
    private static final String VERSION_ID = "11112";
    private static final String CYCLE_ID = "11113";
    private static final String FOLDER_ID = "11114";
    private static final String TEST = "test";

    @Mock
    private JiraFacade jiraFacade;

    @Mock
    private JiraClient client;

    @Mock
    private ZephyrConfiguration zephyrConfiguration;

    @InjectMocks
    private ZephyrFacade zephyrFacade;

    @Test
    void testCreateExecution() throws IOException
    {
        String execution = "{\"cycleId\": \"11113\",\"issueId\": \"11115\",\"projectId\": \"11111\","
                + "\"versionId\": \"11112\",\"folderId\": 11114}";
        when(client.executePost(CREATE_EXECUTION_ENDPOINT, execution)).thenReturn(
                "{\"11116\": {\"id\": 11116,\"executionStatus\": \"-1\"}}");
        assertEquals(11_116, zephyrFacade.createExecution(execution));
    }

    @Test
    void testUpdateExecutionStatus() throws IOException
    {
        String executionBody = "{\"status\": \"1\"}";
        zephyrFacade.updateExecutionStatus(11_116, executionBody);
        verify(client).executePut(String.format(UPDATE_EXECUTION_STATUS_ENDPOINT, "11116"), executionBody);
    }

    @Test
    void testPrepareConfigurationEmptyProjectKey()
    {
        when(zephyrConfiguration.getProjectKey()).thenReturn("");
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> zephyrFacade.prepareConfiguration());
        assertEquals("Property 'zephyr.project-key=' should not be empty", exception.getMessage());
    }

    @Test
    void testPrepareConfigurationEmptyVersionName()
    {
        when(zephyrConfiguration.getProjectKey()).thenReturn(TEST);
        when(zephyrConfiguration.getVersionName()).thenReturn("");
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> zephyrFacade.prepareConfiguration());
        assertEquals("Property 'zephyr.version-name=' should not be empty", exception.getMessage());
    }

    @Test
    void testPrepareConfigurationEmptyCycleName()
    {
        when(zephyrConfiguration.getProjectKey()).thenReturn(TEST);
        when(zephyrConfiguration.getVersionName()).thenReturn(TEST);
        when(zephyrConfiguration.getCycleName()).thenReturn("");
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> zephyrFacade.prepareConfiguration());
        assertEquals("Property 'zephyr.cycle-name=' should not be empty", exception.getMessage());
    }

    @Test
    void testPrepareConfigurationEmptyFolderName()
    {
        when(zephyrConfiguration.getProjectKey()).thenReturn(TEST);
        when(zephyrConfiguration.getVersionName()).thenReturn(TEST);
        when(zephyrConfiguration.getCycleName()).thenReturn(TEST);
        when(zephyrConfiguration.getFolderName()).thenReturn("");
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> zephyrFacade.prepareConfiguration());
        assertEquals("Property 'zephyr.folder-name=' should not be empty", exception.getMessage());
    }

    @Test
    void testFindProjectAndVersionIdDoesNotExist() throws IOException
    {
        setConfiguration();
        Version version = new Version();
        version.setId(VERSION_ID);
        version.setName("test1");
        Project project = new Project();
        project.setId("11110");
        project.setVersions(List.of(version));
        when(jiraFacade.getProject(zephyrConfiguration.getProjectKey())).thenReturn(project);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> zephyrFacade.prepareConfiguration());
        assertEquals("Version with name 'test' does not exist", exception.getMessage());
    }

    @Test
    void testFindCycleIdDoesNotExist() throws IOException
    {
        setConfiguration();
        mockJiraProjectRetrieve();
        when(zephyrConfiguration.getProjectId()).thenReturn(PROJECT_ID);
        when(zephyrConfiguration.getVersionId()).thenReturn(VERSION_ID);
        when(client.executeGet(String.format(GET_CYCLE_ID_ENDPOINT,
                zephyrConfiguration.getProjectId(), zephyrConfiguration.getVersionId()))).
                thenReturn("{\"-1\":{\"name\":\"test1\"},\"recordsCount\":1}");
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> zephyrFacade.prepareConfiguration());
        assertEquals("Cycle with name 'test' does not exist", exception.getMessage());
    }

    @Test
    void testFindFolderIdDoesNotExist() throws IOException
    {
        setConfiguration();
        mockJiraProjectRetrieve();
        when(zephyrConfiguration.getProjectId()).thenReturn(PROJECT_ID);
        when(zephyrConfiguration.getVersionId()).thenReturn(VERSION_ID);
        when(client.executeGet(String.format(GET_CYCLE_ID_ENDPOINT,
                zephyrConfiguration.getProjectId(), zephyrConfiguration.getVersionId()))).
                thenReturn(GET_CYCLE_ID_RESPONSE);
        when(zephyrConfiguration.getCycleId()).thenReturn(CYCLE_ID);
        when(client.executeGet(String.format(GET_FOLDER_ID_ENDPOINT,
                zephyrConfiguration.getCycleId(), zephyrConfiguration.getProjectId(),
                zephyrConfiguration.getVersionId()))).
                thenReturn("[{\"folderId\":0,\"folderName\":\"test1\"}]");
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> zephyrFacade.prepareConfiguration());
        assertEquals("Folder with name 'test' does not exist", exception.getMessage());
    }

    @Test
    void testPrepareConfiguration() throws IOException
    {
        setConfiguration();
        mockJiraProjectRetrieve();
        when(zephyrConfiguration.getProjectId()).thenReturn(PROJECT_ID);
        when(zephyrConfiguration.getVersionId()).thenReturn(VERSION_ID);
        when(client.executeGet(String.format(GET_CYCLE_ID_ENDPOINT,
                zephyrConfiguration.getProjectId(), zephyrConfiguration.getVersionId()))).
                thenReturn(GET_CYCLE_ID_RESPONSE);
        when(zephyrConfiguration.getCycleId()).thenReturn(CYCLE_ID);
        when(client.executeGet(String.format(GET_FOLDER_ID_ENDPOINT,
                zephyrConfiguration.getCycleId(), zephyrConfiguration.getProjectId(),
                zephyrConfiguration.getVersionId()))).
                thenReturn("[{\"folderId\":11114,\"folderName\":\"test\"}]");
        when(zephyrConfiguration.getFolderId()).thenReturn(FOLDER_ID);
        zephyrFacade.prepareConfiguration();
        assertEquals(PROJECT_ID, zephyrConfiguration.getProjectId());
        assertEquals(VERSION_ID, zephyrConfiguration.getVersionId());
        assertEquals(CYCLE_ID, zephyrConfiguration.getCycleId());
        assertEquals(FOLDER_ID, zephyrConfiguration.getFolderId());
    }

    private void mockJiraProjectRetrieve() throws IOException
    {
        Version version = new Version();
        version.setId(VERSION_ID);
        version.setName(TEST);
        Project project = new Project();
        project.setId(PROJECT_ID);
        project.setVersions(List.of(version));
        when(jiraFacade.getProject(zephyrConfiguration.getProjectKey())).thenReturn(project);
    }

    private void setConfiguration()
    {
        when(zephyrConfiguration.getProjectKey()).thenReturn(TEST);
        when(zephyrConfiguration.getVersionName()).thenReturn(TEST);
        when(zephyrConfiguration.getCycleName()).thenReturn(TEST);
        when(zephyrConfiguration.getFolderName()).thenReturn(TEST);
    }
}
