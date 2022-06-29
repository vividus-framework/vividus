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

package org.vividus.selenium.mobileapp;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebDriver;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.mobileapp.screenshot.MobileAppAshotFactory;
import org.vividus.selenium.screenshot.IScreenshotFileNameGenerator;
import org.vividus.selenium.screenshot.Screenshot;
import org.vividus.selenium.screenshot.ScreenshotUtils;

@ExtendWith(MockitoExtension.class)
class MobileAppScreenshotTakerTests
{
    private static final String FILE_NAME = "file-name";
    private static final String SCREENSHOT_NAME = "screenshot-name";
    private static final byte[] DATA = { 1, 0, 1};

    @Mock private IWebDriverProvider webDriverProvider;
    @Mock private IScreenshotFileNameGenerator screenshotFileNameGenerator;
    @Mock private MobileAppAshotFactory ashotFactory;
    @Mock private WebDriver webDriver;
    @InjectMocks private MobileAppScreenshotTaker screenshotTaker;

    @BeforeEach
    void beforeEach()
    {
        when(webDriverProvider.get()).thenReturn(webDriver);
    }

    @Test
    void shouldTakeScreenshot()
    {
        try (MockedStatic<ScreenshotUtils> utils = mockStatic(ScreenshotUtils.class))
        {
            utils.when(() -> ScreenshotUtils.takeViewportScreenshotAsByteArray(webDriver)).thenReturn(DATA);

            when(screenshotFileNameGenerator.generateScreenshotFileName(SCREENSHOT_NAME)).thenReturn(FILE_NAME);

            Optional<Screenshot> takenScreenshot = screenshotTaker.takeScreenshot(SCREENSHOT_NAME);
            assertTrue(takenScreenshot.isPresent());
            Screenshot screenshot = takenScreenshot.get();
            assertEquals(FILE_NAME, screenshot.getFileName());
            assertArrayEquals(DATA, screenshot.getData());
            verifyNoMoreInteractions(screenshotFileNameGenerator, webDriverProvider);
        }
    }

    @Test
    void shouldSaveScreenshotToAPath(@TempDir Path path) throws IOException
    {
        try (MockedStatic<ScreenshotUtils> utils = mockStatic(ScreenshotUtils.class))
        {
            utils.when(() -> ScreenshotUtils.takeViewportScreenshotAsByteArray(webDriver)).thenReturn(DATA);

            Path takenScreenshot = screenshotTaker.takeScreenshot(path.resolve(FILE_NAME));
            assertArrayEquals(DATA, Files.readAllBytes(takenScreenshot));
            verifyNoMoreInteractions(screenshotFileNameGenerator, webDriverProvider);
        }
    }
}
