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

package org.vividus.ui.action;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.vividus.ui.action.ExpectedSearchContextConditions.StaleContextException;

@SuppressWarnings("MethodCount")
class ExpectedSearchContextConditionsTests
{
    private static final String XPATH_STRING = "//xpath";
    private static final By XPATH_LOCATOR = By.xpath(XPATH_STRING);
    private static final String TEXT_TO_FIND = "some_text^to-find";
    private static final String ELEMENT_TEXT = "some text before" + TEXT_TO_FIND + "some text after";
    private static final Boolean TRUE = Boolean.TRUE;
    private static final Boolean FALSE = Boolean.FALSE;
    private static final String ELEMENT_SELECTION_STATE_TO_BE_TO_STRING = "element located by %s to %sbe selected";

    private final ExpectedSearchContextConditions expectedConditions = new ExpectedSearchContextConditions();

    @Test
    void testPresenceOfAllElementsLocatedByAllSuccess()
    {
        WebElement webElement1 = mock(WebElement.class);
        WebElement webElement2 = mock(WebElement.class);
        SearchContext searchContext = mock(SearchContext.class);
        List<WebElement> elements = mockFoundElements(webElement1, webElement2, searchContext);
        assertEquals(elements, expectedConditions.presenceOfAllElementsLocatedBy(XPATH_LOCATOR).apply(searchContext));
    }

    @Test
    void testPresenceOfAllElementsLocatedByNoElements()
    {
        SearchContext searchContext = mock(SearchContext.class);
        assertNull(expectedConditions.presenceOfAllElementsLocatedBy(XPATH_LOCATOR).apply(searchContext));
    }

    @Test
    void testPresenceOfAllElementsLocatedByToString()
    {
        assertEquals("presence of any elements located by " + XPATH_LOCATOR,
                expectedConditions.presenceOfAllElementsLocatedBy(XPATH_LOCATOR).toString());
    }

    @Test
    void testTextToBePresentInElementLocatedSuccessContainsValidText()
    {
        WebElement webElement = mock(WebElement.class);
        SearchContext searchContext = mock(SearchContext.class);
        when(searchContext.findElement(XPATH_LOCATOR)).thenReturn(webElement);
        when(webElement.getText()).thenReturn(ELEMENT_TEXT);
        assertTrue(expectedConditions.textToBePresentInElementLocated(XPATH_LOCATOR, TEXT_TO_FIND)
                .apply(searchContext).booleanValue());
    }

    @Test
    void testTextToBePresentInElementLocatedSuccessException()
    {
        SearchContext searchContext = mock(SearchContext.class);
        when(searchContext.findElement(XPATH_LOCATOR)).thenThrow(StaleElementReferenceException.class);
        assertNull(
                expectedConditions.textToBePresentInElementLocated(XPATH_LOCATOR, TEXT_TO_FIND).apply(searchContext));
    }

    @Test
    void testTextToBePresentInElementLocatedToString()
    {
        assertEquals(
                String.format("text ('%s') to be present in element located by %s", TEXT_TO_FIND, XPATH_LOCATOR),
                expectedConditions.textToBePresentInElementLocated(XPATH_LOCATOR, TEXT_TO_FIND)
                        .toString());
    }

    @Test
    void testVisibilityOfAllElementsLocatedByAllDisplayed()
    {
        WebElement webElement = mock(WebElement.class);
        WebElement webElement2 = mock(WebElement.class);
        SearchContext searchContext = mock(SearchContext.class);
        List<WebElement> elements = mockFoundElements(webElement, webElement2, searchContext);
        when(webElement.isDisplayed()).thenReturn(true);
        when(webElement2.isDisplayed()).thenReturn(true);
        assertEquals(elements, expectedConditions.visibilityOfAllElementsLocatedBy(XPATH_LOCATOR).apply(searchContext));
    }

    @Test
    void testVisibilityOfAllElementsLocatedByNoElements()
    {
        List<WebElement> elements = new ArrayList<>();
        SearchContext searchContext = mock(SearchContext.class);
        doReturn(elements).when(searchContext).findElements(XPATH_LOCATOR);
        assertNull(expectedConditions.visibilityOfAllElementsLocatedBy(XPATH_LOCATOR).apply(searchContext));
    }

