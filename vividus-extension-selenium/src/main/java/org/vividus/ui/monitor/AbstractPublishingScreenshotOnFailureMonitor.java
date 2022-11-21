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

package org.vividus.ui.monitor;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

import com.google.common.eventbus.EventBus;

import org.vividus.context.RunContext;
import org.vividus.report.ui.ImageCompressor;
import org.vividus.reporter.model.Attachment;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.screenshot.Screenshot;

public abstract class AbstractPublishingScreenshotOnFailureMonitor extends AbstractPublishingAttachmentOnFailureMonitor
{
    private final ImageCompressor imageCompressor;

    private List<String> debugModes;

    protected AbstractPublishingScreenshotOnFailureMonitor(EventBus eventBus, RunContext runContext,
            IWebDriverProvider webDriverProvider, ImageCompressor imageCompressor)
    {
        super(runContext, webDriverProvider, eventBus, "noScreenshotOnFailure", "Unable to take a screenshot");
        this.imageCompressor = imageCompressor;
    }

    @Override
    protected Optional<Attachment> createAttachment()
    {
        return takeScreenshot("Assertion_Failure")
                .map(screenshot -> new Attachment(imageCompressor.compress(screenshot.getData()),
                    screenshot.getFileName()));
    }

    @Override
    protected boolean isPublishingEnabled(Method method)
    {
        return getAnnotation(method, TakeScreenshotOnFailure.class)
                .map(TakeScreenshotOnFailure::onlyInDebugMode)
                .map(debugModeProperty -> debugModeProperty.isEmpty()
                        || debugModes != null && debugModes.contains(debugModeProperty))
                .orElse(Boolean.FALSE);
    }

    protected abstract Optional<Screenshot> takeScreenshot(String screenshotName);

    public void setDebugModes(List<String> debugModes)
    {
        this.debugModes = debugModes;
    }
}
