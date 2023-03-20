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

import org.jbehave.core.embedder.StoryControls;
import org.jbehave.core.expressions.ExpressionResolver;
import org.jbehave.core.model.ExamplesTable.TableProperties;
import org.jbehave.core.model.ExamplesTable.TableRows;
import org.jbehave.core.model.TableParsers;
import org.vividus.util.ExamplesTableProcessor;

public class ResolvingExpressionsEagerlyTransformer implements ExtendedTableTransformer
{
    private final ExpressionResolver expressionResolver;
    private final StoryControls storyControls;

    public ResolvingExpressionsEagerlyTransformer(ExpressionResolver expressionResolver, StoryControls storyControls)
    {
        this.expressionResolver = expressionResolver;
        this.storyControls = storyControls;
    }

    @Override
    public String transform(String tableAsString, TableParsers tableParsers, TableProperties properties)
    {
        TableRows tableRows = tableParsers.parseRows(tableAsString, properties);
        List<List<String>> rows = tableRows.getRows();
        resolveExpressions(rows);
        return ExamplesTableProcessor.buildExamplesTable(tableRows.getHeaders(), rows, properties);
    }

    private void resolveExpressions(List<List<String>> rows)
    {
        rows.forEach(row -> row.replaceAll(expression -> String.valueOf(
                expressionResolver.resolveExpressions(storyControls.dryRun(), expression))));
    }
}
