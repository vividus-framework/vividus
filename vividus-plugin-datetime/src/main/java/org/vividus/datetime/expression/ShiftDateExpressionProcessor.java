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

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.Validate;
import org.jbehave.core.expressions.MultiArgExpressionProcessor;
import org.vividus.util.DateUtils;

public class ShiftDateExpressionProcessor extends MultiArgExpressionProcessor<String>
{
    private static final Pattern DURATION_PATTERN = Pattern.compile("(-?P)((?:\\d+[YMWD])*)(T(?:\\d+[HMS])+)?");

    private static final String EXPRESSION_NAME = "shiftDate";
    private static final int EXPECTED_ARGS_NUMBER = 3;
    private static final Locale DEFAULT_LOCALE = Locale.ENGLISH;

    public ShiftDateExpressionProcessor(DateUtils dateUtils)
    {
        super(EXPRESSION_NAME, EXPECTED_ARGS_NUMBER, args -> {
            DateTimeFormatter inputFormat = DateTimeFormatter.ofPattern(args.get(1), DEFAULT_LOCALE);
            ZonedDateTime inputDate = dateUtils.parseDateTime(args.get(0), inputFormat);
            String duration = args.get(2);
            Matcher durationMatcher = DURATION_PATTERN.matcher(duration);
            Validate.isTrue(durationMatcher.matches(),
                    "The third argument of '%s' expression must be a duration in ISO-8601 format, but found: '%s'",
                    EXPRESSION_NAME, duration);
            DateExpression dateExpression = new DateExpression(durationMatcher, args.get(1));
            return dateExpression.format(inputDate, DEFAULT_LOCALE);
        });
    }
}
