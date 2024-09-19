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

import java.io.IOException;
import java.util.Collection;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.vividus.reporter.ModuleVersionProvider;
import org.vividus.util.property.IPropertyMapper;
import org.vividus.util.property.IPropertyParser;

public class EnvironmentConfigurer
{
    public static final Map<PropertyCategory, List<StaticConfigurationDataEntry>> ENVIRONMENT_CONFIGURATION =
            new EnumMap<>(PropertyCategory.class);

    private static final String PROPERTY_PREFIX = "environment-configurer.";
    private static final String DYNAMIC_PROPERTY_PREFIX = PROPERTY_PREFIX + "dynamic" + '.';
    private static final String STATIC_PROPERTY_PREFIX = PROPERTY_PREFIX + "static" + '.';

    static
    {
        Stream.of(PropertyCategory.values())
                .forEach(category -> ENVIRONMENT_CONFIGURATION.put(category, new LinkedList<>()));
        ModuleVersionProvider.getAvailableModulesByRegex("org\\.vividus.*").forEach(
            (key, value) -> {
                StaticConfigurationDataEntry dataEntry = new StaticConfigurationDataEntry();
                dataEntry.setCategory(PropertyCategory.VIVIDUS);
                dataEntry.setDescription(key);
                dataEntry.setValue(value);
                addConfigurationDataEntry(dataEntry);
            });
    }

    private IPropertyMapper propertyMapper;
    private IPropertyParser propertyParser;

    @SuppressWarnings("unchecked")
    public void init() throws IOException
    {
        Collection<DynamicConfigurationDataEntry> dynamicConfiguration = propertyMapper
                .readValues(DYNAMIC_PROPERTY_PREFIX, DynamicConfigurationDataEntry.class).getData().values();
        for (DynamicConfigurationDataEntry configuration : dynamicConfiguration)
        {
            Pattern propertyRegex = configuration.getPropertyRegex();
            Map<String, String> matchedProperties = new TreeMap<>(propertyParser.getPropertiesByRegex(propertyRegex));
            matchedProperties.forEach((key, value) ->
            {
                Matcher matcher = propertyRegex.matcher(key);
                matcher.matches();
                String descriptionPattern = configuration.getDescription();

                StaticConfigurationDataEntry dataEntry = new StaticConfigurationDataEntry();
                dataEntry.setCategory(configuration.getCategory());
                dataEntry.setDescription(matcher.groupCount() > 0 ? String.format(descriptionPattern, matcher.group(1))
                        : descriptionPattern);
                dataEntry.setValue(value);
                dataEntry.setAddToReport(configuration.isAddToReport());
                addConfigurationDataEntry(dataEntry);
            });
        }
        propertyMapper.readValues(STATIC_PROPERTY_PREFIX, StaticConfigurationDataEntry.class).getData().values()
                .forEach(EnvironmentConfigurer::addConfigurationDataEntry);
    }

    public static void addConfigurationDataEntry(StaticConfigurationDataEntry dataEntry)
    {
        if (dataEntry.getDescription() != null)
        {
            ENVIRONMENT_CONFIGURATION.get(dataEntry.getCategory()).add(dataEntry);
        }
    }

    public static Map<String, String> getConfigurationDataAsMap(List<StaticConfigurationDataEntry> data)
    {
        return data.stream().collect(Collectors.toMap(StaticConfigurationDataEntry::getDescription,
                StaticConfigurationDataEntry::getValue, (a, b) -> b, LinkedHashMap::new));
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
