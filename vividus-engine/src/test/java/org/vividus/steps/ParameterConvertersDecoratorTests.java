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

package org.vividus.steps;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.function.Function;

import org.apache.commons.lang3.reflect.TypeLiteral;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.embedder.StoryControls;
import org.jbehave.core.expressions.ExpressionResolver;
import org.jbehave.core.io.StoryLoader;
import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.model.ExamplesTableFactory;
import org.jbehave.core.model.TableTransformers;
import org.jbehave.core.steps.ParameterConverters.FunctionalParameterConverter;
import org.jbehave.core.steps.ParameterConverters.NumberConverter;
import org.jbehave.core.steps.ParameterConverters.StringConverter;
import org.jbehave.core.steps.StepMonitor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.vividus.converter.ResolvingPlaceholdersExamplesTableConverter;
import org.vividus.variable.Variables;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class ParameterConvertersDecoratorTests
{
    private static final String VALUE = "42";

    @Mock private StepMonitor stepMonitor;
    @Mock private StoryLoader storyLoader;
    @Mock private Configuration configuration;
    @Mock private VariableResolver variableResolver;
    @Mock private ExpressionResolver expressionResolver;

    private ParameterConvertersDecorator parameterConverters;

    @BeforeEach
    void beforeEach()
    {
        when(configuration.stepMonitor()).thenReturn(stepMonitor);
        when(configuration.keywords()).thenReturn(new Keywords());
        when(configuration.storyLoader()).thenReturn(storyLoader);
        var placeholderResolver = new PlaceholderResolver(variableResolver, expressionResolver, new StoryControls());
        parameterConverters = new ParameterConvertersDecorator(configuration, placeholderResolver);
        var examplesTableFactory = new ExamplesTableFactory(storyLoader, new TableTransformers());
        var examplesTableConverter = new ResolvingPlaceholdersExamplesTableConverter(examplesTableFactory,
                placeholderResolver);
        parameterConverters.addConverters(examplesTableConverter);
    }

    @Test
    void shouldReplaceVariables()
    {
        shouldReplaceVariables(s -> s);
    }

    @Test
    void shouldReplaceVariablesConvertingBytesToString()
    {
        shouldReplaceVariables(s -> s.getBytes(StandardCharsets.UTF_8));
    }

    void shouldReplaceVariables(Function<String, Object> variableTransformer)
    {
        String value = "var${var}";
        String convertedValue = "Varvar\r\nVarvar2";
        Type type = String.class;
        when(variableResolver.resolve(value)).thenReturn(variableTransformer.apply(convertedValue));
        when(expressionResolver.resolveExpressions(false, convertedValue)).thenReturn(convertedValue);
        when(variableResolver.resolve(convertedValue)).thenReturn(convertedValue);
        String actual = (String) parameterConverters.convert(value, type);
        assertEquals("Varvar" + System.lineSeparator() + "Varvar2", actual);
        verify(stepMonitor).convertedValueOfType(convertedValue, type, actual, queueOf(StringConverter.class));
    }

    @Test
    void shouldReplaceNestedVariablesAndExpressions()
    {
        String value = "${var#{eval(${nestedVar} + 1)}}";
        Type type = String.class;
        String valueWithNestedVarReplaced = "${var#{eval(2 + 1)}}";
        when(variableResolver.resolve(value)).thenReturn(valueWithNestedVarReplaced);
        String valueWithExpressionProcessed = "${var3}";
        when(expressionResolver.resolveExpressions(false, valueWithNestedVarReplaced)).thenReturn(
                valueWithExpressionProcessed);
        String valueWithExternalVarReplaced = "value";
        when(variableResolver.resolve(valueWithExpressionProcessed)).thenReturn(valueWithExternalVarReplaced);
        when(expressionResolver.resolveExpressions(false, valueWithExternalVarReplaced))
                .thenReturn(valueWithExternalVarReplaced);
        when(variableResolver.resolve(valueWithExternalVarReplaced)).thenReturn(valueWithExternalVarReplaced);
        String actual = (String) parameterConverters.convert(value, type);
        assertEquals(valueWithExternalVarReplaced, actual);
        verify(stepMonitor).convertedValueOfType(valueWithExternalVarReplaced, type, actual,
                queueOf(StringConverter.class));
    }

    @Test
    void shouldResolveCircularVariableReferences()
    {
        String prefix = "before-";
        String postfix = "-after";
        int postfixLength = postfix.length();
        int prefixLength = prefix.length();

        String value = prefix + "${value}" + postfix;
        Type type = String.class;
        when(variableResolver.resolve(any())).then((Answer<String>) invocation -> {
            String arg = invocation.getArgument(0).toString();
            int endIndex = arg.length() - postfixLength;
            return arg.substring(0, prefixLength) + "#{removeWrappingDoubleQuotes(" + arg.substring(prefixLength,
                    endIndex) + ")}" + arg.substring(endIndex);
        });
        when(expressionResolver.resolveExpressions(eq(false), any())).then(
                (Answer<String>) invocation -> invocation.getArgument(1));
        String actual = (String) parameterConverters.convert(value, type);
        String resolvedVariable = prefix + "#{removeWrappingDoubleQuotes(${value})}" + postfix;
        assertEquals(resolvedVariable, actual);
        verify(stepMonitor).convertedValueOfType(resolvedVariable, type, actual, queueOf(StringConverter.class));
        verify(variableResolver, times(17)).resolve(any());
        verify(expressionResolver, times(17)).resolveExpressions(eq(false), any());
    }

    private Queue<Class<?>> queueOf(Class<?> clazz)
    {
        return new LinkedList<>(List.of(clazz));
    }

    @Test
    void shouldConvertToEmptyList()
    {
        String value = "  ";
        List<Integer> convertedValue = List.of();
        Type type = new TypeLiteral<List<Integer>>() { }.value;
        when(expressionResolver.resolveExpressions(false, value)).thenReturn(value);
        when(variableResolver.resolve(value)).thenReturn(value);
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
        when(expressionResolver.resolveExpressions(false, value)).thenReturn(value);
        when(variableResolver.resolve(value)).thenReturn(value);
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
        when(expressionResolver.resolveExpressions(false, VALUE)).thenReturn(VALUE);
        when(variableResolver.resolve(VALUE)).thenReturn(VALUE);
        Optional<Integer> actual = (Optional<Integer>) parameterConverters.convert(VALUE, type);
        assertEquals(convertedValue, actual);
        verify(stepMonitor).convertedValueOfType(VALUE, Integer.class, baseConvertedValue,
                new LinkedList<>(List.of(NumberConverter.class)));
    }

    @Test
    void shouldConvertStringsProcessedByExpressionAdapter()
    {
        when(variableResolver.resolve(VALUE)).thenReturn(VALUE);
        when(expressionResolver.resolveExpressions(false, VALUE)).thenReturn(VALUE);
        Type type = int.class;
        Object actual = parameterConverters.convert(VALUE, type);
        assertEquals(Integer.parseInt(VALUE), actual);
        verify(stepMonitor).convertedValueOfType(VALUE, type, actual, new LinkedList<>(List.of(NumberConverter.class)));
    }

    @Test
    void shouldReturnNotStringsAsIsIfExpectedAndReceivedTypesMatch()
    {
        Integer number = Integer.parseInt(VALUE);
        when(variableResolver.resolve(VALUE)).thenReturn(number);
        Type type = Integer.class;
        Object actual = parameterConverters.convert(VALUE, type);
        assertEquals(number, actual);
        verifyNoInteractions(expressionResolver);
    }

    @Test
    void shouldConvertNotStringsIfExpectedAndReceivedTypesMismatch()
    {
        Integer number = Integer.parseInt(VALUE);
        when(variableResolver.resolve(VALUE)).thenReturn(number);
        Type type = Float.class;
        Object actual = parameterConverters.convert(VALUE, type);
        assertEquals(Float.parseFloat(VALUE), actual);
        verify(stepMonitor).convertedValueOfType(VALUE, type, actual, new LinkedList<>(List.of(NumberConverter.class)));
    }

    @Test
    void shouldReturnStringsAsIsProcessedByExpressionAdapterForObjectType()
    {
        when(variableResolver.resolve(VALUE)).thenReturn(VALUE);
        Type type = Object.class;
        when(expressionResolver.resolveExpressions(false, VALUE)).thenReturn(VALUE);
        String actual = (String) parameterConverters.convert(VALUE, type);
        assertEquals(VALUE, actual);
    }

    @Test
    void shouldReturnValueAsIsIfAdaptedValueTypeAssignableFromRequestedType()
    {
        Type type = new TypeLiteral<List<Map<String, Object>>>() { }.value;
        List<Map<Object, Object>> adaptedValue = List.of(Map.of());
        when(variableResolver.resolve(VALUE)).thenReturn(adaptedValue);
        assertEquals(adaptedValue, parameterConverters.convert(VALUE, type));
        verifyNoInteractions(expressionResolver);
    }

    @Test
    void shouldSkipDecorationForSubSteps()
    {
        SubSteps subSteps = mock(SubSteps.class);
        parameterConverters.addConverters(
                new FunctionalParameterConverter<>(String.class, SubSteps.class, s -> subSteps));
        assertEquals(subSteps, parameterConverters.convert("sub-steps", SubSteps.class));
        verifyNoInteractions(variableResolver, expressionResolver);
    }

    @Test
    void shouldConvertToExamplesTableWithResolvedPlaceholders()
    {
        String expressionKey = "expression";
        String expression = "#{expression}";
        String expressionValue = "expressionValue";
        String variableKey = "variable";
        String variable = "${variable}";
        String variableValue = "variableValue";
        String pathToTable = "/table-with-expression-and-variable.table";
        String tableAsString = String.format("|%s|%s|%n|%s|%s|", expressionKey, variableKey, expression, variable);
        when(expressionResolver.resolveExpressions(false, pathToTable)).thenReturn(pathToTable);
        when(variableResolver.resolve(pathToTable)).thenReturn(pathToTable);
        when(variableResolver.resolve(expression)).thenReturn(expression);
        when(expressionResolver.resolveExpressions(false, expression)).thenReturn(expressionValue);
        when(variableResolver.resolve(expressionValue)).thenReturn(expressionValue);
        when(expressionResolver.resolveExpressions(false, expressionValue)).thenReturn(expressionValue);
        when(variableResolver.resolve(variable)).thenReturn(variableValue);
        when(expressionResolver.resolveExpressions(false, variableValue)).thenReturn(variableValue);
        when(variableResolver.resolve(variableValue)).thenReturn(variableValue);
        when(storyLoader.loadResourceAsText(pathToTable)).thenReturn(tableAsString);
        Object result = parameterConverters.convert(pathToTable, ExamplesTable.class);
        assertThat(result, instanceOf(ExamplesTable.class));
        ExamplesTable table = (ExamplesTable) result;
        assertEquals(List.of(Map.of(expressionKey, expressionValue, variableKey, variableValue)), table.getRows());
    }

    @Test
    void shouldConvertToEmptyExamplesTable()
    {
        String pathToTable = "/empty-example-table.table";
        String tableAsString = "";
        when(expressionResolver.resolveExpressions(false, pathToTable)).thenReturn(pathToTable);
        when(variableResolver.resolve(pathToTable)).thenReturn(pathToTable);
        when(storyLoader.loadResourceAsText(pathToTable)).thenReturn(tableAsString);
        Object result = parameterConverters.convert(pathToTable, ExamplesTable.class);
        assertThat(result, instanceOf(ExamplesTable.class));
        ExamplesTable table = (ExamplesTable) result;
        assertTrue(table.isEmpty());
        verifyNoMoreInteractions(expressionResolver, variableResolver);
    }

    @Test
    void shouldReturnValueAsIsIfExpressionsResolvedToNotAStringType()
    {
        when(variableResolver.resolve(VALUE)).thenReturn(VALUE);
        Type type = Object.class;
        Integer expected = Integer.valueOf(42);
        when(expressionResolver.resolveExpressions(false, VALUE)).thenReturn(expected);
        assertEquals(expected, parameterConverters.convert(VALUE, type));
    }

    @Test
    void shouldReturnDataWrapper()
    {
        when(variableResolver.resolve(VALUE)).thenReturn(VALUE);
        Class<DataWrapper> dataWrapperClass = DataWrapper.class;
        byte[] expected = { 0, 1, 2 };
        when(expressionResolver.resolveExpressions(false, VALUE)).thenReturn(expected);
        Object actual = parameterConverters.convert(VALUE, dataWrapperClass);
        assertThat(actual, instanceOf(dataWrapperClass));
        assertArrayEquals(expected, ((DataWrapper) actual).getBytes());
    }

    @Test
    void shouldReturnNullIfRawExpressionsReturnNull()
    {
        when(variableResolver.resolve(Variables.VIVIDUS_NULL_EXPRESSION)).thenReturn(Variables.VIVIDUS_NULL_EXPRESSION);
        assertNull(parameterConverters.convert(Variables.VIVIDUS_NULL_EXPRESSION, String.class));
        verifyNoMoreInteractions(variableResolver, expressionResolver);
    }

    @Test
    void shouldReturnNullIfOriginalValueIsNull()
    {
        when(variableResolver.resolve(null)).thenReturn(null);
        assertNull(parameterConverters.convert(null, Object.class));
        verifyNoMoreInteractions(expressionResolver, variableResolver);
    }
}
