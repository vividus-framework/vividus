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

package org.vividus.transformer;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.jbehave.core.model.ExamplesTable.TableProperties;
import org.jbehave.core.model.ExamplesTable.TableRows;
import org.jbehave.core.model.TableParsers;
import org.vividus.util.ExamplesTableProcessor;

@Named("SORTING")
public class SortingTableTransformer implements ExtendedTableTransformer
{
    @Override
    public String transform(String tableAsString, TableParsers tableParsers, TableProperties properties)
    {
        TableRows tableRows = tableParsers.parseRows(tableAsString, properties);
        String byColumns = properties.getMandatoryNonBlankProperty("byColumns", String.class);
        List<String> headerValues = tableRows.getHeaders();
        List<String> columnsToCompare = Stream.of(StringUtils.split(byColumns, '|'))
                .map(String::trim)
                .collect(Collectors.toList());
        List<Map<String, String>> rows = tableRows.getRows().stream()
                .sorted((r1, r2) ->
                {
                    int result = 0;
                    Iterator<String> columnIterator = columnsToCompare.iterator();
                    while (result == 0 && columnIterator.hasNext())
                    {
                        String column = columnIterator.next();
                        if (headerValues.contains(column))
                        {
                            result = r1.get(column).compareTo(r2.get(column));
                        }
                    }
                    return result;
                })
                .collect(Collectors.toList());
        return ExamplesTableProcessor.buildExamplesTable(headerValues, rows, properties);
    }
}
