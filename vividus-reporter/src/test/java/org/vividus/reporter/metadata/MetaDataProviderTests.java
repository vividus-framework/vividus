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

import static org.junit.jupiter.api.Assertions.assertAll;
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
class MetaDataProviderTests
{
    private static final String PREFIX = "meta-data.";
    private static final String DYNAMIC_PROPERTY_PREFIX = PREFIX + "dynamic" + ".";
    private static final String STATIC_PROPERTY_PREFIX = PREFIX + "static" + '.';
    private static final String KEY = "Some Key";
    private static final String VALUE = "value";

    @Mock private IPropertyMapper propertyMapper;
    @Mock private IPropertyParser propertyParser;
    @InjectMocks private MetaDataProvider metaDataProvider;

    @BeforeEach
    void beforeEach()
    {
        MetaDataProvider.reset();
    }

    @AfterEach
    void afterEach()
    {
        MetaDataProvider.reset();
    }

    @Test
    void testInit() throws Exception
    {
        MetaDataCategory category = MetaDataCategory.CONFIGURATION;
        MetaDataEntry metaDataEntry = new MetaDataEntry();
        metaDataEntry.setCategory(category);
        metaDataEntry.setDescription(KEY);
        metaDataEntry.setValue(VALUE);

        when(propertyMapper.readValues(DYNAMIC_PROPERTY_PREFIX, DynamicMetaDataEntry.class)).thenReturn(
                new PropertyMappedCollection<>(Map.of()));
        when(propertyMapper.readValues(STATIC_PROPERTY_PREFIX, MetaDataEntry.class)).thenReturn(
                new PropertyMappedCollection<>(Map.of(category.toString(), metaDataEntry)));
        metaDataProvider.init();

        assertAll(
                () -> assertEquals(List.of(metaDataEntry), MetaDataProvider.getMetaDataByCategory(category)),
                () -> assertEquals(Map.of(KEY, VALUE), MetaDataProvider.getMetaDataByCategoryAsMap(category))
        );
    }

    @Test
    void testInitDynamicNoRegex() throws Exception
    {
        String descriptionPattern = "descriptionPattern";
        String propertyKey = "propertyWithoutRegex";
        Pattern propertyWithoutRegex = Pattern.compile(propertyKey);
        MetaDataCategory category = MetaDataCategory.VIVIDUS;
        DynamicMetaDataEntry dynamicProperty = new DynamicMetaDataEntry();
        dynamicProperty.setCategory(category);
        dynamicProperty.setDescriptionPattern(descriptionPattern);
        dynamicProperty.setPropertyRegex(propertyWithoutRegex);
        when(propertyMapper.readValues(DYNAMIC_PROPERTY_PREFIX, DynamicMetaDataEntry.class))
                .thenReturn(new PropertyMappedCollection<>(Map.of(VALUE, dynamicProperty)));
        when(propertyMapper.readValues(STATIC_PROPERTY_PREFIX, MetaDataEntry.class))
                .thenReturn(new PropertyMappedCollection<>(Map.of()));
        when(propertyParser.getPropertiesByRegex(propertyWithoutRegex)).thenReturn(Map.of(propertyKey, VALUE));
        metaDataProvider.init();
        List<MetaDataEntry> vividusMetaData = MetaDataProvider.getMetaDataByCategory(category);
        assertEquals(1, vividusMetaData.size());
        MetaDataEntry entry = vividusMetaData.iterator().next();
        assertEquals(descriptionPattern, entry.getDescription());
        assertEquals(VALUE, entry.getValue());
    }

    @Test
    void testInitDynamicRegex() throws Exception
    {
        String descriptionPattern = "description %s Pattern";
        Pattern propertyRegex = Pattern.compile("property(.*)regex");
        MetaDataCategory category = MetaDataCategory.VIVIDUS;
        DynamicMetaDataEntry dynamicProperty = new DynamicMetaDataEntry();
        dynamicProperty.setCategory(category);
        dynamicProperty.setDescriptionPattern(descriptionPattern);
        dynamicProperty.setPropertyRegex(propertyRegex);
        when(propertyMapper.readValues(DYNAMIC_PROPERTY_PREFIX, DynamicMetaDataEntry.class))
                .thenReturn(new PropertyMappedCollection<>(Map.of(VALUE, dynamicProperty)));
        when(propertyMapper.readValues(STATIC_PROPERTY_PREFIX, MetaDataEntry.class))
                .thenReturn(new PropertyMappedCollection<>(Map.of()));
        String index = "123";
        when(propertyParser.getPropertiesByRegex(propertyRegex)).thenReturn(
                Map.of("property" + index + "regex", VALUE));
        metaDataProvider.init();
        List<MetaDataEntry> vividusMetaData = MetaDataProvider.getMetaDataByCategory(category);
        assertEquals(1, vividusMetaData.size());
        MetaDataEntry entry = vividusMetaData.iterator().next();
        assertEquals(String.format(descriptionPattern, index), entry.getDescription());
        assertEquals(VALUE, entry.getValue());
    }

    @Test
    void testAddPropertyNullKey()
    {
        MetaDataProvider.addVividusMetaData(null, null);
        assertEquals(0, MetaDataProvider.getMetaDataByCategory(MetaDataCategory.VIVIDUS).size());
    }
}
