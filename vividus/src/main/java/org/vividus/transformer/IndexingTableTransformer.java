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

import java.util.List;
import java.util.function.IntUnaryOperator;

import org.apache.commons.lang3.Validate;
import org.jbehave.core.model.ExamplesTable.TableProperties;
import org.jbehave.core.model.ExamplesTable.TableRows;
import org.jbehave.core.model.TableParsers;
import org.jbehave.core.model.TableTransformers.TableTransformer;
import org.vividus.util.ExamplesTableProcessor;

public class IndexingTableTransformer implements TableTransformer
{
    private static final String INDEX = "index";

    @Override
    public String transform(String tableAsString, TableParsers tableParsers, TableProperties properties)
    {
        TableRows tableRows = tableParsers.parseRows(tableAsString, properties);
        List<String> headers = tableRows.getHeaders();
        Validate.isTrue(!headers.contains(INDEX),
            "Unable to add column with row indices to the table, because it has `index` column.");
        Order order = properties.getMandatoryNonBlankProperty("order", Order.class);
        List<List<String>> rows = tableRows.getRows();
        IntUnaryOperator indexer = order.getIndexer(rows);
        for (int i = 0; i < rows.size(); i++)
        {
            rows.get(i).add(Integer.toString(indexer.applyAsInt(i)));
        }
        headers.add(INDEX);
        return ExamplesTableProcessor.buildExamplesTable(headers, rows, properties);
    }
}
