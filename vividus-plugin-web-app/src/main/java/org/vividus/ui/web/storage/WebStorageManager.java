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

package org.vividus.ui.web.storage;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.html5.WebStorage;
import org.openqa.selenium.remote.Browser;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.RemoteExecuteMethod;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.html5.RemoteWebStorage;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.manager.WebDriverManager;
import org.vividus.ui.web.action.WebJavascriptActions;

public class WebStorageManager
{
    private final IWebDriverProvider webDriverProvider;
    private final WebJavascriptActions javascriptActions;

    public WebStorageManager(IWebDriverProvider webDriverProvider, WebJavascriptActions javascriptActions)
    {
        this.webDriverProvider = webDriverProvider;
        this.javascriptActions = javascriptActions;
    }

    public String getStorageItem(StorageType storageType, String key)
    {
        return storageType == StorageType.LOCAL ? getWebStorage().getLocalStorage().getItem(key)
                : getWebStorage().getSessionStorage().getItem(key);
    }

    public void setStorageItem(StorageType storageType, String key, String value)
    {
        if (storageType == StorageType.LOCAL)
        {
            getWebStorage().getLocalStorage().setItem(key, value);
        }
        else
        {
            getWebStorage().getSessionStorage().setItem(key, value);
        }
    }

    private WebStorage getWebStorage()
    {
        RemoteWebDriver driver = webDriverProvider.getUnwrapped(RemoteWebDriver.class);
        Capabilities capabilities = driver.getCapabilities();
        if (!WebDriverManager.isBrowserAnyOf(capabilities, Browser.SAFARI) && isWebStorageEnabled(capabilities))
        {
            return new RemoteWebStorage(new RemoteExecuteMethod(driver));
        }
        return new JavascriptWebStorage(javascriptActions);
    }

    private static boolean isWebStorageEnabled(Capabilities capabilities)
    {
        Object webStorageEnabled = capabilities.getCapability(CapabilityType.SUPPORTS_WEB_STORAGE);
        return null != webStorageEnabled && (boolean) webStorageEnabled;
    }
}
