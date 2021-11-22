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

package org.vividus.azure.storage.blob;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.hamcrest.Matcher;
import org.hamcrest.core.IsEqual;
import org.hamcrest.core.StringRegularExpression;
import org.jbehave.core.model.ExamplesTable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class ExamplesTableToBlobFilterConverterTests
{
    private static final String BLOB_NAME_FILTER_RULE = "blobNameFilterRule";
    private static final String BLOB_NAME_FILTER_VALUE = "blobNameFilterValue";
    private static final String RESULTS_LIMIT = "resultsLimit";

    private final ExamplesTableToBlobFilterConverter converter = new ExamplesTableToBlobFilterConverter();

    @Test
    void shouldConvertExamplesTableWithAllFiltersToBlobFilter()
    {
        var blobNamePrefix = "abc/";
        var examplesTable = ExamplesTable.empty().withRows(List.of(Map.of(
                "blobNamePrefix", blobNamePrefix,
                BLOB_NAME_FILTER_RULE, "MATCHES",
                BLOB_NAME_FILTER_VALUE, "\\d{4}",
                RESULTS_LIMIT, "28"
        )));
        var blobFilter = converter.convertValue(examplesTable, null);
        assertEquals(Optional.of(blobNamePrefix), blobFilter.getBlobNamePrefix());
        assertEquals(Optional.of(28), blobFilter.getResultsLimit());
        var blobNameMatcherOptional = blobFilter.getBlobNameMatcher();
        assertTrue(blobNameMatcherOptional.isPresent());
        Matcher<String> blobNameMatcher = blobNameMatcherOptional.get();
        assertThat(blobNameMatcher, instanceOf(StringRegularExpression.class));
        assertTrue(blobNameMatcher.matches("2021"));
    }

    @Test
    void shouldConvertExamplesTableWithFilterMatcherToBlobFilter()
    {
        var filterValue = "blob";
        var examplesTable = ExamplesTable.empty().withRows(List.of(Map.of(
                BLOB_NAME_FILTER_RULE, "IS_EQUAL_TO",
                BLOB_NAME_FILTER_VALUE, filterValue
        )));
        var blobFilter = converter.convertValue(examplesTable, null);
        assertEquals(Optional.empty(), blobFilter.getBlobNamePrefix());
        assertEquals(Optional.empty(), blobFilter.getResultsLimit());
        var blobNameMatcherOptional = blobFilter.getBlobNameMatcher();
        assertTrue(blobNameMatcherOptional.isPresent());
        Matcher<String> blobNameMatcher = blobNameMatcherOptional.get();
        assertThat(blobNameMatcher, instanceOf(IsEqual.class));
        assertTrue(blobNameMatcher.matches(filterValue));
    }

    @Test
    void shouldConvertExamplesTableWithResultsLimitToBlobFilter()
    {
        var examplesTable = ExamplesTable.empty().withRows(List.of(Map.of(RESULTS_LIMIT, "5"
        )));
        var blobFilter = converter.convertValue(examplesTable, null);
        assertEquals(Optional.of(5), blobFilter.getResultsLimit());
        assertEquals(Optional.empty(), blobFilter.getBlobNameMatcher());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 2})
    void shouldThrowErrorOnInvalidNumberOfRowsInExamplesTable(int actualNumberOfRows)
    {
        var examplesTable = new ExamplesTable(
                IntStream.range(0, actualNumberOfRows + 1)
                        .mapToObj(i -> "||")
                        .collect(Collectors.joining("\n", "header", ""))
        );
        var exception = assertThrows(IllegalArgumentException.class, () -> converter.convertValue(examplesTable, null));
        assertEquals("Exactly one row is expected in ExamplesTable representing blob filter, but found "
                + actualNumberOfRows, exception.getMessage());
    }

    @Test
    void shouldThrowErrorOnMissingFiltersInExamplesTable()
    {
        var examplesTable = ExamplesTable.empty().withRows(List.of(Map.of()));
        var exception = assertThrows(IllegalArgumentException.class, () -> converter.convertValue(examplesTable, null));
        assertEquals("At least one filter must be specified", exception.getMessage());
    }

    static Stream<Arguments> invalidFilters()
    {
        return Stream.of(
                arguments(Map.of(BLOB_NAME_FILTER_RULE, "CONTAINS")),
                arguments(Map.of(BLOB_NAME_FILTER_VALUE, ".*"))
        );
    }

    @ParameterizedTest
    @MethodSource("invalidFilters")
    void shouldThrowErrorOnMissingFilterNameRuleOrValueInExamplesTable(Map<String, String> row)
    {
        var examplesTable = ExamplesTable.empty().withRows(List.of(row));
        var exception = assertThrows(IllegalArgumentException.class, () -> converter.convertValue(examplesTable, null));
        assertEquals("'blobNameFilterRule' and 'blobNameFilterValue' must be specified in conjunction",
                exception.getMessage());
    }
}
