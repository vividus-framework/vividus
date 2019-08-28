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

package org.vividus.util.comparison;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class ComparisonUtils
{
    private ComparisonUtils()
    {
        //Utility class
    }

    public static List<EntryComparisonResult> compareMaps(Map<?, ?> var1Map, Map<?, ?> var2Map)
    {
        return Stream.concat(var1Map.keySet().stream(), var2Map.keySet().stream())
                .distinct()
                .map(k -> new EntryComparisonResult(k, convertForConsistency(var1Map.get(k)),
                        convertForConsistency(var2Map.get(k))))
                .collect(Collectors.toList());
    }

    public static List<EntryComparisonResult> checkMapContainsSubMap(Map<?, ?> map, Map<?, ?> subMap)
    {
        return subMap.keySet().stream()
                .map(k -> new EntryComparisonResult(k, convertForConsistency(subMap.get(k)),
                        convertForConsistency(map.get(k))))
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    public static List<List<EntryComparisonResult>> compareListsOfMaps(Object variable1, Object variable2)
    {
        List<List<EntryComparisonResult>> results = new ArrayList<>();
        List<Map<?, ?>> listOfMaps1 = new ArrayList<>((List<Map<?, ?>>) variable1);
        List<Map<?, ?>> listOfMaps2 = new ArrayList<>((List<Map<?, ?>>) variable2);
        Function<List<Map<?, ?>>, Map<?, ?>> removeFirstFunction = lom -> lom.remove(0);
        compareTables(() -> isNotEmptyList(listOfMaps1) && isNotEmptyList(listOfMaps2), listOfMaps1, listOfMaps2,
                results, removeFirstFunction, removeFirstFunction);
        compareTables(() -> isNotEmptyList(listOfMaps1), listOfMaps1, listOfMaps2, results, removeFirstFunction,
            lom -> Collections.emptyMap());
        compareTables(() -> isNotEmptyList(listOfMaps2), listOfMaps1, listOfMaps2, results,
            lom -> Collections.emptyMap(), removeFirstFunction);
        return results;
    }

    private static boolean isNotEmptyList(List<?> list)
    {
        return !list.isEmpty();
    }

    private static void compareTables(BooleanSupplier condition, List<Map<?, ?>> table1, List<Map<?, ?>> table2,
            List<List<EntryComparisonResult>> results, Function<List<Map<?, ?>>, Map<?, ?>> tableRowProvider1,
            Function<List<Map<?, ?>>, Map<?, ?>> tableRowProvider2)
    {
        while (condition.getAsBoolean())
        {
            results.add(compareMaps(tableRowProvider1.apply(table1), tableRowProvider2.apply(table2)));
        }
    }

    private static Object convertForConsistency(Object object)
    {
        return object instanceof Number ? new BigDecimal(object.toString()) : object;
    }

    public static final class EntryComparisonResult
    {
        private final Object key;
        private Object left;
        private Object right;
        private final boolean passed;

        private EntryComparisonResult(Object key, Object left, Object right)
        {
            this.key = key;
            this.left = left;
            this.right = right;
            this.passed = left == null ? right == null : isEqual(left, right);
        }

        private boolean isEqual(Object left, Object right)
        {
            if (left instanceof BigDecimal && right instanceof BigDecimal)
            {
                BigDecimal leftAsBigDecimal = (BigDecimal) left;
                BigDecimal rightAsBigDecimal = (BigDecimal) right;
                int leftScale = leftAsBigDecimal.scale();
                int rightScale = rightAsBigDecimal.scale();
                if (leftScale < rightScale)
                {
                    BigDecimal scaledLeft = leftAsBigDecimal.setScale(rightScale);
                    this.left = scaledLeft;
                    return scaledLeft.equals(right);
                }
                else if (leftScale > rightScale)
                {
                    BigDecimal scaledRight = rightAsBigDecimal.setScale(leftScale);
                    this.right = scaledRight;
                    return scaledRight.equals(left);
                }
            }
            return left.equals(right);
        }

        public Object getKey()
        {
            return key;
        }

        public Object getLeft()
        {
            return left;
        }

        public Object getRight()
        {
            return right;
        }

        public boolean isPassed()
        {
            return passed;
        }

        public String getLeftClassName()
        {
            return getClassName(left);
        }

        public String getRightClassName()
        {
            return getClassName(right);
        }

        private String getClassName(Object toExtract)
        {
            return Optional.ofNullable(toExtract)
                           .map(Object::getClass)
                           .map(Class::getName)
                           .orElse("null");
        }
    }
}
