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
public class FormatDateToExpressionProcessor implements IExpressionProcessor
{
    private static final Pattern FORMAT_TO_PATTERN = Pattern
            .compile("^formatDateTo\\((.+?),(?<!\\\\,)(.+?),(?<!\\\\,)(.+?)\\)$", Pattern.CASE_INSENSITIVE);
    private static final int INPUT_DATE_GROUP = 1;
    private static final int OLD_FORMAT_GROUP = 2;
    private static final int NEW_FORMAT_GROUP = 3;

    private final DateUtils dateUtils;

    public FormatDateToExpressionProcessor(DateUtils dateUtils)
    {
        this.dateUtils = dateUtils;
    }

    @Override
    public Optional<String> execute(String expression)
    {
        Matcher formatToExpressionMatcher = FORMAT_TO_PATTERN.matcher(expression);
        if (formatToExpressionMatcher.find())
        {
            String inputDate = normalize(formatToExpressionMatcher.group(INPUT_DATE_GROUP));
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(
                    normalize(formatToExpressionMatcher.group(OLD_FORMAT_GROUP)));
            ZonedDateTime zonedDate = dateUtils.parseDateTime(inputDate, dateTimeFormatter);
            return formatDate(zonedDate, normalize(formatToExpressionMatcher.group(NEW_FORMAT_GROUP)));
        }
        return Optional.empty();
    }

    private Optional<String> formatDate(ZonedDateTime zonedDateTime, String outputFormat)
    {
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern(outputFormat);
        return Optional.of(outputFormatter.format(zonedDateTime));
    }

    private String normalize(String argument)
    {
        return StringUtils.replace(argument.trim(), "\\,", ",");
    }
}
