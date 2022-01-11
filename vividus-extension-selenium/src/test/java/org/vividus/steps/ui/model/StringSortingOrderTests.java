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

package org.vividus.steps.ui.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class StringSortingOrderTests
{
    private static final String KEY_1 = "Cry Over Spilt Milk";
    private static final String KEY_2 = "an Arm and a Leg";
    private static final String KEY_3 = "A Piece of Cake";
    private static final String KEY_4 = "Beating Around the Bush";

    private static Stream<Arguments> dataProvider()
    {
        return Stream.of(
            Arguments.of(StringSortingOrder.ASCENDING, List.of(KEY_3, KEY_4, KEY_1, KEY_2)),
            Arguments.of(StringSortingOrder.DESCENDING, List.of(KEY_2, KEY_1, KEY_4, KEY_3)),
            Arguments.of(StringSortingOrder.CASE_INSENSITIVE_ASCENDING, List.of(KEY_3, KEY_2, KEY_4, KEY_1)),
            Arguments.of(StringSortingOrder.CASE_INSENSITIVE_DESCENDING, List.of(KEY_1, KEY_4, KEY_2, KEY_3))
        );
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    void shouldReturnSortingComparator(StringSortingOrder sortingOrder, List<String> expected)
    {
        List<String> sorted = List.of(KEY_1, KEY_2, KEY_3, KEY_4).stream()
                                                                 .sorted(sortingOrder.getSortingType())
                                                                 .collect(Collectors.toList());
        assertEquals(expected, sorted);
    }
}
