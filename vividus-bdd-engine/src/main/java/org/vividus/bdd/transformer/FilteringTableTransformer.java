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

import static org.apache.commons.lang3.Validate.isTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.jbehave.core.model.ExamplesTable.TableProperties;
import org.jbehave.core.model.ExamplesTable.TableRows;
import org.jbehave.core.model.TableParsers;
import org.vividus.bdd.util.ExamplesTableProcessor;

@Named("FILTERING")
public class FilteringTableTransformer implements ExtendedTableTransformer
{
    private static final String BY_MAX_COLUMNS_PROPERTY = "byMaxColumns";
    private static final String BY_MAX_ROWS_PROPERTY = "byMaxRows";
    private static final String BY_COLUMNS_NAMES_PROPERTY = "byColumnNames";

    @Override
    public String transform(String tableAsString, TableParsers tableParsers, TableProperties properties)
    {
        String byMaxColumns = properties.getProperties().getProperty(BY_MAX_COLUMNS_PROPERTY);
        String byMaxRows = properties.getProperties().getProperty(BY_MAX_ROWS_PROPERTY);
        String byColumnNames = properties.getProperties().getProperty(BY_COLUMNS_NAMES_PROPERTY);
        isTrue(byMaxColumns != null || byMaxRows != null || byColumnNames != null,
                "At least one of the following properties should be specified: '%s', '%s', '%s'",
                BY_MAX_COLUMNS_PROPERTY, BY_MAX_ROWS_PROPERTY, BY_COLUMNS_NAMES_PROPERTY);
        isTrue(!(byMaxColumns != null && byColumnNames != null),
                "Conflicting properties declaration found: '%s' and '%s'",
                BY_MAX_COLUMNS_PROPERTY, BY_COLUMNS_NAMES_PROPERTY);
        TableRows tableRows = tableParsers.parseRows(tableAsString, properties);

        List<String> filteredColumns = getFilteredHeaders(byMaxColumns, byColumnNames, tableRows.getHeaders());
        List<Map<String, String>> result = filterByHeaders(filteredColumns,
                getFilteredRows(byMaxRows, tableRows.getRows()));

        return ExamplesTableProcessor.buildExamplesTable(filteredColumns, result, properties);
    }

    private List<Map<String, String>> filterByHeaders(List<String> filteredColumns, List<Map<String, String>> result)
    {
        result.stream().map(m -> m.keySet().retainAll(filteredColumns)).collect(Collectors.toList());
        return result;
    }

    private List<Map<String, String>> getFilteredRows(String byMaxRows, List<Map<String, String>> list)
    {
        return Optional.ofNullable(byMaxRows)
                .map(Integer::parseInt)
                .filter(m -> m < list.size())
                .map(m -> list.subList(0, m))
                .orElse(list);
    }

    private List<String> getFilteredHeaders(String byMaxColumns, String byColumnNames, List<String> headerValues)
    {
        int columnsLimit = byMaxColumns == null
                ? headerValues.size() : Math.min(headerValues.size(), Integer.parseInt(byMaxColumns));
        List<String> filteredColumns = new ArrayList<>(headerValues);
        if (byColumnNames == null)
        {
            filteredColumns = filteredColumns.subList(0, columnsLimit);
        }
        else
        {
            List<String> columnNames = Stream.of(StringUtils.split(byColumnNames, ';'))
                    .map(String::trim).collect(Collectors.toList());
            filteredColumns.retainAll(columnNames);
        }
        return filteredColumns;
    }
}
