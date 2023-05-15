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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Keys;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.Browser;
import org.vividus.selenium.manager.IWebDriverManager;
import org.vividus.softassert.ISoftAssert;
import org.vividus.steps.ui.validation.IBaseValidations;
import org.vividus.ui.action.search.Locator;
import org.vividus.ui.action.search.SearchParameters;
import org.vividus.ui.action.search.Visibility;
import org.vividus.ui.web.action.IFieldActions;
import org.vividus.ui.web.action.WebJavascriptActions;
import org.vividus.ui.web.action.search.WebLocatorType;

@ExtendWith(MockitoExtension.class)
class FieldStepsTests
{
    private static final String A_FIELD_WITH_NAME_FIELD_NAME =
            "A field with attributes Field name: 'fieldName'; Visibility: VISIBLE;";
    private static final String FIELD_NAME = "fieldName";
    private static final String TEXT = "text";
    private static final String GET_ELEMENT_VALUE_JS = "return arguments[0].value;";
    private static final String ATTRIBUTES = "An element with attributes";
    private static final String FIELD_TO_CLEAR = "The field to clear";
    private static final String FIELD_TO_ADD_TEXT = "The field to add text";
    private static final String FIELD_TO_ENTER_TEXT = "The field to enter text";

    @Mock private IWebDriverManager webDriverManager;
    @Mock private IFieldActions fieldActions;
    @Mock private WebJavascriptActions javascriptActions;
    @Mock private ISoftAssert softAssert;
    @Mock private IBaseValidations baseValidations;
    @InjectMocks private FieldSteps fieldSteps;
    @Mock private WebElement webElement;

    @Test
    void testDoesNotFieldExist()
    {
        var locator = new Locator(WebLocatorType.FIELD_NAME, new SearchParameters(FIELD_NAME, Visibility.ALL));
        fieldSteps.doesNotFieldExist(locator);
        verify(baseValidations).assertIfElementDoesNotExist(
                "A field with attributes Field name: 'fieldName'; Visibility: ALL;", locator);
    }

    @Test
    void isFieldFound()
    {
        var locator = new Locator(WebLocatorType.FIELD_NAME, FIELD_NAME);
        fieldSteps.findFieldBy(locator);
        verify(baseValidations).assertIfElementExists(A_FIELD_WITH_NAME_FIELD_NAME, locator);
    }

    @Test
    void testEnterTextInFieldNotSafari()
    {
        var locator = mock(Locator.class);
        when(webDriverManager.isBrowserAnyOf(Browser.SAFARI)).thenReturn(false);
        when(baseValidations.assertIfElementExists(ATTRIBUTES + locator, locator)).thenReturn(webElement);
        fieldSteps.enterTextInFieldLocatedBy(TEXT, locator);
        verify(webElement).clear();
        verify(fieldActions).typeText(webElement, TEXT);
    }

    @Test
    void shouldEnterTextInFieldForNotSafariBrowser()
    {
        var locator = mock(Locator.class);
        when(webDriverManager.isBrowserAnyOf(Browser.SAFARI)).thenReturn(false);
        when(baseValidations.assertElementExists(FIELD_TO_ENTER_TEXT, locator)).thenReturn(Optional.of(webElement));
        fieldSteps.enterTextInField(TEXT, locator);
        verify(webElement).clear();
        verify(fieldActions).typeText(webElement, TEXT);
    }

    @Test
    void shouldDoNothingIfFieldToEnterTextIsNotFound()
    {
        var locator = mock(Locator.class);
        when(baseValidations.assertElementExists(FIELD_TO_ENTER_TEXT, locator)).thenReturn(Optional.empty());
        fieldSteps.enterTextInField(TEXT, locator);
    }

