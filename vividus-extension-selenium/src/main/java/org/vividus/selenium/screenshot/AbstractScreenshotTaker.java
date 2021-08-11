/*
 * Copyright 2019-2021 the original author or authors.
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
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Supplier;

import javax.imageio.ImageIO;

import com.google.common.eventbus.EventBus;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.selenium.IWebDriverProvider;

import ru.yandex.qatools.ashot.AShot;

public abstract class AbstractScreenshotTaker implements ScreenshotTaker
{
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractScreenshotTaker.class);

    private final IWebDriverProvider webDriverProvider;
    private final EventBus eventBus;
    private final IScreenshotFileNameGenerator screenshotFileNameGenerator;
    private final ScreenshotDebugger screenshotDebugger;

    private File screenshotDirectory;

    protected AbstractScreenshotTaker(IWebDriverProvider webDriverProvider, EventBus eventBus,
            IScreenshotFileNameGenerator screenshotFileNameGenerator, ScreenshotDebugger screenshotDebugger)
    {
        this.webDriverProvider = webDriverProvider;
        this.eventBus = eventBus;
        this.screenshotFileNameGenerator = screenshotFileNameGenerator;
        this.screenshotDebugger = screenshotDebugger;
    }

    @Override
    public BufferedImage takeViewportScreenshot() throws IOException
    {
        try (InputStream inputStream = new ByteArrayInputStream(getScreenshotBytes()))
        {
            return ImageIO.read(inputStream);
        }
    }

    @Override
    public ru.yandex.qatools.ashot.Screenshot takeAshotScreenshot(SearchContext searchContext,
            Optional<ScreenshotConfiguration> screenshotConfiguration)
    {
        return takeScreenshot(searchContext, crateAshot(screenshotConfiguration));
    }

    private ru.yandex.qatools.ashot.Screenshot takeScreenshot(SearchContext searchContext, AShot aShot)
    {
        ru.yandex.qatools.ashot.Screenshot screenshot = searchContext instanceof WebDriver
                ? aShot.takeScreenshot((WebDriver) searchContext)
                : aShot.takeScreenshot(getWebDriverProvider().get(), (WebElement) searchContext);

        screenshotDebugger.debug(this.getClass(), "After_AShot", screenshot.getImage());
        return screenshot;
    }

    protected abstract AShot crateAshot(Optional<ScreenshotConfiguration> screenshotConfiguration);

    protected Path takeScreenshotAsFile(Path pathProvider, Supplier<byte[]> screenshotTaker) throws IOException
    {
        return takeScreenshot(pathProvider, screenshotTaker);
    }

    protected String generateScreenshotFileName(String screenshotName)
    {
        return screenshotFileNameGenerator.generateScreenshotFileName(screenshotName);
    }

    protected Path generateScreenshotPath(String screenshotName)
    {
        return new File(screenshotDirectory, generateScreenshotFileName(screenshotName)).toPath();
    }

    private Path takeScreenshot(Path screenshotFilePath,
            Supplier<byte[]> screenshotTaker) throws IOException
    {
        byte[] screenshotData = screenshotTaker.get();
        if (screenshotData.length > 0)
        {
            Path parent = screenshotFilePath.getParent();
            if (parent != null)
            {
                FileUtils.forceMkdir(parent.toFile());
            }
            Files.write(screenshotFilePath, screenshotData);

            LOGGER.atInfo().addArgument(screenshotFilePath::toAbsolutePath).log("Screenshot was taken: {}");

            eventBus.post(new ScreenshotTakeEvent(screenshotFilePath));
            return screenshotFilePath;
        }
        return null;
    }

    protected byte[] getScreenshotBytes()
    {
        return webDriverProvider.getUnwrapped(TakesScreenshot.class).getScreenshotAs(OutputType.BYTES);
    }

    protected IWebDriverProvider getWebDriverProvider()
    {
        return webDriverProvider;
    }

    public void setScreenshotDirectory(File screenshotDirectory)
    {
        this.screenshotDirectory = screenshotDirectory;
    }
}
