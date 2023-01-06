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

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.matchesPattern;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.when;

import java.util.Locale;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.util.ILocationProvider;

@ExtendWith(MockitoExtension.class)
class StringExpressionProcessorsTests
{
    private static final String VALUE = "value";
    private static final String BIG_DATA = "big data";

    @Mock private ILocationProvider locationProvider;
    @InjectMocks private StringExpressionProcessors processors;

    @Test
    void testExecuteWithUnsupportedException()
    {
        assertEquals(Optional.empty(), processors.execute("removeWrappingDoubleQuotes(\"value\")"));
    }

    static Stream<Arguments> expressionSource()
    {
        // CHECKSTYLE:OFF
        return Stream.of(
                arguments("trim(value)",                                                   VALUE),
                arguments("TRIM( value )",                                                 VALUE),
                arguments("Trim(  value)",                                                 VALUE),
                arguments("tRIM()",                                                        ""),
                arguments("toLowerCase(aBc)",                                              "abc"),
                arguments("toUpperCase(aBc)",                                              "ABC"),
                arguments("capitalizeFirstWord(aBc)",                                      "ABc"),
                arguments("capitalizeWords(aBc dEf)",                                      "ABc DEf"),
                arguments("capitalizeWordsFully(aBc dEf)",                                 "Abc Def"),
                arguments("uncapitalizeFirstWord(ABc)",                                    "aBc"),
                arguments("uncapitalizeWords(ABc DEf)",                                    "aBc dEf"),
                arguments("generateLocalized(regexify '[Ы]{5}', ru)",                      "ЫЫЫЫЫ"),
                arguments("generateLocalized(number.number_between '1000','1000', es-MX)", "1000"),
                arguments("loadResource(/org/vividus/expressions/resource.txt)",           BIG_DATA),
                arguments("loadResource(org/vividus/expressions/resource.txt)",            BIG_DATA),
                arguments("anyOf(123)",                                                    "123"),
                arguments("anyOf()",                                                       EMPTY),
                arguments("anyOf(,)",                                                      EMPTY),
                arguments("anyOf(, )",                                                     EMPTY),
                arguments("anyOf( )",                                                      ""),
                arguments("anyOf(\"\"\"a,b\\,c\"\"\")",                                    "a,b\\,c"),
                arguments("anyOf(\"\"\" \"\"\")",                                          " "),
                arguments("anyOf(\\,)",                                                    ","),
                arguments("escapeHTML(M&Ms)",                                              "M&amp;Ms"),
                arguments("escapeJSON(\"abc\"\n\"xyz\")",                                  "\\\"abc\\\"\\n\\\"xyz\\\""),
                arguments("quoteRegExp(Customer(Username))",                               "\\QCustomer(Username)\\E"),
                arguments("substringBefore(, a)",                                          ""),
                arguments("substringBefore(abc, a)",                                       ""),
                arguments("substringBefore(abcba, b)",                                     "a"),
                arguments("substringBefore(abcba,b)",                                      "a"),
                arguments("substringBefore(abc, c)",                                       "ab"),
                arguments("substringBefore(abc, d)",                                       "abc"),
                arguments("substringBefore(abc, )",                                        ""),
                arguments("substringBefore(a\\,b\\,c\\,b\\,a, \"\"\",c\"\"\")",            "a,b"),
                arguments("substringBefore(\"\"\"a,b,c,b,a\"\"\", c)",                     "a,b,"),
                arguments("substringAfter(, a)",                                           ""),
                arguments("substringAfter(abc, a)",                                        "bc"),
                arguments("substringAfter(abcba, b)",                                      "cba"),
                arguments("substringAfter(abcba,b)",                                       "cba"),
                arguments("substringAfter(abc, c)",                                        ""),
                arguments("substringAfter(abc, d)",                                        ""),
                arguments("substringAfter(abc, )",                                         "abc"),
                arguments("substringAfter(a\\,b\\,c\\,b\\,a, c)",                          ",b,a"),
                arguments("substringAfter(a\\,b\\,c\\,b\\,a, c\\,)",                       "b,a"),
                arguments("substringAfter(\"\"\"a,b,c,b\\,a\"\"\", c)",                    ",b\\,a"),
                arguments("substringAfter(\"\"\"a,b,c,b\\,a\"\"\", \"\"\"c,\"\"\")",       "b\\,a")
        );
        // CHECKSTYLE:ON
    }

    @ParameterizedTest(name = "{index}: for expression \"{0}\", result is \"{1}\"")
    @MethodSource("expressionSource")
    void testExecute(String expression, String expected)
    {
        assertEquals(expected, processors.execute(expression).get());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "Address.streetAddress",
            "Company.name"
    })
    void shouldExecuteLocalizedExpressions(String expression)
    {
        String result = (String) processors.execute("generateLocalized(" + expression + ", ru-RU)").get();
        assertThat(result, matchesPattern("[А-яёЁ0-9 ,.-]+"));
    }

    @Test
    void shouldGenerateDataBasedOnNonLocalizedExpression()
    {
        when(locationProvider.getLocale()).thenReturn(Locale.US);
        assertEquals("AA", processors.execute("generate(regexify '[A]{2}')").get());
    }

    @Test
    void shouldPickRandomValue()
    {
        assertThat((String) processors.execute("anyOf(one,two, three\\, or,, \"\"\"four\\,five\"\"\")").get(),
                anyOf(equalTo("one"), equalTo("two"), equalTo("three, or"), equalTo("four\\,five"), emptyString()));
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
        // CHECKSTYLE:OFF
        "substringBefore(1, 2, 3) | The expected number of arguments for 'substringBefore' expression is 2, but found 3 arguments: '1, 2, 3'",
        "substringAfter(1, 2, 3)  | The expected number of arguments for 'substringAfter' expression is 2, but found 3 arguments: '1, 2, 3'",
        "substringBefore(1)       | The expected number of arguments for 'substringBefore' expression is 2, but found 1 argument: '1'",
        "substringAfter(1)        | The expected number of arguments for 'substringAfter' expression is 2, but found 1 argument: '1'",
        // CHECKSTYLE:ON
    })
    void shouldAssertParametersNumberWhenSubstring(String expression, String errorMessage)
    {
        var exception = assertThrows(IllegalArgumentException.class, () -> processors.execute(expression));
        assertEquals(errorMessage, exception.getMessage());
    }
}
