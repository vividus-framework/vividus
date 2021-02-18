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

package org.vividus.ui.action.search;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.vividus.ui.State;

class VisibilityTests
{
    @ParameterizedTest
    @MethodSource("elementTypeSource")
    void shouldParseStringIntoElementType(String toParse, Visibility expected)
    {
        assertEquals(expected, Visibility.getElementType(toParse));
    }

    @Test
    void shouldThrowExceptionInCaseOfInvalidVisibility()
    {
        IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class,
            () -> Visibility.getElementType("invalid"));
        assertEquals("Illegal visibility type 'invalid'. Expected one of 'visible', 'invisible', 'all'",
                illegalArgumentException.getMessage());
    }

    @Test
    void shouldThrowExceptionInCaseOfEmptyVisibility()
    {
        IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class,
            () -> Visibility.getElementType(""));
        assertEquals("Visibility type can not be empty. Expected one of 'visible', 'invisible', 'all'",
                illegalArgumentException.getMessage());
    }

    @ParameterizedTest
    @CsvSource({ "VISIBLE, VISIBLE", "INVISIBLE, NOT_VISIBLE", "ALL, "})
    void shouldReturnCorrectState(Visibility visibility, State expectedState)
    {
        assertEquals(visibility.getState(), expectedState);
    }

    static Stream<Arguments> elementTypeSource()
    {
        return Stream.of(Arguments.of("i",                 Visibility.INVISIBLE),
                         Arguments.of("in",                Visibility.INVISIBLE),
                         Arguments.of("I",                 Visibility.INVISIBLE),
                         Arguments.of("v",                 Visibility.VISIBLE),
                         Arguments.of("all",               Visibility.ALL),
                         Arguments.of("ALL",               Visibility.ALL),
                         Arguments.of("aLl",               Visibility.ALL),
                         Arguments.of(" aLl ",             Visibility.ALL));
    }
}
