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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.vividus.steps.CollectionComparisonRule.ARE_EQUAL_TO;
import static org.vividus.steps.CollectionComparisonRule.ARE_EQUAL_TO_ORDERED_COLLECTION;
import static org.vividus.steps.CollectionComparisonRule.CONTAIN;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

class CollectionComparisonRuleTests
{
    @TestFactory
    Stream<DynamicTest> listRules()
    {
        return Stream.of(
            dynamicTest("should fail list contains",
                () -> assertFalse(CONTAIN.getComparisonRule(List.of(1, 0)).matches(List.of(1, 1, 1)))),
            dynamicTest("should pass list contains",
                () -> assertTrue(CONTAIN.getComparisonRule(List.of(1, 0)).matches(List.of(0, 1, 0, 1)))),
            dynamicTest("should fail list equal",
                () -> assertFalse(ARE_EQUAL_TO.getComparisonRule(List.of(1, 0, 0)).matches(List.of(0, 1)))),
            dynamicTest("should pass list equal",
                () -> assertTrue(ARE_EQUAL_TO.getComparisonRule(List.of(1, 0, 1)).matches(List.of(1, 1, 0)))),
            dynamicTest("should fail list equal in order",
                () -> assertFalse(ARE_EQUAL_TO_ORDERED_COLLECTION.getComparisonRule(List.of(0, 1, 0))
                    .matches(List.of(1, 0, 0)))),
            dynamicTest("should pass list equal in order",
                () -> assertTrue(ARE_EQUAL_TO_ORDERED_COLLECTION.getComparisonRule(List.of(0, 1, 0))
                    .matches(List.of(0, 1, 0))))
        );
    }

    @TestFactory
    Stream<DynamicTest> arrayRules()
    {
        return Stream.of(
            dynamicTest("should fail array contains",
                () -> assertFalse(CONTAIN.getComparisonRule(new Integer[] { 1, 0 })
                    .matches(new Integer[] { 1, 1, 1 }))),
            dynamicTest("should pass array contains",
                () -> assertTrue(CONTAIN.getComparisonRule(new Integer[] { 1, 0 })
                    .matches(new Integer[] { 0, 1, 0, 1 }))),
            dynamicTest("should fail array equal",
                () -> assertFalse(ARE_EQUAL_TO.getComparisonRule(new Integer[] { 1, 0, 0 })
                    .matches(new Integer[] { 0, 1 }))),
            dynamicTest("should pass array equal",
                () -> assertTrue(ARE_EQUAL_TO.getComparisonRule(new Integer[] { 1, 0, 1 })
                    .matches(new Integer[] { 1, 1, 0 }))),
            dynamicTest("should fail array equal in order",
                () -> assertFalse(ARE_EQUAL_TO_ORDERED_COLLECTION.getComparisonRule(new Integer[] { 0, 1, 0 })
                    .matches(new Integer[] { 1, 0, 0 }))),
            dynamicTest("should pass array equal in order",
                () -> assertTrue(ARE_EQUAL_TO_ORDERED_COLLECTION.getComparisonRule(new Integer[] { 0, 1, 0 })
                    .matches(new Integer[] { 0, 1, 0 })))
        );
    }
}
