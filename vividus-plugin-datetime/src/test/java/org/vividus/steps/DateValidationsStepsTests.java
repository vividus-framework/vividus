/*
 * Copyright 2019-2021 the original author or authors.
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

package org.vividus.steps;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import org.hamcrest.TypeSafeMatcher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.softassert.ISoftAssert;
import org.vividus.util.DateUtils;

@ExtendWith(MockitoExtension.class)
class DateValidationsStepsTests
{
    private static final long SECONDS = 60L;
    private static final String INVALID_DATE = "invalidDate";
    private static final String DATE_PATTERN = "uuuu-MM-dd'T'HH:mm:ss.nnnX";
    private static final ZoneId TIME_ZONE_ID = ZoneId.of("Z");
    private static final DateUtils DATE_UTILS = new DateUtils(TIME_ZONE_ID);

    @Mock private ISoftAssert softAssert;
    @InjectMocks private final DateValidationSteps dateValidationsSteps = new DateValidationSteps(DATE_UTILS);

    @Test
    void testIsDateLess()
    {
        String date = Instant.now().toString();
        dateValidationsSteps.doesDateConformRule(date, ComparisonRule.LESS_THAN, SECONDS);
        verifyDateAssertion(date);
    }

    @Test
    void testIsDateInFormatLess()
    {
        String date = Instant.now().toString();
        dateValidationsSteps.doesDateConformRule(date, DATE_PATTERN, ComparisonRule.LESS_THAN, SECONDS);
        verifyDateAssertion(date);
    }

    private void verifyDateAssertion(String date)
    {
        verify(softAssert).assertThat(eq(String.format("The difference between %s and the current date", date)),
                any(Long.class), verifyMatcher(0L));
    }

    @Test
    void testIsDateLessFailedToParseDate()
    {
        dateValidationsSteps.doesDateConformRule(INVALID_DATE, ComparisonRule.LESS_THAN, SECONDS);
        verifyDateTimeParseExceptionRecording();
    }

    @Test
    void testIsDateInFormatLessFailedToParseDate()
    {
        dateValidationsSteps.doesDateConformRule(INVALID_DATE, DATE_PATTERN, ComparisonRule.LESS_THAN, SECONDS);
        verifyDateTimeParseExceptionRecording();
    }

    private void verifyDateTimeParseExceptionRecording()
    {
        verifyExceptionRecording(DateTimeParseException.class, "Text 'invalidDate' could not be parsed at index 0");
    }

    @Test
    void testIsDateInFormatLessInvalidPattern()
    {
        dateValidationsSteps
                .doesDateConformRule(Instant.now().toString(), "invalidDatePattern", ComparisonRule.LESS_THAN, SECONDS);
        verifyExceptionRecording(IllegalArgumentException.class, "Unknown pattern letter: i");
    }

    @Test
    void testCompareDatesWithTimeZone()
    {
        String date1 = "2017-02-22T07:06:04.110Z";
        String date2 = "2017-02-22T07:06:04.11Z";
        ZonedDateTime zonedDateTime1 = ZonedDateTime.parse(date1);
        ZonedDateTime zonedDateTime2 = ZonedDateTime.parse(date2);
        verifyDatesComparison(date1, date2, zonedDateTime1, zonedDateTime2);
    }

    @Test
    void testCompareDatesWithoutTimeZone()
    {
        String date1 = "2017-02-22T07:06:04";
        String date2 = "2017-02-22T07:06:04Z";
        ZonedDateTime zonedDateTime1 = LocalDateTime.parse(date1).atZone(TIME_ZONE_ID);
        ZonedDateTime zonedDateTime2 = ZonedDateTime.parse(date2);
        verifyDatesComparison(date1, date2, zonedDateTime1, zonedDateTime2);
    }

    @Test
    void testCompareDatesWithoutTime()
    {
        String date1 = "2021-04-19";
        String date2 = "2021-04-20";
        ZonedDateTime zonedDateTime1 = LocalDate.parse(date1, DateTimeFormatter.ISO_DATE).atStartOfDay(TIME_ZONE_ID);
        ZonedDateTime zonedDateTime2 = LocalDate.parse(date2, DateTimeFormatter.ISO_DATE).atStartOfDay(TIME_ZONE_ID);
        verifyDatesComparison(date1, date2, zonedDateTime1, zonedDateTime2);
    }

    private void verifyDatesComparison(String date1, String date2, ZonedDateTime zonedDateTime1,
            ZonedDateTime zonedDateTime2)
    {
        dateValidationsSteps.compareDates(date1, ComparisonRule.EQUAL_TO, date2);
        verify(softAssert).assertThat(eq("Compare dates"), eq(zonedDateTime1), verifyMatcher(zonedDateTime2));
        verifyNoMoreInteractions(softAssert);
    }

    @Test
    void testCompareDatesFailedToParseDate()
    {
        dateValidationsSteps.compareDates(INVALID_DATE, ComparisonRule.EQUAL_TO, INVALID_DATE);
        verifyDateTimeParseExceptionRecording();
    }

    private void verifyExceptionRecording(Class<? extends Exception> clazz, String message)
    {
        verify(softAssert).recordFailedAssertion(
                (Exception) argThat(arg -> clazz.isInstance(arg) && message.equals(clazz.cast(arg).getMessage())));
        verifyNoMoreInteractions(softAssert);
    }

    private <T, K> T verifyMatcher(K matching)
    {
        return argThat(arg -> arg instanceof TypeSafeMatcher && ((TypeSafeMatcher<?>) arg).matches(matching));
    }
}
