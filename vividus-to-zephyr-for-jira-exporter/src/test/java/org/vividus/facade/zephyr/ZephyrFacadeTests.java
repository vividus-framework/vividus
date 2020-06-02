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

package org.vividus.facade.zephyr;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.facade.jira.JiraConfiguration;
import org.vividus.facade.jira.client.IJiraClient;

@ExtendWith(MockitoExtension.class)
class ZephyrFacadeTests
{
    private static final String GET_PROJECT_ID_AND_VERSION_ID_ENDPOINT = "/rest/api/latest/project/";
    private static final String ZAPI_ENDPOINT = "/rest/zapi/latest/";
    private static final String GET_CYCLE_ID_ENDPOINT = ZAPI_ENDPOINT + "cycle?projectId=%s&versionId=%s";
    private static final String GET_FOLDER_ID_ENDPOINT = ZAPI_ENDPOINT +  "cycle/%s/folders?projectId=%s&versionId=%s";
    private static final String CREATE_EXECUTION_ENDPOINT = ZAPI_ENDPOINT + "execution/";
    private static final String UPDATE_EXECUTION_STATUS_ENDPOINT = ZAPI_ENDPOINT + "execution/%s/execute";
    private static final String GET_PROJECT_ID_AND_VERSION_ID_RESPONSE = "{\"id\":\"11111\",\"key\":\"test\","
            + "\"versions\":[{\"id\":\"11112\",\"name\":\"test\"}]}";
    private static final String GET_CYCLE_ID_RESPONSE = "{\"11113\":{\"name\":\"test\"},\"recordsCount\":1}";
    private static final String PROJECT_ID = "11111";
    private static final String VERSION_ID = "11112";
    private static final String CYCLE_ID = "11113";
    private static final String FOLDER_ID = "11114";
    private static final String TEST = "test";
    private static final URI JIRA_URI = URI.create("https://jira.com");

    @Mock
    private IJiraClient client;

    @Mock
    private JiraConfiguration jiraConfiguration;

    @Mock
    private ZephyrConfiguration zephyrConfiguration;

    @InjectMocks
    private ZephyrFacade zephyrFacade;

    @Test
    void testCreateExecution()
    {
        String execution = "{\"cycleId\": \"11113\",\"issueId\": \"11115\",\"projectId\": \"11111\","
                + "\"versionId\": \"11112\",\"folderId\": 11114}";
        when(client.executePost(jiraConfiguration, CREATE_EXECUTION_ENDPOINT, execution))
                .thenReturn("{\"11116\": {\"id\": 11116,\"executionStatus\": \"-1\"}}");
        assertEquals(11_116, zephyrFacade.createExecution(execution));
    }

    @Test
    void testUpdateExecutionStatus()
    {
        String executionBody = "{\"status\": \"1\"}";
        zephyrFacade.updateExecutionStatus(11_116, executionBody);
        verify(client).executePut(jiraConfiguration, String.format(UPDATE_EXECUTION_STATUS_ENDPOINT, "11116"),
                executionBody);
    }

