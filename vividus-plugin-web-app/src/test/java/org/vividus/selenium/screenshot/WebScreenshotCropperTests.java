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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.ui.action.ISearchActions;
import org.vividus.ui.action.search.Locator;
import org.vividus.ui.context.IUiContext;

import pazone.ashot.coordinates.Coords;
import pazone.ashot.coordinates.CoordsProvider;

@ExtendWith(MockitoExtension.class)
class WebScreenshotCropperTests
{
    @Mock private Locator elementLocator;
    @Mock private WebDriver webDriver;

    @Mock private ISearchActions searchActions;
    @Mock private CoordsProvider coordsProvider;
    @Mock private IWebDriverProvider webDriverProvider;
    @Mock private IUiContext uiContext;
    @Mock private ScreenshotDebugger screenshotDebugger;
    @InjectMocks private WebScreenshotCropper cropper;

    @Test
    void shouldAdjustElementsToTargetContext()
    {
        when(webDriverProvider.get()).thenReturn(webDriver);
        WebElement element = mock(WebElement.class);
        when(searchActions.findElements(elementLocator)).thenReturn(List.of(element));
        when(coordsProvider.ofElements(webDriver, List.of(element))).thenReturn(Set.of(new Coords(100, 100, 100, 100)));

        WebElement currentContext = mock(WebElement.class);
        when(uiContext.getSearchContext(WebElement.class)).thenReturn(Optional.of(currentContext));
        when(coordsProvider.ofElement(webDriver, currentContext)).thenReturn(new Coords(90, 90, 200, 200));

        BufferedImage image = mock(BufferedImage.class);
        Graphics2D g2 = mock(Graphics2D.class);
        when(image.createGraphics()).thenReturn(g2);

        Coords targetContext = new Coords(95, 95, 200, 200);

        cropper.crop(image, Optional.of(targetContext),
                Map.of(IgnoreStrategy.ELEMENT, Set.of(elementLocator)), 0);

        verify(g2).clearRect(105, 105, 100, 100);
    }

    @Test
    void shouldNotCalculateAdjustmentIfContextCoordsAreEmpty()
    {
        Point adjustment = cropper.calculateAdjustment(Optional.empty(), 42);
        assertEquals(new Point(0, -42), adjustment);
        verifyNoInteractions(webDriverProvider, coordsProvider);
    }
}
