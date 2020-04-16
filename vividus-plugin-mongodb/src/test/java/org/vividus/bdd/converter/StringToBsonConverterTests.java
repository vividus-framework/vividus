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

package org.vividus.bdd.converter;

import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.stream.Stream;

import org.bson.Document;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class StringToBsonConverterTests
{
    private static final String EMPTY_JSON = "{}";

    private final StringToBsonConverter converter = new StringToBsonConverter();

    static Stream<Arguments> documents()
    {
        return Stream.of(
                arguments(EMPTY_JSON, null),
                arguments(EMPTY_JSON, ""),
                arguments("{ \"id\" : 1 }", "{\"id\":1}")
            );
    }

    @MethodSource("documents")
    @ParameterizedTest
    void testConvertValue(String expected, String value)
    {
        Assertions.assertEquals(Document.parse(expected), converter.convertValue(value, null));
    }
}
