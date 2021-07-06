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
import static org.mockito.Mockito.when;

import java.security.GeneralSecurityException;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.http.client.HttpClientConfig;
import org.vividus.http.client.IHttpClient;
import org.vividus.http.client.IHttpClientFactory;
import org.vividus.jira.JiraClientProvider.JiraConfigurationException;
import org.vividus.jira.model.JiraConfiguration;
import org.vividus.util.property.PropertyMappedCollection;

@ExtendWith(MockitoExtension.class)
class JiraClientProviderTests
{
    private static final String KEY_1 = "key-1";
    private static final String KEY_2 = "key-2";
    private static final String JIRA_CONFIG_KEY = "config-key";
    private static final String ISSUE_KEY = "VVDS-1408";
    private static final Pattern PROJECT_CODE_PATTERN = Pattern.compile(".*VV.*");

    @Mock private IHttpClientFactory httpClientFactory;
    @Mock private IHttpClient httpClient;
    @Mock private HttpClientConfig defaultHttpClientConfig;

    private JiraClientProvider jiraClientProvider;

    @Test
    void shouldThrowExceptionIfUnableToFindConfigurationByKey()
    {
        init(Map.of());

        JiraConfigurationException thrown = assertThrows(JiraConfigurationException.class,
            () -> jiraClientProvider.get(null, Optional.of(JIRA_CONFIG_KEY)));

        assertEquals("Unable to find JIRA configuration with 'config-key' key", thrown.getMessage());
    }

    @Test
    void shouldWrapExceptionOccurenInCache() throws GeneralSecurityException, JiraConfigurationException
    {
        GeneralSecurityException exception = mock(GeneralSecurityException.class);
        doThrow(exception).when(httpClientFactory).buildHttpClient(defaultHttpClientConfig);
        init(Map.of(KEY_1, spyJiraConfiguration(null, null)));

        JiraConfigurationException thrown = assertThrows(JiraConfigurationException.class,
            () -> jiraClientProvider.get(null, Optional.empty()));

        assertEquals(exception, thrown.getCause());
    }

    @Test
    void shouldReturnDefaultConfiguration() throws GeneralSecurityException, JiraConfigurationException
    {
        when(httpClientFactory.buildHttpClient(defaultHttpClientConfig)).thenReturn(httpClient);
        JiraConfiguration configuration = spyJiraConfiguration(null, null);
        init(Map.of(KEY_1, configuration));

        assertNotNull(jiraClientProvider.get(null, Optional.empty()));

        verifyJiraConfiguration(configuration);
    }

    @Test
    void shouldReturnConfigurationByKey() throws GeneralSecurityException, JiraConfigurationException
    {
        HttpClientConfig config = mock(HttpClientConfig.class);
        when(httpClientFactory.buildHttpClient(config)).thenReturn(httpClient);
        JiraConfiguration configuration = spyJiraConfiguration(config, null);
        init(Map.of(JIRA_CONFIG_KEY, configuration));

        assertNotNull(jiraClientProvider.get(null, Optional.of(JIRA_CONFIG_KEY)));

        verifyJiraConfiguration(configuration);
    }

    @Test
    void shouldThrowExceptionIfMoreThanOneConfigurationMatchesProjectCodePattern()
    {
        init(Map.of(
            KEY_1, spyJiraConfiguration(null, PROJECT_CODE_PATTERN),
            KEY_2, spyJiraConfiguration(null, PROJECT_CODE_PATTERN)
        ));

        JiraConfigurationException thrown = assertThrows(JiraConfigurationException.class,
            () -> jiraClientProvider.get(ISSUE_KEY, Optional.empty()));

        assertThat(thrown.getMessage(), matchesRegex("More than one JIRA configuration maps to VVDS-1408 code:"
                + " \\[(key-2, key-1|key-1, key-2)\\]"));
    }

    @Test
    void shouldReturnFirstMatchedConfiguration() throws JiraConfigurationException
    {
        JiraConfiguration configuration = spyJiraConfiguration(null, PROJECT_CODE_PATTERN);
        init(Map.of(
            KEY_1, configuration,
            KEY_2, spyJiraConfiguration(null, Pattern.compile(".*GG.*"))
        ));

        assertNotNull(jiraClientProvider.get(ISSUE_KEY, Optional.empty()));

        verifyJiraConfiguration(configuration);
    }

    @Test
    void shouldReturnDefaultConfigurationIfThereIsNoMatchByIssueKey() throws JiraConfigurationException
    {
        JiraConfiguration configuration = spyJiraConfiguration(null, PROJECT_CODE_PATTERN);
        init(Map.of(KEY_1, configuration));

        assertNotNull(jiraClientProvider.get("KEK-404", Optional.empty()));

        verifyJiraConfiguration(configuration);
    }

    @Test
    void shouldThrowExceptionIfNoConfigurationsWereFoundByIssueKey() throws JiraConfigurationException
    {
        init(Map.of());

        JiraConfigurationException thrown = assertThrows(JiraConfigurationException.class,
            () -> jiraClientProvider.get(ISSUE_KEY, Optional.empty()));

        assertEquals("Unable to find configuration for JIRA project code VVDS-1408", thrown.getMessage());
    }

    @Test
    void shouldThrowExceptionIfNoIdentifiersWereProvidedAndNoDefaultConfig() throws JiraConfigurationException
    {
        init(Map.of());

        JiraConfigurationException thrown = assertThrows(JiraConfigurationException.class,
            () -> jiraClientProvider.get(null, Optional.empty()));

        assertEquals("Issue key is not specified", thrown.getMessage());
    }

    private void init(Map<String, JiraConfiguration> configs)
    {
        this.jiraClientProvider = new JiraClientProvider(new PropertyMappedCollection<>(configs), httpClientFactory,
                defaultHttpClientConfig);
    }

    private static JiraConfiguration spyJiraConfiguration(HttpClientConfig httpClientConfig,
            Pattern projectCodePattern)
    {
        JiraConfiguration configuration = new JiraConfiguration();
        configuration.setUsername("username");
        configuration.setPassword("password");
        configuration.setProjectCodePattern(projectCodePattern);
        configuration.setHttp(httpClientConfig);
        return spy(configuration);
    }

    private static void verifyJiraConfiguration(JiraConfiguration configuration)
    {
        verify(configuration).getUsername();
        verify(configuration).getPassword();
        verify(configuration).getHttp();
    }
}
