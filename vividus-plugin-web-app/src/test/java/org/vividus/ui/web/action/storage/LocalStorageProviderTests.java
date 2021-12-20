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

package org.vividus.ui.web.action.storage;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.html5.LocalStorage;
import org.openqa.selenium.remote.Browser;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.html5.RemoteLocalStorage;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.manager.IWebDriverManager;

@ExtendWith(MockitoExtension.class)
class LocalStorageProviderTests
{
    @Mock private IWebDriverManager webDriverManager;
    @Mock private IWebDriverProvider webDriverProvider;
    @InjectMocks private LocalStorageProvider localStorageProvider;

    @Test
    void testGetLocalStorageLocalStorageEnabledIfSafariDriver()
    {
        testGetLocalStorage(Boolean.TRUE, Boolean.TRUE, JavascriptLocalStorage.class);
    }

    @Test
    void testGetLocalStorageLocalStorageEnabledFalseIfSafariDriver()
    {
        testGetLocalStorage(Boolean.TRUE, Boolean.FALSE, JavascriptLocalStorage.class);
    }

    @Test
    void testGetLocalStorageNoCapabilityLocalStorageEnabledIfSafariDriver()
    {
        testGetLocalStorage(Boolean.TRUE, null, JavascriptLocalStorage.class);
    }

    @Test
    void testGetLocalStorageLocalStorageNotEnabledNoCapability()
    {
        testGetLocalStorage(Boolean.FALSE, null, JavascriptLocalStorage.class);
    }

    @Test
    void testGetLocalStorageLocalStorageNotEnabledCapabilityFalse()
    {
        testGetLocalStorage(Boolean.FALSE, Boolean.FALSE, JavascriptLocalStorage.class);
    }

    @Test
    void testGetLocalStorageLocalStorageEnabled()
    {
        testGetLocalStorage(Boolean.FALSE, Boolean.TRUE, RemoteLocalStorage.class);
    }

    private void testGetLocalStorage(Boolean safariDriver, Boolean webStorageEnabled,
            Class<? extends LocalStorage> expectedStorageClass)
    {
        RemoteWebDriver driver = mock(RemoteWebDriver.class);
        Mockito.lenient().when(webDriverProvider.getUnwrapped(RemoteWebDriver.class)).thenReturn(driver);
        Mockito.lenient().when(webDriverManager.isBrowserAnyOf(Browser.SAFARI)).thenReturn(safariDriver);
        Capabilities capabilities = mock(Capabilities.class);
        Mockito.lenient().when(driver.getCapabilities()).thenReturn(capabilities);
        Mockito.lenient().when(capabilities.getCapability(CapabilityType.SUPPORTS_WEB_STORAGE))
                .thenReturn(webStorageEnabled);
        assertTrue(expectedStorageClass.isInstance(localStorageProvider.getLocalStorage()));
    }
}
