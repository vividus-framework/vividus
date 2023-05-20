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

package org.vividus.selenium.screenshot;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.ui.web.screenshot.WebScreenshotParameters;
import org.vividus.util.ResourceUtils;

import pazone.ashot.AShot;
import pazone.ashot.util.ImageTool;

@ExtendWith(MockitoExtension.class)
class WebScreenshotTakerTests
{
    private static final BufferedImage IMAGE = loadImage();
    private static final AShot ASHOT = mock(AShot.class);
    private static final String SCREENSHOT_NAME = "screenshotName";
    private static final String SCREENSHOT_NAME_GENERATED = SCREENSHOT_NAME + "Generated";
    private static final pazone.ashot.Screenshot SCREENSHOT = new pazone.ashot.Screenshot(IMAGE);

    @Mock private IWebDriverProvider webDriverProvider;
    @Mock private IScreenshotFileNameGenerator screenshotFileNameGenerator;
    @Mock private IWebElementHighlighter webElementHighlighter;
    @Mock private AshotFactory<WebScreenshotParameters> ashotFactory;
    @Mock private ScreenshotDebugger screenshotDebugger;
    @Mock(extraInterfaces = JavascriptExecutor.class)
    private WebDriver webDriver;

    @InjectMocks private WebScreenshotTaker screenshotTaker;

    @BeforeEach
    void beforeEach()
    {
        screenshotTaker.setHighlighterType(HighlighterType.DEFAULT);
        screenshotTaker.setIndent(1);
    }

    @SuppressWarnings("unchecked")
    private void mockTakeScreenshotWithHighlights()
    {
        when(webElementHighlighter.takeScreenshotWithHighlights(any(Supplier.class)))
                .thenAnswer(invocation -> ((Supplier<?>) invocation.getArguments()[0]).get());
    }

    private static BufferedImage loadImage()
    {
        try
        {
            return ImageIO.read(ResourceUtils.loadFile(WebScreenshotTakerTests.class, "screenshot.png"));
        }
        catch (IOException e)
        {
            throw new UncheckedIOException(e);
        }
    }

    @Test
    void testTakeScreenshotWebElementsDefaultHighlighter() throws IOException
    {
        when(webDriverProvider.isWebDriverInitialized()).thenReturn(true);
        when(webDriverProvider.get()).thenReturn(webDriver);
        when(screenshotFileNameGenerator.generateScreenshotFileName(SCREENSHOT_NAME))
                .thenReturn(SCREENSHOT_NAME_GENERATED);
        when(ashotFactory.create(Optional.empty())).thenReturn(ASHOT);
        mockTakeScreenshotWithHighlights();
        List<WebElement> webElements = List.of(mock(WebElement.class));
        when(ASHOT.takeScreenshot(webDriver, webElements)).thenReturn(SCREENSHOT);
        Optional<Screenshot> screen = screenshotTaker.takeScreenshot(SCREENSHOT_NAME, webElements);
        assertScreenshotWithHighlighter(screen);
    }

    @Test
    void testTakeScreenshotWhenWebDriverIsNotInitialized()
    {
        when(webDriverProvider.isWebDriverInitialized()).thenReturn(Boolean.FALSE);
        Optional<Screenshot> screen = screenshotTaker.takeScreenshot(SCREENSHOT_NAME);
        verifyNoInteractions(webElementHighlighter);
        assertEquals(Optional.empty(), screen);
    }

    @Test
    void testTakeScreenshotWebElementsBlurHighlighter() throws IOException
    {
        when(webDriverProvider.isWebDriverInitialized()).thenReturn(true);
        when(webDriverProvider.get()).thenReturn(webDriver);
        when(screenshotFileNameGenerator.generateScreenshotFileName(SCREENSHOT_NAME))
                .thenReturn(SCREENSHOT_NAME_GENERATED);
        when(ashotFactory.create(Optional.empty())).thenReturn(ASHOT);
        screenshotTaker.setHighlighterType(HighlighterType.BLUR);
        List<WebElement> webElements = List.of(mock(WebElement.class));
        when(ASHOT.takeScreenshot(webDriver, webElements)).thenReturn(SCREENSHOT);
        Optional<Screenshot> screen = screenshotTaker.takeScreenshot(SCREENSHOT_NAME, webElements);
        assertScreenshotWithHighlighter(screen);
    }

