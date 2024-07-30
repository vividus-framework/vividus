/*
 * Copyright 2019-2024 the original author or authors.
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

package org.vividus.ui.web.playwright.screenshot;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.ViewportSize;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.ui.web.playwright.BrowserContextProvider;
import org.vividus.ui.web.playwright.UiContext;

@ExtendWith(MockitoExtension.class)
class PlaywrightScreenshotFileNameGeneratorTests
{
    private static final String FILE_DATE_PATTERN = "\\d{4}-\\d{2}-\\d{2}_\\d{2}-\\d{2}-\\d{2}_\\d{3}-";
    private static final String SEPARATOR = "-";

    private static final String BROWSER_NAME = "chromium";
    private static final String SCREENSHOT_NAME = "some_name";
    private static final int HEIGHT = 200;
    private static final int WIDTH = 100;
    private static final String WIDTH_X_HEIGHT_EXTENSION = SEPARATOR + WIDTH + "x" + HEIGHT + ".png";

    @Mock private BrowserContextProvider browserContextProvider;
    @Mock private UiContext uiContext;

    @Test
    void testGenerateScreenshotFileName()
    {
        PlaywrightScreenshotFileNameGenerator nameGenerator = new PlaywrightScreenshotFileNameGenerator(
                browserContextProvider, uiContext);
        Page page = mock();
        ViewportSize viewportSize = new ViewportSize(WIDTH, HEIGHT);
        when(uiContext.getCurrentPage()).thenReturn(page);
        when(page.viewportSize()).thenReturn(viewportSize);

        BrowserContext browserContext = mock();
        Browser browser = mock();
        BrowserType browserType = mock();
        when(browserContextProvider.get()).thenReturn(browserContext);
        when(browserContext.browser()).thenReturn(browser);
        when(browser.browserType()).thenReturn(browserType);
        when(browserType.name()).thenReturn(BROWSER_NAME);

        String screenshotFileName = nameGenerator.generateScreenshotFileName(SCREENSHOT_NAME);
        assertTrue(screenshotFileName
                .matches(FILE_DATE_PATTERN + SCREENSHOT_NAME + SEPARATOR + BROWSER_NAME + WIDTH_X_HEIGHT_EXTENSION));
    }
}
