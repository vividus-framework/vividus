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

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import org.apache.commons.lang3.function.FailableSupplier;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.message.BasicHeader;
import org.vividus.jira.databind.IssueLinkDeserializer;
import org.vividus.jira.databind.IssueLinkSerializer;
import org.vividus.jira.databind.JiraEntityDeserializer;
import org.vividus.jira.model.Attachment;
import org.vividus.jira.model.IssueLink;
import org.vividus.jira.model.JiraEntity;
import org.vividus.jira.model.Project;
import org.vividus.util.json.JsonPathUtils;

public class JiraFacade
{
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new SimpleModule()
                    .addSerializer(IssueLink.class, new IssueLinkSerializer())
                    .addDeserializer(IssueLink.class, new IssueLinkDeserializer())
                    .addDeserializer(JiraEntity.class, new JiraEntityDeserializer()))
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

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
        String createLinkRequest = OBJECT_MAPPER.writeValueAsString(issueLink);
        jiraClientProvider.getByIssueKey(inwardIssueKey).executePost("/rest/api/latest/issueLink", createLinkRequest);
    }

    public String getIssueStatus(String issueKey) throws IOException, JiraConfigurationException
    {
        String issue = jiraClientProvider.getByIssueKey(issueKey).executeGet(ISSUE_ENDPOINT + issueKey);
        return JsonPathUtils.getData(issue, "$.fields.status.name");
    }

    public Project getProject(String projectKey) throws IOException, JiraConfigurationException
    {
        return getJiraEntity("project/", projectKey, () -> jiraClientProvider.getByProjectKey(projectKey),
                Project.class);
    }

    public JiraEntity getIssue(String issueKey) throws IOException, JiraConfigurationException
    {
        return getJiraEntity(ISSUE, issueKey, JiraEntity.class);
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
        return getJiraEntity(relativeUrl, entityKey, () -> jiraClientProvider.getByIssueKey(entityKey), entityType);
    }

    private <T> T getJiraEntity(String relativeUrl, String entityKey,
            FailableSupplier<JiraClient, JiraConfigurationException> jiraClientSupplier, Class<T> entityType)
            throws IOException, JiraConfigurationException
    {
        String responseBody = jiraClientSupplier.get().executeGet(REST_API_ENDPOINT + relativeUrl + entityKey);
        return OBJECT_MAPPER.readValue(responseBody, entityType);
    }
}
