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

package org.vividus.expression;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.Validate;
import org.jbehave.core.expressions.MultiArgExpressionProcessor;
import org.jbehave.core.steps.ParameterConverters.FluentEnumConverter;

public class RoundExpressionProcessor extends MultiArgExpressionProcessor<String>
{
    private static final int MAX_ARGS = 3;

    private static final int DEFAULT_MAX_FRACTION_DIGITS = 2;

    private static final Pattern VALUE_TO_ROUND_PATTERN = Pattern.compile(
            "[-+]?(\\d+\\.?\\d*|(\\.\\d+))(?:[Ee]+[-+]?\\d+)?");
    private static final Pattern MAX_FRACTION_DIGITS_PATTERN = Pattern.compile("\\d+");
    private static final Pattern ROUNDING_MODE_PATTERN = Stream.of(RoundingMode.values())
            .map(RoundingMode::name)
            .map(String::toLowerCase)
            .map(e -> e.replaceAll("_", "[ _]"))
            .collect(Collectors.collectingAndThen(Collectors.joining("|"), Pattern::compile));

    public RoundExpressionProcessor(FluentEnumConverter fluentEnumConverter)
    {
        super("round", 1, MAX_ARGS, args -> {
            String valueToRound = args.get(0);
            Validate.isTrue(VALUE_TO_ROUND_PATTERN.matcher(valueToRound).matches(),
                    "Invalid value to round: '%s'", valueToRound);

            int maxFractionDigits = Optional.ofNullable(args.size() > 1 ? args.get(1) : null)
                    .map(arg -> {
                        Validate.isTrue(MAX_FRACTION_DIGITS_PATTERN.matcher(arg).matches(),
                                "Invalid max fraction digits value: '%s'", arg);
                        return Integer.parseInt(arg);
                    })
                    .orElse(DEFAULT_MAX_FRACTION_DIGITS);

            RoundingMode roundingMode = Optional.ofNullable(args.size() == MAX_ARGS ? args.get(2) : null)
                    .map(arg -> {
                        Validate.isTrue(ROUNDING_MODE_PATTERN.matcher(arg).matches(), "Invalid rounding mode: '%s'",
                                arg);
                        return (RoundingMode) fluentEnumConverter.convertValue(arg, RoundingMode.class);
                    })
                    .orElseGet(() -> valueToRound.charAt(0) == '-' ? RoundingMode.HALF_DOWN : RoundingMode.HALF_UP);

            return new BigDecimal(valueToRound)
                    .setScale(maxFractionDigits, roundingMode)
                    .stripTrailingZeros()
                    .toPlainString();
        });
    }
}
