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

package org.vividus.steps.ui.web;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriver.Options;
import org.openqa.selenium.WebDriver.Window;
import org.vividus.selenium.BrowserWindowSize;
import org.vividus.selenium.BrowserWindowSizeManager;
import org.vividus.selenium.IBrowserWindowSizeProvider;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.softassert.ISoftAssert;

@ExtendWith(MockitoExtension.class)
class WindowStepsTests
{
    private static final int WIDTH = 640;
    private static final int HEIGHT = 320;
    private static final int SCREEN_WIDTH = 1440;
    private static final int SCREEN_HEIGHT = 900;
    private static final String ASSERTION_PATTERN =
            "The desired browser window size %dx%d fits the screen size (1440x900)";

    @Mock private IWebDriverProvider webDriverProvider;
    @Mock private IBrowserWindowSizeProvider browserWindowSizeProvider;
    @Mock private ISoftAssert softAssert;
    @InjectMocks private BrowserWindowSizeManager browserWindowSizeManager;

    private WindowSteps windowSteps;

    @BeforeEach
    void beforeEach()
    {
        windowSteps = new WindowSteps(browserWindowSizeManager, softAssert);
    }

    @Test
    void testResizeCurrentWindow()
    {
        BrowserWindowSize targetSize = new BrowserWindowSize(WIDTH, HEIGHT);
        BrowserWindowSize screenSize = new BrowserWindowSize(SCREEN_WIDTH, SCREEN_HEIGHT);
        String assertionMessage = String.format(ASSERTION_PATTERN, WIDTH, HEIGHT);
        Window window = mockWindow();
        when(browserWindowSizeProvider.getMaximumBrowserWindowSize(false))
                .thenReturn(Optional.of(screenSize));
        when(softAssert.assertTrue(assertionMessage, true)).thenReturn(true);
        windowSteps.resizeCurrentWindow(targetSize);
        verify(softAssert).assertTrue(assertionMessage, true);
        verify(window).setSize(argThat(dimention -> WIDTH == dimention.getWidth() && HEIGHT == dimention.getHeight()));
    }

    @Test
    void testResizeCurrentWindowSmallScreen()
    {
        int width = 2048;
        int height = 1080;
        BrowserWindowSize targetSize = new BrowserWindowSize(width, height);
        BrowserWindowSize screenSize = new BrowserWindowSize(SCREEN_WIDTH, SCREEN_HEIGHT);
        String assertionMessage = String.format(ASSERTION_PATTERN, width, height);
        Window window = mockWindow();
        when(browserWindowSizeProvider.getMaximumBrowserWindowSize(false))
                .thenReturn(Optional.of(screenSize));
        windowSteps.resizeCurrentWindow(targetSize);
        verify(softAssert).assertTrue(assertionMessage, false);
        verifyNoInteractions(window);
    }

    @Test
    void testResizeCurrentWindowNoScreenSize()
    {
        BrowserWindowSize targetSize = new BrowserWindowSize(WIDTH, HEIGHT);
        Window window = mockWindow();
        when(browserWindowSizeProvider.getMaximumBrowserWindowSize(false))
                .thenReturn(Optional.empty());
        windowSteps.resizeCurrentWindow(targetSize);
        verifyNoInteractions(softAssert);
        verify(window).setSize(argThat(dimension -> WIDTH == dimension.getWidth() && HEIGHT == dimension.getHeight()));
    }

    private Window mockWindow()
    {
        WebDriver mockedWebDriver = mock(WebDriver.class);
        when(webDriverProvider.get()).thenReturn(mockedWebDriver);
        Options mockedOptions = mock(Options.class);
        when(mockedWebDriver.manage()).thenReturn(mockedOptions);
        Window mockedWindow = mock(Window.class);
        when(mockedOptions.window()).thenReturn(mockedWindow);
        return mockedWindow;
    }
}
