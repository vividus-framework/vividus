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

package org.vividus.bdd.steps.ui.web.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.function.BooleanSupplier;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebElement;
import org.vividus.bdd.steps.ui.validation.IDescriptiveSoftAssert;
import org.vividus.bdd.steps.ui.web.Dimension;
import org.vividus.ui.context.IUiContext;
import org.vividus.ui.web.action.WebElementActions;

@ExtendWith(MockitoExtension.class)
class ElementValidationsTests
{
    private static final String ELEMENT_CONTAINS_TEXT = "Element contains text";
    private static final String STRING_ARG = "stringArg";
    private static final int DIMENSION_VALUE = 100;
    private static final int CORRECT_WIDTH_VALUE = 80;

    @Mock
    private WebElementActions webElementActions;

    @Mock
    private WebElement mockedWebElement;

    @Mock
    private org.openqa.selenium.Dimension seleniumDimension;

    @Mock
    private IDescriptiveSoftAssert descriptiveSoftAssert;

    @Mock
    private IUiContext uiContext;

    @InjectMocks
    private ElementValidations elementValidations;

    @Test
    void testAssertIfElementContainsTextOverload()
    {
        boolean result = elementValidations.assertIfElementContainsText(null, STRING_ARG, true);
        assertFalse(result);
    }

    @Test
    void testAssertIfElementContainsTextSuccess()
    {
        when(mockedWebElement.getText()).thenReturn(STRING_ARG);
        when(descriptiveSoftAssert.assertThat(eq(ELEMENT_CONTAINS_TEXT), eq(STRING_ARG), any())).thenReturn(true);
        mockAssertingWebElements(List.of(mockedWebElement));
        boolean result = elementValidations.assertIfElementContainsText(mockedWebElement, STRING_ARG, true);
        assertTrue(result);
    }

    @Test
    void testAssertIfElementContainsTextSuccessInPseudoElement()
    {
        when(mockedWebElement.getText()).thenReturn("");
        when(descriptiveSoftAssert.assertThat(eq(ELEMENT_CONTAINS_TEXT), eq(STRING_ARG), any())).thenReturn(true);
        when(webElementActions.getPseudoElementContent(mockedWebElement)).thenReturn(STRING_ARG);
        mockAssertingWebElements(List.of(mockedWebElement));
        boolean result = elementValidations.assertIfElementContainsText(mockedWebElement, STRING_ARG, true);
        assertTrue(result);
    }

    @Test
    void testAssertIfElementDoesNotContainTextNullWebElement()
    {
        boolean result = elementValidations.assertIfElementContainsText(null, STRING_ARG, false);
        assertFalse(result);
    }

    @Test
    void testAssertIfElementDoesNotContainTextMatches()
    {
        when(mockedWebElement.getText()).thenReturn(STRING_ARG);
        when(webElementActions.getPseudoElementContent(mockedWebElement)).thenReturn("''");
        mockAssertingWebElements(List.of(mockedWebElement));
        boolean result = elementValidations.assertIfElementContainsText(mockedWebElement, STRING_ARG, false);
        assertFalse(result);
    }

    @Test
    void testAssertAllElementsHaveEqualDimensionHeight()
    {
        List<WebElement> mockedWebElements = List.of(mockedWebElement, mockedWebElement);
        when(mockedWebElement.getSize()).thenReturn(seleniumDimension);
        when(seleniumDimension.getHeight()).thenReturn(DIMENSION_VALUE);
        Dimension dimension = Dimension.HEIGHT;
        when(descriptiveSoftAssert.assertEquals(anyString(), anyLong(), anyLong())).thenReturn(true);
        mockAssertingWebElements(mockedWebElements);
        boolean result = elementValidations.assertAllWebElementsHaveEqualDimension(mockedWebElements, dimension);
        assertTrue(result);
    }

    @Test
    void testAssertAllElementsHaveEqualDimensionHeightOnlyOneElement()
    {
        when(mockedWebElement.getSize()).thenReturn(seleniumDimension);
        when(seleniumDimension.getHeight()).thenReturn(DIMENSION_VALUE);
        Dimension dimension = Dimension.HEIGHT;
        boolean result = elementValidations.assertAllWebElementsHaveEqualDimension(
                List.of(mockedWebElement), dimension);
        assertFalse(result);
    }

    @Test
    void testAssertIfElementHasWidthInPercWithTwoArgsSuccess()
    {
        boolean result = ifElementHasWidthInPerc(true);
        assertTrue(result);
    }

    @Test
    void testAssertIfElementHasWidthInPercWithTwoArgsFail()
    {
        boolean result = ifElementHasWidthInPerc(false);
        assertFalse(result);
    }

    @Test
    void testAssertIfElementHasWidthInPercNullParentElement()
    {
        WebElement mockedChildElement = mock(WebElement.class);
        assertFalse(
                elementValidations.assertIfElementHasWidthInPerc(null, mockedChildElement, CORRECT_WIDTH_VALUE));
    }

    @Test
    void testAssertIfElementHasWidthInPercNullChildElement()
    {
        WebElement mockedParentElement = mock(WebElement.class);
        assertFalse(
                elementValidations.assertIfElementHasWidthInPerc(mockedParentElement, null, CORRECT_WIDTH_VALUE));
    }

    @Test
    void testAssertIfElementHasWidthInPercNullElements()
    {
        assertFalse(elementValidations.assertIfElementHasWidthInPerc(null, null, CORRECT_WIDTH_VALUE));
    }

    @Test
    void testAssertIfElementContainsTextFailed()
    {
        when(mockedWebElement.getText()).thenReturn(STRING_ARG);
        when(webElementActions.getPseudoElementContent(mockedWebElement)).thenReturn("");
        mockAssertingWebElements(List.of(mockedWebElement));
        boolean result = elementValidations.assertIfElementContainsText(mockedWebElement, "String", true);
        assertFalse(result);
    }

    private boolean ifElementHasWidthInPerc(boolean elementHasCorrectWidth)
    {
        WebElement mockedParentElement = mock(WebElement.class);
        WebElement mockedChildElement = mock(WebElement.class);
        org.openqa.selenium.Dimension mockedParentDimension = mock(org.openqa.selenium.Dimension.class);
        org.openqa.selenium.Dimension mockedChildDimension = mock(org.openqa.selenium.Dimension.class);
        when(mockedParentElement.getSize()).thenReturn(mockedParentDimension);
        when(mockedChildElement.getSize()).thenReturn(mockedChildDimension);
        when(mockedParentDimension.getWidth()).thenReturn(1000);
        when(mockedChildDimension.getWidth()).thenReturn(800);
        when(descriptiveSoftAssert.assertEquals("Element has correct width", CORRECT_WIDTH_VALUE,
                CORRECT_WIDTH_VALUE, 2)).thenReturn(elementHasCorrectWidth);
        mockAssertingWebElements(List.of(mockedParentElement, mockedChildElement));
        return elementValidations.assertIfElementHasWidthInPerc(mockedParentElement, mockedChildElement,
                CORRECT_WIDTH_VALUE);
    }

    private void mockAssertingWebElements(List<WebElement> elements)
    {
        doAnswer(a ->
        {
            BooleanSupplier supplier = a.getArgument(1, BooleanSupplier.class);
            return supplier.getAsBoolean();
        }).when(uiContext).withAssertingWebElements(eq(elements), any());
    }
}
