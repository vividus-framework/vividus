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

import java.awt.image.BufferedImage;
import java.util.Optional;

import com.microsoft.playwright.Page.ScreenshotOptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.ui.screenshot.Screenshot;
import org.vividus.ui.screenshot.ScreenshotFileNameGenerator;
import org.vividus.ui.screenshot.ScreenshotTaker;
import org.vividus.ui.web.playwright.BrowserContextProvider;
import org.vividus.ui.web.playwright.UiContext;

public class PlaywrightScreenshotTaker implements ScreenshotTaker
{
    private static final Logger LOGGER = LoggerFactory.getLogger(PlaywrightScreenshotTaker.class);

    private final BrowserContextProvider browserContextProvider;
    private final UiContext uiContext;
    private final ScreenshotFileNameGenerator nameGenerator;

    public PlaywrightScreenshotTaker(BrowserContextProvider browserContextProvider, UiContext uiContext,
            ScreenshotFileNameGenerator nameGenerator)
    {
        this.browserContextProvider = browserContextProvider;
        this.uiContext = uiContext;
        this.nameGenerator = nameGenerator;
    }

    @Override
    public Optional<Screenshot> takeScreenshot(String screenshotName)
    {
        byte[] screenshotData = takeScreenshotAsByteArray();

        return screenshotData.length > 0
                ? Optional.of(new Screenshot(nameGenerator.generateScreenshotFileName(screenshotName), screenshotData))
                : Optional.empty();
    }

    @Override
    public BufferedImage takeViewportScreenshot()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public byte[] takeScreenshotAsByteArray()
    {
        if (browserContextProvider.isBrowserContextInitialized())
        {
            ScreenshotOptions options = new ScreenshotOptions();
            options.setFullPage(true);
            return uiContext.getCurrentPage().screenshot(options);
        }
        LOGGER.info("Playwright browser context not initialized.");
        return new byte[0];
    }
}
