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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.matchesRegex;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.security.GeneralSecurityException;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.http.client.HttpClientConfig;
import org.vividus.http.client.IHttpClient;
import org.vividus.http.client.IHttpClientFactory;
import org.vividus.jira.model.JiraConfiguration;
import org.vividus.util.property.PropertyMappedCollection;

@ExtendWith(MockitoExtension.class)
class JiraClientProviderTests
{
    private static final String KEY_1 = "key-1";
    private static final String KEY_2 = "key-2";
    private static final String JIRA_CONFIG_KEY = "config-key";
    private static final String PROJECT_KEY = "VVDS";
    private static final String ISSUE_KEY = PROJECT_KEY + "-1408";
    private static final Pattern PROJECT_CODE_PATTERN = Pattern.compile(".*VV.*");
    private static final Pattern UNMATCHED_PROJECT_CODE_PATTERN = Pattern.compile(".*GG.*");

    @Mock private IHttpClientFactory httpClientFactory;
    @Mock private IHttpClient httpClient;

    private JiraClientProvider jiraClientProvider;

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = JIRA_CONFIG_KEY)
    void shouldGetClientByJiraConfigurationKey(String jiraConfigurationKey)
            throws JiraConfigurationException, GeneralSecurityException
    {
        HttpClientConfig config = mock(HttpClientConfig.class);
        JiraConfiguration configuration = spyJiraConfiguration(config, null);
        init(Map.of(JIRA_CONFIG_KEY, configuration));

        assertNotNull(jiraClientProvider.getByJiraConfigurationKey(Optional.ofNullable(jiraConfigurationKey)));
        verify(httpClientFactory).buildHttpClient(config);
    }

    @Test
    void shouldThrowExceptionIfUnableToFindClientByJiraConfigurationKey()
    {
        init(Map.of());

        JiraConfigurationException thrown = assertThrows(JiraConfigurationException.class,
            () -> jiraClientProvider.getByJiraConfigurationKey(Optional.of(JIRA_CONFIG_KEY)));

        assertEquals("Unable to find JIRA configuration with 'config-key' key", thrown.getMessage());
        verifyNoInteractions(httpClientFactory);
    }

    @Test
    void shouldThrowExceptionIfNeitherJiraConfigurationKeyNorDefaultValueExists()
    {
        init(Map.of());

        JiraConfigurationException thrown = assertThrows(JiraConfigurationException.class,
            () -> jiraClientProvider.getByJiraConfigurationKey(Optional.empty()));

        assertEquals("The JIRA configuration key is not specified", thrown.getMessage());
        verifyNoInteractions(httpClientFactory);
    }

    @Test
    void shouldWrapExceptionOccurenInCacheWhileFindingByJiraConfigurationKey()
            throws GeneralSecurityException, JiraConfigurationException
    {
        HttpClientConfig config = mock(HttpClientConfig.class);
        GeneralSecurityException exception = mock(GeneralSecurityException.class);
        doThrow(exception).when(httpClientFactory).buildHttpClient(config);
        init(Map.of(KEY_1, spyJiraConfiguration(config, null)));

        JiraConfigurationException thrown = assertThrows(JiraConfigurationException.class,
            () -> jiraClientProvider.getByJiraConfigurationKey(Optional.empty()));

        assertEquals(exception, thrown.getCause());
    }

    @Test
    void shouldGetClientByIssueKey() throws JiraConfigurationException, GeneralSecurityException
    {
        HttpClientConfig config = mock(HttpClientConfig.class);
        JiraConfiguration configuration = spyJiraConfiguration(config, PROJECT_CODE_PATTERN);
        init(Map.of(
            KEY_1, configuration,
            KEY_2, spyJiraConfiguration(null, UNMATCHED_PROJECT_CODE_PATTERN)
        ));

        assertNotNull(jiraClientProvider.getByIssueKey(ISSUE_KEY));
        verify(httpClientFactory).buildHttpClient(config);
    }

    @Test
    void shouldThrowExceptionIfIssueKeyIsNull()
    {
        init(Map.of());

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
            () -> jiraClientProvider.getByIssueKey(null));

        assertEquals("The JIRA issue key is not specified", thrown.getMessage());
    }

    @Test
    void shouldGetClientByProjectKey() throws JiraConfigurationException, GeneralSecurityException
    {
        HttpClientConfig config = mock(HttpClientConfig.class);
        JiraConfiguration configuration = spyJiraConfiguration(config, PROJECT_CODE_PATTERN);
        init(Map.of(
            KEY_1, configuration,
            KEY_2, spyJiraConfiguration(null, UNMATCHED_PROJECT_CODE_PATTERN)
        ));

        assertNotNull(jiraClientProvider.getByProjectKey(PROJECT_KEY));

        verify(httpClientFactory).buildHttpClient(config);
    }

    @Test
    void shouldThrowExceptionIfProjectKeyIsNull()
    {
        init(Map.of());

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
            () -> jiraClientProvider.getByProjectKey(null));

        assertEquals("The JIRA project key is not specified", thrown.getMessage());
    }

    @Test
    void shouldThrowExceptionIfMoreThanOneConfigurationMatchesProjectKeyRegex()
    {
        init(Map.of(
            KEY_1, spyJiraConfiguration(null, PROJECT_CODE_PATTERN),
            KEY_2, spyJiraConfiguration(null, PROJECT_CODE_PATTERN)
        ));

        JiraConfigurationException thrown = assertThrows(JiraConfigurationException.class,
                () -> jiraClientProvider.getByProjectKey(PROJECT_KEY));

        assertThat(thrown.getMessage(), matchesRegex("More than one JIRA configuration is mapped to VVDS project key:"
                + " \\[(key-2, key-1|key-1, key-2)\\]"));
    }

    @Test
    void shouldThrowExceptionIfNoConfigurationsWereFoundByIssueKey() throws JiraConfigurationException
    {
        init(Map.of());

        JiraConfigurationException thrown = assertThrows(JiraConfigurationException.class,
                () -> jiraClientProvider.getByIssueKey(ISSUE_KEY));

        assertEquals("Unable to find configuration for JIRA project key VVDS", thrown.getMessage());
    }

    @Test
    void shouldGetDefaultClientIfNoMatchedByProjectKey() throws GeneralSecurityException, JiraConfigurationException
    {
        HttpClientConfig config = mock(HttpClientConfig.class);
        JiraConfiguration configuration = spyJiraConfiguration(config, PROJECT_CODE_PATTERN);
        init(Map.of(JIRA_CONFIG_KEY, configuration));

        assertNotNull(jiraClientProvider.getByProjectKey(KEY_1));

        verify(httpClientFactory).buildHttpClient(config);
    }

    @Test
    void shouldWrapExceptionOccurenInCacheWhileFindingByProjectKey()
            throws GeneralSecurityException, JiraConfigurationException
    {
        HttpClientConfig config = mock(HttpClientConfig.class);
        GeneralSecurityException exception = mock(GeneralSecurityException.class);
        doThrow(exception).when(httpClientFactory).buildHttpClient(config);
        init(Map.of(KEY_1, spyJiraConfiguration(config, PROJECT_CODE_PATTERN)));

        JiraConfigurationException thrown = assertThrows(JiraConfigurationException.class,
            () -> jiraClientProvider.getByProjectKey(PROJECT_KEY));

        assertEquals(exception, thrown.getCause());
    }

    private void init(Map<String, JiraConfiguration> configs)
    {
        this.jiraClientProvider = new JiraClientProvider(new PropertyMappedCollection<>(configs), httpClientFactory);
    }

    private static JiraConfiguration spyJiraConfiguration(HttpClientConfig httpClientConfig,
            Pattern issueKeyRegex)
    {
        JiraConfiguration configuration = new JiraConfiguration();
        configuration.setProjectKeyRegex(issueKeyRegex);
        configuration.setHttpClientConfig(httpClientConfig);
        return spy(configuration);
    }
}
