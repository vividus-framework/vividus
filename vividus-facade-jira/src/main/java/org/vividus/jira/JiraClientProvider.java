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

import static org.apache.commons.lang3.Validate.isTrue;

import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.UncheckedExecutionException;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.vividus.http.client.HttpClientConfig;
import org.vividus.http.client.IHttpClient;
import org.vividus.http.client.IHttpClientFactory;
import org.vividus.http.handler.HttpResponseHandler;
import org.vividus.http.handler.StatusCodeValidatingHandler;
import org.vividus.jira.model.JiraConfiguration;
import org.vividus.util.property.PropertyMappedCollection;

public class JiraClientProvider
{
    private final PropertyMappedCollection<JiraConfiguration> jiraConfigurations;
    private final IHttpClientFactory httpClientFactory;

    private final LoadingCache<JiraConfiguration, JiraClient> jiraClients = CacheBuilder.newBuilder()
            .build(new CacheLoader<>()
            {
                @Override
                public JiraClient load(JiraConfiguration configuration) throws Exception
                {
                    HttpClientConfig clientConfig = configuration.getHttpClientConfig();
                    @SuppressWarnings("MagicNumber")
                    HttpResponseHandler statusCodeHandler = new StatusCodeValidatingHandler(HttpStatus.SC_OK, 299,
                            "JIRA");
                    clientConfig.setHttpResponseHandlers(List.of(statusCodeHandler));
                    IHttpClient httpClient = httpClientFactory.buildHttpClient(clientConfig);

                    return new JiraClient(configuration.getEndpoint(), httpClient);
                }
            });

    public JiraClientProvider(PropertyMappedCollection<JiraConfiguration> jiraConfigurations,
            IHttpClientFactory httpClientFactory)
    {
        this.jiraConfigurations = jiraConfigurations;
        this.httpClientFactory = httpClientFactory;
    }

    /**
     * Get the {@link JiraClient} by the JIRA configuration key, if the key is not specified the default
     * configuration will be used if exists.
     *
     * @param jiraConfigurationKey the JIRA configuration key to find the {@link JiraClient} by
     * @return the {@link JiraClient} for the specified JIRA configuration key
     * @throws JiraConfigurationException if any error occurred during JIRA configuration resolution
     */
    public JiraClient getByJiraConfigurationKey(Optional<String> jiraConfigurationKey) throws JiraConfigurationException
    {
        if (jiraConfigurationKey.isPresent())
        {
            String key = jiraConfigurationKey.get();
            Optional<JiraConfiguration> jiraConfiguration = jiraConfigurations.getNullable(key);
            if (jiraConfiguration.isPresent())
            {
                return getClient(jiraConfiguration.get());
            }
            throw error("Unable to find JIRA configuration with '%s' key", key);
        }

        return getDefaultOrElseThrow(() -> error("The JIRA configuration key is not specified"));
    }

    /**
     * Get the {@link JiraClient} by the JIRA issue key.
     *
     * @param issueKey the JIRA issue key to find the {@link JiraClient} by, must not be null
     * @return the {@link JiraClient} for the specified JIRA issue key
     * @throws JiraConfigurationException if any error occurred during JIRA configuration resolution
     */
    public JiraClient getByIssueKey(String issueKey) throws JiraConfigurationException
    {
        isTrue(issueKey != null, "The JIRA issue key is not specified");
        return getClientByProjectKey(StringUtils.substringBefore(issueKey, "-"));
    }

    /**
     * Get the {@link JiraClient} by the JIRA project key.
     *
     * @param projectKey the JIRA project key to find the {@link JiraClient} by, must not be null
     * @return the {@link JiraClient} for the specified JIRA project key
     * @throws JiraConfigurationException if any error occurred during JIRA configuration resolution
     */
    public JiraClient getByProjectKey(String projectKey) throws JiraConfigurationException
    {
        isTrue(projectKey != null, "The JIRA project key is not specified");
        return getClientByProjectKey(projectKey);
    }

    private JiraClient getClientByProjectKey(String projectKey) throws JiraConfigurationException
    {
        Set<Entry<String, JiraConfiguration>> configurations = jiraConfigurations.getData().entrySet().stream()
                .filter(cnf -> cnf.getValue().getProjectKeyRegex().matcher(projectKey).matches())
                .collect(Collectors.toSet());

        if (configurations.size() == 1)
        {
            return getByFirst(configurations);
        }
        else if (configurations.size() > 1)
        {
            throw error("More than one JIRA configuration is mapped to %s project key: %s", projectKey,
                    configurations.stream().map(Entry::getKey).collect(Collectors.toList()));
        }

        return getDefaultOrElseThrow(() -> error("Unable to find configuration for JIRA project key %s", projectKey));
    }

    private JiraClient getDefaultOrElseThrow(Supplier<JiraConfigurationException> exceptionSupplier)
            throws JiraConfigurationException
    {
        Set<Entry<String, JiraConfiguration>> configurations = jiraConfigurations.getData().entrySet();
        if (configurations.size() == 1)
        {
            return getByFirst(configurations);
        }

        throw exceptionSupplier.get();
    }

    private JiraClient getByFirst(Set<Entry<String, JiraConfiguration>> configurations)
            throws JiraConfigurationException
    {
        return getClient(configurations.iterator().next().getValue());
    }

    @SuppressWarnings("AvoidHidingCauseException")
    private JiraClient getClient(JiraConfiguration jiraConfiguration) throws JiraConfigurationException
    {
        try
        {
            return jiraClients.get(jiraConfiguration);
        }
        catch (ExecutionException | UncheckedExecutionException thrown)
        {
            throw new JiraConfigurationException(thrown.getCause());
        }
    }

    private JiraConfigurationException error(String message, Object... args)
    {
        return new JiraConfigurationException(String.format(message, args));
    }
}
