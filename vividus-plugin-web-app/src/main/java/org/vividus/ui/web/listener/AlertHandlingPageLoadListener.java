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

package org.vividus.ui.web.listener;

import javax.inject.Inject;

import com.google.common.eventbus.Subscribe;

import org.openqa.selenium.NoSuchFrameException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.events.AbstractWebDriverEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.ui.web.action.JavascriptActions;
import org.vividus.ui.web.event.PageLoadEndEvent;

public class AlertHandlingPageLoadListener extends AbstractWebDriverEventListener
{
    private static final Logger LOGGER = LoggerFactory.getLogger(AlertHandlingPageLoadListener.class);

    private static final String ALERT_SCRIPT = "confirm = function(message){return arguments[0];};"
            + "alert = function(message){return arguments[0];};prompt = function(message){return arguments[0];};";

    @Inject private JavascriptActions javascriptActions;
    private AlertHandlingOptions alertHandlingOptions;

    @Override
    public void afterNavigateTo(String url, WebDriver driver)
    {
        selectOptionForAlertHandling();
    }

    @Override
    public void afterNavigateBack(WebDriver driver)
    {
        selectOptionForAlertHandling();
    }

    @Override
    public void afterNavigateForward(WebDriver driver)
    {
        selectOptionForAlertHandling();
    }

    @Override
    public void afterClickOn(WebElement element, WebDriver driver)
    {
        selectOptionForAlertHandling();
    }

    @Subscribe
    public void onPageLoadFinish(@SuppressWarnings("unused") PageLoadEndEvent event)
    {
        selectOptionForAlertHandling();
    }

    private void selectOptionForAlertHandling()
    {
        try
        {
            alertHandlingOptions.selectOptionForAlertHandling(javascriptActions, ALERT_SCRIPT);
        }
        catch (NoSuchFrameException e)
        {
            // SafariDriver do not process frame closure itself and throw NoSuchFrameException -
            // https://github.com/SeleniumHQ/selenium/issues/3314
            LOGGER.warn("Swallowing exception quietly (browser frame may have been closed)", e);
        }
    }

    public void setAlertHandlingOptions(AlertHandlingOptions alertHandlingOptions)
    {
        this.alertHandlingOptions = alertHandlingOptions;
    }
}
