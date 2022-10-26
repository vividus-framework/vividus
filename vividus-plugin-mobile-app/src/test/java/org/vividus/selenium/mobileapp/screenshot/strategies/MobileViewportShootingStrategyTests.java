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

package org.vividus.selenium.mobileapp.screenshot.strategies;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.vividus.util.ResourceUtils;

import pazone.ashot.ImageReadException;
import pazone.ashot.util.ImageBytesDiffer;

class MobileViewportShootingStrategyTests
{
    private static final String MOBILE_VIEWPORT_SCREENSHOT = "mobile:viewportScreenshot";

    private final MobileViewportShootingStrategy strategy = new MobileViewportShootingStrategy();

    @Test
    void shouldTakeScreenshot() throws IOException
    {
        var imageFile = ResourceUtils.loadFile(this.getClass(), "/io/appium/java_client/black.png");
        var imageBytes = Files.readAllBytes(imageFile.toPath());
        var webDriver = mock(WebDriver.class, withSettings().extraInterfaces(JavascriptExecutor.class));
        when(((JavascriptExecutor) webDriver).executeScript(MOBILE_VIEWPORT_SCREENSHOT)).thenReturn(
                Base64.getMimeEncoder().encodeToString(imageBytes));
        var actual = strategy.getScreenshot(webDriver);
        assertTrue(ImageBytesDiffer.areImagesEqual(ImageIO.read(imageFile), actual));
    }

    @Test
    void shouldFailToParseBrokenScreenshot()
    {
        var imageBytes = new byte[] { 0 };
        var webDriver = mock(WebDriver.class, withSettings().extraInterfaces(JavascriptExecutor.class));
        when(((JavascriptExecutor) webDriver).executeScript(MOBILE_VIEWPORT_SCREENSHOT)).thenReturn(
                Base64.getMimeEncoder().encodeToString(imageBytes));
        try (var imageIOStaticMock = mockStatic(ImageIO.class))
        {
            var ioException = new IOException();
            imageIOStaticMock.when(() -> ImageIO.read(any(ByteArrayInputStream.class))).thenThrow(ioException);
            var exception = assertThrows(ImageReadException.class, () -> strategy.getScreenshot(webDriver));
            assertEquals("Can not parse screenshot data", exception.getMessage());
            assertEquals(ioException, exception.getCause());
        }
    }
}
