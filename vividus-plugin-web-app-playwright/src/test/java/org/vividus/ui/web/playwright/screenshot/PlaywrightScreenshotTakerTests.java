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

import static com.github.valfirst.slf4jtest.LoggingEvent.info;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;
import com.microsoft.playwright.Page;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.ui.screenshot.Screenshot;
import org.vividus.ui.screenshot.ScreenshotFileNameGenerator;
import org.vividus.ui.web.playwright.BrowserContextProvider;
import org.vividus.ui.web.playwright.UiContext;

@ExtendWith({ MockitoExtension.class, TestLoggerFactoryExtension.class })
class PlaywrightScreenshotTakerTests
{
    private static final byte[] SCREENSHOT_DATA = new byte[1];
    private static final String SCREENSHOT_NAME = "some_name";
    private static final String SCREENSHOT_NAME_GENERATED = "some_name_generated";

    @Mock private BrowserContextProvider browserContextProvider;
    @Mock private UiContext uiContext;
    @Mock private ScreenshotFileNameGenerator nameGenerator;

    @InjectMocks private PlaywrightScreenshotTaker screenshotTaker;

    private final TestLogger logger = TestLoggerFactory.getTestLogger(PlaywrightScreenshotTaker.class);

    @Test
    void shouldTakeScreenshotAsByteArray()
    {
        mockScreenshotAsByteArray();

        assertArrayEquals(SCREENSHOT_DATA, screenshotTaker.takeScreenshotAsByteArray());
    }

    @Test
    void shouldNotTakeScreenshotIfBrowserContextNotInitialized()
    {
        when(browserContextProvider.isBrowserContextInitialized()).thenReturn(false);

        byte[] actualScreenshotData = screenshotTaker.takeScreenshotAsByteArray();
        assertArrayEquals(new byte[0], actualScreenshotData);
        assertThat(logger.getLoggingEvents(), is(List.of(info("Playwright browser context not initialized."))));
    }

    @Test
    void shouldTakeScreenshot()
    {
        mockScreenshotAsByteArray();
        when(nameGenerator.generateScreenshotFileName(SCREENSHOT_NAME)).thenReturn(SCREENSHOT_NAME_GENERATED);

        Screenshot screenshot = screenshotTaker.takeScreenshot(SCREENSHOT_NAME).get();
        assertEquals(SCREENSHOT_NAME_GENERATED, screenshot.getFileName());
        assertArrayEquals(SCREENSHOT_DATA, screenshot.getData());
    }

    @Test
    void shouldNotCreateScreenshotIfScreenshotDataEmpty()
    {
        when(browserContextProvider.isBrowserContextInitialized()).thenReturn(false);

        assertEquals(Optional.empty(), screenshotTaker.takeScreenshot(SCREENSHOT_NAME));
    }

    @Test
    void shouldNotImplementedTakeViewportScreenshot()
    {
        assertThrows(UnsupportedOperationException.class, screenshotTaker::takeViewportScreenshot);
    }

    private void mockScreenshotAsByteArray()
    {
        when(browserContextProvider.isBrowserContextInitialized()).thenReturn(true);
        Page page = mock();
        when(uiContext.getCurrentPage()).thenReturn(page);
        when(page.screenshot(argThat(o -> o.fullPage))).thenReturn(SCREENSHOT_DATA);
    }
}
