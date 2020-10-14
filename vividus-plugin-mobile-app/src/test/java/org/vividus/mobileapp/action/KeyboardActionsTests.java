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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebElement;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.manager.GenericWebDriverManager;
import org.vividus.ui.action.ISearchActions;
import org.vividus.ui.action.search.Locator;
import org.vividus.ui.action.search.SearchParameters;
import org.vividus.ui.action.search.Visibility;
import org.vividus.ui.mobile.action.search.AppiumLocatorType;

import io.appium.java_client.HidesKeyboard;

@ExtendWith(MockitoExtension.class)
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
    }

    @Test
    void shouldTypeTextAndHideForIOSRealDevice()
    {
        init(true);

        WebElement returnButton = mock(WebElement.class);

        when(genericWebDriverManager.isIOSNativeApp()).thenReturn(true);
        when(searchActions.findElement(KEYBOARD_RETURN_LOCATOR)).thenReturn(Optional.of(returnButton));

        keyboardActions.typeText(element, TEXT);

        verify(touchActions).tap(returnButton);
        verify(element).sendKeys(TEXT);
    }

    @Test
    void shouldTypeTextAndHideForIOSRealDeviceNoKeyboardFound()
    {
        init(true);

        when(genericWebDriverManager.isIOSNativeApp()).thenReturn(true);
        when(searchActions.findElement(KEYBOARD_RETURN_LOCATOR)).thenReturn(Optional.empty());

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

    void init(boolean realDevice)
    {
        keyboardActions = new KeyboardActions(realDevice, touchActions, webDriverProvider,
                genericWebDriverManager, searchActions);
    }
}
