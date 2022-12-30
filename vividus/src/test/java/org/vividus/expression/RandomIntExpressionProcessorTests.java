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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class RandomIntExpressionProcessorTests
{
    private final RandomIntExpressionProcessor processor = new RandomIntExpressionProcessor();

    @ParameterizedTest
    @CsvSource({
            // @formatter:off
            "'randomInt(1.1, 1)', first,  1.1",
            "'randomInt(0, 1.1)', second, 1.1",
            "'randomInt(, 1)',    first,  ''",
            "'randomInt(1, )',    second, ''"
            // @formatter:on
    })
    void shouldFailValidationOfArguments(String expression, String argIndex, String invalidArg)
    {
        var exception = assertThrows(IllegalArgumentException.class, () -> processor.execute(expression));
        assertEquals(
                "The " + argIndex + " argument of 'randomInt' expression must be an integer, but found: '" + invalidArg
                        + "'", exception.getMessage());
    }

    @ParameterizedTest
    @CsvSource({
            // @formatter:off
            "'randomInt(1, 10)',      1,  10",
            "'randomInt(100, 999)', 100, 999",
            "'randomInt(-5, 5)',     -5,   5",
            "'randomInt(-5, -2)',    -5,  -2",
            "'randomInt(1,1)',        1,   1"
            // @formatter:on
    })
    void shouldGenerateRandomIntSuccessfully(String expression, int minInclusive, int maxInclusive)
    {
        var expressionResult = processor.execute(expression);
        assertTrue(expressionResult.isPresent());
        assertThat(expressionResult.get(), allOf(greaterThanOrEqualTo(minInclusive), lessThanOrEqualTo(maxInclusive)));
    }
}
