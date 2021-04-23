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

import java.util.List;
import java.util.Map;

import javax.inject.Named;

import org.jbehave.core.model.ExamplesTable.TableProperties;
import org.jbehave.core.model.ExamplesTable.TableRows;
import org.jbehave.core.model.TableParsers;
import org.vividus.bdd.steps.ExpressionAdaptor;
import org.vividus.bdd.util.ExamplesTableProcessor;

@Named("RESOLVING_EXPRESSIONS_EAGERLY")
public class ResolvingExpressionsEagerlyTransformer implements ExtendedTableTransformer
{
    private final ExpressionAdaptor expressionAdaptor;

    public ResolvingExpressionsEagerlyTransformer(ExpressionAdaptor expressionAdaptor)
    {
        this.expressionAdaptor = expressionAdaptor;
    }

    @Override
    public String transform(String tableAsString, TableParsers tableParsers, TableProperties properties)
    {
        TableRows tableRows = tableParsers.parseRows(tableAsString, properties);
        List<Map<String, String>> rows = tableRows.getRows();
        resolveExpressions(rows);
        return ExamplesTableProcessor.buildExamplesTable(tableRows.getHeaders(), rows, properties);
    }

    private void resolveExpressions(List<Map<String, String>> list)
    {
        list.forEach(map -> map.entrySet().forEach(
                entry -> entry.setValue(String.valueOf(expressionAdaptor.processRawExpression(entry.getValue())))));
    }
}
