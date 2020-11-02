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

package org.vividus.bdd.steps.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebElement;
import org.vividus.bdd.steps.ComparisonRule;
import org.vividus.bdd.steps.ui.model.StringSortingOrder;
import org.vividus.bdd.steps.ui.validation.IBaseValidations;
import org.vividus.softassert.ISoftAssert;
import org.vividus.testdouble.TestLocatorType;
import org.vividus.ui.State;
import org.vividus.ui.action.ElementActions;
import org.vividus.ui.action.search.Locator;
import org.vividus.ui.action.search.SearchParameters;
import org.vividus.ui.action.search.Visibility;

@ExtendWith(MockitoExtension.class)
class GenericElementStepsTests
{
    private static final String THE_NUMBER_OF_FOUND_ELEMENTS = "The number of found elements";
    private static final String VALUE = "value";
    private static final String ELEMENT_CONTAINS_TEXT = "The element with index %d contains not empty text";
    private static final String ELEMENTS_TO_CHECK = "The elements to check the sorting";
    private static final String A_LETTER = "A";
    private static final String B_LETTER = "B";
    private static final String C_LETTER = "C";

    @Mock private IBaseValidations baseValidations;
    @Mock private ElementActions elementActions;
    @Mock private ISoftAssert softAssert;
    @InjectMocks private GenericElementSteps elementSteps;

    @Test
    void shouldAssertElementsNumber()
    {
        ComparisonRule comparisonRule = ComparisonRule.EQUAL_TO;
        int number = 1;
        Locator locator = new Locator(TestLocatorType.SEARCH, VALUE);
        elementSteps.assertElementsNumber(locator, comparisonRule, number);
        verify(baseValidations).assertIfNumberOfElementsFound(THE_NUMBER_OF_FOUND_ELEMENTS,
                locator, number, comparisonRule);
    }

    @Test
    void shouldAssertElementsNumberInState()
    {
        WebElement webElement = mock(WebElement.class);
        ComparisonRule comparisonRule = ComparisonRule.EQUAL_TO;
        int number = 1;
        Locator locator = new Locator(TestLocatorType.SEARCH, VALUE);
        when(baseValidations.assertIfNumberOfElementsFound(THE_NUMBER_OF_FOUND_ELEMENTS,
                locator, number, comparisonRule)).thenReturn(List.of(webElement, webElement, webElement));
        State state = State.ENABLED;
        InOrder ordered = Mockito.inOrder(baseValidations);
        elementSteps.assertElementsNumberInState(state, locator, comparisonRule, number);
        ordered.verify(baseValidations).assertIfNumberOfElementsFound(THE_NUMBER_OF_FOUND_ELEMENTS,
                locator, number, comparisonRule);
        ordered.verify(baseValidations, times(3)).assertElementState("Element is ENABLED", state, webElement);
    }

    @ParameterizedTest
    @CsvSource({ "VISIBLE, VISIBLE", "INVISIBLE, NOT_VISIBLE" })
    void shouldThrowAnExceptionIfLocatorVisibilityAndStateToCheckAreTheSame(Visibility visibility, State state)
    {
        SearchParameters searchParameter = new SearchParameters(VALUE, visibility);
        Locator locator = new Locator(TestLocatorType.SEARCH, searchParameter);
        IllegalArgumentException iae = assertThrows(IllegalArgumentException.class,
            () -> elementSteps.assertElementsNumberInState(state, locator, ComparisonRule.EQUAL_TO, 0));
        assertEquals(String.format(
                "Locator visibility: %s and the state: %s to validate are the same."
                        + " This makes no sense. Please consider validation of elements size instead.",
                visibility, state), iae.getMessage());
    }

    @Test
    void shouldCheckThatElementsAreSorted()
    {
        WebElement webElement = mock(WebElement.class);
        List<WebElement> elements = List.of(webElement, webElement, webElement);
        Locator locator = new Locator(TestLocatorType.SEARCH, VALUE);

        when(baseValidations.assertNumberOfElementsFound(ELEMENTS_TO_CHECK, locator, 1, ComparisonRule.GREATER_THAN))
                .thenReturn(elements);
        when(elementActions.getElementText(webElement)).thenReturn(A_LETTER)
                                                       .thenReturn(B_LETTER)
                                                       .thenReturn(C_LETTER);


        when(softAssert.assertTrue(String.format(ELEMENT_CONTAINS_TEXT, 1), true)).thenReturn(true);
        when(softAssert.assertTrue(String.format(ELEMENT_CONTAINS_TEXT, 2), true)).thenReturn(true);
        when(softAssert.assertTrue(String.format(ELEMENT_CONTAINS_TEXT, 3), true)).thenReturn(true);

        elementSteps.areElementSorted(locator, StringSortingOrder.ASCENDING);

        verify(softAssert).assertEquals("The elements are sorted in ascending order",
                List.of(A_LETTER, B_LETTER, C_LETTER),
                List.of(A_LETTER, B_LETTER, C_LETTER));
        verifyNoMoreInteractions(baseValidations, softAssert, elementActions);
    }

    @Test
    void shouldNotCheckIfNumberOfElementsIsLessThanTwo()
    {
        WebElement webElement = mock(WebElement.class);
        List<WebElement> elements = List.of(webElement);
        Locator locator = new Locator(TestLocatorType.SEARCH, VALUE);

        when(baseValidations.assertNumberOfElementsFound(ELEMENTS_TO_CHECK, locator, 1, ComparisonRule.GREATER_THAN))
                .thenReturn(elements);

        elementSteps.areElementSorted(locator, StringSortingOrder.ASCENDING);

        verifyNoInteractions(softAssert, elementActions);
        verifyNoMoreInteractions(baseValidations);
    }

    @Test
    void shouldNotCheckIfElementsAreFiltredOutByText()
    {
        WebElement webElement = mock(WebElement.class);
        List<WebElement> elements = List.of(webElement, webElement, webElement);
        Locator locator = new Locator(TestLocatorType.SEARCH, VALUE);

        when(baseValidations.assertNumberOfElementsFound(ELEMENTS_TO_CHECK, locator, 1, ComparisonRule.GREATER_THAN))
                .thenReturn(elements);
        when(elementActions.getElementText(webElement)).thenReturn(A_LETTER)
                                                       .thenReturn(StringUtils.EMPTY)
                                                       .thenReturn(null);


        when(softAssert.assertTrue(String.format(ELEMENT_CONTAINS_TEXT, 1), true)).thenReturn(true);
        when(softAssert.assertTrue(String.format(ELEMENT_CONTAINS_TEXT, 2), false)).thenReturn(false);
        when(softAssert.assertTrue(String.format(ELEMENT_CONTAINS_TEXT, 3), false)).thenReturn(false);

        elementSteps.areElementSorted(locator, StringSortingOrder.ASCENDING);

        verify(softAssert).recordFailedAssertion(
                "There are not enough elements with text to check sorting: " + List.of(A_LETTER));
        verifyNoMoreInteractions(baseValidations, softAssert, elementActions);
    }
}
