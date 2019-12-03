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

import java.io.IOException;
import java.util.Collection;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.apache.commons.text.WordUtils;
import org.vividus.reporter.ModuleVersionProvider;
import org.vividus.util.property.IPropertyMapper;
import org.vividus.util.property.IPropertyParser;
import org.vividus.util.property.PropertyMappedDataProvider;

public class EnvironmentConfigurer
{
    public static final Map<PropertyCategory, Map<String, String>> ENVIRONMENT_CONFIGURATION = new EnumMap<>(
            PropertyCategory.class);

    private static final String PROPERTY_PREFIX = "environment-configurer.";
    private static final String DYNAMIC = "dynamic";
    private static final String DYNAMIC_PROPERTY_PREFIX = PROPERTY_PREFIX + DYNAMIC + '.';

    static
    {
        Stream.of(PropertyCategory.values())
                .forEach(category -> ENVIRONMENT_CONFIGURATION.put(category, new LinkedHashMap<>()));
        ModuleVersionProvider.getAvailableModulesByRegex("org\\.vividus.*").forEach(
            (key, value) -> addProperty(PropertyCategory.VIVIDUS, key, value));
    }

    private IPropertyMapper propertyMapper;
    private IPropertyParser propertyParser;


    @SuppressWarnings("unchecked")
    public void init() throws IOException
    {
        Collection<DynamicEnvironmentConfigurationProperty> values = new PropertyMappedDataProvider<>(propertyMapper,
                DYNAMIC_PROPERTY_PREFIX, DynamicEnvironmentConfigurationProperty.class).getData().values();
        for (DynamicEnvironmentConfigurationProperty property: values)
        {
            Pattern propertyRegex = property.getPropertyRegex();
            Map<String, String> matchedProperties = new TreeMap<>(propertyParser.getPropertiesByRegex(propertyRegex));
            matchedProperties.forEach((key, value) ->
            {
                Matcher matcher = propertyRegex.matcher(key);
                matcher.matches();
                String descriptionPattern = property.getDescriptionPattern();
                addProperty(property.getCategory(), matcher.groupCount() > 0
                        ? String.format(descriptionPattern, matcher.group(1)) : descriptionPattern, value);
            });
        }

        propertyMapper.readValues(PROPERTY_PREFIX, Map.class).forEach((category, properties) ->
        {
            if (!DYNAMIC.equals(category))
            {
                PropertyCategory propertyCategory = PropertyCategory.valueOf(category.toUpperCase());
                Map<String, String> currentProperties = ENVIRONMENT_CONFIGURATION.get(propertyCategory);
                properties.forEach((k, v) -> currentProperties
                        .put(WordUtils.capitalize(((String) k).replace('-', ' ')), (String) v));
            }
        });
    }

    public static void addProperty(PropertyCategory category, String key, String value)
    {
        if (key != null)
        {
            ENVIRONMENT_CONFIGURATION.get(category).put(key, value);
        }
    }

    public void setPropertyMapper(IPropertyMapper propertyMapper)
    {
        this.propertyMapper = propertyMapper;
    }

    public void setPropertyParser(IPropertyParser propertyParser)
    {
        this.propertyParser = propertyParser;
    }
}
