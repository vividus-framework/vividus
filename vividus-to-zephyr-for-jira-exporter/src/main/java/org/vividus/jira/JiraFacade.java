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

import org.vividus.jira.model.JiraEntity;
import org.vividus.jira.model.Project;

public class JiraFacade
{
    private static final String REST_API_ENDPOINT = "/rest/api/latest/";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final JiraClient client;

    public JiraFacade(JiraClient client)
    {
        this.client = client;
    }

    public JiraEntity getIssue(String issueKey) throws IOException
    {
        return getJiraEntity("issue/", issueKey, JiraEntity.class);
    }

    public Project getProject(String projectKey) throws IOException
    {
        return getJiraEntity("project/", projectKey, Project.class);
    }

    private <T> T getJiraEntity(String relativeUrl, String entityKey, Class<T> entityType) throws IOException
    {
        String responseBody = client.executeGet(REST_API_ENDPOINT + relativeUrl + entityKey);
        return objectMapper.readValue(responseBody, entityType);
    }
}
