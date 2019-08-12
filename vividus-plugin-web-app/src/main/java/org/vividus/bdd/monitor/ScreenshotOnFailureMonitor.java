/*
 * Copyright 2019 the original author or authors.
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

package org.vividus.bdd.monitor;

import java.lang.reflect.Method;
import java.util.List;

import javax.inject.Inject;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import org.jbehave.core.steps.NullStepMonitor;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.bdd.context.IBddRunContext;
import org.vividus.bdd.model.RunningStory;
import org.vividus.reporter.event.AttachmentPublishEvent;
import org.vividus.reporter.model.Attachment;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.screenshot.IScreenshotTaker;
import org.vividus.softassert.event.AssertionFailedEvent;
import org.vividus.ui.web.context.IWebUiContext;

public class ScreenshotOnFailureMonitor extends NullStepMonitor
{
    private static final String NO_SCREENSHOT_ON_FAILURE_META_NAME = "noScreenshotOnFailure";

    private static final Logger LOGGER = LoggerFactory.getLogger(ScreenshotOnFailureMonitor.class);

    private final ThreadLocal<Boolean> takeScreenshotOnFailureEnabled = InheritableThreadLocal.withInitial(
        () -> Boolean.FALSE);

    @Inject private EventBus eventBus;

    @Inject private IBddRunContext bddRunContext;

    @Inject private IWebDriverProvider webDriverProvider;
    @Inject private IWebUiContext webUiContext;
    @Inject private IScreenshotTaker screenshotTaker;

    @Override
    public void beforePerforming(String step, boolean dryRun, Method method)
    {
        if (takeScreenshotOnFailure(method) && !isStoryHasNoScreenshotOnFailureMeta()
                && !isScenarioHasNoScreenshotsOnFailureMeta())
        {
            enableScreenshotOnFailure();
        }
    }

    @Override
    public void afterPerforming(String step, boolean dryRun, Method method)
    {
        if (takeScreenshotOnFailure(method))
        {
            disableScreenshotOnFailure();
        }
    }

    @Subscribe
    public void onAssertionFailure(AssertionFailedEvent event)
    {
        if (takeScreenshotOnFailureEnabled.get() && webDriverProvider.isWebDriverInitialized())
        {
            SearchContext searchContext = webUiContext.getSearchContext();
            List<WebElement> webElements = searchContext instanceof WebElement ? List.of((WebElement) searchContext)
                    : webUiContext.getAssertingWebElements();
            try
            {
                screenshotTaker.takeScreenshot("Assertion_Failure", webElements).ifPresent(screenshot ->
                {
                    Attachment attachment = new Attachment(screenshot.getData(), screenshot.getFileName());
                    eventBus.post(new AttachmentPublishEvent(attachment));
                });
            }
            //CHECKSTYLE:OFF
            catch (RuntimeException e)
            {
                LOGGER.error("Unable to take a screenshot", e);
            }
            //CHECKSTYLE:ON
        }
    }

    private static boolean takeScreenshotOnFailure(Method method)
    {
        return method != null && (method.isAnnotationPresent(TakeScreenshotOnFailure.class) || method
                .getDeclaringClass().isAnnotationPresent(TakeScreenshotOnFailure.class));
    }

    private void enableScreenshotOnFailure()
    {
        takeScreenshotOnFailureEnabled.set(Boolean.TRUE);
    }

    private void disableScreenshotOnFailure()
    {
        takeScreenshotOnFailureEnabled.set(Boolean.FALSE);
    }

    private boolean isStoryHasNoScreenshotOnFailureMeta()
    {
        RunningStory runningStory = bddRunContext.getRunningStory();
        return runningStory.getStory().getMeta().hasProperty(NO_SCREENSHOT_ON_FAILURE_META_NAME);
    }

    private boolean isScenarioHasNoScreenshotsOnFailureMeta()
    {
        RunningStory runningStory = bddRunContext.getRunningStory();
        return runningStory.getRunningScenario().getScenario().getMeta()
                .hasProperty(NO_SCREENSHOT_ON_FAILURE_META_NAME);
    }
}
