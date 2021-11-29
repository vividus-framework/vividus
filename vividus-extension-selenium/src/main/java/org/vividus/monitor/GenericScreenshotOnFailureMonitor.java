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

package org.vividus.monitor;

import java.util.Optional;

import com.google.common.eventbus.EventBus;

import org.vividus.context.RunContext;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.screenshot.Screenshot;
import org.vividus.selenium.screenshot.ScreenshotTaker;

public class GenericScreenshotOnFailureMonitor extends AbstractScreenshotOnFailureMonitor
{
    private final ScreenshotTaker screenshotTaker;

    public GenericScreenshotOnFailureMonitor(EventBus eventBus, RunContext runContext,
            IWebDriverProvider webDriverProvider, ScreenshotTaker screenshotTaker)
    {
        super(eventBus, runContext, webDriverProvider);
        this.screenshotTaker = screenshotTaker;
    }

    @Override
    protected Optional<Screenshot> takeAssertionFailureScreenshot(String screenshotName)
    {
        return screenshotTaker.takeScreenshot(screenshotName);
    }
}
