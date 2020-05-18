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

package org.vividus.bdd.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Stream;

import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.model.ExamplesTable.ExamplesTableProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ExamplesTableProcessorTests
{
    private static final String ZERO = "0";
    private static final String ONE = "1";
    private static final String THREE = "3";
    private static final String FOUR = "4";
    private static final List<String> VALUES1 = List.of(FOUR, THREE);
    private static final List<String> VALUES2 = List.of(ONE, ZERO);
    private static final String KEY_1 = "key1";
    private static final String KEY_2 = "key2";
    private static final List<String> KEYS = List.of(KEY_1, KEY_2);
    private static final String TABLE = "|key1|key2|\n|4|3|\n|1|0|";

    @ParameterizedTest
    @MethodSource("tableToBuildSource")
    void testBuildTable(String expectedTable, List<List<String>> rows)
    {
        assertEquals(expectedTable, ExamplesTableProcessor.buildExamplesTable(KEYS, rows, createProperties(), true));
    }

    static Stream<Arguments> tableToBuildSource()
    {
        return Stream.of(
            Arguments.of(TABLE, List.of(VALUES1, VALUES2)),
            Arguments.of("|key1|key2|\n!4!3!\n!1|!0!", List.of(VALUES1, List.of("1|", ZERO))),
            Arguments.of("|key1|key2|\n#11|#12!#\n#13?#14$#", List.of(List.of("11|", "12!"), List.of("13?", "14$"))));
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
    void testAsDataRowsExamplesTable()
    {
        assertEquals(List.of(VALUES1, VALUES2), ExamplesTableProcessor.asDataRows(new ExamplesTable(TABLE)));
    }

    @Test
    void testAsDataRowsMap()
    {
        Map<String, String> row1 = new LinkedHashMap<>();
        row1.put(KEY_1, FOUR);
        row1.put(KEY_2, THREE);
        Map<String, String> row2 = new LinkedHashMap<>();
        row2.put(KEY_1, ONE);
        row2.put(KEY_2, ZERO);
        assertEquals(List.of(VALUES1, VALUES2), ExamplesTableProcessor.asDataRows(List.of(row1, row2)));
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
        Properties pp = new Properties();
        pp.setProperty("valueSeparator", "!");
        ExamplesTableProperties p = new ExamplesTableProperties(pp);
        assertEquals(expectedTable,
                ExamplesTableProcessor.buildExamplesTable(KEYS, List.of(values), p, false, true));
    }

    private ExamplesTableProperties createProperties()
    {
        return new ExamplesTableProperties(new Properties());
    }
}
