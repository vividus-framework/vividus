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

import static com.github.valfirst.slf4jtest.LoggingEvent.error;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.apache.commons.lang3.StringUtils;
import org.jbehave.core.embedder.StoryControls;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.expression.DelegatingExpressionProcessor;
import org.vividus.expression.IExpressionProcessor;
import org.vividus.expression.SingleArgExpressionProcessor;

@ExtendWith({ MockitoExtension.class, TestLoggerFactoryExtension.class })
class ExpressionAdapterTests
{
    private static final String EXPRESSION_FORMAT = "#{%s}";

    private static final String EXPRESSION_KEYWORD = "target";

    private static final String EXPRESSION_KEYWORD_WITH_SEPARATOR = "tar\nget";

    private static final String EXPRESSION_RESULT = "target result with \\ and $";

    private static final String EXPRESSION_TRIM = "trim";

    private static final String EXPRESSION_CAPITALIZE = "capitalize";

    private static final String EXPRESSION_LOWER_CASE = "toLowerCase";

    private static final String UNSUPPORTED_EXPRESSION_KEYWORD = "unsupported";

    private static final String UNSUPPORTED_EXPRESSION = String.format(EXPRESSION_FORMAT,
            UNSUPPORTED_EXPRESSION_KEYWORD);

    private final TestLogger logger = TestLoggerFactory.getTestLogger(ExpressionAdaptor.class);

    @Mock private IExpressionProcessor<Object> mockedTargetProcessor;
    @Mock private IExpressionProcessor<Object> mockedAnotherProcessor;
    @Mock private StoryControls storyControls;
    @InjectMocks private ExpressionAdaptor expressionAdaptor;

    @ParameterizedTest
    @CsvSource({
        "'target',                    '#{target}',                           %s,             " + EXPRESSION_RESULT,
        "'target',                    '{#{target}}',                         {%s},           " + EXPRESSION_RESULT,
        "'target',                    '{#{target} and #{target}}',           {%1$s and %1$s}," + EXPRESSION_RESULT,
        "'target(})',                 '#{target(})}',                        %s,             " + EXPRESSION_RESULT,
        "'tar\nget',                  '#{tar\nget}',                         %s,             " + EXPRESSION_RESULT,
        "'expr(value{1})',            '#{expr(#{expr(#{expr(value{1})})})}', %s,             value{1}",
        "'generateDate(-P19Y, yyyy)', '{#{generateDate(-P19Y, yyyy)}\n}',    '{%s\n}',       " + EXPRESSION_RESULT
    })
    void testSupportedExpression(String expressionKeyword, String input, String outputFormat, String outputValue)
    {
        lenient().when(mockedTargetProcessor.execute(EXPRESSION_KEYWORD)).thenReturn(Optional.of(EXPRESSION_RESULT));
        expressionAdaptor.setProcessors(List.of(mockedTargetProcessor, mockedAnotherProcessor));
        when(mockedTargetProcessor.execute(expressionKeyword)).thenReturn(Optional.of(outputValue));
        var actual = expressionAdaptor.processRawExpression(input);
        var output = String.format(outputFormat, outputValue);
        assertEquals(output, actual);
    }

    @Test
    void testSupportedExpressionNestedExpr()
    {
        var input = "#{capitalize(#{trim(#{toLowerCase( VIVIDUS )})})}";
        var output = "Vividus";
        IExpressionProcessor<Object> processor = new DelegatingExpressionProcessor(List.of(
                new SingleArgExpressionProcessor<>(EXPRESSION_TRIM,        StringUtils::trim),
                new SingleArgExpressionProcessor<>(EXPRESSION_LOWER_CASE, StringUtils::lowerCase),
                new SingleArgExpressionProcessor<>(EXPRESSION_CAPITALIZE,  StringUtils::capitalize)
        ));
        expressionAdaptor.setProcessors(List.of(processor));
        var actual = expressionAdaptor.processRawExpression(input);
        assertEquals(output, actual);
    }

    @SuppressWarnings("unchecked")
    @Test
    void testNestedExpressionWithoutParams()
    {
        var input = "#{capitalize(#{trim(#{target})})}";
        var output = "Quetzalcoatlus";

        var mockedExpressionProcessor = (SingleArgExpressionProcessor<String>) mock(SingleArgExpressionProcessor.class);
        when(mockedExpressionProcessor.execute(EXPRESSION_KEYWORD)).thenReturn(Optional.of(" quetzalcoatlus "));
        IExpressionProcessor<?> processor = new DelegatingExpressionProcessor(List.of(
                new SingleArgExpressionProcessor<>(EXPRESSION_TRIM,        StringUtils::trim),
                new SingleArgExpressionProcessor<>(EXPRESSION_CAPITALIZE,  StringUtils::capitalize),
                mockedExpressionProcessor
        ));
        expressionAdaptor.setProcessors(List.of(processor));
        var actual = expressionAdaptor.processRawExpression(input);
        assertEquals(output, actual);
    }

    @Test
    void shouldProcessSimpleExpression()
    {
        expressionAdaptor.setProcessors(List.of(
                new SingleArgExpressionProcessor<>("toUpperCase", StringUtils::upperCase)
        ));
        var actual = expressionAdaptor.processExpression("toUpperCase(this is sparta!)");
        assertEquals("THIS IS SPARTA!", actual);
    }

