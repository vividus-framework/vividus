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

package org.vividus.bdd.transformer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Properties;
import java.util.stream.Stream;

import org.jbehave.core.model.ExamplesTable.TableProperties;
import org.jbehave.core.model.TableParsers;
import org.jbehave.core.steps.ParameterConverters;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FilteringTableTransformerTests
{
    private static final String TABLE = "|key1|key2|key3|\n|1|2|3|\n|4|5|6|\n|7|8|9|";
    private static final String BY_MAX_ROWS_PROPERTY = "byMaxRows";

    private final ParameterConverters parameterConverters = new ParameterConverters();
    private final TableParsers tableParsers = new TableParsers(parameterConverters);
    private final FilteringTableTransformer transformer = new FilteringTableTransformer();

    @ParameterizedTest
    @MethodSource("tableSource")
    void testTransform(String tableToTransform, Properties properties, String expectedTable)
    {
        assertEquals(expectedTable, transformer.transform(tableToTransform, tableParsers,
                new TableProperties(parameterConverters, properties)));
    }

    @Test
    void testTransformUnorderedHeader()
    {
        String table = "|key2|key3|key1|\n|2|3|1|\n|4|5|6|\n|7|8|9|";
        Properties properties = new Properties();
        properties.setProperty(BY_MAX_ROWS_PROPERTY, "1");
        assertEquals("|key2|key3|key1|\n|2|3|1|", transformer.transform(table, tableParsers,
                new TableProperties(parameterConverters, properties)));
    }

    @Test
    void testFailOnMissingProperties()
    {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> transformer.transform(TABLE, tableParsers,
                    new TableProperties(parameterConverters, new Properties())));
        assertEquals("At least one of the following properties should be specified: 'byMaxColumns', 'byMaxRows', "
                + "'byColumnNames', 'column.<regex placeholder>', 'byRowIndexes'", exception.getMessage());
    }

    @Test
    void testFailOnConflictingPropertiesForColumns()
    {
        TableProperties tableProperties = new TableProperties(parameterConverters,
                createProperties(1, null, "key2", null));
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> transformer.transform(TABLE, tableParsers, tableProperties));
        assertEquals("Conflicting properties declaration found: 'byMaxColumns' and 'byColumnNames'",
                exception.getMessage());
    }

    @Test
    void testFailOnConflictingPropertiesForRows()
    {
        TableProperties tableProperties = new TableProperties(parameterConverters,
                createProperties(null, 1, null, "1;2"));
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> transformer.transform(TABLE, tableParsers, tableProperties));
        assertEquals("Conflicting properties declaration found: 'byMaxRows' and 'byRowIndexes'",
                exception.getMessage());
    }

    @Test
    void testTransformUsingRegex()
    {
        String table = "|city    |rand |capital |founded |\n"
                     + "|Minsk   |2152 |true    |1067    |\n"
                     + "|Norilsk |1103 |false   |1920    |\n"
                     + "|Magadan |9843 |false   |1939    |\n";

        Properties properties = new Properties();
        properties.setProperty("column.city", "(?i).*?n.*");
        properties.setProperty("column.capital", "false");
        properties.setProperty("column.founded", "\\d9[23](0|9)");

        String transformed = transformer.transform(table, tableParsers,
                new TableProperties(parameterConverters, properties));

        String expected = "|city|rand|capital|founded|\n"
                        + "|Norilsk|1103|false|1920|\n"
                        + "|Magadan|9843|false|1939|";

        assertEquals(expected, transformed);
    }

    @Test
    void testTransformUsingRegexInvalidProperties()
    {
        Properties properties = new Properties();
        properties.setProperty("column.demo", ".*");
        properties.setProperty(BY_MAX_ROWS_PROPERTY, "2");

        TableProperties tableProperties = new TableProperties(parameterConverters, properties);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> transformer.transform(TABLE, tableParsers, tableProperties));
        assertEquals("Filtering by regex is not allowed to be used together with the following properties:"
                + " 'byMaxColumns', 'byColumnNames', 'byMaxRows', 'byRowIndexes'",
                exception.getMessage());
    }

    private static Properties createProperties(Integer columns, Integer byMaxRows, String byColumnNames,
                                               String byRowIndexes)
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
        if (byRowIndexes != null)
        {
            properties.setProperty("byRowIndexes", byRowIndexes);
        }
        return properties;
    }

    // CHECKSTYLE:OFF
    static Stream<Arguments> tableSource() {
        return Stream.of(
            Arguments.of(TABLE, createProperties(3, 3, null, null),           TABLE),
            Arguments.of(TABLE, createProperties(3, 5, null, null),           TABLE),
            Arguments.of(TABLE, createProperties(2, 2, null, null),           "|key1|key2|\n|1|2|\n|4|5|"),
            Arguments.of(TABLE, createProperties(null, 2, null, null),        "|key1|key2|key3|\n|1|2|3|\n|4|5|6|"),
            Arguments.of(TABLE, createProperties(2, null, null, null),        "|key1|key2|\n|1|2|\n|4|5|\n|7|8|"),
            Arguments.of(TABLE, createProperties(null, 2, "key1;key3", null), "|key1|key3|\n|1|3|\n|4|6|"),
            Arguments.of(TABLE, createProperties(null, 5, "key3;key2", null), "|key2|key3|\n|2|3|\n|5|6|\n|8|9|"),
            Arguments.of(TABLE, createProperties(null, null, null, "0;2"),    "|key1|key2|key3|\n|1|2|3|\n|7|8|9|"),
            Arguments.of(TABLE, createProperties(null, 1, "key1", null),      "|key1|\n|1|")
        );
    }
    // CHECKSTYLE:ON
}
