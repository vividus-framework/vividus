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

package org.vividus.util.comparison;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.hamcrest.collection.IsIterableWithSize;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.vividus.util.comparison.ComparisonUtils.EntryComparisonResult;

class ComparisonUtilsTests
{
    private static final String FIFTH_COLUMN = "5column";
    private static final String FOURTH_COLUMN = "4Column";
    private static final String THIRD_COLUMN = "3Column";
    private static final String COMPARISON_RESULT = "comparison result";
    private static final String SECOND_COLUMN = "2Column";
    private static final String FIRST_COLUMN = "1Column";
    private static final String ONE = "1.0";

    @Test
    void shouldCompareTwoMaps()
    {
        List<EntryComparisonResult> results = ComparisonUtils.compareMaps(
                Map.of(FIRST_COLUMN, 1, SECOND_COLUMN, 1, THIRD_COLUMN, 1.01d, FOURTH_COLUMN, 1, FIFTH_COLUMN, ONE),
                Map.of(FIRST_COLUMN, 1.000d, SECOND_COLUMN, 1, THIRD_COLUMN, 1.0f, FOURTH_COLUMN, ONE, FIFTH_COLUMN, 1))
                .stream()
                .sorted(Comparator.comparing(e -> e.getKey().toString(), Comparator.naturalOrder()))
                .collect(Collectors.toList());
        EntryComparisonResult result1 = results.get(0);
        EntryComparisonResult result2 = results.get(1);
        EntryComparisonResult result3 = results.get(2);
        EntryComparisonResult result4 = results.get(3);
        BigDecimal expected = new BigDecimal(ONE);
        Assertions.assertAll(COMPARISON_RESULT,
            () -> Assertions.assertTrue(result1.isPassed()),
            () -> Assertions.assertEquals(FIRST_COLUMN, result1.getKey()),
            () -> Assertions.assertEquals(expected, result1.getLeft()),
            () -> Assertions.assertEquals(expected, result1.getRight()),
            () -> Assertions.assertTrue(result2.isPassed()),
            () -> Assertions.assertEquals(SECOND_COLUMN, result2.getKey()),
            () -> Assertions.assertEquals(BigDecimal.ONE, result2.getLeft()),
            () -> Assertions.assertEquals(BigDecimal.ONE, result2.getRight()),
            () -> Assertions.assertFalse(result3.isPassed()),
            () -> Assertions.assertEquals(THIRD_COLUMN, result3.getKey()),
            () -> Assertions.assertEquals(new BigDecimal("1.01"), result3.getLeft()),
            () -> Assertions.assertEquals(new BigDecimal("1.00"), result3.getRight()),
            () -> Assertions.assertFalse(result4.isPassed()),
            () -> Assertions.assertEquals(FOURTH_COLUMN, result4.getKey()),
            () -> Assertions.assertEquals(BigDecimal.ONE, result4.getLeft()),
            () -> Assertions.assertEquals(ONE, result4.getRight()));
    }

    @Test
    void shouldCheckMapContainsSubMap()
    {
        List<EntryComparisonResult> results = ComparisonUtils.checkMapContainsSubMap(
                Map.of(FIRST_COLUMN, 1, SECOND_COLUMN, 1, FOURTH_COLUMN, 1, FIFTH_COLUMN, ONE),
                Map.of(FIRST_COLUMN, 1.000d, SECOND_COLUMN, 1, THIRD_COLUMN, 1.0f, FOURTH_COLUMN, ONE))
                .stream()
                .sorted(Comparator.comparing(e -> e.getKey().toString(), Comparator.naturalOrder()))
                .collect(Collectors.toList());
        EntryComparisonResult result1 = results.get(0);
        EntryComparisonResult result2 = results.get(1);
        EntryComparisonResult result3 = results.get(2);
        EntryComparisonResult result4 = results.get(3);
        BigDecimal expected = new BigDecimal(ONE);
        Assertions.assertAll(COMPARISON_RESULT,
            () -> Assertions.assertTrue(result1.isPassed()),
            () -> Assertions.assertEquals(FIRST_COLUMN, result1.getKey()),
            () -> Assertions.assertEquals(expected, result1.getLeft()),
            () -> Assertions.assertEquals(expected, result1.getRight()),
            () -> Assertions.assertTrue(result2.isPassed()),
            () -> Assertions.assertEquals(SECOND_COLUMN, result2.getKey()),
            () -> Assertions.assertEquals(BigDecimal.ONE, result2.getLeft()),
            () -> Assertions.assertEquals(BigDecimal.ONE, result2.getRight()),
            () -> Assertions.assertFalse(result3.isPassed()),
            () -> Assertions.assertEquals(THIRD_COLUMN, result3.getKey()),
            () -> Assertions.assertEquals(expected, result3.getLeft()),
            () -> Assertions.assertNull(result3.getRight()),
            () -> Assertions.assertFalse(result4.isPassed()),
            () -> Assertions.assertEquals(FOURTH_COLUMN, result4.getKey()),
            () -> Assertions.assertEquals(ONE, result4.getLeft()),
            () -> Assertions.assertEquals(BigDecimal.ONE, result4.getRight()));
    }

