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

package org.vividus.steps;

import static com.github.valfirst.slf4jtest.LoggingEvent.error;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.jbehave.core.embedder.StoryControls;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.context.VariableContext;
import org.vividus.variable.DynamicVariable;
import org.vividus.variable.DynamicVariableCalculationResult;

@ExtendWith({ MockitoExtension.class, TestLoggerFactoryExtension.class })
class VariableResolverTests
{
    private static final String KEY = "key";
    private static final String VAR1 = "var1";
    private static final String VALUE1 = "2";
    private static final String VAR2 = "var2";
    private static final String VALUE2 = "3";

    private final TestLogger logger = TestLoggerFactory.getTestLogger(VariableResolver.class);

    @Mock private VariableContext variableContext;
    @Mock private StoryControls storyControls;

    private Object convert(String value)
    {
        return new VariableResolver(variableContext, Map.of(), storyControls).resolve(value);
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
        when(variableContext.getVariable(variableKey)).thenReturn(variableValue);
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
        var object = new Object();
        when(variableContext.getVariable("varWithObject")).thenReturn(object);
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
        when(variableContext.getVariable(VAR1)).thenReturn(VALUE1);
        when(variableContext.getVariable(VAR2)).thenReturn(VALUE2);
        Object actualValue = convert(input);
        assertEquals(expected, actualValue);
    }

    @Test
    void shouldResolveSeveralVariablesWithNonStringValues()
    {
        lenient().when(variableContext.getVariable(VAR1)).thenReturn(1);
        lenient().when(variableContext.getVariable(VAR2)).thenReturn(2L);
        Object actualValue = convert("varvar${var1}${var2}moremore");
        assertEquals("varvar12moremore", actualValue);
    }

    @Test
    void shouldResolveDefaultValueWithVariable()
    {
        when(variableContext.getVariable(VAR1)).thenReturn(VALUE1);
        when(variableContext.getVariable("var:" + VALUE1)).thenReturn(VALUE2);
        assertEquals(VALUE2, convert("${var:${var1}}"));
    }

    @ParameterizedTest
    @CsvSource({
        "dynamic-variable-key, dynamicVariableKey",
        "key                 , key               "
    })
    void testVariablesDynamicValue(String key, String alias)
    {
        var dynamicVariable = mock(DynamicVariable.class);
        when(dynamicVariable.calculateValue()).thenReturn(DynamicVariableCalculationResult.withValue(VALUE1));
        when(variableContext.getVariable(key)).thenReturn(null);
        when(variableContext.getVariable(alias)).thenReturn(null);
        var variableResolver = new VariableResolver(variableContext, Map.of(key, dynamicVariable), storyControls);
        assertEquals(VALUE1, variableResolver.resolve(asRef(key)));
        assertEquals(VALUE1, variableResolver.resolve(asRef(alias)));
    }

    @Test
    void shouldNotResolveDynamicVariablesIfTheExecutionIsDryRun()
    {
        var dynamicVariable = mock(DynamicVariable.class);
        var variableResolver = new VariableResolver(variableContext, Map.of(KEY, dynamicVariable), storyControls);
        when(storyControls.dryRun()).thenReturn(true);
        variableResolver.resolve(asRef(KEY));
        verifyNoInteractions(dynamicVariable);
    }

    @Test
    void shouldNotResolveDynamicVariablesIfTheErrorIsReturned()
    {
        var dynamicVariable = mock(DynamicVariable.class);
        var error = "error";
        when(dynamicVariable.calculateValue()).thenReturn(DynamicVariableCalculationResult.withError(error));
        when(variableContext.getVariable(KEY)).thenReturn(null);
        var variableResolver = new VariableResolver(variableContext, Map.of(KEY, dynamicVariable), storyControls);
        var variableReference = asRef(KEY);
        assertEquals(variableReference, variableResolver.resolve(variableReference));
        assertThat(logger.getLoggingEvents(),
                is(List.of(error("Unable to resolve dynamic variable ${{}}: {}", KEY, error))));
    }

    @Test
    void testVariablesDynamicValueNoProvider()
    {
        var dynamicVariable = mock(DynamicVariable.class);
        when(variableContext.getVariable(VAR1)).thenReturn(null);
        var variableResolver = new VariableResolver(variableContext, Map.of(VAR2, dynamicVariable), storyControls);
        String varReference = asRef(VAR1);
        Object actualValue = variableResolver.resolve(varReference);
        assertEquals(varReference, actualValue);
    }

    private static String asRef(String name)
    {
        return "${" + name + "}";
    }
}
