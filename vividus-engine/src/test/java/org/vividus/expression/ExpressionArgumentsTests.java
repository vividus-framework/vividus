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
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;

@SuppressWarnings("PMD.AvoidDuplicateLiterals")
@ExtendWith(MockitoExtension.class)
class ExpressionArgumentsTests
{
    static Stream<Arguments> unlimitedExpressionArguments()
    {
        // CHECKSTYLE:OFF
        // @formatter:off
        return Stream.of(
                arguments("a,b", List.of("a", "b")),
                arguments("a, b", List.of("a", "b")),
                arguments(" a,b ", List.of("a", "b")),
                arguments(" a, b ", List.of("a", "b")),
                arguments("a,b,c", List.of("a", "b", "c")),
                arguments("a, b, c", List.of("a", "b", "c")),
                arguments("\"\"\"a\"\"\", b", List.of("a", "b")),
                arguments("\"\"\"a\"\"\", \"\"\"b\"\"\"", List.of("a", "b")),
                arguments("\"\"\"a,b\"\"\", \"\"\"c\"\"\"", List.of("a,b", "c")),
                arguments("\"\"\"a\\,b\"\"\", \"\"\"c\"\"\"", List.of("a\\,b", "c")),
                arguments("\"\"\" a, b \"\"\", \"\"\" c \"\"\"", List.of(" a, b ", " c ")),
                arguments("a\\,b,c", List.of("a,b", "c")),
                arguments("a,b,c\\", List.of("a", "b", "c\\")),
                arguments("\\a,b,c", List.of("\\a", "b", "c")),
                arguments(",b,c", List.of("", "b", "c")),
                arguments("a,b,", List.of("a", "b", ""))
        );
        // @formatter:on
        // CHECKSTYLE:ON
    }

    @ParameterizedTest
    @MethodSource("unlimitedExpressionArguments")
    void shouldParseUnlimitedArguments(String commaSeparatedArguments, List<String> parsedArguments)
    {
        var argumentsMatcher = new ExpressionArguments(commaSeparatedArguments);
        assertEquals(parsedArguments, argumentsMatcher.getArguments());
    }

    static Stream<Arguments> limitedExpressionArguments()
    {
        // CHECKSTYLE:OFF
        // @formatter:off
        return Stream.of(
                arguments("a,b", List.of("a", "b")),
                arguments("a, b", List.of("a", "b")),
                arguments(" a,b ", List.of("a", "b")),
                arguments(" a, b ", List.of("a", "b")),
                arguments("a,b,c", List.of("a", "b,c")),
                arguments("a, b, c", List.of("a", "b, c")),
                arguments("\"\"\"a\"\"\", b", List.of("a", "b")),
                arguments("\"\"\"a\"\"\", \"\"\"b\"\"\"", List.of("a", "b")),
                arguments("\"\"\"a,b\"\"\", \"\"\"c\"\"\"", List.of("a,b", "c")),
                arguments("\"\"\"a\\,b\"\"\", \"\"\"c\"\"\"", List.of("a\\,b", "c")),
                arguments("\"\"\" a, b \"\"\", \"\"\" c \"\"\"", List.of(" a, b ", " c ")),
                arguments("a\\,b,c", List.of("a,b", "c")),
                arguments("a,b,c\\", List.of("a", "b,c\\")),
                arguments("\\a,b,c", List.of("\\a", "b,c")),
                arguments(",b,c", List.of("", "b,c")),
                arguments("a,b,", List.of("a", "b,"))
        );
        // @formatter:on
        // CHECKSTYLE:ON
    }

    @ParameterizedTest
    @MethodSource("limitedExpressionArguments")
    void shouldParseLimitedArguments(String commaSeparatedArguments, List<String> parsedArguments)
    {
        var argumentsMatcher = new ExpressionArguments(commaSeparatedArguments, 2);
        assertEquals(parsedArguments, argumentsMatcher.getArguments());
    }
}
