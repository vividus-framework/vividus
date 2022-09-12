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
class ZephyrScaleFacadeTests
{
    private static final String ATM_ENDPOINT = "/rest/atm/1.0";
    private static final String ATM_TEST_RUN_ENDPOINT = ATM_ENDPOINT + "/testrun";
    private static final String ATM_TEST_RESULT_ENDPOINT_FORMAT = ATM_TEST_RUN_ENDPOINT + "/%s/testcase/%s/testresult";
    private static final String PROJECT_ID = "11111";
    private static final String VERSION_ID = "11112";
    private static final String CYCLE_ID = "11113";
    private static final String ISSUE_ID = "11114";
    private static final String TEST = "test";
    private static final String CYCLE_ID_RESPONSE = "{\"key\": \"" + CYCLE_ID + "\"}";
    private static final String EXECUTION_FORMAT = "{\"projectKey\":\"%s\",\"folder\":\"/%s\",\"name\":\"%s\"}";

    @Mock private JiraFacade jiraFacade;
    @Mock private JiraClient client;
    @Mock private JiraClientProvider jiraClientProvider;
    @Mock private ZephyrExporterConfiguration zephyrExporterConfiguration;
    @InjectMocks private ZephyrScaleFacade zephyrScaleFacade;

    @BeforeEach
    void init()
    {
        zephyrScaleFacade = new ZephyrScaleFacade(jiraFacade, jiraClientProvider, zephyrExporterConfiguration,
                new ZephyrExporterProperties());
    }

    @Test
    void testCreateExecution() throws IOException, JiraConfigurationException
    {
        when(jiraClientProvider.getByJiraConfigurationKey(Optional.empty())).thenReturn(client);
        setConfiguration();
        mockJiraProjectRetrieve();
        String execution = String.format(EXECUTION_FORMAT, TEST, TEST, TEST);
        when(client.executePost(ATM_TEST_RUN_ENDPOINT, execution)).thenReturn(CYCLE_ID_RESPONSE);
        zephyrScaleFacade.prepareConfiguration();

        when(client.executePost(String.format(ATM_TEST_RESULT_ENDPOINT_FORMAT, CYCLE_ID, TEST),
                "{}")).thenReturn("{\"id\": 11116}");

        assertEquals(11_116, zephyrScaleFacade.createExecution(TEST));
    }

    @Test
    void testUpdateExecutionStatus() throws IOException, JiraConfigurationException
    {
        when(jiraClientProvider.getByJiraConfigurationKey(Optional.empty())).thenReturn(client);
        setConfiguration();
        mockJiraProjectRetrieve();
        String execution = String.format(EXECUTION_FORMAT, TEST, TEST, TEST);
        when(client.executePost(ATM_TEST_RUN_ENDPOINT, execution)).thenReturn(CYCLE_ID_RESPONSE);
        zephyrScaleFacade.prepareConfiguration();

        String executionBody = "{\"status\": \"1\"}";
        zephyrScaleFacade.updateExecutionStatus(TEST, executionBody);
        verify(client).executePut(String.format(ATM_TEST_RESULT_ENDPOINT_FORMAT, CYCLE_ID, TEST),
                executionBody);
    }

    @Test
    void testPrepareConfiguration() throws IOException, JiraConfigurationException
    {
        when(jiraClientProvider.getByJiraConfigurationKey(Optional.empty())).thenReturn(client);
        String execution = String.format(EXECUTION_FORMAT, TEST, TEST, TEST);
        when(client.executePost(ATM_TEST_RUN_ENDPOINT, execution)).thenReturn(CYCLE_ID_RESPONSE);
        setConfiguration();
        mockJiraProjectRetrieve();
        ZephyrConfiguration actualConfiguration = zephyrScaleFacade.prepareConfiguration();
        assertEquals(PROJECT_ID, actualConfiguration.getProjectId());
        assertEquals(CYCLE_ID, actualConfiguration.getCycleId());
        assertEquals(1, actualConfiguration.getTestStatusPerZephyrStatusMapping().size());
        assertEquals(TEST, actualConfiguration.getTestStatusPerZephyrStatusMapping().get(TestCaseStatus.PASSED));
    }

    @Test
    void testPrepareConfigurationWithoutFolder() throws IOException, JiraConfigurationException
    {
        when(jiraClientProvider.getByJiraConfigurationKey(Optional.empty())).thenReturn(client);
        when(zephyrExporterConfiguration.getProjectKey()).thenReturn(TEST);
        when(zephyrExporterConfiguration.getCycleName()).thenReturn(TEST);
        when(zephyrExporterConfiguration.getFolderName()).thenReturn("");
        Map<TestCaseStatus, String> statuses = new EnumMap<>(TestCaseStatus.class);
        statuses.put(TestCaseStatus.PASSED, TEST);
        when(zephyrExporterConfiguration.getStatuses()).thenReturn(statuses);
        mockJiraProjectRetrieve();
        String execution = String.format(EXECUTION_FORMAT, TEST, "", TEST);
        when(client.executePost(ATM_TEST_RUN_ENDPOINT, execution))
                .thenReturn(CYCLE_ID_RESPONSE);
        ZephyrConfiguration actualConfiguration = zephyrScaleFacade.prepareConfiguration();
        assertEquals(PROJECT_ID, actualConfiguration.getProjectId());
        assertEquals(CYCLE_ID, actualConfiguration.getCycleId());
        assertNull(actualConfiguration.getFolderId());
        assertEquals(1, actualConfiguration.getTestStatusPerZephyrStatusMapping().size());
        assertEquals(TEST, actualConfiguration.getTestStatusPerZephyrStatusMapping().get(TestCaseStatus.PASSED));
    }

    @Test
    void testFindExecutionId() throws IOException, JiraConfigurationException
    {
        UnsupportedOperationException exception = assertThrows(UnsupportedOperationException.class,
                () -> zephyrScaleFacade.findExecutionId(ISSUE_ID));
        assertEquals("Execution ID is not applicable for Zephyr Scale."
                + " All interaction occurs using the id of the test case", exception.getMessage());
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
        when(zephyrExporterConfiguration.getCycleName()).thenReturn(TEST);
        when(zephyrExporterConfiguration.getFolderName()).thenReturn(TEST);
        Map<TestCaseStatus, String> statuses = new EnumMap<>(TestCaseStatus.class);
        statuses.put(TestCaseStatus.PASSED, TEST);
        when(zephyrExporterConfiguration.getStatuses()).thenReturn(statuses);
    }
}
