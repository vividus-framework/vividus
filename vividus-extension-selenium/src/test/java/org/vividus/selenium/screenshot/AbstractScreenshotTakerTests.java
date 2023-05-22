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

import static java.awt.image.BufferedImage.TYPE_INT_ARGB;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.ui.screenshot.ScreenshotParameters;
import org.vividus.util.ResourceUtils;

import pazone.ashot.AShot;

@ExtendWith(MockitoExtension.class)
class AbstractScreenshotTakerTests
{
    private static final String AFTER_A_SHOT = "After_AShot";

    private static final String IMAGE_PNG = "image.png";

    @Mock private IWebDriverProvider webDriverProvider;
    @Mock private TakesScreenshot takesScreenshot;
    @Mock private IScreenshotFileNameGenerator screenshotFileNameGenerator;
    @Mock private ScreenshotDebugger screenshotDebugger;
    @Mock private AshotFactory<ScreenshotParameters> ashotFactory;
    @InjectMocks private TestScreenshotTaker testScreenshotTaker;

    @AfterEach
    void afterEach()
    {
        verifyNoMoreInteractions(webDriverProvider, takesScreenshot);
    }

    @Test
    void shouldTakeViewportScreenshot() throws IOException
    {
        BufferedImage image = testScreenshotTaker.takeViewportScreenshot();

        assertEquals(400, image.getWidth());
        assertEquals(600, image.getHeight());
    }

    @Test
    void shouldGenerateScreenshotFileName()
    {
        String screenshotName = "fileName";
        testScreenshotTaker.generateScreenshotFileName(screenshotName);
        verify(screenshotFileNameGenerator).generateScreenshotFileName(screenshotName);
    }

    @Test
    void shouldSupplyWebDriverProvider()
    {
        assertSame(webDriverProvider, testScreenshotTaker.getWebDriverProvider());
    }

    @Test
    void shouldTakeAshotScreenshot()
    {
        WebDriver webDriver = mock(WebDriver.class, withSettings().extraInterfaces(SearchContext.class));
        AShot ashot = mock(AShot.class);
        when(ashotFactory.create(Optional.empty())).thenReturn(ashot);
        pazone.ashot.Screenshot screenshot = mock(pazone.ashot.Screenshot.class);
        when(ashot.takeScreenshot(webDriver)).thenReturn(screenshot);
        testScreenshotTaker.takeAshotScreenshot(webDriver, Optional.empty());
        verify(ashot).takeScreenshot(webDriver);
        verify(screenshotDebugger).debug(TestScreenshotTaker.class, AFTER_A_SHOT, null);
        verify(screenshot, never()).setImage(any(BufferedImage.class));
    }

    @Test
    void shouldTakeAshotScreenshotWithElementAsTheContext()
    {
        WebElement webElement = mock(WebElement.class, withSettings().extraInterfaces(SearchContext.class));
        WebDriver webDriver = mock(WebDriver.class);
        when(webDriverProvider.get()).thenReturn(webDriver);
        AShot ashot = mock(AShot.class);
        when(ashotFactory.create(Optional.empty())).thenReturn(ashot);
        pazone.ashot.Screenshot screenshot = mock(pazone.ashot.Screenshot.class);
        when(ashot.takeScreenshot(webDriver, webElement)).thenReturn(screenshot);
        testScreenshotTaker.takeAshotScreenshot(webElement, Optional.empty());
        verify(ashot).takeScreenshot(webDriver, webElement);
        verify(screenshotDebugger).debug(TestScreenshotTaker.class, AFTER_A_SHOT, null);
        verify(screenshot, never()).setImage(any(BufferedImage.class));
    }

    @ParameterizedTest
    @CsvSource({ "10, 0, 0, 0, 100, 90",
                 "0, 10, 0, 0, 100, 90",
                 "0, 0, 10, 0, 90, 100",
                 "0, 0, 0, 10, 90, 100",
                 "10, 20, 30, 40, 30, 70"
    })
    void shouldCutScreenshot(int cutTop, int cutBottom, int cutLeft, int cutRight, int expectedWidth,
            int expectedHeight)
    {
        var ashot = mock(AShot.class);
        var screenshotParameters = new ScreenshotParameters();
        screenshotParameters.setCutTop(cutTop);
        screenshotParameters.setCutBottom(cutBottom);
        screenshotParameters.setCutLeft(cutLeft);
        screenshotParameters.setCutRight(cutRight);
        when(ashotFactory.create(Optional.of(screenshotParameters))).thenReturn(ashot);
        var image = new BufferedImage(100, 100, TYPE_INT_ARGB);
        var screenshot = new pazone.ashot.Screenshot(image);
        var webDriver = mock(WebDriver.class, withSettings().extraInterfaces(SearchContext.class));
        when(ashot.takeScreenshot(webDriver)).thenReturn(screenshot);
        testScreenshotTaker.takeAshotScreenshot(webDriver, Optional.of(screenshotParameters));
        verify(ashot).takeScreenshot(webDriver);
        verify(screenshotDebugger).debug(TestScreenshotTaker.class, AFTER_A_SHOT, image);
        var actualImage = screenshot.getImage();
        assertEquals(expectedWidth, actualImage.getWidth());
        assertEquals(expectedHeight, actualImage.getHeight());
    }

    @Test
    void shouldNotCutScreenshotWhenCutsIsNotSet()
    {
        var webDriver = mock(WebDriver.class, withSettings().extraInterfaces(SearchContext.class));
        var ashot = mock(AShot.class);
        var screenshotParameters = Optional.of(new ScreenshotParameters());
        when(ashotFactory.create(screenshotParameters)).thenReturn(ashot);
        var screenshot = mock(pazone.ashot.Screenshot.class);
        when(ashot.takeScreenshot(webDriver)).thenReturn(screenshot);
        testScreenshotTaker.takeAshotScreenshot(webDriver, screenshotParameters);
        verify(ashot).takeScreenshot(webDriver);
        verify(screenshotDebugger).debug(TestScreenshotTaker.class, AFTER_A_SHOT, null);
        verify(screenshot, never()).setImage(any(BufferedImage.class));
    }

    private static class TestScreenshotTaker extends AbstractScreenshotTaker<ScreenshotParameters>
    {
        TestScreenshotTaker(IWebDriverProvider webDriverProvider,
                IScreenshotFileNameGenerator screenshotFileNameGenerator,
                AshotFactory<ScreenshotParameters> ashotFactory, ScreenshotDebugger screenshotDebugger)
        {
            super(webDriverProvider, screenshotFileNameGenerator, ashotFactory, screenshotDebugger);
        }

        @Override
        public Optional<Screenshot> takeScreenshot(String screenshotName)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public byte[] takeScreenshotAsByteArray()
        {
            return ResourceUtils.loadResourceAsByteArray(getClass(), IMAGE_PNG);
        }
    }
}
