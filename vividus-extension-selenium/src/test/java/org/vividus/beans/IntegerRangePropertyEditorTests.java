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

package org.vividus.beans;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.vividus.model.IntegerRange;

class IntegerRangePropertyEditorTests
{
    private final IntegerRangePropertyEditor editor = new IntegerRangePropertyEditor();

    static Stream<Arguments> rangeSource()
    {
        return Stream.of(
                arguments("0..5", Set.of(0, 1, 2, 3, 4, 5)),
                arguments("4,5", Set.of(4, 5)),
                arguments("-1..4,3..6", Set.of(-1, 0, 1, 2, 3, 4, 5, 6)),
                arguments("3..6,-1..4", Set.of(3, 4, 5, 6, -1, 0, 1, 2))
                );
    }

    @MethodSource("rangeSource")
    @ParameterizedTest
    void testConvert(String rangeAsString, Set<Integer> expected)
    {
        editor.setAsText(rangeAsString);
        assertEquals(expected, ((IntegerRange) editor.getValue()).getRange());
    }

    @Test
    void testConvertIsFailed()
    {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> editor.setAsText("error"));
        assertEquals("Expected integers in format 'number' or 'number..number' but got: error", exception.getMessage());
    }
}
