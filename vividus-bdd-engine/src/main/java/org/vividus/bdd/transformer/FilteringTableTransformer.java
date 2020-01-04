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

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.jbehave.core.model.ExamplesTableProperties;
import org.jbehave.core.model.TableUtils;
import org.vividus.bdd.util.ExamplesTableProcessor;

@Named("FILTERING")
public class FilteringTableTransformer implements ExtendedTableTransformer
{
    private static final String BY_MAX_COLUMNS_PROPERTY = "byMaxColumns";
    private static final String BY_MAX_ROWS_PROPERTY = "byMaxRows";
    private static final String BY_COLUMNS_NAMES_PROPERTY = "byColumnNames";

    @Override
    public String transform(String tableAsString, ExamplesTableProperties properties)
    {
        String byMaxColumns = properties.getProperties().getProperty(BY_MAX_COLUMNS_PROPERTY);
        String byMaxRows = properties.getProperties().getProperty(BY_MAX_ROWS_PROPERTY);
        String byColumnNames = properties.getProperties().getProperty(BY_COLUMNS_NAMES_PROPERTY);
        checkArgument(byMaxColumns != null || byMaxRows != null || byColumnNames != null,
                "At least one of the following properties should be specified: '%s', '%s', '%s'",
                BY_MAX_COLUMNS_PROPERTY, BY_MAX_ROWS_PROPERTY, BY_COLUMNS_NAMES_PROPERTY);
        checkArgument(!(byMaxColumns != null && byColumnNames != null),
                "Conflicting properties declaration found: '%s' and '%s'",
                BY_MAX_COLUMNS_PROPERTY, BY_COLUMNS_NAMES_PROPERTY);

        List<String> tableRows = ExamplesTableProcessor.parseRows(tableAsString);
        List<String> headerValues = TableUtils.parseRow(tableRows.get(0), true, properties);
        List<Integer> filteredColumnIndexes = byColumnNames == null
                ? getLimitedColumns(headerValues, byMaxColumns) : getFilteredColumns(headerValues, byColumnNames);

        int resultDataRows = byMaxRows == null ? tableRows.size() - 1 : Integer.parseInt(byMaxRows);
        List<List<String>> updatedDataRows = ExamplesTableProcessor.parseDataRows(tableRows, properties)
                .stream()
                .limit(resultDataRows)
                .map(r -> filterRowByIndexes(filteredColumnIndexes, r))
                .collect(Collectors.toList());

        return ExamplesTableProcessor.buildExamplesTable(filterRowByIndexes(filteredColumnIndexes, headerValues),
                updatedDataRows, properties, true);
    }

    private List<Integer> getLimitedColumns(List<String> headerValues, String byMaxColumns)
    {
        int columnsLimit = byMaxColumns == null
                ? headerValues.size() : Math.min(headerValues.size(), Integer.parseInt(byMaxColumns));
        return IntStream.rangeClosed(0, columnsLimit - 1).boxed().collect(Collectors.toList());
    }

    private List<Integer> getFilteredColumns(List<String> headerValues, String byColumnNames)
    {
        return Arrays.stream(StringUtils.split(byColumnNames, ';'))
                .map(String::trim)
                .map(headerValues::indexOf)
                .filter(i -> i > -1)
                .collect(Collectors.toList());
    }

    private List<String> filterRowByIndexes(List<Integer> columnsToUse, List<String> row)
    {
        return row.stream().filter(e -> columnsToUse.contains(row.indexOf(e))).collect(Collectors.toList());
    }
}
