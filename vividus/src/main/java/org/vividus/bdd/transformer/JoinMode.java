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

package org.vividus.bdd.transformer;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.model.ExamplesTable.TableProperties;
import org.vividus.bdd.util.ExamplesTableProcessor;
import org.vividus.bdd.util.MapUtils;

public enum JoinMode
{
    ROWS
    {
        @Override
        protected String join(ExamplesTable table, TableProperties properties)
        {
            Map<String, List<String>> tableMap = MapUtils.convertExamplesTableToMap(table);
            List<List<String>> tableData = tableMap.values().stream()
                    .map(column -> String.join(DELIMITER, column))
                    .map(List::of)
                    .collect(Collectors.toList());

            return ExamplesTableProcessor.buildExamplesTableFromColumns(tableMap.keySet(), tableData, properties);
        }
    },
    COLUMNS
    {
        @Override
        protected String join(ExamplesTable table, TableProperties properties)
        {
            String joinedColumn = properties.getMandatoryNonBlankProperty("joinedColumn");
            Set<String> columnsToJoin = getColumnsToJoin(table, properties);

            List<Map<String, String>> rows = table.getRows();
            List<String> headers = new ArrayList<>();
            List<List<String>> columns = new ArrayList<>();
            for (String header : table.getHeaders())
            {
                if (!columnsToJoin.contains(header))
                {
                    headers.add(header);
                    columns.add(buildColumn(rows, row -> row.get(header)));
                }
                else if (!headers.contains(joinedColumn))
                {
                    headers.add(joinedColumn);
                    columns.add(buildColumn(rows,
                        row -> columnsToJoin.stream().map(row::get).collect(Collectors.joining(DELIMITER))));
                }
            }

            return ExamplesTableProcessor.buildExamplesTableFromColumns(headers, columns, properties);
        }

        private Set<String> getColumnsToJoin(ExamplesTable table, TableProperties properties)
        {
            return Optional.ofNullable(properties.getProperties().getProperty("columnsToJoin"))
                    .map(columnsToJoin ->
                            Stream.of(columnsToJoin.split("(?<!\\\\);"))
                                    .map(column -> column.replace("\\;", ";"))
                                    .map(String::trim))
                    .orElseGet(() -> table.getHeaders().stream())
                    .collect(Collectors.toCollection(LinkedHashSet::new));
        }
    };

    private static final String DELIMITER = " ";

    private static List<String> buildColumn(List<Map<String, String>> rows,
            Function<Map<String, String>, String> cellBuilder)
    {
        return rows.stream().map(cellBuilder).collect(Collectors.toList());
    }

    protected abstract String join(ExamplesTable table, TableProperties properties);
}
