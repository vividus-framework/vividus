/*
 * Copyright 2019-2025 the original author or authors.
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

package org.vividus.ui.monitor;

import java.util.Optional;

import org.vividus.context.RunContext;
import org.vividus.report.ui.ImageCompressor;
import org.vividus.reporter.event.IAttachmentPublisher;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.ui.screenshot.Screenshot;
import org.vividus.ui.screenshot.ScreenshotTaker;

public class PublishingScreenshotOnFailureMonitor extends AbstractPublishingScreenshotOnFailureMonitor
{
    private final ScreenshotTaker screenshotTaker;

    public PublishingScreenshotOnFailureMonitor(RunContext runContext, IWebDriverProvider webDriverProvider,
            IAttachmentPublisher attachmentPublisher, ScreenshotTaker screenshotTaker, ImageCompressor imageCompressor)
    {
        super(runContext, webDriverProvider, attachmentPublisher, imageCompressor);
        this.screenshotTaker = screenshotTaker;
    }

    @Override
    protected Optional<Screenshot> takeScreenshot(String screenshotName)
    {
        return screenshotTaker.takeScreenshot(screenshotName);
    }
}