    @Test
    void shouldCompareTwoMapsAndTreatComparingWithNullAsFalse()
    {
        List<EntryComparisonResult> results = ComparisonUtils.compareMaps(mapOf(FIRST_COLUMN, null),
                mapOf(FIRST_COLUMN, 1));
        EntryComparisonResult result = results.get(0);
        Assertions.assertAll(COMPARISON_RESULT,
            () -> Assertions.assertFalse(result.isPassed()),
            () -> Assertions.assertEquals(FIRST_COLUMN, result.getKey()),
            () -> Assertions.assertNull(result.getLeft()),
            () -> Assertions.assertEquals(BigDecimal.ONE, result.getRight()),
            () -> Assertions.assertEquals("null", result.getLeftClassName()),
            () -> Assertions.assertEquals("java.math.BigDecimal", result.getRightClassName()));
    }

    @Test
    void shouldCompareTwoMapsAndTreatComparisonOfNullValuesAsTrue()
    {
        List<EntryComparisonResult> results = ComparisonUtils.compareMaps(mapOf(FIRST_COLUMN, null),
                mapOf(FIRST_COLUMN, null));
        EntryComparisonResult result = results.get(0);
        Assertions.assertAll(COMPARISON_RESULT,
            () -> Assertions.assertTrue(result.isPassed()),
            () -> Assertions.assertEquals(FIRST_COLUMN, result.getKey()),
            () -> Assertions.assertNull(result.getLeft()),
            () -> Assertions.assertNull(result.getRight()));
    }

    @Test
    void shouldReturnEmptyResultForEmptyTables()
    {
        List<List<EntryComparisonResult>> result = ComparisonUtils.compareListsOfMaps(List.of(Map.of()),
                List.of(Map.of()));
        assertThat(result.get(0), is(empty()));
    }

    @Test
    void shouldCompareMapsAndReturnResultForEmptyTables()
    {
        List<List<EntryComparisonResult>> result = ComparisonUtils.compareListsOfMaps(List.of(Map.of(FIRST_COLUMN, 1)),
                List.of(Map.of(SECOND_COLUMN, 1)));
        List<EntryComparisonResult> firstRowResult = result.get(0);
        EntryComparisonResult result1 = firstRowResult.get(0);
        EntryComparisonResult result2 = firstRowResult.get(1);
        Assertions.assertAll(COMPARISON_RESULT,
            () -> Assertions.assertFalse(result1.isPassed()),
            () -> Assertions.assertEquals(FIRST_COLUMN, result1.getKey()),
            () -> Assertions.assertEquals(BigDecimal.ONE, result1.getLeft()),
            () -> Assertions.assertNull(result1.getRight()),
            () -> Assertions.assertFalse(result2.isPassed()),
            () -> Assertions.assertEquals(SECOND_COLUMN, result2.getKey()),
            () -> Assertions.assertNull(result2.getLeft()),
            () -> Assertions.assertEquals(BigDecimal.ONE, result2.getRight()));
        assertThat(firstRowResult, IsIterableWithSize.iterableWithSize(2));
    }

    private <K, V> Map<K, V> mapOf(K k, V v)
    {
        Map<K, V> map = new HashMap<>();
        map.put(k, v);
        return map;
    }
}
