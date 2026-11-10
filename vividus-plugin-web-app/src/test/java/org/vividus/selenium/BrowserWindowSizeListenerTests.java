/*
 * Copyright 2019-2025 the original author or authors.
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

package org.vividus.selenium;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriver.Options;
import org.openqa.selenium.WebDriver.Window;
import org.vividus.selenium.event.WebDriverCreateEvent;
import org.vividus.selenium.manager.IWebDriverManager;

@ExtendWith(MockitoExtension.class)
class BrowserWindowSizeListenerTests
{
    @Mock private IWebDriverManager webDriverManager;

    @Test
    void shouldMaximize()
    {
        when(webDriverManager.isElectronApp()).thenReturn(false);
        when(webDriverManager.isMobile()).thenReturn(false);
        WebDriver webDriver = mock();
        Options mockedOptions = mock();
        when(webDriver.manage()).thenReturn(mockedOptions);
        Window mockedWindow = mock();
        when(mockedOptions.window()).thenReturn(mockedWindow);
        var event = new WebDriverCreateEvent(webDriver);
        new BrowserWindowSizeListener(true, webDriverManager).onWebDriverCreate(event);
        verify(mockedWindow).maximize();
    }

    @Test
    void shouldDoNothingForElectronApps()
    {
        when(webDriverManager.isElectronApp()).thenReturn(true);
        WebDriverCreateEvent event = mock();
        new BrowserWindowSizeListener(true, webDriverManager).onWebDriverCreate(event);
        verifyNoInteractions(event);
    }

    @Test
    void shouldDoNothingForMobile()
    {
        when(webDriverManager.isElectronApp()).thenReturn(false);
        when(webDriverManager.isMobile()).thenReturn(true);
        WebDriverCreateEvent event = mock();
        new BrowserWindowSizeListener(true, webDriverManager).onWebDriverCreate(event);
        verifyNoInteractions(event);
    }

    @Test
    void shouldDoNothingWhenMaximizeWindowOnStartOptionIsDisabled()
    {
        WebDriverCreateEvent event = mock();
        new BrowserWindowSizeListener(false, webDriverManager).onWebDriverCreate(event);
        verifyNoInteractions(webDriverManager, event);
    }
}
