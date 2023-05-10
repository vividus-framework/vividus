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

import static org.apache.commons.lang3.Validate.isTrue;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.jbehave.core.model.ExamplesTable.TableProperties;
import org.jbehave.core.model.ExamplesTable.TableRows;
import org.jbehave.core.model.TableParsers;
import org.vividus.converter.FluentTrimmedEnumConverter;
import org.vividus.util.ExamplesTableProcessor;

public class SortingTableTransformer implements ExtendedTableTransformer
{
    private static final char DELIMITER = '|';

    private final FluentTrimmedEnumConverter fluentTrimmedEnumConverter;

    public SortingTableTransformer(FluentTrimmedEnumConverter fluentTrimmedEnumConverter)
    {
        this.fluentTrimmedEnumConverter = fluentTrimmedEnumConverter;
    }

    @Override
    public String transform(String tableAsString, TableParsers tableParsers, TableProperties properties)
    {
        TableRows tableRows = tableParsers.parseRows(tableAsString, properties);
        String byColumns = properties.getMandatoryNonBlankProperty("byColumns", String.class);
        String orderProperty = properties.getProperties().getProperty("order", "ASCENDING");
        Order order = (Order) fluentTrimmedEnumConverter.convertValue(orderProperty, Order.class);
        List<String> headerValues = tableRows.getHeaders();
        List<String> columnsToCompare = Stream.of(StringUtils.split(byColumns, DELIMITER))
                .map(String::trim)
                .collect(Collectors.toList());
        String sortingTypes = properties.getProperties().getProperty("sortingTypes", "STRING");
        List<SortingType> sortingTypesToCompare = Stream.of(StringUtils.split(sortingTypes, DELIMITER))
                .map(s -> (SortingType) fluentTrimmedEnumConverter.convertValue(s, SortingType.class))
                .collect(Collectors.toList());
        int sortingTypesSize = sortingTypesToCompare.size();
        isTrue(sortingTypesSize == 1 || sortingTypesSize == columnsToCompare.size(),
                "Please, specify parameter 'sortingType' (%s) with the same count of types as count of column"
                + " names in parameter 'byColumns' (%s)", sortingTypes, byColumns);
        List<List<String>> rows = sort(tableRows, columnsToCompare, sortingTypesToCompare, headerValues, order);
        return ExamplesTableProcessor.buildExamplesTable(headerValues, rows, properties);
    }

    private static List<List<String>> sort(TableRows tableRows, List<String> columnsToCompare,
            List<SortingType> sortingTypesToCompare, List<String> headerValues, Order order)
    {
        return tableRows.getRows().stream()
                .sorted((r1, r2) ->
                {
                    int result = 0;
                    Iterator<String> columnIterator = columnsToCompare.iterator();
                    Iterator<SortingType> sortingTypeIterator = sortingTypesToCompare.iterator();
                    SortingType sortingType = null;
                    while (result == 0 && columnIterator.hasNext())
                    {
                        String column = columnIterator.next();
                        if (sortingTypeIterator.hasNext())
                        {
                            sortingType = sortingTypeIterator.next();
                        }
                        int indexOfColumn = headerValues.indexOf(column);
                        if (indexOfColumn > -1)
                        {
                            result = order.getDirection() * sortingType.compareValues(r1.get(indexOfColumn),
                                    r2.get(indexOfColumn));
                        }
                    }
                    return result;
                })
                .collect(Collectors.toList());
    }

    private enum SortingType
    {
        STRING
        {
            @Override
            int compareValues(String valueFirst, String valueSecond)
            {
                return valueFirst.compareTo(valueSecond);
            }
        },
        NUMBER
        {
            @Override
            int compareValues(String valueFirst, String valueSecond)
            {
                try
                {
                    return new BigDecimal(valueFirst).compareTo(new BigDecimal(valueSecond));
                }
                catch (NumberFormatException e)
                {
                    throw new IllegalArgumentException(String.format(
                            "NUMBER sorting type supports only number values for comparison. %s", e.getMessage()), e);
                }
            }
        };

        abstract int compareValues(String valueFirst, String valueSecond);
    }
}
