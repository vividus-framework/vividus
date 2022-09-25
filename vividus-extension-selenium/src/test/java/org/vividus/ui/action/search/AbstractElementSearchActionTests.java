/*
 * Copyright 2019-2022 the original author or authors.
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
import static org.mockito.ArgumentMatchers.argThat;
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
import org.mockito.ArgumentMatcher;
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
import org.vividus.ui.action.ElementActions;
import org.vividus.ui.action.IExpectedSearchContextCondition;
import org.vividus.ui.action.IWaitActions;
import org.vividus.ui.action.WaitResult;

@ExtendWith({ TestLoggerFactoryExtension.class, MockitoExtension.class })
class AbstractElementSearchActionTests
{
    private static final String TOTAL_NUMBER_OF_ELEMENTS = "Total number of elements found {} is {}";
    private static final String NUMBER_OF_VISIBLE_ELEMENTS = "Number of {} elements is {}";
    private static final String EXCEPTION = "exception";

    private final TestLogger logger = TestLoggerFactory.getTestLogger(AbstractElementAction.class);

    @Mock private SearchContext searchContext;
    @Mock private IWaitActions waitActions;
    @Mock private By locator;
    @Mock private ElementActions elementActions;

    @InjectMocks
    private final AbstractElementAction elementSearchAction = new AbstractElementAction(TestLocatorType.SEARCH) { };

    private void mockWaitForElements(List<WebElement> foundElements, List<WebElement> filteredElements)
    {
        Duration timeout = Duration.ofSeconds(1);
        elementSearchAction.setWaitForElementTimeout(timeout);
        var waitResult = new WaitResult<List<WebElement>>();
        waitResult.setData(filteredElements);
        when(waitActions.wait(eq(searchContext), eq(timeout),
                argThat((ArgumentMatcher<IExpectedSearchContextCondition<List<WebElement>>>) condition -> {
                    when(searchContext.findElements(locator)).thenReturn(foundElements);
                    condition.apply(searchContext);
                    return true;
                }), eq(false))).thenReturn(waitResult);
    }

    @Test
    void testGetAttributeType()
    {
        assertEquals(TestLocatorType.SEARCH, elementSearchAction.getType());
    }

    @Test
    void testFindElementsSearchContextIsNull()
    {
        var parameters = mock(SearchParameters.class);
        var elements = elementSearchAction.findElements(null, locator, parameters);
        assertThat(elements, empty());
        assertThat(logger.getLoggingEvents(), equalTo(List.of(
                error("Unable to locate elements, because search context is not set"))));
    }

    @Test
    void shouldFindNoElements()
    {
        mockWaitForElements(List.of(), null);
        var foundElements = elementSearchAction.findElements(searchContext, locator, new SearchParameters());
        assertThat(foundElements, empty());
        assertThat(logger.getLoggingEvents(), equalTo(List.of(info(TOTAL_NUMBER_OF_ELEMENTS, locator, 0))));
    }

    @Test
    void shouldFindVisibleElements()
    {
        var element = mock(WebElement.class);
        when(elementActions.isElementVisible(element)).thenReturn(true);
        var elements = List.of(element);
        mockWaitForElements(elements, elements);
        var foundElements = elementSearchAction.findElements(searchContext, locator, new SearchParameters());
        assertEquals(elements, foundElements);
        assertThat(logger.getLoggingEvents(), equalTo(List.of(
            info(TOTAL_NUMBER_OF_ELEMENTS, locator, 1),
            info(NUMBER_OF_VISIBLE_ELEMENTS, Visibility.VISIBLE.getDescription(), 1)
        )));
    }

    @Test
    void shouldFindAllElements()
    {
        var element = mock(WebElement.class);
        var elements = List.of(element);
        mockWaitForElements(elements, elements);
        var foundElements = elementSearchAction.findElements(searchContext, locator,
                new SearchParameters().setVisibility(Visibility.ALL));
        assertEquals(elements, foundElements);
        verifyNoInteractions(element, elementActions);
        assertThat(logger.getLoggingEvents(), equalTo(List.of(info(TOTAL_NUMBER_OF_ELEMENTS, locator, 1))));
    }

    @Test
    void shouldFindInvisibleElements()
    {
        var element1 = mock(WebElement.class);
        when(elementActions.isElementVisible(element1)).thenReturn(Boolean.TRUE);
        var element2 = mock(WebElement.class);
        when(elementActions.isElementVisible(element2)).thenReturn(Boolean.FALSE);
        var elements = List.of(element1, element2);
        mockWaitForElements(elements, List.of(element2));
        var foundElements = elementSearchAction.findElements(searchContext, locator,
                new SearchParameters().setVisibility(Visibility.INVISIBLE));
        assertEquals(List.of(element2), foundElements);
        assertThat(logger.getLoggingEvents(), equalTo(List.of(
                info(TOTAL_NUMBER_OF_ELEMENTS, locator, 2),
                info(NUMBER_OF_VISIBLE_ELEMENTS, Visibility.INVISIBLE.getDescription(), 1)
        )));
    }

    @Test
    void testFindElementsDisplayedOnly()
    {
        var element1 = mock(WebElement.class);
        var element2 = mock(WebElement.class);
        var elementsList = List.of(element1, element2);
        when(searchContext.findElements(locator)).thenReturn(elementsList);
        when(elementActions.isElementVisible(element1)).thenReturn(Boolean.TRUE);
        when(elementActions.isElementVisible(element2)).thenReturn(Boolean.FALSE);
        var foundElements = elementSearchAction.findElements(searchContext, locator,
                new SearchParameters().setWaitForElement(false));
        assertEquals(List.of(element1), foundElements);
        assertThat(logger.getLoggingEvents(), equalTo(List.of(
            info(TOTAL_NUMBER_OF_ELEMENTS, locator, 2),
            info(NUMBER_OF_VISIBLE_ELEMENTS, Visibility.VISIBLE.getDescription(), 1)
        )));
    }

    @Test
    void testFindAllElementsWithException()
    {
        var element = mock(WebElement.class);
        var elements = List.of(element);
        Mockito.doThrow(new StaleElementReferenceException(EXCEPTION)).when(elementActions).isElementVisible(element);
        when(searchContext.findElements(locator)).thenReturn(elements);
        var foundElements = elementSearchAction.findElements(searchContext, locator,
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
        var element = mock(WebElement.class);
        var elements = List.of(element);
        Mockito.doThrow(new StaleElementReferenceException(EXCEPTION)).doAnswer(answer)
                .when(elementActions).isElementVisible(element);
        when(searchContext.findElements(locator)).thenReturn(elements);
        var foundElements = elementSearchAction
                .findElements(searchContext, locator, new SearchParameters().setWaitForElement(false));
        assertEquals(expectedSize, foundElements.size());
        verify(element, Mockito.never()).getSize();
        verifyNoInteractions(waitActions);
        verify(elementActions, times(isDisplayedMethodInvocations)).isElementVisible(element);
        assertThat(logger.getLoggingEvents().get(0), equalTo(info(TOTAL_NUMBER_OF_ELEMENTS, locator, 1)));
    }
}
