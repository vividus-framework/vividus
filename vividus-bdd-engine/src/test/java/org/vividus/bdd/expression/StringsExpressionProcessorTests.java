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

package org.vividus.bdd.expression;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.when;

import java.util.Locale;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.util.ILocationProvider;

@ExtendWith(MockitoExtension.class)
class StringsExpressionProcessorTests
{
    private static final String VALUE = "value";
    private static final String BIG_DATA = "big data";
    private static final String BASE_64 = "YmlnIGRhdGE=";

    @Mock
    private ILocationProvider locationProvider;

    @InjectMocks
    private StringsExpressionProcessor processor;

    @Test
    void testExecuteWithUnsupportedException()
    {
        assertEquals(Optional.empty(), processor.execute("removeWrappingDoubleQuotes(\"value\")"));
    }

    static Stream<Arguments> expressionSource()
    {
        return Stream.of(
                arguments("trim(value)",                                                   VALUE),
                arguments("TRIM( value )",                                                 VALUE),
                arguments("Trim(  value)",                                                 VALUE),
                arguments("tRIM()",                                                        ""),
                arguments("toLowerCase(aBc)",                                              "abc"),
                arguments("toUpperCase(aBc)",                                              "ABC"),
                arguments("capitalize(aBc)",                                               "ABc"),
                arguments("uncapitalize(ABc)",                                             "aBc"),
                arguments("generateLocalized(regexify '[Ы]{5}', ru)",                      "ЫЫЫЫЫ"),
                arguments("generateLocalized(number.number_between '1000','1000', es-MX)", "1000"),
                arguments("encodeUrl(/wiki/w3schools)",                                    "%2Fwiki%2Fw3schools"),
                arguments("encodeUrl(/wls/recipes#!/)",                                    "%2Fwls%2Frecipes%23%21%2F"),
                arguments("loadResource(/org/vividus/bdd/expressions/resource.txt)",       BIG_DATA),
                arguments("loadResource(org/vividus/bdd/expressions/resource.txt)",        BIG_DATA),
                arguments("resourceToBase64(/org/vividus/bdd/expressions/resource.txt)",   BASE_64),
                arguments("resourceToBase64(org/vividus/bdd/expressions/resource.txt)",    BASE_64),
                arguments("decodeFromBase64(QmFydWNo)",                                    "Baruch"),
                arguments("encodeToBase64(Baruch)",                                        "QmFydWNo")
        );
    }

    @ParameterizedTest(name = "{index}: for expression \"{0}\", result is \"{1}\"")
    @MethodSource("expressionSource")
    void testExecute(String expression, String expected)
    {
        assertEquals(expected, processor.execute(expression).get());
    }

    @Test
    void shouldGenerateDataBasedOnNonLocalizedExpression()
    {
        when(locationProvider.getLocale()).thenReturn(Locale.US);
        assertEquals("AA", processor.execute("generate(regexify '[A]{2}')").get());
    }
}
