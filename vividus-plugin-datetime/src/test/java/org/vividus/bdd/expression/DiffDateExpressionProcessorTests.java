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

class DiffDateExpressionProcessorTests
{
    private final DiffDateExpressionProcessor processor = new DiffDateExpressionProcessor(
            new DateUtils(ZoneId.of("GMT-0")));

    static Stream<Arguments> diffDateProvider()
    {
        // CHECKSTYLE:OFF
        // @formatter:off
        return Stream.of(
            Arguments.of("diffDate(2019-01-01T12:00:00.333Z,          yyyy-MM-dd'T'HH:mm:ss.SSSVV, 2019-01-01T12:00:00.333Z,     yyyy-MM-dd'T'HH:mm:ss.SSSVV)",          "PT0S"               ),
            Arguments.of("diffDate(2019-01-01T12:00:00.333Z,          yyyy-MM-dd'T'HH:mm:ss.SSSVV, 2019-01-01T12:00:00.353Z,     yyyy-MM-dd'T'HH:mm:ss.SSSVV)",          "PT0.02S"            ),
            Arguments.of("diffDate(2019-01-01T12:00:00.333Z,          yyyy-MM-dd'T'HH:mm:ss.SSSVV, 2019-01-01T12:03:03.333Z,     yyyy-MM-dd'T'HH:mm:ss.SSSVV)",          "PT3M3S"             ),
            Arguments.of("diffDate(2019-01-01T12:00:00.333Z,          yyyy-MM-dd'T'HH:mm:ss.SSSVV, 2019-01-01T11:05:00.333Z,     yyyy-MM-dd'T'HH:mm:ss.SSSVV)",          "-PT55M"             ),
            Arguments.of("diffDate(2019-01-01T12:00:00.333Z,          yyyy-MM-dd'T'HH:mm:ss.SSSVV, 2019-01-01T11:05:03.353Z,     yyyy-MM-dd'T'HH:mm:ss.SSSVV)",          "-PT54M56.98S"       ),
            Arguments.of("diffDate(2019-01-01T12:00:00.333Z,          yyyy-MM-dd'T'HH:mm:ss.SSSVV, 2019-01-02T12:00:00.333Z,     yyyy-MM-dd'T'HH:mm:ss.SSSVV)",          "PT24H"              ),
            Arguments.of("diffDate(2019-01-01T12:00:00.333Z,          yyyy-MM-dd'T'HH:mm:ss.SSSVV, 2018-02-02T11:05:00.352Z,     yyyy-MM-dd'T'HH:mm:ss.SSSVV)",          "-PT7992H54M59.981S" ),
            Arguments.of("diffDate(10042019 13:20:43 GMT,             ddMMyyyy HH:mm:ss zzz,       10042019 06:22:43 US/Pacific, ddMMyyyy HH:mm:ss zzz)",                "PT2M"               ),
            Arguments.of("diffDate(\"10 Apr 2019 13:20:43\",          \"dd MMM yyyy HH:mm:ss\",    10 Apr 2019 09:20:43,         dd MMM yyyy HH:mm:ss)",                 "-PT4H"              ),
            Arguments.of("diffDate(Wed\\, 10 Apr\\, 2019\\, 13,EEE\\, dd MMM\\, yyyy\\, HH,        2019-04-10 03,                yyyy-MM-dd HH)",                        "-PT10H"             ),
            Arguments.of("diffDate(2019-01-01T12:00:00.333Z,          yyyy-MM-dd'T'HH:mm:ss.SSSVV, 2020-01-01T12:00:00.333Z,     yyyy-MM-dd'T'HH:mm:ss.SSSVV, days)",    "365"                ),
            Arguments.of("diffDate(2019-01-01T12:00:00.333Z,          yyyy-MM-dd'T'HH:mm:ss.SSSVV, 2020-01-01T12:00:00.333Z,     yyyy-MM-dd'T'HH:mm:ss.SSSVV, Hours)",   "8760"               ),
            Arguments.of("diffDate(2019-01-01T12:00:00.333Z,          yyyy-MM-dd'T'HH:mm:ss.SSSVV, 2020-01-01T12:00:00.333Z,     yyyy-MM-dd'T'HH:mm:ss.SSSVV, mInuteS)", "525600"             ),
            Arguments.of("diffDate(2019-01-01T12:00:00.333Z,          yyyy-MM-dd'T'HH:mm:ss.SSSVV, 2019-01-01T12:00:00.555Z,     yyyy-MM-dd'T'HH:mm:ss.SSSVV, milliS)",  "222"                ),
            Arguments.of("diffDate(2019-01-01T12:00:00.333Z,          yyyy-MM-dd'T'HH:mm:ss.SSSVV, 2019-01-01T12:00:00.555Z,     yyyy-MM-dd'T'HH:mm:ss.SSSVV, nanos)",   "222000000"          )
        );
        // CHECKSTYLE:ON
        // @formatter:on
    }

    @ParameterizedTest(name = "{index}: for expression \"{0}\", accepted is \"{1}\"")
    @MethodSource("diffDateProvider")
    void testExecuteDiffDate(String expression, String expected)
    {
        assertEquals(expected, processor.execute(expression).get());
    }

    @Test
    void testExecuteDiffDateIncorrectExpression()
    {
        assertEquals(Optional.empty(), processor.execute("diffDate()"));
    }

    @Test
    void testExecuteUnsupportedSymbols()
    {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> processor.execute(
                "diffDate(2019-01-01T12:00:00.333Z, Byyyy-MM-dd'T'HH:mm:ss.SSSVV, 2019-01-01T12:00:00.333Z, "
                        + "yyyy-MM-dd'T'HH:mm:ss.SSSVV)"));
        assertEquals("Unknown pattern letter: B", exception.getMessage());
    }

    @Test
    void testExecuteIncorrectFormat()
    {
        String inputDate = "2019-01-01T12:00:00.333Z";
        DateTimeParseException exception = assertThrows(DateTimeParseException.class,
            () -> processor.execute("diffDate(" + inputDate + ", yyyy-MM-dd, 2019-01-01, yyyy-MM-dd)"));
        assertThat(exception.getMessage(), containsString(String.format("Text '%s' could not be parsed", inputDate)));
    }
}
