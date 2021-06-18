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

package org.vividus.jira.connector;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.apache.commons.lang3.Validate;
import org.vividus.http.client.IHttpClient;
import org.vividus.http.client.IHttpClientFactory;
import org.vividus.jira.JiraClient;
import org.vividus.jira.JiraFacade;
import org.vividus.util.property.PropertyMappedCollection;

public class JiraFacadeProvider
{
    private static final String DEFAULT = "default";

    private final Map<String, JiraFacade> facades = new ConcurrentHashMap<>();

    private final IHttpClientFactory httpClientFactory;
    private final PropertyMappedCollection<JiraConfiguration> jiraConfigurations;

    public JiraFacadeProvider(PropertyMappedCollection<JiraConfiguration> jiraConfigurations,
            IHttpClientFactory httpClientFactory) throws IOException
    {
        this.httpClientFactory = httpClientFactory;
        this.jiraConfigurations = jiraConfigurations;
    }

    public JiraFacade findByBtsKey(String btsKey)
    {
        return jiraConfigurations.getNullable(btsKey)
                                 .map(cfg -> createJiraFacade(btsKey, cfg))
                                 .orElseThrow(() -> new IllegalStateException(
                                         String.format("Unable to find configuration for BTS with the key '%s'",
                                                 btsKey)));
    }

    public JiraFacade findByJiraCode(String jiraCode)
    {
        List<Entry<String, JiraConfiguration>> configurations = jiraConfigurations.getData().entrySet().stream()
                .filter(cnf -> cnf.getValue().getCode().matcher(jiraCode).matches()).collect(Collectors.toList());

        if (configurations.size() == 1)
        {
            Entry<String, JiraConfiguration> configuration = configurations.get(0);
            return createJiraFacade(configuration.getKey(), configuration.getValue());
        }

        Validate.isTrue(configurations.size() <= 1, "More than one configuratio maps to %s code: %s", jiraCode,
                configurations.stream().map(Entry::getKey).collect(Collectors.toList()));

        Optional<JiraConfiguration> defaultConfiguration = jiraConfigurations.getNullable(DEFAULT);
        if (configurations.size() == 0 && defaultConfiguration.isPresent())
        {
            return createJiraFacade(DEFAULT, defaultConfiguration.get());
        }

        throw new IllegalStateException(String.format("Unable to find configuration for JIRA code %s", jiraCode));
    }

    private JiraFacade createJiraFacade(String key, JiraConfiguration jiraConfiguration)
    {
        return facades.computeIfAbsent(key, k ->
        {
            try
            {
                IHttpClient httpClient = httpClientFactory.buildHttpClient(jiraConfiguration.getHttpConfig());
                JiraClient jiraClient = new JiraClient(jiraConfiguration.getEndpoint(), jiraConfiguration.getUsername(),
                        jiraConfiguration.getPassword(), httpClient);
                return new JiraFacade(jiraClient);
            }
            catch (GeneralSecurityException e)
            {
                throw new IllegalStateException(e);
            }
        });
    }
}
