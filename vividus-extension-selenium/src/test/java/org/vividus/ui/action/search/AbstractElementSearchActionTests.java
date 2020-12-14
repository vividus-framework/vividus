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

package org.vividus.ui.action.search;

import static com.github.valfirst.slf4jtest.LoggingEvent.error;
import static com.github.valfirst.slf4jtest.LoggingEvent.info;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.List;
import java.util.stream.Stream;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.vividus.testdouble.TestLocatorType;
import org.vividus.ui.action.IExpectedConditions;
import org.vividus.ui.action.IExpectedSearchContextCondition;
import org.vividus.ui.action.IWaitActions;
import org.vividus.ui.action.WaitResult;

@ExtendWith({ TestLoggerFactoryExtension.class, MockitoExtension.class })
class AbstractElementSearchActionTests
{
    private static final String TOTAL_NUMBER_OF_ELEMENTS = "Total number of elements found {} is {}";
    private static final String NUMBER_OF_VISIBLE_ELEMENTS = "Number of {} elements is {}";
    private static final String EXCEPTION = "exception";
    private static final Duration TIMEOUT = Duration.ofSeconds(0);

    private final TestLogger logger = TestLoggerFactory.getTestLogger(AbstractElementAction.class);

    @Mock private SearchContext searchContext;
    @Mock private IWaitActions waitActions;
    @Mock private WaitResult<Object> result;
    @Mock private By locator;
    @Mock private IExpectedConditions<By> expectedConditions;

    @InjectMocks
    private final AbstractElementAction elementSearchAction = new AbstractElementAction(TestLocatorType.SEARCH)
    { };

    @Test
    void testGetAttributeType()
    {
        assertEquals(TestLocatorType.SEARCH, elementSearchAction.getType());
    }

    @Test
    void testFindElementsSearchContextIsNull()
    {
        SearchParameters parameters = mock(SearchParameters.class);
        List<WebElement> elements = elementSearchAction.findElements(null, locator, parameters);
        assertThat(elements, empty());
        assertThat(logger.getLoggingEvents(), equalTo(List.of(
                error("Unable to locate elements, because search context is not set"))));
    }

    @Test
    void testFindNoElements()
    {
        elementSearchAction.setWaitForElementTimeout(TIMEOUT);
        when(searchContext.findElements(locator)).thenReturn(null);
        List<WebElement> foundElements = elementSearchAction.findElements(searchContext, locator,
                new SearchParameters().setWaitForElement(false));
        assertThat(foundElements, empty());
        assertThat(logger.getLoggingEvents(), equalTo(List.of(info(TOTAL_NUMBER_OF_ELEMENTS, locator, 0))));
    }

    @SuppressWarnings("unchecked")
    @Test
    void testFindAndScroll()
    {
        elementSearchAction.setWaitForElementTimeout(TIMEOUT);
        when(waitActions.wait(eq(searchContext), eq(TIMEOUT), any(), eq(false))).thenReturn(result);
        WebElement element1 = mock(WebElement.class);
        List<WebElement> elements = List.of(element1);
        IExpectedSearchContextCondition<List<WebElement>> condition = mock(IExpectedSearchContextCondition.class);
        when(expectedConditions.presenceOfAllElementsLocatedBy(any(By.class))).thenReturn(condition);
        when(result.getData()).thenReturn(elements);
        when(element1.isDisplayed()).thenReturn(true);
        List<WebElement> foundElements = elementSearchAction.findElements(searchContext, locator,
                new SearchParameters());
        assertEquals(1, foundElements.size());
        assertEquals(element1, foundElements.get(0));
        verify(waitActions).wait(searchContext, TIMEOUT, condition, false);
        assertThat(logger.getLoggingEvents(), equalTo(List.of(
            info(TOTAL_NUMBER_OF_ELEMENTS, locator, 1),
            info(NUMBER_OF_VISIBLE_ELEMENTS, Visibility.VISIBLE.getDescription(), 1)
        )));
        verify(element1).isDisplayed();
    }

