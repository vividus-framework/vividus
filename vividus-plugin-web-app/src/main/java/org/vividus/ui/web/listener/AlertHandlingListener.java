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

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchFrameException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriver.Navigation;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.events.WebDriverListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.ui.web.event.PageLoadEndEvent;

public final class AlertHandlingListener implements WebDriverListener
{
    private static final Logger LOGGER = LoggerFactory.getLogger(AlertHandlingListener.class);

    private final WebDriver webDriver;
    private final AlertHandlingOption alertHandlingOption;
    private final EventBus eventBus;

    private AlertHandlingListener(WebDriver webDriver, AlertHandlingOption alertHandlingOption, EventBus eventBus)
    {
        this.webDriver = webDriver;
        this.alertHandlingOption = alertHandlingOption;
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
        selectOptionForAlertHandling();
    }

    @Override
    public void afterClick(WebElement element)
    {
        selectOptionForAlertHandling();
    }

    @SuppressWarnings("unused")
    @Subscribe
    public void onPageLoadFinish(PageLoadEndEvent event)
    {
        selectOptionForAlertHandling();
    }

    private void selectOptionForAlertHandling()
    {
        try
        {
            alertHandlingOption.doAction((JavascriptExecutor) webDriver);
        }
        catch (NoSuchFrameException e)
        {
            // SafariDriver do not process frame closure itself and throw NoSuchFrameException -
            // https://github.com/SeleniumHQ/selenium/issues/3314
            LOGGER.warn("Swallowing exception quietly (browser frame may have been closed)", e);
        }
    }

    public static class Factory implements WebDriverListenerFactory
    {
        private final AlertHandlingOption alertHandlingOption;
        private final EventBus eventBus;

        public Factory(AlertHandlingOption alertHandlingOption, EventBus eventBus)
        {
            this.alertHandlingOption = alertHandlingOption;
            this.eventBus = eventBus;
        }

        @Override
        public AlertHandlingListener createListener(WebDriver webDriver)
        {
            return new AlertHandlingListener(webDriver, alertHandlingOption, eventBus);
        }
    }

    public enum AlertHandlingOption
    {
        ACCEPT(Boolean.TRUE),
        DISMISS(Boolean.FALSE),
        DO_NOTHING(null)
        {
            @Override
            public void doAction(JavascriptExecutor javascriptExecutor)
            {
                // Nothing to do
            }
        };

        private final Boolean result;

        AlertHandlingOption(Boolean result)
        {
            this.result = result;
        }

        protected void doAction(JavascriptExecutor javascriptExecutor)
        {
            javascriptExecutor.executeScript("confirm = function(message){return arguments[0];};"
                    + "alert = function(message){return arguments[0];};"
                    + "prompt = function(message){return arguments[0];};", result);
        }
    }
}
