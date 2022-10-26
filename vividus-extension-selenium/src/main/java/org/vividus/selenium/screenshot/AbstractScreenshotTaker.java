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

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.ui.screenshot.ScreenshotParameters;

import pazone.ashot.AShot;
import pazone.ashot.util.ImageTool;

public abstract class AbstractScreenshotTaker<T extends ScreenshotParameters>
        implements ScreenshotTaker, AshotScreenshotTaker<T>
{
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractScreenshotTaker.class);

    private final IWebDriverProvider webDriverProvider;
    private final IScreenshotFileNameGenerator screenshotFileNameGenerator;
    private final AshotFactory<T> ashotFactory;
    private final ScreenshotDebugger screenshotDebugger;

    protected AbstractScreenshotTaker(IWebDriverProvider webDriverProvider,
            IScreenshotFileNameGenerator screenshotFileNameGenerator, AshotFactory<T> ashotFactory,
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
    public Path takeScreenshot(Path screenshotFilePath) throws IOException
    {
        byte[] screenshotData = takeScreenshotAsByteArray();
        if (screenshotData.length > 0)
        {
            Path parent = screenshotFilePath.getParent();
            if (parent != null)
            {
                FileUtils.forceMkdir(parent.toFile());
            }
            Files.write(screenshotFilePath, screenshotData);

            LOGGER.atInfo().addArgument(screenshotFilePath::toAbsolutePath).log("Screenshot was taken: {}");

            return screenshotFilePath;
        }
        return null;
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

    private pazone.ashot.Screenshot takeScreenshot(SearchContext searchContext, AShot aShot)
    {
        pazone.ashot.Screenshot screenshot = searchContext instanceof WebDriver
                ? aShot.takeScreenshot((WebDriver) searchContext)
                : aShot.takeScreenshot(getWebDriverProvider().get(), (WebElement) searchContext);

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

    protected byte[] takeScreenshotAsByteArray()
    {
        return webDriverProvider.getUnwrapped(TakesScreenshot.class).getScreenshotAs(AppiumOutputType.INSTANCE);
    }

    protected IWebDriverProvider getWebDriverProvider()
    {
        return webDriverProvider;
    }
}
