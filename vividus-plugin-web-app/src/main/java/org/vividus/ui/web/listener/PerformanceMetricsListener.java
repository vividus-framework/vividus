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

package org.vividus.ui.web.listener;

import com.google.common.eventbus.Subscribe;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.events.AbstractWebDriverEventListener;
import org.vividus.testcontext.TestContext;
import org.vividus.ui.web.event.PageLoadEndEvent;
import org.vividus.ui.web.performance.PerformanceMetrics;

public class PerformanceMetricsListener extends AbstractWebDriverEventListener
{
    private final TestContext testContext;

    public PerformanceMetricsListener(TestContext testContext)
    {
        this.testContext = testContext;
    }

    @Override
    public void afterNavigateBack(WebDriver driver)
    {
        resetPerformanceMetrics();
    }

    @Override
    public void afterNavigateForward(WebDriver driver)
    {
        resetPerformanceMetrics();
    }

    @Override
    public void afterNavigateTo(String url, WebDriver driver)
    {
        resetPerformanceMetrics();
    }

    @Override
    public void afterSwitchToWindow(String windowName, WebDriver driver)
    {
        resetPerformanceMetrics();
    }

    @Subscribe
    public void handle(PageLoadEndEvent event)
    {
        if (event.isNewPageLoaded())
        {
            resetPerformanceMetrics();
        }
    }

    private void resetPerformanceMetrics()
    {
        testContext.remove(PerformanceMetrics.class);
    }
}
