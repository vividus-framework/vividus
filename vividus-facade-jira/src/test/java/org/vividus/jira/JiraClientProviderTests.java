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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.security.GeneralSecurityException;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.http.client.HttpClientConfig;
import org.vividus.http.client.IHttpClient;
import org.vividus.http.client.IHttpClientFactory;
import org.vividus.jira.model.JiraConfiguration;

@ExtendWith(MockitoExtension.class)
class JiraClientProviderTests
{
    @Mock private IHttpClientFactory httpClientFactory;
    @Mock private IHttpClient httpClient;
    @Mock private JiraConfigurationProvider jiraConfigurationProvider;
    @InjectMocks private JiraClientProvider jiraClientProvider;

    @Test
    void shouldGetClientByJiraConfigurationKey() throws JiraConfigurationException, GeneralSecurityException
    {
        HttpClientConfig config = mock(HttpClientConfig.class);
        JiraConfiguration configuration = new JiraConfiguration();
        configuration.setHttpClientConfig(config);
        Optional<String> jiraConfigKey = Optional.ofNullable("config-key");
        when(jiraConfigurationProvider.getByJiraConfigurationKey(jiraConfigKey)).thenReturn(configuration);

        assertNotNull(jiraClientProvider.getByJiraConfigurationKey(jiraConfigKey));
        verify(httpClientFactory).buildHttpClient(config);
    }

    @Test
    void shouldGetClientByIssueKey() throws JiraConfigurationException, GeneralSecurityException
    {
        HttpClientConfig config = mock(HttpClientConfig.class);
        JiraConfiguration configuration = new JiraConfiguration();
        configuration.setHttpClientConfig(config);
        String issueKey = "VVDS-1408";
        when(jiraConfigurationProvider.getByIssueKey(issueKey)).thenReturn(configuration);

        assertNotNull(jiraClientProvider.getByIssueKey(issueKey));
        verify(httpClientFactory).buildHttpClient(config);
    }

    @Test
    void shouldGetClientByProjectKey() throws JiraConfigurationException, GeneralSecurityException
    {
        HttpClientConfig config = mock(HttpClientConfig.class);
        JiraConfiguration configuration = new JiraConfiguration();
        configuration.setHttpClientConfig(config);
        String projectKey = "VVDS";
        when(jiraConfigurationProvider.getByProjectKey(projectKey)).thenReturn(configuration);

        assertNotNull(jiraClientProvider.getByProjectKey(projectKey));

        verify(httpClientFactory).buildHttpClient(config);
    }

    @Test
    void shouldWrapExceptionOccurenInCacheWhileFindingByJiraConfigurationKey()
            throws GeneralSecurityException, JiraConfigurationException
    {
        HttpClientConfig config = mock(HttpClientConfig.class);
        JiraConfiguration configuration = new JiraConfiguration();
        configuration.setHttpClientConfig(config);
        when(jiraConfigurationProvider.getByJiraConfigurationKey(Optional.empty())).thenReturn(configuration);
        GeneralSecurityException exception = mock(GeneralSecurityException.class);
        doThrow(exception).when(httpClientFactory).buildHttpClient(config);

        JiraConfigurationException thrown = assertThrows(JiraConfigurationException.class,
            () -> jiraClientProvider.getByJiraConfigurationKey(Optional.empty()));

        assertEquals(exception, thrown.getCause());
    }
}
