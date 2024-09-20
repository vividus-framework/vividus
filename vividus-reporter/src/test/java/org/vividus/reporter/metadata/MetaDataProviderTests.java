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

import java.io.IOException;
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
    private static final String PREFIX = "metadata.";
    private static final String DYNAMIC_PROPERTY_PREFIX = PREFIX + "dynamic" + ".";
    private static final String STATIC_PROPERTY_PREFIX = PREFIX + "static" + '.';
    private static final String KEY = "Some Key";
    private static final String VALUE = "value";

    @Mock private IPropertyMapper propertyMapper;
    @Mock private IPropertyParser propertyParser;
    @InjectMocks private MetadataProvider metadataProvider;

    @BeforeEach
    void beforeEach()
    {
        MetadataProvider.reset();
    }

    @AfterEach
    void afterEach()
    {
        MetadataProvider.reset();
    }

    @Test
    void testInit() throws IOException
    {
        var category = MetadataCategory.CONFIGURATION;
        var metadataEntry = new MetadataEntry();
        metadataEntry.setCategory(category);
        metadataEntry.setName(KEY);
        metadataEntry.setValue(VALUE);

        when(propertyMapper.readValues(DYNAMIC_PROPERTY_PREFIX, DynamicMetadataEntry.class)).thenReturn(
                new PropertyMappedCollection<>(Map.of()));
        when(propertyMapper.readValues(STATIC_PROPERTY_PREFIX, MetadataEntry.class)).thenReturn(
                new PropertyMappedCollection<>(Map.of(category.toString(), metadataEntry)));
        metadataProvider.init();

        assertAll(
                () -> assertEquals(List.of(metadataEntry), MetadataProvider.getMetaDataByCategory(category)),
                () -> assertEquals(Map.of(KEY, VALUE), MetadataProvider.getMetaDataByCategoryAsMap(category))
        );
    }

    @Test
    void testInitDynamicNoRegex() throws IOException
    {
        var namePattern = "namePattern";
        var propertyKey = "propertyWithoutRegex";
        var propertyWithoutRegex = Pattern.compile(propertyKey);
        var category = MetadataCategory.VIVIDUS;
        var dynamicMetadataEntry = new DynamicMetadataEntry();
        dynamicMetadataEntry.setCategory(category);
        dynamicMetadataEntry.setNamePattern(namePattern);
        dynamicMetadataEntry.setPropertyRegex(propertyWithoutRegex);
        when(propertyMapper.readValues(DYNAMIC_PROPERTY_PREFIX, DynamicMetadataEntry.class))
                .thenReturn(new PropertyMappedCollection<>(Map.of(VALUE, dynamicMetadataEntry)));
        when(propertyMapper.readValues(STATIC_PROPERTY_PREFIX, MetadataEntry.class))
                .thenReturn(new PropertyMappedCollection<>(Map.of()));
        when(propertyParser.getPropertiesByRegex(propertyWithoutRegex)).thenReturn(Map.of(propertyKey, VALUE));
        metadataProvider.init();
        List<MetadataEntry> vividusMetaData = MetadataProvider.getMetaDataByCategory(category);
        assertEquals(1, vividusMetaData.size());
        MetadataEntry entry = vividusMetaData.iterator().next();
        assertEquals(namePattern, entry.getName());
        assertEquals(VALUE, entry.getValue());
    }

    @Test
    void testInitDynamicRegex() throws Exception
    {
        var namePattern = "Name %s Pattern";
        var propertyRegex = Pattern.compile("property(.*)regex");
        var category = MetadataCategory.VIVIDUS;
        var dynamicMetadataEntry = new DynamicMetadataEntry();
        dynamicMetadataEntry.setCategory(category);
        dynamicMetadataEntry.setNamePattern(namePattern);
        dynamicMetadataEntry.setPropertyRegex(propertyRegex);
        when(propertyMapper.readValues(DYNAMIC_PROPERTY_PREFIX, DynamicMetadataEntry.class))
                .thenReturn(new PropertyMappedCollection<>(Map.of(VALUE, dynamicMetadataEntry)));
        when(propertyMapper.readValues(STATIC_PROPERTY_PREFIX, MetadataEntry.class))
                .thenReturn(new PropertyMappedCollection<>(Map.of()));
        var index = "123";
        when(propertyParser.getPropertiesByRegex(propertyRegex)).thenReturn(
                Map.of("property" + index + "regex", VALUE));
        metadataProvider.init();
        List<MetadataEntry> vividusMetaData = MetadataProvider.getMetaDataByCategory(category);
        assertEquals(1, vividusMetaData.size());
        MetadataEntry entry = vividusMetaData.iterator().next();
        assertEquals(String.format(namePattern, index), entry.getName());
        assertEquals(VALUE, entry.getValue());
    }

    @Test
    void testAddPropertyNullKey()
    {
        MetadataProvider.addVividusMetaData(null, null);
        assertEquals(0, MetadataProvider.getMetaDataByCategory(MetadataCategory.VIVIDUS).size());
    }
}
