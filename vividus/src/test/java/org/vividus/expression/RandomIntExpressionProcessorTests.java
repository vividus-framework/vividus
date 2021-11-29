/*
 * Copyright 2019-2021 the original author or authors.
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
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class RandomIntExpressionProcessorTests
{
    private final RandomIntExpressionProcessor processor = new RandomIntExpressionProcessor();

    @ParameterizedTest
    @ValueSource(strings = {
        "randomInt(3)",
        "randomInt(-3)",
        "randomInt(+3)",
        "randomInt(1.1, 1)",
        "randomInt(0, 1.1)",
        "randomInt(0, 1, 2)",
        "randomInt(1aef)",
        "randomInt(-)",
        "randomInt()",
        "randomInt(, 1)",
        "randomInt(1, )"
    })
    void testDoesNotMatch(String expression)
    {
        assertFalse(processor.execute(expression).isPresent());
    }

    @ParameterizedTest
    @CsvSource({
        "'randomInt(1, 10)',      1,  10",
        "'randomInt(100, 999)', 100, 999",
        "'randomInt(-5, 5)',     -5,   5",
        "'randomInt(-5, -2)',    -5,  -2",
        "'randomInt(1,1)',        1,   1"
    })
    void testRandomInt(String expression, Integer minInclusive, Integer maxInclusive)
    {
        assertThat(processor.execute(expression).get(),
                allOf(greaterThanOrEqualTo(minInclusive), lessThanOrEqualTo(maxInclusive)));
    }
}
