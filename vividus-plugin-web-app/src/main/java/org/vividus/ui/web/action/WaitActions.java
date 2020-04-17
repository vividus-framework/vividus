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

import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.support.ui.Wait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.WebDriverType;
import org.vividus.selenium.manager.IWebDriverManager;
import org.vividus.softassert.ISoftAssert;
import org.vividus.util.Sleeper;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public class WaitActions implements IWaitActions
{
    private static final Logger LOGGER = LoggerFactory.getLogger(WaitActions.class);

    @Inject private IWebDriverProvider webDriverProvider;
    @Inject private ISoftAssert softAssert;
    @Inject private IWaitFactory waitFactory;

    @Inject private IJavascriptActions javascriptActions;
    @Inject private IAlertActions alertActions;
    @Inject private IWebDriverManager webDriverManager;

    private Duration pageOpenTimeout;
    private Duration windowOpenTimeout;
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

    @Override
    public WaitResult<Boolean> waitForNewWindowOpen(WebDriver webDriver, int alreadyOpenedWindowNumber)
    {
        return waitForNewWindowOpen(webDriver, alreadyOpenedWindowNumber, windowOpenTimeout, true);
    }

    @Override
    public WaitResult<Boolean> waitForNewWindowOpen(WebDriver webDriver, int alreadyOpenedWindowNumber,
            boolean recordAssertionIfTimeout)
    {
        return waitForNewWindowOpen(webDriver, alreadyOpenedWindowNumber, windowOpenTimeout, recordAssertionIfTimeout);
    }

    @Override
    public WaitResult<Boolean> waitForNewWindowOpen(WebDriver webDriver, int alreadyOpenedWindowNumber,
            Duration timeOut)
    {
        return waitForNewWindowOpen(webDriver, alreadyOpenedWindowNumber, timeOut, true);
    }

    @Override
    public WaitResult<Boolean> waitForNewWindowOpen(WebDriver webDriver, int alreadyOpenedWindowNumber,
            Duration timeOut, boolean recordAssertionIfTimeout)
    {
        return wait(webDriver, timeOut, new Function<>()
        {
            @Override
            public Boolean apply(WebDriver webDriver)
            {
                return webDriver.getWindowHandles().size() > alreadyOpenedWindowNumber;
            }

            @Override
            public String toString()
            {
                return "new window is opening";
            }
        }, recordAssertionIfTimeout);
    }

    @Override
    public WaitResult<Boolean> waitForPageOpen(WebDriver webDriver, String oldUrl)
    {
        return waitForPageOpen(webDriver, oldUrl, pageOpenTimeout);
    }

    @Override
    public WaitResult<Boolean> waitForPageOpen(WebDriver webDriver, String oldUrl, Duration timeout)
    {
        return wait(webDriver, timeout, new Function<>()
        {
            @Override
            public Boolean apply(WebDriver webDriver)
            {
                return !oldUrl.equals(webDriver.getCurrentUrl());
            }

            @Override
            public String toString()
            {
                return "new page is opening";
            }
        });
    }

    @Override
    public <T, V> WaitResult<V> wait(T input, Function<T, V> isTrue)
    {
        return wait(input, isTrue, true);
    }

    @Override
    public <T, V> WaitResult<V> wait(T input, Function<T, V> isTrue, boolean recordAssertionIfTimeout)
    {
        return wait(input, waitFactory::createWait, isTrue, recordAssertionIfTimeout);
    }

    @Override
    public <T, V> WaitResult<V> wait(T input, Duration timeout, Function<T, V> isTrue)
    {
        return wait(input, timeout, isTrue, true);
    }

    @Override
    public <T, V> WaitResult<V> wait(T input, Duration timeout, Function<T, V> isTrue, boolean recordAssertionIfTimeout)
    {
        return wait(input, i -> waitFactory.createWait(i, timeout), isTrue,
                recordAssertionIfTimeout);
    }

    @Override
    public <T, V> WaitResult<V> wait(T input, Duration timeout, Duration pollingPeriod, Function<T, V> isTrue)
    {
        return wait(input, timeout, pollingPeriod, isTrue, true);
    }

    @Override
    public <T, V> WaitResult<V> wait(T input, Duration timeout, Duration pollingPeriod, Function<T, V> isTrue,
            boolean recordAssertionIfTimeout)
    {
        return wait(input, i -> waitFactory.createWait(i, timeout, pollingPeriod), isTrue, recordAssertionIfTimeout);
    }

    @SuppressFBWarnings("NP_LOAD_OF_KNOWN_NULL_VALUE")
    private <T, V> WaitResult<V> wait(T input, Function<T, Wait<T>> waitSupplier, Function<T, V> isTrue,
            boolean recordAssertionIfTimeout)
    {
        WaitResult<V> result = new WaitResult<>();
        if (input != null)
        {
            Wait<T> wait = waitSupplier.apply(input);
            try
            {
                V value = wait.until(isTrue);
                result.setWaitPassed(true);
                LOGGER.debug(wait.toString());
                result.setData(value);
                return result;
            }
            catch (TimeoutException timeoutException)
            {
                if (recordAssertionIfTimeout)
                {
                    recordFailedAssertion(wait, timeoutException);
                }
                return result;
            }
            catch (NoSuchElementException noSuchElementException)
            {
                recordFailedAssertion(wait, noSuchElementException);
                return result;
            }
        }
        result.setWaitPassed(softAssert.assertNotNull("The input value to pass to the wait condition", input));
        return result;
    }

    @Override
    public WaitResult<Boolean> waitForWindowToClose(WebDriver webDriver, String windowHandleToClose)
    {
        return wait(webDriver, new Function<>()
        {
            @Override
            public Boolean apply(WebDriver webDriver)
            {
                return !webDriver.getWindowHandles().contains(windowHandleToClose);
            }

            @Override
            public String toString()
            {
                return "waiting for specified window to close";
            }
        });
    }

    @Override
    public void sleepForTimeout(Duration time)
    {
        Sleeper.sleep(time);
    }

    private boolean recordFailedAssertion(Wait<?> wait, WebDriverException e)
    {
        return softAssert.recordFailedAssertion(wait + ". Error: " + e.getMessage());
    }

    public void setPageOpenTimeout(Duration pageOpenTimeout)
    {
        this.pageOpenTimeout = pageOpenTimeout;
    }

    public void setWindowOpenTimeout(Duration windowOpenTimeout)
    {
        this.windowOpenTimeout = windowOpenTimeout;
    }

    public void setPageStartsToLoadTimeout(Duration pageStartsToLoadTimeout)
    {
        this.pageStartsToLoadTimeout = pageStartsToLoadTimeout;
    }
}
