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

import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import org.vividus.http.client.HttpClientConfig;
import org.vividus.http.client.IHttpClient;
import org.vividus.http.client.IHttpClientFactory;
import org.vividus.jira.model.JiraConfiguration;
import org.vividus.util.property.PropertyMappedCollection;

public class JiraClientProvider
{
    private final PropertyMappedCollection<JiraConfiguration> jiraConfigurations;
    private final IHttpClientFactory httpClientFactory;
    private final HttpClientConfig defaultHttpClientConfig;

    private final LoadingCache<JiraConfiguration, JiraClient> jiraClients = CacheBuilder.newBuilder()
            .build(new CacheLoader<>()
            {
                @Override
                public JiraClient load(JiraConfiguration configuration) throws Exception
                {
                    HttpClientConfig httpConfig = Optional.ofNullable(configuration.getHttp())
                            .orElse(defaultHttpClientConfig);
                    IHttpClient httpClient = httpClientFactory.buildHttpClient(httpConfig);
                    return new JiraClient(configuration.getEndpoint(), configuration.getUsername(),
                            configuration.getPassword(), httpClient);
                }
            });

    public JiraClientProvider(PropertyMappedCollection<JiraConfiguration> jiraConfigurations,
            IHttpClientFactory httpClientFactory, HttpClientConfig defaultHttpClientConfig)
    {
        this.jiraConfigurations = jiraConfigurations;
        this.httpClientFactory = httpClientFactory;
        this.defaultHttpClientConfig = defaultHttpClientConfig;
    }

    @SuppressWarnings("AvoidHidingCauseException")
    public JiraClient get(String issueKey, Optional<String> jiraConfigurationKey) throws JiraConfigurationException
    {
        try
        {
            if (jiraConfigurationKey.isPresent())
            {
                String key = jiraConfigurationKey.get();
                Optional<JiraConfiguration> jiraConfiguration = jiraConfigurations.getNullable(key);
                if (jiraConfiguration.isPresent())
                {
                    return jiraClients.get(jiraConfiguration.get());
                }
                throw error("Unable to find JIRA configuration with '%s' key", key);
            }

            if (issueKey == null)
            {
                return getDefaultOrElseThrow(() -> error("Issue key is not specified"));
            }

            Set<Entry<String, JiraConfiguration>> configurations = jiraConfigurations.getData().entrySet().stream()
                    .filter(cnf -> cnf.getValue().getProjectCodePattern().matcher(issueKey).matches())
                    .collect(Collectors.toSet());

            if (configurations.size() == 1)
            {
                return getByFirst(configurations);
            }
            else if (configurations.size() > 1)
            {
                throw error("More than one JIRA configuration maps to %s code: %s", issueKey,
                        configurations.stream().map(Entry::getKey).collect(Collectors.toList()));
            }

            return getDefaultOrElseThrow(
                () -> error("Unable to find configuration for JIRA project code %s", issueKey));
        }
        catch (ExecutionException thrown)
        {
            throw new JiraConfigurationException(thrown.getCause());
        }
    }

    private JiraClient getDefaultOrElseThrow(Supplier<JiraConfigurationException> exceptionSupplier)
            throws ExecutionException, JiraConfigurationException
    {
        Set<Entry<String, JiraConfiguration>> configurations = jiraConfigurations.getData().entrySet();
        if (configurations.size() == 1)
        {
            return getByFirst(configurations);
        }

        throw exceptionSupplier.get();
    }

    private JiraClient getByFirst(Set<Entry<String, JiraConfiguration>> configurations) throws ExecutionException
    {
        return jiraClients.get(configurations.iterator().next().getValue());
    }

    private JiraConfigurationException error(String message, Object... args)
    {
        return new JiraConfigurationException(String.format(message, args));
    }

    public static class JiraConfigurationException extends Exception
    {
        private static final long serialVersionUID = -7392541975134809633L;

        public JiraConfigurationException(String message)
        {
            super(message);
        }

        public JiraConfigurationException(Throwable cause)
        {
            super(cause);
        }
    }
}
