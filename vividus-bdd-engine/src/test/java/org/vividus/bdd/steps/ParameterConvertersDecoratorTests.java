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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.reflect.TypeLiteral;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.steps.ParameterConverters.FunctionalParameterConverter;
import org.jbehave.core.steps.ParameterConverters.NumberConverter;
import org.jbehave.core.steps.ParameterConverters.StringConverter;
import org.jbehave.core.steps.StepMonitor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class ParameterConvertersDecoratorTests
{
    private static final String VALUE = "42";

    @Mock
    private StepMonitor stepMonitor;

    @Mock
    private Configuration configuration;

    @Mock
    private ParameterAdaptor parameterAdaptor;

    @Mock
    private ExpressionAdaptor expressionAdaptor;

    private ParameterConvertersDecorator parameterConverters;

    @BeforeEach
    void beforeEach()
    {
        when(configuration.stepMonitor()).thenReturn(stepMonitor);
        parameterConverters = new ParameterConvertersDecorator(configuration, parameterAdaptor, expressionAdaptor);
    }

    @Test
    void shouldReplaceVariables()
    {
        String value = "var${var}";
        String convertedValue = "Varvar\r\nVarvar2";
        Type type = String.class;
        when(expressionAdaptor.process(convertedValue)).thenReturn(convertedValue);
        when(parameterAdaptor.convert(value)).thenReturn(convertedValue);
        String actual = (String) parameterConverters.convert(value, type);
        assertEquals("Varvar" + System.lineSeparator() + "Varvar2", actual);
        verify(stepMonitor).convertedValueOfType(convertedValue, type, actual,
                new LinkedList<>(List.of(StringConverter.class)));
    }

    @Test
    void shouldConvertToEmptyList()
    {
        String value = "  ";
        List<Integer> convertedValue = List.of();
        Type type = new TypeLiteral<List<Integer>>() { }.value;
        when(expressionAdaptor.process(value)).thenReturn(value);
        when(parameterAdaptor.convert(value)).thenReturn(value);
        List<Integer> actual = (List<Integer>) parameterConverters.convert(value, type);
        assertEquals(convertedValue, actual);
        verifyNoInteractions(stepMonitor);
    }

    @Test
    void shouldConvertToEmptyOptional()
    {
        String value = " ";
        Optional<Integer> convertedValue = Optional.empty();
        Type type = new TypeLiteral<Optional<Integer>>() { }.value;
        when(expressionAdaptor.process(value)).thenReturn(value);
        when(parameterAdaptor.convert(value)).thenReturn(value);
        Optional<Integer> actual = (Optional<Integer>) parameterConverters.convert(value, type);
        assertEquals(convertedValue, actual);
        verifyNoInteractions(stepMonitor);
    }

    @Test
    void shouldConvertToNonEmptyOptional()
    {
        Integer baseConvertedValue = Integer.valueOf(VALUE);
        Optional<Integer> convertedValue = Optional.of(baseConvertedValue);
        Type type = new TypeLiteral<Optional<Integer>>() { }.value;
        when(expressionAdaptor.process(VALUE)).thenReturn(VALUE);
        when(parameterAdaptor.convert(VALUE)).thenReturn(VALUE);
        Optional<Integer> actual = (Optional<Integer>) parameterConverters.convert(VALUE, type);
        assertEquals(convertedValue, actual);
        verify(stepMonitor).convertedValueOfType(VALUE, Integer.class, baseConvertedValue,
                new LinkedList<>(List.of(NumberConverter.class)));
    }

    @Test
    void shouldConvertStringsProcessedByExpressionAdapter()
    {
        when(parameterAdaptor.convert(VALUE)).thenReturn(VALUE);
        when(expressionAdaptor.process(VALUE)).thenReturn(VALUE);
        Type type = int.class;
        Object actual = parameterConverters.convert(VALUE, type);
        assertEquals(Integer.parseInt(VALUE), actual);
        verify(stepMonitor).convertedValueOfType(VALUE, type, actual, new LinkedList<>(List.of(NumberConverter.class)));
    }

    @Test
    void shouldReturnNotStringsAsIsIfExpectedAndReceivedTypesMatch()
    {
        Integer number = Integer.parseInt(VALUE);
        when(parameterAdaptor.convert(VALUE)).thenReturn(number);
        Type type = Integer.class;
        Object actual = parameterConverters.convert(VALUE, type);
        assertEquals(number, actual);
        verifyNoInteractions(expressionAdaptor);
    }

    @Test
    void shouldConvertNotStringsIfExpectedAndReceivedTypesMismatch()
    {
        Integer number = Integer.parseInt(VALUE);
        when(parameterAdaptor.convert(VALUE)).thenReturn(number);
        Type type = Float.class;
        Object actual = parameterConverters.convert(VALUE, type);
        assertEquals(Float.parseFloat(VALUE), actual);
        verify(stepMonitor).convertedValueOfType(VALUE, type, actual, new LinkedList<>(List.of(NumberConverter.class)));
    }

    @Test
    void shouldReturnStringsAsIsProcessedByExpressionAdapterForObjectType()
    {
        when(parameterAdaptor.convert(VALUE)).thenReturn(VALUE);
        Type type = Object.class;
        when(expressionAdaptor.process(VALUE)).thenReturn(VALUE);
        String actual = (String) parameterConverters.convert(VALUE, type);
        assertEquals(VALUE, actual);
    }

    @Test
    void shouldReturnValueAsIsIfAdaptedValueTypeAssignableFromRequestedType()
    {
        Type type = new TypeLiteral<List<Map<String, Object>>>() { }.value;
        List<Map<Object, Object>> adaptedValue = List.of(Map.of());
        when(parameterAdaptor.convert(VALUE)).thenReturn(adaptedValue);
        assertEquals(adaptedValue, parameterConverters.convert(VALUE, type));
        verifyNoInteractions(expressionAdaptor);
    }

    @Test
    void shouldSkipDecorationForSubSteps()
    {
        SubSteps subSteps = mock(SubSteps.class);
        parameterConverters.addConverters(new FunctionalParameterConverter<>(SubSteps.class, s -> subSteps));
        assertEquals(subSteps, parameterConverters.convert("sub-steps", SubSteps.class));
        verifyNoInteractions(parameterAdaptor, expressionAdaptor);
    }
}
