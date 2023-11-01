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
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriver.TargetLocator;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.steps.StringComparisonRule;
import org.vividus.steps.ui.validation.IBaseValidations;
import org.vividus.steps.ui.validation.IDescriptiveSoftAssert;
import org.vividus.ui.action.WaitResult;
import org.vividus.ui.action.search.Locator;
import org.vividus.ui.context.IUiContext;
import org.vividus.ui.util.XpathLocatorUtils;
import org.vividus.ui.web.action.FrameActions;
import org.vividus.ui.web.action.IWebWaitActions;
import org.vividus.ui.web.action.IWindowsActions;
import org.vividus.ui.web.action.search.WebLocatorType;

@ExtendWith({ MockitoExtension.class, TestLoggerFactoryExtension.class })
class SetContextStepsTests
{
    private static final String EQUALS_MATCHER = "\"new title\"";
    private static final String NEW_TITLE = "new title";
    private static final String QUOTE = "\"";
    private static final String TITLE = "Title";
    private static final String MATCHER_STRING = "not \"{770e3411-5e19-4831-8f36-fc76e46a2807}\"";
    private static final String WINDOW_OR_TAB_WITH_NAME = "Window or tab name is ";
    private static final String IS_FOUND = " is found";
    private static final String NEW_WINDOW_OR_TAB_IS_FOUND = "New window or browser tab name is ";
    private static final String APOSTROPHE = "'";
    private static final String XPATH = "someXpath";
    private static final String CURRENT_WINDOW_HANDLE = "{770e3411-5e19-4831-8f36-fc76e46a2807}";
    private static final String OTHER_WINDOW_HANDLE = "{248427e8-e67d-47ba-923f-4051f349f813}";
    private static final String NEW_WINDOW_IS_FOUND = "New window is found";
    private static final String NEW_WINDOW = "New window '";
    private static final String A_FRAME = "A frame";
    private static final String FRAME_TO_SWITCH = "The frame to switch context";

    private static final String TOTAL_NUMBER_OF_OPENED_TABS_MESSAGE = "Total number of opened tabs is {}";

    @Mock private IBaseValidations baseValidations;
    @Mock private IUiContext uiContext;
    @Mock private WebElement mockedWebElement;
    @Mock private IWebDriverProvider webDriverProvider;
    @Mock private WebDriver mockedWebDiver;
    @Mock private IDescriptiveSoftAssert softAssert;
    @Mock private FrameActions frameActions;
    @Mock private IWindowsActions windowsActions;
    @Mock private IWebWaitActions waitActions;
    @InjectMocks private SetContextSteps setContextSteps;

    private final TestLogger logger = TestLoggerFactory.getTestLogger(SetContextSteps.class);

    @Test
    void shouldSwitchingToDefault()
    {
        InOrder ordered = inOrder(frameActions, uiContext);

        setContextSteps.switchingToDefault();

        ordered.verify(uiContext).reset();
        ordered.verify(frameActions).switchToRoot();
        ordered.verifyNoMoreInteractions();
    }

    @Test
    void testSwitchingToWindow()
    {
        when(webDriverProvider.get()).thenReturn(mockedWebDiver);
        when(mockedWebDiver.getWindowHandle()).thenReturn(CURRENT_WINDOW_HANDLE);
        when(windowsActions.switchToNewTab(CURRENT_WINDOW_HANDLE)).thenReturn(OTHER_WINDOW_HANDLE);
        when(softAssert.assertThat(eq(NEW_WINDOW + OTHER_WINDOW_HANDLE + APOSTROPHE + IS_FOUND),
                eq(NEW_WINDOW_IS_FOUND), eq(OTHER_WINDOW_HANDLE), argThat(matcher -> matcher.toString()
                        .contains(MATCHER_STRING)))).thenReturn(true);
        setContextSteps.switchingToWindow();

        verify(uiContext).reset();
    }

