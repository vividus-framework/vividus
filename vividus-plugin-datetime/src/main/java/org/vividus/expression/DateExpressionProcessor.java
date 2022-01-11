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

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.vividus.util.DateUtils;

public class DateExpressionProcessor extends AbstractExpressionProcessor<String>
{
    private static final String DATE_TIME_REGEX_PART = "((-)?P((?:\\d+[YMWD])*)(T(?:\\d+[HMS])+)?)";

    private static final Pattern GENERATE_DATE_PATTERN = Pattern
            .compile("^generateDate\\(" + DATE_TIME_REGEX_PART + "(,\\s*(.*))?\\)$");
    private static final int GENERATE_DATE_FORMAT_GROUP = 6;

    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private static final int MINUS_SIGN_GROUP = 2;
    private static final int PERIOD_GROUP = 3;
    private static final int DURATION_GROUP = 4;

    private final DateUtils dateUtils;
    private Locale locale;

    public DateExpressionProcessor(DateUtils dateUtils)
    {
        super(GENERATE_DATE_PATTERN);
        this.dateUtils = dateUtils;
    }

    @Override
    protected String evaluateExpression(Matcher expressionMatcher)
    {
        DateExpression dateExpression = new DateExpression(expressionMatcher, MINUS_SIGN_GROUP, PERIOD_GROUP,
                DURATION_GROUP, GENERATE_DATE_FORMAT_GROUP);
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
        return current.format(format);
    }

    public void setLocale(Locale locale)
    {
        this.locale = locale;
    }
}
