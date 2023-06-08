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

package org.vividus.accessibility.factory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.accessibility.model.axe.AxeOptions;
import org.vividus.util.property.IPropertyParser;

@ExtendWith(MockitoExtension.class)
class AxeOptionsFactoryTests
{
    private static final String PREFIX = "accessibility.axe-core.tag.";
    private static final String REGRESSION_CHECKS = "regression-checks";
    private static final String TAG = "tag";
    private static final String WCAG2A = "wcag2a";

    @Mock private IPropertyParser propertyParser;
    private AxeOptionsFactory factory;

    @BeforeEach
    void init()
    {
        when(propertyParser.getPropertiesByPrefix(PREFIX)).thenReturn(Map.of(
            PREFIX + REGRESSION_CHECKS, "wcag2a, best-practice"
        ));
        factory = new AxeOptionsFactory(propertyParser);
    }

    static Stream<Arguments> defaultStandards()
    {
        // CHECKSTYLE:OFF
        return Stream.of(
                arguments("WCAG2xA", List.of(WCAG2A, "wcag21a")),
                arguments("WCAG2xAA", List.of(WCAG2A, "wcag21a", "wcag2aa", "wcag21aa", "wcag22aa")),
                arguments("WCAG2xAAA", List.of(WCAG2A, "wcag21a", "wcag2aa", "wcag21aa", "wcag22aa", "wcag2aaa"))
        );
        // CHECKSTYLE:ON
    }

    @ParameterizedTest
    @MethodSource("defaultStandards")
    void shouldReturnOptionsForDefaultStandards(String tag, List<String> values)
    {
        AxeOptions options = factory.createOptions(Pair.of(tag, null));
        assertEquals(TAG, options.getType());
        assertEquals(values, options.getValues());
    }

    @Test
    void shouldReturnOptionsForUserTag()
    {
        AxeOptions options = factory.createOptions(Pair.of(REGRESSION_CHECKS, null));
        assertEquals(TAG, options.getType());
        assertEquals(List.of(WCAG2A, "best-practice"), options.getValues());
    }

    @Test
    void shouldReturnForStandard()
    {
        AxeOptions options = factory.createOptions(Pair.of(WCAG2A, null));
        assertEquals(TAG, options.getType());
        assertEquals(List.of(WCAG2A), options.getValues());
    }

    @Test
    void shouldReturnForRule()
    {
        String rule = "rule";
        AxeOptions options = factory.createOptions(Pair.of(null, List.of(rule)));
        assertEquals(rule, options.getType());
        assertEquals(List.of(rule), options.getValues());
    }
}
