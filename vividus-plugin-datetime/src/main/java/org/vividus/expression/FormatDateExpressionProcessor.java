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

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Named;

import org.vividus.util.DateUtils;

@Named
public class FormatDateExpressionProcessor extends AbstractExpressionProcessor<String>
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
        super(FORMAT_PATTERN);
        this.dateUtils = dateUtils;
    }

    @Override
    protected String evaluateExpression(Matcher expressionMatcher)
    {
        ZonedDateTime zonedDate = dateUtils.parseDateTime(expressionMatcher.group(INPUT_DATE_GROUP),
                ISO_STANDARD_FORMAT);
        String outputFormat = expressionMatcher.group(OUTPUT_FORMAT_GROUP);
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern(outputFormat);
        String outputTimeZone = expressionMatcher.group(OUTPUT_TIMEZONE_GROUP);
        if (outputTimeZone != null)
        {
            zonedDate = zonedDate.withZoneSameInstant(ZoneId.of(outputTimeZone));
        }
        return outputFormatter.format(zonedDate);
    }
}
