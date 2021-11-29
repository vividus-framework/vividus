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

package org.vividus.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.model.ExamplesTable.TableProperties;
import org.jbehave.core.steps.ParameterConverters;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ExamplesTableProcessorTests
{
    private static final String ZERO = "0";
    private static final List<String> VALUES1 = List.of("4", "3");
    private static final List<String> VALUES2 = List.of("1", ZERO);
    private static final List<String> KEYS = List.of("key1", "key2");
    private static final String TABLE = "|key1|key2|\n|4|3|\n|1|0|";

    private final Keywords keywords = new Keywords();
    private final ParameterConverters parameterConverters = new ParameterConverters();

    @ParameterizedTest
    @MethodSource("tableToBuildSource")
    void testBuildTable(String expectedTable, List<List<String>> rows)
    {
        assertEquals(expectedTable, ExamplesTableProcessor.buildExamplesTable(KEYS, rows, createProperties(), true));
    }

    static Stream<Arguments> tableToBuildSource()
    {
        List<String> row = new ArrayList<>();
        row.add(null);
        row.add(ZERO);
        return Stream.of(
            Arguments.of(TABLE, List.of(VALUES1, VALUES2)),
            Arguments.of("|key1|key2|\n!4!3!\n!1|!0!", List.of(VALUES1, List.of("1|", ZERO))),
            Arguments.of("|key1|key2|\n#11|#12!#\n#13?#14$#", List.of(List.of("11|", "12!"), List.of("13?", "14$"))),
            Arguments.of("|key1|key2|\n|null|0|", List.of(row))
        );
    }

    @Test
    void testBuildExamplesTableException()
    {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> ExamplesTableProcessor
                .buildExamplesTable(KEYS, List.of(List.of("0|", "1!"), List.of("2?", "3$"), List.of("4#", "5%"),
                        List.of("6*", "7*")), createProperties(), true));
        assertEquals("There are not alternative value separators applicable for examples table",
                exception.getMessage());
    }

    @Test
    void testBuildExamplesTableFromColumns()
    {
        String expectedTable = "|key1|key2|\n|4|1|\n|3|0|";
        assertEquals(expectedTable, ExamplesTableProcessor.buildExamplesTableFromColumns(KEYS,
                List.of(VALUES1, VALUES2), createProperties()));
    }

    @Test
    void testBuildExamplesTableCheckForValueSeparatorIsFalse()
    {
        List<String> values = List.of("v\\|a1", "val\\|2");
        String expectedTable = "|key1|key2|\n|v\\|a1|val\\|2|";
        assertEquals(expectedTable,
                ExamplesTableProcessor.buildExamplesTable(KEYS, List.of(values), createProperties(), false));
    }

    @Test
    void testBuildExamplesTableFromColumnsException()
    {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> ExamplesTableProcessor
                .buildExamplesTableFromColumns(KEYS, List.of(VALUES1, List.of(ZERO)), createProperties()));
        assertEquals("Columns are not aligned: column 'key1' has 2 value(s), column 'key2' has 1 value(s)",
                exception.getMessage());
    }

    @Test
    void testBuildExamplesTableAppendTablePropertiesTrue()
    {
        List<String> values = List.of("a1", "a2");
        String expectedTable = "{valueSeparator=!}\n|key1|key2|\n!a1!a2!";
        TableProperties tableProperties = new TableProperties("valueSeparator=!", keywords, parameterConverters);
        assertEquals(expectedTable,
                ExamplesTableProcessor.buildExamplesTable(KEYS, List.of(values), tableProperties, false, true));
    }

    private TableProperties createProperties()
    {
        return new TableProperties("", keywords, parameterConverters);
    }
}
