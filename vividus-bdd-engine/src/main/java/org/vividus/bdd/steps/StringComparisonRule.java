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

package org.vividus.bdd.steps;

import java.util.function.Function;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;

public enum StringComparisonRule
{
    IS_EQUAL_TO(Matchers::equalTo),
    CONTAINS(Matchers::containsString),
    DOES_NOT_CONTAIN(expected -> Matchers.not(CONTAINS.createMatcher(expected)));

    private final Function<String, Matcher<String>> matcherFactory;

    StringComparisonRule(Function<String, Matcher<String>> matcherFactory)
    {
        this.matcherFactory = matcherFactory;
    }

    public Matcher<String> createMatcher(String expected)
    {
        return matcherFactory.apply(expected);
    }
}
