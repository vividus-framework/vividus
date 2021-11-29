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

package org.vividus.expression;

import java.time.Duration;
import java.time.Period;
import java.time.ZonedDateTime;
import java.util.regex.Matcher;

import org.apache.commons.lang3.StringUtils;

public final class DateExpression
{
    private static final String DURATION_DESIGNATOR = "P";

    private final String periodString;
    private final String durationString;
    private final String customFormatString;
    private final String minusSign;

    public DateExpression(Matcher matcher, int minusSignGroup, int periodGroup, int durationGroup,
            int formatGroup)
    {
        periodString = matcher.group(periodGroup);
        durationString = matcher.group(durationGroup);
        customFormatString = matcher.group(formatGroup);
        minusSign = matcher.group(minusSignGroup);
    }

    public boolean hasPeriod()
    {
        return exists(periodString);
    }

    public boolean hasDuration()
    {
        return exists(durationString);
    }

    public boolean hasCustomFormat()
    {
        return exists(customFormatString);
    }

    public String getCustomFormatString()
    {
        return customFormatString;
    }

    public ZonedDateTime processPeriod(ZonedDateTime zonedDateTime)
    {
        Period period = Period.parse(DURATION_DESIGNATOR + getPeriodString());
        return !hasMinusSign() ? zonedDateTime.plus(period) : zonedDateTime.minus(period);
    }

    public ZonedDateTime processDuration(ZonedDateTime zonedDateTime)
    {
        Duration duration = Duration.parse(DURATION_DESIGNATOR + getDurationString());
        return !hasMinusSign() ? zonedDateTime.plus(duration) : zonedDateTime.minus(duration);
    }

    private boolean exists(String valueToCheck)
    {
        return StringUtils.isNotEmpty(valueToCheck);
    }

    private String getPeriodString()
    {
        return periodString;
    }

    private String getDurationString()
    {
        return durationString;
    }

    private boolean hasMinusSign()
    {
        return exists(minusSign);
    }
}
