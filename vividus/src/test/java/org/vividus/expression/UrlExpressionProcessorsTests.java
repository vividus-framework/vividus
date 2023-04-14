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

package org.vividus.expression;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class UrlExpressionProcessorsTests
{
    private static final String EXAMPLE_URL = "https://www.example.com/one/two?id=0&name=temp";
    private static final String URL_HOST = "www.example.com";
    private static final String URL_PATH = "/one/two";
    private static final String URL_QUERY = "id=0&name=temp";

    private final UrlExpressionProcessors processors = new UrlExpressionProcessors();

    static Stream<Arguments> validExpressionSource()
    {
        // CHECKSTYLE:OFF
        return Stream.of(
                arguments(String.format("extractHostFromUrl(%s)", EXAMPLE_URL),  URL_HOST),
                arguments(String.format("extractPathFromUrl(%s)", EXAMPLE_URL),  URL_PATH),
                arguments(String.format("extractQueryFromUrl(%s)", EXAMPLE_URL), URL_QUERY)
        );
        // CHECKSTYLE:ON
    }

    @ParameterizedTest(name = "{index}: for expression \"{0}\", result is \"{1}\"")
    @MethodSource("validExpressionSource")
    void shouldExecuteSuccessfully(String expression, String expected)
    {
        assertEquals(expected, processors.execute(expression).get());
    }
}
