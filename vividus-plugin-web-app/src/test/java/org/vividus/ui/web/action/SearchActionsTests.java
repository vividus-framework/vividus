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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.vividus.ui.web.action.search.ActionAttributeType;
import org.vividus.ui.web.action.search.ButtonNameSearch;
import org.vividus.ui.web.action.search.CaseSensitiveTextSearch;
import org.vividus.ui.web.action.search.DefaultSearch;
import org.vividus.ui.web.action.search.IActionAttributeType;
import org.vividus.ui.web.action.search.IElementAction;
import org.vividus.ui.web.action.search.IElementFilterAction;
import org.vividus.ui.web.action.search.IElementSearchAction;
import org.vividus.ui.web.action.search.SearchAttributes;
import org.vividus.ui.web.action.search.SearchParameters;
import org.vividus.ui.web.context.IWebUiContext;

@ExtendWith(MockitoExtension.class)
class SearchActionsTests
{
    private static final String BUTTON_NAME = "buttonName";
    private static final String XPATH_LOCATOR = "/xpathLocator";
    private static final String LINK_TEXT = "linkText";
    private static final String TEXT_UPPER = "Text";
    private static final By ELEMENT_BY_TEXT_LOCATOR = By.xpath(".//*[contains(normalize-space(text()), 'Text')]");

    @Mock
    private WebElement webElement;

    @Mock
    private SearchContext searchContext;

    @Mock(extraInterfaces = IElementFilterAction.class)
    private IElementSearchAction searchAction;

    @Mock
    private DefaultSearch defaultSearch;

    @Mock
    private CaseSensitiveTextSearch caseSensitivetextSearch;

    @Mock
    private ButtonNameSearch buttonNameSearch;

    @Mock
    private IWebUiContext webUiContext;

    @InjectMocks
    private SearchActions searchActions;

    @Test
    void testFindElementsBySearchAttributesSingleAttribute()
    {
        createAndSetElementActionsMap();
        when(caseSensitivetextSearch.search(eq(searchContext), any(SearchParameters.class)))
                .thenReturn(List.of(webElement));
        SearchAttributes attributes = new SearchAttributes(ActionAttributeType.CASE_SENSITIVE_TEXT, TEXT_UPPER);
        List<WebElement> foundElements = searchActions.findElements(searchContext, attributes);
        assertEquals(List.of(webElement), foundElements);
    }

    @Test
    void shouldFindElementsUsingSearchAttributesAndWebUiContext()
    {
        createAndSetElementActionsMap();
        when(caseSensitivetextSearch.search(eq(searchContext), any(SearchParameters.class)))
                .thenReturn(List.of(webElement));
        when(webUiContext.getSearchContext()).thenReturn(searchContext);
        SearchAttributes attributes = new SearchAttributes(ActionAttributeType.CASE_SENSITIVE_TEXT, TEXT_UPPER);
        List<WebElement> foundElements = searchActions.findElements(attributes);
        assertEquals(List.of(webElement), foundElements);
    }

    @Test
    void shouldReturnEmptyOptionalIfNoElementFound()
    {
        createAndSetElementActionsMap();
        when(caseSensitivetextSearch.search(eq(searchContext), any(SearchParameters.class))).thenReturn(List.of());
        when(webUiContext.getSearchContext()).thenReturn(searchContext);
        SearchAttributes attributes = new SearchAttributes(ActionAttributeType.CASE_SENSITIVE_TEXT, TEXT_UPPER);
        assertEquals(Optional.empty(), searchActions.findElement(attributes));
    }

    @Test
    void shouldReturnFirstElementIfFewFound()
    {
        createAndSetElementActionsMap();
        WebElement element1 = mock(WebElement.class);
        WebElement element2 = mock(WebElement.class);
        when(caseSensitivetextSearch.search(eq(searchContext), any(SearchParameters.class)))
            .thenReturn(List.of(element1, element2));
        when(webUiContext.getSearchContext()).thenReturn(searchContext);
        SearchAttributes attributes = new SearchAttributes(ActionAttributeType.CASE_SENSITIVE_TEXT, TEXT_UPPER);
        assertEquals(Optional.of(element1), searchActions.findElement(attributes));
    }

