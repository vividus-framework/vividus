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

package org.vividus.selenium.screenshot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.vividus.selenium.manager.IWebDriverManager;

import ru.yandex.qatools.ashot.coordinates.Coords;

@ExtendWith(MockitoExtension.class)
class WebCoordsProviderTests
{
    private static final int X = 1;
    private static final int Y = 2;
    private static final int WIDTH = 3;
    private static final int HEIGHT = 4;
    private static final String SCRIPT = "return pageYOffset";

    @Mock(extraInterfaces = JavascriptExecutor.class)
    private WebDriver webDriver;

    @Mock
    private WebElement webElement;

    @Mock
    private IWebDriverManager webDriverManager;

    @Mock
    private IScrollbarHandler scrollbarHandler;

    @InjectMocks
    private WebCoordsProvider adjustingCoordsProvider;

    @Test
    void testGetCoordsNotIOS()
    {
        mockScrollbarActions();
        when(webDriverManager.isIOS()).thenReturn(Boolean.FALSE);
        when(webElement.getLocation()).thenReturn(new Point(X, Y));
        when(webElement.getSize()).thenReturn(new Dimension(WIDTH, HEIGHT));
        Coords coords = adjustingCoordsProvider.ofElement(webDriver, webElement);
        verify((JavascriptExecutor) webDriver, never()).executeScript(SCRIPT, webElement);
        verifyCoords(coords);
    }

    @Test
    void testGetCoordsIOS()
    {
        mockScrollbarActions();
        when(webDriverManager.isIOS()).thenReturn(Boolean.TRUE);
        when(webElement.getLocation()).thenReturn(new Point(X, 0));
        when(webElement.getSize()).thenReturn(new Dimension(WIDTH, HEIGHT));
        when(((JavascriptExecutor) webDriver).executeScript(SCRIPT)).thenReturn((long) Y);
        Coords coords = adjustingCoordsProvider.ofElement(webDriver, webElement);
        verifyCoords(coords);
    }

    @Test
    void shouldIntersectIgnoredElementWithContextAndAdjustCoordinates()
    {
        Coords expectedCoords = new Coords(1, 2, 4, 6);
        mockScrollbarActions(expectedCoords);
        when(webElement.getLocation()).thenReturn(new Point(X, Y));
        when(webElement.getSize()).thenReturn(new Dimension(4, 6));
        Coords coords = adjustingCoordsProvider.ofElement(webDriver, webElement);
        verify((JavascriptExecutor) webDriver, never()).executeScript(SCRIPT, webElement);
        assertEquals(expectedCoords, coords);
    }

    @Test
    void shouldRelateCoordsIfTheyInsideContext()
    {
        Coords expectedCoords = new Coords(2, 2, WIDTH, HEIGHT);
        mockScrollbarActions(expectedCoords);
        when(webElement.getLocation()).thenReturn(new Point(2, 2));
        when(webElement.getSize()).thenReturn(new Dimension(WIDTH, HEIGHT));
        Coords coords = adjustingCoordsProvider.ofElement(webDriver, webElement);
        verify((JavascriptExecutor) webDriver, never()).executeScript(SCRIPT, webElement);
        assertEquals(expectedCoords, coords);
    }

    private void mockScrollbarActions()
    {
        Coords expectedCoords = new Coords(X, Y, WIDTH, HEIGHT);
        mockScrollbarActions(expectedCoords);
    }

    private void mockScrollbarActions(Coords expectedCoords)
    {
        when(scrollbarHandler.performActionWithHiddenScrollbars(argThat(a -> expectedCoords.equals(a.get()))))
                .thenReturn(expectedCoords);
    }

    private void verifyCoords(Coords coordsToCheck)
    {
        Coords expectedCoords = new Coords(X, Y, WIDTH, HEIGHT);
        assertEquals(expectedCoords, coordsToCheck);
    }
}
