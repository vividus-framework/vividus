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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Utility class for date parsing.
 */
public class DateUtils
{
    private final ZoneId zoneId;

    public DateUtils(ZoneId zoneId)
    {
        this.zoneId = zoneId;
    }

    /**
     * Obtains the current date-time from the system clock in the specified time-zone.
     * @return the current date-time using the system clock
     */
    public ZonedDateTime getCurrentDateTime()
    {
        return ZonedDateTime.now(zoneId);
    }

    /**
     * <p>Obtains an instance of ZonedDateTime from a text string using an ISO-like date-time formatter.</p>
     * <p>If time part is not present, it will be set to 00:00</p>
     * <p>If offset or zone is not present (and not required by the formatter), a date-time will be parsed
     * to LocalDateTime and then converted to ZonedDateTime using the system default time-zone.</p>
     * @param dateTime A date-time text string
     * @param formatter A formatter for parsing date-time strings
     * @return an instance of ZonedDateTime
     */
    public ZonedDateTime parseDateTime(String dateTime, DateTimeFormatter formatter)
    {
        ZonedDateTime zonedDateTime;
        try
        {
            zonedDateTime = ZonedDateTime.parse(dateTime, formatter);
        }
        catch (DateTimeParseException e)
        {
            try
            {
                LocalDateTime localDateTime = LocalDateTime.parse(dateTime, formatter);
                zonedDateTime = ZonedDateTime.of(localDateTime, zoneId);
            }
            catch (DateTimeParseException err)
            {
                LocalDate localDate = LocalDate.parse(dateTime, formatter);
                zonedDateTime = ZonedDateTime.of(localDate, LocalTime.MIN, zoneId);
            }
        }
        return zonedDateTime;
    }
}
