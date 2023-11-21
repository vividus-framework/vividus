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

package org.vividus.ui.action;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.vividus.selenium.locator.Locator;
import org.vividus.ui.action.search.SearchParameters;

@SuppressWarnings("MethodCount")
@ExtendWith(MockitoExtension.class)
class ExpectedSearchActionsConditionsTests
{
    private static final String TEXT = "text";
    private static final String LOCATED_BY_TEXT = "located by " + TEXT;
    private static final String ELEMENT_LOCATED_BY_TEXT = "element " + LOCATED_BY_TEXT;
    private static final String NO_SUCH_ELEMENT = "No such element " + LOCATED_BY_TEXT;

    @Mock private ISearchActions searchActions;
    @Mock private WebElement webElement;
    @Mock private Locator locator;
    @Mock private SearchParameters searchParameters;
    @Mock private SearchContext searchContext;
    @InjectMocks private ExpectedSearchActionsConditions expectedSearchActionsConditions;

    @BeforeEach
    void beforeEach()
    {
        lenient().when(locator.getSearchParameters()).thenReturn(searchParameters);
    }

    @Test
    void testTextToBePresentInElementLocatedNoElements()
    {
        when(locator.toHumanReadableString()).thenReturn(TEXT);
        mockFindElements();
        var condition = expectedSearchActionsConditions.textToBePresentInElementLocated(locator, TEXT);
        var exception = assertThrows(NoSuchElementException.class, () -> condition.apply(searchContext));
        assertThat(exception.getMessage(), containsString(NO_SUCH_ELEMENT));
        validateSearchParameters();
    }

    @Test
    void testTextToBePresentInElementLocated()
    {
        when(locator.toHumanReadableString()).thenReturn(TEXT);
        mockFindElements(webElement);
        when(webElement.getText()).thenReturn(TEXT);
        var condition = expectedSearchActionsConditions.textToBePresentInElementLocated(locator, TEXT);
        assertToString("text ('text') to be present in element " + LOCATED_BY_TEXT, condition);
        assertTrue(condition.apply(searchContext));
        validateSearchParameters();
    }

    @Test
    void testTextToBePresentInElementLocatedStaleElementException()
    {
        mockFindElements(webElement);
        when(webElement.getText()).thenThrow(new StaleElementReferenceException(TEXT));
        var condition = expectedSearchActionsConditions.textToBePresentInElementLocated(locator, TEXT);
        assertNull(condition.apply(searchContext));
        validateSearchParameters();
    }

    @Test
    void testVisibilityOfAllElementsIsDisplayedInvisible()
    {
        when(locator.toHumanReadableString()).thenReturn(TEXT);
        mockFindElements(webElement);
        when(webElement.isDisplayed()).thenReturn(false);
        var condition = expectedSearchActionsConditions.visibilityOfAllElementsLocatedBy(locator);
        assertToString("visibility of all elements " + LOCATED_BY_TEXT, condition);
        assertNull(condition.apply(searchContext));
        validateSearchParameters();
    }

    @Test
    void testVisibilityOfAllElementsNoElements()
    {
        mockFindElements();
        var condition = expectedSearchActionsConditions.visibilityOfAllElementsLocatedBy(locator);
        assertNull(condition.apply(searchContext));
        validateSearchParameters();
    }

    @Test
    void testVisibilityOfAllElementsStaleElementException()
    {
        List<WebElement> elements = new ArrayList<>();
        elements.add(webElement);
        when(searchActions.findElements(searchContext, locator)).thenReturn(elements);
        when(webElement.isDisplayed()).thenThrow(new StaleElementReferenceException(TEXT));
        var condition = expectedSearchActionsConditions.visibilityOfAllElementsLocatedBy(locator);
        assertNull(condition.apply(searchContext));
        validateSearchParameters();
    }

    @Test
    void testVisibilityOfAllElementsIsDisplayedVisible()
    {
        mockFindElements(webElement);
        when(webElement.isDisplayed()).thenReturn(true);
        var condition = expectedSearchActionsConditions.visibilityOfAllElementsLocatedBy(locator);
        assertEquals(List.of(webElement), condition.apply(searchContext));
        validateSearchParameters();
    }

