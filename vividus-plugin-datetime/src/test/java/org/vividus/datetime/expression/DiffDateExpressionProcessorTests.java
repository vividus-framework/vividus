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
import java.util.stream.Stream;

import org.jbehave.core.steps.ParameterConverters.FluentEnumConverter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.vividus.util.DateUtils;

class DiffDateExpressionProcessorTests
{
    private final DiffDateExpressionProcessor processor = new DiffDateExpressionProcessor(
            new DateUtils(ZoneId.of("GMT-0")), new FluentEnumConverter());

    static Stream<Arguments> diffDateProvider()
    {
        // CHECKSTYLE:OFF
        // @formatter:off
        return Stream.of(
            arguments("diffDate(2019-01-01T12:00:00.333Z,          yyyy-MM-dd'T'HH:mm:ss.SSSVV, 2019-01-01T12:00:00.333Z,     yyyy-MM-dd'T'HH:mm:ss.SSSVV)",          "PT0S"               ),
            arguments("diffDate(2019-01-01T12:00:00.333Z,          yyyy-MM-dd'T'HH:mm:ss.SSSVV, 2019-01-01T12:00:00.353Z,     yyyy-MM-dd'T'HH:mm:ss.SSSVV)",          "PT0.02S"            ),
            arguments("diffDate(2019-01-01T12:00:00.333Z,          yyyy-MM-dd'T'HH:mm:ss.SSSVV, 2019-01-01T12:03:03.333Z,     yyyy-MM-dd'T'HH:mm:ss.SSSVV)",          "PT3M3S"             ),
            arguments("diffDate(2019-01-01T12:00:00.333Z,          yyyy-MM-dd'T'HH:mm:ss.SSSVV, 2019-01-01T11:05:00.333Z,     yyyy-MM-dd'T'HH:mm:ss.SSSVV)",          "-PT55M"             ),
            arguments("diffDate(2019-01-01T12:00:00.333Z,          yyyy-MM-dd'T'HH:mm:ss.SSSVV, 2019-01-01T11:05:03.353Z,     yyyy-MM-dd'T'HH:mm:ss.SSSVV)",          "-PT54M56.98S"       ),
            arguments("diffDate(2019-01-01T12:00:00.333Z,          yyyy-MM-dd'T'HH:mm:ss.SSSVV, 2019-01-02T12:00:00.333Z,     yyyy-MM-dd'T'HH:mm:ss.SSSVV)",          "PT24H"              ),
            arguments("diffDate(2019-01-01T12:00:00.333Z,          yyyy-MM-dd'T'HH:mm:ss.SSSVV, 2018-02-02T11:05:00.352Z,     yyyy-MM-dd'T'HH:mm:ss.SSSVV)",          "-PT7992H54M59.981S" ),
            arguments("diffDate(10042019 13:20:43 GMT,             ddMMyyyy HH:mm:ss zzz,       10042019 06:22:43 US/Pacific, ddMMyyyy HH:mm:ss zzz)",                "PT2M"               ),
            arguments("diffDate(\"10 Apr 2019 13:20:43\",          \"dd MMM yyyy HH:mm:ss\",    10 Apr 2019 09:20:43,         dd MMM yyyy HH:mm:ss)",                 "-PT4H"              ),
            arguments("diffDate(Wed\\, 10 Apr\\, 2019\\, 13,EEE\\, dd MMM\\, yyyy\\, HH,        2019-04-10 03,                yyyy-MM-dd HH)",                        "-PT10H"             ),
            arguments("diffDate(Wed\\, 10 Apr\\, 2019\\, 13,EEE\\, dd MMM\\, yyyy\\, HH,        2019\\,04\\,10 03,            yyyy\\,MM\\,dd HH)",                    "-PT10H"             ),
            arguments("diffDate(\"\"\"11, Apr\\, 2019\"\"\",       \"\"\"dd, MMM\\, yyyy\"\"\", \"\"\"2019\\,04,10\"\"\",     \"\"\"yyyy\\,MM,dd\"\"\")",             "-PT24H"             ),
            arguments("diffDate(2019-01-01T12:00:00.333Z,          yyyy-MM-dd'T'HH:mm:ss.SSSVV, 2020-01-01T12:00:00.333Z,     yyyy-MM-dd'T'HH:mm:ss.SSSVV, days)",    "365"                ),
            arguments("diffDate(2019-01-01T12:00:00.333Z,          yyyy-MM-dd'T'HH:mm:ss.SSSVV, 2020-01-01T12:00:00.333Z,     yyyy-MM-dd'T'HH:mm:ss.SSSVV, Hours)",   "8760"               ),
            arguments("diffDate(2019-01-01T12:00:00.333Z,          yyyy-MM-dd'T'HH:mm:ss.SSSVV, 2020-01-01T12:00:00.333Z,     yyyy-MM-dd'T'HH:mm:ss.SSSVV, mInuteS)", "525600"             ),
            arguments("diffDate(2019-01-01T12:00:00.333Z,          yyyy-MM-dd'T'HH:mm:ss.SSSVV, 2019-01-01T12:00:00.555Z,     yyyy-MM-dd'T'HH:mm:ss.SSSVV, milliS)",  "222"                ),
            arguments("diffDate(2019-01-01T12:00:00.333Z,          yyyy-MM-dd'T'HH:mm:ss.SSSVV, 2019-01-01T12:00:00.555Z,     yyyy-MM-dd'T'HH:mm:ss.SSSVV, nanos)",   "222000000"          )
        );
        // @formatter:on
        // CHECKSTYLE:ON
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
        var exception = assertThrows(IllegalArgumentException.class, () -> processor.execute("diffDate(x)"));
        assertEquals(
                "The expected number of arguments for 'diffDate' expression is from 4 to 5, but found 1 argument: 'x'",
                exception.getMessage());
    }

    @Test
    void testExecuteUnsupportedSymbols()
    {
        var exception = assertThrows(IllegalArgumentException.class, () -> processor.execute(
                "diffDate(2019-01-01T12:00:00.333Z, fyyyy-MM-dd'T'HH:mm:ss.SSSVV, 2019-01-01T12:00:00.333Z, "
                        + "yyyy-MM-dd'T'HH:mm:ss.SSSVV)"));
        assertEquals("Unknown pattern letter: f", exception.getMessage());
    }

    @Test
    void testExecuteIncorrectFormat()
    {
        var inputDate = "2019-01-01T12:00:00.333Z";
        var exception = assertThrows(DateTimeParseException.class,
            () -> processor.execute("diffDate(" + inputDate + ", yyyy-MM-dd, 2019-01-01, yyyy-MM-dd)"));
        assertThat(exception.getMessage(), containsString(String.format("Text '%s' could not be parsed", inputDate)));
    }
}
