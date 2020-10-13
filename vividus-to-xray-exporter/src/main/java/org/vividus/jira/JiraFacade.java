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

package org.vividus.jira;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import org.vividus.jira.databind.IssueLinkSerializer;
import org.vividus.jira.model.IssueLink;
import org.vividus.util.json.JsonPathUtils;

public class JiraFacade
{
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new SimpleModule().addSerializer(IssueLink.class, new IssueLinkSerializer()));

    private static final String ISSUE_ENDPOINT = "/rest/api/latest/issue/";

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

    public String getIssueStatus(String issueKey) throws IOException
    {
        String issue = jiraClient.executeGet(ISSUE_ENDPOINT + issueKey);
        return JsonPathUtils.getData(issue, "$.fields.status.name");
    }
}
