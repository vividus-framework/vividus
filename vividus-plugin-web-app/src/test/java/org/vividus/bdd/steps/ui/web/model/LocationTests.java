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

package org.vividus.bdd.steps.ui.web.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.Rectangle;

@ExtendWith(MockitoExtension.class)
class LocationTests
{
    private static final Rectangle ORIGIN_RECT = new Rectangle(new Point(400, 2000), new Dimension(50, 100));
    private static final Rectangle TARGET_RECT = new Rectangle(new Point(700, 2500), new Dimension(150, 200));

    static Stream<Arguments> dataForTestGetPoint()
    {
        return Stream.of(
            Arguments.of(Location.TOP, new Point(350, 400)),
            Arguments.of(Location.LEFT, new Point(250, 550)),
            Arguments.of(Location.RIGHT, new Point(450, 550)),
            Arguments.of(Location.BOTTOM, new Point(350, 700)),
            Arguments.of(Location.CENTER, new Point(350, 550)),
            Arguments.of(Location.RIGHT_BOTTOM, new Point(450, 700)),
            Arguments.of(Location.RIGHT_TOP, new Point(450, 400)),
            Arguments.of(Location.LEFT_BOTTOM, new Point(250, 700)),
            Arguments.of(Location.LEFT_TOP, new Point(250, 400))
        );
    }

    @ParameterizedTest
    @MethodSource("dataForTestGetPoint")
    void testGetPoint(Location location, Point expected)
    {
        assertEquals(expected, location.getPoint(ORIGIN_RECT, TARGET_RECT));
    }

    @Test
    void testGetPointFromElementIsBiggerThatToElement()
    {
        assertEquals(new Point(-350, -700), Location.TOP.getPoint(TARGET_RECT, ORIGIN_RECT));
    }
}
