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

import static org.apache.commons.lang3.ObjectUtils.allNull;
import static org.apache.commons.lang3.ObjectUtils.anyNotNull;
import static org.apache.commons.lang3.ObjectUtils.anyNull;
import static org.apache.commons.lang3.Validate.isTrue;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.jbehave.core.model.ExamplesTable.TableProperties;
import org.jbehave.core.model.ExamplesTable.TableRows;
import org.jbehave.core.model.TableParsers;
import org.vividus.util.ExamplesTableProcessor;

public class FilteringTableTransformer extends AbstractFilteringTableTransformer
{
    private static final String BY_MAX_COLUMNS_PROPERTY = "byMaxColumns";
    private static final String BY_MAX_ROWS_PROPERTY = "byMaxRows";
    private static final String BY_ROW_INDEXES_PROPERTY = "byRowIndexes";
    private static final String BY_RANDOM_ROWS_PROPERTY = "byRandomRows";
    private static final String COLUMN_PREFIX = "column.";

    private static final String REGEX_FILTER_DECLARATION = COLUMN_PREFIX + "<regex placeholder>";
    private static final String CONFLICTING_PROPERTIES_MESSAGE =
            "Conflicting properties declaration found: '%s' and '%s'";

    @Override
    public String transform(String tableAsString, TableParsers tableParsers, TableProperties tableProperties)
    {
        Properties properties = tableProperties.getProperties();

        String byMaxColumns = properties.getProperty(BY_MAX_COLUMNS_PROPERTY);
        String byMaxRows = properties.getProperty(BY_MAX_ROWS_PROPERTY);
        String byColumnNames = properties.getProperty(BY_COLUMNS_NAMES_PROPERTY);
        String byRowIndexes = properties.getProperty(BY_ROW_INDEXES_PROPERTY);
        String byRandomRows = properties.getProperty(BY_RANDOM_ROWS_PROPERTY);
        Set<String> columnFilters = findColumnFilters(properties);

        isTrue(anyNotNull(byMaxColumns, byMaxRows, byColumnNames, byRowIndexes, byRandomRows)
                || !columnFilters.isEmpty(),
                "At least one of the following properties should be specified: '%s', '%s', '%s', '%s', '%s', '%s'",
                BY_MAX_COLUMNS_PROPERTY, BY_MAX_ROWS_PROPERTY, BY_COLUMNS_NAMES_PROPERTY, REGEX_FILTER_DECLARATION,
                BY_ROW_INDEXES_PROPERTY, BY_RANDOM_ROWS_PROPERTY);

        TableRows tableRows = tableParsers.parseRows(tableAsString, tableProperties);
        List<String> allColumnNames = tableRows.getHeaders();
        if (!columnFilters.isEmpty())
        {
            isTrue(allNull(byMaxColumns, byColumnNames, byMaxRows, byRowIndexes, byRandomRows),
                    "Filtering by regex is not allowed to be used together with the following properties:"
                            + " '%s', '%s', '%s', '%s', '%s'",
                    BY_MAX_COLUMNS_PROPERTY, BY_COLUMNS_NAMES_PROPERTY, BY_MAX_ROWS_PROPERTY, BY_ROW_INDEXES_PROPERTY,
                    BY_RANDOM_ROWS_PROPERTY);

            return ExamplesTableProcessor.buildExamplesTable(tableRows.getHeaders(),
                    filterRows(columnFilters, allColumnNames, tableRows.getRows(), properties), tableProperties);
        }

        isTrue(anyNull(byMaxColumns, byColumnNames), CONFLICTING_PROPERTIES_MESSAGE,
                BY_MAX_COLUMNS_PROPERTY, BY_COLUMNS_NAMES_PROPERTY);

        isTrue(anyNull(byMaxRows, byRowIndexes), CONFLICTING_PROPERTIES_MESSAGE,
                BY_MAX_ROWS_PROPERTY, BY_ROW_INDEXES_PROPERTY);

        isTrue(anyNull(byRandomRows, byRowIndexes), CONFLICTING_PROPERTIES_MESSAGE,
                BY_RANDOM_ROWS_PROPERTY, BY_ROW_INDEXES_PROPERTY);

        isTrue(anyNull(byRandomRows, byMaxRows), CONFLICTING_PROPERTIES_MESSAGE,
                BY_RANDOM_ROWS_PROPERTY, BY_MAX_ROWS_PROPERTY);

        List<String> filteredColumnNames = filterColumnNames(byMaxColumns, byColumnNames, allColumnNames);
        List<List<String>> filteredRows = filterRows(byMaxRows, byRowIndexes, byRandomRows, tableRows.getRows());
        filterRowsByColumnNames(allColumnNames, filteredRows, filteredColumnNames);

        return ExamplesTableProcessor.buildExamplesTable(filteredColumnNames, filteredRows, tableProperties);
    }

