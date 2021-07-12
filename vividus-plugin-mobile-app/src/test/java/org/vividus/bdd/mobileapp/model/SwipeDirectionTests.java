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

package org.vividus.bdd.mobileapp.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;

class SwipeDirectionTests
{
    @CsvSource({
        "UP,   1436, 358,  50,  540",
        "DOWN, 358 , 1436, 0,   1",
        "DOWN, 358 , 1436, 100, 1079"
    })
    @ParameterizedTest
    void shouldCalculateCoordinates(SwipeDirection direction, int fromY, int toY, int xPercentage, int x)
    {
        Dimension dimension = new Dimension(1080, 1794);

        SwipeCoordinates coordinates = direction.calculateCoordinates(dimension, xPercentage);

        assertPoint(coordinates.getStart(), x, fromY);
        assertPoint(coordinates.getEnd(), x, toY);
    }

    private static void assertPoint(Point point, int x, int y)
    {
        assertEquals(x, point.getX());
        assertEquals(y, point.getY());
    }
}
