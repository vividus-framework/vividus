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

import java.time.Duration;
import java.time.Period;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.regex.Matcher;

import org.apache.commons.lang3.StringUtils;

public final class DateExpression
{
    private static final int DURATION_DESIGNATOR_GROUP = 1;
    private static final int DATE_GROUP = 2;
    private static final int TIME_GROUP = 3;

    private final String durationDesignator;
    private final String dateString;
    private final String timeString;
    private final String outputFormatString;

    public DateExpression(Matcher durationMatcher, String outputFormatString)
    {
        durationDesignator = durationMatcher.group(DURATION_DESIGNATOR_GROUP);
        dateString = durationMatcher.group(DATE_GROUP);
        timeString = durationMatcher.group(TIME_GROUP);
        this.outputFormatString = outputFormatString;
    }

    public String format(ZonedDateTime zonedDateTime, Locale locale)
    {
        DateTimeFormatter outputFormat = DateTimeFormatter.ISO_LOCAL_DATE;
        ZonedDateTime result = zonedDateTime;
        if (exists(dateString))
        {
            result = result.plus(Period.parse(durationDesignator + dateString));
        }
        if (exists(timeString))
        {
            result = result.plus(Duration.parse(durationDesignator + timeString));
            outputFormat = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        }
        if (exists(outputFormatString))
        {
            outputFormat = DateTimeFormatter.ofPattern(outputFormatString, locale);
        }
        return result.format(outputFormat);
    }

    private boolean exists(String valueToCheck)
    {
        return StringUtils.isNotEmpty(valueToCheck);
    }
}
