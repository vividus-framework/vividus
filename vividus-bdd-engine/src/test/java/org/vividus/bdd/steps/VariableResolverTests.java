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

package org.vividus.bdd.steps;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.jbehave.core.embedder.StoryControls;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.bdd.context.IBddVariableContext;
import org.vividus.bdd.variable.DynamicVariable;

@ExtendWith(MockitoExtension.class)
class VariableResolverTests
{
    private static final String KEY = "key";
    private static final String VAR1 = "var1";
    private static final String VALUE1 = "2";
    private static final String VAR2 = "var2";
    private static final String VALUE2 = "3";

    @Mock private IBddVariableContext bddVariableContext;
    @Mock private StoryControls storyControls;

    private Object convert(String value)
    {
        return new VariableResolver(bddVariableContext, Map.of(), storyControls).resolve(value);
    }

    @ParameterizedTest
    @CsvSource({
            " ${var},                               var,             value, value",
            " ${var}${var},                         var,             value, valuevalue",
            " ${var[0].Name},                       var[0].Name,     value, value",
            " ${var}|#{expr},                       var,             value, value|#{expr}",
            " #{eval(${var:0} + 1)},                var:0,           0,     #{eval(0 + 1)}",
            " ${var},                               var,             ,      ${var}",
            " ${var:default},                       var:default,     value, value",
            " ${var:},                              var:,            value, value",
            " '{\"ids\": [\"${var}\", \"12-ce\"]}', var,             value, '{\"ids\": [\"value\", \"12-ce\"]}'",
            " ${var${varPartName}},                 varPartName,     value, ${varvalue}",
            " '#{exp(\\}{BNS_TRX_ID=, A, ${var})}', var,             value, '#{exp(\\}{BNS_TRX_ID=, A, value)}'",
            " [JAVA_HOME=${var}].*[^${PATH}].*,     var,             value, [JAVA_HOME=value].*[^${PATH}].*"
    })
    void shouldResolveVariables(String valueToResolve, String variableKey, String variableValue, String expected)
    {
        when(bddVariableContext.getVariable(variableKey)).thenReturn(variableValue);
        assertEquals(expected, convert(valueToResolve));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "${varWithObject}",
            "\n${varWithObject}",
            "${varWithObject}\r\n",
            "\n\n\n${varWithObject}\r\n\r\n"
    })
    void shouldResolveVariableAsObject()
    {
        Object object = new Object();
        when(bddVariableContext.getVariable("varWithObject")).thenReturn(object);
        assertEquals(object, convert("${varWithObject}"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "varvar",
            "${}",
            "$",
            "{}",
            "$}{",
            "${var[0]}"
    })
    @NullSource
    void shouldReturnTheSameValueWhenNothingToResolve(String input)
    {
        Object actualValue = convert(input);
        assertEquals(input, actualValue);
    }

    @ParameterizedTest
    @CsvSource({
            "varvar${var1}${var2}moremore,  varvar23moremore",
            "${var1}${var2}moremore,        23moremore",
            "varvar${var1}${var2},          varvar23",
            "${var${var1}},                 3",
            "${var${var1}} + ${var${var1}}, 3 + 3"
    })
    void shouldResolveSeveralVariables(String input, String expected)
    {
        when(bddVariableContext.getVariable(VAR1)).thenReturn(VALUE1);
        when(bddVariableContext.getVariable(VAR2)).thenReturn(VALUE2);
        Object actualValue = convert(input);
        assertEquals(expected, actualValue);
    }

    @Test
    void shouldResolveSeveralVariablesWithNonStringValues()
    {
        lenient().when(bddVariableContext.getVariable(VAR1)).thenReturn(1);
        lenient().when(bddVariableContext.getVariable(VAR2)).thenReturn(2L);
        Object actualValue = convert("varvar${var1}${var2}moremore");
        assertEquals("varvar12moremore", actualValue);
    }

    @Test
    void shouldResolveDefaultValueWithVariable()
    {
        when(bddVariableContext.getVariable(VAR1)).thenReturn(VALUE1);
        when(bddVariableContext.getVariable("var:" + VALUE1)).thenReturn(VALUE2);
        assertEquals(VALUE2, convert("${var:${var1}}"));
    }

    @ParameterizedTest
    @CsvSource({
        "dynamic-variable-key, dynamicVariableKey",
        "key                 , key               "
    })
    void testVariablesDynamicValue(String key, String alias)
    {
        DynamicVariable dynamicVariable = mock(DynamicVariable.class);
        when(bddVariableContext.getVariable(key)).thenReturn(null);
        when(bddVariableContext.getVariable(alias)).thenReturn(null);
        VariableResolver variableResolver = new VariableResolver(bddVariableContext, Map.of(key, dynamicVariable),
                storyControls);
        when(dynamicVariable.getValue()).thenReturn(VALUE1);
        assertEquals(VALUE1, variableResolver.resolve(asRef(key)));
        assertEquals(VALUE1, variableResolver.resolve(asRef(alias)));
    }

    @Test
    void shouldNotResolveDynamicVariablesIfTheExecutionIsDryRun()
    {
        DynamicVariable dynamicVariable = mock(DynamicVariable.class);
        VariableResolver variableResolver = new VariableResolver(bddVariableContext, Map.of(KEY, dynamicVariable),
                storyControls);
        when(storyControls.dryRun()).thenReturn(true);
        variableResolver.resolve(asRef(KEY));
        verifyNoInteractions(dynamicVariable);
    }

    @Test
    void testVariablesDynamicValueNoProvider()
    {
        DynamicVariable dynamicVariable = mock(DynamicVariable.class);
        when(bddVariableContext.getVariable(VAR1)).thenReturn(null);
        VariableResolver variableResolver = new VariableResolver(bddVariableContext, Map.of(VAR2, dynamicVariable),
                storyControls);
        String test1 = asRef(VAR1);
        Object actualValue = variableResolver.resolve(test1);
        assertEquals(test1, actualValue);
    }

    private static String asRef(String name)
    {
        return "${" + name + "}";
    }
}
