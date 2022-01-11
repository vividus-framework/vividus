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

package org.vividus.selenium.mobileapp.screenshot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.vividus.selenium.mobileapp.MobileAppWebDriverManager;
import org.vividus.ui.context.UiContext;

import ru.yandex.qatools.ashot.coordinates.Coords;

@ExtendWith(MockitoExtension.class)
class MobileAppCoordsProviderTests
{
    private static final Point POINT = new Point(10, 10);
    private static final Dimension DIMENSION = new Dimension(1, 1);

    @Mock private MobileAppWebDriverManager driverManager;
    @Mock private WebElement webElement;
    @Spy private UiContext uiContext;

    @Test
    void shoudProvideAdjustedWithNativeHeaderHeightCoordinates()
    {
        MobileAppCoordsProvider coordsProvider = new MobileAppCoordsProvider(true, driverManager, uiContext);
        when(driverManager.getStatusBarSize()).thenReturn(100);
        when(webElement.getLocation()).thenReturn(new Point(0, 234));
        when(webElement.getSize()).thenReturn(DIMENSION);
        doReturn(null).when(uiContext).getSearchContext();
        Coords coords = coordsProvider.ofElement(null, webElement);
        Assertions.assertAll(() -> assertEquals(0, coords.getX()),
                             () -> assertEquals(134, coords.getY()),
                             () -> assertEquals(1, coords.getWidth()),
                             () -> assertEquals(1, coords.getHeight()));
    }

    @Test
    void shouldAdjustElementCoordsToTheCurrentSearchContext()
    {
        MobileAppCoordsProvider coordsProvider = new MobileAppCoordsProvider(true, driverManager, uiContext);
        WebElement contextElement = mock(WebElement.class);
        when(contextElement.getLocation()).thenReturn(POINT);
        when(contextElement.getSize()).thenReturn(new Dimension(100, 50));
        when(webElement.getLocation()).thenReturn(new Point(5, 15));
        when(webElement.getSize()).thenReturn(new Dimension(150, 30));
        doReturn(contextElement).when(uiContext).getSearchContext();
        Coords coords = coordsProvider.ofElement(null, webElement);
        Assertions.assertAll(() -> assertEquals(0, coords.getX()),
                             () -> assertEquals(5, coords.getY()),
                             () -> assertEquals(100, coords.getWidth()),
                             () -> assertEquals(30, coords.getHeight()));
    }

    @Test
    void shouldNotAdjustCoordsForTheCurrentSearchContext()
    {
        MobileAppCoordsProvider coordsProvider = new MobileAppCoordsProvider(true, driverManager, uiContext);
        WebElement contextElement = mock(WebElement.class);
        when(contextElement.getLocation()).thenReturn(POINT);
        when(contextElement.getSize()).thenReturn(new Dimension(100, 50));
        doReturn(contextElement).when(uiContext).getSearchContext();
        Coords coords = coordsProvider.ofElement(null, contextElement);
        Assertions.assertAll(
                () -> assertEquals(10, coords.getX()),
                () -> assertEquals(10, coords.getY()),
                () -> assertEquals(100, coords.getWidth()),
                () -> assertEquals(50, coords.getHeight()));
    }

    @ParameterizedTest
    @MethodSource("coordsSource")
    void testCoordsIsMultipliedWithDpr(boolean downscale, Coords expectedCoords)
    {
        MobileAppCoordsProvider coordsDecorator =
            new MobileAppCoordsProvider(downscale, driverManager, uiContext);
        WebElement element = mock(WebElement.class);
        when(element.getLocation()).thenReturn(POINT);
        when(element.getSize()).thenReturn(DIMENSION);
        doReturn(element).when(uiContext).getSearchContext();
        lenient().when(driverManager.getDpr()).thenReturn(2.0);

        WebDriver driver = mock(WebDriver.class);
        assertEquals(expectedCoords, coordsDecorator.ofElement(driver, element));
    }

    static Stream<Arguments> coordsSource()
    {
        return Stream.of(Arguments.of(true, new Coords(10, 10, 1, 1)),
            Arguments.of(false, new Coords(20, 20, 2, 2))
        );
    }
}
