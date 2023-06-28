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

package org.vividus.ui.web.action;

import static com.github.valfirst.slf4jtest.LoggingEvent.info;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

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

@ExtendWith({ MockitoExtension.class, TestLoggerFactoryExtension.class })
class WindowsActionsTests
{
    private static final String BROWSER_TAB3 = "tab3";
    private static final int DIMENSION_SIZE = 100;
    private static final String BROWSER_TAB2 = "tab2";
    private static final String BROWSER_TAB1 = "tab1";

    private final TestLogger logger = TestLoggerFactory.getTestLogger(WindowsActions.class);

    @Mock
    private IWebDriverProvider webDriverProvider;

    @Mock(extraInterfaces = HasCapabilities.class)
    private WebDriver webDriver;

    @Mock
    private IWebDriverManager webDriverManager;

    @InjectMocks
    private WindowsActions windowsActions;

    @Test
    void testCloseAllTabsExceptOneOneTab()
    {
        when(webDriverProvider.get()).thenReturn(webDriver);
        when(webDriver.getWindowHandles()).thenReturn(new HashSet<>());
        windowsActions.closeAllTabsExceptOne();
        verify(webDriver).getWindowHandles();
    }

    @Test
    void testCloseAllTabsExceptOneNotMobile()
    {
        when(webDriverProvider.get()).thenReturn(webDriver);
        Set<String> windows = new LinkedHashSet<>(List.of(BROWSER_TAB1, BROWSER_TAB2));
        TargetLocator targetLocator = mock(TargetLocator.class);
        when(webDriver.getWindowHandles()).thenReturn(windows);
        when(webDriver.switchTo()).thenReturn(targetLocator);
        Options options = mock(Options.class);
        Window window = mock(Window.class);
        when(webDriver.manage()).thenReturn(options);
        when(options.window()).thenReturn(window);
        when(window.getSize()).thenAnswer(i -> new Dimension(DIMENSION_SIZE, DIMENSION_SIZE));
        when(webDriverManager.isAndroid()).thenReturn(false);
        windowsActions.closeAllTabsExceptOne();
        verify(webDriver, times(1)).close();
    }

    @Test
    void testCloseAllTabsExceptOneMobile()
    {
        WebDriver webDriver = mock(WebDriver.class);
        when(webDriverProvider.get()).thenReturn(webDriver);
        when(webDriverManager.isAndroid()).thenReturn(true);
        Set<String> windows = new LinkedHashSet<>(List.of(BROWSER_TAB1, BROWSER_TAB2));
        TargetLocator targetLocator = mock(TargetLocator.class);
        when(webDriver.getWindowHandles()).thenReturn(windows);
        when(webDriver.switchTo()).thenReturn(targetLocator);
        Navigation navigation = mock(Navigation.class);
        when(webDriver.navigate()).thenReturn(navigation);
        windowsActions.closeAllTabsExceptOne();
        verify(navigation, times(2)).back();
    }

    @Test
    void testSwitchToNewTab()
    {
        when(webDriverProvider.get()).thenReturn(webDriver);
        TargetLocator targetLocator = mock(TargetLocator.class);
        mockWindowHandles(BROWSER_TAB1, BROWSER_TAB2);
        when(webDriver.switchTo()).thenReturn(targetLocator);

        assertEquals(BROWSER_TAB2, windowsActions.switchToNewTab(BROWSER_TAB1));
        verify(targetLocator).window(BROWSER_TAB2);
    }

    @Test
    void testSwitchToNewTabNoNewTab()
    {
        when(webDriverProvider.get()).thenReturn(webDriver);
        TargetLocator targetLocator = mock(TargetLocator.class);
        mockWindowHandles(BROWSER_TAB1);

        assertEquals(BROWSER_TAB1, windowsActions.switchToNewTab(BROWSER_TAB1));
        Mockito.verifyNoInteractions(targetLocator);
    }

