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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Properties;

import org.jbehave.core.model.ExamplesTable.TableProperties;
import org.jbehave.core.model.TableParsers;
import org.jbehave.core.steps.ParameterConverters;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.bdd.steps.ExpressionAdaptor;

@ExtendWith(MockitoExtension.class)
class ResolvingExpressionsEagerlyTransformerTests
{
    private final ParameterConverters parameterConverters = new ParameterConverters();

    @Mock private ExpressionAdaptor expressionAdaptor;
    @InjectMocks private ResolvingExpressionsEagerlyTransformer transformer;

    @Test
    void shouldResolveDataRowsTest()
    {
        String table = "|header|\n|row1|\n|row2|";
        when(expressionAdaptor.processRawExpression("row1")).thenReturn("resolved_row1");
        when(expressionAdaptor.processRawExpression("row2")).thenReturn("resolved_row2");
        String actual = transformer.transform(table, new TableParsers(parameterConverters),
                new TableProperties(parameterConverters, new Properties()));
        assertEquals("|header|\n|resolved_row1|\n|resolved_row2|", actual);
    }

    @Test
    void shouldNotResolveHeaderTest()
    {
        String table = "|header|\n|row|";
        when(expressionAdaptor.processRawExpression("row")).thenReturn("resolved_row");
        String actual = transformer.transform(table, new TableParsers(parameterConverters),
                new TableProperties(parameterConverters, new Properties()));
        assertEquals("|header|\n|resolved_row|", actual);
    }
}
