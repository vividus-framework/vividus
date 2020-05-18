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

import static org.vividus.bdd.util.ExamplesTableProcessor.asDataRows;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.inject.Named;

import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.model.ExamplesTable.ExamplesTableProperties;
import org.jbehave.core.model.ExamplesTableFactory;
import org.jbehave.core.model.TableParsers;
import org.vividus.bdd.steps.ExpressionAdaptor;
import org.vividus.bdd.util.ExamplesTableProcessor;

@Named("RESOLVING_EXPRESSIONS_EAGERLY")
public class ResolvingExpressionsEagerlyTransformer implements ExtendedTableTransformer
{
    private final ExpressionAdaptor expressionAdaptor;
    private final Supplier<ExamplesTableFactory> examplesTableFactory;

    public ResolvingExpressionsEagerlyTransformer(ExpressionAdaptor expressionAdaptor,
            Supplier<ExamplesTableFactory> examplesTableFactory)
    {
        this.expressionAdaptor = expressionAdaptor;
        this.examplesTableFactory = examplesTableFactory;
    }

    @Override
    public String transform(String tableAsString, TableParsers tableParsers, ExamplesTableProperties properties)
    {
        ExamplesTable table = examplesTableFactory.get().createExamplesTable(tableAsString);
        List<String> headerValues = table.getHeaders();
        List<List<String>> resolvedData = resolveExpressions(asDataRows(table));
        return ExamplesTableProcessor.buildExamplesTable(headerValues, resolvedData, properties, true);
    }

    private List<List<String>> resolveExpressions(List<List<String>> rows)
    {
        return rows.stream().map(l -> l.stream().map(expressionAdaptor::process).collect(Collectors.toList()))
                .collect(Collectors.toList());
    }
}
