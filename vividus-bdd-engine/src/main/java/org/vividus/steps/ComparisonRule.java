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

package org.vividus.steps;

import java.util.stream.Stream;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.vividus.util.EnumUtils;

public enum ComparisonRule
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

    public static ComparisonRule fromString(String sign)
    {
        return Stream.of(values())
                     .filter(comparisonRule -> sign.equalsIgnoreCase(comparisonRule.sign))
                     .findFirst()
                     .orElse(null);
    }

    public abstract <T extends Comparable<T>> Matcher<T> getComparisonRule(T variable);

    @Override
    public String toString()
    {
        return EnumUtils.toHumanReadableForm(this);
    }

    public String getSign()
    {
        return sign;
    }
}
