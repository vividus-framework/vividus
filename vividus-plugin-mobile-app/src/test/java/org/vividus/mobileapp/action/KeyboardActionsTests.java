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

package org.vividus.mobileapp.action;

import static com.github.valfirst.slf4jtest.LoggingEvent.info;
import static com.github.valfirst.slf4jtest.LoggingEvent.warn;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.manager.GenericWebDriverManager;
import org.vividus.ui.action.ISearchActions;
import org.vividus.ui.action.search.Locator;
import org.vividus.ui.action.search.SearchParameters;
import org.vividus.ui.action.search.Visibility;
import org.vividus.ui.mobile.action.search.AppiumLocatorType;

import io.appium.java_client.HidesKeyboard;

@ExtendWith({ MockitoExtension.class, TestLoggerFactoryExtension.class })
class KeyboardActionsTests
{
    private static final String TEXT = "text";
    private static final Locator KEYBOARD_RETURN_LOCATOR = new Locator(AppiumLocatorType.XPATH, new SearchParameters(
        "//XCUIElementTypeKeyboard//XCUIElementTypeButton[@name='Return']", Visibility.VISIBLE, false));

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

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void shouldTypeTextForNotRealDevices(boolean iosNativeApp)
    {
        init(false);

        when(genericWebDriverManager.isIOSNativeApp()).thenReturn(iosNativeApp);
        when(webDriverProvider.getUnwrapped(HidesKeyboard.class)).thenReturn(hidesKeyboard);

        keyboardActions.typeText(element, TEXT);

        verify(element).sendKeys(TEXT);
        verify(hidesKeyboard).hideKeyboard();
        assertThat(logger.getLoggingEvents(), is(List.of(info("Typing text '{}' into the field", TEXT))));
    }

    @Test
    void shouldTypeTextAndHideForIOSRealDevice()
    {
        init(true);

        WebDriver context = mock(WebDriver.class);
        WebElement returnButton = mock(WebElement.class);

        when(genericWebDriverManager.isIOSNativeApp()).thenReturn(true);
        when(webDriverProvider.get()).thenReturn(context);
        when(searchActions.findElement(context, KEYBOARD_RETURN_LOCATOR)).thenReturn(Optional.of(returnButton));

        keyboardActions.typeText(element, TEXT);

        verify(touchActions).tap(returnButton);
        verify(element).sendKeys(TEXT);
    }

    @Test
    void shouldTypeTextAndHideForIOSRealDeviceNoKeyboardFound()
    {
        init(true);

        WebDriver context = mock(WebDriver.class);

        when(genericWebDriverManager.isIOSNativeApp()).thenReturn(true);
        when(webDriverProvider.get()).thenReturn(context);
        when(searchActions.findElement(context, KEYBOARD_RETURN_LOCATOR)).thenReturn(Optional.empty());

        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> keyboardActions.typeText(element, TEXT));
        assertEquals("Unable to find 'Return' button to close the keyboard", exception.getMessage());
    }

    @Test
    void shouldClearText()
    {
        init(false);
        when(genericWebDriverManager.isIOSNativeApp()).thenReturn(true);
        when(webDriverProvider.getUnwrapped(HidesKeyboard.class)).thenReturn(hidesKeyboard);

        keyboardActions.clearText(element);

        verify(element).clear();
        verify(hidesKeyboard).hideKeyboard();
    }

    @Test
    void shouldClearTextButNotCloseKeyboardIfElementIsTypeTextView()
    {
        init(true);
        when(genericWebDriverManager.isIOSNativeApp()).thenReturn(true);
        String typeTextView = "XCUIElementTypeTextView";
        when(element.getTagName()).thenReturn(typeTextView);

        keyboardActions.clearText(element);

        verify(element).clear();
        verifyNoInteractions(hidesKeyboard);
        assertThat(logger.getLoggingEvents(), is(List.of(warn("Skip hiding keyboard for {}. Use the tap step to tap"
            + " outside the {} to hide the keyboard", typeTextView))));
    }

    void init(boolean realDevice)
    {
        keyboardActions = new KeyboardActions(realDevice, touchActions, webDriverProvider,
                genericWebDriverManager, searchActions);
    }
}
