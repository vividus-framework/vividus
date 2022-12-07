/*
 * Copyright 2019-2022 the original author or authors.
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
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

import javax.inject.Named;

import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.vividus.util.DateUtils;

@Named
public class DiffDateExpressionProcessor extends AbstractExpressionProcessor<String>
{
    // CHECKSTYLE:OFF
    // @formatter:off
    private static final Pattern DIFF_DATE_PATTERN = Pattern.compile(
            "^diffDate\\(\\s*(\"\"\".+?\"\"\"|.+?),(?<!\\\\,)\\s*(\"\"\".+?\"\"\"|.+?),(?<!\\\\,)\\s*(\"\"\".+?\"\"\"|.+?),(?<!\\\\,)\\s*(\"\"\".+?\"\"\"|.+?)(,(?<!\\\\,)(.+?))?\\)$",
                    Pattern.CASE_INSENSITIVE);
    // CHECKSTYLE:ON
    // @formatter:on
    private static final String MINUS_SIGN = "-";

    private static final int FIRST_INPUT_DATE_GROUP = 1;
    private static final int FIRST_INPUT_FORMAT_GROUP = 2;
    private static final int SECOND_INPUT_DATE_GROUP = 3;
    private static final int SECOND_INPUT_FORMAT_GROUP = 4;
    private static final int FORMAT_GROUP = 6;

    private final DateUtils dateUtils;

    public DiffDateExpressionProcessor(DateUtils dateUtils)
    {
        super(DIFF_DATE_PATTERN);
        this.dateUtils = dateUtils;
    }

    @Override
    protected String evaluateExpression(ExpressionArgumentMatcher expressionMatcher)
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
                .orElseGet(() -> processNegative(duration, durationAsString));
    }

    private String processNegative(Duration duration, String durationAsString)
    {
        return duration.isNegative()
                ? MINUS_SIGN + durationAsString.replace(MINUS_SIGN, StringUtils.EMPTY)
                : durationAsString;
    }

    private ZonedDateTime getZonedDateTime(ExpressionArgumentMatcher expressionMatcher, int inputDateGroup,
                                           int inputFormatGroup)
    {
        DateTimeFormatter inputFormat = DateTimeFormatter
                .ofPattern(expressionMatcher.getArgument(inputFormatGroup), Locale.ENGLISH);
        return dateUtils.parseDateTime(expressionMatcher.getArgument(inputDateGroup), inputFormat);
    }
}
