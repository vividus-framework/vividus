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

package org.vividus.reporter.metadata;

import java.io.IOException;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.vividus.util.property.IPropertyMapper;
import org.vividus.util.property.IPropertyParser;

public class MetadataProvider
{
    private static final List<MetadataEntry> META_DATA = new LinkedList<>();

    private static final String PROPERTY_PREFIX = "metadata.";
    private static final String DYNAMIC_PROPERTY_PREFIX = PROPERTY_PREFIX + "dynamic.";
    private static final String STATIC_PROPERTY_PREFIX = PROPERTY_PREFIX + "static.";

    static
    {
        ModuleVersionProvider.getAvailableModulesByRegex("org\\.vividus.*").forEach(
                MetadataProvider::addVividusMetaData);
    }

    private IPropertyMapper propertyMapper;
    private IPropertyParser propertyParser;

    public void init() throws IOException
    {
        propertyMapper.readValues(DYNAMIC_PROPERTY_PREFIX, DynamicMetadataEntry.class).getData().values().stream()
                .flatMap(dynamicMetaDataEntry ->
                {
                    String namePattern = dynamicMetaDataEntry.getNamePattern();
                    Pattern propertyRegex = dynamicMetaDataEntry.getPropertyRegex();

                    return propertyParser.getPropertiesByRegex(propertyRegex).entrySet().stream().map(e ->
                    {
                        Matcher matcher = propertyRegex.matcher(e.getKey());
                        matcher.matches();

                        MetadataEntry entry = new MetadataEntry();
                        entry.setCategory(dynamicMetaDataEntry.getCategory());
                        entry.setName(matcher.groupCount() > 0 ? namePattern.formatted(matcher.group(1)) : namePattern);
                        entry.setValue(e.getValue());
                        entry.setShowInReport(dynamicMetaDataEntry.isShowInReport());

                        return entry;
                    });
                })
                .sorted(Comparator.comparing(MetadataEntry::getName))
                .forEach(META_DATA::add);

        META_DATA.addAll(propertyMapper.readValues(STATIC_PROPERTY_PREFIX, MetadataEntry.class).getData().values());
    }

    static void addVividusMetaData(String name, String value)
    {
        if (name != null)
        {
            MetadataEntry entry = new MetadataEntry();
            entry.setCategory(MetadataCategory.VIVIDUS);
            entry.setName(name);
            entry.setValue(value);
            entry.setShowInReport(false);

            META_DATA.add(entry);
        }
    }

    static void reset()
    {
        META_DATA.clear();
    }

    public static List<MetadataEntry> getMetaDataByCategory(MetadataCategory category)
    {
        return META_DATA.stream()
                .filter(entry -> entry.getCategory() == category)
                .collect(Collectors.toCollection(LinkedList::new));
    }

    public static Map<String, String> getMetaDataByCategoryAsMap(MetadataCategory category)
    {
        return META_DATA.stream()
                .filter(entry -> entry.getCategory() == category)
                .collect(Collectors.toMap(MetadataEntry::getName, MetadataEntry::getValue, (a, b) -> b,
                        LinkedHashMap::new));
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
