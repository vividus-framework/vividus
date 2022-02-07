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
import static org.mockito.ArgumentMatchers.eq;
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
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriver.Options;
import org.openqa.selenium.WebDriver.Window;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.manager.IWebDriverManager;
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
    @Mock private IWebDriverManager webDriverManager;
    @Mock private ISoftAssert softAssert;
    @InjectMocks private WindowSteps windowSteps;

    @Test
    void testResizeCurrentWindow()
    {
        var targetSize = new Dimension(WIDTH, HEIGHT);
        var screenSize = new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT);
        var assertionMessage = String.format(ASSERTION_PATTERN, WIDTH, HEIGHT);
        var window = mockWindow();
        when(webDriverManager.checkWindowFitsScreen(eq(targetSize), argThat(consumer -> {
            consumer.accept(true, screenSize);
            return true;
        }))).thenReturn(Optional.of(true));
        windowSteps.resizeCurrentWindow(targetSize);
        verify(softAssert).assertTrue(assertionMessage, true);
        verify(window).setSize(targetSize);
    }

    @Test
    void testResizeCurrentWindowSmallScreen()
    {
        int width = 2048;
        int height = 1080;
        var targetSize = new Dimension(width, height);
        var screenSize = new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT);
        var assertionMessage = String.format(ASSERTION_PATTERN, width, height);
        when(webDriverManager.checkWindowFitsScreen(eq(targetSize), argThat(consumer -> {
            consumer.accept(false, screenSize);
            return true;
        }))).thenReturn(Optional.of(false));
        windowSteps.resizeCurrentWindow(targetSize);
        verify(softAssert).assertTrue(assertionMessage, false);
        verifyNoInteractions(webDriverProvider);
    }

    @Test
    void testResizeCurrentWindowNoScreenSize()
    {
        var targetSize = new Dimension(WIDTH, HEIGHT);
        var window = mockWindow();
        when(webDriverManager.checkWindowFitsScreen(eq(targetSize), argThat(consumer -> true))).thenReturn(
                Optional.empty());
        windowSteps.resizeCurrentWindow(targetSize);
        verifyNoInteractions(softAssert);
        verify(window).setSize(targetSize);
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
