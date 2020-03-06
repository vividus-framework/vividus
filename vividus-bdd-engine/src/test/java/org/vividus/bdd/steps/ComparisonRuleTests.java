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

import static org.hamcrest.Matchers.comparesEqualTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Optional;
import java.util.stream.Stream;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ComparisonRuleTests
{
    private static final String INCORRECT_SIGN = "incorrectSign";
    private static final int VARIABLE = 42;

    static Stream<Arguments> getComparisonRule()
    {
        // @formatter:off
        return Stream.of(
            Arguments.of("<", ComparisonRule.LESS_THAN),
            Arguments.of("<=", ComparisonRule.LESS_THAN_OR_EQUAL_TO),
            Arguments.of(">", ComparisonRule.GREATER_THAN),
            Arguments.of(">=", ComparisonRule.GREATER_THAN_OR_EQUAL_TO),
            Arguments.of("=", ComparisonRule.EQUAL_TO),
            Arguments.of("!=", ComparisonRule.NOT_EQUAL_TO)
        );
        // @formatter:on
    }

    static Stream<Arguments> getComparisonRuleAsString()
    {
        // @formatter:off
        return Stream.of(
            Arguments.of("less than", ComparisonRule.LESS_THAN),
            Arguments.of("less than or equal to", ComparisonRule.LESS_THAN_OR_EQUAL_TO),
            Arguments.of("greater than", ComparisonRule.GREATER_THAN),
            Arguments.of("greater than or equal to", ComparisonRule.GREATER_THAN_OR_EQUAL_TO),
            Arguments.of("equal to", ComparisonRule.EQUAL_TO),
            Arguments.of("not equal to", ComparisonRule.NOT_EQUAL_TO)
        );
        // @formatter:on
    }

    static Stream<Arguments> getVariableMatcher()
    {
        // @formatter:off
        return Stream.of(
            Arguments.of(ComparisonRule.LESS_THAN, lessThan(VARIABLE)),
            Arguments.of(ComparisonRule.LESS_THAN_OR_EQUAL_TO, lessThanOrEqualTo(VARIABLE)),
            Arguments.of(ComparisonRule.GREATER_THAN, greaterThan(VARIABLE)),
            Arguments.of(ComparisonRule.GREATER_THAN_OR_EQUAL_TO, greaterThanOrEqualTo(VARIABLE)),
            Arguments.of(ComparisonRule.EQUAL_TO, comparesEqualTo(VARIABLE)),
            Arguments.of(ComparisonRule.NOT_EQUAL_TO, not(comparesEqualTo(VARIABLE)))
        );
        // @formatter:on
    }

    @ParameterizedTest
    @MethodSource("getComparisonRule")
    void comparisonRuleParsePositiveTest(String sign, ComparisonRule rule)
    {
        assertEquals(ComparisonRule.parse(sign), rule);
    }

    @ParameterizedTest
    @MethodSource("getComparisonRule")
    void comparisonRuleBySignTest(String sign, ComparisonRule rule)
    {
        assertEquals(rule, ComparisonRule.bySign(sign).get());
    }

    @Test
    void comparisonRuleBySignEmptyOptionalTest()
    {
        assertEquals(Optional.empty(), ComparisonRule.bySign("==="));
    }

    @Test
    void comparisonRuleParseNegativeTest()
    {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> ComparisonRule.parse(INCORRECT_SIGN));
        assertEquals("Unknown comparison sign: '" + INCORRECT_SIGN + "'", exception.getMessage());
    }

    @ParameterizedTest
    @MethodSource("getVariableMatcher")
    void testGetComparisonRule(ComparisonRule rule, Matcher<?> matcher)
    {
        assertEquals(rule.getComparisonRule(VARIABLE).toString(), matcher.toString());
    }

    @Test
    void shouldReturnEqualToMatcherForStringVariables()
    {
        String variable = "variable";
        assertEquals(ComparisonRule.EQUAL_TO.getComparisonRule(variable).toString(),
                Matchers.equalTo(variable).toString());
    }

    @ParameterizedTest
    @MethodSource("getComparisonRuleAsString")
    void shouldReturnComparisonRulesInPlainText(String expecred, ComparisonRule toCheck)
    {
        assertEquals(expecred, toCheck.toString());
    }
}
