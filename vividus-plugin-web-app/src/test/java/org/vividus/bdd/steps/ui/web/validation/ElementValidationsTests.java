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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebElement;
import org.vividus.bdd.steps.ui.web.Dimension;
import org.vividus.ui.web.action.WebElementActions;

@ExtendWith(MockitoExtension.class)
class ElementValidationsTests
{
    private static final String TITLE = "title";
    private static final String ELEMENT_CONTAINS_TEXT = "Element contains text";
    private static final String DESCRIPTION = "desc";
    private static final String STRING_ARG = "stringArg";
    private static final int DIMENSION_VALUE = 100;
    private static final int CORRECT_WIDTH_VALUE = 80;
    private static final String BUSINESS_DESCRIPTION = "Test business description";

    @Mock
    private IHighlightingSoftAssert mockedIHighlightingSoftAssert;

    @Mock
    private WebElementActions webElementActions;

    @Mock
    private WebElement mockedWebElement;

    @Mock
    private org.openqa.selenium.Dimension seleniumDimension;

    @Mock
    private IDescriptiveSoftAssert descriptiveSoftAssert;

    @InjectMocks
    private ElementValidations elementValidations;

    @Test
    void testAssertElementNumber()
    {
        List<WebElement> mockedWebElements = List.of(mockedWebElement);
        when(mockedIHighlightingSoftAssert.withHighlightedElements(mockedWebElements))
                .thenReturn(descriptiveSoftAssert);
        elementValidations.assertElementNumber(BUSINESS_DESCRIPTION, DESCRIPTION, mockedWebElements, any());
        verify(descriptiveSoftAssert).assertThat(eq(BUSINESS_DESCRIPTION), eq(DESCRIPTION), eq(
                mockedWebElements),
                any());
    }

    @Test
    void testAssertIfElementContainsTextOverload()
    {
        boolean result = elementValidations.assertIfElementContainsText(null, STRING_ARG, true);
        assertFalse(result);
    }

    @Test
    void testAssertIfElementContainsTextSuccess()
    {
        when(mockedIHighlightingSoftAssert.withHighlightedElement(mockedWebElement)).thenReturn(descriptiveSoftAssert);
        when(mockedWebElement.getText()).thenReturn(STRING_ARG);
        when(descriptiveSoftAssert.assertThat(eq(ELEMENT_CONTAINS_TEXT), eq(STRING_ARG), any())).thenReturn(true);
        boolean result = elementValidations.assertIfElementContainsText(mockedWebElement, STRING_ARG, true);
        assertTrue(result);
    }

    @Test
    void testAssertIfElementContainsTextSuccessInPseudoElement()
    {
        when(mockedIHighlightingSoftAssert.withHighlightedElement(mockedWebElement)).thenReturn(descriptiveSoftAssert);
        when(mockedWebElement.getText()).thenReturn("");
        when(descriptiveSoftAssert.assertThat(eq(ELEMENT_CONTAINS_TEXT), eq(STRING_ARG), any())).thenReturn(true);
        when(webElementActions.getPseudoElementContent(mockedWebElement)).thenReturn(STRING_ARG);
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
        when(mockedIHighlightingSoftAssert.withHighlightedElement(mockedWebElement)).thenReturn(descriptiveSoftAssert);
        when(mockedWebElement.getText()).thenReturn(STRING_ARG);
        when(webElementActions.getPseudoElementContent(mockedWebElement)).thenReturn("''");
        boolean result = elementValidations.assertIfElementContainsText(mockedWebElement, STRING_ARG, false);
        assertFalse(result);
    }

    @Test
    void testAssertAllElementsHaveEqualDimensionHeight()
    {
        List<WebElement> mockedWebElements = List.of(mockedWebElement, mockedWebElement);
        when(mockedIHighlightingSoftAssert.withHighlightedElements(mockedWebElements))
                .thenReturn(descriptiveSoftAssert);
        when(mockedWebElement.getSize()).thenReturn(seleniumDimension);
        when(seleniumDimension.getHeight()).thenReturn(DIMENSION_VALUE);
        Dimension dimension = Dimension.HEIGHT;
        when(mockedIHighlightingSoftAssert.withHighlightedElements(mockedWebElements))
                .thenReturn(descriptiveSoftAssert);
        when(descriptiveSoftAssert.assertEquals(anyString(), anyLong(), anyLong())).thenReturn(true);
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
    void testAssertIfElementContainsTooltip()
    {
        assertFalse(elementValidations.assertIfElementContainsTooltip(null, STRING_ARG));
    }

    @Test
    void testAssertIfElementContainsTooltipEquals()
    {
        when(mockedIHighlightingSoftAssert.withHighlightedElement(mockedWebElement)).thenReturn(descriptiveSoftAssert);
        when(mockedWebElement.getAttribute(TITLE)).thenReturn(STRING_ARG);
        elementValidations.assertIfElementContainsTooltip(mockedWebElement, STRING_ARG);
        verify(descriptiveSoftAssert).assertEquals("Element has correct tooltip", STRING_ARG, STRING_ARG);
    }

    @Test
    void testAssertIfElementContainsTextFailed()
    {
        when(mockedIHighlightingSoftAssert.withHighlightedElement(mockedWebElement)).thenReturn(descriptiveSoftAssert);
        when(mockedWebElement.getText()).thenReturn(STRING_ARG);
        when(webElementActions.getPseudoElementContent(mockedWebElement)).thenReturn("");
        boolean result = elementValidations.assertIfElementContainsText(mockedWebElement, "String", true);
        assertFalse(result);
    }

    private boolean ifElementHasWidthInPerc(boolean elementHasCorrectWidth)
    {
        WebElement mockedParentElement = mock(WebElement.class);
        WebElement mockedChildElement = mock(WebElement.class);
        org.openqa.selenium.Dimension mockedParentDimension = mock(org.openqa.selenium.Dimension.class);
        org.openqa.selenium.Dimension mockedChildDimension = mock(org.openqa.selenium.Dimension.class);
        when(mockedIHighlightingSoftAssert.withHighlightedElements(List.of(mockedParentElement, mockedChildElement)))
                .thenReturn(descriptiveSoftAssert);
        when(mockedParentElement.getSize()).thenReturn(mockedParentDimension);
        when(mockedChildElement.getSize()).thenReturn(mockedChildDimension);
        when(mockedParentDimension.getWidth()).thenReturn(1000);
        when(mockedChildDimension.getWidth()).thenReturn(800);
        when(descriptiveSoftAssert.assertEquals("Element has correct width", CORRECT_WIDTH_VALUE,
                CORRECT_WIDTH_VALUE, 2)).thenReturn(elementHasCorrectWidth);
        return elementValidations.assertIfElementHasWidthInPerc(mockedParentElement, mockedChildElement,
                CORRECT_WIDTH_VALUE);
    }
}
