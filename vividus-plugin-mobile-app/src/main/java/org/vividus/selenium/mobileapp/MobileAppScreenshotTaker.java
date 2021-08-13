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

package org.vividus.selenium.mobileapp;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

import com.google.common.eventbus.EventBus;

import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.mobileapp.screenshot.MobileAppAshotFactory;
import org.vividus.selenium.screenshot.AbstractScreenshotTaker;
import org.vividus.selenium.screenshot.IScreenshotFileNameGenerator;
import org.vividus.selenium.screenshot.Screenshot;
import org.vividus.selenium.screenshot.ScreenshotConfiguration;
import org.vividus.selenium.screenshot.ScreenshotDebugger;

import ru.yandex.qatools.ashot.AShot;

public class MobileAppScreenshotTaker extends AbstractScreenshotTaker
{
    private final MobileAppAshotFactory mobileAppAshotFactory;

    public MobileAppScreenshotTaker(IWebDriverProvider webDriverProvider,
            IScreenshotFileNameGenerator screenshotFileNameGenerator, EventBus eventBus,
            MobileAppAshotFactory mobileAppAshotFactory, ScreenshotDebugger screenshotDebugger)
    {
        super(webDriverProvider, eventBus, screenshotFileNameGenerator, screenshotDebugger);
        this.mobileAppAshotFactory = mobileAppAshotFactory;
    }

    @Override
    public Optional<Screenshot> takeScreenshot(String screenshotName)
    {
        String fileName = generateScreenshotFileName(screenshotName);
        return Optional.of(new Screenshot(fileName, getScreenshotBytes()));
    }

    @Override
    public Path takeScreenshotAsFile(String screenshotName) throws IOException
    {
        return takeScreenshot(generateScreenshotPath(screenshotName));
    }

    @Override
    public Path takeScreenshot(Path screenshotFilePath) throws IOException
    {
        return takeScreenshotAsFile(screenshotFilePath, this::getScreenshotBytes);
    }

    @Override
    protected AShot crateAshot(Optional<ScreenshotConfiguration> screenshotConfiguration)
    {
        return mobileAppAshotFactory.create(screenshotConfiguration);
    }
}