    @Test
    void testEnterTextInFieldIExploreRequireWindowFocusFalse()
    {
        var locator = mock(Locator.class);
        Mockito.lenient().when(webDriverManager.isBrowserAnyOf(Browser.IE)).thenReturn(true);
        mockRequireWindowFocusOption(false);
        when(baseValidations.assertIfElementExists(ATTRIBUTES + locator, locator)).thenReturn(webElement);
        fieldSteps.enterTextInFieldLocatedBy(TEXT, locator);
        var inOrder = inOrder(webElement, fieldActions);
        inOrder.verify(webElement).clear();
        inOrder.verify(fieldActions).typeText(webElement, TEXT);
    }

    @Test
    void testEnterTextInFieldIExploreRequireWindowFocusTrueWithoutReentering()
    {
        var locator = mock(Locator.class);
        Mockito.lenient().when(webDriverManager.isBrowserAnyOf(Browser.IE)).thenReturn(true);
        mockRequireWindowFocusOption(true);
        when(baseValidations.assertIfElementExists(ATTRIBUTES + locator, locator))
                .thenReturn(webElement);
        when(javascriptActions.executeScript(GET_ELEMENT_VALUE_JS, webElement)).thenReturn(TEXT);
        fieldSteps.enterTextInFieldLocatedBy(TEXT, locator);
        var inOrder = inOrder(webElement, fieldActions);
        inOrder.verify(webElement).clear();
        inOrder.verify(fieldActions).typeText(webElement, TEXT);
    }

    @Test
    void testEnterTextInFieldIExploreRequireWindowFocusTrueWithReentering()
    {
        var locator = mock(Locator.class);
        Mockito.lenient().when(webDriverManager.isBrowserAnyOf(Browser.IE)).thenReturn(true);
        mockRequireWindowFocusOption(true);
        when(javascriptActions.executeScript(GET_ELEMENT_VALUE_JS, webElement)).thenReturn(StringUtils.EMPTY, TEXT);
        when(baseValidations.assertIfElementExists(ATTRIBUTES + locator, locator)).thenReturn(webElement);
        fieldSteps.enterTextInFieldLocatedBy(TEXT, locator);
        verify(webElement, times(2)).clear();
        verify(fieldActions, times(2)).typeText(webElement, TEXT);
    }

    @Test
    void testEnterTextInFieldIExploreRequireWindowFocusTrueFieldNotFilledCorrectly()
    {
        var locator = mock(Locator.class);
        Mockito.lenient().when(webDriverManager.isBrowserAnyOf(Browser.IE)).thenReturn(true);
        mockRequireWindowFocusOption(true);
        when(javascriptActions.executeScript(GET_ELEMENT_VALUE_JS, webElement)).thenReturn(StringUtils.EMPTY);
        when(baseValidations.assertIfElementExists(ATTRIBUTES + locator, locator)).thenReturn(webElement);
        fieldSteps.enterTextInFieldLocatedBy(TEXT, locator);
        verify(webElement, times(6)).clear();
        verify(fieldActions, times(6)).typeText(webElement, TEXT);
        verify(softAssert).recordFailedAssertion("The element is not filled correctly after 6 typing attempt(s)");
    }

    @Test
    void testEnterTextInFieldIExploreRequireWindowFocusTrueFieldIsFilledCorrectlyAfter5Attempts()
    {
        var locator = mock(Locator.class);
        Mockito.lenient().when(webDriverManager.isBrowserAnyOf(Browser.IE)).thenReturn(true);
        mockRequireWindowFocusOption(true);
        when(javascriptActions.executeScript(GET_ELEMENT_VALUE_JS, webElement)).thenReturn(StringUtils.EMPTY,
                StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY, TEXT);
        when(baseValidations.assertIfElementExists(ATTRIBUTES + locator, locator)).thenReturn(webElement);
        fieldSteps.enterTextInFieldLocatedBy(TEXT, locator);
        verify(webElement, times(6)).clear();
        verify(fieldActions, times(6)).typeText(webElement, TEXT);
        verifyNoInteractions(softAssert);
    }

