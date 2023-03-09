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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.model.ExamplesTable.TableProperties;
import org.jbehave.core.model.TableParsers;
import org.vividus.util.ExamplesTableProcessor;

public class InnerJoinTableTransformer extends AbstractTableLoadingTransformer
{
    public InnerJoinTableTransformer()
    {
        super(false);
    }

    @Override
    public String transform(String tableAsString, TableParsers tableParsers, TableProperties properties)
    {
        List<ExamplesTable> tables = loadTables(tableAsString, properties);
        isTrue(tables.size() == 2, "Please, specify only two ExamplesTable-s");
        ExamplesTable leftTable = tables.get(0);
        ExamplesTable rightTable = tables.get(1);
        String leftTableJoinColumn = properties.getMandatoryNonBlankProperty("leftTableJoinColumn", String.class);
        String rightTableJoinColumn = properties.getMandatoryNonBlankProperty("rightTableJoinColumn", String.class);
        isTrue(leftTable.getHeaders().contains(leftTableJoinColumn),
                "The left table doesn't contain the following column: %s", leftTableJoinColumn);
        isTrue(rightTable.getHeaders().contains(rightTableJoinColumn),
                "The right table doesn't contain the following column: %s", rightTableJoinColumn);
        Set<String> repeatingKeys = leftTable.getHeaders().stream()
                .filter(e -> !e.equals(leftTableJoinColumn) && rightTable.getHeaders().contains(e))
                .collect(Collectors.toSet());
        isTrue(repeatingKeys.isEmpty(), "Tables must contain different columns (except joint column),"
                + " but found the same columns: %s", repeatingKeys);

        ExamplesTable resultTable = innerJoin(leftTable, rightTable, leftTableJoinColumn, rightTableJoinColumn);
        return resultTable.isEmpty() ? getEmptyTableWithHeaders(tables, properties) : resultTable.asString();
    }

    private static ExamplesTable innerJoin(ExamplesTable leftTable, ExamplesTable rightTable,
            String leftTableJoinColumn, String rightTableJoinColumn)
    {
        List<Map<String, String>> leftRows = leftTable.getRows();
        List<Map<String, String>> rightRows = rightTable.getRows();
        Map<String, List<Map<String, String>>> rightByColumn = rightRows.stream()
                .collect(Collectors.groupingBy(m -> m.get(rightTableJoinColumn)));
        List<Map<String, String>> examplesTableRows = leftRows.stream()
                .flatMap(leftRow -> Optional.ofNullable(rightByColumn.get(leftRow.get(leftTableJoinColumn)))
                        .stream().flatMap(value -> value.stream()
                                .map(rightRow -> {
                                    Map<String, String> jointRow = new HashMap<>();
                                    jointRow.putAll(leftRow);
                                    jointRow.putAll(rightRow);
                                    return jointRow;
                                }))).collect(Collectors.toList());
        return ExamplesTable.empty().withRows(examplesTableRows);
    }

    private static String getEmptyTableWithHeaders(List<ExamplesTable> tables, TableProperties tableProperties)
    {
        Set<String> headers = tables.stream().map(ExamplesTable::getHeaders)
                .flatMap(List::stream).collect(Collectors.toSet());
        return ExamplesTableProcessor.buildExamplesTable(headers, List.of(), tableProperties);
    }
}
