/*
 * Copyright 2019-2023 the original author or authors.
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

package org.vividus.transformer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.stream.Stream;

import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.model.ExamplesTable.TableProperties;
import org.jbehave.core.model.TableParsers;
import org.jbehave.core.steps.ParameterConverters;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.vividus.converter.FluentTrimmedEnumConverter;

class SortingTableTransformerTests
{
    private static final String TABLE_WITH_SAME_VALUES = "|key1|key2|\n|10|0|\n|1|0|";
    private static final String TABLE = "|key1|key2|\n|4|3|\n|10|0|";
    private static final String REVERSE_TABLE = "|key1|key2|\n|10|0|\n|4|3|";
    private static final String TABLE_STRING = "|key1|key2|key3|\n|4a|3|4|\n|10|0|1|";

    private final Keywords keywords = new Keywords();
    private final ParameterConverters parameterConverters = new ParameterConverters();
    private final TableParsers tableParsers = new TableParsers(parameterConverters);

    private final SortingTableTransformer transformer = new SortingTableTransformer(new FluentTrimmedEnumConverter());

    // CHECKSTYLE:OFF
    static Stream<Arguments> tableSource() {
        return Stream.of(
                arguments(TABLE_WITH_SAME_VALUES,              "byColumns=key2",                                         TABLE_WITH_SAME_VALUES),
                arguments("|key1|key2|key3|\n|0|2|1|\n|0|2|5|","byColumns=key1|key3",                                    "|key1|key2|key3|\n|0|2|5|\n|0|2|1|"),
                arguments(TABLE_STRING,                        "byColumns=key5, order=ascending",                        TABLE_STRING),
                arguments(REVERSE_TABLE,                       "byColumns=key1, order=ASCENDING",                        TABLE),
                arguments(TABLE,                               "byColumns=key1, order=DESCENDING",                       REVERSE_TABLE),
                arguments(TABLE,                               "byColumns=key1, order=DESCENDING",                       REVERSE_TABLE),
                arguments(TABLE,                               "byColumns=key1, order=ASCENDING, sortingTypes = number", TABLE),
                arguments(REVERSE_TABLE,                "byColumns=key1, order=ASCENDING, sortingTypes = string",        TABLE),
                arguments("|key1|key2|key3|\n|a|2|1|\n|a|2|5|","byColumns=key1|key3, sortingTypes = string|number",      "|key1|key2|key3|\n|a|2|5|\n|a|2|1|")
        );
    }
    // CHECKSTYLE:ON

    @ParameterizedTest
    @MethodSource("tableSource")
    void testTransform(String expectedTable, String propertiesAsString, String tableToTransform)
    {
        var tableProperties = new TableProperties(propertiesAsString, keywords, parameterConverters);
        assertEquals(expectedTable, transformer.transform(tableToTransform, tableParsers, tableProperties));
    }

    @Test
    void testFailOnMissingTableProperty()
    {
        var tableProperties = new TableProperties("", keywords, parameterConverters);
        var exception = assertThrows(IllegalArgumentException.class,
            () -> transformer.transform(TABLE, tableParsers, tableProperties));
        assertEquals("'byColumns' is not set in ExamplesTable properties", exception.getMessage());
    }

    @Test
    void testFailOnUnsupportedValueForNumber()
    {
        var tableProperties = new TableProperties("byColumns=key1, sortingTypes = NUMBER", keywords,
                parameterConverters);
        var exception = assertThrows(IllegalArgumentException.class,
            () -> transformer.transform(TABLE_STRING, tableParsers, tableProperties));
        assertEquals("NUMBER sorting type supports only number values for comparison. Character a is neither a"
                + " decimal digit number, decimal point, nor \"e\" notation exponential mark.", exception.getMessage());
    }

    @Test
    void testFailOnDifferentCountForSortingTypesAndByColumns()
    {
        var tableProperties = new TableProperties("byColumns=key1|key2|key3, sortingTypes = String|number", keywords,
                parameterConverters);
        var exception = assertThrows(IllegalArgumentException.class,
            () -> transformer.transform(TABLE_STRING, tableParsers, tableProperties));
        assertEquals("Please, specify parameter 'sortingType' (String|number) with the same count of types as count"
                + " of column names in parameter 'byColumns' (key1|key2|key3)",
                exception.getMessage());
    }
}
