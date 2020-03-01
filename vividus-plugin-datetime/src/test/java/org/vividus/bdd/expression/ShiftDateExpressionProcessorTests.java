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

class ShiftDateExpressionProcessorTests
{
    private static final String SHIFT_DATE = "shiftDate(";
    private static final char COMMA = ',';
    private static final String INPUT_DATE = "2019-01-01T12:00:00.333Z";
    private static final String FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSVV";

    private final ShiftDateExpressionProcessor processor = new ShiftDateExpressionProcessor(
            new DateUtils(ZoneId.of("GMT-0")));

    static Stream<Arguments> shiftDateProvider()
    {
        // CHECKSTYLE:OFF
        // @formatter:off
        return Stream.of(
            Arguments.of(SHIFT_DATE + INPUT_DATE + COMMA + FORMAT + COMMA + " -P1Y)",                                        "2018-01-01T12:00:00.333Z"),
            Arguments.of(SHIFT_DATE + INPUT_DATE + COMMA + FORMAT + COMMA + " P12D)",                                        "2019-01-13T12:00:00.333Z"),
            Arguments.of(SHIFT_DATE + INPUT_DATE + COMMA + FORMAT + COMMA + " -P3MT3S)",                                     "2018-10-01T11:59:57.333Z"),
            Arguments.of(SHIFT_DATE + INPUT_DATE + COMMA + FORMAT + COMMA + " P1MT2H)",                                      "2019-02-01T14:00:00.333Z"),
            Arguments.of(SHIFT_DATE + INPUT_DATE + COMMA + FORMAT + COMMA + " -PT10M)",                                      "2019-01-01T11:50:00.333Z"),
            Arguments.of(SHIFT_DATE + INPUT_DATE + COMMA + FORMAT + COMMA + " PT3H)",                                        "2019-01-01T15:00:00.333Z"),
            Arguments.of(SHIFT_DATE + "2019-01-01T12:00:00-05:00" + COMMA + "yyyy-MM-dd'T'HH:mm:ssXXX" + COMMA + " PT5S)",   "2019-01-01T12:00:05-05:00"),
            Arguments.of("shiftDate( Tue\\, 01 Jan 2019 12:00:00 GMT, EEE\\, dd MMM yyyy HH:mm:ss zzz,    P1MT2H5S)",        "Fri, 01 Feb 2019 14:00:05 GMT"),
            Arguments.of("shiftDate(\"Tue\\, 01 Jan 2019 12:00:00 GMT\", \"EEE\\, dd MMM yyyy HH:mm:ss zzz\",    P1MT2H5S)", "\"Fri, 01 Feb 2019 14:00:05 GMT\"")
        );
        // CHECKSTYLE:ON
        // @formatter:on
    }

    @ParameterizedTest(name = "{index}: for expression \"{0}\", accepted is \"{1}\"")
    @MethodSource("shiftDateProvider")
    void testExecuteShiftDate(String expression, String expected)
    {
        assertEquals(expected, processor.execute(expression).get());
    }

    @Test
    void testExecuteShiftDateIncorrectExpression()
    {
        assertEquals(Optional.empty(), processor.execute("shiftDate()"));
    }

    @Test
    void testExecuteUnsupportedSymbols()
    {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> processor
                .execute(SHIFT_DATE + INPUT_DATE + COMMA + "Byyyy-MM-dd'T'HH:mm:ss.SSSVV" + COMMA + "-P1Y)"));
        assertEquals("Unknown pattern letter: B", exception.getMessage());
    }

    @Test
    void testExecuteIncorrectFormat()
    {
        DateTimeParseException exception = assertThrows(DateTimeParseException.class,
            () -> processor.execute(SHIFT_DATE + INPUT_DATE + COMMA + "yyyy-MM-dd" + COMMA + "-P2Y)"));
        assertThat(exception.getMessage(), containsString(String.format("Text '%s' could not be parsed", INPUT_DATE)));
    }
}
