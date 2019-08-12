/*
 * Copyright 2019 the original author or authors.
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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;

import org.hamcrest.Matcher;
import org.hamcrest.core.IsEqual;
import org.hamcrest.core.IsNot;
import org.hamcrest.core.StringContains;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class StringComparisonRuleTests
{
    static Stream<Arguments> dataProvider()
    {
        // @formatter:off
        return Stream.of(
            Arguments.of(StringComparisonRule.IS_EQUAL_TO,      IsEqual.class),
            Arguments.of(StringComparisonRule.CONTAINS,         StringContains.class),
            Arguments.of(StringComparisonRule.DOES_NOT_CONTAIN, IsNot.class)
        );
        // @formatter:on
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    void testGetMatcher(StringComparisonRule comparisonRule, Class<? extends Matcher<String>> matcherClass)
    {
        assertEquals(matcherClass, comparisonRule.createMatcher("string").getClass());
    }
}
