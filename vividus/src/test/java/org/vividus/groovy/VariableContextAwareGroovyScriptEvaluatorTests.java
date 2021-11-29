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

package org.vividus.groovy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scripting.ScriptSource;
import org.springframework.scripting.support.StaticScriptSource;
import org.vividus.context.VariableContext;

@ExtendWith(MockitoExtension.class)
class VariableContextAwareGroovyScriptEvaluatorTests
{
    @Mock
    private VariableContext variableContext;

    @Test
    void shouldPassVariableToEvaluate()
    {
        VariableContextAwareGroovyScriptEvaluator evaluator =
            Mockito.spy(new VariableContextAwareGroovyScriptEvaluator(variableContext));
        ScriptSource script = new StaticScriptSource("return 'value'");
        Map<String, Object> variables = Map.of();
        when(variableContext.getVariables()).thenReturn(variables);
        String expected = "value";
        when(evaluator.evaluate(script, variables)).thenReturn(expected);
        assertEquals(expected, evaluator.evaluate(script));
    }
}