    @Test
    void testTakeScreenshotEmptyWebElements() throws IOException
    {
        when(webDriverProvider.isWebDriverInitialized()).thenReturn(true);
        when(webDriverProvider.get()).thenReturn(webDriver);
        when(screenshotFileNameGenerator.generateScreenshotFileName(SCREENSHOT_NAME))
                .thenReturn(SCREENSHOT_NAME_GENERATED);
        when(ashotFactory.create(Optional.empty())).thenReturn(ASHOT);
        mockTakeScreenshotWithHighlights();
        when(ASHOT.takeScreenshot(webDriver)).thenReturn(SCREENSHOT);
        Optional<Screenshot> screen = screenshotTaker.takeScreenshot(SCREENSHOT_NAME, List.of());
        assertScreenshotWithHighlighter(screen);
    }

    @Test
    void testTakeScreenshotWebElementsFullPage() throws IOException
    {
        when(webDriverProvider.get()).thenReturn(webDriver);
        when(webDriverProvider.isWebDriverInitialized()).thenReturn(true);
        when(screenshotFileNameGenerator.generateScreenshotFileName(SCREENSHOT_NAME))
                .thenReturn(SCREENSHOT_NAME_GENERATED);
        when(ashotFactory.create(Optional.empty())).thenReturn(ASHOT);
        screenshotTaker.setFullPageScreenshots(true);
        mockTakeScreenshotWithHighlights();
        List<WebElement> webElements = Collections.singletonList(mock(WebElement.class));
        when(ASHOT.takeScreenshot(webDriver)).thenReturn(SCREENSHOT);
        Optional<Screenshot> screen = screenshotTaker.takeScreenshot(SCREENSHOT_NAME, webElements);
        assertScreenshotWithHighlighter(screen);
    }

    @Test
    void testTakeScreenshot() throws IOException
    {
        when(webDriverProvider.isWebDriverInitialized()).thenReturn(true);
        when(webDriverProvider.get()).thenReturn(webDriver);
        when(screenshotFileNameGenerator.generateScreenshotFileName(SCREENSHOT_NAME))
                .thenReturn(SCREENSHOT_NAME_GENERATED);
        when(ashotFactory.create(Optional.empty())).thenReturn(ASHOT);
        screenshotTaker.setHighlighterType(HighlighterType.MONOCHROME);
        when(ASHOT.takeScreenshot(webDriver)).thenReturn(SCREENSHOT);
        Optional<Screenshot> screen = screenshotTaker.takeScreenshot(SCREENSHOT_NAME);
        assertScreenshotWithHighlighter(screen);
    }

    @Test
    void shouldTakeAShotScreenshotWithCustomConfiguration() throws IOException
    {
        WebScreenshotParameters parametersMock = mock(WebScreenshotParameters.class);
        Optional<WebScreenshotParameters> screenshotParameters = Optional.of(parametersMock);
        when(ashotFactory.create(Optional.of(parametersMock))).thenReturn(ASHOT);
        when(ASHOT.takeScreenshot(webDriver)).thenReturn(SCREENSHOT);
        assertArrayEquals(ImageTool.toByteArray(SCREENSHOT),
                ImageTool.toByteArray(screenshotTaker.takeAshotScreenshot(webDriver,
                        screenshotParameters)));
        verify(ASHOT).takeScreenshot(webDriver);
        verify(screenshotDebugger).debug(WebScreenshotTaker.class, "After_AShot", IMAGE);
    }

    private void assertScreenshotWithHighlighter(Optional<Screenshot> actualScreenshot) throws IOException
    {
        assertEquals(SCREENSHOT_NAME_GENERATED, actualScreenshot.get().getFileName());
        assertNotEquals(Optional.empty(), actualScreenshot);
        assertArrayEquals(ImageTool.toByteArray(SCREENSHOT), actualScreenshot.get().getData());
    }
}