    @Test
    void testEnterTextInFieldSafariContentEditableFrame()
    {
        var locator = mock(Locator.class);
        when(fieldActions.isElementContenteditable(webElement)).thenReturn(true);
        when(webDriverManager.isBrowserAnyOf(Browser.SAFARI)).thenReturn(true);
        when(baseValidations.assertIfElementExists(ATTRIBUTES + locator, locator)).thenReturn(webElement);
        fieldSteps.enterTextInFieldLocatedBy(TEXT, locator);
        verify(webElement).clear();
        verify(javascriptActions).executeScript("var element = arguments[0];element.innerHTML = arguments[1];",
                webElement, TEXT);
        verify(webElement, never()).sendKeys(TEXT);
    }

    @Test
    void testEnterTextInFieldSafariSimpleFrame()
    {
        var locator = mock(Locator.class);
        when(fieldActions.isElementContenteditable(webElement)).thenReturn(false);
        when(webDriverManager.isBrowserAnyOf(Browser.SAFARI)).thenReturn(true);
        when(baseValidations.assertIfElementExists(ATTRIBUTES + locator, locator)).thenReturn(webElement);
        fieldSteps.enterTextInFieldLocatedBy(TEXT, locator);
        verify(webElement).clear();
        verify(fieldActions).typeText(webElement, TEXT);
    }

    @Test
    void testEnterTextInFieldInNullElement()
    {
        var locator = mock(Locator.class);
        when(baseValidations.assertIfElementExists(ATTRIBUTES + locator, locator)).thenReturn(null);
        fieldSteps.enterTextInFieldLocatedBy(TEXT, locator);
        verifyNoInteractions(fieldActions, javascriptActions, webDriverManager, softAssert);
    }

    @Test
    void shouldEnterTextInFieldWhichBecameStaleOnce()
    {
        var locator = mock(Locator.class);
        when(webDriverManager.isBrowserAnyOf(Browser.SAFARI)).thenReturn(false);
        when(baseValidations.assertIfElementExists(ATTRIBUTES + locator, locator)).thenReturn(webElement);
        doThrow(StaleElementReferenceException.class).doNothing().when(fieldActions).typeText(webElement, TEXT);
        fieldSteps.enterTextInFieldLocatedBy(TEXT, locator);
        verify(webElement, times(2)).clear();
        verify(fieldActions, times(2)).typeText(webElement, TEXT);
    }

    @Test
    void shouldFailToEnterTextInFieldWhichIsAlwaysStale()
    {
        var locator = mock(Locator.class);
        when(webDriverManager.isBrowserAnyOf(Browser.SAFARI)).thenReturn(false);
        when(baseValidations.assertIfElementExists(ATTRIBUTES + locator, locator)).thenReturn(webElement);
        var exception1 = new StaleElementReferenceException("one");
        var exception2 = new StaleElementReferenceException("two");
        doThrow(exception1, exception2).when(fieldActions).typeText(webElement, TEXT);
        var actual = assertThrows(StaleElementReferenceException.class,
                () -> fieldSteps.enterTextInFieldLocatedBy(TEXT, locator));
        assertEquals(exception2, actual);
        verify(webElement, times(2)).clear();
        verify(fieldActions, times(2)).typeText(webElement, TEXT);
    }

    @Test
    void testAddText()
    {
        var locator = new Locator(WebLocatorType.FIELD_NAME, FIELD_NAME);
        when(baseValidations.assertIfElementExists(A_FIELD_WITH_NAME_FIELD_NAME, locator)).thenReturn(webElement);
        fieldSteps.addTextToFieldLocatedBy(TEXT, locator);
        verify(fieldActions).addText(webElement, TEXT);
    }

    @Test
    void testAddTextNullField()
    {
        fieldSteps.addTextToFieldLocatedBy(TEXT, mock(Locator.class));
        verify(webElement, never()).sendKeys(TEXT);
    }

