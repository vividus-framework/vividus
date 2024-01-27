/*
 * Copyright 2019-2024 the original author or authors.
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

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.function.FailableSupplier;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.message.BasicHeader;
import org.vividus.jira.mapper.JiraEntityMapper;
import org.vividus.jira.model.Attachment;
import org.vividus.jira.model.IssueLink;
import org.vividus.jira.model.JiraEntity;
import org.vividus.jira.model.Project;

public class JiraFacade
{
    private static final String REST_API_ENDPOINT = "/rest/api/latest/";
    private static final String ISSUE = "issue/";
    private static final String ISSUE_ENDPOINT = REST_API_ENDPOINT + ISSUE;

    private final JiraClientProvider jiraClientProvider;

    public JiraFacade(JiraClientProvider jiraClientProvider)
    {
        this.jiraClientProvider = jiraClientProvider;
    }

    public String createIssue(String issueBody, Optional<String> jiraInstanceKey)
            throws IOException, JiraConfigurationException
    {
        return jiraClientProvider.getByJiraConfigurationKey(jiraInstanceKey).executePost(ISSUE_ENDPOINT, issueBody);
    }

    public String updateIssue(String issueKey, String issueBody) throws IOException, JiraConfigurationException
    {
        return jiraClientProvider.getByIssueKey(issueKey).executePut(ISSUE_ENDPOINT + issueKey, issueBody);
    }

    public void createIssueLink(String inwardIssueKey, String outwardIssueKey, String type)
            throws IOException, JiraConfigurationException
    {
        IssueLink issueLink = new IssueLink(type, inwardIssueKey, outwardIssueKey);
        String createLinkRequest = JiraEntityMapper.writeValueAsString(issueLink);
        jiraClientProvider.getByIssueKey(inwardIssueKey).executePost("/rest/api/latest/issueLink", createLinkRequest);
    }

    public Project getProject(String projectKey) throws IOException, JiraConfigurationException
    {
        String response = retrieveEntity("project/", projectKey, () -> jiraClientProvider.getByProjectKey(projectKey));
        return JiraEntityMapper.readValue(response, Project.class);
    }

    public JiraEntity getIssue(String issueKey) throws IOException, JiraConfigurationException
    {
        return getJiraEntity(ISSUE, issueKey, JiraEntity.class);
    }

    public String getIssueAsJson(String issueKey) throws IOException, JiraConfigurationException
    {
        return retrieveEntity(ISSUE, issueKey, () -> jiraClientProvider.getByIssueKey(issueKey));
    }

    public void addAttachments(String issueKey, List<Attachment> attachments)
            throws IOException, JiraConfigurationException
    {
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        attachments.forEach(attachment -> builder.addBinaryBody("file", attachment.getBody(),
                ContentType.MULTIPART_FORM_DATA, attachment.getName()));
        jiraClientProvider.getByIssueKey(issueKey).executePost(ISSUE_ENDPOINT + issueKey + "/attachments",
                List.of(new BasicHeader("X-Atlassian-Token", "no-check")), builder.build());
    }

    private <T> T getJiraEntity(String relativeUrl, String entityKey, Class<T> entityType)
            throws IOException, JiraConfigurationException
    {
        String response = retrieveEntity(relativeUrl, entityKey, () -> jiraClientProvider.getByIssueKey(entityKey));
        return JiraEntityMapper.readValue(response, entityType);
    }

    private String retrieveEntity(String relativeUrl, String entityKey,
            FailableSupplier<JiraClient, JiraConfigurationException> jiraClientSupplier)
            throws IOException, JiraConfigurationException
    {
        return jiraClientSupplier.get().executeGet(REST_API_ENDPOINT + relativeUrl + entityKey);
    }
}
