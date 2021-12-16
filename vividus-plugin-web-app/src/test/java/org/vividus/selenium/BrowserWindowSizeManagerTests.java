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

package org.vividus.selenium;

import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriver.Options;
import org.openqa.selenium.WebDriver.Window;

@ExtendWith(MockitoExtension.class)
public class BrowserWindowSizeManagerTests
{
    private static final int WIDTH = 500;
    private static final int HEIGHT = 500;

    @Mock private IWebDriverProvider webDriverProvider;
    @Mock private IBrowserWindowSizeProvider browserWindowSizeProvider;
    @InjectMocks private BrowserWindowSizeManager browserWindowSizeManager;

    @Test
    void shouldResizeWindowNoScreenSize()
    {
        Window window = mockWindow();
        when(browserWindowSizeProvider.getMaximumBrowserWindowSize(false)).thenReturn(Optional.empty());
        browserWindowSizeManager.resizeBrowserWindow(new BrowserWindowSize(WIDTH, HEIGHT), screenSize -> true,
                Window::maximize);
        verify(window).maximize();
    }

    @Test
    void shouldResizeWindowScreenSizePassed()
    {
        Window window = mockWindow();
        when(browserWindowSizeProvider.getMaximumBrowserWindowSize(false))
                .thenReturn(Optional.of(new BrowserWindowSize(100, 100)));
        browserWindowSizeManager.resizeBrowserWindow(new BrowserWindowSize(WIDTH, HEIGHT), screenSize -> true,
                Window::maximize);
        verify(window).setSize(argThat(dimension -> WIDTH == dimension.getWidth() && HEIGHT == dimension.getHeight()));
    }

    @Test
    void shouldResizeWindowScreenSizeFailed()
    {
        Window window = mockWindow();
        when(browserWindowSizeProvider.getMaximumBrowserWindowSize(false))
                .thenReturn(Optional.of(new BrowserWindowSize(100, 100)));
        browserWindowSizeManager.resizeBrowserWindow(new BrowserWindowSize(WIDTH, HEIGHT), screenSize -> false,
                Window::maximize);
        verifyNoInteractions(window);
    }

    @Test
    void shouldResizeWindowNoTarget()
    {
        Window window = mockWindow();
        browserWindowSizeManager.resizeBrowserWindow(null, Window::maximize);
        verify(window).maximize();
    }

    @Test
    void shouldResizeWindowToSpecified()
    {
        Window window = mockWindow();
        browserWindowSizeManager.resizeBrowserWindow(new BrowserWindowSize(WIDTH, HEIGHT), Window::maximize);
        verify(window).setSize(argThat(dimension -> WIDTH == dimension.getWidth() && HEIGHT == dimension.getHeight()));
    }

    private Window mockWindow()
    {
        WebDriver webDriver = mock(WebDriver.class);
        when(webDriverProvider.get()).thenReturn(webDriver);
        Options options = mock(Options.class);
        when(webDriver.manage()).thenReturn(options);
        Window window = mock(Window.class);
        when(options.window()).thenReturn(window);
        return window;
    }
}
