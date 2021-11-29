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

package org.vividus.steps.db;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import com.google.common.collect.ListMultimap;

import org.apache.commons.lang3.tuple.Pair;

public enum DataSetComparisonRule
{
    IS_EQUAL_TO("Query results are equal")
    {
        @Override
        Stream<Object> collectComparisonKeys(ListMultimap<Object, Map<String, Object>> leftData,
                ListMultimap<Object, Map<String, Object>> rightData)
        {
            return Stream.concat(leftData.keySet().stream(), rightData.keySet().stream()).distinct();
        }

        @Override
        public void fillStatistics(DataSourceStatistics statistics,
                List<Pair<Map<String, Object>, Map<String, Object>>> comparison)
        {
            statistics.getLeft().setNoPair(comparison.stream().map(Pair::getRight).filter(Map::isEmpty).count());
            statistics.getRight().setNoPair(comparison.stream().map(Pair::getLeft).filter(Map::isEmpty).count());
        }
    },
    CONTAINS("The left data set contains all rows from the right data set")
    {
        @Override
        Stream<Object> collectComparisonKeys(ListMultimap<Object, Map<String, Object>> leftData,
                ListMultimap<Object, Map<String, Object>> rightData)
        {
            return rightData.keySet().stream();
        }

        @Override
        public void fillStatistics(DataSourceStatistics statistics,
                List<Pair<Map<String, Object>, Map<String, Object>>> comparison)
        {
            statistics.getRight().setNoPair(comparison.stream().map(Pair::getLeft).filter(Map::isEmpty).count());
        }
    };

    private final String assertionDescription;

    DataSetComparisonRule(String assertionDescription)
    {
        this.assertionDescription = assertionDescription;
    }

    public String getAssertionDescription()
    {
        return assertionDescription;
    }

    abstract Stream<Object> collectComparisonKeys(ListMultimap<Object, Map<String, Object>> leftData,
            ListMultimap<Object, Map<String, Object>> rightData);

    abstract void fillStatistics(DataSourceStatistics statistics,
            List<Pair<Map<String, Object>, Map<String, Object>>> comparison);
}
