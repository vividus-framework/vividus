/*
 * Copyright 2019-2024 the original author or authors.
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.LongFunction;
import java.util.function.ToLongFunction;

import org.jbehave.core.expressions.DelegatingExpressionProcessor;
import org.jbehave.core.expressions.SingleArgExpressionProcessor;
import org.vividus.util.DateUtils;

public class EpochExpressionProcessors extends DelegatingExpressionProcessor
{
    private static final DateTimeFormatter ISO_DATE_TIME_WITH_MILLIS_PRECISION = DateTimeFormatter.ofPattern(
            "yyyy-MM-dd'T'HH:mm:ss.SSS");

    public EpochExpressionProcessors(DateUtils dateUtils)
    {
        super(List.of(
                toEpoch("toEpochSecond", dateUtils, ZonedDateTime::toEpochSecond),
                toEpoch("toEpochMilli", dateUtils, zdt -> zdt.toInstant().toEpochMilli()),
                fromEpoch("fromEpochSecond", DateTimeFormatter.ISO_DATE_TIME, dateUtils::fromEpochSecond),
                fromEpoch("fromEpochMilli", ISO_DATE_TIME_WITH_MILLIS_PRECISION, dateUtils::fromEpochMilli)
        ));
    }

    private static SingleArgExpressionProcessor<String> toEpoch(String funtionName, DateUtils dateUtils,
            ToLongFunction<ZonedDateTime> converter)
    {
        return new SingleArgExpressionProcessor<>(funtionName, arg -> {
            ZonedDateTime zonedDateTime = dateUtils.parseDateTime(arg, DateTimeFormatter.ISO_DATE_TIME);
            return String.valueOf(converter.applyAsLong(zonedDateTime));
        });
    }

    private static SingleArgExpressionProcessor<String> fromEpoch(String funtionName, DateTimeFormatter formatter,
            LongFunction<LocalDateTime> converter)
    {
        return new SingleArgExpressionProcessor<>(funtionName, arg -> {
            LocalDateTime localDateTime = converter.apply(new BigDecimal(arg).longValueExact());
            return formatter.format(localDateTime);
        });
    }
}
