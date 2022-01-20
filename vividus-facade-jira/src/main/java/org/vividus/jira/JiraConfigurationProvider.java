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

import static org.apache.commons.lang3.Validate.isTrue;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.vividus.jira.model.JiraConfiguration;
import org.vividus.util.property.PropertyMappedCollection;

public final class JiraConfigurationProvider
{
    private final PropertyMappedCollection<JiraConfiguration> jiraConfigurations;

    public JiraConfigurationProvider(PropertyMappedCollection<JiraConfiguration> jiraConfigurations)
    {
        this.jiraConfigurations = jiraConfigurations;
    }

    JiraConfiguration getByJiraConfigurationKey(Optional<String> jiraConfigurationKey) throws JiraConfigurationException
    {
        if (jiraConfigurationKey.isPresent())
        {
            String key = jiraConfigurationKey.get();
            Optional<JiraConfiguration> jiraConfiguration = jiraConfigurations.getNullable(key);
            if (jiraConfiguration.isPresent())
            {
                return jiraConfiguration.get();
            }
            throw error("Unable to find JIRA configuration with '%s' key", key);
        }

        return getDefaultOrElseThrow(() -> error("The JIRA configuration key is not specified"));
    }

    JiraConfiguration getByIssueKey(String issueKey) throws JiraConfigurationException
    {
        isTrue(issueKey != null, "The JIRA issue key is not specified");
        String projectKey = StringUtils.substringBefore(issueKey, "-");
        return getConfigurationByProjectKey(projectKey);
    }

    JiraConfiguration getByProjectKey(String projectKey) throws JiraConfigurationException
    {
        isTrue(projectKey != null, "The JIRA project key is not specified");
        return getConfigurationByProjectKey(projectKey);
    }

    public Map<String, String> getFieldsMappingByProjectKey(String projectKey) throws JiraConfigurationException
    {
        JiraConfiguration configuration = getByProjectKey(projectKey);
        return Optional.ofNullable(configuration.getFieldsMapping()).orElseGet(Map::of);
    }

    private JiraConfiguration getConfigurationByProjectKey(String projectKey) throws JiraConfigurationException
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

    private JiraConfiguration getDefaultOrElseThrow(Supplier<JiraConfigurationException> exceptionSupplier)
            throws JiraConfigurationException
    {
        Set<Entry<String, JiraConfiguration>> configurations = jiraConfigurations.getData().entrySet();
        if (configurations.size() == 1)
        {
            return getByFirst(configurations);
        }

        throw exceptionSupplier.get();
    }

    private JiraConfiguration getByFirst(Set<Entry<String, JiraConfiguration>> configurations)
    {
        return configurations.iterator().next().getValue();
    }

    private JiraConfigurationException error(String message, Object... args)
    {
        return new JiraConfigurationException(String.format(message, args));
    }
}
