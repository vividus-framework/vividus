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

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Keys;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.vividus.bdd.steps.ui.validation.IBaseValidations;
import org.vividus.selenium.WebDriverType;
import org.vividus.selenium.manager.IWebDriverManager;
import org.vividus.softassert.ISoftAssert;
import org.vividus.ui.action.search.Locator;
import org.vividus.ui.action.search.SearchParameters;
import org.vividus.ui.action.search.Visibility;
import org.vividus.ui.web.action.IFieldActions;
import org.vividus.ui.web.action.IJavascriptActions;
import org.vividus.ui.web.action.WebElementActions;
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

    @Mock
    private IBaseValidations baseValidations;

    @Mock
    private WebElementActions webElementActions;

    @Mock
    private WebElement webElement;

    @Mock
    private IFieldActions fieldActions;

    @Mock
    private IJavascriptActions javascriptActions;

    @Mock
    private IWebDriverManager webDriverManager;

    @Mock
    private ISoftAssert softAssert;

    @InjectMocks
    private FieldSteps fieldSteps;

    @Test
    void testDoesNotFieldExist()
    {
        Locator locator = new Locator(WebLocatorType.FIELD_NAME,
                new SearchParameters(FIELD_NAME, Visibility.ALL));
        fieldSteps.doesNotFieldExist(locator);
        verify(baseValidations).assertIfElementDoesNotExist(
                "A field with attributes Field name: 'fieldName'; Visibility: ALL;", locator);
    }

    @Test
    void isFieldFound()
    {
        Locator locator = new Locator(WebLocatorType.FIELD_NAME, FIELD_NAME);
        fieldSteps.findFieldBy(locator);
        verify(baseValidations).assertIfElementExists(A_FIELD_WITH_NAME_FIELD_NAME,
                locator);
    }

    @Test
    void testEnterTextInFieldNotSafari()
    {
        Locator locator = mock(Locator.class);
        when(webDriverManager.isTypeAnyOf(WebDriverType.SAFARI)).thenReturn(false);
        when(baseValidations.assertIfElementExists(ATTRIBUTES + locator, locator))
                .thenReturn(webElement);
        fieldSteps.enterTextInField(TEXT, locator);
        verify(webElement).clear();
        verify(webElement).sendKeys(TEXT);
    }

    @Test
    void testEnterTextInFieldIExploreRequireWindowFocusFalse()
    {
        Locator locator = mock(Locator.class);
        Mockito.lenient().when(webDriverManager.isTypeAnyOf(WebDriverType.IEXPLORE)).thenReturn(true);
        mockRequireWindowFocusOption(false);
        when(baseValidations.assertIfElementExists(ATTRIBUTES + locator, locator))
                .thenReturn(webElement);
        fieldSteps.enterTextInField(TEXT, locator);
        InOrder inOrder = inOrder(webElement);
        inOrder.verify(webElement).clear();
        inOrder.verify(webElement).sendKeys(TEXT);
    }

    @Test
    void testEnterTextInFieldIExploreRequireWindowFocusTrueWithoutReentering()
    {
        Locator locator = mock(Locator.class);
        Mockito.lenient().when(webDriverManager.isTypeAnyOf(WebDriverType.IEXPLORE)).thenReturn(true);
        mockRequireWindowFocusOption(true);
        when(baseValidations.assertIfElementExists(ATTRIBUTES + locator, locator))
                .thenReturn(webElement);
        when(javascriptActions.executeScript(GET_ELEMENT_VALUE_JS, webElement)).thenReturn(TEXT);
        fieldSteps.enterTextInField(TEXT, locator);
        InOrder inOrder = inOrder(webElement);
        inOrder.verify(webElement).clear();
        inOrder.verify(webElement).sendKeys(TEXT);
    }

    @Test
    void testEnterTextInFieldIExploreRequireWindowFocusTrueWithReentering()
    {
        Locator locator = mock(Locator.class);
        Mockito.lenient().when(webDriverManager.isTypeAnyOf(WebDriverType.IEXPLORE)).thenReturn(true);
        mockRequireWindowFocusOption(true);
        when(javascriptActions.executeScript(GET_ELEMENT_VALUE_JS, webElement)).thenReturn(StringUtils.EMPTY, TEXT);
        when(baseValidations.assertIfElementExists(ATTRIBUTES + locator, locator))
                .thenReturn(webElement);
        fieldSteps.enterTextInField(TEXT, locator);
        verify(webElement, times(2)).clear();
        verify(webElement, times(2)).sendKeys(TEXT);
    }

    @Test
    void testEnterTextInFieldIExploreRequireWindowFocusTrueFieldNotFilledCorrectly()
    {
        Locator locator = mock(Locator.class);
        Mockito.lenient().when(webDriverManager.isTypeAnyOf(WebDriverType.IEXPLORE)).thenReturn(true);
        mockRequireWindowFocusOption(true);
        when(javascriptActions.executeScript(GET_ELEMENT_VALUE_JS, webElement)).thenReturn(StringUtils.EMPTY);
        when(baseValidations.assertIfElementExists(ATTRIBUTES + locator, locator))
                .thenReturn(webElement);
        fieldSteps.enterTextInField(TEXT, locator);
        verify(webElement, times(6)).clear();
        verify(webElement, times(6)).sendKeys(TEXT);
        verify(softAssert).recordFailedAssertion("The element is not filled correctly after 6 typing attempt(s)");
    }

    @Test
    void testEnterTextInFieldIExploreRequireWindowFocusTrueFieldIsFilledCorrectlyAfter5Attempts()
    {
        Locator locator = mock(Locator.class);
        Mockito.lenient().when(webDriverManager.isTypeAnyOf(WebDriverType.IEXPLORE)).thenReturn(true);
        mockRequireWindowFocusOption(true);
        when(javascriptActions.executeScript(GET_ELEMENT_VALUE_JS, webElement)).thenReturn(StringUtils.EMPTY,
                StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY, TEXT);
        when(baseValidations.assertIfElementExists(ATTRIBUTES + locator, locator))
                .thenReturn(webElement);
        fieldSteps.enterTextInField(TEXT, locator);
        verify(webElement, times(6)).clear();
        verify(webElement, times(6)).sendKeys(TEXT);
        verifyNoInteractions(softAssert);
    }

    @Test
    void testEnterTextInFieldSafariContentEditableFrame()
    {
        Locator locator = mock(Locator.class);
        when(webElementActions.isElementContenteditable(webElement)).thenReturn(true);
        when(webDriverManager.isTypeAnyOf(WebDriverType.SAFARI)).thenReturn(true);
        when(baseValidations.assertIfElementExists(ATTRIBUTES + locator, locator))
                .thenReturn(webElement);
        fieldSteps.enterTextInField(TEXT, locator);
        verify(webElement).clear();
        verify(javascriptActions).executeScript("var element = arguments[0];element.innerHTML = arguments[1];",
                webElement, TEXT);
        verify(webElement, never()).sendKeys(TEXT);
    }

    @Test
    void testEnterTextInFieldSafariSimpleFrame()
    {
        Locator locator = mock(Locator.class);
        when(webElementActions.isElementContenteditable(webElement)).thenReturn(false);
        when(webDriverManager.isTypeAnyOf(WebDriverType.SAFARI)).thenReturn(true);
        when(baseValidations.assertIfElementExists(ATTRIBUTES + locator, locator))
                .thenReturn(webElement);
        fieldSteps.enterTextInField(TEXT, locator);
        verify(webElement).clear();
        verify(webElement).sendKeys(TEXT);
    }

    @Test
    void testEnterTextInFieldInNullElement()
    {
        Locator locator = mock(Locator.class);
        when(baseValidations.assertIfElementExists(ATTRIBUTES + locator, locator)).thenReturn(null);
        fieldSteps.enterTextInField(TEXT, locator);
        verifyNoInteractions(webElementActions, fieldActions, javascriptActions, webDriverManager,
                softAssert);
    }

    @Test
    void testEnterTextInFieldInStaleElement()
    {
        Locator locator = mock(Locator.class);
        when(webDriverManager.isTypeAnyOf(WebDriverType.SAFARI)).thenReturn(false);
        when(baseValidations.assertIfElementExists(ATTRIBUTES + locator, locator))
                .thenReturn(webElement);
        doThrow(StaleElementReferenceException.class).doNothing().when(webElement).sendKeys(TEXT);
        fieldSteps.enterTextInField(TEXT, locator);
        verify(webElement).clear();
        verify(webElement, times(2)).sendKeys(TEXT);
    }

    @Test
    void testAddText()
    {
        Locator locator = new Locator(WebLocatorType.FIELD_NAME, FIELD_NAME);
        when(baseValidations.assertIfElementExists(A_FIELD_WITH_NAME_FIELD_NAME, locator))
                .thenReturn(webElement);
        fieldSteps.addTextToField(TEXT, locator);
        verify(webElementActions).addText(webElement, TEXT);
    }

    @Test
    void testAddTextNullField()
    {
        fieldSteps.addTextToField(TEXT, mock(Locator.class));
        verify(webElement, never()).sendKeys(TEXT);
    }

    @Test
    void testClearFieldWithName()
    {
        Locator locator = new Locator(WebLocatorType.FIELD_NAME, FIELD_NAME);
        when(baseValidations.assertIfElementExists(A_FIELD_WITH_NAME_FIELD_NAME,
                locator)).thenReturn(webElement);
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
    void testClearFieldWithNameUsingKeyboard()
    {
        Locator locator = new Locator(WebLocatorType.FIELD_NAME, FIELD_NAME);
        when(baseValidations.assertIfElementExists(A_FIELD_WITH_NAME_FIELD_NAME,
                locator)).thenReturn(webElement);
        fieldSteps.clearFieldLocatedByUsingKeyboard(locator);
        verify(fieldActions).clearFieldUsingKeyboard(webElement);
    }

    @Test
    void testClearFieldWithNameUsingKeyboardNull()
    {
        fieldSteps.clearFieldLocatedByUsingKeyboard(mock(Locator.class));
        verify(webElement, never()).sendKeys(Keys.chord(Keys.CONTROL, "a") + Keys.BACK_SPACE);
    }

    private void mockRequireWindowFocusOption(boolean requireWindowFocus)
    {
        Map<String, Object> options = Map.of("requireWindowFocus", requireWindowFocus);
        Capabilities capabilities = mock(Capabilities.class);
        when(capabilities.getCapability("se:ieOptions")).thenReturn(options);
        when(webDriverManager.getCapabilities()).thenReturn(capabilities);
    }
}
