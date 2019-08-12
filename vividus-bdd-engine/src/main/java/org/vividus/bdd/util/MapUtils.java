/*
 * Copyright 2019 the original author or authors.
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

package org.vividus.bdd.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.steps.Parameters;

public final class MapUtils
{
    private MapUtils()
    {
    }

    public static Map<String, String> convertSingleRowExamplesTableToMap(ExamplesTable table)
    {
        List<Parameters> rows = table.getRowsAsParameters(true);
        if (rows.size() != 1)
        {
            throw new IllegalArgumentException("ExamplesTable should contain single row with values");
        }
        return rows.get(0).values();
    }

    public static Map<String, List<String>> convertExamplesTableToMap(ExamplesTable table)
    {
        Map<String, List<String>> convertedTable = new LinkedHashMap<>();
        for (Parameters row : table.getRowsAsParameters(true))
        {
            for (Entry<String, String> entry : row.values().entrySet())
            {
                convertedTable.computeIfAbsent(entry.getKey(), k -> new ArrayList<>()).add(entry.getValue());
            }
        }
        return convertedTable;
    }
}
