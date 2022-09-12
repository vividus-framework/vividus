/*
 * Copyright 2019-2022 the original author or authors.
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

import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.util.Optional;
import java.util.stream.Stream;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.Browser;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.session.WebDriverSessionInfo;

public class WebDriverManager extends GenericWebDriverManager implements IWebDriverManager
{
    private boolean electronApp;
    private Dimension remoteScreenResolution;

    public WebDriverManager(IWebDriverProvider webDriverProvider, WebDriverSessionInfo webDriverSessionInfo)
    {
        super(webDriverProvider, webDriverSessionInfo);
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

    public static boolean isBrowserAnyOf(Capabilities capabilities, Browser... browsers)
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

    @Override
    public Optional<Dimension> getScreenResolution()
    {
        if (getWebDriverProvider().isRemoteExecution())
        {
            return Optional.ofNullable(remoteScreenResolution);
        }
        if (GraphicsEnvironment.isHeadless())
        {
            return Optional.empty();
        }
        java.awt.Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        return Optional.of(new Dimension(screenSize.width, screenSize.height));
    }

    public void setElectronApp(boolean electronApp)
    {
        this.electronApp = electronApp;
    }

    public void setRemoteScreenResolution(Dimension remoteScreenResolution)
    {
        this.remoteScreenResolution = remoteScreenResolution;
    }
}
