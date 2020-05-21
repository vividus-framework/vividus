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

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.jbehave.core.model.ExamplesTable.TableProperties;
import org.jbehave.core.model.TableParsers;
import org.vividus.bdd.util.ExamplesTableProcessor;

@Named("SORTING")
public class SortingTableTransformer implements ExtendedTableTransformer
{
    @Override
    public String transform(String tableAsString, TableParsers tableParsers, TableProperties properties)
    {
        String byColumns = ExtendedTableTransformer.getMandatoryNonBlankProperty(properties, "byColumns");
        List<String> rowsToSort = ExamplesTableProcessor.parseRows(tableAsString);
        String header = rowsToSort.get(0);
        List<String> headerValues = tableParsers.parseRow(header, true, properties);
        List<Integer> columnsToCompare = Stream.of(StringUtils.split(byColumns, '|'))
                .map(String::trim)
                .map(headerValues::indexOf)
                .filter(i -> i > -1)
                .collect(Collectors.toList());
        List<List<String>> rows = ExamplesTableProcessor.parseDataRows(rowsToSort, tableParsers, properties).stream()
                .sorted((r1, r2) ->
                {
                    int result = 0;
                    Iterator<Integer> indexIterator = columnsToCompare.iterator();
                    while (result == 0 && indexIterator.hasNext())
                    {
                        int index = indexIterator.next();
                        result = r1.get(index).compareTo(r2.get(index));
                    }
                    return result;
                })
                .collect(Collectors.toList());
        return ExamplesTableProcessor.buildExamplesTable(headerValues, rows, properties, true);
    }
}
