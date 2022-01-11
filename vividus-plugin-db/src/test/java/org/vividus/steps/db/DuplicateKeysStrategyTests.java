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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class DuplicateKeysStrategyTests
{
    @Test
    void shouldAlwaysReturnOneForDistinctStrategy()
    {
        assertEquals(1, DuplicateKeysStrategy.DISTINCT.getTargetSize(null, 0, 0));
    }

    @ParameterizedTest
    @CsvSource({
        "IS_EQUAL_TO, 2, 3, 3",
        "CONTAINS,    2, 3, 3",
        "IS_EQUAL_TO, 5, 4, 5",
        "CONTAINS,    5, 4, 4",
        "IS_EQUAL_TO, 1, 0, 1",
        "CONTAINS,    1, 0, 0",
        "IS_EQUAL_TO, 0, 1, 1"
    })
    void shouldReturnMaxValueForNoopStrategy(DataSetComparisonRule comparisonRule, int leftSize, int rightSize,
            int expected)
    {
        assertEquals(expected, DuplicateKeysStrategy.NOOP.getTargetSize(comparisonRule, leftSize, rightSize));
    }
}