    @Test
    void testVisibilityOfAllElementsLocatedBySuccessNoDisplayed()
    {
        WebElement webElement1 = mock(WebElement.class);
        WebElement webElement2 = mock(WebElement.class);
        SearchContext searchContext = mock(SearchContext.class);
        mockFoundElements(webElement1, webElement2, searchContext);
        when(webElement1.isDisplayed()).thenReturn(false);
        when(webElement2.isDisplayed()).thenReturn(false);
        assertNull(expectedConditions.visibilityOfAllElementsLocatedBy(XPATH_LOCATOR).apply(searchContext));
    }

    @Test
    void testVisibilityOfAllElementsLocatedBySuccessException()
    {
        WebElement webElement1 = mock(WebElement.class);
        WebElement webElement2 = mock(WebElement.class);
        SearchContext searchContext = mock(SearchContext.class);
        mockFoundElements(webElement1, webElement2, searchContext);
        List<WebElement> expectedElements = new ArrayList<>();
        expectedElements.add(webElement1);
        when(webElement1.isDisplayed()).thenReturn(true);
        when(webElement2.isDisplayed()).thenThrow(StaleElementReferenceException.class);
        assertEquals(expectedElements,
                expectedConditions.visibilityOfAllElementsLocatedBy(XPATH_LOCATOR).apply(searchContext));
    }

    @Test
    void testVisibilityOfAllElementsLocatedByToString()
    {
        assertEquals("visibility of all elements located by " + XPATH_LOCATOR,
                expectedConditions.visibilityOfAllElementsLocatedBy(XPATH_LOCATOR).toString());
    }

    @Test
    void testNotResultIsNull()
    {
        IExpectedSearchContextCondition<?> condition = mock(IExpectedSearchContextCondition.class);
        SearchContext searchContext = mock(SearchContext.class);
        doReturn(null).when(condition).apply(searchContext);
        assertEquals(TRUE, expectedConditions.not(condition).apply(searchContext));
    }

    @Test
    void testNotResultIsTrue()
    {
        IExpectedSearchContextCondition<?> condition = mock(IExpectedSearchContextCondition.class);
        SearchContext searchContext = mock(SearchContext.class);
        doReturn(TRUE).when(condition).apply(searchContext);
        assertEquals(FALSE, expectedConditions.not(condition).apply(searchContext));
    }

    @Test
    void testNotResultIsFalse()
    {
        IExpectedSearchContextCondition<?> condition = mock(IExpectedSearchContextCondition.class);
        SearchContext searchContext = mock(SearchContext.class);
        doReturn(FALSE).when(condition).apply(searchContext);
        assertEquals(TRUE, expectedConditions.not(condition).apply(searchContext));
    }

    @Test
    void testNotToString()
    {
        IExpectedSearchContextCondition<?> condition = mock(IExpectedSearchContextCondition.class);
        assertEquals("condition to not be valid: " + condition,
                expectedConditions.not(condition).toString());
    }

    @Test
    void testElementToBeClickableVisibleEnabled()
    {
        WebElement webElement = mock(WebElement.class);
        SearchContext searchContext = mock(SearchContext.class);
        when(searchContext.findElement(XPATH_LOCATOR)).thenReturn(webElement);
        when(webElement.isDisplayed()).thenReturn(TRUE);
        when(webElement.isEnabled()).thenReturn(TRUE);
        assertEquals(webElement,
                expectedConditions.elementToBeClickable(XPATH_LOCATOR).apply(searchContext));
    }

    @Test
    void testElementToBeClickableVisibleDisabled()
    {
        WebElement webElement = mock(WebElement.class);
        SearchContext searchContext = mock(SearchContext.class);
        when(searchContext.findElement(XPATH_LOCATOR)).thenReturn(webElement);
        when(webElement.isDisplayed()).thenReturn(TRUE);
        when(webElement.isEnabled()).thenReturn(FALSE);
        assertNull(expectedConditions.elementToBeClickable(XPATH_LOCATOR).apply(searchContext));
    }

