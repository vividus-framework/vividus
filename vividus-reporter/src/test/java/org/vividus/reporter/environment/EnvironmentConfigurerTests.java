/*
 * Copyright 2019-2024 the original author or authors.
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
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
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
import org.vividus.util.property.PropertyMappedCollection;

@ExtendWith(MockitoExtension.class)
class EnvironmentConfigurerTests
{
    private static final String PREFIX = "environment-configurer.";
    private static final String DYNAMIC_PROPERTY_PREFIX = PREFIX + "dynamic" + ".";
    private static final String STATIC_PROPERTY_PREFIX = PREFIX + "static" + '.';
    private static final String KEY = "Some Key";
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
        EnvironmentConfigurer.ENVIRONMENT_CONFIGURATION.values().forEach(List::clear);
    }

    @AfterEach
    void afterEach()
    {
        EnvironmentConfigurer.ENVIRONMENT_CONFIGURATION.values().forEach(List::clear);
    }

    @Test
    void testInit() throws Exception
    {
        StaticConfigurationDataEntry vividusConfig = new StaticConfigurationDataEntry();
        vividusConfig.setCategory(PropertyCategory.VIVIDUS);
        vividusConfig.setDescription(KEY);
        vividusConfig.setValue(VALUE);

        PropertyCategory category = PropertyCategory.VIVIDUS;
        when(propertyMapper.readValues(DYNAMIC_PROPERTY_PREFIX, DynamicConfigurationDataEntry.class))
                .thenReturn(new PropertyMappedCollection<>(Map.of()));
        when(propertyMapper.readValues(STATIC_PROPERTY_PREFIX, StaticConfigurationDataEntry.class))
                .thenReturn(new PropertyMappedCollection<>(Map.of(PropertyCategory.VIVIDUS.toString(), vividusConfig)));
        environmentConfigurer.init();
        List<StaticConfigurationDataEntry> vividusConfigurationList = EnvironmentConfigurer.ENVIRONMENT_CONFIGURATION
                .get(category);
        assertEquals(List.of(vividusConfig), vividusConfigurationList);
    }

    @Test
    void testInitDynamicNoRegex() throws Exception
    {
        String descriptionPattern = "descriptionPattern";
        String propertyKey = "propertyWithoutRegex";
        Pattern propertyWithoutRegex = Pattern.compile(propertyKey);
        PropertyCategory category = PropertyCategory.VIVIDUS;
        DynamicConfigurationDataEntry dynamicProperty = new DynamicConfigurationDataEntry();
        dynamicProperty.setCategory(category);
        dynamicProperty.setDescription(descriptionPattern);
        dynamicProperty.setPropertyRegex(propertyWithoutRegex);
        when(propertyMapper.readValues(DYNAMIC_PROPERTY_PREFIX, DynamicConfigurationDataEntry.class))
                .thenReturn(new PropertyMappedCollection<>(Map.of(VALUE, dynamicProperty)));
        when(propertyMapper.readValues(STATIC_PROPERTY_PREFIX, StaticConfigurationDataEntry.class))
                .thenReturn(new PropertyMappedCollection<>(Map.of()));
        when(propertyParser.getPropertiesByRegex(propertyWithoutRegex)).thenReturn(Map.of(propertyKey, VALUE));
        environmentConfigurer.init();
        List<StaticConfigurationDataEntry> vividusConfigurationList = EnvironmentConfigurer.ENVIRONMENT_CONFIGURATION
                .get(category);
        assertEquals(1, vividusConfigurationList.size());
        StaticConfigurationDataEntry entry = vividusConfigurationList.iterator().next();
        assertEquals(descriptionPattern, entry.getDescription());
        assertEquals(VALUE, entry.getValue());
    }

    @Test
    void testInitDynamicRegex() throws Exception
    {
        String descriptionPattern = "description %s Pattern";
        Pattern propertyRegex = Pattern.compile("property(.*)regex");
        PropertyCategory category = PropertyCategory.VIVIDUS;
        DynamicConfigurationDataEntry dynamicProperty = new DynamicConfigurationDataEntry();
        dynamicProperty.setCategory(category);
        dynamicProperty.setDescription(descriptionPattern);
        dynamicProperty.setPropertyRegex(propertyRegex);
        when(propertyMapper.readValues(DYNAMIC_PROPERTY_PREFIX, DynamicConfigurationDataEntry.class))
                .thenReturn(new PropertyMappedCollection<>(Map.of(VALUE, dynamicProperty)));
        when(propertyMapper.readValues(STATIC_PROPERTY_PREFIX, StaticConfigurationDataEntry.class))
                .thenReturn(new PropertyMappedCollection<>(Map.of()));
        String index = "123";
        when(propertyParser.getPropertiesByRegex(propertyRegex)).thenReturn(
                Map.of("property" + index + "regex", VALUE));
        environmentConfigurer.init();
        List<StaticConfigurationDataEntry> vividusConfigurationList = EnvironmentConfigurer.ENVIRONMENT_CONFIGURATION
                .get(category);
        assertEquals(1, vividusConfigurationList.size());
        StaticConfigurationDataEntry entry = vividusConfigurationList.iterator().next();
        assertEquals(String.format(descriptionPattern, index), entry.getDescription());
        assertEquals(VALUE, entry.getValue());
    }

    @Test
    void testAddPropertyNullKey()
    {
        PropertyCategory category = PropertyCategory.VIVIDUS;
        StaticConfigurationDataEntry config = new StaticConfigurationDataEntry();
        config.setCategory(category);
        EnvironmentConfigurer.addConfigurationDataEntry(config);
        assertEquals(0, EnvironmentConfigurer.ENVIRONMENT_CONFIGURATION.get(category).size());
    }

    @Test
    void testGetConfigurationDataAsMap()
    {
        StaticConfigurationDataEntry config = new StaticConfigurationDataEntry();
        config.setDescription(KEY);
        config.setValue(VALUE);
        assertEquals(Map.of(KEY, VALUE), EnvironmentConfigurer.getConfigurationDataAsMap(List.of(config)));
    }
}
