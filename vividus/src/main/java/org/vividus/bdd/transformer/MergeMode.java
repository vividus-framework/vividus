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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.collections4.CollectionUtils;
import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.model.ExamplesTable.TableProperties;
import org.jbehave.core.steps.ParameterConverters;
import org.vividus.bdd.util.ExamplesTableProcessor;

public enum MergeMode
{
    ROWS
    {
        @Override
        protected void validateInput(ExamplesTable current, ExamplesTable next, boolean strict)
        {
            isTrue(!strict || CollectionUtils.isEqualCollection(current.getHeaders(), next.getHeaders()),
                    "Please, specify tables with the same sets of headers");
        }

        @Override
        protected List<ExamplesTable> alignTables(Keywords keywords, ParameterConverters parameterConverters,
                List<ExamplesTable> examplesTables, String fillerValue)
        {
            List<String> mergedHeaders = mergeHeaders(examplesTables);
            return examplesTables.stream().map(table -> {
                Collection<String> missingHeaders = CollectionUtils.subtract(mergedHeaders, table.getHeaders());
                if (missingHeaders.isEmpty())
                {
                    return table;
                }
                TableProperties tableProperties = new TableProperties(table.getPropertiesAsString(), keywords,
                        parameterConverters);
                ExamplesTable supplementingTable = buildSupplementingTable(tableProperties, fillerValue, missingHeaders,
                        table.getRowCount());
                String merged = COLUMNS.merge(keywords, parameterConverters, List.of(table, supplementingTable),
                        tableProperties, Optional.empty(), true);
                return new ExamplesTable(merged);
            }).collect(Collectors.toList());
        }

        @Override
        protected List<List<String>> merge(List<ExamplesTable> examplesTables)
        {
            return examplesTables.stream()
                    .map(ExamplesTable::getRows)
                    .flatMap(List::stream)
                    .map(TreeMap::new)
                    .map(Map::values)
                    .map(ArrayList::new)
                    .collect(Collectors.toList());
        }
    },
    COLUMNS
    {
        @Override
        protected void validateInput(ExamplesTable current, ExamplesTable next, boolean strict)
        {
            isTrue(!strict || current.getRows().size() == next.getRows().size(),
                    "Please, specify tables with the same number of rows");
            Collection<String> intersection = CollectionUtils.intersection(current.getHeaders(), next.getHeaders());
            isTrue(intersection.isEmpty(),
                "Please, specify tables with the unique sets of headers. Conflicting headers are: %s",
                String.join(", ", intersection));
        }

        @Override
        protected List<ExamplesTable> alignTables(Keywords keywords, ParameterConverters parameterConverters,
                List<ExamplesTable> examplesTables, String fillerValue)
        {
            int maxRowCount = examplesTables.stream().mapToInt(ExamplesTable::getRowCount).max().orElse(0);

            return examplesTables.stream().map(table -> {
                int missingRowCount = maxRowCount - table.getRowCount();
                if (missingRowCount == 0)
                {
                    return table;
                }
                TableProperties tableProperties = new TableProperties(table.getPropertiesAsString(), keywords,
                        parameterConverters);
                ExamplesTable supplementingTable = buildSupplementingTable(tableProperties, fillerValue,
                        table.getHeaders(), missingRowCount);
                String merged = ROWS.merge(keywords, parameterConverters, List.of(table, supplementingTable),
                        tableProperties, Optional.empty(), true);
                return new ExamplesTable(merged);
            }).collect(Collectors.toList());
        }

        @Override
        protected List<List<String>> merge(List<ExamplesTable> examplesTables)
        {
            return IntStream.range(0, examplesTables.get(0).getRowCount()).mapToObj(index ->
                examplesTables.stream()
                         .map(table -> table.getRow(index))
                         .map(Map::entrySet)
                         .flatMap(Set::stream)
                         .sorted(Entry.comparingByKey())
                         .map(Entry::getValue)
                         .collect(Collectors.toList())
            ).collect(Collectors.toList());
        }
    };

    public String merge(Keywords keywords, ParameterConverters parameterConverters, List<ExamplesTable> tables,
            TableProperties properties, Optional<String> fillerValue)
    {
        return merge(keywords, parameterConverters, tables, properties, fillerValue, false);
    }

    private String merge(Keywords keywords, ParameterConverters parameterConverters, List<ExamplesTable> tables,
            TableProperties properties, Optional<String> fillerValue, boolean appendTableProperties)
    {
        validate(tables, fillerValue.isEmpty());
        List<ExamplesTable> tablesToMerge = fillerValue.map(v -> alignTables(keywords, parameterConverters, tables, v))
                .orElse(tables);
        List<List<String>> mergedRows = merge(tablesToMerge);
        List<String> mergeHeaders = mergeHeaders(tables);
        return ExamplesTableProcessor.buildExamplesTable(mergeHeaders, mergedRows, properties, true,
                appendTableProperties);
    }

    protected abstract void validateInput(ExamplesTable current, ExamplesTable next, boolean strict);

    protected abstract List<ExamplesTable> alignTables(Keywords keywords, ParameterConverters parameterConverters,
            List<ExamplesTable> examplesTables, String fillerValue);

    protected abstract List<List<String>> merge(List<ExamplesTable> examplesTables);

    private void validate(List<ExamplesTable> tables, boolean strict)
    {
        IntStream.range(0, tables.size() - 1).forEach(i -> validateInput(tables.get(i), tables.get(i + 1), strict));
    }

    protected List<String> mergeHeaders(List<ExamplesTable> examplesTables)
    {
        return examplesTables.stream()
                .map(ExamplesTable::getHeaders)
                .flatMap(List::stream)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    private static ExamplesTable buildSupplementingTable(TableProperties properties, String fillerValue,
            Collection<String> header, int rowCount)
    {
        List<List<String>> rows = Collections.nCopies(rowCount, Collections.nCopies(header.size(), fillerValue));
        return new ExamplesTable(ExamplesTableProcessor.buildExamplesTable(header, rows, properties, true, true));
    }
}