    @Test
    void testElementToBeClickableNotVisible()
    {
        WebElement webElement = mock(WebElement.class);
        SearchContext searchContext = mock(SearchContext.class);
        when(searchContext.findElement(XPATH_LOCATOR)).thenReturn(webElement);
        when(webElement.isDisplayed()).thenReturn(FALSE);
        assertNull(expectedConditions.elementToBeClickable(XPATH_LOCATOR).apply(searchContext));
    }

    @Test
    void testElementToBeClickableStaleElementException()
    {
        WebElement webElement = mock(WebElement.class);
        SearchContext searchContext = mock(SearchContext.class);
        when(searchContext.findElement(XPATH_LOCATOR)).thenReturn(webElement);
        when(webElement.isDisplayed()).thenReturn(TRUE);
        when(webElement.isEnabled()).thenThrow(StaleElementReferenceException.class);
        assertNull(expectedConditions.elementToBeClickable(XPATH_LOCATOR).apply(searchContext));
    }

    @Test
    void testElementToBeClickableToString()
    {
        assertEquals("element to be clickable: located by By.xpath: //xpath",
                expectedConditions.elementToBeClickable(XPATH_LOCATOR).toString());
    }

    @Test
    void testVisibilityOfElementLocatedSuccess()
    {
        WebElement webElement = mock(WebElement.class);
        SearchContext searchContext = mock(SearchContext.class);
        when(searchContext.findElement(XPATH_LOCATOR)).thenReturn(webElement);
        when(webElement.isDisplayed()).thenReturn(true);
        assertEquals(webElement,
                expectedConditions.visibilityOfElement(XPATH_LOCATOR).apply(searchContext));
    }

    @Test
    void testVisibilityOfElementLocatedNotDisplayed()
    {
        WebElement webElement = mock(WebElement.class);
        SearchContext searchContext = mock(SearchContext.class);
        when(searchContext.findElement(XPATH_LOCATOR)).thenReturn(webElement);
        when(webElement.isDisplayed()).thenReturn(false);
        assertNull(expectedConditions.visibilityOfElement(XPATH_LOCATOR).apply(searchContext));
    }

    @Test
    void testVisibilityOfElementLocatedNoElement()
    {
        SearchContext searchContext = mock(SearchContext.class);
        when(searchContext.findElement(XPATH_LOCATOR)).thenThrow(StaleElementReferenceException.class);
        assertNull(expectedConditions.visibilityOfElement(XPATH_LOCATOR).apply(searchContext));
    }

    @Test
    void testVisibilityOfElementLocatedToString()
    {
        assertEquals("visibility of element located by " + XPATH_LOCATOR,
                expectedConditions.visibilityOfElement(XPATH_LOCATOR).toString());
    }

    private List<WebElement> mockFoundElements(WebElement webElement1, WebElement webElement2,
            SearchContext searchContext)
    {
        List<WebElement> elements = new ArrayList<>();
        elements.add(webElement1);
        elements.add(webElement2);
        doReturn(elements).when(searchContext).findElements(XPATH_LOCATOR);
        return elements;
    }

    @Test
    void testElementSelectionStateToBeSuccess()
    {
        WebElement webElement = mock(WebElement.class);
        SearchContext searchContext = mock(SearchContext.class);
        when(searchContext.findElement(XPATH_LOCATOR)).thenReturn(webElement);
        when(webElement.isSelected()).thenReturn(TRUE);
        assertTrue(expectedConditions.elementSelectionStateToBe(XPATH_LOCATOR, TRUE).apply(searchContext)
                .booleanValue());
    }

    @Test
    void testElementSelectionStateToBeNotLikeSelected()
    {
        WebElement webElement = mock(WebElement.class);
        SearchContext searchContext = mock(SearchContext.class);
        when(searchContext.findElement(XPATH_LOCATOR)).thenReturn(webElement);
        when(webElement.isSelected()).thenReturn(TRUE);
        assertFalse(expectedConditions.elementSelectionStateToBe(XPATH_LOCATOR, FALSE).apply(searchContext)
                .booleanValue());
    }

