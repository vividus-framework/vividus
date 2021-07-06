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

import java.io.IOException;
import java.util.Optional;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import org.vividus.jira.JiraClientProvider.JiraConfigurationException;
import org.vividus.jira.databind.IssueLinkSerializer;
import org.vividus.jira.model.IssueLink;
import org.vividus.jira.model.JiraEntity;
import org.vividus.jira.model.Project;
import org.vividus.util.json.JsonPathUtils;

public class JiraFacade
{
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new SimpleModule().addSerializer(IssueLink.class, new IssueLinkSerializer()))
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    private static final String REST_API_ENDPOINT = "/rest/api/latest/";
    private static final String ISSUE = "issue/";
    private static final String ISSUE_ENDPOINT = REST_API_ENDPOINT + ISSUE;

    private final JiraClientProvider jiraClientProvider;

    public JiraFacade(JiraClientProvider jiraClientProvider)
    {
        this.jiraClientProvider = jiraClientProvider;
    }

    public String createIssue(String issueBody, Optional<String> jiraKey) throws IOException, JiraConfigurationException
    {
        return jiraClientProvider.get(null, jiraKey).executePost(ISSUE_ENDPOINT, issueBody);
    }

    public String updateIssue(String issueKey, String issueBody, Optional<String> jiraKey)
            throws IOException, JiraConfigurationException
    {
        return jiraClientProvider.get(issueKey, jiraKey).executePut(ISSUE_ENDPOINT + issueKey, issueBody);
    }

    public void createIssueLink(String inwardIssueKey, String outwardIssueKey, String type, Optional<String> jiraKey)
            throws IOException, JiraConfigurationException
    {
        IssueLink issueLink = new IssueLink(type, inwardIssueKey, outwardIssueKey);
        String createLinkRequest = OBJECT_MAPPER.writeValueAsString(issueLink);
        jiraClientProvider.get(inwardIssueKey, jiraKey).executePost("/rest/api/latest/issueLink", createLinkRequest);
    }

    public String getIssueStatus(String issueKey, Optional<String> jiraKey)
            throws IOException, JiraConfigurationException
    {
        String issue = jiraClientProvider.get(issueKey, jiraKey).executeGet(ISSUE_ENDPOINT + issueKey);
        return JsonPathUtils.getData(issue, "$.fields.status.name");
    }

    public Project getProject(String projectKey, Optional<String> jiraKey)
            throws IOException, JiraConfigurationException
    {
        return getJiraEntity("project/", projectKey, Project.class, jiraKey);
    }

    public JiraEntity getIssue(String issueKey, Optional<String> jiraKey) throws IOException, JiraConfigurationException
    {
        return getJiraEntity(ISSUE, issueKey, JiraEntity.class, jiraKey);
    }

    private <T> T getJiraEntity(String relativeUrl, String entityKey, Class<T> entityType, Optional<String> jiraKey)
            throws IOException, JiraConfigurationException
    {
        String responseBody = jiraClientProvider.get(entityKey, jiraKey)
                .executeGet(REST_API_ENDPOINT + relativeUrl + entityKey);
        return OBJECT_MAPPER.readValue(responseBody, entityType);
    }
}
