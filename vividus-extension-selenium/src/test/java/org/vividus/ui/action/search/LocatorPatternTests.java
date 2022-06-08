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

package org.vividus.ui.action.search;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.of;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class LocatorPatternTests
{
    private static final String VALUE_1 = "value1";
    private static final String VALUE = "value";
    private static final String VALUE_2 = "value2";

    static Stream<Arguments> patternsProvider()
    {
        return Stream.of(
                of("$%%%%$$",           0, new String[]{VALUE},                     "$%%$$"),
                of("%%%s",              1, new String[]{VALUE},                     "%value"),
                of("%s",                1, new String[]{VALUE, VALUE_1},            VALUE),
                of("%s-%1$s",           1, new String[]{VALUE},                     "value-value"),
                of("%1$s-%s-%1$s",      1, new String[]{VALUE},                     "value-value-value"),
                of("%2$s-%s-%s-%1$s",   2, new String[]{VALUE,  VALUE_1},           "value1-value-value1-value"),
                of("%3$s-%s-%2$s-%2$s", 3, new String[]{VALUE,  VALUE_1, VALUE_2 }, "value2-value-value1-value1"),
                of("%2$s-%s-%s-%s",     3, new String[]{VALUE, VALUE_1, VALUE_2 },  "value1-value-value1-value2"));
    }

    @ParameterizedTest
    @MethodSource("patternsProvider")
    void shouldCountNumberOfParameters(String pattern, int expectedParametersCount, String[] args, String expectedValue)
    {
        assertEquals(expectedParametersCount, new LocatorPattern("xpath", pattern).getParametersQuantity());
        assertEquals(expectedValue, String.format(pattern, args));
    }
}
