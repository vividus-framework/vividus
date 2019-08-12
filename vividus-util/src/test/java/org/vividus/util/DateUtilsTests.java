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

package org.vividus.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class DateUtilsTests
{
    private static final String DATE_TIME_WITHOUT_TIMEZONE = "2011-12-03T10:15:30";
    private static final ZoneId ZERO_TIMEZONE = ZoneId.of("Z");

    static Stream<Arguments> parseDateTimeDataProvider()
    {
        // @formatter:off
        // CHECKSTYLE:OFF
        return Stream.of(
            Arguments.of(DATE_TIME_WITHOUT_TIMEZONE,                DATE_TIME_WITHOUT_TIMEZONE, ZoneId.systemDefault(),    DateTimeFormatter.ISO_DATE_TIME),
            Arguments.of("2011-12-03T10:15:30+01:00",               DATE_TIME_WITHOUT_TIMEZONE, ZoneId.of("+01:00")      , DateTimeFormatter.ISO_DATE_TIME),
            Arguments.of("2011-12-03T10:15:30+01:00[Europe/Paris]", DATE_TIME_WITHOUT_TIMEZONE, ZoneId.of("Europe/Paris"), DateTimeFormatter.ISO_DATE_TIME),
            Arguments.of("2011-12-03T10:15:30Z",                    DATE_TIME_WITHOUT_TIMEZONE, ZERO_TIMEZONE,             DateTimeFormatter.ISO_DATE_TIME),
            Arguments.of("2011-12-03T10:15:30.123Z",                "2011-12-03T10:15:30.123",  ZERO_TIMEZONE,             DateTimeFormatter.ISO_DATE_TIME),
            Arguments.of("2011-12-03",                              "2011-12-03T00:00",         ZERO_TIMEZONE,             DateTimeFormatter.ISO_LOCAL_DATE)
        );
        // CHECKSTYLE:ON
        // @formatter:on
    }

    @ParameterizedTest
    @MethodSource("parseDateTimeDataProvider")
    void shouldParseDateTime(String dateTime, String expectedLocalDateTime, ZoneId expectedZoneId,
            DateTimeFormatter formatter)
    {
        ZonedDateTime actual = new DateUtils(expectedZoneId).parseDateTime(dateTime, formatter);
        assertEquals(expectedZoneId, actual.getZone(), "Time zone is correct");
        assertEquals(expectedLocalDateTime, actual.toLocalDateTime().toString(), "Local date-time is correct");
    }

    @Test
    void shouldReturnCurrentDateTime()
    {
        ZonedDateTime actual = new DateUtils(ZERO_TIMEZONE).getCurrentDateTime();
        ZonedDateTime expected = ZonedDateTime.now(ZERO_TIMEZONE);
        assertThat(ChronoUnit.SECONDS.between(actual, expected), lessThan(10L));
    }
}