    @Test
    void testNotNull()
    {
        verifyNot(Boolean.TRUE, null);
    }

    @Test
    void testNotTrue()
    {
        verifyNot(Boolean.FALSE, Boolean.TRUE);
    }

    @Test
    void testNotFalse()
    {
        verifyNot(Boolean.TRUE, Boolean.FALSE);
    }

    @Test
    void testVisibilityOfElement()
    {
        when(locator.toHumanReadableString()).thenReturn(TEXT);
        mockFindElements(webElement);
        when(webElement.isDisplayed()).thenReturn(true);
        var condition = expectedSearchActionsConditions.visibilityOfElement(locator);
        assertToString("visibility of element " + LOCATED_BY_TEXT, condition);
        assertEquals(webElement, condition.apply(searchContext));
        validateSearchParameters();
    }

    @Test
    void testVisibilityOfElementNotVisible()
    {
        mockFindElements(webElement);
        when(webElement.isDisplayed()).thenReturn(false);
        assertNull(expectedSearchActionsConditions.visibilityOfElement(locator).apply(searchContext));
        validateSearchParameters();
    }

    @Test
    void testVisibilityOfElementStaleElement()
    {
        mockFindElements(webElement);
        when(webElement.isDisplayed()).thenThrow(new StaleElementReferenceException(TEXT));
        assertNull(expectedSearchActionsConditions.visibilityOfElement(locator).apply(searchContext));
        validateSearchParameters();
    }

    @Test
    void testVisibilityOfElementNoSuchElement()
    {
        when(searchActions.findElements(searchContext, locator)).thenThrow(new NoSuchElementException(TEXT));
        assertNull(expectedSearchActionsConditions.visibilityOfElement(locator).apply(searchContext));
        validateSearchParameters();
    }

    @Test
    void testVisibilityOfElementNoElements()
    {
        mockFindElements(webElement);
        assertNull(expectedSearchActionsConditions.visibilityOfElement(locator).apply(searchContext));
        validateSearchParameters();
    }

    @Test
    void testElementToBeClickable()
    {
        when(locator.toHumanReadableString()).thenReturn(TEXT);
        mockIsDisplayed(true);
        when(webElement.isEnabled()).thenReturn(true);
        var condition = expectedSearchActionsConditions.elementToBeClickable(locator);
        assertToString(ELEMENT_LOCATED_BY_TEXT + " to be clickable", condition);
        assertEquals(webElement, condition.apply(searchContext));
        validateSearchParameters();
    }

    @Test
    void testElementToBeClickableNotVisible()
    {
        mockIsDisplayed(false);
        assertNull(expectedSearchActionsConditions.elementToBeClickable(locator).apply(searchContext));
        validateSearchParameters();
    }

    @Test
    void testElementToBeClickableNotEnabled()
    {
        mockIsDisplayed(true);
        when(webElement.isEnabled()).thenReturn(false);
        assertNull(expectedSearchActionsConditions.elementToBeClickable(locator).apply(searchContext));
        validateSearchParameters();
    }

    @Test
    void testElementToBeClickableStaleElement()
    {
        mockIsDisplayed(true);
        when(webElement.isEnabled()).thenThrow(new StaleElementReferenceException(TEXT));
        assertNull(expectedSearchActionsConditions.elementToBeClickable(locator).apply(searchContext));
        validateSearchParameters();
    }

    @Test
    void testElementSelectionStateToBeTrueSelected()
    {
        when(locator.toHumanReadableString()).thenReturn(TEXT);
        mockFindElements(webElement);
        when(webElement.isSelected()).thenReturn(true);
        var condition = expectedSearchActionsConditions.elementSelectionStateToBe(locator, true);
        assertToString(String.format("element %s to be selected", LOCATED_BY_TEXT), condition);
        assertTrue(condition.apply(searchContext));
        validateSearchParameters();
    }

