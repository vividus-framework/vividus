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

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.math.NumberUtils;
import org.vividus.util.comparison.ComparisonUtils;
import org.vividus.util.comparison.ComparisonUtils.EntryComparisonResult;

public abstract class VariableComparator
{
    public boolean compare(Object variable1, ComparisonRule condition, Object variable2)
    {
        if (ComparisonRule.EQUAL_TO.equals(condition))
        {
            if (isEmptyOrListOfMaps(variable1) && isEmptyOrListOfMaps(variable2))
            {
                return compareListsOfMaps(variable1, variable2);
            }
            else if (variable1 instanceof Map && variable2 instanceof Map)
            {
                return compareListsOfMaps(List.of(variable1), List.of(variable2));
            }
        }
        String variable1AsString = String.valueOf(variable1);
        String variable2AsString = String.valueOf(variable2);
        if (NumberUtils.isCreatable(variable1AsString) && NumberUtils.isCreatable(variable2AsString))
        {
            BigDecimal number1 = NumberUtils.createBigDecimal(variable1AsString);
            BigDecimal number2 = NumberUtils.createBigDecimal(variable2AsString);
            return compareValues(number1, condition, number2);
        }
        return compareValues(variable1AsString, condition, variable2AsString);
    }

    protected boolean isEmptyOrListOfMaps(Object list)
    {
        return list instanceof List && (((List<?>) list).isEmpty() || ((List<?>) list).get(0) instanceof Map);
    }

    protected abstract <T extends Comparable<T>> boolean compareValues(T value1, ComparisonRule condition, T value2);

    protected boolean compareListsOfMaps(Object variable1, Object variable2)
    {
        List<List<EntryComparisonResult>> results = ComparisonUtils.compareListsOfMaps(variable1, variable2);
        return areAllResultsPassed(results);
    }

    protected boolean areAllResultsPassed(List<List<EntryComparisonResult>> results)
    {
        return results.stream().flatMap(List::stream).allMatch(EntryComparisonResult::isPassed);
    }
}
