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

package org.vividus.ui.web.action.storage;

import javax.inject.Inject;

import org.openqa.selenium.html5.LocalStorage;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.RemoteExecuteMethod;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.html5.RemoteLocalStorage;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.WebDriverType;
import org.vividus.selenium.manager.IWebDriverManager;
import org.vividus.ui.web.action.WebJavascriptActions;

class LocalStorageProvider implements ILocalStorageProvider
{
    @Inject private IWebDriverManager webDriverManager;
    @Inject private IWebDriverProvider webDriverProvider;
    @Inject private WebJavascriptActions javascriptActions;

    @Override
    public LocalStorage getLocalStorage()
    {
        RemoteWebDriver driver = webDriverProvider.getUnwrapped(RemoteWebDriver.class);
        if (!webDriverManager.isTypeAnyOf(WebDriverType.SAFARI) && isWebStorageEnabled(driver))
        {
            return new RemoteLocalStorage(new RemoteExecuteMethod(driver));
        }
        return new JavascriptLocalStorage(javascriptActions);
    }

    private static boolean isWebStorageEnabled(RemoteWebDriver driver)
    {
        Object webStorageEnabled = driver.getCapabilities().getCapability(CapabilityType.SUPPORTS_WEB_STORAGE);
        return null != webStorageEnabled && (boolean) webStorageEnabled;
    }
}
