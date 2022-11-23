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

package org.vividus.mobileapp.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.Rectangle;

class ZoomTypeTests
{
    @CsvSource({
            "OUT,  216, 1221, 475, 895, 864, 407, 604, 732, 0, 0",
            "IN,   475, 895, 216, 1221, 604, 732, 864, 407, 0, 0",
            "OUT,  251, 1242, 510, 916, 899, 428, 639, 753, 35, 21",
            "IN,   510, 916, 251, 1242, 639, 753, 899, 428, 35, 21"
    })
    @ParameterizedTest
    @SuppressWarnings("paramNum")
    void shouldCalculateCoordinates(ZoomType zoomType, int pointer1StartX, int pointer1StartY, int pointer1EndX,
            int pointer1EndY, int pointer2StartX, int pointer2StartY, int pointer2EndX, int pointer2EndY, int pointX,
            int pointY)
    {
        Dimension dimension = new Dimension(1080, 2036);
        Rectangle zoomArea = new Rectangle(new Point(pointX, pointY), dimension);
        ZoomCoordinates coordinates = zoomType.calculateCoordinates(zoomArea);

        assertPoint(coordinates.getFinger1MoveCoordinates().getStart(), pointer1StartX, pointer1StartY);
        assertPoint(coordinates.getFinger1MoveCoordinates().getEnd(), pointer1EndX, pointer1EndY);
        assertPoint(coordinates.getFinger2MoveCoordinates().getStart(), pointer2StartX, pointer2StartY);
        assertPoint(coordinates.getFinger2MoveCoordinates().getEnd(), pointer2EndX, pointer2EndY);
    }

    private static void assertPoint(Point point, int x, int y)
    {
        assertEquals(new Point(x, y), point);
    }
}
