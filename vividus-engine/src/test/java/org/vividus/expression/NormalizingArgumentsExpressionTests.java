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

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class NormalizingArgumentsExpressionTests
{
    private final TestExpressionProcessor expressionProcessor = new TestExpressionProcessor();

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "expression(input) | input",
            "expression(a,b,c) | a,b,c",
            "expression(a\\,b) | a,b"
    })
    void shouldEvaluateExpression(String expression, String expectedResult)
    {
        var actualResult = expressionProcessor.execute(expression);
        assertEquals(Optional.of(expectedResult), actualResult);
    }

    private static class TestExpressionProcessor extends AbstractExpressionProcessor<String>
            implements NormalizingArguments
    {
        protected TestExpressionProcessor()
        {
            super(Pattern.compile("^expression\\((.*)\\)$", Pattern.CASE_INSENSITIVE | Pattern.DOTALL));
        }

        @Override
        protected String evaluateExpression(Matcher expressionMatcher)
        {
            return normalize(expressionMatcher.group(1));
        }
    }
}