    @Test
    void testElementSelectionStateToBeStaleException()
    {
        SearchContext searchContext = mock(SearchContext.class);
        when(searchContext.findElement(XPATH_LOCATOR)).thenThrow(StaleElementReferenceException.class);
        assertNull(expectedConditions.elementSelectionStateToBe(XPATH_LOCATOR, TRUE).apply(searchContext));
    }

    @Test
    void testElementSelectionStateToBeToStringSelected()
    {
        assertEquals(String.format(ELEMENT_SELECTION_STATE_TO_BE_TO_STRING, XPATH_LOCATOR, ""),
                expectedConditions.elementSelectionStateToBe(XPATH_LOCATOR, TRUE).toString());
    }

    @Test
    void testElementSelectionStateToBeToStringNotSelected()
    {
        assertEquals(String.format(ELEMENT_SELECTION_STATE_TO_BE_TO_STRING, XPATH_LOCATOR, "not "),
                expectedConditions.elementSelectionStateToBe(XPATH_LOCATOR, FALSE).toString());
    }

    @Test
    void testInvisibilityOfElementLocatedSuccessIsDisplayed()
    {
        WebElement webElement = mock(WebElement.class);
        SearchContext searchContext = mock(SearchContext.class);
        when(searchContext.findElement(XPATH_LOCATOR)).thenReturn(webElement);
        when(webElement.isDisplayed()).thenReturn(true);
        assertFalse(expectedConditions.invisibilityOfElement(XPATH_LOCATOR).apply(searchContext)
                .booleanValue());
    }

    @Test
    void testInvisibilityOfElementLocatedSuccessNotDisplayed()
    {
        WebElement webElement = mock(WebElement.class);
        SearchContext searchContext = mock(SearchContext.class);
        when(searchContext.findElement(XPATH_LOCATOR)).thenReturn(webElement);
        when(webElement.isDisplayed()).thenReturn(false);
        assertTrue(expectedConditions.invisibilityOfElement(XPATH_LOCATOR).apply(searchContext)
                .booleanValue());
    }

    @Test
    void testInvisibilityOfElementLocatedNoSuchElementException()
    {
        SearchContext searchContext = mock(SearchContext.class);
        when(searchContext.findElement(XPATH_LOCATOR)).thenThrow(NoSuchElementException.class);
        assertTrue(expectedConditions.invisibilityOfElement(XPATH_LOCATOR).apply(searchContext)
                .booleanValue());
    }

    @Test
    void testInvisibilityOfElementLocatedStaleElementException()
    {
        SearchContext searchContext = mock(SearchContext.class);
        when(searchContext.findElement(XPATH_LOCATOR)).thenThrow(StaleElementReferenceException.class);
        assertTrue(expectedConditions.invisibilityOfElement(XPATH_LOCATOR).apply(searchContext).booleanValue());
    }

    @Test
    void testInvisibilityOfElementLocatedToString()
    {
        assertEquals("element to no longer be visible: located by " + XPATH_LOCATOR,
                expectedConditions.invisibilityOfElement(XPATH_LOCATOR).toString());
    }

    @Test
    void shouldWrapExpcetionWithAMessageInCaseOfStaleElementReferenceException()
    {
        SearchContext searchContext = mock(SearchContext.class);
        when(searchContext.findElement(XPATH_LOCATOR)).thenThrow(StaleElementReferenceException.class);
        var exception = assertThrows(StaleContextException.class,
            () -> expectedConditions.findElement(searchContext, XPATH_LOCATOR));
        assertEquals("Search context used for search is stale.\n"
                   + "Please double check the tests.\n"
                   + "You have a few options:\n"
                   + "1. Reset context;\n"
                   + "2. Synchronize the tests to wait for context's stabilization;", exception.getMessage());
        assertThat(exception.getCause(), instanceOf(StaleElementReferenceException.class));
    }
}
