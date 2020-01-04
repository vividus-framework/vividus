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

import java.util.List;
import java.util.Map;

import org.jbehave.core.model.ExamplesTable;
import org.junit.jupiter.api.Test;

class MapUtilsTests
{
    private static final String SHOULD_BE_CONVERTED_INTO_MAP_WITH_SIZE = "Should be converted into map with size ";
    private static final String KEY1 = "key1";
    private static final String KEY2 = "key2";
    private static final String KEY3 = "key3";
    private static final String VALUE1 = "value1";
    private static final String VALUE2 = "value2";
    private static final String VALUE3 = "value3";
    private static final String SINGLE_ROW_TABLE = "|key1|key2|key3|\n|value1|value2|value3|";

    @Test
    void testConvertExamplesTableToMapSimple()
    {
        int expectedMapSize = 3;
        ExamplesTable exTable = new ExamplesTable(SINGLE_ROW_TABLE);
        Map<String, ?> actualMap = MapUtils.convertExamplesTableToMap(exTable);
        assertEquals(expectedMapSize, actualMap.size(), SHOULD_BE_CONVERTED_INTO_MAP_WITH_SIZE + expectedMapSize);
        assertEquals(List.of(VALUE1), actualMap.get(KEY1));
        assertEquals(List.of(VALUE2), actualMap.get(KEY2));
        assertEquals(List.of(VALUE3), actualMap.get(KEY3));
    }

    @Test
    void testConvertExamplesTableToMapMultiline()
    {
        int expectedMapSize = 3;
        ExamplesTable exTable = new ExamplesTable("|key1|key2|key3|\n|value1|value2|value3|\n|valueA|valueB|valueC|");
        Map<String, ?> actualMap = MapUtils.convertExamplesTableToMap(exTable);
        assertEquals(expectedMapSize, actualMap.size(), SHOULD_BE_CONVERTED_INTO_MAP_WITH_SIZE + expectedMapSize);
        assertEquals(List.of(VALUE1, "valueA"), actualMap.get(KEY1));
        assertEquals(List.of(VALUE2, "valueB"), actualMap.get(KEY2));
        assertEquals(List.of(VALUE3, "valueC"), actualMap.get(KEY3));
    }

    @Test
    void testConvertEmptyExamplesTableToMap()
    {
        int expectedMapSize = 0;
        ExamplesTable exTable = ExamplesTable.EMPTY;
        Map<String, ?> actualMap = MapUtils.convertExamplesTableToMap(exTable);
        assertEquals(expectedMapSize, actualMap.size(), SHOULD_BE_CONVERTED_INTO_MAP_WITH_SIZE + expectedMapSize);
    }

    @Test
    void testConvertSingleRowExamplesTableToMap()
    {
        int expectedMapSize = 3;
        ExamplesTable exTable = new ExamplesTable(SINGLE_ROW_TABLE);
        Map<String, String> actualMap = MapUtils.convertSingleRowExamplesTableToMap(exTable);
        assertEquals(expectedMapSize, actualMap.size(), SHOULD_BE_CONVERTED_INTO_MAP_WITH_SIZE + expectedMapSize);
        assertEquals(VALUE1, actualMap.get(KEY1));
        assertEquals(VALUE2, actualMap.get(KEY2));
        assertEquals(VALUE3, actualMap.get(KEY3));
    }

    @Test
    void testConvertSingleRowExamplesTableToMapMultiRows()
    {
        ExamplesTable exTable = new ExamplesTable("|key1|key2|\n|value1|value2|\n|valueA|valueB|");
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> MapUtils
                .convertSingleRowExamplesTableToMap(exTable));
        assertEquals("ExamplesTable should contain single row with values", exception.getMessage());
    }
}
