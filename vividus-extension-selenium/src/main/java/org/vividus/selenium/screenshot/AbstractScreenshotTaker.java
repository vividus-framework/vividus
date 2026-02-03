/*
 * Copyright 2019-2026 the original author or authors.
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

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Optional;

import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.WebDriverUtils;
import org.vividus.ui.screenshot.ScreenshotFileNameGenerator;
import org.vividus.ui.screenshot.ScreenshotParameters;
import org.vividus.ui.screenshot.ScreenshotTaker;

import pazone.ashot.AShot;
import pazone.ashot.util.ImageTool;

public abstract class AbstractScreenshotTaker<T extends ScreenshotParameters>
        implements ScreenshotTaker, AshotScreenshotTaker<T>
{
    private final IWebDriverProvider webDriverProvider;
    private final ScreenshotFileNameGenerator screenshotFileNameGenerator;
    private final AshotFactory<T> ashotFactory;
    private final ScreenshotDebugger screenshotDebugger;

    protected AbstractScreenshotTaker(IWebDriverProvider webDriverProvider,
            ScreenshotFileNameGenerator screenshotFileNameGenerator, AshotFactory<T> ashotFactory,
            ScreenshotDebugger screenshotDebugger)
    {
        this.webDriverProvider = webDriverProvider;
        this.screenshotFileNameGenerator = screenshotFileNameGenerator;
        this.ashotFactory = ashotFactory;
        this.screenshotDebugger = screenshotDebugger;
    }

    @Override
    public BufferedImage takeViewportScreenshot() throws IOException
    {
        return ImageTool.toBufferedImage(takeScreenshotAsByteArray());
    }

    @Override
    public pazone.ashot.Screenshot takeAshotScreenshot(SearchContext searchContext, Optional<T> screenshotParameters)
    {
        pazone.ashot.Screenshot screenshot = takeScreenshot(searchContext, crateAshot(screenshotParameters));
        screenshotParameters.filter(p -> p.getCutBottom() > 0
                                      || p.getCutTop() > 0
                                      || p.getCutLeft() > 0
                                      || p.getCutRight() > 0)
                            .ifPresent(p -> {
                                BufferedImage bufferedImage = screenshot.getImage();
                                screenshot.setImage(bufferedImage.getSubimage(p.getCutLeft(), p.getCutTop(),
                                        bufferedImage.getWidth() - p.getCutLeft() - p.getCutRight(),
                                        bufferedImage.getHeight() - p.getCutTop() - p.getCutBottom()));
                            });
        return screenshot;
    }

    @SuppressWarnings("PMD.GuardLogStatement")
    private pazone.ashot.Screenshot takeScreenshot(SearchContext searchContext, AShot aShot)
    {
        pazone.ashot.Screenshot screenshot = searchContext instanceof WebDriver webDriver
                ? aShot.takeScreenshot(WebDriverUtils.unwrap(webDriver, RemoteWebDriver.class))
                : aShot.takeScreenshot(WebDriverUtils.unwrap(getWebDriverProvider().get(), RemoteWebDriver.class),
                        (WebElement) searchContext);

        screenshotDebugger.debug(this.getClass(), "After_AShot", screenshot.getImage());
        return screenshot;
    }

    protected AShot crateAshot(Optional<T> screenshotConfiguration)
    {
        return ashotFactory.create(screenshotConfiguration);
    }

    protected String generateScreenshotFileName(String screenshotName)
    {
        return screenshotFileNameGenerator.generateScreenshotFileName(screenshotName);
    }

    protected IWebDriverProvider getWebDriverProvider()
    {
        return webDriverProvider;
    }
}
