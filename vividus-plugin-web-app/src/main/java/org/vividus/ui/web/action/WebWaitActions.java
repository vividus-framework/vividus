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

import java.time.Duration;
import java.util.function.Function;

import javax.inject.Inject;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.WebDriverType;
import org.vividus.selenium.manager.IWebDriverManager;
import org.vividus.ui.action.WaitActions;

public class WebWaitActions extends WaitActions implements IWebWaitActions
{
    @Inject private IWebDriverProvider webDriverProvider;

    @Inject private WebJavascriptActions javascriptActions;
    @Inject private IAlertActions alertActions;
    @Inject private IWebDriverManager webDriverManager;

    private Duration pageStartsToLoadTimeout;

    @Override
    public void waitForPageLoad()
    {
        waitForPageLoad(webDriverProvider.get());
    }

    @Override
    public void waitForPageLoad(WebDriver webDriver)
    {
        if (!alertActions.isAlertPresent(webDriver) && !webDriverManager.isElectronApp())
        {
            boolean iexplore = webDriverManager.isTypeAnyOf(WebDriverType.IEXPLORE);
            /*
             * Workaround for ChromeDriver. Waits for the specified timeout for the page to start reloading.
             */
            if ((webDriverManager.isTypeAnyOf(WebDriverType.CHROME) || webDriverManager.isIOS())
                    && checkDocumentReadyState(webDriver, iexplore))
            {
                sleepForTimeout(pageStartsToLoadTimeout);
            }
            wait(webDriver, new Function<WebDriver, Boolean>()
            {
                @Override
                public Boolean apply(WebDriver webDriver)
                {
                    return checkDocumentReadyState(webDriver, iexplore);
                }

                @Override
                public String toString()
                {
                    return "page load";
                }
            });
        }
    }

    private boolean checkDocumentReadyState(WebDriver webDriver, boolean isIExplore)
    {
        if (alertActions.isAlertPresent(webDriver))
        {
            return true;
        }
        String status = "";
        try
        {
            status = javascriptActions.executeScript("return document.readyState");
        }
        catch (WebDriverException e)
        {
            webDriver.switchTo().defaultContent();
        }
        return "complete".equals(status) || isIExplore && "interactive".equals(status);
    }

    public void setPageStartsToLoadTimeout(Duration pageStartsToLoadTimeout)
    {
        this.pageStartsToLoadTimeout = pageStartsToLoadTimeout;
    }
}
