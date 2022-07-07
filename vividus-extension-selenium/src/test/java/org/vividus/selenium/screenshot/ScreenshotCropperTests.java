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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.imageio.ImageIO;

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
import org.vividus.util.ResourceUtils;

import pazone.ashot.coordinates.Coords;
import pazone.ashot.coordinates.CoordsProvider;
import pazone.ashot.util.ImageTool;

@ExtendWith(MockitoExtension.class)
class ScreenshotCropperTests
{
    private static final String ORIGINAL = "original";

    @Mock private Locator emptyLocator;
    @Mock private Locator elementLocator;
    @Mock private Locator areaLocator;
    @Mock private WebDriver webDriver;

    @Mock private ScreenshotDebugger screenshotDebugger;
    @Mock private ISearchActions searchActions;
    @Mock private CoordsProvider coordsProvider;
    @Mock private IWebDriverProvider webDriverProvider;
    @InjectMocks private ScreenshotCropper cropper;

    @Test
    void shouldCropElementsFromImage() throws IOException
    {
        when(webDriverProvider.get()).thenReturn(webDriver);
        WebElement element = mock(WebElement.class);
        when(searchActions.findElements(elementLocator)).thenReturn(List.of(element));
        when(searchActions.findElements(emptyLocator)).thenReturn(List.of());
        when(coordsProvider.ofElements(webDriver, List.of(element))).thenReturn(Set.of(new Coords(704, 89, 272, 201)));
        WebElement area = mock(WebElement.class);
        when(searchActions.findElements(areaLocator)).thenReturn(List.of(area));
        when(coordsProvider.ofElements(webDriver, List.of(area))).thenReturn(Set.of(new Coords(270, 311, 1139, 52)));

        BufferedImage originalImage = loadImage(ORIGINAL);
        BufferedImage actual = cropper.crop(originalImage, Optional.empty(), Map.of(
            IgnoreStrategy.ELEMENT, Set.of(elementLocator, emptyLocator),
            IgnoreStrategy.AREA, Set.of(areaLocator)
        ), 0);

        verifyScreenshot(actual);
    }

    @Test
    void shouldCropNothingIfNoElementsWereFound() throws IOException
    {
        when(webDriverProvider.get()).thenReturn(webDriver);
        when(searchActions.findElements(emptyLocator)).thenReturn(List.of());

        BufferedImage originalImage = loadImage(ORIGINAL);
        BufferedImage actual = cropper.crop(originalImage, Optional.empty(), Map.of(
            IgnoreStrategy.ELEMENT, Set.of(emptyLocator)
        ), 0);

        assertThat(actual, ImageTool.equalImage(originalImage));
    }

    @Test
    void shouldReturnCoordsProvider()
    {
        assertEquals(coordsProvider, cropper.getCoordsProvider());
    }

    @Test
    void shouldReturnWebDriverProvider()
    {
        assertEquals(webDriverProvider, cropper.getWebDriverProvider());
    }

    private void verifyScreenshot(BufferedImage actual) throws IOException
    {
        BufferedImage afterCropping = loadImage("after_cropping");
        assertThat(actual, ImageTool.equalImage(afterCropping));
        verify(searchActions).findElements(emptyLocator);
        verify(searchActions).findElements(elementLocator);
        verify(searchActions).findElements(areaLocator);
        BufferedImage elementCropped = loadImage("element_cropped");
        verify(screenshotDebugger).debug(eq(ScreenshotCropper.class), eq("cropped_by_ELEMENT"),
                equalTo(elementCropped));
        verify(screenshotDebugger).debug(eq(ScreenshotCropper.class), eq("cropped_by_AREA"),
                equalTo(afterCropping));
    }

    private BufferedImage loadImage(String fileName) throws IOException
    {
        return ImageIO.read(ResourceUtils.loadFile(ScreenshotCropperTests.class, fileName + ".png"));
    }

    private BufferedImage equalTo(BufferedImage expected)
    {
        return argThat(actual -> ImageTool.equalImage(expected).matches(actual));
    }
}
