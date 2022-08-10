/*
 * Copyright 2019-2022 the original author or authors.
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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

public abstract class AbstractFilteringTableTransformer implements ExtendedTableTransformer
{
    protected static final String BY_COLUMNS_NAMES_PROPERTY = "byColumnNames";

    protected List<String> filterColumnNames(List<String> allColumnNames, String byColumnNames)
    {
        List<String> selectedColumnNames = Stream.of(StringUtils.split(byColumnNames, ';'))
                .map(String::trim)
                .collect(Collectors.toList());

        String undefinedColumns = selectedColumnNames
                .stream().filter(element -> !allColumnNames.contains(element)).collect(Collectors.joining("; "));
        isTrue(undefinedColumns.isEmpty(),
                "'byColumnNames' refers columns missing in ExamplesTable: %s", undefinedColumns);

        List<String> filtered = new ArrayList<>(allColumnNames);
        filtered.retainAll(selectedColumnNames);
        return filtered;
    }

    protected void filterRowsByColumnNames(List<String> allColumnNames, List<List<String>> rows,
            List<String> columnNamesToKeep)
    {
        int[] indexesToRemove = IntStream.range(0, allColumnNames.size())
                .filter(i -> !columnNamesToKeep.contains(allColumnNames.get(i)))
                .sorted()
                .toArray();
        rows.forEach(row -> {
            for (int i = indexesToRemove.length - 1; i >= 0; i--)
            {
                row.remove(indexesToRemove[i]);
            }
        });
    }
}
