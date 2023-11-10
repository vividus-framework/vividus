/*
 * Copyright 2019-2023 the original author or authors.
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

package org.vividus.ui.web.action;

import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver.Navigation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.softassert.ISoftAssert;

import jakarta.inject.Inject;

public class NavigateActions implements INavigateActions
{
    private static final Logger LOGGER = LoggerFactory.getLogger(NavigateActions.class);

    @Inject private IWebDriverProvider webDriverProvider;
    @Inject private ISoftAssert softAssert;
    @Inject private WebJavascriptActions javascriptActions;
    @Inject private IWebWaitActions waitActions;

    @Override
    public void navigateTo(String url)
    {
        LOGGER.info("Loading: {}", url);
        try
        {
            getNavigator().to(url);
        }
        catch (TimeoutException ex)
        {
            handleTimeoutException(ex);
        }
    }

    @Override
    public void refresh()
    {
        LOGGER.info("Refreshing the current page");
        try
        {
            getNavigator().refresh();
        }
        catch (TimeoutException ex)
        {
            handleTimeoutException(ex);
        }
        // Chrome browser doesn't wait for page load to the end (like Firefox) and stops waiting when the web site is
        // still loading. In this case Vividus additional wait.
        waitActions.waitForPageLoad();
    }

    @SuppressWarnings("checkstyle:IllegalCatchExtended")
    private void handleTimeoutException(TimeoutException exception)
    {
        softAssert.recordFailedAssertion(exception);
        try
        {
            javascriptActions.executeScript("window.stop()");
        }
        catch (Exception e)
        {
            LOGGER.error("Unable to stop resource loading", e);
        }
    }

    private Navigation getNavigator()
    {
        return webDriverProvider.get().navigate();
    }
}
