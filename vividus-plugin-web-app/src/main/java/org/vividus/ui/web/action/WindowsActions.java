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
    public void closeAllTabsExceptOne()
    {
        Set<String> tabs = getWebDriver().getWindowHandles();
        if (tabs.size() > 1)
        {
            if (webDriverManager.isAndroid())
            {
                closeMobileTabs(tabs);
            }
            else
            {
                closeSmallerWindows(tabs);
            }
        }
    }

    @Override
    public String switchToNewTab(String currentTab)
    {
        return pickTab((WebDriver driver, String tab) ->
        {
            if (!tab.equals(currentTab))
            {
                driver.switchTo().window(tab);
                return tab;
            }
            return null;
        }, () -> currentTab);
    }

    @Override
    public String switchToTabWithMatchingTitle(Matcher<String> matcher)
    {
        return pickTab((WebDriver driver, String tab) ->
        {
            LOGGER.atInfo().addArgument(tab).log("Switching to a tab \"{}\"");
            driver.switchTo().window(tab);
            String title = driver.getTitle();
            LOGGER.atInfo().addArgument(title).log("Switched to a tab with the title: \"{}\"");
            if (matcher.matches(title))
            {
                return title;
            }
            return null;
        }, () -> getWebDriver().getTitle());
    }

    @Override
    public void switchToPreviousTab()
    {
        WebDriver webDriver = getWebDriver();
        webDriver.switchTo().window(webDriver.getWindowHandles().iterator().next());
    }

    private String pickTab(BiFunction<WebDriver, String, String> picker, Supplier<String> defaultValueProvider)
    {
        WebDriver driver = getWebDriver();
        for (String window : driver.getWindowHandles())
        {
            String value = picker.apply(driver, window);
            if (null != value)
            {
                return value;
            }
        }
        return defaultValueProvider.get();
    }

    private void closeMobileTabs(Set<String> windows)
    {
        LOGGER.info("Closing tabs except the first one");
        windows.remove(IWebDriverManager.NATIVE_APP_CONTEXT);
        Iterator<String> iterator = windows.iterator();
        WebDriver driver = getWebDriver();
        while (iterator.hasNext())
        {
            iterator.next();
            driver.navigate().back();
        }
        switchToPreviousTab();
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
                closeTab(maxId, currentId);
                maxId = currentId;
                maxSize = size;
            }
            else
            {
                closeTab(currentId, maxId);
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

    private void closeTab(String windowToClose, String windowToSwitch)
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
