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

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;

public enum CollectionComparisonRule
{
    CONTAIN
    {
        @Override
        public <T> Matcher<Iterable<T>> getComparisonRule(List<T> items)
        {
            return Matchers.hasItems(asArray(items));
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        public <T> Matcher<T[]> getComparisonRule(T[] items)
        {
            return Stream.of(items)
                         .map(Matchers::hasItemInArray)
                         .collect(Collectors.collectingAndThen(Collectors.toList(),
                             m -> Matchers.allOf((Iterable) m)));
        }
    },
    ARE_EQUAL_TO
    {
        @Override
        public <T> Matcher<Iterable<T>> getComparisonRule(List<T> items)
        {
            return Matchers.allOf(Matchers.containsInAnyOrder(asArray(items)));
        }

        @Override
        public <T> Matcher<T[]> getComparisonRule(T[] items)
        {
            return Matchers.arrayContainingInAnyOrder(items);
        }
    },
    ARE_EQUAL_TO_ORDERED_COLLECTION
    {
        @Override
        public <T> Matcher<Iterable<T>> getComparisonRule(List<T> items)
        {
            return Matchers.equalTo(items);
        }

        @Override
        public <T> Matcher<T[]> getComparisonRule(T[] items)
        {
            return Matchers.arrayContaining(items);
        }
    };

    @SuppressWarnings("unchecked")
    private static <T> T[] asArray(List<T> items)
    {
        return (T[]) items.toArray(Object[]::new);
    }

    public abstract <T> Matcher<Iterable<T>> getComparisonRule(List<T> items);

    public abstract <T> Matcher<T[]> getComparisonRule(T[] items);
}
