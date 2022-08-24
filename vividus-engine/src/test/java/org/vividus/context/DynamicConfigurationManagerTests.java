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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.vividus.testcontext.SimpleTestContext;
import org.vividus.util.property.PropertyMappedCollection;
import org.vividus.util.property.PropertyMapper;

class DynamicConfigurationManagerTests
{
    private static DynamicConfigurationManager<Object> createDynamicConfigurationManager(
            Map<String, Object> staticConfigurations) throws IOException
    {
        var propertyPrefix = "property.prefix.";
        var propertyMapper = mock(PropertyMapper.class);
        var clazz = Object.class;
        when(propertyMapper.readValues(propertyPrefix, clazz)).thenReturn(
                new PropertyMappedCollection<>(staticConfigurations));
        return new DynamicConfigurationManager<>("My configuration", propertyPrefix, clazz, propertyMapper,
                new SimpleTestContext());
    }

    @Test
    void shouldThrowAnErrorIfNothingIsConfigured() throws IOException
    {
        var manager = createDynamicConfigurationManager(Map.of());
        var exception = assertThrows(IllegalArgumentException.class, () -> manager.getConfiguration("any"));
        assertEquals("My configuration with key 'any' is not configured in the current story nor in properties",
                exception.getMessage());
    }

    @Test
    void shouldProvideStaticConfiguration() throws IOException
    {
        var configurationKey = "static";
        var staticConfiguration = new Object();
        var manager = createDynamicConfigurationManager(Map.of(configurationKey, staticConfiguration));
        assertSame(staticConfiguration, manager.getConfiguration(configurationKey));
    }

    @Test
    void shouldProvideDynamicConfigurationEvenIfStaticConfigurationWithSameKeyIsAvailable() throws IOException
    {
        var configurationKey = "common";
        var staticConfiguration = new Object();
        var dynamicConfiguration = new Object();
        var manager = createDynamicConfigurationManager(Map.of(configurationKey, staticConfiguration));
        manager.addDynamicConfiguration(configurationKey, dynamicConfiguration);
        assertSame(dynamicConfiguration, manager.getConfiguration(configurationKey));
    }
}
