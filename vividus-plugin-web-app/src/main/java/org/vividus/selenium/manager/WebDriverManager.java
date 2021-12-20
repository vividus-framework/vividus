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

package org.vividus.selenium.manager;

import java.util.stream.Stream;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.Browser;
import org.vividus.selenium.IWebDriverProvider;

public class WebDriverManager extends GenericWebDriverManager implements IWebDriverManager
{
    private boolean electronApp;

    public WebDriverManager(IWebDriverProvider webDriverProvider, IWebDriverManagerContext webDriverManagerContext)
    {
        super(webDriverProvider, webDriverManagerContext);
    }

    @Override
    public boolean isBrowserAnyOf(Browser... browsers)
    {
        return isBrowserAnyOf(getCapabilities(), browsers);
    }

    public static boolean isBrowserAnyOf(WebDriver webDriver, Browser... browsers)
    {
        return isBrowserAnyOf(getCapabilities(webDriver), browsers);
    }

    private static boolean isBrowserAnyOf(Capabilities capabilities, Browser... browsers)
    {
        return checkCapabilities(capabilities,
                () -> Stream.of(browsers).anyMatch(browser -> isBrowser(capabilities, browser)));
    }

    public static boolean isBrowser(Capabilities capabilities, Browser browser)
    {
        return browser.browserName().equalsIgnoreCase(capabilities.getBrowserName());
    }

    @Override
    public boolean isElectronApp()
    {
        return electronApp;
    }

    public void setElectronApp(boolean electronApp)
    {
        this.electronApp = electronApp;
    }
}
