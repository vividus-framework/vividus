/*
 * Copyright 2019-2022 the original author or authors.
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.matchesRegex;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.jira.model.JiraConfiguration;
import org.vividus.util.property.PropertyMappedCollection;

@ExtendWith(MockitoExtension.class)
class JiraConfigurationProviderTests
{
    private static final String KEY_1 = "key-1";
    private static final String KEY_2 = "key-2";
    private static final String JIRA_CONFIG_KEY = "config-key";
    private static final String PROJECT_KEY = "VVDS";
    private static final String ISSUE_KEY = PROJECT_KEY + "-1408";
    private static final Pattern PROJECT_CODE_PATTERN = Pattern.compile(".*VV.*");
    private static final Pattern UNMATCHED_PROJECT_CODE_PATTERN = Pattern.compile(".*GG.*");

    private JiraConfigurationProvider jiraConfigurationProvider;

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = JIRA_CONFIG_KEY)
    void shouldGetConfigurationByJiraConfigurationKey(String jiraConfigurationKey) throws JiraConfigurationException
    {
        JiraConfiguration configuration = createJiraConfiguration(null);
        init(Map.of(JIRA_CONFIG_KEY, configuration));

        JiraConfiguration actual = jiraConfigurationProvider
                .getByJiraConfigurationKey(Optional.ofNullable(jiraConfigurationKey));
        assertEquals(configuration, actual);
    }

    @Test
    void shouldThrowExceptionIfUnableToFindConfigurationByJiraConfigurationKey()
    {
        init(Map.of());

        JiraConfigurationException thrown = assertThrows(JiraConfigurationException.class,
            () -> jiraConfigurationProvider.getByJiraConfigurationKey(Optional.of(JIRA_CONFIG_KEY)));

        assertEquals("Unable to find JIRA configuration with 'config-key' key", thrown.getMessage());
    }

    @Test
    void shouldThrowExceptionIfNeitherJiraConfigurationKeyNorDefaultValueExists()
    {
        init(Map.of());

        JiraConfigurationException thrown = assertThrows(JiraConfigurationException.class,
            () -> jiraConfigurationProvider.getByJiraConfigurationKey(Optional.empty()));

        assertEquals("The JIRA configuration key is not specified", thrown.getMessage());
    }

    @Test
    void shouldGetConfigurationByIssueKey() throws JiraConfigurationException
    {
        JiraConfiguration configuration = createJiraConfiguration(PROJECT_CODE_PATTERN);
        init(Map.of(
            KEY_1, configuration,
            KEY_2, createJiraConfiguration(UNMATCHED_PROJECT_CODE_PATTERN)
        ));

        JiraConfiguration actual = jiraConfigurationProvider.getByIssueKey(ISSUE_KEY);
        assertEquals(configuration, actual);
    }

    @Test
    void shouldThrowExceptionIfIssueKeyIsNull()
    {
        init(Map.of());

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
            () -> jiraConfigurationProvider.getByIssueKey(null));

        assertEquals("The JIRA issue key is not specified", thrown.getMessage());
    }

    @Test
    void shouldGetConfigurationByProjectKey() throws JiraConfigurationException
    {
        JiraConfiguration configuration = createJiraConfiguration(PROJECT_CODE_PATTERN);
        init(Map.of(
            KEY_1, configuration,
            KEY_2, createJiraConfiguration(UNMATCHED_PROJECT_CODE_PATTERN)
        ));

        JiraConfiguration actual = jiraConfigurationProvider.getByProjectKey(PROJECT_KEY);
        assertEquals(configuration, actual);
    }

    @Test
    void shouldThrowExceptionIfProjectKeyIsNull()
    {
        init(Map.of());

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
            () -> jiraConfigurationProvider.getByProjectKey(null));

        assertEquals("The JIRA project key is not specified", thrown.getMessage());
    }

    @Test
    void shouldThrowExceptionIfMoreThanOneConfigurationMatchesProjectKeyRegex()
    {
        init(Map.of(
            KEY_1, createJiraConfiguration(PROJECT_CODE_PATTERN),
            KEY_2, createJiraConfiguration(PROJECT_CODE_PATTERN)
        ));

        JiraConfigurationException thrown = assertThrows(JiraConfigurationException.class,
                () -> jiraConfigurationProvider.getByProjectKey(PROJECT_KEY));

        assertThat(thrown.getMessage(), matchesRegex("More than one JIRA configuration is mapped to VVDS project key:"
                + " \\[(key-2, key-1|key-1, key-2)\\]"));
    }

    @Test
    void shouldThrowExceptionIfNoConfigurationsWereFoundByIssueKey() throws JiraConfigurationException
    {
        init(Map.of());

        JiraConfigurationException thrown = assertThrows(JiraConfigurationException.class,
                () -> jiraConfigurationProvider.getByIssueKey(ISSUE_KEY));

        assertEquals("Unable to find configuration for JIRA project key VVDS", thrown.getMessage());
    }

    @Test
    void shouldGetDefaultConfigurationIfNoMatchedByProjectKey() throws JiraConfigurationException
    {
        JiraConfiguration configuration = createJiraConfiguration(PROJECT_CODE_PATTERN);
        init(Map.of(JIRA_CONFIG_KEY, configuration));

        JiraConfiguration actual = jiraConfigurationProvider.getByProjectKey(KEY_1);
        assertEquals(configuration, actual);
    }

    @Test
    void shouldGetFieldsMappingByProjectKey() throws JiraConfigurationException
    {
        JiraConfiguration configuration = createJiraConfiguration(PROJECT_CODE_PATTERN);
        Map<String, String> fieldsMapping = Map.of("field-key", "field-mapping");
        configuration.setFieldsMapping(fieldsMapping);
        init(Map.of(JIRA_CONFIG_KEY, configuration));

        assertEquals(fieldsMapping, jiraConfigurationProvider.getFieldsMappingByProjectKey(KEY_1));
    }

    private void init(Map<String, JiraConfiguration> configs)
    {
        this.jiraConfigurationProvider = new JiraConfigurationProvider(new PropertyMappedCollection<>(configs));
    }

    private static JiraConfiguration createJiraConfiguration(Pattern issueKeyRegex)
    {
        JiraConfiguration configuration = new JiraConfiguration();
        configuration.setProjectKeyRegex(issueKeyRegex);
        return configuration;
    }
}