    @Test
    void testElementSelectionStateToBeNotSelected()
    {
        when(locator.toHumanReadableString()).thenReturn(TEXT);
        mockFindElements(webElement);
        when(webElement.isSelected()).thenReturn(false);
        var condition = expectedSearchActionsConditions.elementSelectionStateToBe(locator, false);
        assertToString(String.format("element %s to not be selected", LOCATED_BY_TEXT), condition);
        assertTrue(condition.apply(searchContext));
        validateSearchParameters();
    }

    @Test
    void testElementSelectionStateToBeNotSelectedNegative()
    {
        mockFindElements(webElement);
        when(webElement.isSelected()).thenReturn(true);
        assertFalse(expectedSearchActionsConditions.elementSelectionStateToBe(locator, false).apply(searchContext));
        validateSearchParameters();
    }

    @Test
    void testElementSelectionStateToBeNoElement()
    {
        when(locator.toHumanReadableString()).thenReturn(TEXT);
        mockFindElements();
        var condition = expectedSearchActionsConditions.elementSelectionStateToBe(locator, false);
        var exception = assertThrows(NoSuchElementException.class, () -> condition.apply(searchContext));
        assertThat(exception.getMessage(), containsString(NO_SUCH_ELEMENT));
        validateSearchParameters();
    }

    @Test
    void testElementSelectionStateToBeStaleElement()
    {
        mockFindElements(webElement);
        when(webElement.isSelected()).thenThrow(new StaleElementReferenceException(TEXT));
        var condition = expectedSearchActionsConditions.elementSelectionStateToBe(locator, false);
        assertNull(condition.apply(searchContext));
        validateSearchParameters();
    }

    @Test
    void testInvisibilityOfElementInvisible()
    {
        when(locator.toHumanReadableString()).thenReturn(TEXT);
        mockFindElements(webElement);
        when(webElement.isDisplayed()).thenReturn(false);
        var condition = expectedSearchActionsConditions.invisibilityOfElement(locator);
        assertToString(ELEMENT_LOCATED_BY_TEXT + " to be no longer visible", condition);
        assertTrue(condition.apply(searchContext));
        validateSearchParameters();
    }

    @Test
    void testInvisibilityOfElementNotInvisible()
    {
        mockFindElements(webElement);
        when(webElement.isDisplayed()).thenReturn(true);
        assertFalse(expectedSearchActionsConditions.invisibilityOfElement(locator).apply(searchContext));
        validateSearchParameters();
    }

    @Test
    void testInvisibilityOfElementNoElement()
    {
        when(locator.toHumanReadableString()).thenReturn(TEXT);
        mockFindElements();
        assertTrue(expectedSearchActionsConditions.invisibilityOfElement(locator).apply(searchContext));
        validateSearchParameters();
    }

    @Test
    void testInvisibilityOfElementStaleElement()
    {
        mockFindElements(webElement);
        when(webElement.isDisplayed()).thenThrow(new StaleElementReferenceException(TEXT));
        assertTrue(expectedSearchActionsConditions.invisibilityOfElement(locator).apply(searchContext));
        validateSearchParameters();
    }

    private void mockIsDisplayed(boolean condition)
    {
        mockFindElements(webElement);
        when(webElement.isDisplayed()).thenReturn(condition);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void verifyNot(Object expectedCondition, Object mockedCondition)
    {
        var condition = mock(IExpectedSearchContextCondition.class);
        when(condition.apply(searchContext)).thenReturn(mockedCondition);
        IExpectedSearchContextCondition notCondition = expectedSearchActionsConditions.not(condition);
        assertThat(notCondition.toString(), containsString("condition to not be valid: "));
        assertEquals(expectedCondition, notCondition.apply(searchContext));
    }

    private void mockFindElements(WebElement... elements)
    {
        when(searchActions.findElements(searchContext, locator)).thenReturn(Arrays.asList(elements));
    }

    private void assertToString(String expected, IExpectedSearchContextCondition<?> condition)
    {
        assertEquals(expected, condition.toString());
    }

    private void validateSearchParameters()
    {
        verify(searchParameters).setWaitForElement(false);
    }
}
