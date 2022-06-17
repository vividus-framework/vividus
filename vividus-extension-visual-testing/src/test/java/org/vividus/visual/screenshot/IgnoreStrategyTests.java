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

package org.vividus.visual.screenshot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.vividus.selenium.screenshot.IgnoreStrategy;

import ru.yandex.qatools.ashot.coordinates.Coords;

class IgnoreStrategyTests
{
    private static final int X = 1;
    private static final int Y = 2;
    private static final int WIDTH = 3;
    private static final int HEIGHT = 4;
    private static final int IMAGE_WIDTH = 5;
    private static final int IMAGE_HEIGHT = 6;

    @Test
    void testElementAreaCutStrategy()
    {
        BufferedImage image = mock(BufferedImage.class);
        Graphics2D graphics = mock(Graphics2D.class);
        when(image.createGraphics()).thenReturn(graphics);
        Coords coords = new Coords(X, Y, WIDTH, HEIGHT);
        Set<Coords> ignoredCoords = new HashSet<>();
        ignoredCoords.add(coords);
        BufferedImage result = IgnoreStrategy.ELEMENT.crop(image, ignoredCoords);
        assertEquals(image, result);
        InOrder inOrder = Mockito.inOrder(graphics);
        inOrder.verify(graphics).setBackground(new Color(0, true));
        inOrder.verify(graphics).clearRect(X, Y, WIDTH, HEIGHT);
        inOrder.verify(graphics).dispose();
    }

    @Test
    void testPageAreaCutStrategy()
    {
        BufferedImage image = new BufferedImage(IMAGE_WIDTH, IMAGE_HEIGHT, BufferedImage.TYPE_INT_RGB);
        Coords coords = new Coords(X, Y, WIDTH, HEIGHT);
        Set<Coords> ignoredCoords = new HashSet<>();
        ignoredCoords.add(coords);
        BufferedImage result = IgnoreStrategy.AREA.crop(image, ignoredCoords);
        assertNotNull(result);
        assertEquals(IMAGE_WIDTH, result.getWidth());
        assertEquals(IMAGE_HEIGHT - HEIGHT, result.getHeight());
    }
}
