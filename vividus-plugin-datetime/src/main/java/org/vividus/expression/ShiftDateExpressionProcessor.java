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

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.regex.Pattern;

import javax.inject.Named;

import org.vividus.util.DateUtils;

@Named
public class ShiftDateExpressionProcessor extends AbstractExpressionProcessor<String>
{
    private static final Pattern SHIFT_DATE_PATTERN = Pattern.compile(
            "^shiftDate\\((\"\"\".+?\"\"\"|.+?),(?<!\\\\,)(.+?),\\s*(-)?P((?:\\d+[YMWD])*)((?:T?\\d+[HMS])*)\\)$",
            Pattern.CASE_INSENSITIVE);

    private static final int INPUT_DATE_GROUP = 1;
    private static final int FORMAT_GROUP = 2;
    private static final int MINUS_SIGN_GROUP = 3;
    private static final int PERIOD_GROUP = 4;
    private static final int DURATION_GROUP = 5;
    private static final Locale DEFAULT_LOCALE = Locale.ENGLISH;

    private final DateUtils dateUtils;

    public ShiftDateExpressionProcessor(DateUtils dateUtils)
    {
        super(SHIFT_DATE_PATTERN);
        this.dateUtils = dateUtils;
    }

    @Override
    protected String evaluateExpression(ExpressionArgumentMatcher expressionMatcher)
    {
        DateTimeFormatter format = DateTimeFormatter.ofPattern(expressionMatcher.getArgument(FORMAT_GROUP),
                DEFAULT_LOCALE);
        ZonedDateTime zonedDateTime = dateUtils.parseDateTime(expressionMatcher.getArgument(INPUT_DATE_GROUP),
                format);
        DateExpression dateExpression = new DateExpression(expressionMatcher, MINUS_SIGN_GROUP, PERIOD_GROUP,
                DURATION_GROUP, FORMAT_GROUP);
        return dateExpression.format(zonedDateTime, DEFAULT_LOCALE);
    }
}
