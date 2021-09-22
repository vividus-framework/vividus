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

package org.vividus.bdd.mobileapp.steps;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Optional;

import org.jbehave.core.model.ExamplesTable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebElement;
import org.vividus.bdd.steps.ui.validation.IBaseValidations;
import org.vividus.mobileapp.action.KeyboardActions;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.manager.GenericWebDriverManager;
import org.vividus.ui.action.JavascriptActions;
import org.vividus.ui.action.search.Locator;

import io.appium.java_client.android.nativekey.AndroidKey;
import io.appium.java_client.android.nativekey.KeyEvent;
import io.appium.java_client.android.nativekey.PressesKey;

@ExtendWith(MockitoExtension.class)
class KeyboardStepsTests
{
    private static final String TEXT = "text";
    private static final String ELEMENT_TO_TYPE_TEXT = "The element to type text";
    private static final String NAME = "name";
    private static final String MOBILE_PRESS_BUTTON = "mobile: pressButton";

    @Mock private IBaseValidations baseValidations;
    @Mock private KeyboardActions keyboardActions;
    @Mock private Locator locator;
    @Mock private GenericWebDriverManager genericWebDriverManager;
    @Mock private JavascriptActions javascriptActions;
    @Mock private IWebDriverProvider webDriverProvider;
    @InjectMocks private KeyboardSteps keyboardSteps;

    @AfterEach
    void afterEach()
    {
        verifyNoMoreInteractions(keyboardActions, baseValidations, locator);
    }

    @Test
    void testTypeTextInField()
    {
        WebElement element = mock(WebElement.class);
        when(baseValidations.assertElementExists(ELEMENT_TO_TYPE_TEXT, locator)).thenReturn(Optional.of(element));
        keyboardSteps.typeTextInField(TEXT, locator);
        verify(keyboardActions).typeTextAndHide(element, TEXT);
    }

    @Test
    void testTypeTextWithKeepingKeyboardInField()
    {
        WebElement element = mock(WebElement.class);
        when(baseValidations.assertElementExists(ELEMENT_TO_TYPE_TEXT, locator)).thenReturn(Optional.of(element));
        keyboardSteps.typeTextInFieldAndKeepKeyboard(TEXT, locator);
        verify(keyboardActions).typeText(element, TEXT);
    }

    @Test
    void testTypeTextInFieldElementIsEmpty()
    {
        when(baseValidations.assertElementExists(ELEMENT_TO_TYPE_TEXT, locator)).thenReturn(Optional.empty());
        keyboardSteps.typeTextInField(TEXT, locator);
    }

    @Test
    void shouldClearTextInField()
    {
        WebElement element = mock(WebElement.class);
        when(baseValidations.assertElementExists("The element to clear", locator)).thenReturn(Optional.of(element));
        keyboardSteps.clearTextInField(locator);
        verify(keyboardActions).clearText(element);
    }

    @Test
    void shouldPressKeyOnIOS()
    {
        when(genericWebDriverManager.isIOSNativeApp()).thenReturn(true);

        performPressdKeyTest();
    }

    @Test
    void shouldPressIOSKeyOnTvOS()
    {
        when(genericWebDriverManager.isIOSNativeApp()).thenReturn(false);
        when(genericWebDriverManager.isTvOS()).thenReturn(true);

        performPressdKeyTest();
    }

    @Test
    void shouldTypeIOSKeyOnTvOS()
    {
        when(genericWebDriverManager.isIOSNativeApp()).thenReturn(false);
        when(genericWebDriverManager.isTvOS()).thenReturn(true);
        InOrder ordered = Mockito.inOrder(javascriptActions);
        keyboardSteps.typeKeys("home");
        ordered.verify(javascriptActions).executeScript(MOBILE_PRESS_BUTTON, Map.of(NAME, "h"));
        ordered.verify(javascriptActions).executeScript(MOBILE_PRESS_BUTTON, Map.of(NAME, "o"));
        ordered.verify(javascriptActions).executeScript(MOBILE_PRESS_BUTTON, Map.of(NAME, "m"));
        ordered.verify(javascriptActions).executeScript(MOBILE_PRESS_BUTTON, Map.of(NAME, "e"));
        verifyNoInteractions(webDriverProvider);
    }

    private void performPressdKeyTest()
    {
        String key = "Home";

        keyboardSteps.pressKey(key);

        verify(javascriptActions).executeScript(MOBILE_PRESS_BUTTON, Map.of(NAME, key));
        verifyNoInteractions(webDriverProvider);
    }

    @Test
    void shouldPressAndroidKey()
    {
        performPressAndroidKeyTest(() -> keyboardSteps.pressKey(AndroidKey.SPACE.name()));
    }

    @Test
    void shouldPressAndroidKeys()
    {
        performPressAndroidKeyTest(() -> keyboardSteps.pressKeys(new ExamplesTable("|key|\n|SPACE|")));
    }

    @CsvSource(value = { "' ',62", "0, 7", "1, 8", "2, 9", "3, 10", "4, 11", "5, 12", "6, 13", "7, 14", "8, 15",
            "9, 16", "a, 29" })
    @ParameterizedTest
    void shouldTypeAndroidKeys(String key, int expectedCode)
    {
        when(genericWebDriverManager.isAndroid()).thenReturn(true);
        performPressAndroidKeyTest(() -> keyboardSteps.typeKeys(key), expectedCode);
    }

    private void performPressAndroidKeyTest(Runnable run)
    {
        performPressAndroidKeyTest(run, 62);
    }

    private void performPressAndroidKeyTest(Runnable run, int exptectedCode)
    {
        ArgumentCaptor<KeyEvent> keyCaptor = ArgumentCaptor.forClass(KeyEvent.class);
        PressesKey pressesKey = mock(PressesKey.class);
        when(genericWebDriverManager.isIOSNativeApp()).thenReturn(false);
        when(genericWebDriverManager.isTvOS()).thenReturn(false);
        when(webDriverProvider.getUnwrapped(PressesKey.class)).thenReturn(pressesKey);

        run.run();

        verify(pressesKey).pressKey(keyCaptor.capture());

        assertEquals(Map.of("keycode", exptectedCode), keyCaptor.getValue().build());
        verifyNoMoreInteractions(webDriverProvider, genericWebDriverManager);
    }

    @Test
    void shouldNotPressUnsupportedAndroidKey()
    {
        PressesKey pressesKey = mock(PressesKey.class);
        when(genericWebDriverManager.isIOSNativeApp()).thenReturn(false);
        when(webDriverProvider.getUnwrapped(PressesKey.class)).thenReturn(pressesKey);
        when(genericWebDriverManager.isTvOS()).thenReturn(false);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> keyboardSteps.pressKey("unsupported key"));
        assertEquals("Unsupported Android key: unsupported key", exception.getMessage());
        verifyNoMoreInteractions(webDriverProvider, genericWebDriverManager);
        verifyNoInteractions(pressesKey);
    }
}