    @Test
    void testSwitchToTabWithMatchingTitle()
    {
        @SuppressWarnings("unchecked")
        Matcher<String> matcher = mock(Matcher.class);
        TargetLocator targetLocator = mock(TargetLocator.class);
        when(webDriverProvider.get()).thenReturn(webDriver);
        mockWindowHandles(BROWSER_TAB3, BROWSER_TAB2, BROWSER_TAB1);
        when(webDriver.switchTo()).thenReturn(targetLocator).thenReturn(targetLocator);
        when(webDriver.getTitle()).thenReturn(BROWSER_TAB3).thenReturn(BROWSER_TAB2).thenReturn(BROWSER_TAB1);
        when(matcher.matches(BROWSER_TAB3)).thenReturn(false);
        when(matcher.matches(BROWSER_TAB2)).thenReturn(false);
        when(matcher.matches(BROWSER_TAB1)).thenReturn(true);
        InOrder inOrder = Mockito.inOrder(targetLocator, targetLocator);

        assertEquals(BROWSER_TAB1, windowsActions.switchToTabWithMatchingTitle(matcher));
        inOrder.verify(targetLocator).window(BROWSER_TAB3);
        inOrder.verify(targetLocator).window(BROWSER_TAB2);
        inOrder.verify(targetLocator).window(BROWSER_TAB1);
    }

    @Test
    void testSwitchToTavWithMatchingTitleNoDesiredTab()
    {
        @SuppressWarnings("unchecked")
        Matcher<String> matcher = mock(Matcher.class);
        TargetLocator targetLocator = mock(TargetLocator.class);
        when(webDriverProvider.get()).thenReturn(webDriver);
        mockWindowHandles(BROWSER_TAB3, BROWSER_TAB2, BROWSER_TAB1);
        when(webDriver.switchTo()).thenReturn(targetLocator).thenReturn(targetLocator);
        when(webDriver.getTitle()).thenReturn(BROWSER_TAB3).thenReturn(BROWSER_TAB2).thenReturn(BROWSER_TAB1);
        when(matcher.matches(BROWSER_TAB3)).thenReturn(false);
        when(matcher.matches(BROWSER_TAB2)).thenReturn(false);
        when(matcher.matches(BROWSER_TAB1)).thenReturn(false);
        InOrder inOrder = Mockito.inOrder(targetLocator, targetLocator, targetLocator);

        assertEquals(BROWSER_TAB1, windowsActions.switchToTabWithMatchingTitle(matcher));
        inOrder.verify(targetLocator).window(BROWSER_TAB3);
        inOrder.verify(targetLocator).window(BROWSER_TAB2);
        inOrder.verify(targetLocator).window(BROWSER_TAB1);
        String switchMessage = "Switching to a tab \"{}\"";
        String titleMessage = "Switched to a tab with the title: \"{}\"";
        assertThat(logger.getLoggingEvents(), is(List.of(info(switchMessage, BROWSER_TAB3),
                                                         info(titleMessage, BROWSER_TAB3),
                                                         info(switchMessage, BROWSER_TAB2),
                                                         info(titleMessage, BROWSER_TAB2),
                                                         info(switchMessage, BROWSER_TAB1),
                                                         info(titleMessage, BROWSER_TAB1))));
    }

    @Test
    void testSwitchToPreviousTab()
    {
        when(webDriverProvider.get()).thenReturn(webDriver);
        TargetLocator targetLocator = mock(TargetLocator.class);
        when(webDriver.getWindowHandles()).thenReturn(Set.of(BROWSER_TAB1));
        when(webDriver.switchTo()).thenReturn(targetLocator).thenReturn(targetLocator);
        windowsActions.switchToPreviousTab();

        verify(targetLocator).window(BROWSER_TAB1);
    }

    private void mockWindowHandles(String... windowHandles)
    {
        when(webDriver.getWindowHandles()).thenReturn(new LinkedHashSet<>(List.of(windowHandles)));
    }
}
