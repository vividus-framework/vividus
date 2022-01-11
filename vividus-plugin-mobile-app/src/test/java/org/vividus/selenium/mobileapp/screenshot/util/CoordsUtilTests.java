/*
 * Copyright 2019-2022 the original author or authors.
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

package org.vividus.selenium.mobileapp.screenshot.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import ru.yandex.qatools.ashot.coordinates.Coords;

class CoordsUtilTests
{
    public static final Coords COORDS =  new Coords(3, 5, 8, 10);

    private static Stream<Arguments> coordsProvider()
    {
        return Stream.of(
            Arguments.of(2.49, COORDS),
            Arguments.of(2.5, COORDS),
            Arguments.of(2.51, new Coords(3, 6, 8, 11))
        );
    }

    @ParameterizedTest
    @CsvSource({"2.49, 5", "2.5, 5", "2.51, 6"})
    void shouldMultiplyAndProvideRoundedIntResult(double dpr, int expectedResult)
    {
        int coordinate = 2;
        int multipliedCoordinate = CoordsUtil.scale(coordinate, dpr);
        assertEquals(expectedResult, multipliedCoordinate);
    }

    @ParameterizedTest
    @MethodSource("coordsProvider")
    void shouldMultiplyAndProvideRoundedIntResult(double dpr, Coords expectedResult)
    {
        Coords sourceCoords = new Coords(1, 2, 3, 4);
        Coords result = CoordsUtil.scale(sourceCoords, dpr);
        assertEquals(expectedResult, result);
    }
}
