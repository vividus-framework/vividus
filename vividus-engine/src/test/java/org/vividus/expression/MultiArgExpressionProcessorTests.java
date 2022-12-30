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

package org.vividus.expression;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class MultiArgExpressionProcessorTests
{
    private static final String EXPRESSION_NAME = "expr";
    private static final Function<List<String>, String> EVALUATOR = args -> String.join("+", args);

    @ParameterizedTest
    @CsvSource({
            "'expr(x, y)',         x+y",
            "'expr(x,y)',          x+y",
            "'expr( x , y )',      x+y",
            "'expr(x\n, y\n)',     x+y",
            "'expr(x\r\n, y\r\n)', x+y",
            "'expr(\nx\n,\ny\n)',  x+y"
    })
    void shouldMatchExpressionExactNumberOfArguments(String expression, String expected)
    {
        var processor = new MultiArgExpressionProcessor<>(EXPRESSION_NAME, 2, EVALUATOR);
        var actual = processor.execute(expression);
        assertEquals(Optional.of(expected), actual);
    }

    @ParameterizedTest
    @CsvSource({
            "'expr(x, y, z)',      'x+y+z'",
            "'expr(x,y,z)',        'x+y+z'",
            "'expr(x, y)',         x+y",
            "'expr(x,y)',          x+y"
    })
    void shouldMatchExpressionWithDifferentNumberOfArguments(String expression, String expected)
    {
        var processor = new MultiArgExpressionProcessor<>(EXPRESSION_NAME, 2, 3, EVALUATOR);
        var actual = processor.execute(expression);
        assertEquals(Optional.of(expected), actual);
    }

    @ParameterizedTest
    @CsvSource({
            "expr(x,y'",
            "expr( x,y", EXPRESSION_NAME,
            "exp",
            "expr(x, y)",
            "''"
    })
    void shouldNotMatchExpression(String expression)
    {
        var processor = new MultiArgExpressionProcessor<>(EXPRESSION_NAME, 2, EVALUATOR);
        var actual = processor.execute(expression);
        assertEquals(Optional.empty(), actual);
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
        "expr(x)       | The expected number of arguments for 'expr' expression is 2, but found 1 argument: 'x'",
        "expr()        | The expected number of arguments for 'expr' expression is 2, but found 0 arguments",
        "expr(x, y, z) | The expected number of arguments for 'expr' expression is 2, but found 3 arguments: 'x, y, z'",
        "expr(x,y,z)   | The expected number of arguments for 'expr' expression is 2, but found 3 arguments: 'x,y,z'"
    })
    void shouldFailOnInvalidNumberOfParameters(String expression, String error)
    {
        var processor = new MultiArgExpressionProcessor<>(EXPRESSION_NAME, 2, EVALUATOR);
        var exception = assertThrows(IllegalArgumentException.class, () -> processor.execute(expression));
        assertEquals(error, exception.getMessage());
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
        // CHECKSTYLE:OFF
        // @formatter:off
        "expr()        | The expected number of arguments for 'expr' expression is from 1 to 3, but found 0 arguments",
        "expr(x,y,z,a) | The expected number of arguments for 'expr' expression is from 1 to 3, but found 4 arguments: 'x,y,z,a'",
        // @formatter:on
        // CHECKSTYLE:ON
    })
    void shouldFailOnInvalidNumberOfParametersForExpressionAcceptingRange(String expression, String error)
    {
        var processor = new MultiArgExpressionProcessor<>(EXPRESSION_NAME, 1, 3, EVALUATOR);
        var exception = assertThrows(IllegalArgumentException.class, () -> processor.execute(expression));
        assertEquals(error, exception.getMessage());
    }
}
