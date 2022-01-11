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

package org.vividus.transformer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.model.ExamplesTable.TableProperties;
import org.jbehave.core.model.TableParsers;
import org.jbehave.core.steps.ParameterConverters;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FilteringTableTransformerTests
{
    private static final String TABLE = "|key1|key2|key3|\n|1|2|3|\n|4|5|6|\n|7|8|9|";

    private final Keywords keywords = new Keywords();
    private final ParameterConverters parameterConverters = new ParameterConverters();
    private final TableParsers tableParsers = new TableParsers(parameterConverters);
    private final FilteringTableTransformer transformer = new FilteringTableTransformer();

    static Stream<Arguments> tableSource()
    {
        return Stream.of(
                arguments("byMaxColumns=3, byMaxRows=3",          TABLE),
                arguments("byMaxColumns=3, byMaxRows=5",          TABLE),
                arguments("byMaxColumns=2, byMaxRows=2",          "|key1|key2|\n|1|2|\n|4|5|"),
                arguments("byMaxRows=2",                          "|key1|key2|key3|\n|1|2|3|\n|4|5|6|"),
                arguments("byMaxColumns=2",                       "|key1|key2|\n|1|2|\n|4|5|\n|7|8|"),
                arguments("byMaxRows=2, byColumnNames=key1;key3", "|key1|key3|\n|1|3|\n|4|6|"),
                arguments("byMaxRows=5, byColumnNames=key3;key2", "|key2|key3|\n|2|3|\n|5|6|\n|8|9|"),
                arguments("byRowIndexes=0;2",                     "|key1|key2|key3|\n|1|2|3|\n|7|8|9|"),
                arguments("byMaxRows=1, byColumnNames=key1",      "|key1|\n|1|")
        );
    }

    @ParameterizedTest
    @MethodSource("tableSource")
    void testTransform(String propertiesAsString, String expectedTable)
    {
        var tableProperties = new TableProperties(propertiesAsString, keywords, parameterConverters);
        assertEquals(expectedTable, transformer.transform(TABLE, tableParsers, tableProperties));
    }

    @Test
    void testTransformUnorderedHeader()
    {
        String table = "|key2|key3|key1|\n|2|3|1|\n|4|5|6|\n|7|8|9|";
        var tableProperties = new TableProperties("byMaxRows=1", keywords, parameterConverters);
        assertEquals("|key2|key3|key1|\n|2|3|1|", transformer.transform(table, tableParsers, tableProperties));
    }

    @Test
    void testTransformRandomRows()
    {
        var tableProperties = new TableProperties("byRandomRows=1", keywords, parameterConverters);
        String table = "|key2|key3|key1|\n|1|2|3|\n|4|5|6|\n|7|8|9|\n|10|11|12|\n|13|14|15|\n|16|17|18|\n|19|20|21|"
                + "\n|22|23|24|\n|25|26|27|\n|28|29|30|";
        List<String> transformerRandomTables = new ArrayList<>();
        Map<String, Integer> randomTablesCount = new HashMap<>();
        for (int i = 0; i < 1000; i++)
        {
            transformerRandomTables.add(transformer.transform(table, tableParsers, tableProperties));
        }
        for (String tableStr : transformerRandomTables)
        {
            randomTablesCount.put(tableStr, Collections.frequency(transformerRandomTables, tableStr));
        }
        Integer minCountSameRows = Collections.min(randomTablesCount.values());
        Integer maxCountSameRows = Collections.max(randomTablesCount.values());
        int maxThreshold = 70;
        assertThat(maxCountSameRows - minCountSameRows, lessThanOrEqualTo(maxThreshold));
    }

    @ParameterizedTest
    // CHECKSTYLE:OFF
    // @formatter:off
    @CsvSource({
            "'',                                   'At least one of the following properties should be specified: ''byMaxColumns'', ''byMaxRows'', ''byColumnNames'', ''column.<regex placeholder>'', ''byRowIndexes'', ''byRandomRows'''",
            "'byMaxColumns=1, byColumnNames=key2', Conflicting properties declaration found: 'byMaxColumns' and 'byColumnNames'",
            "'byMaxRows=1, byRowIndexes=1;2',      Conflicting properties declaration found: 'byMaxRows' and 'byRowIndexes'",
            "'byRandomRows=1, byMaxRows=1',        Conflicting properties declaration found: 'byRandomRows' and 'byMaxRows'",
            "'byRandomRows=1, byRowIndexes=1;2',   Conflicting properties declaration found: 'byRandomRows' and 'byRowIndexes'",
            "'column.demo=.*, byMaxRows=2',        'Filtering by regex is not allowed to be used together with the following properties: ''byMaxColumns'', ''byColumnNames'', ''byMaxRows'', ''byRowIndexes'', ''byRandomRows'''",
            "'byRandomRows=4',                     'byRandomRows' must be less than or equal to the number of table rows",
            "'byColumnNames=key11;key2;key13',     'byColumnNames' refers columns missing in ExamplesTable: key11; key13"
    })
    // CHECKSTYLE:ON
    // @formatter:on
    void shouldHandleInvalidInputs(String propertiesAsString, String errorMessage)
    {
        var tableProperties = new TableProperties(propertiesAsString, keywords, parameterConverters);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> transformer.transform(TABLE, tableParsers, tableProperties));
        assertEquals(errorMessage, exception.getMessage());
    }

    @Test
    void testTransformUsingRegex()
    {
        var table = "|city    |rand |capital |founded |\n"
                     + "|Minsk   |2152 |true    |1067    |\n"
                     + "|Norilsk |1103 |false   |1920    |\n"
                     + "|Magadan |9843 |false   |1939    |\n";

        var tableProperties = new TableProperties(
                "column.city=(?i).*?n.*, column.capital=false, column.founded=\\d9[23](0|9)", keywords,
                parameterConverters);
        String transformed = transformer.transform(table, tableParsers, tableProperties);

        var expected = "|city|rand|capital|founded|\n"
                        + "|Norilsk|1103|false|1920|\n"
                        + "|Magadan|9843|false|1939|";

        assertEquals(expected, transformed);
    }
}
