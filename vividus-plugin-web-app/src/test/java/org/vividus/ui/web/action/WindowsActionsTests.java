/*
 * Copyright 2019 the original author or authors.
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

package org.vividus.ui.web.action;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.HasCapabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriver.Navigation;
import org.openqa.selenium.WebDriver.Options;
import org.openqa.selenium.WebDriver.TargetLocator;
import org.openqa.selenium.WebDriver.Window;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.manager.IWebDriverManager;

@ExtendWith(MockitoExtension.class)
class WindowsActionsTests
{
    private static final String WINDOW3 = "window3";
    private static final int DIMENSION_SIZE = 100;
    private static final String WINDOW2 = "window2";
    private static final String WINDOW1 = "window1";

    @Mock
    private IWebDriverProvider webDriverProvider;

    @Mock(extraInterfaces = HasCapabilities.class)
    private WebDriver webDriver;

    @Mock
    private IWebDriverManager webDriverManager;

    @InjectMocks
    private WindowsActions windowsActions;

    @Test
    void testCloseAllWindowsExceptOneOneWindow()
    {
        when(webDriverManager.getWindowHandles()).thenReturn(new HashSet<>());
        windowsActions.closeAllWindowsExceptOne();
        verify(webDriverManager, times(1)).getWindowHandles();
        verifyNoMoreInteractions(webDriver);
    }

    @Test
    void testCloseAllWindowsExceptOneNotMobile()
    {
        when(webDriverProvider.get()).thenReturn(webDriver);
        Set<String> windows = new HashSet<>();
        windows.add(WINDOW1);
        windows.add(WINDOW2);
        TargetLocator targetLocator = mock(TargetLocator.class);
        when(webDriverManager.getWindowHandles()).thenReturn(windows);
        when(webDriver.switchTo()).thenReturn(targetLocator);
        Options options = mock(Options.class);
        Window window = mock(Window.class);
        when(webDriver.manage()).thenReturn(options);
        when(options.window()).thenReturn(window);
        when(window.getSize()).thenAnswer(i -> new Dimension(DIMENSION_SIZE, DIMENSION_SIZE));
        when(webDriverManager.isAndroid()).thenReturn(false);
        windowsActions.closeAllWindowsExceptOne();
        verify(webDriver, times(1)).close();
    }

    @Test
    void testCloseAllWindowsExceptOneMobile()
    {
        WebDriver webDriver = mock(WebDriver.class);
        when(webDriverProvider.get()).thenReturn(webDriver);
        when(webDriverManager.isAndroid()).thenReturn(true);
        Set<String> windows = new HashSet<>();
        windows.add(WINDOW1);
        windows.add(WINDOW2);
        TargetLocator targetLocator = mock(TargetLocator.class);
        when(webDriverManager.getWindowHandles()).thenReturn(windows);
        when(webDriver.switchTo()).thenReturn(targetLocator);
        Navigation navigation = mock(Navigation.class);
        when(webDriver.navigate()).thenReturn(navigation);
        windowsActions.closeAllWindowsExceptOne();
        verify(navigation, times(2)).back();
    }

    @Test
    void testSwitchToNewWindow()
    {
        when(webDriverProvider.get()).thenReturn(webDriver);
        TargetLocator targetLocator = mock(TargetLocator.class);
        mockWindowHandles(WINDOW1, WINDOW2);
        when(webDriver.switchTo()).thenReturn(targetLocator);

        assertEquals(WINDOW2, windowsActions.switchToNewWindow(WINDOW1));
        verify(targetLocator).window(WINDOW2);
    }

    @Test
    void testSwitchToNewWindowNoNewWindow()
    {
        when(webDriverProvider.get()).thenReturn(webDriver);
        TargetLocator targetLocator = mock(TargetLocator.class);
        mockWindowHandles(WINDOW1);

        assertEquals(WINDOW1, windowsActions.switchToNewWindow(WINDOW1));
        Mockito.verifyZeroInteractions(targetLocator);
    }

    @Test
    void testSwitchToWindowWithMatchingTitle()
    {
        @SuppressWarnings("unchecked")
        Matcher<String> matcher = mock(Matcher.class);
        TargetLocator targetLocator = mock(TargetLocator.class);
        when(webDriverProvider.get()).thenReturn(webDriver);
        mockWindowHandles(WINDOW3, WINDOW2, WINDOW1);
        when(webDriver.switchTo()).thenReturn(targetLocator).thenReturn(targetLocator);
        when(webDriver.getTitle()).thenReturn(WINDOW3).thenReturn(WINDOW2).thenReturn(WINDOW1);
        when(matcher.matches(WINDOW3)).thenReturn(false);
        when(matcher.matches(WINDOW2)).thenReturn(false);
        when(matcher.matches(WINDOW1)).thenReturn(true);
        InOrder inOrder = Mockito.inOrder(targetLocator, targetLocator);

        assertEquals(WINDOW1, windowsActions.switchToWindowWithMatchingTitle(matcher));
        inOrder.verify(targetLocator).window(WINDOW3);
        inOrder.verify(targetLocator).window(WINDOW2);
        inOrder.verify(targetLocator).window(WINDOW1);
    }

    @Test
    void testSwitchToWindowWithMatchingTitleNoDesiredWindow()
    {
        @SuppressWarnings("unchecked")
        Matcher<String> matcher = mock(Matcher.class);
        TargetLocator targetLocator = mock(TargetLocator.class);
        when(webDriverProvider.get()).thenReturn(webDriver);
        mockWindowHandles(WINDOW3, WINDOW2, WINDOW1);
        when(webDriver.switchTo()).thenReturn(targetLocator).thenReturn(targetLocator);
        when(webDriver.getTitle()).thenReturn(WINDOW3).thenReturn(WINDOW2).thenReturn(WINDOW1);
        when(matcher.matches(WINDOW3)).thenReturn(false);
        when(matcher.matches(WINDOW2)).thenReturn(false);
        when(matcher.matches(WINDOW1)).thenReturn(false);
        InOrder inOrder = Mockito.inOrder(targetLocator, targetLocator, targetLocator);

        assertEquals(WINDOW1, windowsActions.switchToWindowWithMatchingTitle(matcher));
        inOrder.verify(targetLocator).window(WINDOW3);
        inOrder.verify(targetLocator).window(WINDOW2);
        inOrder.verify(targetLocator).window(WINDOW1);
    }

    @Test
    void testSwitchToPreviousWindow()
    {
        when(webDriverProvider.get()).thenReturn(webDriver);
        TargetLocator targetLocator = mock(TargetLocator.class);
        when(webDriverManager.getWindowHandles()).thenReturn(Collections.singleton(WINDOW1));
        when(webDriver.switchTo()).thenReturn(targetLocator).thenReturn(targetLocator);
        windowsActions.switchToPreviousWindow();

        verify(targetLocator).window(WINDOW1);
    }

    private void mockWindowHandles(String... windowHandles)
    {
        when(webDriverManager.getWindowHandles()).thenReturn(new HashSet<>(Arrays.asList(windowHandles)));
    }
}
