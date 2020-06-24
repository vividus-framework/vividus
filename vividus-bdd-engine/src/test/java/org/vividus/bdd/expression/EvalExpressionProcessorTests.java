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

package org.vividus.bdd.expression;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class EvalExpressionProcessorTests
{
    private static final String FALSE = "false";
    private final EvalExpressionProcessor processor = new EvalExpressionProcessor();

    @SuppressWarnings("unused")
    private static Stream<Arguments> evalArguments()
    {
        return Stream.of(
                Arguments.of("eval(null)", "null"),
                Arguments.of("eval(16 + 2 * 6)", "28"),
                Arguments.of("eval((16 + 2) * 6)", "108"),
                Arguments.of("eval(100 / 5 - 16 * 2 + 6)", "-6"),
                Arguments.of("eval(`string\n1` == `string\n1`)", "true"),
                Arguments.of("eval(`string\n1` == `string1`)", FALSE),
                Arguments.of("eval(`string\n1` == 'string1')", FALSE),
                Arguments.of("eval(var t = 20; var s = function(x, y) {x + y + t}; t = 54; s(15, 7))", "42")
        );
    }

    @ParameterizedTest(name = "{index}: for expression \"{0}\"")
    @MethodSource("evalArguments")
    void testExecuteMatchingExpression(String expression, String expected)
    {
        assertEquals(Optional.of(expected), processor.execute(expression));
    }

    @Test
    void testExecuteNonMatchingExpression()
    {
        assertEquals(Optional.empty(), processor.execute("evaluate(1+1)"));
    }
}
