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

package org.vividus.jira;

import static java.util.Optional.empty;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
    void shouldReturnIssueStatue() throws IOException, JiraConfigurationException
    {
        when(jiraClientProvider.getByIssueKey(ISSUE_ID)).thenReturn(jiraClient);
        when(jiraClient.executeGet(ISSUE_ENDPOINT + ISSUE_ID))
                .thenReturn("{\"fields\":{\"status\": {\"name\" : \"Open\"}}}");
        assertEquals("Open", jiraFacade.getIssueStatus(ISSUE_ID));
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
        when(jiraClient.executeGet("/rest/api/latest/project/TEST")).thenReturn("{\"id\": \"002\", \"key\": \"TEST\", "
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