    private <T> List<T> filterRows(String byMaxRows, String byRowIndexes, String byRandomRows, List<T> rows)
    {
        int numberOfRows = rows.size();
        if (byRowIndexes == null && byRandomRows == null)
        {
            return Optional.ofNullable(byMaxRows)
                    .map(Integer::parseInt)
                    .filter(m -> m < numberOfRows)
                    .map(m -> rows.subList(0, m))
                    .orElse(rows);
        }
        else if (byRandomRows != null)
        {
            int randomRowsCount = Integer.parseInt(byRandomRows);
            isTrue(randomRowsCount <= numberOfRows,
                    "'byRandomRows' is %d, but it must be less than or equal to %d (the number of table rows)",
                    randomRowsCount, numberOfRows);
            return ThreadLocalRandom.current()
                    .ints(0, numberOfRows)
                    .distinct()
                    .limit(randomRowsCount)
                    .mapToObj(rows::get)
                    .collect(Collectors.toList());
        }
        else
        {
            return Stream.of(StringUtils.split(byRowIndexes, ';'))
                    .flatMapToInt(range ->
                    {
                        String[] rangeBounds = StringUtils.split(range, "-", 2);
                        int startInclusive = Integer.parseInt(rangeBounds[0].trim());
                        if (rangeBounds.length == 1)
                        {
                            return IntStream.of(startInclusive);
                        }
                        int endInclusive = Integer.parseInt(rangeBounds[1].trim());
                        isTrue(startInclusive <= endInclusive, "'byRowIndexes' has invalid range of indexes: %s."
                                + " The start index must be less than or equal to the end index.", range);
                        return IntStream.rangeClosed(startInclusive, endInclusive);
                    })
                    .mapToObj(rows::get)
                    .collect(Collectors.toList());
        }
    }

    private List<String> filterColumnNames(String byMaxColumns, String byColumnNames, List<String> allColumnNames)
    {
        if (byColumnNames != null)
        {
            return filterColumnNames(allColumnNames, byColumnNames);
        }
        if (byMaxColumns != null)
        {
            int columnsLimit = Math.min(allColumnNames.size(), Integer.parseInt(byMaxColumns));
            return allColumnNames.subList(0, columnsLimit);
        }
        return allColumnNames;
    }

    private static Set<String> findColumnFilters(Properties properties)
    {
        return properties.keySet()
                .stream()
                .map(String.class::cast)
                .filter(c -> c.startsWith(COLUMN_PREFIX))
                .collect(Collectors.toSet());
    }

    private List<List<String>> filterRows(Set<String> columnFilters, List<String> allColumnNames,
            List<List<String>> rows, Properties properties)
    {
        return columnFilters.stream().collect(
                Collectors.collectingAndThen(Collectors.toMap(k -> StringUtils.substringAfter(k, COLUMN_PREFIX),
                        k -> createFilter(properties.getProperty(k))),
                        filters -> filterRows(allColumnNames, rows, filters)));
    }

    private static Predicate<String> createFilter(String regex)
    {
        return Pattern.compile(regex).asPredicate();
    }

    public static List<List<String>> filterRows(List<String> allColumnNames, List<List<String>> rows,
            Map<String, Predicate<String>> columnFilters)
    {
        return rows.stream()
                   .filter(row -> applyFilters(allColumnNames, row, columnFilters))
                   .collect(Collectors.toList());
    }

    private static boolean applyFilters(List<String> allColumnNames, List<String> row,
            Map<String, Predicate<String>> columnFilters)
    {
        return IntStream.range(0, allColumnNames.size()).allMatch(i -> {
            String columnName = allColumnNames.get(i);
            Predicate<String> filter = columnFilters.get(columnName);
            return Optional.ofNullable(filter).map(f -> f.test(row.get(i))).orElse(true);
        });
    }
}
