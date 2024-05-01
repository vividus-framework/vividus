/*
 * Copyright 2019-2024 the original author or authors.
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

import java.lang.reflect.Method;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriver.Navigation;
import org.openqa.selenium.WebDriver.TargetLocator;
import org.openqa.selenium.support.events.WebDriverListener;
import org.vividus.testcontext.TestContext;
import org.vividus.ui.web.event.PageLoadEndEvent;
import org.vividus.ui.web.performance.PerformanceMetrics;

public class PerformanceMetricsListener implements WebDriverListener
{
    private final TestContext testContext;
    private final EventBus eventBus;

    public PerformanceMetricsListener(TestContext testContext, EventBus eventBus)
    {
        this.testContext = testContext;
        this.eventBus = eventBus;
        eventBus.register(this);
    }

    @Override
    public void beforeQuit(WebDriver driver)
    {
        eventBus.unregister(this);
    }

    @Override
    public void afterAnyNavigationCall(Navigation navigation, Method method, Object[] args, Object result)
    {
        resetPerformanceMetrics();
    }

    @Override
    public void afterWindow(TargetLocator targetLocator, String nameOrHandle, WebDriver driver)
    {
        resetPerformanceMetrics();
    }

    @Subscribe
    public void onPageLoadFinish(PageLoadEndEvent event)
    {
        if (event.newPageLoaded())
        {
            resetPerformanceMetrics();
        }
    }

    private void resetPerformanceMetrics()
    {
        // Can't move this logic to PerformanceMetrics: the workaround to break circular dependency
        testContext.remove(PerformanceMetrics.KEY);
    }

    public static class Factory implements WebDriverListenerFactory
    {
        private final TestContext testContext;
        private final EventBus eventBus;

        public Factory(TestContext testContext, EventBus eventBus)
        {
            this.testContext = testContext;
            this.eventBus = eventBus;
        }

        @Override
        public PerformanceMetricsListener createListener(WebDriver webDriver)
        {
            return new PerformanceMetricsListener(testContext, eventBus);
        }
    }
}
