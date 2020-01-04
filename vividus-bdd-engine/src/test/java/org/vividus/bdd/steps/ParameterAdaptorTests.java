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

package org.vividus.bdd.steps;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.bdd.context.IBddVariableContext;
import org.vividus.bdd.variable.DynamicVariable;

@ExtendWith(MockitoExtension.class)
class ParameterAdaptorTests
{
    private static final String VAR1_VARIABLE = "${var1}";
    private static final String UID = "\"4e201077-3b63-40a8-919f-1b1c1617b8c0\"]}";
    private static final String OBJECT = "object";
    private static final String VAR1 = "var1";
    private static final String VALUE1 = "value1";
    private static final String VAR3 = "var3";
    private static final String VALUE3 = "value3";
    private static final String VAR3_PROPERTY = "${" + VAR3 + "}";
    private static final String JSON_INPUT_PARAMETER = "{\"relatedObjectIds\": [\"${var1}\", " + UID;
    private static final String JSON_OUTPUT_WITH_VARIABLE = "{\"relatedObjectIds\": [\"value1\", " + UID;

    @Mock
    private IBddVariableContext bddVariableContext;

    @InjectMocks
    private ParameterAdaptor parameterAdaptor;

    @Test
    void testTextBeforeAfterTwoVariablesAndList()
    {
        when(bddVariableContext.getVariable(VAR1)).thenReturn(VALUE1);
        when(bddVariableContext.getVariable("product[2]")).thenReturn(OBJECT);
        when(bddVariableContext.getVariable(VAR3)).thenReturn(VALUE3);
        Object actualValue = parameterAdaptor.convert("varvar${" + VAR3 + "}${product[2]}${" + VAR1 + "}moremore");
        assertEquals("varvarvalue3objectvalue1moremore", actualValue);
    }

    @Test
    void testTextAfterTwoVariablesAndList()
    {
        when(bddVariableContext.getVariable(VAR1)).thenReturn(VALUE1);
        when(bddVariableContext.getVariable("product[1]")).thenReturn(OBJECT);
        when(bddVariableContext.getVariable(VAR3)).thenReturn(VALUE3);
        Object actualValue = parameterAdaptor.convert("${var3}${product[1]}${var1}moremore");
        assertEquals("value3objectvalue1moremore", actualValue);
    }

    @Test
    void testParameterInJson()
    {
        when(bddVariableContext.getVariable(VAR1)).thenReturn(VALUE1);
        Object actualValue = parameterAdaptor.convert(JSON_INPUT_PARAMETER);
        assertEquals(JSON_OUTPUT_WITH_VARIABLE, actualValue);
    }

    @Test
    void testListIndexOutOfBounds()
    {
        String value = "${product[1]}";
        Object actualValue = parameterAdaptor.convert(value);
        assertEquals(value, actualValue);
    }

    @Test
    void testNullListVariable()
    {
        String value = "${product[0]}";
        Object actualValue = parameterAdaptor.convert(value);
        assertEquals(value, actualValue);
    }

    @Test
    void testNullListVariableIndexedVariableExists()
    {
        when(bddVariableContext.getVariable("product[0].Name")).thenReturn(VALUE1);
        String value = "${product[0].Name}";
        assertEquals(VALUE1, parameterAdaptor.convert(value));
    }

    @Test
    void testTextBeforeAfterTwoVariables()
    {
        when(bddVariableContext.getVariable(VAR1)).thenReturn(VALUE1);
        when(bddVariableContext.getVariable(VAR3)).thenReturn(VALUE3);
        Object actualValue = parameterAdaptor.convert("varvar${var3}${var1}moremore");
        assertEquals("varvarvalue3value1moremore", actualValue);
    }

    @Test
    void testTextBeforeTwoVariables()
    {
        String test1 = "varvar${var3}${var1}";
        when(bddVariableContext.getVariable(VAR1)).thenReturn(VALUE1);
        when(bddVariableContext.getVariable(VAR3)).thenReturn(VALUE3);
        Object actualValue = parameterAdaptor.convert(test1);
        assertEquals("varvarvalue3value1", actualValue);
    }

    @Test
    void testVariablesAndExpressions()
    {
        String test1 = "${var1}|#{expr}";
        when(bddVariableContext.getVariable(VAR1)).thenReturn(VALUE1);
        Object actualValue = parameterAdaptor.convert(test1);
        assertEquals("value1|#{expr}", actualValue);
    }

    @Test
    void testVariablesDynamicValue()
    {
        DynamicVariable dynamicVariable = mock(DynamicVariable.class);
        when(bddVariableContext.getVariable(VAR1)).thenReturn(null);
        parameterAdaptor.setDynamicVariables(Map.of(VAR1, dynamicVariable));
        when(dynamicVariable.getValue()).thenReturn(VALUE1);
        Object actualValue = parameterAdaptor.convert(VAR1_VARIABLE);
        assertEquals(VALUE1, actualValue);
    }

    @Test
    void testVariablesDynamicValueNoProvider()
    {
        DynamicVariable dynamicVariable = mock(DynamicVariable.class);
        String test1 = VAR1_VARIABLE;
        when(bddVariableContext.getVariable(VAR1)).thenReturn(null);
        parameterAdaptor.setDynamicVariables(Map.of("var2", dynamicVariable));
        Object actualValue = parameterAdaptor.convert(test1);
        assertEquals(test1, actualValue);
    }

    @Test
    void testNoConversion()
    {
        String value = "varvar";
        Object actualValue = parameterAdaptor.convert(value);
        assertEquals(value, actualValue);
    }

    @Test
    void testSimpleVariable()
    {
        when(bddVariableContext.getVariable(VAR3)).thenReturn(VALUE3);
        Object actualValue = parameterAdaptor.convert(VAR3_PROPERTY);
        assertEquals(VALUE3, actualValue);
    }

    @Test
    void testNullVariable()
    {
        when(bddVariableContext.getVariable(VAR3)).thenReturn(null);
        String value = VAR3_PROPERTY;
        Object actualValue = parameterAdaptor.convert(value);
        assertEquals(value, actualValue);
    }

    @Test
    void testConvertNullVariable()
    {
        Object actualValue = parameterAdaptor.convert(null);
        assertNull(actualValue);
    }
}
