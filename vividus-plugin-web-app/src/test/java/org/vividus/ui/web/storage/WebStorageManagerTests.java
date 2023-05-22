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

package org.vividus.ui.web.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.html5.SessionStorage;
import org.openqa.selenium.remote.Browser;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.RemoteExecuteMethod;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.html5.RemoteWebStorage;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.ui.web.action.WebJavascriptActions;

@ExtendWith(MockitoExtension.class)
class WebStorageManagerTests
{
    private static final String KEY = "key";
    private static final String VALUE = "value";

    @Mock private IWebDriverProvider webDriverProvider;
    @Mock private WebJavascriptActions javascriptActions;
    @InjectMocks private WebStorageManager webStorageManager;

    @Test
    void shouldGetLocalStorageItemForSafari()
    {
        mockDriver(Browser.SAFARI);
        when(javascriptActions.executeScript("return window.localStorage.getItem(arguments[0])", KEY)).thenReturn(
                VALUE);
        String storageItem = webStorageManager.getStorage(StorageType.LOCAL).getItem(KEY);
        assertEquals(VALUE, storageItem);
    }

    @CsvSource({
            "SESSION, sessionStorage",
            "LOCAL,   localStorage"
    })
    @ParameterizedTest
    void shouldSetStorageItemForIE(StorageType storageType, String jsStorageName)
    {
        mockDriver(Browser.IE);
        webStorageManager.getStorage(storageType).setItem(KEY, VALUE);
        verify(javascriptActions).executeScript("window." + jsStorageName + ".setItem(arguments[0], arguments[1])", KEY,
                VALUE);
    }

    @SuppressWarnings("try")
    @Test
    void shouldGetLocalStorageItemForFirefox()
    {
        Map<Class<?>, Object> constructedMocks = new HashMap<>();

        RemoteWebDriver driver = mockDriver(Browser.FIREFOX);
        try (
            var remoteExecuteMethodMockedConstruction = mockConstruction(RemoteExecuteMethod.class, (mock, context) -> {
                assertEquals(1, context.getCount());
                assertEquals(List.of(driver), context.arguments());
                constructedMocks.put(RemoteExecuteMethod.class, mock);
            });
            var remoteWebStorageMockedConstruction = mockConstruction(RemoteWebStorage.class, (mock, context) -> {
                assertEquals(1, context.getCount());
                assertEquals(List.of(constructedMocks.get(RemoteExecuteMethod.class)), context.arguments());

                var sessionStorage = mock(SessionStorage.class);
                when(sessionStorage.getItem(KEY)).thenReturn(VALUE);
                when(mock.getSessionStorage()).thenReturn(sessionStorage);
            })
        )
        {
            assertEquals(VALUE, webStorageManager.getStorage(StorageType.SESSION).getItem(KEY));
        }
    }

    private RemoteWebDriver mockDriver(Browser browser)
    {
        RemoteWebDriver driver = mock(RemoteWebDriver.class);
        when(webDriverProvider.getUnwrapped(RemoteWebDriver.class)).thenReturn(driver);
        MutableCapabilities capabilities = new MutableCapabilities();
        capabilities.setCapability(CapabilityType.BROWSER_NAME, browser.browserName());
        when(driver.getCapabilities()).thenReturn(capabilities);
        return driver;
    }
}
