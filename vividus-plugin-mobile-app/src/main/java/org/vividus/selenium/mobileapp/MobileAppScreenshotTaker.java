/*
 * Copyright 2019-2020 the original author or authors.
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

import java.util.Optional;

import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.screenshot.AbstractScreenshotTaker;
import org.vividus.selenium.screenshot.IScreenshotFileNameGenerator;
import org.vividus.selenium.screenshot.Screenshot;

public class MobileAppScreenshotTaker extends AbstractScreenshotTaker
{
    private final IScreenshotFileNameGenerator screenshotFileNameGenerator;

    public MobileAppScreenshotTaker(IWebDriverProvider webDriverProvider,
            IScreenshotFileNameGenerator screenshotFileNameGenerator)
    {
        super(webDriverProvider);
        this.screenshotFileNameGenerator = screenshotFileNameGenerator;
    }

    @Override
    public Optional<Screenshot> takeScreenshot(String screenshotName)
    {
        String fileName = screenshotFileNameGenerator.generateScreenshotFileName(screenshotName);
        return Optional.of(new Screenshot(fileName, getScreenshotBytes()));
    }
}
