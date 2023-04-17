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

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.Validate;
import org.jbehave.core.expressions.RelaxedMultiArgExpressionProcessor;
import org.vividus.util.DateUtils;

public class DateExpressionProcessor extends RelaxedMultiArgExpressionProcessor<String>
{
    private static final Pattern DURATION_PATTERN = Pattern.compile("(-?P)((?:\\d+[YMWD])*)(T(?:\\d+[HMS])+)?");

    private static final String FUNCTION_NAME = "generateDate";

    public DateExpressionProcessor(Locale locale, DateUtils dateUtils)
    {
        super(FUNCTION_NAME, 1, 2, args -> {
            String duration = args.get(0);
            Matcher durationMatcher = DURATION_PATTERN.matcher(duration);
            Validate.isTrue(durationMatcher.matches(),
                    "The first argument of '%s' expression must be a duration in ISO-8601 format, but found: '%s'",
                    FUNCTION_NAME, duration);
            DateExpression dateExpression = new DateExpression(durationMatcher, args.size() > 1 ? args.get(1) : null);
            return dateExpression.format(dateUtils.getCurrentDateTime(), locale);
        });
    }
}
