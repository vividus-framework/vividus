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

import static com.github.valfirst.slf4jtest.LoggingEvent.error;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.bdd.expression.IExpressionProcessor;
import org.vividus.bdd.expression.StringsExpressionProcessor;
import org.vividus.util.ILocationProvider;

@ExtendWith({ MockitoExtension.class, TestLoggerFactoryExtension.class })
class ExpressionAdapterTests
{
    private static final String EXPRESSION_FORMAT = "#{%s}";

    private static final String EXPRESSION_KEYWORD = "target";

    private static final String EXPRESSION_KEYWORD_WITH_SEPARATOR = "tar\nget";

    private static final String EXPRESSION_RESULT = "target result with \\ and $";

    private static final String UNSUPPORTED_EXPRESSION_KEYWORD = "unsupported";

    private static final String UNSUPPORTED_EXPRESSION = String.format(EXPRESSION_FORMAT,
            UNSUPPORTED_EXPRESSION_KEYWORD);

    private final TestLogger logger = TestLoggerFactory.getTestLogger(ExpressionAdaptor.class);

    @Mock
    private IExpressionProcessor mockedTargetProcessor;

    @Mock
    private IExpressionProcessor mockedAnotherProcessor;

    @InjectMocks
    private ExpressionAdaptor expressionAdaptor;

    @ParameterizedTest
    @CsvSource({
        "'target',         '#{target}',                                %s,             " + EXPRESSION_RESULT,
        "'target',         '{#{target}}',                              {%s},           " + EXPRESSION_RESULT,
        "'target',         '{#{target} and #{target}}',                {%1$s and %1$s}," + EXPRESSION_RESULT,
        "'target(})',      '#{target(})}',                             %s,             " + EXPRESSION_RESULT,
        "'tar\nget',       '#{tar\nget}',                              %s,             " + EXPRESSION_RESULT,
        "'expr(value{1})', '#{expr(#{expr(#{expr(value{1})})})}',      %s,                 value{1}"
    })
    void testSupportedExpression(String expressionKeyword, String input, String outputFormat, String outputValue)
    {
        Mockito.lenient().when(mockedTargetProcessor.execute(EXPRESSION_KEYWORD))
                .thenReturn(Optional.of(EXPRESSION_RESULT));
        expressionAdaptor.setProcessors(List.of(mockedTargetProcessor, mockedAnotherProcessor));
        when(mockedTargetProcessor.execute(expressionKeyword)).thenReturn(Optional.of(outputValue));
        String actual = expressionAdaptor.process(input);
        String output = String.format(outputFormat, outputValue);
        assertEquals(output, actual);
    }

    @Test
    void testSupportedExpressionNestedExpr()
    {
        String input = "#{capitalize(#{trim(#{toLowerCase( VIVIDUS )})})}";
        String output = "Vividus";
        ILocationProvider locationProvider = mock(ILocationProvider.class);
        IExpressionProcessor processor = new StringsExpressionProcessor(locationProvider);
        expressionAdaptor.setProcessors(List.of(processor));
        String actual = expressionAdaptor.process(input);
        assertEquals(output, actual);
    }

    @Test
    void testUnsupportedExpression()
    {
        when(mockedAnotherProcessor.execute(UNSUPPORTED_EXPRESSION_KEYWORD)).thenReturn(Optional.empty());
        when(mockedTargetProcessor.execute(UNSUPPORTED_EXPRESSION_KEYWORD)).thenReturn(Optional.empty());
        expressionAdaptor.setProcessors(List.of(mockedTargetProcessor, mockedAnotherProcessor));
        String actual = expressionAdaptor.process(UNSUPPORTED_EXPRESSION);
        assertEquals(UNSUPPORTED_EXPRESSION, actual, "Unsupported expression, should leave as is");

        verify(mockedTargetProcessor, times(2)).execute(UNSUPPORTED_EXPRESSION_KEYWORD);
        verify(mockedAnotherProcessor, times(2)).execute(UNSUPPORTED_EXPRESSION_KEYWORD);
    }

    @ParameterizedTest
    @CsvSource({"${var}", "'#expr'", "{expr}", "value"})
    void testNonExpression(String nonExpression)
    {
        String actual = expressionAdaptor.process(nonExpression);
        assertEquals(nonExpression, actual, "Not expression, should leave as is");

        verify(mockedTargetProcessor, never()).execute(nonExpression);
        verify(mockedAnotherProcessor, never()).execute(nonExpression);
    }

