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

package org.vividus.ui.web.action;

import java.util.Iterator;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import javax.inject.Inject;

import org.hamcrest.Matcher;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.manager.IWebDriverManager;

public class WindowsActions implements IWindowsActions
{
    private static final Logger LOGGER = LoggerFactory.getLogger(WindowsActions.class);

    @Inject private IWebDriverProvider webDriverProvider;
    @Inject private IWebDriverManager webDriverManager;

    @Override
    public void closeAllWindowsExceptOne()
    {
        Set<String> windows = webDriverManager.getWindowHandles();
        if (windows.size() > 1)
        {
            if (webDriverManager.isAndroid())
            {
                closeMobileWindows(windows);
            }
            else
            {
                closeSmallerWindows(windows);
            }
        }
    }

    @Override
    public String switchToNewWindow(String currentWindow)
    {
        return pickWindow((WebDriver driver, String window) ->
        {
            if (!window.equals(currentWindow))
            {
                driver.switchTo().window(window);
                return window;
            }
            return null;
        }, () -> currentWindow);
    }

    @Override
    public String switchToWindowWithMatchingTitle(Matcher<String> matcher)
    {
        return pickWindow((WebDriver driver, String window) ->
        {
            driver.switchTo().window(window);
            String title = driver.getTitle();
            if (matcher.matches(title))
            {
                return title;
            }
            return null;
        }, () -> getWebDriver().getTitle());
    }

    @Override
    public void switchToPreviousWindow()
    {
        getWebDriver().switchTo().window(webDriverManager.getWindowHandles().iterator().next());
    }

    private String pickWindow(BiFunction<WebDriver, String, String> picker, Supplier<String> defaultValueProvider)
    {
        WebDriver driver = getWebDriver();
        for (String window : webDriverManager.getWindowHandles())
        {
            String value = picker.apply(driver, window);
            if (null != value)
            {
                return value;
            }
        }
        return defaultValueProvider.get();
    }

    private void closeMobileWindows(Set<String> windows)
    {
        LOGGER.info("Closing windows except the first one");
        windows.remove(IWebDriverManager.NATIVE_APP_CONTEXT);
        Iterator<String> iterator = windows.iterator();
        WebDriver driver = getWebDriver();
        while (iterator.hasNext())
        {
            iterator.next();
            driver.navigate().back();
        }
        switchToPreviousWindow();
    }

    private void closeSmallerWindows(Set<String> windows)
    {
        LOGGER.info("Closing windows except larger one");
        Iterator<String> iterator = windows.iterator();
        String maxId = iterator.next();
        int maxSize = getWindowSize(maxId);
        while (iterator.hasNext())
        {
            String currentId = iterator.next();
            int size = getWindowSize(currentId);
            if (size > maxSize)
            {
                closeWindow(maxId, currentId);
                maxId = currentId;
                maxSize = size;
            }
            else
            {
                closeWindow(currentId, maxId);
            }
        }
    }

    private int getWindowSize(String windowHandle)
    {
        WebDriver driver = getWebDriver();
        driver.switchTo().window(windowHandle);
        Dimension dimension = driver.manage().window().getSize();
        return dimension.getHeight() * dimension.getWidth();
    }

    private void closeWindow(String windowToClose, String windowToSwitch)
    {
        WebDriver driver = getWebDriver();
        driver.switchTo().window(windowToClose);
        driver.close();
        driver.switchTo().window(windowToSwitch);
    }

    private WebDriver getWebDriver()
    {
        return webDriverProvider.get();
    }
}
