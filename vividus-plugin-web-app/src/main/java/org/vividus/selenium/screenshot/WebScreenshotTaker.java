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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import com.google.common.eventbus.EventBus;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.util.property.PropertyMappedCollection;

import ru.yandex.qatools.ashot.AShot;
import ru.yandex.qatools.ashot.cropper.indent.IndentCropper;
import ru.yandex.qatools.ashot.util.ImageTool;

public class WebScreenshotTaker extends AbstractScreenshotTaker
{
    private static final Logger LOGGER = LoggerFactory.getLogger(WebScreenshotTaker.class);

    private final IScreenshotFileNameGenerator screenshotFileNameGenerator;
    private final IWebElementHighlighter webElementHighlighter;
    private final EventBus eventBus;
    private final IScrollbarHandler scrollbarHandler;
    private final IAshotFactory ashotFactory;
    private final ScreenshotDebugger screenshotDebugger;

    private File screenshotDirectory;
    private boolean fullPageScreenshots;
    private int indent;
    private HighlighterType highlighterType;
    private String shootingStrategy;
    private PropertyMappedCollection<ScreenshotConfiguration> ashotConfigurations;

    public WebScreenshotTaker(IWebDriverProvider webDriverProvider,
            IScreenshotFileNameGenerator screenshotFileNameGenerator, IWebElementHighlighter webElementHighlighter,
            EventBus eventBus, IScrollbarHandler scrollbarHandler, IAshotFactory ashotFactory,
            ScreenshotDebugger screenshotDebugger)
    {
        super(webDriverProvider);
        this.screenshotFileNameGenerator = screenshotFileNameGenerator;
        this.webElementHighlighter = webElementHighlighter;
        this.eventBus = eventBus;
        this.scrollbarHandler = scrollbarHandler;
        this.ashotFactory = ashotFactory;
        this.screenshotDebugger = screenshotDebugger;
    }

    @Override
    public Optional<Screenshot> takeScreenshot(String screenshotName)
    {
        return takeScreenshot(screenshotName, List.of());
    }

    public Path takeScreenshotAsFile(String screenshotName) throws IOException
    {
        return takeScreenshot(() -> new File(screenshotDirectory, generateScreenshotFileName(screenshotName)).toPath(),
                false);
    }

    public void takeScreenshot(Path screenshotFilePath) throws IOException
    {
        takeScreenshot(() -> screenshotFilePath, false);
    }

    public Optional<Screenshot> takeScreenshot(String screenshotName, List<WebElement> webElementsToHighlight)
    {
        byte[] screenshotData = takeScreenshotAsByteArray(webElementsToHighlight, false);
        return createScreenshot(screenshotData, screenshotName);
    }

    private ru.yandex.qatools.ashot.Screenshot takeScreenshot(SearchContext searchContext, AShot aShot,
            Optional<WebElement> scrollableElement)
    {
        Supplier<ru.yandex.qatools.ashot.Screenshot> screenshotTaker = () -> searchContext instanceof WebDriver
                ? aShot.takeScreenshot((WebDriver) searchContext)
                : aShot.takeScreenshot(getWebDriverProvider().get(), (WebElement) searchContext);

        ru.yandex.qatools.ashot.Screenshot screenshot =
                scrollableElement.map(e -> scrollbarHandler.performActionWithHiddenScrollbars(screenshotTaker, e))
            .orElseGet(() -> scrollbarHandler.performActionWithHiddenScrollbars(screenshotTaker));

        screenshotDebugger.debug(this.getClass(), "After_AShot", screenshot.getImage());
        return screenshot;
    }

    public ru.yandex.qatools.ashot.Screenshot takeAshotScreenshot(SearchContext searchContext,
            Optional<ScreenshotConfiguration> screenshotConfiguration)
    {
        Optional<ScreenshotConfiguration> configuration = screenshotConfiguration.or(this::getConfiguration);
        AShot aShot = createAShot(false, configuration);
        return takeScreenshot(searchContext, aShot,
                configuration.map(ScreenshotConfiguration::getScrollableElement).flatMap(Supplier::get));
    }

    private Optional<ScreenshotConfiguration> getConfiguration()
    {
        return ashotConfigurations.getNullable(shootingStrategy);
    }

    private Optional<Screenshot> createScreenshot(byte[] screenshotData, String screenshotName)
    {
        return screenshotData.length > 0
                ? Optional.of(new Screenshot(generateScreenshotFileName(screenshotName), screenshotData))
                : Optional.empty();
    }

    private Path takeScreenshot(Supplier<Path> screenshotFilePathSupplier,
            boolean viewportScreenshot) throws IOException
    {
        byte[] screenshotData = takeScreenshotAsByteArray(List.of(), viewportScreenshot);
        if (screenshotData.length > 0)
        {
            Path screenshotFilePath = screenshotFilePathSupplier.get();
            Path parent = screenshotFilePath.getParent();
            if (parent != null)
            {
                FileUtils.forceMkdir(parent.toFile());
            }
            Files.write(screenshotFilePath, screenshotData);

            LOGGER.info("Screenshot was taken: {}", screenshotFilePath.toAbsolutePath());

            eventBus.post(new ScreenshotTakeEvent(screenshotFilePath));
            return screenshotFilePath;
        }
        return null;
    }

    private byte[] takeScreenshotAsByteArray(List<WebElement> webElements, boolean viewportScreenshot)
    {
        return takeScreenshotAsByteArray(() ->
        {
            byte[] screenshotData;
            if (HighlighterType.DEFAULT == highlighterType)
            {
                screenshotData = webElementHighlighter.takeScreenshotWithHighlights(
                    () -> takeScreenshotAsByteArrayImpl(webElements, viewportScreenshot));
            }
            else
            {
                screenshotData = takeScreenshotAsByteArrayImpl(webElements, viewportScreenshot);
            }
            return screenshotData;
        });
    }

    private byte[] takeScreenshotAsByteArray(Supplier<byte[]> screenshotDataSupplier)
    {
        if (getWebDriverProvider().isWebDriverInitialized())
        {
            return screenshotDataSupplier.get();
        }
        LOGGER.info("WebDriver is not initialized");
        return new byte[0];
    }

    private byte[] takeScreenshotAsByteArrayImpl(List<WebElement> webElements, boolean viewportScreenshot)
    {
        WebDriver webDriver = getWebDriverProvider().get();
        try
        {
            AShot aShot = createAShot(viewportScreenshot);
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

    private AShot createAShot(boolean viewportScreenshot)
    {
        return ashotFactory.create(viewportScreenshot, getConfiguration());
    }

    private AShot createAShot(boolean viewportScreenshot, Optional<ScreenshotConfiguration> screenshotConfiguration)
    {
        return ashotFactory.create(viewportScreenshot, screenshotConfiguration);
    }

    private String generateScreenshotFileName(String screenshotName)
    {
        return screenshotFileNameGenerator.generateScreenshotFileName(screenshotName);
    }

    public void setScreenshotDirectory(File screenshotDirectory)
    {
        this.screenshotDirectory = screenshotDirectory;
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

    public void setShootingStrategy(String shootingStrategy)
    {
        this.shootingStrategy = shootingStrategy;
    }

    public void setAshotConfigurations(PropertyMappedCollection<ScreenshotConfiguration> ashotConfigurations)
    {
        this.ashotConfigurations = ashotConfigurations;
    }
}
