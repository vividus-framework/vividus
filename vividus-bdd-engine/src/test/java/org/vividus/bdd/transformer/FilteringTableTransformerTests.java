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

package org.vividus.bdd.transformer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Properties;
import java.util.stream.Stream;

import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.model.ExamplesTable.ExamplesTableProperties;
import org.jbehave.core.model.ExamplesTableFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FilteringTableTransformerTests
{
    private static final String TABLE = "|key1|key2|key3|\n|1|2|3|\n|4|5|6|\n|7|8|9|";
    private static final String BY_MAX_ROWS_PROPERTY = "byMaxRows";

    @Mock
    private ExamplesTableFactory factory;

    @InjectMocks
    private FilteringTableTransformer transformer;

    @ParameterizedTest
    @MethodSource("tableSource")
    void testTransform(String tableToTransform, Properties properties, String expectedTable)
    {
        transformer.setExamplesTableFactory(() -> factory);
        when(factory.createExamplesTable(tableToTransform)).thenReturn(new ExamplesTable(TABLE));
        assertEquals(expectedTable, transformer.transform(tableToTransform, null,
                new ExamplesTableProperties(properties)));
    }

    @Test
    void testTransformUnorderedHeader()
    {
        String table = "|key2|key3|key1|\n|2|3|1|\n|4|5|6|\n|7|8|9|";
        transformer.setExamplesTableFactory(() -> factory);
        when(factory.createExamplesTable(table)).thenReturn(new ExamplesTable(table));
        Properties properties = new Properties();
        properties.setProperty(BY_MAX_ROWS_PROPERTY, "1");
        assertEquals("|key2|key3|key1|\n|2|3|1|", transformer.transform(table, null,
                new ExamplesTableProperties(properties)));
    }

    @Test
    void testFailOnMissingProperties()
    {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> transformer.transform(TABLE, null, new ExamplesTableProperties(new Properties())));
        assertEquals("At least one of the following properties should be specified: 'byMaxColumns', 'byMaxRows', "
                + "'byColumnNames'", exception.getMessage());
    }

    @Test
    void testFailOnConflictingProperties()
    {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> transformer.transform(TABLE, null, new ExamplesTableProperties(createProperties(1, null, "key2"))));
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
            properties.setProperty(BY_MAX_ROWS_PROPERTY, String.valueOf(byMaxRows));
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
            Arguments.of(TABLE, createProperties(null, 1, "key1"),      "|key1|\n|1|"),
            Arguments.of("/test.table", createProperties(2, 1, null),   "|key1|key2|\n|1|2|")
        );
    }
    // CHECKSTYLE:ON
}
