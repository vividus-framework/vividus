/*
 * Copyright 2019 the original author or authors.
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

package org.vividus.ui.web.action;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.vividus.ui.web.action.search.SearchAttributes;

@SuppressWarnings("MethodCount")
@ExtendWith(MockitoExtension.class)
class ExpectedSearchActionsConditionsTests
{
    private static final String WITH_SEARCH_ATTRIBUTES = "with search attributes:";
    private static final String TEXT = "text";
    private static final String SEARCH_ATTRIBUTES_PLUS_TEXT = WITH_SEARCH_ATTRIBUTES + TEXT;
    private static final String NO_SUCH_ELEMENT = "No such element with search attributes:text";

    @Mock
    private ISearchActions searchActions;

    @Mock
    private WebElement webElement;

    @Mock
    private SearchAttributes searchAttributes;

    @Mock
    private SearchContext searchContext;

    @InjectMocks
    private ExpectedSearchActionsConditions expectedSearchActionsConditions;

    @Test
    void tetsPresenceOfAllElementsLocatedByNoElements()
    {
        IExpectedSearchContextCondition<List<WebElement>> condition = expectedSearchActionsConditions
                .presenceOfAllElementsLocatedBy(searchAttributes);
        mockFindElements();
        List<WebElement> actual = condition.apply(searchContext);
        assertNull(actual);
    }

    @Test
    void tetsPresenceOfAllElementsLocatedBy()
    {
        when(searchAttributes.toString()).thenReturn(TEXT);
        mockFindElements(webElement);
        IExpectedSearchContextCondition<List<WebElement>> condition = expectedSearchActionsConditions
                .presenceOfAllElementsLocatedBy(searchAttributes);
        assertToString("presence of any elements " + SEARCH_ATTRIBUTES_PLUS_TEXT, condition);
        assertEquals(List.of(webElement), condition.apply(searchContext));
    }

    @Test
    void testTextToBePresentInElementLocatedNoElements()
    {
        when(searchAttributes.toString()).thenReturn(TEXT);
        mockFindElements();
        NoSuchElementException exception = assertThrows(NoSuchElementException.class,
            () -> expectedSearchActionsConditions.textToBePresentInElementLocated(searchAttributes, TEXT)
                    .apply(searchContext));
        assertThat(exception.getMessage(), containsString(NO_SUCH_ELEMENT));
    }

    @Test
    void testTextToBePresentInElementLocated()
    {
        when(searchAttributes.toString()).thenReturn(TEXT);
        mockFindElements(webElement);
        when(webElement.getText()).thenReturn(TEXT);
        IExpectedSearchContextCondition<Boolean> condition = expectedSearchActionsConditions
                .textToBePresentInElementLocated(searchAttributes, TEXT);
        assertToString("text ('text') to be present in element " + SEARCH_ATTRIBUTES_PLUS_TEXT, condition);
        assertTrue(condition.apply(searchContext).booleanValue());
    }

    @Test
    void testTextToBePresentInElementLocatedStaleElementException()
    {
        mockFindElements(webElement);
        when(webElement.getText()).thenThrow(new StaleElementReferenceException(TEXT));
        IExpectedSearchContextCondition<Boolean> condition = expectedSearchActionsConditions
                .textToBePresentInElementLocated(searchAttributes, TEXT);
        assertNull(condition.apply(searchContext));
    }

    @Test
    void tetsVisibilityOfAllElementsIsDisplayedInvisible()
    {
        when(searchAttributes.toString()).thenReturn(TEXT);
        mockFindElements(webElement);
        when(webElement.isDisplayed()).thenReturn(false);
        IExpectedSearchContextCondition<List<WebElement>> condition = expectedSearchActionsConditions
                .visibilityOfAllElementsLocatedBy(searchAttributes);
        assertToString("visibility of all elements " + SEARCH_ATTRIBUTES_PLUS_TEXT, condition);
        assertNull(condition.apply(searchContext));
    }

    @Test
    void tetsVisibilityOfAllElementsNoElements()
    {
        mockFindElements();
        IExpectedSearchContextCondition<List<WebElement>> condition = expectedSearchActionsConditions
                .visibilityOfAllElementsLocatedBy(searchAttributes);
        assertNull(condition.apply(searchContext));
    }

    @Test
    void testVisibilityOfAllElementsStaleElementException()
    {
        List<WebElement> elements = new ArrayList<>();
        elements.add(webElement);
        when(searchActions.findElements(searchContext, searchAttributes)).thenReturn(elements);
        when(webElement.isDisplayed()).thenThrow(new StaleElementReferenceException(TEXT));
        IExpectedSearchContextCondition<List<WebElement>> condition = expectedSearchActionsConditions
                .visibilityOfAllElementsLocatedBy(searchAttributes);
        assertNull(condition.apply(searchContext));
    }

    @Test
    void tetsVisibilityOfAllElementsIsDisplayedVisible()
    {
        mockFindElements(webElement);
        when(webElement.isDisplayed()).thenReturn(true);
        assertEquals(List.of(webElement), expectedSearchActionsConditions
                .visibilityOfAllElementsLocatedBy(searchAttributes).apply(searchContext));
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
        when(searchAttributes.toString()).thenReturn(TEXT);
        mockFindElements(webElement);
        when(webElement.isDisplayed()).thenReturn(true);
        IExpectedSearchContextCondition<WebElement> condition = expectedSearchActionsConditions
                .visibilityOfElement(searchAttributes);
        assertToString("visibility of element " + SEARCH_ATTRIBUTES_PLUS_TEXT, condition);
        assertEquals(webElement, condition.apply(searchContext));
    }

    @Test
    void testVisibilityOfElementNotVisible()
    {
        mockFindElements(webElement);
        when(webElement.isDisplayed()).thenReturn(false);
        assertNull(expectedSearchActionsConditions.visibilityOfElement(searchAttributes).apply(searchContext));
    }

    @Test
    void testVisibilityOfElementStaleElement()
    {
        mockFindElements(webElement);
        when(webElement.isDisplayed()).thenThrow(new StaleElementReferenceException(TEXT));
        assertNull(expectedSearchActionsConditions.visibilityOfElement(searchAttributes).apply(searchContext));
    }

    @Test
    void testVisibilityOfElementNoSuchElement()
    {
        when(searchActions.findElements(searchContext, searchAttributes)).thenThrow(new NoSuchElementException(TEXT));
        assertNull(expectedSearchActionsConditions.visibilityOfElement(searchAttributes).apply(searchContext));
    }

    @Test
    void testVisibilityOfElementNoElements()
    {
        mockFindElements(webElement);
        assertNull(expectedSearchActionsConditions.visibilityOfElement(searchAttributes).apply(searchContext));
    }

    @Test
    void testElementToBeClickable()
    {
        when(searchAttributes.toString()).thenReturn(TEXT);
        mockIsDisplayed(true);
        when(webElement.isEnabled()).thenReturn(true);
        IExpectedSearchContextCondition<WebElement> condition = expectedSearchActionsConditions
                .elementToBeClickable(searchAttributes);
        assertToString("element to be clickable: " + SEARCH_ATTRIBUTES_PLUS_TEXT, condition);
        assertEquals(webElement, condition.apply(searchContext));
    }

    @Test
    void testElementToBeClickableNotVisible()
    {
        mockIsDisplayed(false);
        assertNull(expectedSearchActionsConditions.elementToBeClickable(searchAttributes).apply(searchContext));
    }

    @Test
    void testElementToBeClickableNotEnabled()
    {
        mockIsDisplayed(true);
        when(webElement.isEnabled()).thenReturn(false);
        assertNull(expectedSearchActionsConditions.elementToBeClickable(searchAttributes).apply(searchContext));
    }

    @Test
    void testElementToBeClickableStaleElement()
    {
        mockIsDisplayed(true);
        when(webElement.isEnabled()).thenThrow(new StaleElementReferenceException(TEXT));
        assertNull(expectedSearchActionsConditions.elementToBeClickable(searchAttributes).apply(searchContext));
    }

    @Test
    void testElementSelectionStateToBeTrueSelected()
    {
        when(searchAttributes.toString()).thenReturn(TEXT);
        mockFindElements(webElement);
        when(webElement.isSelected()).thenReturn(true);
        IExpectedSearchContextCondition<Boolean> condition = expectedSearchActionsConditions
                .elementSelectionStateToBe(searchAttributes, true);
        assertToString(String.format("element %s to be selected", SEARCH_ATTRIBUTES_PLUS_TEXT), condition);
        assertTrue(condition.apply(searchContext).booleanValue());
    }

    @Test
    void testElementSelectionStateToBeNotSelected()
    {
        when(searchAttributes.toString()).thenReturn(TEXT);
        mockFindElements(webElement);
        when(webElement.isSelected()).thenReturn(false);
        IExpectedSearchContextCondition<Boolean> condition = expectedSearchActionsConditions
                .elementSelectionStateToBe(searchAttributes, false);
        assertToString(String.format("element %s to not be selected", SEARCH_ATTRIBUTES_PLUS_TEXT), condition);
        assertTrue(condition.apply(searchContext).booleanValue());
    }

    @Test
    void testElementSelectionStateToBeNotSelectedNegative()
    {
        mockFindElements(webElement);
        when(webElement.isSelected()).thenReturn(true);
        assertFalse(expectedSearchActionsConditions.elementSelectionStateToBe(searchAttributes, false)
                .apply(searchContext).booleanValue());
    }

    @Test
    void testElementSelectionStateToBeNoElement()
    {
        when(searchAttributes.toString()).thenReturn(TEXT);
        mockFindElements();
        NoSuchElementException exception = assertThrows(NoSuchElementException.class,
            () -> expectedSearchActionsConditions.elementSelectionStateToBe(searchAttributes, false)
                    .apply(searchContext));
        assertThat(exception.getMessage(), containsString(NO_SUCH_ELEMENT));
    }

    @Test
    void testElementSelectionStateToBeStaleElement()
    {
        mockFindElements(webElement);
        when(webElement.isSelected()).thenThrow(new StaleElementReferenceException(TEXT));
        assertNull(expectedSearchActionsConditions.elementSelectionStateToBe(searchAttributes, false)
                .apply(searchContext));
    }

    @Test
    void testInvisibilityOfElementInvisible()
    {
        when(searchAttributes.toString()).thenReturn(TEXT);
        mockFindElements(webElement);
        when(webElement.isDisplayed()).thenReturn(false);
        IExpectedSearchContextCondition<Boolean> condition = expectedSearchActionsConditions
                .invisibilityOfElement(searchAttributes);
        assertToString("element to no longer be visible: " + SEARCH_ATTRIBUTES_PLUS_TEXT, condition);
        assertTrue(condition.apply(searchContext).booleanValue());
    }

    @Test
    void testInvisibilityOfElementNotInvisible()
    {
        mockFindElements(webElement);
        when(webElement.isDisplayed()).thenReturn(true);
        assertFalse(expectedSearchActionsConditions.invisibilityOfElement(searchAttributes).apply(searchContext)
                .booleanValue());
    }

    @Test
    void testInvisibilityOfElementNoElement()
    {
        when(searchAttributes.toString()).thenReturn(TEXT);
        mockFindElements();
        assertTrue(expectedSearchActionsConditions.invisibilityOfElement(searchAttributes).apply(searchContext)
                .booleanValue());
    }

    @Test
    void testInvisibilityOfElementStaleElement()
    {
        mockFindElements(webElement);
        when(webElement.isDisplayed()).thenThrow(new StaleElementReferenceException(TEXT));
        assertTrue(expectedSearchActionsConditions.invisibilityOfElement(searchAttributes).apply(searchContext)
                .booleanValue());
    }

    private void mockIsDisplayed(boolean condition)
    {
        mockFindElements(webElement);
        when(webElement.isDisplayed()).thenReturn(condition);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void verifyNot(Object expectedCondition, Object mockedCondition)
    {

        IExpectedSearchContextCondition condition = mock(IExpectedSearchContextCondition.class);
        when(condition.apply(searchContext)).thenReturn(mockedCondition);
        IExpectedSearchContextCondition notCondition = expectedSearchActionsConditions.not(condition);
        assertThat(notCondition.toString(), containsString("condition to not be valid: "));
        assertEquals(expectedCondition, notCondition.apply(searchContext));
    }

    private void mockFindElements(WebElement... elements)
    {
        when(searchActions.findElements(searchContext, searchAttributes)).thenReturn(Arrays.asList(elements));
    }

    private void assertToString(String expected, IExpectedSearchContextCondition<?> condition)
    {
        assertEquals(expected, condition.toString());
    }
}
