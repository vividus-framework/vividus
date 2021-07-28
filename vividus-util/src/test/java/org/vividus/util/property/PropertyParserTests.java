/*
 * Copyright 2019-2021 the original author or authors.
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

package org.vividus.util.property;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PropertyParserTests
{
    private static final String PREFIX = "prefix.";
    private static final String OTHER = "other";
    private static final char DOT = '.';
    private static final String PROP_1 = "prop1";
    private static final String VAL_1 = "val1";
    private static final String PROP_2 = "prop2";
    private static final String VAL_2 = "val2";
    private static final String VAL_3 = "val3";
    private static final String PROP_3 = "prop3";
    private static final String OTHER_PROP3 = OTHER + DOT + PROP_3;

    private PropertyParser parser;
    private Map<String, String> expectedPropertyValues;
    private Properties properties;

    @BeforeEach
    void beforeEach()
    {
        expectedPropertyValues = new HashMap<>();
        expectedPropertyValues.put(PROP_1, VAL_1);
        expectedPropertyValues.put(PROP_2, VAL_2);

        properties = new Properties();
        expectedPropertyValues.forEach((key, value) -> properties.put(PREFIX + key, value));
        properties.put(PREFIX + OTHER_PROP3, VAL_3);
        parser = new PropertyParser(properties);
    }

    @Test
    void testGetPropertiesByPrefix()
    {
        Map<String, String> propertiesByPrefix = parser.getPropertiesByPrefix(PREFIX);
        assertThat(propertiesByPrefix.entrySet(), equalTo(properties.entrySet()));
    }

    @Test
    void testGetPropertyValuesByPrefix()
    {
        Map<String, String> actualPropertyValues = parser.getPropertyValuesByPrefix(PREFIX);
        Map<String, String> expected = new HashMap<>(expectedPropertyValues);
        expected.put(OTHER_PROP3, VAL_3);
        assertThat(actualPropertyValues.entrySet(), equalTo(expected.entrySet()));
    }

    @Test
    void testGetPropertiesByRegex()
    {
        Map<String, String> propertiesByPrefix = parser.getPropertiesByRegex(Pattern.compile(PREFIX + ".*prop3"));
        assertThat(propertiesByPrefix.entrySet(),
                equalTo(Collections.singletonMap(PREFIX + OTHER_PROP3, VAL_3).entrySet()));
    }

    @Test
    void testGetPropertyValue()
    {
        Entry<String, String> property = expectedPropertyValues.entrySet().iterator().next();
        assertEquals(property.getValue(), parser.getPropertyValue(PREFIX + "%s", property.getKey()));
    }

    @Test
    void testGetPropertyValuesTreeByPrefix()
    {
        String val4 = "val4";
        String val5 = "val5";
        properties.put(PREFIX + "other.other1.prop4", val4);
        properties.put(PREFIX + "other.other1.prop5", val5);

        Map<String, Object> actualPropertyValues = parser.getPropertyValuesTreeByPrefix(PREFIX);
        Map<String, Object> expected = Map.of(
                PROP_1, VAL_1,
                PROP_2, VAL_2,
                OTHER, Map.of(
                        PROP_3, VAL_3,
                        "other1", Map.of(
                                "prop4", val4,
                                "prop5", val5
                                )
                        ));
        assertThat(actualPropertyValues.entrySet(), equalTo(expected.entrySet()));
    }

    @Test
    void testGetPropertyValuesTreeByPrefixWrongKey()
    {
        properties.put(PREFIX + OTHER, VAL_1);
        properties.put(PREFIX + OTHER + DOT + PROP_1, VAL_1);

        Exception exception = Assertions.assertThrows(IllegalArgumentException.class,
            () -> parser.getPropertyValuesTreeByPrefix(PREFIX));
        assertThat("Path key 'other' from path 'other.prop1' is already used as a property key",
            equalTo(exception.getMessage()));
    }

    @Test
    void shouldReadValuesByPrefix()
    {
        PropertyMappedCollection<String> propertiesByPrefix = parser.readValues(PREFIX);
        assertThat(propertiesByPrefix.getData().values(), hasItems(VAL_1, VAL_2, VAL_3));
    }
}
