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

public class WebScreenshotTaker extends AbstractScreenshotTaker<WebScreenshotConfiguration>
{
    private static final Logger LOGGER = LoggerFactory.getLogger(WebScreenshotTaker.class);

    private final IWebElementHighlighter webElementHighlighter;

    private boolean fullPageScreenshots;
    private int indent;
    private HighlighterType highlighterType;

    public WebScreenshotTaker(IWebDriverProvider webDriverProvider, EventBus eventBus,
            IScreenshotFileNameGenerator screenshotFileNameGenerator,
            AshotFactory<WebScreenshotConfiguration> ashotFactory, ScreenshotDebugger screenshotDebugger,
            IWebElementHighlighter webElementHighlighter)
    {
        super(webDriverProvider, eventBus, screenshotFileNameGenerator, ashotFactory, screenshotDebugger);
        this.webElementHighlighter = webElementHighlighter;
    }

    @Override
    public Optional<Screenshot> takeScreenshot(String screenshotName)
    {
        return takeScreenshot(screenshotName, List.of());
    }

    @Override
    protected byte[] takeScreenshotAsByteArray()
    {
        return takeScreenshotAsByteArray(List.of());
    }

    public Optional<Screenshot> takeScreenshot(String screenshotName, List<WebElement> webElementsToHighlight)
    {
        byte[] screenshotData = takeScreenshotAsByteArray(webElementsToHighlight);
        return screenshotData.length > 0
                ? Optional.of(new Screenshot(generateScreenshotFileName(screenshotName), screenshotData))
                : Optional.empty();
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
            AShot aShot = crateAshot(Optional.empty());
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
