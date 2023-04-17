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

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.jbehave.core.expressions.MultiArgExpressionProcessor;
import org.vividus.util.DateUtils;

public class FormatDateExpressionProcessor extends MultiArgExpressionProcessor<String>
{
    private static final int MIN_ARG_NUMBER = 2;
    private static final int MAX_ARG_NUMBER = 3;

    private static final int INPUT_DATE_ARG_INDEX = 0;
    private static final int OUTPUT_FORMAT_ARG_INDEX = 1;
    private static final int OUTPUT_TIMEZONE_ARG_INDEX = 2;

    private static final DateTimeFormatter ISO_STANDARD_FORMAT = DateTimeFormatter.ISO_DATE_TIME;

    public FormatDateExpressionProcessor(DateUtils dateUtils)
    {
        super("formatDate", MIN_ARG_NUMBER, MAX_ARG_NUMBER, args -> {
            ZonedDateTime inputDate = dateUtils.parseDateTime(args.get(INPUT_DATE_ARG_INDEX), ISO_STANDARD_FORMAT);
            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern(args.get(OUTPUT_FORMAT_ARG_INDEX));
            if (args.size() == MAX_ARG_NUMBER)
            {
                ZoneId outputZone = ZoneId.of(args.get(OUTPUT_TIMEZONE_ARG_INDEX));
                inputDate = inputDate.withZoneSameInstant(outputZone);
            }
            return outputFormatter.format(inputDate);
        });
    }
}
