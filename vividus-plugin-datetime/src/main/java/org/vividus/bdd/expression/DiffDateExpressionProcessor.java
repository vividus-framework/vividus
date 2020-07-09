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

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Named;

import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.vividus.util.DateUtils;

@Named
public class DiffDateExpressionProcessor implements IExpressionProcessor
{
    private static final int FORMAT_GROUP = 6;
    private static final Pattern DIFF_DATE_PATTERN = Pattern
            .compile("^diffDate\\((.+?),(?<!\\\\,)(.+?),(?<!\\\\,)(.+?),(?<!\\\\,)(.+?)(,(?<!\\\\,)(.+?))?\\)$",
                    Pattern.CASE_INSENSITIVE);
    private static final String MINUS_SIGN = "-";

    private static final int FIRST_INPUT_DATE_GROUP = 1;
    private static final int FIRST_INPUT_FORMAT_GROUP = 2;
    private static final int SECOND_INPUT_DATE_GROUP = 3;
    private static final int SECOND_INPUT_FORMAT_GROUP = 4;

    private final DateUtils dateUtils;

    public DiffDateExpressionProcessor(DateUtils dateUtils)
    {
        this.dateUtils = dateUtils;
    }

    @Override
    public Optional<String> execute(String expression)
    {
        Matcher expressionMatcher = DIFF_DATE_PATTERN.matcher(expression);
        if (expressionMatcher.find())
        {
            ZonedDateTime firstZonedDateTime = getZonedDateTime(expressionMatcher, FIRST_INPUT_DATE_GROUP,
                    FIRST_INPUT_FORMAT_GROUP);
            ZonedDateTime secondZonedDateTime = getZonedDateTime(expressionMatcher, SECOND_INPUT_DATE_GROUP,
                    SECOND_INPUT_FORMAT_GROUP);
            Duration duration = Duration.between(firstZonedDateTime, secondZonedDateTime);
            String durationAsString = duration.toString();
            return Optional.ofNullable(expressionMatcher.group(FORMAT_GROUP))
                           .map(String::trim)
                           .map(String::toUpperCase)
                           .map(t -> EnumUtils.getEnum(ChronoUnit.class, t))
                           .map(u -> u.between(firstZonedDateTime, secondZonedDateTime))
                           .map(Object::toString)
                           .or(() -> processNegative(duration, durationAsString));
        }
        return Optional.empty();
    }

    private Optional<String> processNegative(Duration duration, String durationAsString)
    {
        return duration.isNegative()
                ? Optional.of(MINUS_SIGN + durationAsString.replace(MINUS_SIGN, StringUtils.EMPTY))
                : Optional.of(durationAsString);
    }

    private ZonedDateTime getZonedDateTime(Matcher expressionMatcher, int inputDateGroup, int inputFormatGroup)
    {
        DateTimeFormatter inputFormat = DateTimeFormatter
                .ofPattern(normalize(expressionMatcher.group(inputFormatGroup)), Locale.ENGLISH);
        return dateUtils.parseDateTime(normalize(expressionMatcher.group(inputDateGroup)), inputFormat);
    }

    private String normalize(String argument)
    {
        return StringUtils.replace(argument.trim(), "\\,", ",");
    }
}
