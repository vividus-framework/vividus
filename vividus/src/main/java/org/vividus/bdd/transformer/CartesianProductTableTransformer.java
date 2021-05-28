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

import static org.apache.commons.lang3.Validate.isTrue;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.inject.Named;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.model.ExamplesTable.TableProperties;
import org.jbehave.core.model.TableParsers;

@Named("CARTESIAN_PRODUCT")
public class CartesianProductTableTransformer extends AbstractTableLoadingTransformer
{
    public CartesianProductTableTransformer()
    {
        super(true);
    }

    @Override
    public String transform(String tableAsString, TableParsers tableParsers, TableProperties properties)
    {
        List<ExamplesTable> tables = loadTables(tableAsString, properties);

        Set<String> repeatingKeys = tables.stream()
                                          .map(ExamplesTable::getHeaders)
                                          .flatMap(List::stream)
                                          .collect(Collectors.collectingAndThen(
                                              Collectors.groupingBy(Function.identity(),
                                                  Collectors.counting()), map ->
                                              {
                                                  map.entrySet().removeIf(e -> e.getValue() == 1);
                                                  return map.keySet();
                                              }));

        isTrue(repeatingKeys.isEmpty(), "Tables must contain different keys, but found the same keys: %s",
                repeatingKeys);

        return tables.stream()
                     .reduce(CartesianProductTableTransformer::cartesianProduct)
                     .map(ExamplesTable::asString)
                     .get();
    }

    private static ExamplesTable cartesianProduct(ExamplesTable left, ExamplesTable right)
    {
        List<String> headers = new ArrayList<>(left.getHeaders());
        headers.addAll(right.getHeaders());

        int counter = headers.size();
        List<Map<String, String>> examplesTableRows = new ArrayList<>(left.getRowCount() * right.getRowCount());

        Lists.cartesianProduct(List.of(getRows(left), getRows(right)))
             .stream()
             .map(Iterables::concat)
             .map(Lists::newArrayList)
             .forEach(row ->
             {
                 Map<String, String> tableRow = new LinkedHashMap<>(counter);
                 IntStream.range(0, counter).forEach(index -> tableRow.put(headers.get(index), row.get(index)));
                 examplesTableRows.add(tableRow);
             });

        return ExamplesTable.empty().withRows(examplesTableRows);
    }

    private static List<List<String>> getRows(ExamplesTable table)
    {
        return table.getRows().stream()
                              .map(Map::values)
                              .map(ArrayList::new)
                              .collect(Collectors.toList());
    }
}
