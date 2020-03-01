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

package org.vividus.bdd.expression;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.vividus.util.DateUtils;

class FormatDateToExpressionProcessorTests
{
    private static final String FORMAT_DATE_TO = "formatDateTo(";
    private static final char COMMA = ',';
    private static final String INPUT_DATE = "2017-01-10T13:04:20.677Z";
    private static final String INPUT_DATE_FORMAT = " yyyy-MM-dd'T'HH:mm:ss.SSSVV";

    private final FormatDateToExpressionProcessor processor = new FormatDateToExpressionProcessor(
            new DateUtils(ZoneId.of("GMT-0")));

    static Stream<Arguments> formateToDataProvider()
    {
        // CHECKSTYLE:OFF
        // @formatter:off
        return Stream.of(
            Arguments.of(FORMAT_DATE_TO + INPUT_DATE + COMMA + INPUT_DATE_FORMAT + COMMA + " EEE\\, dd MMM yyyy HH:mm:ss ZZZZ)", "Tue, 10 Jan 2017 13:04:20 GMT"),
            Arguments.of(FORMAT_DATE_TO + INPUT_DATE + COMMA + INPUT_DATE_FORMAT + COMMA + " EEE\\, dd MMM yyyy HH:mm:ss)", "Tue, 10 Jan 2017 13:04:20"),
            Arguments.of("formatDateTo(\"Tue\\, 10 Jan 2017 13:04:20 GMT\", \"EEE\\, dd MMM yyyy HH:mm:ss zzz\", " + "yyyy-MM-dd'T'HH:mm:ss" + ")", "2017-01-10T13:04:20")
        );
        // CHECKSTYLE:ON
        // @formatter:on
    }

    @ParameterizedTest(name = "{index}: for expression \"{0}\", accepted is \"{1}\"")
    @MethodSource("formateToDataProvider")
    void testExecuteFormateTo(String expression, String expected)
    {
        assertEquals(expected, processor.execute(expression).get());
    }

    @Test
    void testExecuteFormatToIncorrectExpression()
    {
        assertEquals(Optional.empty(), processor.execute("formatToDate()"));
    }

    @Test
    void testExecuteUnsupportedSymbols()
    {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> processor.execute("formatDateTo(2017:18:05T09:09:09Z, Byyyy-MM-dd, MM dd yyyy)"));
        assertEquals("Unknown pattern letter: B", exception.getMessage());
    }

    @Test
    void testExecuteIncorrectFormat()
    {
        DateTimeParseException exception = assertThrows(DateTimeParseException.class,
            () -> processor.execute("formatDateTo(2017:18:05T09:09:09Z, yyyy-MM-dd, MM dd yyyy)"));
        assertThat(exception.getMessage(), containsString("Text '2017:18:05T09:09:09Z' could not be parsed"));
    }
}
