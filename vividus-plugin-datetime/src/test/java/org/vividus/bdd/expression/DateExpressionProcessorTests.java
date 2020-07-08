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

import static com.github.valfirst.slf4jtest.LoggingEvent.warn;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Locale;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.apache.commons.lang3.LocaleUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.util.DateUtils;

@ExtendWith({ MockitoExtension.class, TestLoggerFactoryExtension.class })
class DateExpressionProcessorTests
{
    private final TestLogger logger = TestLoggerFactory.getTestLogger(DateExpressionProcessor.class);

    @Mock
    private DateUtils dateUtils;

    @InjectMocks
    private DateExpressionProcessor dateExpressionProcessor;

    @BeforeEach
    void beforeEach()
    {
        dateExpressionProcessor.setLocale(Locale.ENGLISH);
    }

    private void mockGetCurrentDate()
    {
        final int year = 1900;
        when(dateUtils.getCurrentDateTime()).thenReturn(
                ZonedDateTime.of(year, 1, 1, 0, 0, 0, 0, ZoneId.of("Etc/GMT-0")));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "-P1D",
            "P1D(dd-MM-yyyy)",
            "P1Y2M3W4D",
            "-P20D",
            "P1DT20H",
            "P1MT10M",
            "PT10H",
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
    @ValueSource(strings = {
            "P10M12T",
            "PT1",
            "P10M12",
            "P10",
            "T10M",
            "generateDate(P10M12T)",
            "generateDate(PT1)",
            "generateDate(P10M12)",
            "generateDate(P10)",
            "generateDate(T10M)"
    })
    void shouldNotAccept(String expression)
    {
        assertFalse(dateExpressionProcessor.execute(expression).isPresent());
    }

    @ParameterizedTest
    @CsvSource(delimiter = ';', value = {
            "P5Y1M10D;                    1905-02-11;               P5Y1M10D; ''",
            "P1Y1M1W(dd-MM-yyyy);         08-02-1901;               P1Y1M1W;  , dd-MM-yyyy",
            "PT1H2M3S;                    1900-01-01T01:02:03;      PT1H2M3S; ''",
            "PT61M63S(yyyy'T'HH-mm-ss);   1900T01-02-03;            PT61M63S; , yyyy'T'HH-mm-ss",
            "-P1MT1M;                     1899-11-30T23:59:00;      -P1MT1M;  ''",
            "P(MMM);                      Jan;                      P;        , MMM",
            "P(MMMM);                     January;                  P;        , MMMM",
            "P(d);                        1;                        P;        , d",
            "P(EEE);                      Mon;                      P;        , EEE",
            "P(EEEE);                     Monday;                   P;        , EEEE",
            "P(yyyy-MM-dd'T'HH:mm:ssZ);   1900-01-01T00:00:00+0000; P;        , yyyy-MM-dd'T'HH:mm:ssZ"
    })
    void testCalculatePeriodDeprecated(String expression, String expected, String period, String format)
    {
        mockGetCurrentDate();
        String actual = dateExpressionProcessor.execute(expression).get();
        assertEquals(expected, actual);
        assertThat(logger.getLoggingEvents(),
                equalTo(List.of(warn(
                        "WARNING: The syntax of expression #{{}} is deprecated, use new syntax #{generateDate({}{})",
                        expression, period, format))));
    }

    @ParameterizedTest
    @CsvSource(delimiter = ';', value = {
            "generateDate(P5Y1M10D);                    1905-02-11",
            "generateDate(P1Y1M1W, dd-MM-yyyy);         08-02-1901",
            "generateDate(PT1H2M3S);                    1900-01-01T01:02:03",
            "generateDate(PT61M63S, yyyy'T'HH-mm-ss);   1900T01-02-03",
            "generateDate(-P1MT1M);                     1899-11-30T23:59:00",
            "generateDate(P, MMM);                      Jan",
            "generateDate(P, MMMM);                     January",
            "generateDate(P, d);                        1",
            "generateDate(P, EEE);                      Mon",
            "generateDate(P, EEEE);                     Monday",
            "generateDate(P, yyyy-MM-dd'T'HH:mm:ssZ);   1900-01-01T00:00:00+0000"
    })
    void testCalculatePeriod(String expression, String expected)
    {
        mockGetCurrentDate();
        String actual = dateExpressionProcessor.execute(expression).get();
        assertEquals(expected, actual);
        assertThat(logger.getLoggingEvents(), equalTo(List.of()));
    }

    @Test
    void testExecuteWithNonDefaultLocale()
    {
        mockGetCurrentDate();
        dateExpressionProcessor.setLocale(LocaleUtils.toLocale("be_BY"));
        String actual = dateExpressionProcessor.execute("P4D(d MMMM EEEE)").get();
        assertEquals("5 студзеня пятніца", actual);
    }
}
