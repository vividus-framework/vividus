/*
 * Copyright 2019-2024 the original author or authors.
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

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.jbehave.core.model.ExamplesTable.TableProperties;
import org.jbehave.core.model.ExamplesTable.TableRows;
import org.jbehave.core.model.TableParsers;
import org.vividus.util.ExamplesTableProcessor;

public class DistinctingTableTransformer extends AbstractFilteringTableTransformer
{
    @Override
    public String transform(String tableAsString, TableParsers tableParsers, TableProperties tableProperties)
    {
        String byColumnNames = tableProperties.getMandatoryNonBlankProperty(BY_COLUMNS_NAMES_PROPERTY, String.class);
        String keepAllColumns = tableProperties.getProperties().getProperty("keepAllColumns");

        TableRows tableRows = tableParsers.parseRows(tableAsString, tableProperties);
        List<String> allColumnNames = tableRows.getHeaders();
        List<String> filteredColumnNames = filterColumnNames(allColumnNames, byColumnNames);
        List<List<String>> rows = tableRows.getRows();

        Collection<String> headers;
        Collection<List<String>> distinctRows;
        if (Boolean.parseBoolean(keepAllColumns))
        {
            int[] indexesToDistinct = IntStream.range(0, allColumnNames.size())
                    .filter(i -> filteredColumnNames.contains(allColumnNames.get(i)))
                    .sorted()
                    .toArray();

            distinctRows = rows.stream().collect(Collectors.toMap(
                    r -> IntStream.of(indexesToDistinct).mapToObj(r::get).toList(),
                    Function.identity(),
                    (r1, r2) -> r1,
                    LinkedHashMap::new
            )).values();
            headers = allColumnNames;
        }
        else
        {
            filterRowsByColumnNames(allColumnNames, rows, filteredColumnNames);
            distinctRows = new LinkedHashSet<>(rows);
            headers = filteredColumnNames;
        }
        return ExamplesTableProcessor.buildExamplesTable(headers, distinctRows, tableProperties);
    }
}
