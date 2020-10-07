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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

public abstract class AbstractFilteringTableTransformer implements ExtendedTableTransformer
{
    protected static final String BY_COLUMNS_NAMES_PROPERTY = "byColumnNames";

    protected List<String> filterColumnNames(List<String> allColumnNames, String byColumnNames)
    {
        List<String> filtered = new ArrayList<>(allColumnNames);
        filtered.retainAll(Stream.of(StringUtils.split(byColumnNames, ';'))
                .map(String::trim)
                .collect(Collectors.toList()));
        return filtered;
    }

    protected void filterRowsByColumnNames(List<Map<String, String>> rows, List<String> columnNames)
    {
        rows.forEach(row -> row.keySet().retainAll(columnNames));
    }
}
