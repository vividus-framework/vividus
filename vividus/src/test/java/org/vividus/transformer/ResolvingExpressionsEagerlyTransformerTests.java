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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.embedder.StoryControls;
import org.jbehave.core.expressions.ExpressionResolver;
import org.jbehave.core.model.ExamplesTable.TableProperties;
import org.jbehave.core.model.TableParsers;
import org.jbehave.core.steps.ParameterConverters;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ResolvingExpressionsEagerlyTransformerTests
{
    private final ParameterConverters parameterConverters = new ParameterConverters();

    @Mock private ExpressionResolver expressionResolver;
    @Mock private StoryControls storyControls;
    @InjectMocks private ResolvingExpressionsEagerlyTransformer transformer;

    @Test
    void shouldResolveDataRowsTest()
    {
        String table = "|header|\n|row1|\n|row2|";
        boolean dryRun = false;
        when(storyControls.dryRun()).thenReturn(dryRun);
        when(expressionResolver.resolveExpressions(dryRun, "row1")).thenReturn("resolved_row1");
        when(expressionResolver.resolveExpressions(dryRun, "row2")).thenReturn("resolved_row2");
        String actual = transformer.transform(table, new TableParsers(parameterConverters), createTableProperties());
        assertEquals("|header|\n|resolved_row1|\n|resolved_row2|", actual);
    }

    @Test
    void shouldNotResolveHeaderTest()
    {
        String table = "|header|\n|row|";
        boolean dryRun = false;
        when(storyControls.dryRun()).thenReturn(dryRun);
        when(expressionResolver.resolveExpressions(dryRun, "row")).thenReturn("resolved_row");
        String actual = transformer.transform(table, new TableParsers(parameterConverters), createTableProperties());
        assertEquals("|header|\n|resolved_row|", actual);
    }

    private TableProperties createTableProperties()
    {
        return new TableProperties("", new Keywords(), parameterConverters);
    }
}
