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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Named;

import org.apache.commons.lang3.math.NumberUtils;

@Named
public class RoundExpressionProcessor implements IExpressionProcessor
{
    private static final Pattern ROUND_EXPRESSION_PATTERN = Pattern.compile(
            "^round(?:\\((-?\\d+(?:\\.\\d*)?)(?:,\\s*(\\d+))?\\))$", Pattern.CASE_INSENSITIVE);

    @Override
    public Optional<String> execute(String expression)
    {
        Matcher expressionMatcher = ROUND_EXPRESSION_PATTERN.matcher(expression);
        if (expressionMatcher.find())
        {
            RoundExpression roundExpression = new RoundExpression(expressionMatcher);
            return Optional.of(round(roundExpression.getValue(), roundExpression.getMaxFractionDigits()));
        }
        return Optional.empty();
    }

    private String round(String value, int fractionDigitsNumber)
    {
        RoundingMode mode = value.charAt(0) == '-' ? RoundingMode.HALF_DOWN : RoundingMode.HALF_UP;
        return new BigDecimal(value).setScale(fractionDigitsNumber, mode).stripTrailingZeros().toPlainString();
    }

    private static final class RoundExpression
    {
        private static final int VALUE_GROUP = 1;
        private static final int MAX_FRACTION_DIGITS_GROUP = 2;

        private static final int DEFAULT_MAX_FRACTION_DIGITS = 2;

        private final String value;
        private final String maxFractionDigits;

        RoundExpression(Matcher durationMatcher)
        {
            value = durationMatcher.group(VALUE_GROUP);
            maxFractionDigits = durationMatcher.group(MAX_FRACTION_DIGITS_GROUP);
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
    }
}
