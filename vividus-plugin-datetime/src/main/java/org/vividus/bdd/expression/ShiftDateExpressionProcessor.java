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

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.vividus.util.DateUtils;

@Named
public class ShiftDateExpressionProcessor implements IExpressionProcessor
{
    private static final Pattern SHIFT_DATE_PATTERN = Pattern.compile(
            "^shiftDate\\((.+?),(?<!\\\\,)(.+?),\\s*(-)?P((?:\\d+[YMWD])*)((?:T?\\d+[HMS])*)\\)$",
            Pattern.CASE_INSENSITIVE);

    private static final int INPUT_DATE_GROUP = 1;
    private static final int FORMAT_GROUP = 2;
    private static final int MINUS_SIGN_GROUP = 3;
    private static final int PERIOD_GROUP = 4;
    private static final int DURATION_GROUP = 5;

    private final DateUtils dateUtils;

    public ShiftDateExpressionProcessor(DateUtils dateUtils)
    {
        this.dateUtils = dateUtils;
    }

    @Override
    public Optional<String> execute(String expression)
    {
        Matcher expressionMatcher = SHIFT_DATE_PATTERN.matcher(expression);
        if (expressionMatcher.find())
        {
            DateTimeFormatter format = DateTimeFormatter.ofPattern(normalize(expressionMatcher.group(FORMAT_GROUP)));
            ZonedDateTime zonedDateTime = dateUtils.parseDateTime(normalize(expressionMatcher.group(INPUT_DATE_GROUP)),
                    format);
            DateExpression dateExpression = new DateExpression(expressionMatcher, MINUS_SIGN_GROUP, PERIOD_GROUP,
                    DURATION_GROUP, FORMAT_GROUP);
            if (dateExpression.hasPeriod())
            {
                zonedDateTime = dateExpression.processPeriod(zonedDateTime);
            }
            if (dateExpression.hasDuration())
            {
                zonedDateTime = dateExpression.processDuration(zonedDateTime);
            }
            return Optional.of(format.format(zonedDateTime));
        }
        return Optional.empty();
    }

    private String normalize(String argument)
    {
        return StringUtils.replace(argument.trim(), "\\,", ",");
    }
}
