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

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import com.google.common.eventbus.EventBus;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.selenium.IWebDriverProvider;

import ru.yandex.qatools.ashot.AShot;
import ru.yandex.qatools.ashot.cropper.indent.IndentCropper;
import ru.yandex.qatools.ashot.util.ImageTool;

public class WebScreenshotTaker extends AbstractScreenshotTaker
{
    private static final Logger LOGGER = LoggerFactory.getLogger(WebScreenshotTaker.class);

    private final IWebElementHighlighter webElementHighlighter;
    private final IAshotFactory<WebScreenshotConfiguration> ashotFactory;

    private boolean fullPageScreenshots;
    private int indent;
    private HighlighterType highlighterType;

    public WebScreenshotTaker(IWebDriverProvider webDriverProvider,
            IScreenshotFileNameGenerator screenshotFileNameGenerator, IWebElementHighlighter webElementHighlighter,
            EventBus eventBus, IAshotFactory<WebScreenshotConfiguration> ashotFactory,
            ScreenshotDebugger screenshotDebugger)
    {
        super(webDriverProvider, eventBus, screenshotFileNameGenerator, screenshotDebugger);
        this.webElementHighlighter = webElementHighlighter;
        this.ashotFactory = ashotFactory;
    }

    @Override
    public Optional<Screenshot> takeScreenshot(String screenshotName)
    {
        return takeScreenshot(screenshotName, List.of());
    }

    @Override
    public Path takeScreenshotAsFile(String screenshotName) throws IOException
    {
        return takeScreenshotTo(generateScreenshotPath(screenshotName));
    }

    @Override
    public Path takeScreenshot(Path screenshotFilePath) throws IOException
    {
        return takeScreenshotTo(screenshotFilePath);
    }

    @Override
    protected AShot crateAshot(Optional<ScreenshotConfiguration> screenshotConfiguration)
    {
        return ashotFactory.create(screenshotConfiguration.map(WebScreenshotConfiguration.class::cast));
    }

    public Optional<Screenshot> takeScreenshot(String screenshotName, List<WebElement> webElementsToHighlight)
    {
        byte[] screenshotData = takeScreenshotAsByteArray(webElementsToHighlight);
        return createScreenshot(screenshotData, screenshotName);
    }

    private Optional<Screenshot> createScreenshot(byte[] screenshotData, String screenshotName)
    {
        return screenshotData.length > 0
                ? Optional.of(new Screenshot(generateScreenshotFileName(screenshotName), screenshotData))
                : Optional.empty();
    }

    private Path takeScreenshotTo(Path screenshotFilePathSupplier) throws IOException
    {
        return takeScreenshotAsFile(screenshotFilePathSupplier,
                () -> takeScreenshotAsByteArray(List.of()));
    }

    private byte[] takeScreenshotAsByteArray(List<WebElement> webElements)
    {
        if (getWebDriverProvider().isWebDriverInitialized())
        {
            byte[] screenshotData;
            if (HighlighterType.DEFAULT == highlighterType)
            {
                screenshotData = webElementHighlighter.takeScreenshotWithHighlights(
                        () -> takeScreenshotAsByteArrayImpl(webElements));
            }
            else
            {
                screenshotData = takeScreenshotAsByteArrayImpl(webElements);
            }
            return screenshotData;
        }
        LOGGER.info("WebDriver is not initialized");
        return new byte[0];
    }

    private byte[] takeScreenshotAsByteArrayImpl(List<WebElement> webElements)
    {
        WebDriver webDriver = getWebDriverProvider().get();
        try
        {
            AShot aShot = ashotFactory.create(Optional.empty());
            IndentCropper indentCropper = new IndentCropper(fullPageScreenshots ? Integer.MAX_VALUE : indent);
            highlighterType.addIndentFilter(indentCropper);
            aShot.imageCropper(indentCropper);
            return ImageTool.toByteArray(
                    webElements.isEmpty() || HighlighterType.DEFAULT == highlighterType && fullPageScreenshots
                            ? aShot.takeScreenshot(webDriver) : aShot.takeScreenshot(webDriver, webElements));
        }
        catch (IOException e)
        {
            throw new IllegalStateException(e);
        }
    }

    public void setFullPageScreenshots(boolean fullPageScreenshots)
    {
        this.fullPageScreenshots = fullPageScreenshots;
    }

    public void setIndent(int indent)
    {
        this.indent = indent;
    }

    public void setHighlighterType(HighlighterType highlighterType)
    {
        this.highlighterType = highlighterType;
    }
}
