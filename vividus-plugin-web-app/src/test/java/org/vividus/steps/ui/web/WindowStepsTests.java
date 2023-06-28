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

package org.vividus.steps.ui.web;

import static com.github.valfirst.slf4jtest.LoggingEvent.info;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriver.Options;
import org.openqa.selenium.WebDriver.TargetLocator;
import org.openqa.selenium.WebDriver.Window;
import org.openqa.selenium.WindowType;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.manager.IWebDriverManager;
import org.vividus.softassert.ISoftAssert;
import org.vividus.ui.web.action.IAlertActions;
import org.vividus.ui.web.action.WebJavascriptActions;

@ExtendWith({ MockitoExtension.class, TestLoggerFactoryExtension.class })
class WindowStepsTests
{
    private static final int WIDTH = 640;
    private static final int HEIGHT = 320;
    private static final int SCREEN_WIDTH = 1440;
    private static final int SCREEN_HEIGHT = 900;
    private static final String ASSERTION_PATTERN =
            "The desired browser window size %dx%d fits the screen size (1440x900)";
    private static final String CURRENT_WINDOW_GUID = "{770e3411-5e19-4831-8f36-fc76e46a2807}";
    private static final String WINDOW_TO_SWITCH_TO = "{248427e8-e67d-47ba-923f-4051f349f813}";

    private final TestLogger logger = TestLoggerFactory.getTestLogger(WindowSteps.class);

    @Mock private WebDriver driver;
    @Mock private WebJavascriptActions javascriptActions;
    @Mock private IAlertActions alertActions;
    @Mock private IWebDriverProvider webDriverProvider;
    @Mock private IWebDriverManager webDriverManager;
    @Mock private ISoftAssert softAssert;
    @InjectMocks private WindowSteps windowSteps;

    @Test
    void shouldOpenNewTab()
    {
        when(webDriverProvider.get()).thenReturn(driver);
        var targetLocator = mockTargetLocator();
        windowSteps.openNewTab();
        verify(targetLocator).newWindow(WindowType.TAB);
    }

    @Test
    void testResizeCurrentWindow()
    {
        var targetSize = new Dimension(WIDTH, HEIGHT);
        var screenSize = new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT);
        var assertionMessage = String.format(ASSERTION_PATTERN, WIDTH, HEIGHT);
        var window = mockWindow();
        when(webDriverManager.getScreenResolution()).thenReturn(Optional.of(screenSize));
        when(softAssert.assertTrue(assertionMessage, true)).thenReturn(true);
        windowSteps.resizeCurrentWindow(targetSize);
        verify(window).setSize(targetSize);
    }

    @ParameterizedTest
    @CsvSource({
            "1441, 1080",
            "1440, 901"
    })
    void testResizeCurrentWindowSmallScreen(int targetWidth, int targetHeight)
    {
        var targetSize = new Dimension(targetWidth, targetHeight);
        var screenSize = new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT);
        var assertionMessage = String.format(ASSERTION_PATTERN, targetWidth, targetHeight);
        when(webDriverManager.getScreenResolution()).thenReturn(Optional.of(screenSize));
        when(softAssert.assertTrue(assertionMessage, false)).thenReturn(false);
        windowSteps.resizeCurrentWindow(targetSize);
        verifyNoInteractions(webDriverProvider);
    }

    @Test
    void testResizeCurrentWindowNoScreenSize()
    {
        var targetSize = new Dimension(WIDTH, HEIGHT);
        var window = mockWindow();
        when(webDriverManager.getScreenResolution()).thenReturn(Optional.empty());
        windowSteps.resizeCurrentWindow(targetSize);
        verifyNoInteractions(softAssert);
        verify(window).setSize(targetSize);
    }

    @Test
    void testCloseCurrentWindow()
    {
        mockWindowHandles();
        var targetLocator = mockTargetLocator();
        windowSteps.closeCurrentTab();
        verify(driver).close();
        verify(targetLocator).window(WINDOW_TO_SWITCH_TO);
    }

    @Test
    void testCloseCurrentWindowNoAdditionalWindowIsOpened()
    {
        var currentWindow = CURRENT_WINDOW_GUID;
        when(webDriverProvider.get()).thenReturn(driver);
        when(driver.getWindowHandle()).thenReturn(currentWindow);
        when(driver.getWindowHandles()).thenReturn(Set.of(currentWindow));
        windowSteps.closeCurrentTab();
        verify(softAssert, times(0)).assertThat("New window or browser tab is found",
                driver.getWindowHandles(), contains(currentWindow));
    }

    @Test
    void testCloseCurrentWindowWithAlerts()
    {
        mockWindowHandles();
        when(alertActions.isAlertPresent(driver)).thenReturn(true);
        windowSteps.closeCurrentTabWithAlertsHandling();
        assertThat(logger.getLoggingEvents(),
                is(List.of(info("Alert dialog box is shown and must be handled in the subsequent steps."))));
        verify(javascriptActions).closeCurrentTab();
        verify(driver, never()).switchTo();
    }

    @Test
    void testCloseCurrentWindowWithoutAlerts()
    {
        mockWindowHandles();
        var targetLocator = mockTargetLocator();
        when(alertActions.isAlertPresent(driver)).thenReturn(false);
        windowSteps.closeCurrentTabWithAlertsHandling();
        verify(javascriptActions).closeCurrentTab();
        verify(targetLocator).window(WINDOW_TO_SWITCH_TO);
    }

    private TargetLocator mockTargetLocator()
    {
        var targetLocator = mock(TargetLocator.class);
        when(driver.switchTo()).thenReturn(targetLocator);
        return targetLocator;
    }

    private Window mockWindow()
    {
        var mockedWebDriver = mock(WebDriver.class);
        when(webDriverProvider.get()).thenReturn(mockedWebDriver);
        var mockedOptions = mock(Options.class);
        when(mockedWebDriver.manage()).thenReturn(mockedOptions);
        var mockedWindow = mock(Window.class);
        when(mockedOptions.window()).thenReturn(mockedWindow);
        return mockedWindow;
    }

    private void mockWindowHandles()
    {
        var currentWindow = CURRENT_WINDOW_GUID;
        when(webDriverProvider.get()).thenReturn(driver);
        when(driver.getWindowHandle()).thenReturn(currentWindow);
        Set<String> windowHandles = new LinkedHashSet<>(List.of(currentWindow, WINDOW_TO_SWITCH_TO));
        when(driver.getWindowHandles()).thenReturn(windowHandles);
    }
}
