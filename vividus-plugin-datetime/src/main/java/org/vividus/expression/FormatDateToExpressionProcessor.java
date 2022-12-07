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
public class FormatDateToExpressionProcessor extends AbstractExpressionProcessor<String>
{
    private static final Pattern FORMAT_TO_PATTERN = Pattern.compile(
            "^formatDateTo\\(\\s*(\"\"\".+?\"\"\"|.+?),(?<!\\\\,)\\s*(\"\"\".+?\"\"\"|.+?),(?<!\\\\,)(.+?)\\)$",
            Pattern.CASE_INSENSITIVE);
    private static final int INPUT_DATE_GROUP = 1;
    private static final int OLD_FORMAT_GROUP = 2;
    private static final int NEW_FORMAT_GROUP = 3;

    private final DateUtils dateUtils;

    public FormatDateToExpressionProcessor(DateUtils dateUtils)
    {
        super(FORMAT_TO_PATTERN);
        this.dateUtils = dateUtils;
    }

    @Override
    protected String evaluateExpression(ExpressionArgumentMatcher expressionMatcher)
    {
        String inputDate = expressionMatcher.getArgument(INPUT_DATE_GROUP);
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(
                expressionMatcher.getArgument(OLD_FORMAT_GROUP), Locale.ENGLISH);
        ZonedDateTime zonedDate = dateUtils.parseDateTime(inputDate, dateTimeFormatter);
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern(expressionMatcher.getArgument(NEW_FORMAT_GROUP),
                Locale.ENGLISH);
        return outputFormatter.format(zonedDate);
    }
}