    @Test
    void testFindElementsDisplayedOnly()
    {
        WebElement element1 = mock(WebElement.class);
        WebElement element2 = mock(WebElement.class);
        List<WebElement> elementsList = List.of(element1, element2);
        when(searchContext.findElements(locator)).thenReturn(elementsList);
        when(element1.isDisplayed()).thenReturn(Boolean.TRUE);
        when(element2.isDisplayed()).thenReturn(Boolean.FALSE);
        List<WebElement> foundElements = elementSearchAction.findElements(searchContext, locator,
                new SearchParameters().setWaitForElement(false));
        assertEquals(1, foundElements.size());
        assertEquals(element1, foundElements.get(0));
        assertThat(logger.getLoggingEvents(), equalTo(List.of(
            info(TOTAL_NUMBER_OF_ELEMENTS, locator, 2),
            info(NUMBER_OF_VISIBLE_ELEMENTS, Visibility.VISIBLE.getDescription(), 1)
        )));
        verify(element2).isDisplayed();
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldFindAllElements()
    {
        elementSearchAction.setWaitForElementTimeout(TIMEOUT);
        when(waitActions.wait(eq(searchContext), eq(TIMEOUT), any(), eq(false))).thenReturn(result);
        WebElement element1 = mock(WebElement.class);
        WebElement element2 = mock(WebElement.class);
        List<WebElement> elements = List.of(element1, element2);
        when(result.getData()).thenReturn(elements);
        IExpectedSearchContextCondition<List<WebElement>> condition = mock(IExpectedSearchContextCondition.class);
        when(expectedConditions.presenceOfAllElementsLocatedBy(any(By.class))).thenReturn(condition);
        List<WebElement> foundElements = elementSearchAction.findElements(searchContext, locator,
                new SearchParameters().setVisibility(Visibility.ALL));
        assertEquals(2, foundElements.size());
        assertEquals(element1, foundElements.get(0));
        assertEquals(element2, foundElements.get(1));
        verify(waitActions).wait(searchContext, TIMEOUT, condition, false);
        assertThat(logger.getLoggingEvents(), equalTo(List.of(info(TOTAL_NUMBER_OF_ELEMENTS, locator, 2))));
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldFindInvisibleElements()
    {
        elementSearchAction.setWaitForElementTimeout(TIMEOUT);
        when(waitActions.wait(eq(searchContext), eq(TIMEOUT), any(), eq(false))).thenReturn(result);
        WebElement element1 = mock(WebElement.class);
        WebElement element2 = mock(WebElement.class);
        List<WebElement> elements = List.of(element1, element2);
        when(result.getData()).thenReturn(elements);
        when(element1.isDisplayed()).thenReturn(Boolean.TRUE);
        when(element2.isDisplayed()).thenReturn(Boolean.FALSE);
        IExpectedSearchContextCondition<List<WebElement>> condition = mock(IExpectedSearchContextCondition.class);
        when(expectedConditions.presenceOfAllElementsLocatedBy(any(By.class))).thenReturn(condition);
        List<WebElement> foundElements = elementSearchAction.findElements(searchContext, locator,
                new SearchParameters().setVisibility(Visibility.INVISIBLE));
        assertEquals(1, foundElements.size());
        assertEquals(List.of(element2), foundElements);
        verify(waitActions).wait(searchContext, TIMEOUT, condition, false);
        assertThat(logger.getLoggingEvents(), equalTo(List.of(
            info(TOTAL_NUMBER_OF_ELEMENTS, locator, 2),
            info(NUMBER_OF_VISIBLE_ELEMENTS, Visibility.INVISIBLE.getDescription(), 1)
        )));
        verify(element2).isDisplayed();
    }

    @Test
    void testFindAllElementsWithException()
    {
        WebElement element = mock(WebElement.class);
        List<WebElement> elements = List.of(element);
        Mockito.doThrow(new StaleElementReferenceException(EXCEPTION)).when(element).isDisplayed();
        when(searchContext.findElements(locator)).thenReturn(elements);
        List<WebElement> foundElements = elementSearchAction.findElements(searchContext, locator,
                new SearchParameters().setWaitForElement(false));
        assertEquals(0, foundElements.size());
        verify(element, Mockito.never()).getSize();
        verifyNoInteractions(waitActions);
        assertThat(logger.getLoggingEvents().get(0), equalTo(info(TOTAL_NUMBER_OF_ELEMENTS, locator, 1)));
    }

    private static Stream<Arguments> provideStaleElementTestData()
    {
        Answer<Boolean> elementStale = invocation -> {
            throw new StaleElementReferenceException(EXCEPTION);
        };
        Answer<Boolean> elementDisplayed = invocation -> true;
        return Stream.of(Arguments.of(elementStale, 0, 2), Arguments.of(elementDisplayed, 1, 2));
    }

    @ParameterizedTest
    @MethodSource("provideStaleElementTestData")
    void testStaleElementSearchRetry(Answer<Boolean> answer, int expectedSize,
            int isDisplayedMethodInvocations)
    {
        elementSearchAction.setRetrySearchIfStale(true);
        WebElement element = mock(WebElement.class);
        List<WebElement> elements = List.of(element);
        Mockito.doThrow(new StaleElementReferenceException(EXCEPTION)).doAnswer(answer)
                .when(element).isDisplayed();
        when(searchContext.findElements(locator)).thenReturn(elements);
        List<WebElement> foundElements = elementSearchAction
                .findElements(searchContext, locator, new SearchParameters().setWaitForElement(false));
        assertEquals(expectedSize, foundElements.size());
        verify(element, Mockito.never()).getSize();
        verifyNoInteractions(waitActions);
        verify(element, times(isDisplayedMethodInvocations)).isDisplayed();
        assertThat(logger.getLoggingEvents().get(0), equalTo(info(TOTAL_NUMBER_OF_ELEMENTS, locator, 1)));
    }
}
