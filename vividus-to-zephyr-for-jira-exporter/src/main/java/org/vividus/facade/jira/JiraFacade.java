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

package org.vividus.facade.jira;

import org.vividus.facade.jira.client.IJiraClient;
import org.vividus.util.json.JsonPathUtils;

public class JiraFacade implements IJiraFacade
{
    private static final String GET_ISSUE_ID_ENDPOINT = "/rest/api/latest/issue/";
    private static final String ISSUE_ID_JSON_PATH = "$.id";

    private IJiraClient client;
    private JiraConfiguration jiraConfiguration;

    @Override
    public String getIssueId(String issueKey)
    {
        String responseBody = client.executeGet(jiraConfiguration, GET_ISSUE_ID_ENDPOINT + issueKey);
        return JsonPathUtils.getData(responseBody, ISSUE_ID_JSON_PATH);
    }

    public void setClient(IJiraClient client)
    {
        this.client = client;
    }

    public void setJiraConfiguration(JiraConfiguration jiraConfiguration)
    {
        this.jiraConfiguration = jiraConfiguration;
    }
}
