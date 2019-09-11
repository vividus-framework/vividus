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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.facade.jira.model.JiraConfiguration;

@ExtendWith(MockitoExtension.class)
class JiraConfigurationProviderTests
{
    private static final String PROJECT_KEY = "HSM";
    private static final String ISSUE_KEY = "HSM-1000";

    @InjectMocks
    private JiraConfigurationProvider jiraConfigurationProvider;

    @Test
    void testGetConfigurationByIssueKey()
    {
        JiraConfiguration jiraConfiguration = setJiraConfigurations();
        assertEquals(jiraConfiguration, jiraConfigurationProvider.getConfigurationByIssueKey(ISSUE_KEY).get());
    }

    @Test
    void testGetConfigurationByProjectKey()
    {
        JiraConfiguration jiraConfiguration = setJiraConfigurations();
        assertEquals(jiraConfiguration, jiraConfigurationProvider.getConfigurationByProjectKey(PROJECT_KEY));
    }

    @Test
    void testGetConfigurationByIssueKeyWhenJiraConfigurationIsNotFound()
    {
        jiraConfigurationProvider.setJiraConfigurationMap(Map.of());
        jiraConfigurationProvider.getConfigurationByIssueKey(ISSUE_KEY);
    }

    @Test
    void testGetConfigurationByProjectKeyWhenJiraConfigurationIsNotFound()
    {
        jiraConfigurationProvider.setJiraConfigurationMap(Map.of());
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> jiraConfigurationProvider.getConfigurationByProjectKey(PROJECT_KEY));
        assertEquals("No Jira configuration for project with key " + PROJECT_KEY, exception.getMessage());
    }

    @Test
    void testGetConfigurationByIssueKeyWhenConfigurationIsEmpty()
    {
        Map<String, JiraConfiguration> jiraConfigurationMap = Map.of("JiraConfiguration", new JiraConfiguration());
        jiraConfigurationProvider.setJiraConfigurationMap(jiraConfigurationMap);
        assertEquals(Optional.empty(), jiraConfigurationProvider.getConfigurationByIssueKey(ISSUE_KEY));
    }

    private JiraConfiguration setJiraConfigurations()
    {
        JiraConfiguration jiraConfigurationFirst = new JiraConfiguration();
        jiraConfigurationFirst.setIssueKeyPattern(PROJECT_KEY + "-.*");
        JiraConfiguration jiraConfigurationSecond = new JiraConfiguration();
        jiraConfigurationSecond.setIssueKeyPattern("ABC-.*");
        Map<String, JiraConfiguration> jiraConfigurationMap = Map.of("JiraConfiguration-" + PROJECT_KEY,
                jiraConfigurationFirst, "JiraConfiguration-ABC", jiraConfigurationSecond);
        jiraConfigurationProvider.setJiraConfigurationMap(jiraConfigurationMap);
        return jiraConfigurationFirst;
    }
}
