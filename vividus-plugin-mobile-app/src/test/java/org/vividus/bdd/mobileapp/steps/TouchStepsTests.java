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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BooleanSupplier;

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
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;
import org.vividus.bdd.mobileapp.model.SwipeDirection;
import org.vividus.bdd.steps.ComparisonRule;
import org.vividus.bdd.steps.ui.validation.IBaseValidations;
import org.vividus.mobileapp.action.KeyboardActions;
import org.vividus.mobileapp.action.TouchActions;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.manager.GenericWebDriverManager;
import org.vividus.ui.action.ISearchActions;
import org.vividus.ui.action.JavascriptActions;
import org.vividus.ui.action.search.Locator;
import org.vividus.ui.action.search.SearchParameters;

import io.appium.java_client.android.nativekey.AndroidKey;
import io.appium.java_client.android.nativekey.KeyEvent;
import io.appium.java_client.android.nativekey.PressesKey;

@ExtendWith(MockitoExtension.class)
class TouchStepsTests
{
    private static final String TEXT = "text";
    private static final String ELEMENT_TO_TAP = "The element to tap";
    private static final String ELEMENT_TO_TYPE_TEXT = "The element to type text";
    private static final String NAME = "name";
    private static final String MOBILE_PRESS_BUTTON = "mobile: pressButton";

    @Mock private IBaseValidations baseValidations;
    @Mock private TouchActions touchActions;
    @Mock private KeyboardActions keyboardActions;
    @Mock private ISearchActions searchActions;
    @Mock private Locator locator;
    @Mock private GenericWebDriverManager genericWebDriverManager;
    @Mock private JavascriptActions javascriptActions;
    @Mock private IWebDriverProvider webDriverProvider;
    @InjectMocks private TouchSteps touchSteps;

    @AfterEach
    void afterEach()
    {
        verifyNoMoreInteractions(touchActions, keyboardActions, baseValidations, searchActions, locator);
    }

    @Test
    void testTapByLocatorWithDuration()
    {
        WebElement element = mock(WebElement.class);
        when(baseValidations.assertElementExists(ELEMENT_TO_TAP, locator)).thenReturn(Optional.of(element));
        touchSteps.tapByLocatorWithDuration(locator, Duration.ZERO);
        verify(touchActions).tap(element, Duration.ZERO);
    }

    @Test
    void testTapByLocatorWithDurationElementIsEmpty()
    {
        when(baseValidations.assertElementExists(ELEMENT_TO_TAP, locator)).thenReturn(Optional.empty());
        touchSteps.tapByLocatorWithDuration(locator, Duration.ZERO);
    }

    @Test
    void testTapByLocator()
    {
        WebElement element = mock(WebElement.class);
        when(baseValidations.assertElementExists(ELEMENT_TO_TAP, locator)).thenReturn(Optional.of(element));
        touchSteps.tapByLocator(locator);
        verify(touchActions).tap(element);
    }

    @Test
    void testTapByLocatorElementIsEmpty()
    {
        when(baseValidations.assertElementExists(ELEMENT_TO_TAP, locator)).thenReturn(Optional.empty());
        touchSteps.tapByLocator(locator);
    }

    @Test
    void testTypeTextInField()
    {
        WebElement element = mock(WebElement.class);
        when(baseValidations.assertElementExists(ELEMENT_TO_TYPE_TEXT, locator)).thenReturn(Optional.of(element));
        touchSteps.typeTextInField(TEXT, locator);
        verify(keyboardActions).typeText(element, TEXT);
    }

    @Test
    void testTypeTextInFieldElementIsEmpty()
    {
        when(baseValidations.assertElementExists(ELEMENT_TO_TYPE_TEXT, locator)).thenReturn(Optional.empty());
        touchSteps.typeTextInField(TEXT, locator);
    }

    @Test
    void shouldClearTextInField()
    {
        WebElement element = mock(WebElement.class);
        when(baseValidations.assertElementExists("The element to clear", locator)).thenReturn(Optional.of(element));
        touchSteps.clearTextInField(locator);
        verify(keyboardActions).clearText(element);
    }

