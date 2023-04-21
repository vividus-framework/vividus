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

package org.vividus.jira;

import static java.util.Optional.empty;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;

import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.jira.model.Attachment;
import org.vividus.jira.model.IssueLink;
import org.vividus.jira.model.JiraEntity;
import org.vividus.jira.model.Project;
import org.vividus.util.ResourceUtils;

@ExtendWith(MockitoExtension.class)
class JiraFacadeTests
{
    private static final String ISSUE_ID = "issue id";
    private static final String ISSUE_BODY = "issue body";
    private static final String ISSUE_ENDPOINT = "/rest/api/latest/issue/";
    private static final String TESTS_LINK = "Tests";

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
        jiraFacade.createIssueLink(ISSUE_ID, requirementKey, TESTS_LINK);
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
        String responseBody = ResourceUtils.loadResource(getClass(), "issue.json");
        when(jiraClient.executeGet(ISSUE_ENDPOINT + ISSUE_ID)).thenReturn(responseBody);
        JiraEntity issue = jiraFacade.getIssue(ISSUE_ID);
        assertEquals("31354", issue.getId());
        List<IssueLink> issueLinks = issue.getIssueLinks();
        assertThat(issueLinks, hasSize(1));
        IssueLink issueLink = issueLinks.get(0);
        assertNull(issueLink.getInwardIssueKey());
        assertEquals(TESTS_LINK, issueLink.getType());
        assertEquals("VVDS-2", issueLink.getOutwardIssueKey());
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

    @SuppressWarnings("unchecked")
    @Test
    void shouldAddAttachment() throws IOException, JiraConfigurationException, ParseException
    {
        when(jiraClientProvider.getByIssueKey(ISSUE_ID)).thenReturn(jiraClient);

        String name = "attachment-name";
        byte[] body = { 1, 0, 1, 0, 1, 0 };

        jiraFacade.addAttachments(ISSUE_ID, List.of(new Attachment(name, body)));

        ArgumentCaptor<HttpEntity> httpEntityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        ArgumentCaptor<List<Header>> headersCaptor = ArgumentCaptor.forClass(List.class);

        verify(jiraClient).executePost(eq(ISSUE_ENDPOINT + ISSUE_ID + "/attachments"), headersCaptor.capture(),
                httpEntityCaptor.capture());
        List<Header> headers = headersCaptor.getValue();
        assertThat(headers, hasSize(1));
        assertEquals("X-Atlassian-Token", headers.get(0).getName());
        assertEquals("no-check", headers.get(0).getValue());
        HttpEntity httpEntity = httpEntityCaptor.getValue();
        String entityAsString = EntityUtils.toString(httpEntity);
        assertTrue(entityAsString.contains("Content-Disposition: form-data; name=\"file\"; "
                + "filename=\"attachment-name\""));
        assertTrue(entityAsString.contains("Content-Type: multipart/form-data; charset=ISO-8859-1"));
    }
}
