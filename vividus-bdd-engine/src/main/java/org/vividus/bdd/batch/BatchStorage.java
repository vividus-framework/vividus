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

package org.vividus.bdd.batch;

import java.io.IOException;
import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.vividus.util.property.IPropertyMapper;

public class BatchStorage
{
    private static final String BATCH = "batch-";

    private final Map<String, BatchResourceConfiguration> batchResourceConfigurations;
    private final Map<String, BatchExecutionConfiguration> batchExecutionConfigurations;

    private final Duration defaultStoryExecutionTimeout;
    private final List<String> defaultMetaFilters;
    private final boolean failFast;

    public BatchStorage(IPropertyMapper propertyMapper, String defaultStoryExecutionTimeout,
            List<String> defaultMetaFilters, boolean failFast) throws IOException
    {
        this.defaultMetaFilters = defaultMetaFilters;
        this.defaultStoryExecutionTimeout = Duration.ofSeconds(Long.parseLong(defaultStoryExecutionTimeout));
        this.failFast = failFast;

        batchResourceConfigurations = readFromProperties(propertyMapper, "bdd.story-loader.batch-",
                BatchResourceConfiguration.class);
        batchResourceConfigurations.forEach((batchKey, resourceConfiguration) -> Validate.isTrue(
                resourceConfiguration.getResourceLocation() != null, "'resource-location' is missing for %s",
                batchKey));

        batchExecutionConfigurations = readFromProperties(propertyMapper, "bdd.batch-",
                BatchExecutionConfiguration.class);
        batchExecutionConfigurations.forEach((key, config) -> {
            if (config.getName() == null)
            {
                config.setName(key);
            }
            if (config.getMetaFilters() == null)
            {
                config.setMetaFilters(this.defaultMetaFilters);
            }
            if (config.getStoryExecutionTimeout() == null)
            {
                config.setStoryExecutionTimeout(this.defaultStoryExecutionTimeout);
            }
            if (config.isFailFast() == null)
            {
                config.setFailFast(failFast);
            }
        });
    }

    private <T> Map<String, T> readFromProperties(IPropertyMapper propertyMapper, String propertyPrefix,
            Class<T> valueType) throws IOException
    {
        return propertyMapper.readValues(propertyPrefix, BATCH::concat, getBatchKeyComparator(), valueType).getData();
    }

    private Comparator<String> getBatchKeyComparator()
    {
        return Comparator.comparingInt(batchKey -> Integer.parseInt(StringUtils.removeStart(batchKey, BATCH)));
    }

    public BatchResourceConfiguration getBatchResourceConfiguration(String batchKey)
    {
        return getBatchResourceConfigurations().get(batchKey);
    }

    public Map<String, BatchResourceConfiguration> getBatchResourceConfigurations()
    {
        return batchResourceConfigurations;
    }

    public BatchExecutionConfiguration getBatchExecutionConfiguration(String batchKey)
    {
        return batchExecutionConfigurations.computeIfAbsent(batchKey, b -> {
            BatchExecutionConfiguration config = new BatchExecutionConfiguration();
            config.setName(batchKey);
            config.setStoryExecutionTimeout(defaultStoryExecutionTimeout);
            config.setMetaFilters(defaultMetaFilters);
            config.setFailFast(failFast);
            return config;
        });
    }
}
