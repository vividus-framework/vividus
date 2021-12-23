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

package org.vividus.monitor;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

import com.google.common.eventbus.EventBus;

import org.vividus.context.RunContext;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.screenshot.Screenshot;

public abstract class AbstractScreenshotOnFailureMonitor extends AbstractPublishingAttachmentOnFailureMonitor
{
    private static final String NO_SCREENSHOT_ON_FAILURE_META_NAME = "noScreenshotOnFailure";

    private List<String> debugModes;

    public AbstractScreenshotOnFailureMonitor(EventBus eventBus, RunContext runContext,
            IWebDriverProvider webDriverProvider)
    {
        super(runContext, webDriverProvider, eventBus, NO_SCREENSHOT_ON_FAILURE_META_NAME);
    }

    @Override

    protected void publishAttachment()
    {
        performOperation(() -> takeAssertionFailureScreenshot("Assertion_Failure").ifPresent(
                        screenshot -> publishAttachment(screenshot.getData(), screenshot.getFileName())),
                "Unable to take a screenshot");
    }

    @Override
    protected boolean isPublishingEnabled(Method method)
    {
        TakeScreenshotOnFailure annotation = (TakeScreenshotOnFailure) getAnnotation(method,
                TakeScreenshotOnFailure.class);
        if (annotation != null)
        {
            String debugModeProperty = annotation.onlyInDebugMode();
            return debugModeProperty.isEmpty()
                    || debugModes != null && debugModes.stream().anyMatch(debugModeProperty::equals);
        }
        return false;
    }

    protected abstract Optional<Screenshot> takeAssertionFailureScreenshot(String screenshotName);

    public void setDebugModes(List<String> debugModes)
    {
        this.debugModes = debugModes;
    }
}
