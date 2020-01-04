/*
 * Copyright 2019-2020 the original author or authors.
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

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Dimension;
import org.vividus.selenium.manager.IWebDriverManager;

@ExtendWith(MockitoExtension.class)
class ScreenshotFileNameGeneratorTests
{
    private static final String FILE_DATE_PATTERN = "\\d{4}-\\d{2}-\\d{2}_\\d{2}-\\d{2}-\\d{2}_\\d{3}-";
    private static final String SEPARATOR = "-";

    private static final String SCREENSHOT_NAME = "some_name";
    private static final int HEIGHT = 200;
    private static final int WIDTH = 100;
    private static final String WIDTH_X_HEIGHT_EXTENSION = SEPARATOR + WIDTH + "x" + HEIGHT + ".png";

    @Mock
    private IWebDriverManager webDriverManager;

    @InjectMocks
    private ScreenshotFileNameGenerator screenshotFileNameGenerator;

    @Test
    void testGenerateScreenshotFileNameWithoutBrowserName()
    {
        Dimension dimension = new Dimension(WIDTH, HEIGHT);
        when(webDriverManager.getSize()).thenReturn(dimension);
        String screenshotFileName = screenshotFileNameGenerator.generateScreenshotFileName(SCREENSHOT_NAME);
        assertTrue(screenshotFileName.matches(FILE_DATE_PATTERN + SCREENSHOT_NAME + WIDTH_X_HEIGHT_EXTENSION));
    }

    @Test
    void testGenerateScreenshotFileNameWithBrowserName()
    {
        Capabilities capabilities = mock(Capabilities.class);
        Dimension dimension = new Dimension(WIDTH, HEIGHT);
        when(webDriverManager.getSize()).thenReturn(dimension);
        when(webDriverManager.getCapabilities()).thenReturn(capabilities);
        String browserName = "firefox";
        when(capabilities.getBrowserName()).thenReturn(browserName);
        String screenshotFileName = screenshotFileNameGenerator.generateScreenshotFileName(SCREENSHOT_NAME);
        assertTrue(screenshotFileName
                .matches(FILE_DATE_PATTERN + SCREENSHOT_NAME + SEPARATOR + browserName + WIDTH_X_HEIGHT_EXTENSION));
    }
}