    @Test
    void testUnsupportedExpression()
    {
        when(mockedAnotherProcessor.execute(UNSUPPORTED_EXPRESSION_KEYWORD)).thenReturn(Optional.empty());
        when(mockedTargetProcessor.execute(UNSUPPORTED_EXPRESSION_KEYWORD)).thenReturn(Optional.empty());
        expressionAdaptor.setProcessors(List.of(mockedTargetProcessor, mockedAnotherProcessor));
        var actual = expressionAdaptor.processRawExpression(UNSUPPORTED_EXPRESSION);
        assertEquals(UNSUPPORTED_EXPRESSION, actual, "Unsupported expression, should leave as is");

        verify(mockedTargetProcessor, times(2)).execute(UNSUPPORTED_EXPRESSION_KEYWORD);
        verify(mockedAnotherProcessor, times(2)).execute(UNSUPPORTED_EXPRESSION_KEYWORD);
    }

    @ParameterizedTest
    @CsvSource({"${var}", "'#expr'", "{expr}", "value"})
    void testNonExpression(String nonExpression)
    {
        var actual = expressionAdaptor.processRawExpression(nonExpression);
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
        var header = "|value1|value2|value3|value4|\n";
        var inputTable = header + "|#{target}|simple|#{target}|#{tar\nget}|\n|#{target (something inside#$)}|simple"
                + "|#{target}|#{tar\nget}|";
        var expectedTable = header + "|target result with \\ and $|simple|target result with \\ and $|target result"
                + " with \\ and $|\n|target result with \\ and $|simple|target result with \\ and $|target result with"
                + " \\ and $|";
        when(mockedTargetProcessor.execute("target (something inside#$)"))
                .thenReturn(Optional.of(EXPRESSION_RESULT));
        var actualTable = expressionAdaptor.processRawExpression(inputTable);
        assertEquals(expectedTable, actualTable);
        verify(mockedTargetProcessor, times(6)).execute(anyString());
    }

    @Test
    void testUnsupportedValuesInExampleTable()
    {
        when(mockedAnotherProcessor.execute(UNSUPPORTED_EXPRESSION_KEYWORD)).thenReturn(Optional.empty());
        when(mockedTargetProcessor.execute(UNSUPPORTED_EXPRESSION_KEYWORD)).thenReturn(Optional.empty());
        expressionAdaptor.setProcessors(List.of(mockedTargetProcessor, mockedAnotherProcessor));
        var inputTable = "|value1|value2|value3|\n|#{unsupported}|simple|#{unsupported}|";
        var actualTable = expressionAdaptor.processRawExpression(inputTable);
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
        var anotherExpressionKeyword = "another";
        when(mockedAnotherProcessor.execute(anotherExpressionKeyword)).thenReturn(Optional.of("another result"));

        var header = "|value1|value2|value3|value4|value5|\n";
        var inputTable = header + "|#{unsupported}|simple|#{target}|#{tar\nget}|#{another}|";
        var expectedTable = header + "|#{unsupported}|simple|target result with \\ and $|target result with \\ and"
                + " $|another result|";
        var actualTable = expressionAdaptor.processRawExpression(inputTable);
        assertEquals(expectedTable, actualTable);
    }

    @Test
    void testMixedExpressionsAndVariablesInExampleTable()
    {
        when(mockedTargetProcessor.execute(EXPRESSION_KEYWORD)).thenReturn(Optional.of(EXPRESSION_RESULT));
        expressionAdaptor.setProcessors(List.of(mockedTargetProcessor, mockedAnotherProcessor));
        var inputTable = "|value1|value2|\n|#{target}|${variable}|";
        var expectedTable = "|value1|value2|\n|target result with \\ and $|${variable}|";
        var actualTable = expressionAdaptor.processRawExpression(inputTable);
        assertEquals(expectedTable, actualTable);
    }

    @Test
    void testExpressionProcessingError()
    {
        var input = "#{exp(any)}";
        var exception = new RuntimeException();
        IExpressionProcessor<String> processor = new SingleArgExpressionProcessor<>("exp", value -> {
            throw exception;
        });
        expressionAdaptor.setProcessors(List.of(processor));
        var actualException = assertThrows(RuntimeException.class,
            () -> expressionAdaptor.processRawExpression(input));
        assertEquals(exception, actualException);
        assertThat(logger.getLoggingEvents(),
                is(List.of(error("Unable to process expression '{}'", input))));
    }

    @Test
    void shouldNotProcessExpressionsDuringDryRun()
    {
        var input = "#{expr(ess)}";
        var processor = mock(IExpressionProcessor.class);
        when(storyControls.dryRun()).thenReturn(true);
        expressionAdaptor.setProcessors(List.of(processor));
        assertEquals(input, expressionAdaptor.processRawExpression(input));
        verifyNoInteractions(processor);
    }

    @Test
    void shouldReturnNotAStringValueForATopLevelExpression()
    {
        var expression = "#{object(#{string()})}";
        lenient().when(mockedTargetProcessor.execute("string()")).thenReturn(Optional.of("result"));
        expressionAdaptor.setProcessors(List.of(mockedTargetProcessor, mockedAnotherProcessor));
        var value = new Object();
        lenient().when(mockedTargetProcessor.execute("object(result)")).thenReturn(Optional.of(value));
        assertSame(value, expressionAdaptor.processRawExpression(expression));
    }

    @ParameterizedTest
    @CsvSource({
            "'#{string(#{integer()})}', '#{string(42)}'",
            "'24 + #{integer()}',       '24 + 42'",
            "'#{integer()} + 24',       '42 + 24'"
    })
    void shouldConvertNotAStringValueToAStringForNotTopLevelExpression(String expression, String expected)
    {
        lenient().when(mockedTargetProcessor.execute("integer()")).thenReturn(Optional.of(Integer.valueOf(42)));
        expressionAdaptor.setProcessors(List.of(mockedTargetProcessor, mockedAnotherProcessor));
        assertEquals(expected, expressionAdaptor.processRawExpression(expression));
    }
}
