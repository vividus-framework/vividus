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

package org.vividus.jira;

import static java.util.Optional.empty;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.jira.model.JiraEntity;
import org.vividus.jira.model.Project;

@ExtendWith(MockitoExtension.class)
class JiraFacadeTests
{
    private static final String ISSUE_ID = "issue id";
    private static final String ISSUE_BODY = "issue body";
    private static final String ISSUE_ENDPOINT = "/rest/api/latest/issue/";
    private static final String TRANSITIONS_ENDPOINT = ISSUE_ENDPOINT + "%s/transitions/";
    private static final String BACKLOG = "Backlog";

    @Mock private JiraClient jiraClient;
    @Mock private JiraClientProvider jiraClientProvider;
    @InjectMocks private JiraFacade jiraFacade;

    @AfterEach
    void afterEach()
    {
        verifyNoMoreInteractions(jiraClient);
    }

    @Test
    void shouldCreateIssue() throws IOException, JiraConfigurationException
    {
        when(jiraClientProvider.getByJiraConfigurationKey(empty())).thenReturn(jiraClient);
        when(jiraClient.executePost(ISSUE_ENDPOINT, ISSUE_BODY)).thenReturn(ISSUE_ID);
        String issueId = jiraFacade.createIssue(ISSUE_BODY, empty());
        assertEquals(ISSUE_ID, issueId);
    }

    @Test
    void shouldUpdateIssue() throws IOException, JiraConfigurationException
    {
        when(jiraClientProvider.getByIssueKey(ISSUE_ID)).thenReturn(jiraClient);
        when(jiraClient.executePut(ISSUE_ENDPOINT + ISSUE_ID, ISSUE_BODY)).thenReturn(ISSUE_ID);
        String issueId = jiraFacade.updateIssue(ISSUE_ID, ISSUE_BODY);
        assertEquals(ISSUE_ID, issueId);
    }

    @Test
    void shouldCreateIssueLink() throws IOException, JiraConfigurationException
    {
        String requirementKey = "requirement id";
        String linkRequest = "{\"type\":{\"name\":\"Tests\"},\"inwardIssue\":{\"key\":\"issue id\"},"
                + "\"outwardIssue\":{\"key\":\"requirement id\"}}";
        when(jiraClientProvider.getByIssueKey(ISSUE_ID)).thenReturn(jiraClient);
        jiraFacade.createIssueLink(ISSUE_ID, requirementKey, "Tests");
        verify(jiraClient).executePost("/rest/api/latest/issueLink", linkRequest);
    }

    @Test
    void shouldReturnIssueStatus() throws IOException, JiraConfigurationException
    {
        when(jiraClientProvider.getByIssueKey(ISSUE_ID)).thenReturn(jiraClient);
        when(jiraClient.executeGet(ISSUE_ENDPOINT + ISSUE_ID))
                .thenReturn("{\"fields\":{\"status\": {\"name\" : \"Open\"}}}");
        assertEquals("Open", jiraFacade.getIssueStatus(ISSUE_ID));
    }

    @Test
    void shouldSetIssueStatus() throws JiraConfigurationException, IOException
    {
        String transitionsEndpoint = String.format(TRANSITIONS_ENDPOINT, ISSUE_ID);
        when(jiraClientProvider.getByIssueKey(ISSUE_ID)).thenReturn(jiraClient);
        when(jiraClient.executePost(eq(transitionsEndpoint), eq("{'transition':{'id':'1'}}")))
                .thenReturn(ISSUE_ID);
        when(jiraClient.executeGet(eq(transitionsEndpoint)))
                .thenReturn("{\"expand\": \"transitions\",\"transitions\": [{\"id\": \"1"
                        + "\",\"name\": \"Move to Backlog\",\"to\":{\"name\": \"Backlog\",\"id\":\"10000\"}}]}");

        String issueId = jiraFacade.changeIssueStatus(ISSUE_ID, BACKLOG);
        assertEquals(ISSUE_ID, issueId);
    }

    @Test
    void shouldGetTransitionId() throws IOException, JiraConfigurationException
    {
        String expectedTransitionId = "11";
        when(jiraClientProvider.getByIssueKey(ISSUE_ID)).thenReturn(jiraClient);
        when(jiraClient.executeGet(String.format(TRANSITIONS_ENDPOINT, ISSUE_ID)))
                .thenReturn("{\"expand\": \"transitions\",\"transitions\": [{\"id\": \"" + expectedTransitionId
                + "\",\"name\": \"To Do\",\"to\":{\"self\": \"https://jira.atlassian.net/rest/api/2/status/10000\","
                + "\"name\": \"Backlog\",\"id\":\"10000\"}},{\"id\": \"12\",\"name\": \"To Do\",\"to\": {\"self\": "
                + "\"https://jira.atlassian.net/rest/api/2/status/10001\",\"name\": \"Open\",\"id\": \"10001\"}}]}");

        String transitionId = jiraFacade.getTransitionIdByName(ISSUE_ID, BACKLOG);
        assertEquals(expectedTransitionId, transitionId);
    }

    @Test
    void shouldGetIssue() throws IOException, JiraConfigurationException
    {
        when(jiraClientProvider.getByIssueKey(ISSUE_ID)).thenReturn(jiraClient);
        when(jiraClient.executeGet(ISSUE_ENDPOINT + ISSUE_ID)).thenReturn("{\"id\":\"001\"}");
        JiraEntity issue = jiraFacade.getIssue(ISSUE_ID);
        assertEquals("001", issue.getId());
    }

    @Test
    void shouldGetProject() throws IOException, JiraConfigurationException
    {
        String projectKey = "TEST";
        when(jiraClientProvider.getByProjectKey(projectKey)).thenReturn(jiraClient);
        when(jiraClient.executeGet("/rest/api/latest/project/TEST"))
                .thenReturn("{\"id\": \"002\", \"key\": \"TEST\", "
                + "\"versions\": [{\"id\": \"0021\", \"name\": \"Release 1.0\"}, "
                + "{\"id\": \"0022\", \"name\": \"Release 2.0\"}]}");
        Project project = jiraFacade.getProject(projectKey);
        assertEquals("002", project.getId());
        assertEquals(2, project.getVersions().size());
        assertEquals("0021", project.getVersions().get(0).getId());
        assertEquals("Release 1.0", project.getVersions().get(0).getName());
        assertEquals("0022", project.getVersions().get(1).getId());
        assertEquals("Release 2.0", project.getVersions().get(1).getName());
    }
}
