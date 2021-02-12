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

package org.vividus.bdd.expression;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Named;

import org.apache.commons.lang3.math.NumberUtils;
import org.jbehave.core.steps.ParameterConverters.FluentEnumConverter;

@Named
public class RoundExpressionProcessor extends AbstractExpressionProcessor<String>
{
    private static final Pattern ROUND_EXPRESSION_PATTERN;

    private final FluentEnumConverter fluentEnumConverter;

    static
    {
        String roundingModes = Stream.of(RoundingMode.values())
                                     .map(RoundingMode::name)
                                     .map(String::toLowerCase)
                                     .map(e -> e.replaceAll("_", "[ _]"))
                                     .collect(Collectors.joining("|"));
        String pattern = String.format("^round(?:\\((-?\\d+(?:\\.\\d*)?(?:[Ee]+(?:-|\\+)?\\d+)?)(?:,\\s*(\\d+))?"
                + "(?:,\\s*(%s))?\\))$", roundingModes);
        ROUND_EXPRESSION_PATTERN = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
    }

    public RoundExpressionProcessor(FluentEnumConverter fluentEnumConverter)
    {
        super(ROUND_EXPRESSION_PATTERN);
        this.fluentEnumConverter = fluentEnumConverter;
    }

    @Override
    protected String evaluateExpression(Matcher expressionMatcher)
    {
        RoundExpression roundExpression = new RoundExpression(expressionMatcher, fluentEnumConverter);
        return new BigDecimal(roundExpression.getValue())
                .setScale(roundExpression.getMaxFractionDigits(), roundExpression.getRoundingMode())
                .stripTrailingZeros()
                .toPlainString();
    }

    private static final class RoundExpression
    {
        private static final int VALUE_GROUP = 1;
        private static final int MAX_FRACTION_DIGITS_GROUP = 2;
        private static final int ROUNDING_MODE_GROUP = 3;

        private static final int DEFAULT_MAX_FRACTION_DIGITS = 2;

        private final FluentEnumConverter converter;
        private final String value;
        private final String maxFractionDigits;
        private final String roundingMode;

        RoundExpression(Matcher durationMatcher, FluentEnumConverter fluentEnumConverter)
        {
            converter = fluentEnumConverter;
            value = durationMatcher.group(VALUE_GROUP);
            maxFractionDigits = durationMatcher.group(MAX_FRACTION_DIGITS_GROUP);
            roundingMode = durationMatcher.group(ROUNDING_MODE_GROUP);
        }

        public String getValue()
        {
            return value;
        }

        public int getMaxFractionDigits()
        {
            return isApplicable(maxFractionDigits) ? Integer.parseInt(maxFractionDigits) : DEFAULT_MAX_FRACTION_DIGITS;
        }

        private boolean isApplicable(String valueToCheck)
        {
            return valueToCheck != null && !valueToCheck.isEmpty() && NumberUtils.isCreatable(valueToCheck);
        }

        public RoundingMode getRoundingMode()
        {
            if (roundingMode == null)
            {
                return value.charAt(0) == '-' ? RoundingMode.HALF_DOWN : RoundingMode.HALF_UP;
            }
            return (RoundingMode) converter.convertValue(roundingMode, RoundingMode.class);
        }
    }
}
