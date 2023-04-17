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

package org.vividus.datetime.expression;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.time.zone.ZoneRulesException;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.util.DateUtils;

@SuppressWarnings({ "checkstyle:MultipleStringLiterals", "checkstyle:MultipleStringLiteralsExtended",
        "checkstyle:LineLength", "PMD.AvoidDuplicateLiterals" })
@ExtendWith(MockitoExtension.class)
class FormatDateExpressionProcessorTests
{
    private static final String INPUT_DATE = "2017-01-10T13:04:20.677Z";

    private static final String INPUT_DATE_NOT_ZERO_TIMEZONE = "2017-01-10T08:04:20.677-05:00";

    private final FormatDateExpressionProcessor processor = new FormatDateExpressionProcessor(
            new DateUtils(ZoneId.of("Etc/GMT-0")));

    // @formatter:off
    static Stream<Arguments> executeWithoutTZDDataProvider()
    {
        return Stream.of(
            arguments("formatDate(" + INPUT_DATE + ", yyyy-MM-dd'T'HH:mm:ss.SSS)",                  "2017-01-10T13:04:20.677"),
            arguments("formatDate(" + INPUT_DATE + ", yyyy-MM-dd'T'HH:mm:ss)",                      "2017-01-10T13:04:20"),
            arguments("formatDate(" + INPUT_DATE + ", yyyy-MM-dd'T'HH:mm:ss.SSSXXX)",               INPUT_DATE),
            arguments("formatDate(" + INPUT_DATE + ", yyyy-MM-dd)",                                 "2017-01-10"),
            arguments("formatDate(" + INPUT_DATE + ", yyyy\\,MM\\,dd)",                             "2017,01,10"),
            arguments("formatDate(" + INPUT_DATE + ", yyyy-MM-dd'T'HH:mm:ssXXX)",                   "2017-01-10T13:04:20Z"),
            arguments("formatDate(" + INPUT_DATE_NOT_ZERO_TIMEZONE + ", yyyy-MM-dd'T'HH:mm:ssXXX)", "2017-01-10T08:04:20-05:00"),
            arguments("formatDate(2017-01-10T13:04:20Z, yyyy-MM-dd'T'HH:mmXXX)",                    "2017-01-10T13:04Z"),
            arguments("formatDate(1994-11-05T08:15:30, yyyy-MM-dd'T'HH:mm:ss.SSS)",                 "1994-11-05T08:15:30.000"),
            arguments("formatDate(1994-11-05T09:15:30, \"\"\"yyyy\\,MM,dd\"\"\")",                  "1994\\,11,05")
        );
    }

    static Stream<Arguments> executeWithTZDDataProvider()
    {
        return Stream.of(
            arguments("formatDate(" + INPUT_DATE + ",  yyyy-MM-dd'T'HH:mm:ss.SSSXXX, -05:00)",                   INPUT_DATE_NOT_ZERO_TIMEZONE),
            arguments("formatDate(" + INPUT_DATE_NOT_ZERO_TIMEZONE + ", yyyy-MM-dd'T'HH:mm:ss.SSSXXX, GMT)",     "2017-01-10T13:04:20.677Z"),
            arguments("formatDate(" + INPUT_DATE + ",  yyyy-MM-dd'T'HH:mm:ss.SSSXXX, America/New_York)",         "2017-01-10T08:04:20.677-05:00"),
            arguments("formatDate(" + INPUT_DATE + ",  yyyy\\,MM\\,dd'T'HH:mm:ss.SSSXXX, America/New_York)",     "2017,01,10T08:04:20.677-05:00"),
            arguments("formatDate(" + INPUT_DATE + ",  yyyy-MM-dd'T'HH:mm:ss.SSSXXX,  -05:00)",                  INPUT_DATE_NOT_ZERO_TIMEZONE),
            arguments("formatDate(" + INPUT_DATE + ",  yyyy-MM-dd'T'HH:mm:ss.SSSXXX,-05:00)",                    INPUT_DATE_NOT_ZERO_TIMEZONE),
            arguments("formatDate(1994-11-05T08:15:30, yyyy-MM-dd'T'HH:mm:ss.SSSXXX, -05:00)",                   "1994-11-05T03:15:30.000-05:00"),
            arguments("formatDate(1994-11-05T08:15:30, \"\"\"yyyy MM dd\\, HH:mm XXX\"\"\", -05:00)",            "1994 11 05\\, 03:15 -05:00")
        );
    }
    // @formatter:on

    @ParameterizedTest(name = "{index}: for expression \"{0}\", accepted is \"{1}\"")
    @MethodSource("executeWithoutTZDDataProvider")
    void testExecuteWithoutTZD(String expression, String expected)
    {
        assertEquals(expected, processor.execute(expression).get());
    }

    @ParameterizedTest(name = "{index}: for expression \"{0}\", accepted is \"{1}\"")
    @MethodSource("executeWithTZDDataProvider")
    void testExecuteWithTZD(String expression, String expected)
    {
        assertEquals(expected, processor.execute(expression).get());
    }

    @Test
    void testExecuteIncorrectExpression()
    {
        var exception = assertThrows(IllegalArgumentException.class,
                () -> processor.execute("formatDate(" + INPUT_DATE + ")"));
        assertEquals(
                "The expected number of arguments for 'formatDate' expression is from 2 to 3, but found 1 argument: '"
                        + INPUT_DATE + "'", exception.getMessage());
    }

    @Test
    void testExecuteIncorrectTimeZoneId()
    {
        var exception = assertThrows(ZoneRulesException.class,
            () -> processor.execute("formatDate(1994-11-05T08:15:30Z, yyyy-MM-dd'T'HH:mm:ssXXX, Incorrect)"));
        assertEquals("Unknown time-zone ID: Incorrect", exception.getMessage());
    }

    @Test
    void testExecuteOutputFormatWithUnsupportedSymbols()
    {
        var exception = assertThrows(IllegalArgumentException.class,
            () -> processor.execute("formatDate(1994-11-05T08:15:30Z, fyyyy-MM-dd, GMT)"));
        assertEquals("Unknown pattern letter: f", exception.getMessage());
    }

    @Test
    void testExecuteIncorrectFormatOfInputDate()
    {
        var exception = assertThrows(DateTimeParseException.class,
            () -> processor.execute("formatDate(1994:11:05T08:15:30Z, yyyy-MM-dd)"));
        assertThat(exception.getMessage(), containsString("Text '1994:11:05T08:15:30Z' could not be parsed"));
    }
}
