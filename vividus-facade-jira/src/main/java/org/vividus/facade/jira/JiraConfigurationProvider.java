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

import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.facade.jira.model.JiraConfiguration;

public class JiraConfigurationProvider implements IJiraConfigurationProvider
{
    private static final Logger LOGGER = LoggerFactory.getLogger(JiraConfigurationProvider.class);
    private Map<String, JiraConfiguration> jiraConfigurationMap;

    @Override
    public Optional<JiraConfiguration> getConfigurationByIssueKey(String issueKey)
    {
        for (JiraConfiguration jiraConfiguration : jiraConfigurationMap.values())
        {
            Pattern issueKeyPattern = jiraConfiguration.getIssueKeyPattern();
            if (issueKeyPattern != null && issueKeyPattern.matcher(issueKey).matches())
            {
                return Optional.of(jiraConfiguration);
            }
        }
        LOGGER.warn("No Jira configuration was found for issue with key: {}", issueKey);
        return Optional.empty();
    }

    @Override
    public JiraConfiguration getConfigurationByProjectKey(String projectKey)
    {
        return getConfigurationByIssueKey(projectKey + "-1").orElseThrow(
            () -> new IllegalArgumentException("No Jira configuration for project with key " + projectKey));
    }

    public void setJiraConfigurationMap(Map<String, JiraConfiguration> jiraConfigurationMap)
    {
        this.jiraConfigurationMap = jiraConfigurationMap;
    }
}
