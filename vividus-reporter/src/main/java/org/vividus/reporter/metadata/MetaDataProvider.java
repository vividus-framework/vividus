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

public class MetaDataProvider
{
    private static final List<MetaDataEntry> META_DATA = new LinkedList<>();

    private static final String PROPERTY_PREFIX = "meta-data.";
    private static final String DYNAMIC_PROPERTY_PREFIX = PROPERTY_PREFIX + "dynamic.";
    private static final String STATIC_PROPERTY_PREFIX = PROPERTY_PREFIX + "static.";

    static
    {
        ModuleVersionProvider.getAvailableModulesByRegex("org\\.vividus.*").forEach(
                MetaDataProvider::addVividusMetaData);
    }

    private IPropertyMapper propertyMapper;
    private IPropertyParser propertyParser;

    public void init() throws IOException
    {
        propertyMapper.readValues(DYNAMIC_PROPERTY_PREFIX, DynamicMetaDataEntry.class).getData().values().stream()
                .flatMap(dynamicMetaDataEntry ->
                {
                    String descriptionPattern = dynamicMetaDataEntry.getDescriptionPattern();
                    Pattern propertyRegex = dynamicMetaDataEntry.getPropertyRegex();

                    return propertyParser.getPropertiesByRegex(propertyRegex).entrySet().stream().map(e ->
                    {
                        Matcher matcher = propertyRegex.matcher(e.getKey());
                        matcher.matches();

                        MetaDataEntry entry = new MetaDataEntry();
                        entry.setCategory(dynamicMetaDataEntry.getCategory());
                        entry.setDescription(matcher.groupCount() > 0 ? descriptionPattern.formatted(matcher.group(1))
                                : descriptionPattern);
                        entry.setValue(e.getValue());
                        entry.setAddToReport(dynamicMetaDataEntry.isAddToReport());

                        return entry;
                    });
                })
                .sorted(Comparator.comparing(MetaDataEntry::getDescription))
                .forEach(META_DATA::add);

        META_DATA.addAll(propertyMapper.readValues(STATIC_PROPERTY_PREFIX, MetaDataEntry.class).getData().values());
    }

    static void addVividusMetaData(String description, String value)
    {
        if (description != null)
        {
            MetaDataEntry entry = new MetaDataEntry();
            entry.setCategory(MetaDataCategory.VIVIDUS);
            entry.setDescription(description);
            entry.setValue(value);
            entry.setAddToReport(false);

            META_DATA.add(entry);
        }
    }

    static void reset()
    {
        META_DATA.clear();
    }

    public static List<MetaDataEntry> getMetaDataByCategory(MetaDataCategory category)
    {
        return META_DATA.stream()
                .filter(entry -> entry.getCategory() == category)
                .collect(Collectors.toCollection(LinkedList::new));
    }

    public static Map<String, String> getMetaDataByCategoryAsMap(MetaDataCategory category)
    {
        return META_DATA.stream()
                .filter(entry -> entry.getCategory() == category)
                .collect(Collectors.toMap(MetaDataEntry::getDescription, MetaDataEntry::getValue, (a, b) -> b,
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
