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

package org.vividus.mobileapp.action;

import static com.github.valfirst.slf4jtest.LoggingEvent.info;
import static com.github.valfirst.slf4jtest.LoggingEvent.warn;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.manager.GenericWebDriverManager;
import org.vividus.ui.action.ISearchActions;
import org.vividus.ui.action.search.Locator;
import org.vividus.ui.action.search.SearchParameters;
import org.vividus.ui.action.search.Visibility;
import org.vividus.ui.mobile.action.search.AppiumLocatorType;

import io.appium.java_client.HasOnScreenKeyboard;
import io.appium.java_client.HidesKeyboard;

@ExtendWith({ MockitoExtension.class, TestLoggerFactoryExtension.class })
class KeyboardActionsTests
{
    private static final String TEXT = "text";
    private static final Locator KEYBOARD_RETURN_LOCATOR = new Locator(AppiumLocatorType.XPATH,
            new SearchParameters("(//XCUIElementTypeKeyboard//XCUIElementTypeButton)[last()]", Visibility.VISIBLE,
                    false));
    private static final String XCUIELEMENT_TYPE_TEXT_VIEW = "XCUIElementTypeTextView";

    @Mock private TouchActions touchActions;
    @Mock private IWebDriverProvider webDriverProvider;
    @Mock private GenericWebDriverManager genericWebDriverManager;
    @Mock private ISearchActions searchActions;
    @Mock private WebElement element;
    @Mock private HidesKeyboard hidesKeyboard;

    private KeyboardActions keyboardActions;

    private final TestLogger logger = TestLoggerFactory.getTestLogger(KeyboardActions.class);

    @AfterEach
    void afterEach()
    {
        verifyNoMoreInteractions(hidesKeyboard, searchActions, genericWebDriverManager);
    }

    @Test
    void shouldTypeTextForNotRealDevices()
    {
        init(false);

        when(genericWebDriverManager.isIOSNativeApp()).thenReturn(false);
        when(webDriverProvider.getUnwrapped(HidesKeyboard.class)).thenReturn(hidesKeyboard);

        keyboardActions.typeTextAndHide(element, TEXT);

        verify(element).sendKeys(TEXT);
        verify(hidesKeyboard).hideKeyboard();
        assertThat(logger.getLoggingEvents(), is(List.of(info("Typing text '{}' into the field", TEXT))));
    }

    @Test
    void shouldTypeTextWithoutKeyboardHidingForNotRealDevice()
    {
        init(false);

        keyboardActions.typeText(element, TEXT);

        verify(element).sendKeys(TEXT);
        verifyNoInteractions(hidesKeyboard);
    }

    @Test
    void shouldClearTextInEmptyElement()
    {
        init(false);

        when(genericWebDriverManager.isIOSNativeApp()).thenReturn(true);
        enableOnScreenKeyboard(false);
        when(webDriverProvider.getUnwrapped(HidesKeyboard.class)).thenReturn(hidesKeyboard);

        keyboardActions.clearText(element);

        verify(element).clear();
        verify(hidesKeyboard).hideKeyboard();
        assertThat(logger.getLoggingEvents(), is(empty()));
    }

    @Test
    void shouldTypeTextAndHideForIOSRealDevice()
    {
        init(true);

        WebDriver context = mock(WebDriver.class);
        WebElement returnButton = mock(WebElement.class);

        when(genericWebDriverManager.isIOSNativeApp()).thenReturn(true);
        enableOnScreenKeyboard(true);
        when(webDriverProvider.get()).thenReturn(context);
        when(searchActions.findElements(context, KEYBOARD_RETURN_LOCATOR)).thenReturn(List.of(returnButton));

        keyboardActions.typeTextAndHide(element, TEXT);

        verify(touchActions).tap(returnButton);
        verify(element).sendKeys(TEXT);
    }

    @Test
    void shouldTypeTextAndHideForIOSRealDeviceNoKeyboardFound()
    {
        init(true);

        WebDriver context = mock(WebDriver.class);

        when(genericWebDriverManager.isIOSNativeApp()).thenReturn(true);
        enableOnScreenKeyboard(true);
        when(webDriverProvider.get()).thenReturn(context);
        when(searchActions.findElements(context, KEYBOARD_RETURN_LOCATOR)).thenReturn(List.of());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> keyboardActions.typeTextAndHide(element, TEXT));
        assertEquals("Unable to find a button to close the keyboard", exception.getMessage());
    }

    @Test
    void shouldClearTextButNotCloseKeyboardIfElementIsTypeTextView()
    {
        init(true);
        when(genericWebDriverManager.isIOSNativeApp()).thenReturn(true);
        enableOnScreenKeyboard(true);
        when(element.getTagName()).thenReturn(XCUIELEMENT_TYPE_TEXT_VIEW);

        keyboardActions.clearText(element);

        verify(element).clear();
        verifyNoInteractions(hidesKeyboard);
        assertThat(logger.getLoggingEvents(), is(List.of(warn("Skip hiding keyboard for {}. Use the tap step to tap"
            + " outside the {} to hide the keyboard", XCUIELEMENT_TYPE_TEXT_VIEW))));
    }

    @Test
    void shouldClearTextAndCloseKeyboardIfElementIsTypeTextViewForSimulator()
    {
        init(false);
        when(genericWebDriverManager.isIOSNativeApp()).thenReturn(true);
        enableOnScreenKeyboard(true);
        when(webDriverProvider.getUnwrapped(HidesKeyboard.class)).thenReturn(hidesKeyboard);
        when(element.getTagName()).thenReturn(XCUIELEMENT_TYPE_TEXT_VIEW);

        keyboardActions.clearText(element);

        verify(element).clear();
        verify(hidesKeyboard).hideKeyboard();
    }

    @Test
    void shouldTryHideKeyboardIfElementToTypeTextBecamesStale()
    {
        init(true);

        WebDriver context = mock(WebDriver.class);
        WebElement returnButton = mock(WebElement.class);

        when(genericWebDriverManager.isIOSNativeApp()).thenReturn(true);
        enableOnScreenKeyboard(true);
        when(element.getTagName()).thenThrow(new StaleElementReferenceException("Stale"));
        when(webDriverProvider.get()).thenReturn(context);
        when(searchActions.findElements(context, KEYBOARD_RETURN_LOCATOR)).thenReturn(List.of(returnButton));

        keyboardActions.typeTextAndHide(element, TEXT);

        verify(touchActions).tap(returnButton);
        verify(element).sendKeys(TEXT);
    }

    void init(boolean realDevice)
    {
        keyboardActions = new KeyboardActions(realDevice, touchActions, webDriverProvider,
                genericWebDriverManager, searchActions);
    }

    private void enableOnScreenKeyboard(boolean keyboardShown)
    {
        HasOnScreenKeyboard webDriverWithOnScreenKeyboard = mock(HasOnScreenKeyboard.class);
        when(webDriverProvider.getUnwrapped(HasOnScreenKeyboard.class)).thenReturn(webDriverWithOnScreenKeyboard);
        when(webDriverWithOnScreenKeyboard.isKeyboardShown()).thenReturn(keyboardShown);
    }
}
