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

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.jbehave.core.expressions.MultiArgExpressionProcessor;
import org.jbehave.core.steps.ParameterConverters.FluentEnumConverter;
import org.vividus.util.DateUtils;

public class DiffDateExpressionProcessor extends MultiArgExpressionProcessor<String>
{
    private static final String MINUS_SIGN = "-";

    private static final int MIN_ARG_NUMBER = 4;
    private static final int MAX_ARG_NUMBER = 5;

    private static final int INPUT_DATE_1_ARG_INDEX = 0;
    private static final int INPUT_FORMAT_1_ARG_INDEX = 1;
    private static final int INPUT_DATE_2_ARG_INDEX = 2;
    private static final int INPUT_FORMAT_2_ARG_INDEX = 3;
    private static final int OUTPUT_UNIT_ARG_INDEX = 4;

    public DiffDateExpressionProcessor(DateUtils dateUtils, FluentEnumConverter fluentEnumConverter)
    {
        super("diffDate", MIN_ARG_NUMBER, MAX_ARG_NUMBER, args ->
        {
            ZonedDateTime inputDate1 = getZonedDateTime(dateUtils, args.get(INPUT_DATE_1_ARG_INDEX),
                    args.get(INPUT_FORMAT_1_ARG_INDEX));
            ZonedDateTime inputDate2 = getZonedDateTime(dateUtils, args.get(INPUT_DATE_2_ARG_INDEX),
                    args.get(INPUT_FORMAT_2_ARG_INDEX));

            if (args.size() == OUTPUT_UNIT_ARG_INDEX)
            {
                Duration duration = Duration.between(inputDate1, inputDate2);
                String durationAsString = duration.toString();
                if (duration.isNegative())
                {
                    durationAsString = MINUS_SIGN + durationAsString.replace(MINUS_SIGN, StringUtils.EMPTY);
                }
                return durationAsString;
            }

            String outputUnit = args.get(OUTPUT_UNIT_ARG_INDEX);
            ChronoUnit chronoUnit = (ChronoUnit) fluentEnumConverter.convertValue(outputUnit, ChronoUnit.class);
            return Long.toString(chronoUnit.between(inputDate1, inputDate2));
        });
    }

    private static ZonedDateTime getZonedDateTime(DateUtils dateUtils, String datetime, String format)
    {
        DateTimeFormatter inputFormat = DateTimeFormatter.ofPattern(format, Locale.ENGLISH);
        return dateUtils.parseDateTime(datetime, inputFormat);
    }
}
