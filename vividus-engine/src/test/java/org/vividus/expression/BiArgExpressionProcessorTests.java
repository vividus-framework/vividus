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

import java.util.Optional;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class BiArgExpressionProcessorTests
{
    private final BiArgExpressionProcessor<String> processor = new BiArgExpressionProcessor<>("expr", (x, y) -> x + y);

    @ParameterizedTest
    @ValueSource(strings = {
            "expr(x, y)",
            "expr(x,y)",
            "expr( x , y )",
            "expr(x\n, y\n)",
            "expr(x\r\n, y\r\n)",
            "expr(\nx\n,\ny\n)"
    })
    void shouldMatchExpression(String expression)
    {
        Optional<String> actual = processor.execute(expression);
        assertEquals(Optional.of("xy"), actual);
    }

    @ParameterizedTest
    @CsvSource({
            "expr(x,y'",
            "expr( x,y",
            "expr",
            "exp",
            "expr(x, y)",
            "''"
    })
    void shouldNotMatchExpression(String expression)
    {
        Optional<String> actual = processor.execute(expression);
        assertEquals(Optional.empty(), actual);
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "expr(x,y,z) | The expected number of arguments for 'expr' expression is 2, but found 3 arguments: 'x,y,z'",
            "expr(x)     | The expected number of arguments for 'expr' expression is 2, but found 1 argument: 'x'",
            "expr()      | The expected number of arguments for 'expr' expression is 2, but found 0 arguments"
    })
    void shouldFailOnInvalidNumberOfParameters(String expression, String error)
    {
        var exception = assertThrows(IllegalArgumentException.class, () -> processor.execute(expression));
        assertEquals(error, exception.getMessage());
    }
}
