/*
 * Copyright 2019 the original author or authors.
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

import java.io.IOException;

import org.vividus.facade.jira.client.IJiraClient;
import org.vividus.facade.jira.model.Issue;

public class JiraFacade implements IJiraFacade
{
    private IJiraClient client;
    private IJiraConfigurationProvider jiraConfigurationProvider;

    @Override
    public Issue createIssue(String projectKey, String issue) throws IOException
    {
        return client.createIssue(jiraConfigurationProvider.getConfigurationByProjectKey(projectKey), issue,
                Issue.class);
    }

    public void setClient(IJiraClient client)
    {
        this.client = client;
    }

    public void setJiraConfigurationProvider(IJiraConfigurationProvider jiraConfigurationProvider)
    {
        this.jiraConfigurationProvider = jiraConfigurationProvider;
    }
}