    @ParameterizedTest
    @CsvSource({
        "138, 631",
        "1154, 326"
    })
    void shouldSwipeToElement(int elementY, int endY)
    {
        mockScreenSize();
        WebElement element = mock(WebElement.class);
        mockAssertElementsNumber(List.of(element), true);
        when(element.getLocation()).thenReturn(new Point(-1, elementY));
        SearchParameters parameters = initSwipeMocks();

        when(searchActions.findElements(locator)).thenReturn(new ArrayList<>())
                                                 .thenReturn(List.of())
                                                 .thenReturn(List.of(element));
        doAnswer(a ->
        {
            BooleanSupplier condition = a.getArgument(2, BooleanSupplier.class);
            condition.getAsBoolean();
            condition.getAsBoolean();
            return null;
        }).when(touchActions).swipeUntil(eq(SwipeDirection.UP), eq(Duration.ZERO),
                any(BooleanSupplier.class));

        touchSteps.swipeToElement(SwipeDirection.UP, locator, Duration.ZERO);

        verifyNoMoreInteractions(parameters);
        verify(touchActions).performVerticalSwipe(592, endY, Duration.ZERO);
    }

    @Test
    void shouldSwipeToElementToTryToFindItButThatDoesntExist()
    {
        mockAssertElementsNumber(List.of(), false);
        initSwipeMocks();
        when(searchActions.findElements(locator)).thenReturn(List.of());
        doAnswer(a ->
        {
            BooleanSupplier condition = a.getArgument(2, BooleanSupplier.class);
            condition.getAsBoolean();
            return null;
        }).when(touchActions).swipeUntil(eq(SwipeDirection.UP), eq(Duration.ZERO),
                any(BooleanSupplier.class));

        touchSteps.swipeToElement(SwipeDirection.UP, locator, Duration.ZERO);

        verifyNoInteractions(genericWebDriverManager);
    }

    @Test
    void shouldNotSwipeToElementIfItAlreadyExists()
    {
        mockScreenSize();
        SearchParameters parameters = initSwipeMocks();
        WebElement element = mock(WebElement.class);
        when(element.getLocation()).thenReturn(new Point(-1, 500));
        mockAssertElementsNumber(List.of(element), true);

        when(searchActions.findElements(locator)).thenReturn(List.of(element));

        touchSteps.swipeToElement(SwipeDirection.UP, locator, Duration.ZERO);

        verifyNoMoreInteractions(parameters);
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
        touchSteps.typeKeys("home");
        ordered.verify(javascriptActions).executeScript(MOBILE_PRESS_BUTTON, Map.of(NAME, "h"));
        ordered.verify(javascriptActions).executeScript(MOBILE_PRESS_BUTTON, Map.of(NAME, "o"));
        ordered.verify(javascriptActions).executeScript(MOBILE_PRESS_BUTTON, Map.of(NAME, "m"));
        ordered.verify(javascriptActions).executeScript(MOBILE_PRESS_BUTTON, Map.of(NAME, "e"));
        verifyNoInteractions(webDriverProvider);
    }

    private void performPressdKeyTest()
    {
        String key = "Home";

        touchSteps.pressKey(key);

        verify(javascriptActions).executeScript(MOBILE_PRESS_BUTTON, Map.of(NAME, key));
        verifyNoInteractions(webDriverProvider);
    }

    @Test
    void shouldPressAndroidKey()
    {
        performPressAndroidKeyTest(() -> touchSteps.pressKey(AndroidKey.SPACE.name()));
    }

    @Test
    void shouldPressAndroidKeys()
    {
        performPressAndroidKeyTest(() -> touchSteps.pressKeys(new ExamplesTable("|key|\n|SPACE|")));
    }

    @CsvSource(value = { "' ',62", "0, 7", "1, 8", "2, 9", "3, 10", "4, 11", "5, 12", "6, 13", "7, 14", "8, 15",
            "9, 16", "a, 29" })
    @ParameterizedTest
    void shouldTypeAndroidKeys(String key, int expectedCode)
    {
        when(genericWebDriverManager.isAndroid()).thenReturn(true);
        performPressAndroidKeyTest(() -> touchSteps.typeKeys(key), expectedCode);
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
                () -> touchSteps.pressKey("unsupported key"));
        assertEquals("Unsupported Android key: unsupported key", exception.getMessage());
        verifyNoMoreInteractions(webDriverProvider, genericWebDriverManager);
        verifyNoInteractions(pressesKey);
    }

    private SearchParameters initSwipeMocks()
    {
        SearchParameters parameters = mock(SearchParameters.class);
        when(locator.getSearchParameters()).thenReturn(parameters);
        when(parameters.setWaitForElement(false)).thenReturn(parameters);
        return parameters;
    }

    private void mockAssertElementsNumber(List<WebElement> elements, boolean result)
    {
        when(baseValidations.assertElementsNumber(String.format("The element by locator %s exists", locator),
                elements, ComparisonRule.EQUAL_TO, 1)).thenReturn(result);
    }

    private void mockScreenSize()
    {
        Dimension dimension = mock(Dimension.class);
        when(dimension.getHeight()).thenReturn(1184);
        when(genericWebDriverManager.getSize()).thenReturn(dimension);
    }
}