    @Test
    void testPrepareConfigurationEmptyUsername()
    {
        when(jiraConfiguration.getUsername()).thenReturn("");
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> zephyrFacade.prepareConfiguration());
        assertEquals("Property 'jira.username=' should not be empty", exception.getMessage());
    }

    @Test
    void testPrepareConfigurationEmptyPassword()
    {
        when(jiraConfiguration.getUsername()).thenReturn(TEST);
        when(jiraConfiguration.getPassword()).thenReturn("");
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> zephyrFacade.prepareConfiguration());
        assertEquals("Property 'jira.password=' should not be empty", exception.getMessage());
    }

    @Test
    void testPrepareConfigurationEmptyEndpoint()
    {
        when(jiraConfiguration.getUsername()).thenReturn(TEST);
        when(jiraConfiguration.getPassword()).thenReturn(TEST);
        when(jiraConfiguration.getEndpoint()).thenReturn(URI.create(""));
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> zephyrFacade.prepareConfiguration());
        assertEquals("Property 'jira.endpoint=' should not be empty", exception.getMessage());
    }

    @Test
    void testPrepareConfigurationEmptyProjectKey()
    {
        when(jiraConfiguration.getUsername()).thenReturn(TEST);
        when(jiraConfiguration.getPassword()).thenReturn(TEST);
        when(jiraConfiguration.getEndpoint()).thenReturn(JIRA_URI);
        when(zephyrConfiguration.getProjectKey()).thenReturn("");
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> zephyrFacade.prepareConfiguration());
        assertEquals("Property 'zephyr.project-key=' should not be empty", exception.getMessage());
    }

    @Test
    void testPrepareConfigurationEmptyVersionName()
    {
        when(jiraConfiguration.getUsername()).thenReturn(TEST);
        when(jiraConfiguration.getPassword()).thenReturn(TEST);
        when(jiraConfiguration.getEndpoint()).thenReturn(JIRA_URI);
        when(zephyrConfiguration.getProjectKey()).thenReturn(TEST);
        when(zephyrConfiguration.getVersionName()).thenReturn("");
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> zephyrFacade.prepareConfiguration());
        assertEquals("Property 'zephyr.version-name=' should not be empty", exception.getMessage());
    }

    @Test
    void testPrepareConfigurationEmptyCycleName()
    {
        when(jiraConfiguration.getUsername()).thenReturn(TEST);
        when(jiraConfiguration.getPassword()).thenReturn(TEST);
        when(jiraConfiguration.getEndpoint()).thenReturn(JIRA_URI);
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
        when(jiraConfiguration.getUsername()).thenReturn(TEST);
        when(jiraConfiguration.getPassword()).thenReturn(TEST);
        when(jiraConfiguration.getEndpoint()).thenReturn(JIRA_URI);
        when(zephyrConfiguration.getProjectKey()).thenReturn(TEST);
        when(zephyrConfiguration.getVersionName()).thenReturn(TEST);
        when(zephyrConfiguration.getCycleName()).thenReturn(TEST);
        when(zephyrConfiguration.getFolderName()).thenReturn("");
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> zephyrFacade.prepareConfiguration());
        assertEquals("Property 'zephyr.folder-name=' should not be empty", exception.getMessage());
    }

    @Test
    void testFindProjectAndVersionIdDoesNotExist()
    {
        setConfiguration();
        when(client.executeGet(jiraConfiguration, GET_PROJECT_ID_AND_VERSION_ID_ENDPOINT
                + zephyrConfiguration.getProjectKey())).thenReturn("{\"id\":\"11110\",\"key\":\"test\","
                + "\"versions\":[{\"id\":\"11112\",\"name\":\"test1\"}]}");
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> zephyrFacade.prepareConfiguration());
        assertEquals("Version by name 'test' does not exist", exception.getMessage());
    }

    @Test
    void testFindCycleIdDoesNotExist()
    {
        setConfiguration();
        when(client.executeGet(jiraConfiguration, GET_PROJECT_ID_AND_VERSION_ID_ENDPOINT
                + zephyrConfiguration.getProjectKey())).thenReturn(GET_PROJECT_ID_AND_VERSION_ID_RESPONSE);
        when(zephyrConfiguration.getProjectId()).thenReturn(PROJECT_ID);
        when(zephyrConfiguration.getVersionId()).thenReturn(VERSION_ID);
        when(client.executeGet(jiraConfiguration, String.format(GET_CYCLE_ID_ENDPOINT,
                zephyrConfiguration.getProjectId(), zephyrConfiguration.getVersionId()))).
                thenReturn("{\"-1\":{\"name\":\"test1\"},\"recordsCount\":1}");
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> zephyrFacade.prepareConfiguration());
        assertEquals("Cycle by name 'test' does not exist", exception.getMessage());
    }

    @Test
    void testFindFolderIdDoesNotExist()
    {
        setConfiguration();
        when(client.executeGet(jiraConfiguration, GET_PROJECT_ID_AND_VERSION_ID_ENDPOINT
                + zephyrConfiguration.getProjectKey())).thenReturn(GET_PROJECT_ID_AND_VERSION_ID_RESPONSE);
        when(zephyrConfiguration.getProjectId()).thenReturn(PROJECT_ID);
        when(zephyrConfiguration.getVersionId()).thenReturn(VERSION_ID);
        when(client.executeGet(jiraConfiguration, String.format(GET_CYCLE_ID_ENDPOINT,
                zephyrConfiguration.getProjectId(), zephyrConfiguration.getVersionId()))).
                thenReturn(GET_CYCLE_ID_RESPONSE);
        when(zephyrConfiguration.getCycleId()).thenReturn(CYCLE_ID);
        when(client.executeGet(jiraConfiguration, String.format(GET_FOLDER_ID_ENDPOINT,
                zephyrConfiguration.getCycleId(), zephyrConfiguration.getProjectId(),
                zephyrConfiguration.getVersionId()))).
                thenReturn("[{\"folderId\":0,\"folderName\":\"test1\"}]");
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> zephyrFacade.prepareConfiguration());
        assertEquals("Folder by name 'test' does not exist", exception.getMessage());
    }

    @Test
    void testPrepareConfiguration()
    {
        setConfiguration();
        when(client.executeGet(jiraConfiguration, GET_PROJECT_ID_AND_VERSION_ID_ENDPOINT
                + zephyrConfiguration.getProjectKey())).thenReturn(GET_PROJECT_ID_AND_VERSION_ID_RESPONSE);
        when(zephyrConfiguration.getProjectId()).thenReturn(PROJECT_ID);
        when(zephyrConfiguration.getVersionId()).thenReturn(VERSION_ID);
        when(client.executeGet(jiraConfiguration, String.format(GET_CYCLE_ID_ENDPOINT,
                zephyrConfiguration.getProjectId(), zephyrConfiguration.getVersionId()))).
                thenReturn(GET_CYCLE_ID_RESPONSE);
        when(zephyrConfiguration.getCycleId()).thenReturn(CYCLE_ID);
        when(client.executeGet(jiraConfiguration, String.format(GET_FOLDER_ID_ENDPOINT,
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

    private void setConfiguration()
    {
        when(jiraConfiguration.getUsername()).thenReturn(TEST);
        when(jiraConfiguration.getPassword()).thenReturn(TEST);
        when(jiraConfiguration.getEndpoint()).thenReturn(JIRA_URI);
        when(zephyrConfiguration.getProjectKey()).thenReturn(TEST);
        when(zephyrConfiguration.getVersionName()).thenReturn(TEST);
        when(zephyrConfiguration.getCycleName()).thenReturn(TEST);
        when(zephyrConfiguration.getFolderName()).thenReturn(TEST);
    }
}
