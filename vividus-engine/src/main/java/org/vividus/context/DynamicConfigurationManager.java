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

package org.vividus.context;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.vividus.testcontext.TestContext;
import org.vividus.util.property.PropertyMappedCollection;
import org.vividus.util.property.PropertyMapper;

public class DynamicConfigurationManager<T>
{
    private final String configurationDefinition;
    private final PropertyMappedCollection<T> staticConfigurations;
    private final TestContext testContext;
    private final Object key;

    public DynamicConfigurationManager(String configurationDefinition, String propertyPrefix,
            Class<T> configurationClass, PropertyMapper propertyMapper, TestContext testContext) throws IOException
    {
        this.configurationDefinition = configurationDefinition;
        this.staticConfigurations = propertyMapper.readValues(propertyPrefix, configurationClass);
        this.testContext = testContext;
        this.key = ConfigurationData.class.getCanonicalName() + "<" + configurationClass.getCanonicalName() + ">";
    }

    /**
     * Provides a configuration mapped to the key from properties or at runtime.
     *
     * @param configurationKey The configuration key used for unique identification
     * @return The configuration mapped to the key
     * @throws IllegalArgumentException if no configuration is found by the key
     */
    public T getConfiguration(String configurationKey)
    {
        return getDynamicConfiguration(configurationKey).orElseGet(() -> {
            String messageFormat = "%s with key '%s' is not configured in the current story nor in properties";
            return staticConfigurations.get(configurationKey, messageFormat, configurationDefinition, configurationKey);
        });
    }

    /**
     * Maps the provided configuration to the given key. If the same property-based configuration exist, it will be
     * hidden by the provided configuration until test context is reset.
     *
     * @param configurationKey The key to map new configuration
     * @param configuration New configuration to map
     */
    public void addDynamicConfiguration(String configurationKey, T configuration)
    {
        getData().dynamicConfigurations.put(configurationKey, configuration);
    }

    private Optional<T> getDynamicConfiguration(String configurationKey)
    {
        return Optional.ofNullable(getData().dynamicConfigurations.get(configurationKey));
    }

    private ConfigurationData<T> getData()
    {
        return testContext.get(key, ConfigurationData::new);
    }

    private static final class ConfigurationData<T>
    {
        private final Map<String, T> dynamicConfigurations = new HashMap<>();
    }
}
