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

package org.vividus.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.lang.reflect.Type;
import java.util.EnumSet;
import java.util.stream.Stream;

import com.browserup.harreader.model.HttpMethod;

import org.apache.commons.lang3.reflect.TypeLiteral;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class HttpMethodSetConverterTests
{
    static Stream<Arguments> inputData()
    {
        return Stream.of(
                arguments("GET",                  EnumSet.of(HttpMethod.GET)),
                arguments("GET,POST",             EnumSet.of(HttpMethod.GET, HttpMethod.POST)),
                arguments("GET,  POST ",          EnumSet.of(HttpMethod.GET, HttpMethod.POST)),
                arguments("GET or POST",          EnumSet.of(HttpMethod.GET, HttpMethod.POST)),
                arguments("GET  or POST or  PUT", EnumSet.of(HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT))
        );
    }

    @ParameterizedTest
    @MethodSource("inputData")
    void shouldConvertStringToEnumSetOfHttpMethods(String input, EnumSet<HttpMethod> expected)
    {
        Type type = new TypeLiteral<EnumSet<HttpMethod>>() { }.value;
        assertEquals(expected, new HttpMethodSetConverter().convertValue(input, type));
    }
}
