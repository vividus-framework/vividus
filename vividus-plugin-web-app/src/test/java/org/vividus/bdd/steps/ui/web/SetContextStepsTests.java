/*
 * Copyright 2019-2020 the original author or authors.
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

package org.vividus.bdd.steps.ui.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.function.Function;

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
import org.vividus.bdd.steps.StringComparisonRule;
import org.vividus.bdd.steps.ui.validation.IBaseValidations;
import org.vividus.bdd.steps.ui.validation.IDescriptiveSoftAssert;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.ui.State;
import org.vividus.ui.action.WaitResult;
import org.vividus.ui.action.search.Locator;
import org.vividus.ui.context.IUiContext;
import org.vividus.ui.context.SearchContextSetter;
import org.vividus.ui.web.action.IWebWaitActions;
import org.vividus.ui.web.action.IWindowsActions;
import org.vividus.ui.web.action.search.WebLocatorType;
import org.vividus.ui.web.util.LocatorUtil;

@ExtendWith(MockitoExtension.class)
class SetContextStepsTests
{
    private static final String EQUALS_MATCHER = "\"new title\"";
    private static final String NEW_TITLE = "new title";
    private static final String QUOTE = "\"";
    private static final String TITLE = "Title";
    private static final String MATCHER_STRING = "not \"{770e3411-5e19-4831-8f36-fc76e46a2807}\"";
    private static final String AN_ELEMENT_WITH_THE_NAME_NAME = "An element with the name 'name'";
    private static final String AN_ELEMENT_WITH_THE_ATTRIBUTE_ATTRIBUTE_TYPE_ATTRIBUTE_VALUE =
            "An element with the attribute 'attributeType'='attributeValue'";
    private static final String THE_FOUND_ELEMENT_IS = "The found element is ";
    private static final String ATTRIBUTE_TYPE = "attributeType";
    private static final String ATTRIBUTE_VALUE = "attributeValue";
    private static final String XPATH_ATTRIBUTE_VALUE = LocatorUtil.getXPath(".//*[@attributeType=%s]",
            ATTRIBUTE_VALUE);
    private static final String WINDOW_OR_TAB_WITH_NAME = "Window or tab name is ";
    private static final String IS_FOUND = " is found";
    private static final String NEW_WINDOW_OR_TAB_IS_FOUND = "New window or browser tab name is ";
    private static final String NAME = "name";
    private static final String APOSTROPHE = "'";
    private static final String XPATH = "someXpath";
    private static final String CURRENT_WINDOW_HANDLE = "{770e3411-5e19-4831-8f36-fc76e46a2807}";
    private static final String OTHER_WINDOW_HANDLE = "{248427e8-e67d-47ba-923f-4051f349f813}";
    private static final String NEW_WINDOW_IS_FOUND = "New window is found";
    private static final String NEW_WINDOW = "New window '";

    @Mock
    private IBaseValidations mockedBaseValidations;

    @Mock
    private IUiContext uiContext;

    @Mock
    private WebElement mockedWebElement;

    @Mock
    private IWebDriverProvider webDriverProvider;

    @Mock
    private WebDriver mockedWebDiver;

    @Mock
    private TargetLocator mockedTargetLocator;

    @Mock
    private IDescriptiveSoftAssert softAssert;

    @Mock
    private IWindowsActions windowsActions;

    @Mock
    private IWebWaitActions waitActions;

    @InjectMocks
    private SetContextSteps setContextSteps;

    private void verifyContextSetting(WebElement element)
    {
        verify(uiContext).reset();
        verify(uiContext).putSearchContext(eq(element), any(SearchContextSetter.class));
    }

    @Test
    void testChangeContextToElementWithAttribute()
    {
        when(mockedBaseValidations.assertIfElementExists(AN_ELEMENT_WITH_THE_ATTRIBUTE_ATTRIBUTE_TYPE_ATTRIBUTE_VALUE,
                new Locator(WebLocatorType.XPATH, XPATH_ATTRIBUTE_VALUE))).thenReturn(mockedWebElement);
        setContextSteps.changeContextToElementWithAttribute(ATTRIBUTE_TYPE, ATTRIBUTE_VALUE);
        verifyContextSetting(mockedWebElement);
    }

    @Test
    void testChangeContextToStateElementWithAttribute()
    {
        when(mockedBaseValidations.assertIfElementExists(AN_ELEMENT_WITH_THE_ATTRIBUTE_ATTRIBUTE_TYPE_ATTRIBUTE_VALUE,
                new Locator(WebLocatorType.XPATH, XPATH_ATTRIBUTE_VALUE))).thenReturn(mockedWebElement);
        when(mockedBaseValidations.assertElementState(THE_FOUND_ELEMENT_IS + State.ENABLED, State.ENABLED,
                mockedWebElement)).thenReturn(true);
        setContextSteps.changeContextToStateElementWithAttribute(State.ENABLED, ATTRIBUTE_TYPE, ATTRIBUTE_VALUE);
        verifyContextSetting(mockedWebElement);
    }

    @Test
    void testChangeContextToNotElementWithAttribute()
    {
        setContextSteps.changeContextToStateElementWithAttribute(State.ENABLED, ATTRIBUTE_TYPE, ATTRIBUTE_VALUE);
        verifyContextSetting(null);
    }

    @Test
    void testChangeContextToStateElementWithName()
    {
        when(mockedBaseValidations
                .assertIfElementExists(AN_ELEMENT_WITH_THE_NAME_NAME,
                        new Locator(WebLocatorType.ELEMENT_NAME, NAME)
                                .addFilter(WebLocatorType.STATE, State.ENABLED.toString())))
                .thenReturn(mockedWebElement);
        setContextSteps.changeContextToElementWithName(State.ENABLED, NAME);
        verifyContextSetting(mockedWebElement);
    }

    @Test
    void testChangeContextToNotStateElementWithName()
    {
        setContextSteps.changeContextToElementWithName(State.ENABLED, NAME);
        verifyContextSetting(null);
    }

    @Test
    void testSwitchingToWindow()
    {
        when(webDriverProvider.get()).thenReturn(mockedWebDiver);
        when(mockedWebDiver.getWindowHandle()).thenReturn(CURRENT_WINDOW_HANDLE);
        when(windowsActions.switchToNewWindow(CURRENT_WINDOW_HANDLE)).thenReturn(OTHER_WINDOW_HANDLE);
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
        when(windowsActions.switchToNewWindow(CURRENT_WINDOW_HANDLE)).thenReturn(CURRENT_WINDOW_HANDLE);
        setContextSteps.switchingToWindow();

        verify(softAssert).assertThat(eq(NEW_WINDOW + CURRENT_WINDOW_HANDLE + APOSTROPHE + IS_FOUND),
                eq(NEW_WINDOW_IS_FOUND), eq(CURRENT_WINDOW_HANDLE), argThat(matcher -> matcher.toString()
                        .contains(MATCHER_STRING)));
    }

    @Test
    void shouldSwitchAndResetContex()
    {
        when(windowsActions.switchToWindowWithMatchingTitle(argThat(matcher -> matcher.toString()
                .equals(EQUALS_MATCHER)))).thenReturn(TITLE);
        when(softAssert.assertThat(eq(NEW_WINDOW_OR_TAB_IS_FOUND), eq(WINDOW_OR_TAB_WITH_NAME),
                eq(TITLE), argThat(matcher -> matcher.toString().equals(EQUALS_MATCHER)))).thenReturn(true);

        setContextSteps.switchingToWindow(StringComparisonRule.IS_EQUAL_TO, NEW_TITLE);

        verify(uiContext).reset();
    }

    @Test
    void testSwitchingToWindowWithPartNameNoNewPage()
    {
        when(windowsActions.switchToWindowWithMatchingTitle(argThat(matcher -> matcher.toString()
                .contains(EQUALS_MATCHER)))).thenReturn(TITLE);
        setContextSteps.switchingToWindow(StringComparisonRule.IS_EQUAL_TO, NEW_TITLE);
        verify(softAssert).assertThat(eq(NEW_WINDOW_OR_TAB_IS_FOUND), eq(WINDOW_OR_TAB_WITH_NAME),
                eq(TITLE), argThat(matcher -> matcher.toString().contains(EQUALS_MATCHER)));
        verifyNoInteractions(uiContext);
    }

    @Test
    void testSwitchingToFrameByXpathIfElementExist()
    {
        when(webDriverProvider.get()).thenReturn(mockedWebDiver);
        when(mockedWebDiver.switchTo()).thenReturn(mockedTargetLocator);
        Locator locator = new Locator(WebLocatorType.XPATH,
                LocatorUtil.getXPath(XPATH));
        when(mockedBaseValidations.assertIfElementExists("A frame", locator)).thenReturn(mockedWebElement);
        InOrder ordered = inOrder(mockedTargetLocator, uiContext);
        setContextSteps.switchingToFrame(locator);
        ordered.verify(uiContext).reset();
        ordered.verify(mockedTargetLocator).frame(mockedWebElement);
        verify(mockedTargetLocator, never()).defaultContent();
    }

    @Test
    void testSwitchingToFrameByXpathIfElementNotExist()
    {
        Locator locator = new Locator(WebLocatorType.XPATH,
                LocatorUtil.getXPath(XPATH));
        when(mockedWebDiver.switchTo()).thenReturn(mockedTargetLocator);
        setContextSteps.switchingToFrame(locator);
        verify(mockedWebDiver.switchTo(), never()).frame(mockedWebElement);
    }

    @Test
    void shouldSwitchToAWindowMatchingTheRuleAndResetContext()
    {
        Duration duration = Duration.ofMillis(1);
        when(webDriverProvider.get()).thenReturn(mockedWebDiver);
        WaitResult<Boolean> result = new WaitResult<>();
        result.setWaitPassed(true);
        when(waitActions.wait(eq(mockedWebDiver), eq(duration), waitForWindow())).thenReturn(result);
        when(windowsActions.switchToWindowWithMatchingTitle(
            argThat(m -> (QUOTE + TITLE + QUOTE).equals(m.toString()))))
            .thenThrow(new WebDriverException()).thenReturn(TITLE);
        setContextSteps.waitForWindowAndSwitch(duration, StringComparisonRule.IS_EQUAL_TO, TITLE);
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
        setContextSteps.waitForWindowAndSwitch(duration, StringComparisonRule.IS_EQUAL_TO, TITLE);
        verifyNoInteractions(softAssert);
    }

    private Function<WebDriver, Boolean> waitForWindow()
    {
        return argThat(waiter -> {
            assertFalse(waiter.apply(mockedWebDiver));
            assertTrue(waiter.apply(mockedWebDiver));
            assertEquals("switch to a window where title IS_EQUAL_TO \"Title\"", waiter.toString());
            return true;
        });
    }
}