    @Test
    void testFindElementsBySearchAttributesSeveralAttributes()
    {
        createAndSetElementActionsMap();
        List<WebElement> list = List.of(webElement);
        when(caseSensitivetextSearch.search(eq(searchContext), any(SearchParameters.class)))
                .thenReturn(List.of(webElement));
        when(((IElementFilterAction) searchAction).filter(List.of(webElement), LINK_TEXT)).thenReturn(list);
        SearchAttributes attributes = new SearchAttributes(ActionAttributeType.CASE_SENSITIVE_TEXT, TEXT_UPPER)
                .addFilter(ActionAttributeType.LINK_URL, LINK_TEXT);
        List<WebElement> foundElements = searchActions.findElements(searchContext, attributes);
        assertEquals(list, foundElements);
    }

    @Test
    void testFindElements()
    {
        createAndSetElementActionsMap();
        SearchParameters parameters = new SearchParameters();
        when(defaultSearch.findElements(searchContext, ELEMENT_BY_TEXT_LOCATOR, parameters))
                .thenReturn(List.of(webElement));
        List<WebElement> foundElements = searchActions.findElements(searchContext, ELEMENT_BY_TEXT_LOCATOR);
        assertEquals(List.of(webElement), foundElements);
    }

    @Test
    void shouldFindElementsUsingWebUiContext()
    {
        createAndSetElementActionsMap();
        SearchParameters parameters = new SearchParameters();
        when(webUiContext.getSearchContext()).thenReturn(searchContext);
        when(defaultSearch.findElements(searchContext, ELEMENT_BY_TEXT_LOCATOR, parameters))
                .thenReturn(List.of(webElement));
        List<WebElement> foundElements = searchActions.findElements(ELEMENT_BY_TEXT_LOCATOR);
        assertEquals(List.of(webElement), foundElements);
    }

    @Test
    void testFindElementsWithChildrenSearchAndFilter()
    {
        List<WebElement> webElements = new ArrayList<>();
        webElements.add(webElement);
        createAndSetElementActionsMap();
        SearchAttributes attributes = new SearchAttributes(ActionAttributeType.CASE_SENSITIVE_TEXT, XPATH_LOCATOR);
        SearchAttributes childAttributes = new SearchAttributes(ActionAttributeType.BUTTON_NAME, BUTTON_NAME);
        childAttributes.addFilter(ActionAttributeType.LINK_URL, LINK_TEXT);
        attributes.addChildSearchAttributes(childAttributes);
        WebElement wrongElement = mock(WebElement.class);
        webElements.add(wrongElement);
        List<WebElement> list = List.of(webElement);
        when(caseSensitivetextSearch.search(eq(searchContext), any(SearchParameters.class))).thenReturn(webElements);
        when(buttonNameSearch.search(eq(webElement), any(SearchParameters.class))).thenReturn(list);
        when(((IElementFilterAction) searchAction).filter(list, LINK_TEXT)).thenReturn(list);
        List<WebElement> foundElements = searchActions.findElements(searchContext, attributes);
        webElements.remove(wrongElement);
        assertEquals(webElements, foundElements);
    }

    @Test
    void testFindElementsWithChildrenEmptyResults()
    {
        List<WebElement> webElements = new ArrayList<>();
        webElements.add(webElement);
        createAndSetElementActionsMap();
        SearchAttributes attributes = new SearchAttributes(ActionAttributeType.CASE_SENSITIVE_TEXT, XPATH_LOCATOR);
        SearchAttributes childAttributes = new SearchAttributes(ActionAttributeType.BUTTON_NAME, BUTTON_NAME);
        attributes.addChildSearchAttributes(childAttributes);
        when(caseSensitivetextSearch.search(eq(searchContext), any(SearchParameters.class))).thenReturn(webElements);
        when(buttonNameSearch.search(eq(webElement), any(SearchParameters.class))).thenReturn(List.of());
        List<WebElement> foundElements = searchActions.findElements(searchContext, attributes);
        assertEquals(List.of(), foundElements);
    }

    private void createAndSetElementActionsMap()
    {
        Map<IActionAttributeType, IElementAction> map = new HashMap<>();
        map.put(ActionAttributeType.CASE_SENSITIVE_TEXT, caseSensitivetextSearch);
        map.put(ActionAttributeType.LINK_URL, searchAction);
        map.put(ActionAttributeType.LINK_TEXT, searchAction);
        map.put(ActionAttributeType.DEFAULT, defaultSearch);
        map.put(ActionAttributeType.BUTTON_NAME, buttonNameSearch);
        searchActions.setElementActions(map);
    }
}
