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

package org.vividus.ui.web.monitor;

import java.util.List;
import java.util.Optional;

import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.vividus.context.RunContext;
import org.vividus.report.ui.ImageCompressor;
import org.vividus.reporter.event.IAttachmentPublisher;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.screenshot.WebScreenshotTaker;
import org.vividus.ui.context.IUiContext;
import org.vividus.ui.monitor.AbstractPublishingScreenshotOnFailureMonitor;
import org.vividus.ui.screenshot.Screenshot;

public class PublishingWebScreenshotOnFailureMonitor extends AbstractPublishingScreenshotOnFailureMonitor
{
    private final IUiContext uiContext;
    private final WebScreenshotTaker webScreenshotTaker;

    public PublishingWebScreenshotOnFailureMonitor(RunContext runContext, IWebDriverProvider webDriverProvider,
            IAttachmentPublisher attachmentPublisher, IUiContext uiContext, WebScreenshotTaker webScreenshotTaker,
            ImageCompressor imageCompressor)
    {
        super(runContext, webDriverProvider, attachmentPublisher, imageCompressor);
        this.uiContext = uiContext;
        this.webScreenshotTaker = webScreenshotTaker;
    }

    @Override
    protected Optional<Screenshot> takeScreenshot(String screenshotName)
    {
        SearchContext searchContext = uiContext.getSearchContext();
        List<WebElement> webElements = searchContext instanceof WebElement webElement ? List.of(webElement)
                : uiContext.getAssertingWebElements();
        return webScreenshotTaker.takeScreenshot(screenshotName, webElements);
    }
}
