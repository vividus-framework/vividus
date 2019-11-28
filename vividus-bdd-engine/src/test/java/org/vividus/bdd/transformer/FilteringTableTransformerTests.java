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

package org.vividus.bdd.transformer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Properties;
import java.util.stream.Stream;

import org.jbehave.core.model.ExamplesTableProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class FilteringTableTransformerTests
{
    private static final String TABLE = "|key1|key2|key3|\n|1|2|3|\n|4|5|6|\n|7|8|9|";
    private final FilteringTableTransformer transformer = new FilteringTableTransformer();

    @ParameterizedTest
    @MethodSource("tableSource")
    void testTransform(String tableToTransform, Properties properties, String expectedTable)
    {
        assertEquals(expectedTable, transformer.transform(tableToTransform, new ExamplesTableProperties(properties)));
    }

    @Test
    void testFailOnMissingProperties()
    {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> transformer.transform(TABLE, new ExamplesTableProperties(new Properties())));
        assertEquals("At least one of the following properties should be specified: 'byMaxColumns', 'byMaxRows', "
                + "'byColumnNames'", exception.getMessage());
    }

    @Test
    void testFailOnConflictingProperties()
    {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> transformer.transform(TABLE, new ExamplesTableProperties(createProperties(1, null, "key2"))));
        assertEquals("Conflicting properties declaration found: 'byMaxColumns' and 'byColumnNames'",
                exception.getMessage());
    }

    private static Properties createProperties(Integer columns, Integer byMaxRows, String byColumnNames)
    {
        Properties properties = new Properties();
        if (columns != null)
        {
            properties.setProperty("byMaxColumns", String.valueOf(columns));
        }
        if (byMaxRows != null)
        {
            properties.setProperty("byMaxRows", String.valueOf(byMaxRows));
        }
        if (byColumnNames != null)
        {
            properties.setProperty("byColumnNames", byColumnNames);
        }
        return properties;
    }

    // CHECKSTYLE:OFF
    static Stream<Arguments> tableSource() {
        return Stream.of(
            Arguments.of(TABLE, createProperties(3, 3, null),           TABLE),
            Arguments.of(TABLE, createProperties(3, 5, null),           TABLE),
            Arguments.of(TABLE, createProperties(2, 2, null),           "|key1|key2|\n|1|2|\n|4|5|"),
            Arguments.of(TABLE, createProperties(null, 2, null),        "|key1|key2|key3|\n|1|2|3|\n|4|5|6|"),
            Arguments.of(TABLE, createProperties(2, null, null),        "|key1|key2|\n|1|2|\n|4|5|\n|7|8|"),
            Arguments.of(TABLE, createProperties(null, 2, "key1;key3"), "|key1|key3|\n|1|3|\n|4|6|"),
            Arguments.of(TABLE, createProperties(null, 5, "key3;key2"), "|key2|key3|\n|2|3|\n|5|6|\n|8|9|"),
            Arguments.of(TABLE, createProperties(null, 1, "key1"),      "|key1|\n|1|")
        );
    }
    // CHECKSTYLE:ON
}