    @Test
    void testSwitchingToWindowNoWindow()
    {
        when(webDriverProvider.get()).thenReturn(mockedWebDiver);
        when(mockedWebDiver.getWindowHandle()).thenReturn(CURRENT_WINDOW_HANDLE);
        when(windowsActions.switchToNewTab(CURRENT_WINDOW_HANDLE)).thenReturn(CURRENT_WINDOW_HANDLE);
        setContextSteps.switchingToWindow();

        verify(softAssert).assertThat(eq(NEW_WINDOW + CURRENT_WINDOW_HANDLE + APOSTROPHE + IS_FOUND),
                eq(NEW_WINDOW_IS_FOUND), eq(CURRENT_WINDOW_HANDLE), argThat(matcher -> matcher.toString()
                        .contains(MATCHER_STRING)));
    }

    @Test
    void shouldSwitchToNewTab()
    {
        when(webDriverProvider.get()).thenReturn(mockedWebDiver);
        when(mockedWebDiver.getWindowHandle()).thenReturn(CURRENT_WINDOW_HANDLE);
        when(mockedWebDiver.getWindowHandles()).thenReturn(Set.of(CURRENT_WINDOW_HANDLE, OTHER_WINDOW_HANDLE));
        TargetLocator targetLocator = mock();
        when(mockedWebDiver.switchTo()).thenReturn(targetLocator);
        setContextSteps.switchToTab();
        var ordered = inOrder(targetLocator, uiContext);
        ordered.verify(targetLocator).window(OTHER_WINDOW_HANDLE);
        ordered.verify(uiContext).reset();
        assertThat(logger.getLoggingEvents(), is(List.of(
                info(TOTAL_NUMBER_OF_OPENED_TABS_MESSAGE, 2),
                info("Switching to a new tab")
        )));
    }

    @Test
    void testSwitchToTabNoTab()
    {
        when(webDriverProvider.get()).thenReturn(mockedWebDiver);
        when(mockedWebDiver.getWindowHandle()).thenReturn(CURRENT_WINDOW_HANDLE);
        when(mockedWebDiver.getWindowHandles()).thenReturn(Set.of(CURRENT_WINDOW_HANDLE));
        setContextSteps.switchToTab();
        verify(softAssert).recordFailedAssertion("Tab to switch is not found");
        verifyNoInteractions(uiContext);
        assertThat(logger.getLoggingEvents(), is(List.of(
                info(TOTAL_NUMBER_OF_OPENED_TABS_MESSAGE, 1)
        )));
    }

    @Test
    void shouldSwitchAndResetContex()
    {
        when(windowsActions.switchToTabWithMatchingTitle(argThat(matcher -> EQUALS_MATCHER
                .equals(matcher.toString())))).thenReturn(TITLE);
        when(softAssert.assertThat(eq(NEW_WINDOW_OR_TAB_IS_FOUND), eq(WINDOW_OR_TAB_WITH_NAME),
                eq(TITLE), argThat(matcher -> EQUALS_MATCHER.equals(matcher.toString())))).thenReturn(true);

        setContextSteps.switchingToWindow(StringComparisonRule.IS_EQUAL_TO, NEW_TITLE);

        verify(uiContext).reset();
    }

    @Test
    void shouldSwitchToTabAndResetContext()
    {
        when(windowsActions.switchToTabWithMatchingTitle(argThat(matcher -> EQUALS_MATCHER
                .equals(matcher.toString())))).thenReturn(TITLE);
        when(softAssert.assertThat(eq(NEW_WINDOW_OR_TAB_IS_FOUND),
                eq(TITLE), argThat(matcher -> EQUALS_MATCHER.equals(matcher.toString())))).thenReturn(true);

        setContextSteps.switchToTab(StringComparisonRule.IS_EQUAL_TO, NEW_TITLE);

        verify(uiContext).reset();
    }

    @Test
    void testSwitchingToWindowWithPartNameNoNewPage()
    {
        when(windowsActions.switchToTabWithMatchingTitle(argThat(matcher -> matcher.toString()
                .contains(EQUALS_MATCHER)))).thenReturn(TITLE);
        setContextSteps.switchingToWindow(StringComparisonRule.IS_EQUAL_TO, NEW_TITLE);
        verify(softAssert).assertThat(eq(NEW_WINDOW_OR_TAB_IS_FOUND), eq(WINDOW_OR_TAB_WITH_NAME),
                eq(TITLE), argThat(matcher -> matcher.toString().contains(EQUALS_MATCHER)));
        verifyNoInteractions(uiContext);
    }

