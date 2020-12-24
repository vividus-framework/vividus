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

package org.vividus.ui.web.action;

import java.net.URI;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.softassert.ISoftAssert;

public class NavigateActions implements INavigateActions
{
    private static final Logger LOGGER = LoggerFactory.getLogger(NavigateActions.class);

    @Inject private IWebDriverProvider webDriverProvider;
    @Inject private ISoftAssert softAssert;
    @Inject private WebJavascriptActions javascriptActions;
    @Inject private IWebWaitActions waitActions;
    private final ThreadLocal<Long> actualPageLoadTimeInMs = ThreadLocal.withInitial(() -> 0L);

    @Override
    public void navigateTo(String url)
    {
        LOGGER.info("Loading: {}", url);
        try
        {
            getWebDriver().navigate().to(url);
        }
        catch (TimeoutException ex)
        {
            handleTimeoutException(ex);
        }
    }

    @Override
    public void navigateTo(URI url)
    {
        navigateTo(url.toString());
    }

    @Override
    public void refresh()
    {
        refresh(getWebDriver());
    }

    @Override
    public void refresh(WebDriver webDriver)
    {
        LOGGER.info("Refreshing the current page");
        try
        {
            webDriver.navigate().refresh();
        }
        catch (TimeoutException ex)
        {
            handleTimeoutException(ex);
        }
        // Chrome browser doesn't wait for page load to the end (like Firefox) and stops waiting when the web site is
        // still loading. In this case Vividus additional wait.
        waitActions.waitForPageLoad();
    }

    @Override
    public void back()
    {
        LOGGER.info("Navigating back to the previous page");
        getWebDriver().navigate().back();
        waitActions.waitForPageLoad();
    }

    @Override
    public void back(String previousPageUrl)
    {
        String currentUrl = getWebDriver().getCurrentUrl();
        if (!previousPageUrl.equals(currentUrl))
        {
            navigateTo(previousPageUrl);
        }
    }

    @Override
    public void loadPage(String pageURL)
    {
        long start = System.nanoTime();
        navigateTo(pageURL);
        actualPageLoadTimeInMs.set(TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start));
    }

    @Override
    public long getActualPageLoadTimeInMs()
    {
        return actualPageLoadTimeInMs.get();
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

    private WebDriver getWebDriver()
    {
        return webDriverProvider.get();
    }
}
