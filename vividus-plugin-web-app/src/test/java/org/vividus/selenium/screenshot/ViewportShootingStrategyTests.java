/*
 * Copyright 2019 the original author or authors.
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

package org.vividus.selenium.screenshot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

import ru.yandex.qatools.ashot.coordinates.Coords;
import ru.yandex.qatools.ashot.shooting.SimpleShootingStrategy;

@ExtendWith(MockitoExtension.class)
class ViewportShootingStrategyTests
{
    private static final String SCRIPT_PAGE_Y = "return window.pageYOffset;";
    private static final String SCRIPT_PAGE_INNER_HEIGHT = "return window.innerHeight;";
    private static final long PAGE_Y = 50;
    private static final long PAGE_INNER_HEIGHT = 100;
    private static final int SCREENSHOT_WIDTH = 100;
    private static final int SCREENSHOT_HEIGHT = 500;

    private ViewportShootingStrategy viewportShootingStrategy;

    @Mock(extraInterfaces = JavascriptExecutor.class)
    private WebDriver webDriver;

    @Mock
    private SimpleShootingStrategy simpleShootingStrategy;

    @Mock
    private BufferedImage bufferedImage;

    @Test
    void testGetScreenshot()
    {
        viewportShootingStrategy = new ViewportShootingStrategy(simpleShootingStrategy);
        BufferedImage screenshot = new BufferedImage(SCREENSHOT_WIDTH, SCREENSHOT_HEIGHT, BufferedImage.TYPE_INT_RGB);
        when(((JavascriptExecutor) webDriver).executeScript(SCRIPT_PAGE_Y)).thenReturn(PAGE_Y);
        when(((JavascriptExecutor) webDriver).executeScript(SCRIPT_PAGE_INNER_HEIGHT)).thenReturn(PAGE_INNER_HEIGHT);
        BufferedImage expectedScreenshot = new BufferedImage(SCREENSHOT_WIDTH, (int) PAGE_INNER_HEIGHT,
                BufferedImage.TYPE_INT_RGB);
        when(simpleShootingStrategy.getScreenshot(webDriver)).thenReturn(screenshot);
        BufferedImage bufferedImageActual = viewportShootingStrategy.getScreenshot(webDriver);
        assertEquals(expectedScreenshot.getWidth(), bufferedImageActual.getWidth());
        assertEquals(expectedScreenshot.getHeight(), bufferedImageActual.getHeight());
    }

    @Test
    void testGetScreenshotWithCoords()
    {
        viewportShootingStrategy = new ViewportShootingStrategy(simpleShootingStrategy);
        ViewportShootingStrategy spy = Mockito.spy(viewportShootingStrategy);
        doReturn(bufferedImage).when(spy).getScreenshot(webDriver);
        spy.getScreenshot(webDriver, new HashSet<>());
        verify(spy).getScreenshot(webDriver);
    }

    @Test
    void testPrepareCoords()
    {
        viewportShootingStrategy = new ViewportShootingStrategy(simpleShootingStrategy);
        Set<Coords> coordsSetExpected = Set.of(new Coords(new Rectangle()));
        Set<Coords> coordsSetActual = viewportShootingStrategy.prepareCoords(coordsSetExpected);
        assertEquals(coordsSetExpected, coordsSetActual);
    }
}
