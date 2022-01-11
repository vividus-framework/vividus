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

package org.vividus.selenium;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriver.Options;
import org.openqa.selenium.WebDriver.Window;
import org.vividus.selenium.event.WebDriverCreateEvent;
import org.vividus.selenium.manager.IWebDriverManager;

@ExtendWith(MockitoExtension.class)
class BrowserWindowSizeListenerTests
{
    @Mock private IWebDriverManager webDriverManager;
    @Mock private IWebDriverProvider webDriverProvider;
    @Mock private IBrowserWindowSizeProvider browserWindowSizeProvider;
    @InjectMocks private BrowserWindowSizeListener browserWindowSizeListener;

    @Test
    void shouldResizeWindowWithDesiredBrowserSizeOnCreate()
    {
        when(webDriverManager.isElectronApp()).thenReturn(false);
        when(webDriverManager.isMobile()).thenReturn(false);
        WebDriver webDriver = mock(WebDriver.class);
        when(webDriverProvider.get()).thenReturn(webDriver);
        BrowserWindowSize browserWindowSize = mock(BrowserWindowSize.class);
        Options options = mock(Options.class);
        when(webDriver.manage()).thenReturn(options);
        Window window = mock(Window.class);
        when(options.window()).thenReturn(window);
        Dimension dimension = mock(Dimension.class);
        when(browserWindowSize.toDimension()).thenReturn(dimension);
        boolean remoteExecution = true;
        when(webDriverProvider.isRemoteExecution()).thenReturn(remoteExecution);
        when(browserWindowSizeProvider.getBrowserWindowSize(remoteExecution)).thenReturn(browserWindowSize);
        browserWindowSizeListener.onWebDriverCreate(new WebDriverCreateEvent(mock(WebDriver.class)));
        verify(window).setSize(dimension);
    }

    @Test
    void shouldMaximizeWindowWithDesiredBrowserSizeOnCreate()
    {
        when(webDriverManager.isElectronApp()).thenReturn(false);
        when(webDriverManager.isMobile()).thenReturn(false);
        WebDriver webDriver = mock(WebDriver.class);
        when(webDriverProvider.get()).thenReturn(webDriver);
        Options options = mock(Options.class);
        when(webDriver.manage()).thenReturn(options);
        Window window = mock(Window.class);
        when(options.window()).thenReturn(window);
        boolean remoteExecution = true;
        when(webDriverProvider.isRemoteExecution()).thenReturn(remoteExecution);
        when(browserWindowSizeProvider.getBrowserWindowSize(remoteExecution)).thenReturn(null);
        browserWindowSizeListener.onWebDriverCreate(new WebDriverCreateEvent(mock(WebDriver.class)));
        verify(window).maximize();
    }

    @Test
    void shouldDoNothingForElectronApps()
    {
        when(webDriverManager.isElectronApp()).thenReturn(true);
        browserWindowSizeListener.onWebDriverCreate(new WebDriverCreateEvent(mock(WebDriver.class)));
        verifyNoInteractions(webDriverProvider);
    }

    @Test
    void shouldDoNothingForMobile()
    {
        when(webDriverManager.isElectronApp()).thenReturn(false);
        when(webDriverManager.isMobile()).thenReturn(true);
        browserWindowSizeListener.onWebDriverCreate(new WebDriverCreateEvent(mock(WebDriver.class)));
        verifyNoInteractions(webDriverProvider);
    }
}
