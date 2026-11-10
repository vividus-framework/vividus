/*
 * Copyright 2019-2026 the original author or authors.
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.jira.JiraClient;
import org.vividus.jira.JiraClientProvider;
import org.vividus.jira.JiraConfigurationException;

@ExtendWith(MockitoExtension.class)
class XrayServerClientTests
{
    private static final String EXECUTION_RESPONSE =
            "{\"testExecIssue\":{\"id\":\"01101\",\"key\":\"TEST-1\","
            + "\"self\":\"https://jira.com/rest/api/2/issue/01101\"}}";
    private static final String ISSUE_KEY = "TEST-1";
    private static final String TEST_SET_KEY = "TEST-2";
    private static final String TEST_CASE_KEY_1 = "TEST-3";
    private static final String TEST_CASE_KEY_2 = "TEST-4";
    private static final String IMPORT_ENDPOINT = "/rest/raven/1.0/import/execution";
    private static final String EXECUTION_JSON = "{\"tests\":[]}";
    private static final String CONFIG_ERROR = "config error";

    @Mock private JiraClientProvider jiraClientProvider;
    @Mock private JiraClient jiraClient;

    @Test
    void shouldImportExecution() throws IOException, JiraConfigurationException
    {
        when(jiraClientProvider.getByJiraConfigurationKey(Optional.empty())).thenReturn(jiraClient);
        when(jiraClient.executePost(IMPORT_ENDPOINT, EXECUTION_JSON)).thenReturn(EXECUTION_RESPONSE);

        XrayServerClient client = new XrayServerClient(jiraClientProvider, null);
        String key = client.importExecution(EXECUTION_JSON);

        assertEquals(ISSUE_KEY, key);
        verify(jiraClient).executePost(IMPORT_ENDPOINT, EXECUTION_JSON);
    }

    @Test
    void shouldImportExecutionWithJiraInstanceKey() throws IOException, JiraConfigurationException
    {
        String instanceKey = "my-jira";
        when(jiraClientProvider.getByJiraConfigurationKey(Optional.of(instanceKey))).thenReturn(jiraClient);
        when(jiraClient.executePost(IMPORT_ENDPOINT, EXECUTION_JSON)).thenReturn(EXECUTION_RESPONSE);

        XrayServerClient client = new XrayServerClient(jiraClientProvider, instanceKey);
        String key = client.importExecution(EXECUTION_JSON);

        assertEquals(ISSUE_KEY, key);
    }

    @Test
    void shouldWrapJiraConfigurationExceptionOnImportExecution() throws JiraConfigurationException
    {
        JiraConfigurationException cause = new JiraConfigurationException(CONFIG_ERROR);
        when(jiraClientProvider.getByJiraConfigurationKey(Optional.empty())).thenThrow(cause);

        XrayServerClient client = new XrayServerClient(jiraClientProvider, null);
        IOException thrown = assertThrows(IOException.class, () -> client.importExecution(EXECUTION_JSON));

        assertInstanceOf(JiraConfigurationException.class, thrown.getCause());
        assertEquals(cause, thrown.getCause());
    }

    @Test
    void shouldAddTestsToTestSet() throws IOException, JiraConfigurationException
    {
        when(jiraClientProvider.getByIssueKey(TEST_SET_KEY)).thenReturn(jiraClient);

        XrayServerClient client = new XrayServerClient(jiraClientProvider, null);
        client.addTestsToTestSet(TEST_SET_KEY, List.of(TEST_CASE_KEY_1, TEST_CASE_KEY_2));

        verify(jiraClient).executePost(
                "/rest/raven/1.0/api/testset/" + TEST_SET_KEY + "/test",
                "{\"add\":[\"TEST-3\",\"TEST-4\"]}");
    }

    @Test
    void shouldWrapJiraConfigurationExceptionOnAddTestsToTestSet() throws JiraConfigurationException
    {
        JiraConfigurationException cause = new JiraConfigurationException(CONFIG_ERROR);
        when(jiraClientProvider.getByIssueKey(TEST_SET_KEY)).thenThrow(cause);

        XrayServerClient client = new XrayServerClient(jiraClientProvider, null);
        IOException thrown = assertThrows(IOException.class,
                () -> client.addTestsToTestSet(TEST_SET_KEY, List.of(TEST_CASE_KEY_1)));

        assertInstanceOf(JiraConfigurationException.class, thrown.getCause());
        assertEquals(cause, thrown.getCause());
    }
}
