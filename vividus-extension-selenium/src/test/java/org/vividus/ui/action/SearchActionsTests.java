/*
 * Copyright 2019-2024 the original author or authors.
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

import static com.github.valfirst.slf4jtest.LoggingEvent.info;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.vividus.selenium.locator.Locator;
import org.vividus.testdouble.TestElementFilter;
import org.vividus.testdouble.TestElementSearch;
import org.vividus.testdouble.TestLocatorType;
import org.vividus.ui.action.search.ElementActionService;
import org.vividus.ui.context.IUiContext;

@ExtendWith({ MockitoExtension.class, TestLoggerFactoryExtension.class })
class SearchActionsTests
{
    private static final String VALUE = "value";
    private static final String FILTER_MESSAGE = "{} of {} elements were filtered out by {} filter with '{}' value";

    @Mock private SearchContext searchContext;
    @Mock private TestElementSearch testSearch;
    @Mock private TestElementFilter testFilter;
    @Mock private IUiContext uiContext;
    @Mock private ElementActionService elementActionService;
    @InjectMocks private SearchActions searchActions;

    private final TestLogger logger = TestLoggerFactory.getTestLogger(SearchActions.class);

    @Test
    void shouldFindElements()
    {
        var locator = new Locator(TestLocatorType.SEARCH, VALUE);

        var webElements = List.of(mock(WebElement.class));
        when(elementActionService.find(TestLocatorType.SEARCH)).thenReturn(testSearch);
        when(testSearch.search(searchContext, locator.getSearchParameters())).thenReturn(webElements);

        var foundElements = searchActions.findElements(searchContext, locator);
        assertEquals(webElements, foundElements);
        assertThat(logger.getLoggingEvents(), is(empty()));
    }

    @Test
    void shouldFindElementsUsingLocatorAndUiContext()
    {
        when(uiContext.getSearchContext()).thenReturn(searchContext);

        var locator = new Locator(TestLocatorType.SEARCH, VALUE);

        var webElements = List.of(mock(WebElement.class));
        when(elementActionService.find(TestLocatorType.SEARCH)).thenReturn(testSearch);
        when(testSearch.search(searchContext, locator.getSearchParameters())).thenReturn(webElements);

        var foundElements = searchActions.findElements(locator);
        assertEquals(webElements, foundElements);
        assertThat(logger.getLoggingEvents(), is(empty()));
    }

    @Test
    void shouldReturnEmptyOptionalIfNoElementFound()
    {
        when(uiContext.getSearchContext()).thenReturn(searchContext);

        var locator = new Locator(TestLocatorType.SEARCH, VALUE);

        when(elementActionService.find(TestLocatorType.SEARCH)).thenReturn(testSearch);
        when(testSearch.search(searchContext, locator.getSearchParameters())).thenReturn(List.of());

        assertEquals(Optional.empty(), searchActions.findElement(locator));
        assertThat(logger.getLoggingEvents(), is(empty()));
    }

    @Test
    void shouldReturnFirstElementIfFewFound()
    {
        when(uiContext.getSearchContext()).thenReturn(searchContext);

        var locator = new Locator(TestLocatorType.SEARCH, VALUE);

        WebElement element1 = mock();
        WebElement element2 = mock();
        when(elementActionService.find(TestLocatorType.SEARCH)).thenReturn(testSearch);
        when(testSearch.search(searchContext, locator.getSearchParameters())).thenReturn(List.of(element1, element2));

        assertEquals(Optional.of(element1), searchActions.findElement(locator));
        assertThat(logger.getLoggingEvents(), is(empty()));
    }

    @Test
    void shouldFindElementInSearchContext()
    {
        var locator = new Locator(TestLocatorType.SEARCH, VALUE);

        WebDriver webDriver = mock();
        WebElement webElement = mock();

        when(elementActionService.find(TestLocatorType.SEARCH)).thenReturn(testSearch);
        when(testSearch.search(webDriver, locator.getSearchParameters())).thenReturn(List.of(webElement));

        assertEquals(Optional.of(webElement), searchActions.findElement(webDriver, locator));
        verifyNoInteractions(uiContext, searchContext);
        assertThat(logger.getLoggingEvents(), is(empty()));
    }

    @Test
    void shouldFindElementsAndFilter()
    {
        var locator = new Locator(TestLocatorType.SEARCH, VALUE).addFilter(TestLocatorType.FILTER, VALUE);

        var webElements = List.of(mock(WebElement.class));
        when(elementActionService.find(TestLocatorType.SEARCH)).thenReturn(testSearch);
        when(testSearch.search(searchContext, locator.getSearchParameters())).thenReturn(webElements);

        when(elementActionService.find(TestLocatorType.FILTER)).thenReturn(testFilter);
        when(testFilter.filter(webElements, VALUE)).thenReturn(webElements);

        var foundElements = searchActions.findElements(searchContext, locator);
        assertEquals(webElements, foundElements);
        assertThat(logger.getLoggingEvents(),
                equalTo(List.of(info(FILTER_MESSAGE, 0, 1, TestLocatorType.FILTER, VALUE))));
    }

    @Test
    void shouldRetrySearchOnStaleElementReferenceExceptionAtFiltering()
    {
        var locator = new Locator(TestLocatorType.SEARCH, VALUE).addFilter(TestLocatorType.FILTER, VALUE);

        var webElements1 = List.of(mock(WebElement.class));
        var webElements2 = List.of(mock(WebElement.class));
        when(elementActionService.find(TestLocatorType.SEARCH)).thenReturn(testSearch);
        when(testSearch.search(searchContext, locator.getSearchParameters())).thenReturn(webElements1).thenReturn(
                webElements2);

        when(elementActionService.find(TestLocatorType.FILTER)).thenReturn(testFilter);
        var staleElementReferenceException = new StaleElementReferenceException("test");
        when(testFilter.filter(webElements1, VALUE)).thenThrow(staleElementReferenceException);
        when(testFilter.filter(webElements2, VALUE)).thenReturn(webElements2);

        var foundElements = searchActions.findElements(searchContext, locator);

        assertEquals(webElements2, foundElements);
        assertThat(logger.getLoggingEvents(),
                equalTo(List.of(info(FILTER_MESSAGE, 0, 1, TestLocatorType.FILTER, VALUE))));

        var ordered = inOrder(elementActionService, testSearch, testFilter);
        ordered.verify(elementActionService).find(TestLocatorType.SEARCH);
        ordered.verify(testSearch).search(searchContext, locator.getSearchParameters());
        ordered.verify(elementActionService).find(TestLocatorType.FILTER);
        ordered.verify(testFilter).filter(webElements1, VALUE);
        ordered.verify(testSearch).search(searchContext, locator.getSearchParameters());
        ordered.verify(testFilter).filter(webElements2, VALUE);
        ordered.verifyNoMoreInteractions();
    }

    @Test
    void shouldThrowErrorOnTwoSubsequentStaleElementReferenceExceptionsAtFiltering()
    {
        var locator = new Locator(TestLocatorType.SEARCH, VALUE).addFilter(TestLocatorType.FILTER, VALUE);

        var webElements1 = List.of(mock(WebElement.class));
        var webElements2 = List.of(mock(WebElement.class));
        when(elementActionService.find(TestLocatorType.SEARCH)).thenReturn(testSearch);
        when(testSearch.search(searchContext, locator.getSearchParameters())).thenReturn(webElements1).thenReturn(
                webElements2);

        when(elementActionService.find(TestLocatorType.FILTER)).thenReturn(testFilter);
        var staleElementReferenceException1 = new StaleElementReferenceException("test1");
        when(testFilter.filter(webElements1, VALUE)).thenThrow(staleElementReferenceException1);
        var staleElementReferenceException2 = new StaleElementReferenceException("test2");
        when(testFilter.filter(webElements2, VALUE)).thenThrow(staleElementReferenceException2);

        var actual = assertThrows(StaleElementReferenceException.class,
                () -> searchActions.findElements(searchContext, locator));

        assertEquals(staleElementReferenceException2, actual);
        assertThat(logger.getLoggingEvents(), is(empty()));

        var ordered = inOrder(elementActionService, testSearch, testFilter);
        ordered.verify(elementActionService).find(TestLocatorType.SEARCH);
        ordered.verify(testSearch).search(searchContext, locator.getSearchParameters());
        ordered.verify(elementActionService).find(TestLocatorType.FILTER);
        ordered.verify(testFilter).filter(webElements1, VALUE);
        ordered.verify(testSearch).search(searchContext, locator.getSearchParameters());
        ordered.verify(testFilter).filter(webElements2, VALUE);
        ordered.verifyNoMoreInteractions();
    }

    @Test
    void shouldFindElementsAndApplySeveralFilters()
    {
        var filterOne = "filter-one";
        var filterTwo = "filter-two";
        var filterThree = "filter-three";
        var locator = new Locator(TestLocatorType.SEARCH, VALUE);
        locator.addFilter(TestLocatorType.FILTER, filterOne);
        locator.addFilter(TestLocatorType.FILTER, filterTwo);
        locator.addFilter(TestLocatorType.ADDITIONAL_FILTER, filterThree);
        locator.addFilter(TestLocatorType.ADDITIONAL_FILTER, "filter-four");

        WebElement element = mock();
        TestElementFilter additionalFilter = mock();

        when(elementActionService.find(TestLocatorType.SEARCH)).thenReturn(testSearch);
        when(elementActionService.find(TestLocatorType.FILTER)).thenReturn(testFilter);
        when(elementActionService.find(TestLocatorType.ADDITIONAL_FILTER)).thenReturn(additionalFilter);
        when(testSearch.search(searchContext, locator.getSearchParameters()))
                .thenReturn(List.of(element, element, element));
        when(testFilter.filter(List.of(element, element, element), filterOne))
                .thenReturn(List.of(element, element, element));
        when(testFilter.filter(List.of(element, element, element), filterTwo))
                .thenReturn(List.of(element, element));
        when(additionalFilter.filter(List.of(element, element), filterThree)).thenReturn(List.of());

        assertEquals(List.of(), searchActions.findElements(searchContext, locator));

        assertThat(logger.getLoggingEvents(), equalTo(List.of(
            info(FILTER_MESSAGE, 0, 3, TestLocatorType.FILTER, filterOne),
            info(FILTER_MESSAGE, 1, 3, TestLocatorType.FILTER, filterTwo),
            info(FILTER_MESSAGE, 2, 2, TestLocatorType.ADDITIONAL_FILTER, filterThree)
        )));
        verifyNoMoreInteractions(testFilter, additionalFilter);
    }
}
