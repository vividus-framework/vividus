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

package org.vividus.bdd.expression;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.util.DateUtils;

public class DateExpressionProcessor implements IExpressionProcessor
{
    private static final Logger LOGGER = LoggerFactory.getLogger(DateExpressionProcessor.class);

    private static final Pattern ISO_DURATION_PATTERN_WITH_FORMAT_PATTERN = Pattern
            .compile("^((-)?P((?:\\d+[YMWD]{1})*)((?:T?\\d+[HMS]{1})*))(?:\\((.*)\\))?$");
    private static final int FORMAT_GROUP = 5;

    private static final Pattern GENERATE_DATE_PATTERN = Pattern
            .compile("^generateDate\\(((-)?P((?:\\d+[YMWD]{1})*)((?:T?\\d+[HMS]{1})*))(,\\s*(.*))?\\)$");
    private static final int GENERATE_DATE_FORMAT_GROUP = 6;

    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("YYYY-MM-dd\'T\'HH:mm:ss");

    private static final int MINUS_SIGN_GROUP = 2;
    private static final int PERIOD_GROUP = 3;
    private static final int DURATION_GROUP = 4;

    private final DateUtils dateUtils;
    private Locale locale;

    public DateExpressionProcessor(DateUtils dateUtils)
    {
        this.dateUtils = dateUtils;
    }

    @Override
    public Optional<String> execute(String expression)
    {
        Matcher durationMatcher = GENERATE_DATE_PATTERN.matcher(expression);
        if (durationMatcher.find())
        {
            return generateDate(new DateExpression(durationMatcher, MINUS_SIGN_GROUP, PERIOD_GROUP, DURATION_GROUP,
                    GENERATE_DATE_FORMAT_GROUP));
        }
        durationMatcher = ISO_DURATION_PATTERN_WITH_FORMAT_PATTERN.matcher(expression);
        if (durationMatcher.find())
        {
            DateExpression dateExpression = new DateExpression(durationMatcher, MINUS_SIGN_GROUP, PERIOD_GROUP,
                    DURATION_GROUP, FORMAT_GROUP);
            LOGGER.warn("WARNING: The syntax of expression #{{}} is deprecated, use new syntax #{generateDate({}{})",
                    expression, durationMatcher.group(1),
                    dateExpression.hasCustomFormat() ? ", " + dateExpression.getCustomFormatString() : "");
            return generateDate(dateExpression);
        }
        return Optional.empty();
    }

    private Optional<String> generateDate(DateExpression dateExpression)
    {
        ZonedDateTime current = dateUtils.getCurrentDateTime();
        DateTimeFormatter format = DateTimeFormatter.ISO_LOCAL_DATE;
        if (dateExpression.hasPeriod())
        {
            current = dateExpression.processPeriod(current);
        }
        if (dateExpression.hasDuration())
        {
            current = dateExpression.processDuration(current);
            format = DATE_TIME;
        }
        if (dateExpression.hasCustomFormat())
        {
            format = DateTimeFormatter.ofPattern(dateExpression.getCustomFormatString(), locale);
        }
        return Optional.of(current.format(format));
    }

    public void setLocale(Locale locale)
    {
        this.locale = locale;
    }
}
