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

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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
        TableRows tableRows = tableParsers.parseRows(tableAsString, tableProperties);
        List<String> allColumnNames = tableRows.getHeaders();
        List<String> filteredColumnNames = filterColumnNames(allColumnNames, byColumnNames);
        List<List<String>> rows = tableRows.getRows();
        filterRowsByColumnNames(allColumnNames, rows, filteredColumnNames);

        Set<List<String>> distinctRows = new LinkedHashSet<>(rows);

        return ExamplesTableProcessor.buildExamplesTable(filteredColumnNames, distinctRows, tableProperties);
    }
}
