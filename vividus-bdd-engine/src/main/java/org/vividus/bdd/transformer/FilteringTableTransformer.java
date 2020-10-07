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

import static org.apache.commons.lang3.StringUtils.substringAfter;
import static org.apache.commons.lang3.Validate.isTrue;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.jbehave.core.model.ExamplesTable.TableProperties;
import org.jbehave.core.model.ExamplesTable.TableRows;
import org.jbehave.core.model.TableParsers;
import org.vividus.bdd.util.ExamplesTableProcessor;

@Named("FILTERING")
public class FilteringTableTransformer extends AbstractFilteringTableTransformer
{
    private static final String BY_MAX_COLUMNS_PROPERTY = "byMaxColumns";
    private static final String BY_MAX_ROWS_PROPERTY = "byMaxRows";
    private static final String BY_ROW_INDEXES_PROPERTY = "byRowIndexes";
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
        Set<String> columnFilters = findColumnFilters(properties);

        isTrue(byMaxColumns != null || byMaxRows != null || byColumnNames != null
                        || !columnFilters.isEmpty() || byRowIndexes != null,
                "At least one of the following properties should be specified: '%s', '%s', '%s', '%s', '%s'",
                BY_MAX_COLUMNS_PROPERTY, BY_MAX_ROWS_PROPERTY, BY_COLUMNS_NAMES_PROPERTY,
                REGEX_FILTER_DECLARATION, BY_ROW_INDEXES_PROPERTY);

        TableRows tableRows = tableParsers.parseRows(tableAsString, tableProperties);
        if (!columnFilters.isEmpty())
        {
            isTrue(byMaxColumns == null && byColumnNames == null && byMaxRows == null && byRowIndexes == null,
                    "Filtering by regex is not allowed to be used together with the following properties:"
                    + " '%s', '%s', '%s', '%s'",
                    BY_MAX_COLUMNS_PROPERTY, BY_COLUMNS_NAMES_PROPERTY, BY_MAX_ROWS_PROPERTY, BY_ROW_INDEXES_PROPERTY);

            return ExamplesTableProcessor.buildExamplesTable(tableRows.getHeaders(),
                    filterRows(columnFilters, tableRows.getRows(), properties), tableProperties);
        }

        isTrue(byMaxColumns == null || byColumnNames == null, CONFLICTING_PROPERTIES_MESSAGE,
                BY_MAX_COLUMNS_PROPERTY, BY_COLUMNS_NAMES_PROPERTY);

        isTrue(byMaxRows == null || byRowIndexes == null, CONFLICTING_PROPERTIES_MESSAGE,
                BY_MAX_ROWS_PROPERTY, BY_ROW_INDEXES_PROPERTY);

        List<String> filteredColumnNames = filterColumnNames(byMaxColumns, byColumnNames, tableRows.getHeaders());
        List<Map<String, String>> filteredRows = filterRows(byMaxRows, byRowIndexes, tableRows.getRows());
        filterRowsByColumnNames(filteredRows, filteredColumnNames);

        return ExamplesTableProcessor.buildExamplesTable(filteredColumnNames, filteredRows, tableProperties);
    }

    private List<Map<String, String>> filterRows(String byMaxRows, String byRowIndexes, List<Map<String, String>> rows)
    {
        if (byRowIndexes == null)
        {
            return Optional.ofNullable(byMaxRows)
                    .map(Integer::parseInt)
                    .filter(m -> m < rows.size())
                    .map(m -> rows.subList(0, m))
                    .orElse(rows);
        }
        else
        {
            return Stream.of(StringUtils.split(byRowIndexes, ';'))
                    .mapToInt(Integer::parseInt)
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

    private List<Map<String, String>> filterRows(Set<String> columnFilters, List<Map<String, String>> rows,
            Properties properties)
    {
        return columnFilters.stream().collect(Collectors.collectingAndThen(
                Collectors.toMap(k -> substringAfter(k, COLUMN_PREFIX), k -> createFilter(properties.getProperty(k))),
            filters -> filterRows(rows, filters)));
    }

    private static Predicate<String> createFilter(String regex)
    {
        return Pattern.compile(regex).asPredicate();
    }

    public static List<Map<String, String>> filterRows(List<Map<String, String>> rows,
            Map<String, Predicate<String>> columnFilters)
    {
        return rows.stream()
                   .filter(row -> row.entrySet()
                                     .stream()
                                     .allMatch(applyFilters(columnFilters)))
                   .collect(Collectors.toList());
    }

    private static Predicate<Map.Entry<String, String>> applyFilters(Map<String, Predicate<String>> columnFilters)
    {
        return col ->
        {
            Predicate<String> filter = columnFilters.get(col.getKey());
            return Optional.ofNullable(filter).map(f -> f.test(col.getValue())).orElse(true);
        };
    }
}
