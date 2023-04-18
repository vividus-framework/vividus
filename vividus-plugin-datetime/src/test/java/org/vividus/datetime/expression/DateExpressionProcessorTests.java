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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Locale;

import org.apache.commons.lang3.LocaleUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.util.DateUtils;

@ExtendWith(MockitoExtension.class)
class DateExpressionProcessorTests
{
    @Mock private DateUtils dateUtils;
    private DateExpressionProcessor dateExpressionProcessor;

    @BeforeEach
    void beforeEach()
    {
        dateExpressionProcessor = new DateExpressionProcessor(Locale.ENGLISH, dateUtils);
    }

    private void mockGetCurrentDate()
    {
        final var year = 1900;
        when(dateUtils.getCurrentDateTime()).thenReturn(
                ZonedDateTime.of(year, 1, 1, 0, 0, 0, 0, ZoneId.of("Etc/GMT-0")));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "generateDate(-P1D)",
            "generateDate(P1D, dd-MM-yyyy)",
            "generateDate(P1Y2M3W4D)",
            "generateDate(-P20D)",
            "generateDate(P1DT20H)",
            "generateDate(P1MT10M)",
            "generateDate(PT10H)"
    })
    void shouldAccept(String expression)
    {
        mockGetCurrentDate();
        assertTrue(dateExpressionProcessor.execute(expression).isPresent());
    }

    @ParameterizedTest
    @CsvSource({
            // @formatter:off
            "generateDate(P10M12T), P10M12T",
            "generateDate(PT1),     PT1",
            "generateDate(P10M12),  P10M12",
            "generateDate(P10),     P10",
            "generateDate(P10S),    P10S",
            "generateDate(T10M),    T10M"
            // @formatter:on
    })
    void shouldNotAccept(String expression, String invalidArg)
    {
        var exception = assertThrows(IllegalArgumentException.class, () -> dateExpressionProcessor.execute(expression));
        assertEquals(
                "The first argument of 'generateDate' expression must be a duration in ISO-8601 format, but found: '"
                        + invalidArg + "'", exception.getMessage());
    }

    @ParameterizedTest
    @CsvSource(delimiter = ';', value = {
            "generateDate(P5Y1M10D);                                 1905-02-11",
            "generateDate(P1Y1M1W, dd-MM-yyyy);                      08-02-1901",
            "generateDate(P1Y1M1W, dd,MM,yyyy);                      08,02,1901",
            "generateDate(P1Y1M1W, dd\\,MM\\,yyyy);                  08,02,1901",
            "generateDate(PT1H2M3S);                                 1900-01-01T01:02:03",
            "generateDate(PT61M63S, yyyy'T'HH-mm-ss);                1900T01-02-03",
            "generateDate(-P1MT1M);                                  1899-11-30T23:59:00",
            "generateDate(P, MMM);                                   Jan",
            "generateDate(P, MMMM);                                  January",
            "generateDate(P, d);                                     1",
            "generateDate(P, EEE);                                   Mon",
            "generateDate(P, EEEE);                                  Monday",
            "generateDate(P, yyyy-MM-dd'T'HH:mm:ssZ);                1900-01-01T00:00:00+0000",
            "generateDate(P, \"\"\"yyyy\\,MM dd HH mm ss Z\"\"\");   1900\\,01 01 00 00 00 +0000"
    })
    void testCalculatePeriod(String expression, String expected)
    {
        mockGetCurrentDate();
        var actual = dateExpressionProcessor.execute(expression).get();
        assertEquals(expected, actual);
    }

    @Test
    void testExecuteWithNonDefaultLocale()
    {
        mockGetCurrentDate();
        dateExpressionProcessor = new DateExpressionProcessor(LocaleUtils.toLocale("be_BY"), dateUtils);
        var actual = dateExpressionProcessor.execute("generateDate(P4D, d MMMM EEEE)").get();
        assertEquals("5 студзеня пятніца", actual);
    }
}
