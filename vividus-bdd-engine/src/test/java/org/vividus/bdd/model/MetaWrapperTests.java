/*
 * Copyright 2019-2020 the original author or authors.
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

package org.vividus.bdd.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Stream;

import org.jbehave.core.model.Meta;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class MetaWrapperTests
{
    @Test
    void testHasProperty()
    {
        String propertyName = "propertyName";
        Properties properties = new Properties();
        properties.setProperty(propertyName, "propertyValue");
        Meta meta = new Meta(properties);
        MetaWrapper metaWrapper = new MetaWrapper(meta);
        assertTrue(metaWrapper.hasProperty(propertyName));
    }

    @Test
    void toTagsTest()
    {
        String propertyName = "name2";
        List<String> list = List.of("name1=value1", propertyName);
        Properties properties = new Properties();
        properties.setProperty("name1", "value1");
        properties.setProperty(propertyName, "");
        Meta meta = new Meta(properties);
        assertEquals(list, new MetaWrapper(meta).toTags());
    }

    @Test
    void testGetOptionalPropertyValue()
    {
        Meta meta = mock(Meta.class);
        String propertyName = "name";
        String propertyValue = "value";
        when(meta.getProperty(propertyName)).thenReturn(propertyValue);
        MetaWrapper metaWrapper = new MetaWrapper(meta);
        assertEquals(Optional.of(propertyValue), metaWrapper.getOptionalPropertyValue(propertyName));
    }

    static Stream<Arguments> metaValues()
    {
        return Stream.of(
                arguments("gh-25;gh-128;gh-25", Set.of("gh-25", "gh-128")),
                arguments("",                   Set.of())
        );
    }

    @ParameterizedTest
    @MethodSource("metaValues")
    void testGetPropertyValues(String value, Set<String> expected)
    {
        String name = "testCaseId";
        Properties properties = new Properties();
        properties.setProperty(name, value);
        Meta meta = new Meta(properties);
        assertEquals(expected, new MetaWrapper(meta).getPropertyValues(name));
    }

    @Test
    void testGetPropertyValuesForNonExistentMeta()
    {
        assertEquals(Set.of(), new MetaWrapper(new Meta()).getPropertyValues("non-existent"));
    }

    @Test
    void testGetPropertiesByKey()
    {
        Map<String, String> values = Map.of(
            "key-1", "value-1",
            "key-2", "value-2");
        Properties properties = new Properties();
        properties.putAll(values);
        properties.put("fn", "Bob");
        Meta meta = new Meta(properties);
        assertEquals(values, new MetaWrapper(meta).getPropertiesByKey(k -> k.startsWith("key")));
    }
}
