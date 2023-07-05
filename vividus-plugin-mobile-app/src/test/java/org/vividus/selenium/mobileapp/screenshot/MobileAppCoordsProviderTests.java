/*
 * Copyright 2019-2023 the original author or authors.
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

package org.vividus.selenium.mobileapp.screenshot;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.vividus.selenium.mobileapp.MobileAppWebDriverManager;
import org.vividus.ui.context.IUiContext;

import pazone.ashot.coordinates.Coords;

@ExtendWith(MockitoExtension.class)
class MobileAppCoordsProviderTests
{
    private static final Point POINT = new Point(10, 10);
    private static final Dimension DIMENSION = new Dimension(1, 1);

    @Mock private MobileAppWebDriverManager driverManager;
    @Mock private IUiContext uiContext;
    @Mock private WebElement webElement;

    @Test
    void shouldProvideAdjustedWithNativeHeaderHeightCoordinates()
    {
        MobileAppCoordsProvider coordsProvider = new MobileAppCoordsProvider(driverManager, uiContext);
        when(driverManager.getDpr()).thenReturn(1.0);
        when(driverManager.getStatusBarSize()).thenReturn(100);
        when(webElement.getLocation()).thenReturn(new Point(0, 234));
        when(webElement.getSize()).thenReturn(DIMENSION);
        Coords coords = coordsProvider.ofElement(null, webElement);
        assertAll(
                () -> assertEquals(0, coords.getX()),
                () -> assertEquals(134, coords.getY()),
                () -> assertEquals(1, coords.getWidth()),
                () -> assertEquals(1, coords.getHeight())
        );
    }

    @Test
    void shouldNotAdjustCoordsForTheCurrentSearchContext()
    {
        MobileAppCoordsProvider coordsProvider = new MobileAppCoordsProvider(driverManager, uiContext);
        when(driverManager.getDpr()).thenReturn(1.0);
        WebElement contextElement = mock(WebElement.class);
        when(contextElement.getLocation()).thenReturn(POINT);
        when(contextElement.getSize()).thenReturn(new Dimension(100, 50));
        Coords coords = coordsProvider.ofElement(null, contextElement);
        assertAll(
                () -> assertEquals(10, coords.getX()),
                () -> assertEquals(10, coords.getY()),
                () -> assertEquals(100, coords.getWidth()),
                () -> assertEquals(50, coords.getHeight())
        );
    }

    @Test
    void testCoordsIsMultipliedWithDpr()
    {
        MobileAppCoordsProvider coordsDecorator = new MobileAppCoordsProvider(driverManager, uiContext);
        WebElement element = mock(WebElement.class);
        when(element.getLocation()).thenReturn(POINT);
        when(element.getSize()).thenReturn(DIMENSION);
        lenient().when(driverManager.getDpr()).thenReturn(2.0);

        WebDriver driver = mock(WebDriver.class);
        assertEquals(new Coords(20, 20, 2, 2), coordsDecorator.ofElement(driver, element));
    }

    @Test
    void shouldScaleElementCoordsRelativelyToContextCoords()
    {
        WebElement contextElement = mock(WebElement.class);
        mockCoords(contextElement, 0, 138, 768, 1046);
        when(uiContext.getOptionalSearchContext()).thenReturn(Optional.of(contextElement));
        WebElement element = mock(WebElement.class);
        mockCoords(element, 0, 215, 768, 78);
        when(driverManager.getStatusBarSize()).thenReturn(48);
        when(driverManager.getDpr()).thenReturn(1.0810810327529907d);

        MobileAppCoordsProvider coordsDecorator = new MobileAppCoordsProvider(driverManager, uiContext);
        Coords coords = coordsDecorator.ofElement(null, element);
        assertAll(
                () -> assertEquals(0, coords.getX()),
                () -> assertEquals(182, coords.getY()),
                () -> assertEquals(831, coords.getWidth()),
                () -> assertEquals(85, coords.getHeight()));
    }

    @Test
    void shouldNotAdjustCoordsForTheWebDriverSearchContext()
    {
        MobileAppCoordsProvider coordsDecorator = new MobileAppCoordsProvider(driverManager, uiContext);
        WebElement element = mock(WebElement.class);
        when(element.getLocation()).thenReturn(POINT);
        when(element.getSize()).thenReturn(DIMENSION);
        lenient().when(driverManager.getDpr()).thenReturn(2.0);
        WebDriver webDriver = mock(WebDriver.class);
        when(uiContext.getOptionalSearchContext()).thenReturn(Optional.of(webDriver));

        assertEquals(new Coords(20, 20, 2, 2), coordsDecorator.ofElement(webDriver, element));
    }

    @Test
    void shouldNotAdjustCoordsIfElementIsSearchContextElement()
    {
        MobileAppCoordsProvider coordsDecorator = new MobileAppCoordsProvider(driverManager, uiContext);
        WebElement element = mock(WebElement.class);
        when(element.getLocation()).thenReturn(POINT);
        when(element.getSize()).thenReturn(DIMENSION);
        lenient().when(driverManager.getDpr()).thenReturn(2.0);
        when(uiContext.getOptionalSearchContext()).thenReturn(Optional.of(element));

        WebDriver driver = mock(WebDriver.class);
        assertEquals(new Coords(20, 20, 2, 2), coordsDecorator.ofElement(driver, element));
    }

    private void mockCoords(WebElement element, int x, int y, int w, int h)
    {
        Point point = mock(Point.class);
        when(element.getLocation()).thenReturn(point);
        when(point.getX()).thenReturn(x);
        when(point.getY()).thenReturn(y);
        Dimension dimension = mock(Dimension.class);
        when(element.getSize()).thenReturn(dimension);
        when(dimension.getWidth()).thenReturn(w);
        when(dimension.getHeight()).thenReturn(h);
    }
}