    @Test
    void testSwitchingToTabWithPartNameNoNewPage()
    {
        when(windowsActions.switchToTabWithMatchingTitle(argThat(matcher -> matcher.toString()
                .contains(EQUALS_MATCHER)))).thenReturn(TITLE);
        setContextSteps.switchToTab(StringComparisonRule.IS_EQUAL_TO, NEW_TITLE);
        verify(softAssert).assertThat(eq(NEW_WINDOW_OR_TAB_IS_FOUND),
                eq(TITLE), argThat(matcher -> matcher.toString().contains(EQUALS_MATCHER)));
        verifyNoInteractions(uiContext);
    }

    @Test
    void testSwitchingToFrameByXpathIfElementExist()
    {
        Locator locator = new Locator(WebLocatorType.XPATH,
                XpathLocatorUtils.getXPath(XPATH));
        when(baseValidations.assertIfElementExists(A_FRAME, locator)).thenReturn(mockedWebElement);
        InOrder ordered = inOrder(frameActions, uiContext);
        setContextSteps.switchingToFrame(locator);
        ordered.verify(uiContext).reset();
        ordered.verify(frameActions).switchToFrame(mockedWebElement);
        ordered.verifyNoMoreInteractions();
    }

    @Test
    void testSwitchingToFrameByXpathIfElementNotExist()
    {
        Locator locator = new Locator(WebLocatorType.XPATH, XpathLocatorUtils.getXPath(XPATH));
        when(baseValidations.assertIfElementExists(A_FRAME, locator)).thenReturn(null);
        setContextSteps.switchingToFrame(locator);
        verifyNoInteractions(frameActions);
    }

    @Test
    void shouldSwitchToFrame()
    {
        Locator locator = mock();
        when(baseValidations.assertElementExists(FRAME_TO_SWITCH, locator)).thenReturn(Optional.of(mockedWebElement));

        setContextSteps.switchToFrame(locator);

        verify(frameActions).switchToFrame(mockedWebElement);
    }

    @Test
    void shouldNotSwitchToFrameIfItDoesnNotExist()
    {
        Locator locator = mock();
        when(baseValidations.assertElementExists(FRAME_TO_SWITCH, locator)).thenReturn(Optional.empty());

        setContextSteps.switchToFrame(locator);

        verifyNoInteractions(frameActions);
    }

    @Test
    void shouldSwitchToAWindowMatchingTheRuleAndResetContext()
    {
        Duration duration = Duration.ofMillis(1);
        when(webDriverProvider.get()).thenReturn(mockedWebDiver);
        WaitResult<Boolean> result = new WaitResult<>();
        result.setWaitPassed(true);
        when(waitActions.wait(eq(mockedWebDiver), eq(duration), waitForWindow())).thenReturn(result);
        when(windowsActions.switchToTabWithMatchingTitle(
            argThat(m -> (QUOTE + TITLE + QUOTE).equals(m.toString()))))
            .thenThrow(new WebDriverException()).thenReturn(TITLE);
        setContextSteps.waitForTabAndSwitch(duration, StringComparisonRule.IS_EQUAL_TO, TITLE);
        verify(uiContext).reset();
    }

    @Test
    void shouldNotResetToAPageIfSwitchNotSucceeded()
    {
        Duration duration = Duration.ofMillis(1);
        when(webDriverProvider.get()).thenReturn(mockedWebDiver);
        WaitResult<Object> result = new WaitResult<>();
        result.setWaitPassed(false);
        when(waitActions.wait(eq(mockedWebDiver), eq(duration), any())).thenReturn(result);
        setContextSteps.waitForTabAndSwitch(duration, StringComparisonRule.IS_EQUAL_TO, TITLE);
        verifyNoInteractions(softAssert);
    }

    private Function<WebDriver, Boolean> waitForWindow()
    {
        return argThat(waiter -> {
            assertFalse(waiter.apply(mockedWebDiver));
            assertTrue(waiter.apply(mockedWebDiver));
            assertEquals("switch to the tab where title is equal to \"Title\"", waiter.toString());
            return true;
        });
    }
}