    @Test
    void shouldAddText()
    {
        var locator = new Locator(WebLocatorType.FIELD_NAME, FIELD_NAME);
        when(baseValidations.assertElementExists(FIELD_TO_ADD_TEXT, locator)).thenReturn(Optional.of(webElement));
        fieldSteps.addTextToField(TEXT, locator);
        verify(fieldActions).addText(webElement, TEXT);
    }

    @Test
    void shouldDoNothingIfFieldToAddTextIsNotFound()
    {
        var locator = new Locator(WebLocatorType.FIELD_NAME, FIELD_NAME);
        when(baseValidations.assertElementExists(FIELD_TO_ADD_TEXT, locator)).thenReturn(Optional.empty());
        fieldSteps.addTextToField(TEXT, locator);
        verifyNoInteractions(fieldActions);
    }

    @Test
    void testClearFieldWithName()
    {
        var locator = new Locator(WebLocatorType.FIELD_NAME, FIELD_NAME);
        when(baseValidations.assertIfElementExists(A_FIELD_WITH_NAME_FIELD_NAME, locator)).thenReturn(webElement);
        fieldSteps.clearFieldLocatedBy(locator);
        verify(webElement).clear();
    }

    @Test
    void testClearFieldWithNameNull()
    {
        fieldSteps.clearFieldLocatedBy(mock(Locator.class));
        verify(webElement, never()).clear();
    }

    @Test
    void shouldClearField()
    {
        var locator = new Locator(WebLocatorType.FIELD_NAME, FIELD_NAME);
        when(baseValidations.assertElementExists(FIELD_TO_CLEAR, locator)).thenReturn(Optional.of(webElement));
        fieldSteps.clearField(locator);
        verify(webElement).clear();
    }

    @Test
    void shouldDoNothingIfFieldToClearIsNotFound()
    {
        var locator = new Locator(WebLocatorType.FIELD_NAME, FIELD_NAME);
        when(baseValidations.assertElementExists(FIELD_TO_CLEAR, locator)).thenReturn(Optional.empty());
        fieldSteps.clearField(locator);
    }

    @Test
    void testClearFieldWithNameUsingKeyboard()
    {
        var locator = new Locator(WebLocatorType.FIELD_NAME, FIELD_NAME);
        when(baseValidations.assertIfElementExists(A_FIELD_WITH_NAME_FIELD_NAME, locator)).thenReturn(webElement);
        fieldSteps.clearFieldLocatedByUsingKeyboard(locator);
        verify(fieldActions).clearFieldUsingKeyboard(webElement);
    }

    @Test
    void testClearFieldWithNameUsingKeyboardNull()
    {
        fieldSteps.clearFieldLocatedByUsingKeyboard(mock(Locator.class));
        verify(webElement, never()).sendKeys(Keys.chord(Keys.CONTROL, "a") + Keys.BACK_SPACE);
    }

    @Test
    void shouldClearFieldUsingKeyboard()
    {
        var locator = new Locator(WebLocatorType.FIELD_NAME, FIELD_NAME);
        when(baseValidations.assertElementExists(FIELD_TO_CLEAR, locator)).thenReturn(Optional.of(webElement));
        fieldSteps.clearFieldUsingKeyboard(locator);
        verify(fieldActions).clearFieldUsingKeyboard(webElement);
    }

    @Test
    void shouldDoNothingIfFieldToClearUsingKeyboardIsNotFound()
    {
        var locator = new Locator(WebLocatorType.FIELD_NAME, FIELD_NAME);
        when(baseValidations.assertElementExists(FIELD_TO_CLEAR, locator)).thenReturn(Optional.empty());
        fieldSteps.clearFieldUsingKeyboard(locator);
        verifyNoInteractions(fieldActions);
    }

    private void mockRequireWindowFocusOption(boolean requireWindowFocus)
    {
        Map<String, Object> options = Map.of("requireWindowFocus", requireWindowFocus);
        var capabilities = mock(Capabilities.class);
        when(capabilities.getCapability("se:ieOptions")).thenReturn(options);
        when(webDriverManager.getCapabilities()).thenReturn(capabilities);
    }
}