    @Test
    void testValuesInExampleTable()
    {
        when(mockedTargetProcessor.execute(EXPRESSION_KEYWORD)).thenReturn(Optional.of(EXPRESSION_RESULT));
        when(mockedTargetProcessor.execute(EXPRESSION_KEYWORD_WITH_SEPARATOR))
                .thenReturn(Optional.of(EXPRESSION_RESULT));
        expressionAdaptor.setProcessors(List.of(mockedTargetProcessor, mockedAnotherProcessor));
        String header = "|value1|value2|value3|value4|\n";
        String inputTable = header + "|#{target}|simple|#{target}|#{tar\nget}|\n|#{target (something inside#$)}|simple"
                + "|#{target}|#{tar\nget}|";
        String expectedTable = header + "|target result with \\ and $|simple|target result with \\ and $|target result"
                + " with \\ and $|\n|target result with \\ and $|simple|target result with \\ and $|target result with"
                + " \\ and $|";
        when(mockedTargetProcessor.execute("target (something inside#$)"))
                .thenReturn(Optional.of(EXPRESSION_RESULT));
        String actualTable = expressionAdaptor.process(inputTable);
        assertEquals(expectedTable, actualTable);
        verify(mockedTargetProcessor, times(6)).execute(anyString());
    }

    @Test
    void testUnsupportedValuesInExampleTable()
    {
        when(mockedAnotherProcessor.execute(UNSUPPORTED_EXPRESSION_KEYWORD)).thenReturn(Optional.empty());
        when(mockedTargetProcessor.execute(UNSUPPORTED_EXPRESSION_KEYWORD)).thenReturn(Optional.empty());
        expressionAdaptor.setProcessors(List.of(mockedTargetProcessor, mockedAnotherProcessor));
        String inputTable = "|value1|value2|value3|\n|#{unsupported}|simple|#{unsupported}|";
        String actualTable = expressionAdaptor.process(inputTable);
        assertEquals(inputTable, actualTable);
    }

    @Test
    void testMixedValuesInExampleTable()
    {
        when(mockedAnotherProcessor.execute(UNSUPPORTED_EXPRESSION_KEYWORD)).thenReturn(Optional.empty());
        when(mockedTargetProcessor.execute(UNSUPPORTED_EXPRESSION_KEYWORD)).thenReturn(Optional.empty());
        when(mockedTargetProcessor.execute(EXPRESSION_KEYWORD)).thenReturn(Optional.of(EXPRESSION_RESULT));
        when(mockedTargetProcessor.execute(EXPRESSION_KEYWORD_WITH_SEPARATOR))
                .thenReturn(Optional.of(EXPRESSION_RESULT));
        expressionAdaptor.setProcessors(List.of(mockedTargetProcessor, mockedAnotherProcessor));
        String anotherExpressionKeyword = "another";
        when(mockedAnotherProcessor.execute(anotherExpressionKeyword)).thenReturn(Optional.of("another result"));

        String header = "|value1|value2|value3|value4|value5|\n";
        String inputTable = header + "|#{unsupported}|simple|#{target}|#{tar\nget}|#{another}|";
        String expectedTable = header + "|#{unsupported}|simple|target result with \\ and $|target result with \\ and"
                + " $|another result|";
        String actualTable = expressionAdaptor.process(inputTable);
        assertEquals(expectedTable, actualTable);
    }

    @Test
    void testMixedExpressionsAndVariablesInExampleTable()
    {
        when(mockedTargetProcessor.execute(EXPRESSION_KEYWORD)).thenReturn(Optional.of(EXPRESSION_RESULT));
        expressionAdaptor.setProcessors(List.of(mockedTargetProcessor, mockedAnotherProcessor));
        String inputTable = "|value1|value2|\n|#{target}|${variable}|";
        String expectedTable = "|value1|value2|\n|target result with \\ and $|${variable}|";
        String actualTable = expressionAdaptor.process(inputTable);
        assertEquals(expectedTable, actualTable);
    }

    @Test
    void testExpressionProcessingError()
    {
        String input = "#{generateLocalized(number.number_between 'a','b', ru)}";
        ILocationProvider locationProvider = mock(ILocationProvider.class);
        IExpressionProcessor processor = new StringsExpressionProcessor(locationProvider);
        expressionAdaptor.setProcessors(List.of(processor));
        assertThrows(RuntimeException.class, () -> expressionAdaptor.process(input));
        assertThat(logger.getLoggingEvents(),
                is(List.of(error("Unable to process expression '{}'", input))));
    }
}
