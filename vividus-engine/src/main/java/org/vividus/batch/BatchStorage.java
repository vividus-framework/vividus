/*
 * Copyright 2019-2023 the original author or authors.
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

package org.vividus.batch;

import java.io.IOException;
import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.util.property.IPropertyMapper;

public class BatchStorage
{
    private static final Logger LOGGER = LoggerFactory.getLogger(BatchStorage.class);

    private static final String BATCH = "batch-";
    private static final String DEPRECATION_MESSAGE =
            "Property `bdd.story-execution-timeout` is deprecated and will be removed in"
                    + " VIVIDUS 0.7.0. Please use `story.execution-timeout` instead.";
    private static final Duration DEFAULT_STORY_TIMEOUT = Duration.ofHours(3);

    private final Map<String, BatchConfiguration> batchConfigurations;
    private final Duration defaultStoryExecutionTimeout;
    private final List<String> defaultMetaFilters;
    private final boolean failFast;

    public BatchStorage(IPropertyMapper propertyMapper, Duration defaultStoryExecutionTimeout,
        String deprecatedDefaultStoryExecutionTimeout, List<String> defaultMetaFilters,
            boolean failFast) throws IOException
    {
        this.defaultMetaFilters = defaultMetaFilters;
        if (null != deprecatedDefaultStoryExecutionTimeout)
        {
            Validate.isTrue(defaultStoryExecutionTimeout == null,
                "Conflicting properties are found: `bdd.story-execution-timeout` and `story.execution-timeout`. "
                    + DEPRECATION_MESSAGE);
            this.defaultStoryExecutionTimeout =
                Duration.ofSeconds(Long.parseLong(deprecatedDefaultStoryExecutionTimeout));
            LOGGER.warn(DEPRECATION_MESSAGE);
        }
        else
        {
            this.defaultStoryExecutionTimeout = Optional.ofNullable(defaultStoryExecutionTimeout)
                                                        .orElse(DEFAULT_STORY_TIMEOUT);
        }
        this.failFast = failFast;

        batchConfigurations = readFromProperties(propertyMapper, BATCH, BatchConfiguration.class);
        batchConfigurations.forEach((batchKey, batchConfiguration) -> {
            Validate.isTrue(
                    batchConfiguration.getResourceLocation() != null, "'resource-location' is missing for %s",
                    batchKey);
            if (batchConfiguration.getName() == null)
            {
                batchConfiguration.setName(batchKey);
            }
            if (batchConfiguration.getMetaFilters() == null)
            {
                batchConfiguration.setMetaFilters(this.defaultMetaFilters);
            }
            if (batchConfiguration.getStoryExecutionTimeout() == null)
            {
                batchConfiguration.overrideStoryExecutionTimeout(this.defaultStoryExecutionTimeout);
            }
            if (batchConfiguration.isFailFast() == null)
            {
                batchConfiguration.setFailFast(failFast);
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

    public Map<String, BatchConfiguration> getBatchConfigurations()
    {
        return batchConfigurations;
    }

    public BatchConfiguration getBatchConfiguration(String batchKey)
    {
        return batchConfigurations.computeIfAbsent(batchKey, b -> {
            BatchConfiguration config = new BatchConfiguration();
            config.setName(batchKey);
            config.overrideStoryExecutionTimeout(defaultStoryExecutionTimeout);
            config.setMetaFilters(defaultMetaFilters);
            config.setFailFast(failFast);
            return config;
        });
    }
}
