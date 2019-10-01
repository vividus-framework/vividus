/*
 * Copyright 2019 the original author or authors.
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

package org.vividus.reporter.environment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.util.property.IPropertyMapper;
import org.vividus.util.property.IPropertyParser;

@ExtendWith(MockitoExtension.class)
class EnvironmentConfigurerTests
{
    private static final String PREFIX = "environment-configurer.";
    private static final String DYNAMIC = "dynamic";
    private static final String DYNAMIC_PROPERTY_PREFIX = PREFIX + DYNAMIC + ".";
    private static final String VALUE = "value";

    @Mock
    private IPropertyMapper propertyMapper;

    @Mock
    private IPropertyParser propertyParser;

    @InjectMocks
    private EnvironmentConfigurer environmentConfigurer;

    @BeforeEach
    void beforeEach()
    {
        EnvironmentConfigurer.ENVIRONMENT_CONFIGURATION.values().forEach(Map::clear);
    }

    @AfterEach
    void afterEach()
    {
        EnvironmentConfigurer.ENVIRONMENT_CONFIGURATION.values().forEach(Map::clear);
    }

    @Test
    void testInit() throws Exception
    {
        PropertyCategory category = PropertyCategory.VIVIDUS;
        doReturn(Map.of()).when(propertyMapper).readValues(DYNAMIC_PROPERTY_PREFIX,
                DynamicEnvironmentConfigurationProperty.class);
        doReturn(Map.of(
                PropertyCategory.VIVIDUS.toString(), Map.of("some-key", VALUE),
                DYNAMIC, Map.of(DYNAMIC, VALUE)
        )).when(propertyMapper).readValues(PREFIX, Map.class);
        environmentConfigurer.init();
        Map<String, String> vividusProps = EnvironmentConfigurer.ENVIRONMENT_CONFIGURATION.get(category);
        assertEquals(Map.of("Some Key", VALUE), vividusProps);
    }

    @Test
    void testInitDynamicNoRegex() throws Exception
    {
        String descriptionPattern = "descriptionPattern";
        String propertyKey = "propertyWithoutRegex";
        Pattern propertyWithoutRegex = Pattern.compile(propertyKey);
        PropertyCategory category = PropertyCategory.VIVIDUS;
        DynamicEnvironmentConfigurationProperty dynamicProperty = new DynamicEnvironmentConfigurationProperty();
        dynamicProperty.setCategory(category);
        dynamicProperty.setDescriptionPattern(descriptionPattern);
        dynamicProperty.setPropertyRegex(propertyWithoutRegex);
        when(propertyMapper.readValues(DYNAMIC_PROPERTY_PREFIX, DynamicEnvironmentConfigurationProperty.class))
                .thenReturn(Map.of(VALUE, dynamicProperty));

        when(propertyParser.getPropertiesByRegex(propertyWithoutRegex)).thenReturn(Map.of(propertyKey, VALUE));
        environmentConfigurer.init();
        Map<String, String> vividusProps = EnvironmentConfigurer.ENVIRONMENT_CONFIGURATION.get(category);
        assertEquals(1, vividusProps.size());
        Entry<String, String> entry = vividusProps.entrySet().iterator().next();
        assertEquals(descriptionPattern, entry.getKey());
        assertEquals(VALUE, entry.getValue());
    }

    @Test
    void testInitDynamicRegex() throws Exception
    {
        String descriptionPattern = "description %s Pattern";
        Pattern propertyRegex = Pattern.compile("property(.*)regex");
        PropertyCategory category = PropertyCategory.VIVIDUS;
        DynamicEnvironmentConfigurationProperty dynamicProperty = new DynamicEnvironmentConfigurationProperty();
        dynamicProperty.setCategory(category);
        dynamicProperty.setDescriptionPattern(descriptionPattern);
        dynamicProperty.setPropertyRegex(propertyRegex);
        when(propertyMapper.readValues(DYNAMIC_PROPERTY_PREFIX, DynamicEnvironmentConfigurationProperty.class))
                .thenReturn(Map.of(VALUE, dynamicProperty));
        String index = "123";
        when(propertyParser.getPropertiesByRegex(propertyRegex)).thenReturn(
                Map.of("property" + index + "regex", VALUE));
        environmentConfigurer.init();
        Map<String, String> vividusProps = EnvironmentConfigurer.ENVIRONMENT_CONFIGURATION.get(category);
        assertEquals(1, vividusProps.size());
        Entry<String, String> entry = vividusProps.entrySet().iterator().next();
        assertEquals(String.format(descriptionPattern, index), entry.getKey());
        assertEquals(VALUE, entry.getValue());
    }

    @Test
    void testAddPropertyNullKey()
    {
        PropertyCategory category = PropertyCategory.VIVIDUS;
        EnvironmentConfigurer.addProperty(category, null, null);
        assertEquals(0, EnvironmentConfigurer.ENVIRONMENT_CONFIGURATION.get(category).size());
    }
}
