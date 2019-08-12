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

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.vividus.util.DateUtils;

public class FormatDateExpressionProcessor implements IExpressionProcessor
{
    private static final Pattern FORMAT_PATTERN = Pattern
            .compile("^formatDate\\(([^,]*),\\s*([^,]*)(?:,\\s*(.*))?\\)$", Pattern.CASE_INSENSITIVE);
    private static final int INPUT_DATE_GROUP = 1;
    private static final int OUTPUT_FORMAT_GROUP = 2;
    private static final int OUTPUT_TIMEZONE_GROUP = 3;
    private static final DateTimeFormatter ISO_STANDARD_FORMAT = DateTimeFormatter.ISO_DATE_TIME;

    private final DateUtils dateUtils;

    public FormatDateExpressionProcessor(DateUtils dateUtils)
    {
        this.dateUtils = dateUtils;
    }

    @Override
    public Optional<String> execute(String expression)
    {
        Matcher expressionMatcher = FORMAT_PATTERN.matcher(expression);
        if (expressionMatcher.find())
        {
            ZonedDateTime zonedDate = dateUtils.parseDateTime(expressionMatcher.group(INPUT_DATE_GROUP),
                    ISO_STANDARD_FORMAT);
            String outputFormat = expressionMatcher.group(OUTPUT_FORMAT_GROUP);
            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern(outputFormat);
            zonedDate = updateTimeZone(expressionMatcher, zonedDate);
            return Optional.of(outputFormatter.format(zonedDate));
        }
        return Optional.empty();
    }

    private ZonedDateTime updateTimeZone(Matcher expressionMatcher, ZonedDateTime zonedDate)
    {
        String outputTimeZone = expressionMatcher.group(OUTPUT_TIMEZONE_GROUP);
        if (outputTimeZone != null)
        {
            return zonedDate.withZoneSameInstant(ZoneId.of(outputTimeZone));
        }
        return zonedDate;
    }
}
