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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.vividus.testdouble.TestElementFilter;
import org.vividus.testdouble.TestElementSearch;
import org.vividus.testdouble.TestLocatorType;
import org.vividus.ui.action.search.ElementActionService;
import org.vividus.ui.action.search.IElementFilterAction;
import org.vividus.ui.action.search.Locator;
import org.vividus.ui.action.search.SearchParameters;
import org.vividus.ui.context.IUiContext;

@ExtendWith(MockitoExtension.class)
class SearchActionsTests
{
    private static final String VALUE = "value";

    @Mock
    private WebElement webElement;

    @Mock
    private SearchContext searchContext;

    @Mock
    private TestElementSearch testSearch;

    @Mock
    private TestElementFilter testFilter;

    @Mock
    private IUiContext uiContext;

    @Mock
    private ElementActionService elementActionService;

    @InjectMocks
    private SearchActions searchActions;

    @BeforeEach
    void init()
    {
        lenient().when(elementActionService.find(TestLocatorType.SEARCH))
            .thenReturn(testSearch);
        lenient().when(elementActionService.find(TestLocatorType.FILTER))
            .thenReturn(testFilter);
    }

    @Test
    void testFindElementsByLocatorSingleAttribute()
    {
        when(testSearch.search(eq(searchContext), any(SearchParameters.class)))
                .thenReturn(List.of(webElement));
        Locator attributes = new Locator(TestLocatorType.SEARCH, VALUE);
        List<WebElement> foundElements = searchActions.findElements(searchContext, attributes);
        assertEquals(List.of(webElement), foundElements);
    }

    @Test
    void shouldFindElementsUsingLocatorAndUiContext()
    {
        when(testSearch.search(eq(searchContext), any(SearchParameters.class)))
                .thenReturn(List.of(webElement));
        when(uiContext.getSearchContext()).thenReturn(searchContext);
        Locator attributes = new Locator(TestLocatorType.SEARCH, VALUE);
        List<WebElement> foundElements = searchActions.findElements(attributes);
        assertEquals(List.of(webElement), foundElements);
    }

    @Test
    void shouldReturnEmptyOptionalIfNoElementFound()
    {
        when(testSearch.search(eq(searchContext), any(SearchParameters.class))).thenReturn(List.of());
        when(uiContext.getSearchContext()).thenReturn(searchContext);
        Locator attributes = new Locator(TestLocatorType.SEARCH, VALUE);
        assertEquals(Optional.empty(), searchActions.findElement(attributes));
    }

    @Test
    void shouldReturnFirstElementIfFewFound()
    {
        WebElement element1 = mock(WebElement.class);
        WebElement element2 = mock(WebElement.class);
        when(testSearch.search(eq(searchContext), any(SearchParameters.class)))
            .thenReturn(List.of(element1, element2));
        when(uiContext.getSearchContext()).thenReturn(searchContext);
        Locator attributes = new Locator(TestLocatorType.SEARCH, VALUE);
        assertEquals(Optional.of(element1), searchActions.findElement(attributes));
    }

    @Test
    void testFindElementsByLocatorSeveralAttributes()
    {
        List<WebElement> list = List.of(webElement);
        when(testSearch.search(eq(searchContext), any(SearchParameters.class)))
                .thenReturn(List.of(webElement));
        when(((IElementFilterAction) testFilter).filter(List.of(webElement), VALUE)).thenReturn(list);
        Locator attributes = new Locator(TestLocatorType.SEARCH, VALUE)
                .addFilter(TestLocatorType.FILTER, VALUE);
        List<WebElement> foundElements = searchActions.findElements(searchContext, attributes);
        assertEquals(list, foundElements);
    }

    @Test
    void testFindElementsWithChildrenSearchAndFilter()
    {
        TestElementSearch additionalTestSearch = mock(TestElementSearch.class);
        when(elementActionService.find(TestLocatorType.ADDITIONAL_SEARCH))
            .thenReturn(additionalTestSearch);
        List<WebElement> webElements = new ArrayList<>();
        webElements.add(webElement);
        Locator attributes = new Locator(TestLocatorType.SEARCH, VALUE);
        Locator childAttributes = new Locator(TestLocatorType.ADDITIONAL_SEARCH,
            VALUE);
        childAttributes.addFilter(TestLocatorType.FILTER, VALUE);
        attributes.addChildLocator(childAttributes);
        WebElement wrongElement = mock(WebElement.class);
        webElements.add(wrongElement);
        List<WebElement> list = List.of(webElement);
        when(testSearch.search(eq(searchContext), any(SearchParameters.class))).thenReturn(webElements);
        when(additionalTestSearch.search(eq(webElement), any(SearchParameters.class))).thenReturn(list);
        when(((IElementFilterAction) testFilter).filter(list, VALUE)).thenReturn(list);
        List<WebElement> foundElements = searchActions.findElements(searchContext, attributes);
        webElements.remove(wrongElement);
        assertEquals(webElements, foundElements);
    }

    @Test
    void testFindElementsWithChildrenEmptyResults()
    {
        TestElementSearch additionalTestSearch = mock(TestElementSearch.class);
        when(elementActionService.find(TestLocatorType.ADDITIONAL_SEARCH))
            .thenReturn(additionalTestSearch);
        List<WebElement> webElements = new ArrayList<>();
        webElements.add(webElement);
        Locator attributes = new Locator(TestLocatorType.SEARCH, VALUE);
        Locator childAttributes = new Locator(TestLocatorType.ADDITIONAL_SEARCH,
            VALUE);
        attributes.addChildLocator(childAttributes);
        when(testSearch.search(eq(searchContext), any(SearchParameters.class))).thenReturn(webElements);
        when(additionalTestSearch.search(eq(webElement), any(SearchParameters.class))).thenReturn(List.of());
        List<WebElement> foundElements = searchActions.findElements(searchContext, attributes);
        assertEquals(List.of(), foundElements);
    }
}
