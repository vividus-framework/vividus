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

package org.vividus.bdd.steps;

import java.util.Optional;
import java.util.stream.Stream;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;

public enum ComparisonRule implements IComparisonRule
{
    LESS_THAN("<")
    {
        @Override
        public <T extends Comparable<T>> Matcher<T> getComparisonRule(T variable)
        {
            return Matchers.lessThan(variable);
        }
    },
    LESS_THAN_OR_EQUAL_TO("<=")
    {
        @Override
        public <T extends Comparable<T>> Matcher<T> getComparisonRule(T variable)
        {
            return Matchers.lessThanOrEqualTo(variable);
        }
    },
    GREATER_THAN(">")
    {
        @Override
        public <T extends Comparable<T>> Matcher<T> getComparisonRule(T variable)
        {
            return Matchers.greaterThan(variable);
        }
    },
    GREATER_THAN_OR_EQUAL_TO(">=")
    {
        @Override
        public <T extends Comparable<T>> Matcher<T> getComparisonRule(T variable)
        {
            return Matchers.greaterThanOrEqualTo(variable);
        }
    },
    EQUAL_TO("=")
    {
        @Override
        public <T extends Comparable<T>> Matcher<T> getComparisonRule(T variable)
        {
            return variable instanceof String
                    ? Matchers.equalTo(variable) : Matchers.comparesEqualTo(variable);
        }
    },
    NOT_EQUAL_TO("!=")
    {
        @Override
        public <T extends Comparable<T>> Matcher<T> getComparisonRule(T variable)
        {
            return Matchers.not(EQUAL_TO.getComparisonRule(variable));
        }
    };

    private final String sign;

    ComparisonRule(String sign)
    {
        this.sign = sign;
    }

    public static ComparisonRule parse(String sign)
    {
        return bySign(sign).orElseThrow(() ->
            new IllegalArgumentException("Unknown comparison sign: \'" + sign + "\'"));
    }

    public static Optional<ComparisonRule> bySign(String sign)
    {
        return Stream.of(ComparisonRule.values())
                .filter(comparisonRule -> sign.equalsIgnoreCase(comparisonRule.sign))
                .findFirst();
    }

    public String toString()
    {
        return super.toString().replace('_', ' ').toLowerCase();
    }

    public String getSign()
    {
        return sign;
    }
}
