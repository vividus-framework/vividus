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

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import org.vividus.jira.databind.IssueLinkSerializer;
import org.vividus.jira.databind.JiraIssueDeserializer;
import org.vividus.jira.model.IssueLink;
import org.vividus.jira.model.JiraIssue;
import org.vividus.jira.model.Project;

public class JiraFacade
{
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new SimpleModule().addSerializer(IssueLink.class, new IssueLinkSerializer())
                                              .addDeserializer(JiraIssue.class, new JiraIssueDeserializer()))
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    private static final String REST_API_ENDPOINT = "/rest/api/latest/";
    private static final String ISSUE = "issue/";
    private static final String ISSUE_ENDPOINT = REST_API_ENDPOINT + ISSUE;

    private final JiraClient jiraClient;

    public JiraFacade(JiraClient jiraClient)
    {
        this.jiraClient = jiraClient;
    }

    public String createIssue(String issueBody) throws IOException
    {
        return jiraClient.executePost(ISSUE_ENDPOINT, issueBody);
    }

    public String updateIssue(String issueKey, String issueBody) throws IOException
    {
        return jiraClient.executePut(ISSUE_ENDPOINT + issueKey, issueBody);
    }

    public void createIssueLink(String inwardIssueKey, String outwardIssueKey, String type) throws IOException
    {
        IssueLink issueLink = new IssueLink(type, inwardIssueKey, outwardIssueKey);
        String createLinkRequest = OBJECT_MAPPER.writeValueAsString(issueLink);
        jiraClient.executePost("/rest/api/latest/issueLink", createLinkRequest);
    }

    public Project getProject(String projectKey) throws IOException
    {
        return getJiraEntity("project/", projectKey, Project.class);
    }

    public JiraIssue getIssue(String issueKey) throws IOException
    {
        return getJiraEntity(ISSUE, issueKey, JiraIssue.class);
    }

    private <T> T getJiraEntity(String relativeUrl, String entityKey, Class<T> entityType) throws IOException
    {
        String responseBody = jiraClient.executeGet(REST_API_ENDPOINT + relativeUrl + entityKey);
        return OBJECT_MAPPER.readValue(responseBody, entityType);
    }
}
