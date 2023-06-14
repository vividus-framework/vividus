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

package org.vividus.accessibility.model.axe;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class AxeOptionsTests
{
    private static final String STANDARD = "WCAG2A";
    private static final String RULE = "rule";
    private static final String TAG = "tag";

    @Test
    void shouldReturnRulesAsString()
    {
        AxeOptions options = AxeOptions.forRules(List.of("button-name", "duplicate-id-active"));
        assertEquals("button-name, duplicate-id-active rules", options.getStandardOrRulesAsString());
        assertEquals(RULE, options.getType());
    }

    @Test
    void shouldReturnRuleAsString()
    {
        AxeOptions options = AxeOptions.forRules(List.of("html-has-lang"));
        assertEquals("html-has-lang rule", options.getStandardOrRulesAsString());
        assertEquals(RULE, options.getType());
    }

    @Test
    void shouldReturnStandardAsString()
    {
        AxeOptions options = AxeOptions.forStandard(STANDARD);
        assertEquals(STANDARD + " standard", options.getStandardOrRulesAsString());
        assertEquals(TAG, options.getType());
    }

    static Stream<Arguments> defaultStandards()
    {
        // CHECKSTYLE:OFF
        return Stream.of(
                arguments("WCAG2xA", List.of("wcag2a", "wcag21a")),
                arguments("WCAG2xAA", List.of("wcag2a", "wcag21a", "wcag2aa", "wcag21aa", "wcag22aa")),
                arguments("WCAG2xAAA", List.of("wcag2a", "wcag21a", "wcag2aa", "wcag21aa", "wcag22aa", "wcag2aaa"))
        );
        // CHECKSTYLE:ON
    }

    static Stream<Arguments> caseSensitiveStandards()
    {
        // CHECKSTYLE:OFF
        return Stream.of(
                arguments("act", List.of("ACT")),
                arguments("ttv5", List.of("TTv5")),
                arguments("tt1.A", List.of("TT1.a"))
        );
        // CHECKSTYLE:ON
    }

    @ParameterizedTest
    @MethodSource({"defaultStandards", "caseSensitiveStandards"})
    void shouldReturnOptionsForStandards(String tag, List<String> values)
    {
        AxeOptions options = AxeOptions.forStandard(tag);
        assertEquals(values, options.getValues());
        assertEquals(TAG, options.getType());
    }
}
