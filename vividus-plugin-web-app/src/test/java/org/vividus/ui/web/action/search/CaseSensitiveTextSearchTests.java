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

package org.vividus.ui.web.action.search;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.vividus.ui.action.search.SearchParameters;
import org.vividus.ui.web.action.IWebElementActions;

@ExtendWith(MockitoExtension.class)
class CaseSensitiveTextSearchTests
{
    private static final String TEXT = "Text";
    private static final String ANY = "*";
    private static final String XPATH_FORMAT = ".//*[normalize-space(.)=\"%1$s\" and "
            + "not(.//*[normalize-space(.)=\"%1$s\"])]";
    private static final By FULL_INNER_TEXT_LOCATOR = By.xpath(String.format(XPATH_FORMAT, TEXT));
    private static final String SOME_TEXT_LOWER_CASE = "some text";
    private static final String SOME_TEXT_UPPER_CASE = "SOME TEXT";
    private static final String TEXT_TRANSFORM = "text-transform";
    private static final String EMPTY_STRING = "";
    private static final String UPPERCASE = "uppercase";

    private final SearchParameters parameters = new SearchParameters(TEXT);

    @Mock
    private WebElement webElement;

    @Mock
    private IWebElementActions webElementActions;

    @InjectMocks
    private CaseSensitiveTextSearch caseSensitiveTextSearch;

    @Test
    void testFindLinksByTextByFullLinkText()
    {
        SearchContext searchContext = mock(SearchContext.class);
        CaseSensitiveTextSearch spy = Mockito.spy(caseSensitiveTextSearch);
        List<WebElement> webElements = List.of(mock(WebElement.class));
        doReturn(webElements).when(spy).findElementsByText(searchContext, FULL_INNER_TEXT_LOCATOR, parameters, ANY);
        List<WebElement> foundElements = spy.search(searchContext, parameters);
        assertEquals(webElements, foundElements);
    }

    @Test
    void testFindLinksByTextByLinkText()
    {
        By innerTextLocator = By.xpath(String.format(".//*[contains(normalize-space(.), \"%1$s\") and "
                + "not(.//*[contains(normalize-space(.), \"%1$s\")])]", TEXT));
        SearchContext searchContext = mock(SearchContext.class);
        CaseSensitiveTextSearch spy = Mockito.spy(caseSensitiveTextSearch);
        List<WebElement> webElements = List.of(mock(WebElement.class));
        doReturn(List.of()).when(spy).findElementsByText(searchContext, FULL_INNER_TEXT_LOCATOR, parameters, ANY);
        doReturn(webElements).when(spy).findElementsByText(searchContext, innerTextLocator, parameters, ANY);
        List<WebElement> foundElements = spy.search(searchContext, parameters);
        assertEquals(webElements, foundElements);
    }

    @Test
    void testFindElementsByTextNullSearchContext()
    {
        List<WebElement> foundElements = caseSensitiveTextSearch.search(null, parameters);
        assertEquals(List.of(), foundElements);
    }

    @Test
    void testTextFilter()
    {
        when(webElementActions.getElementText(webElement)).thenReturn(SOME_TEXT_LOWER_CASE);
        List<WebElement> webElements = List.of(webElement);
        List<WebElement> foundElements =  caseSensitiveTextSearch.filter(webElements, SOME_TEXT_LOWER_CASE);
        assertEquals(webElements, foundElements);
    }

    @Test
    void testTextFilterWithTransformation()
    {
        when(webElementActions.getElementText(webElement)).thenReturn(SOME_TEXT_LOWER_CASE);
        when(webElementActions.getCssValue(webElement, TEXT_TRANSFORM)).thenReturn(UPPERCASE);
        List<WebElement> webElements = List.of(webElement);
        List<WebElement> foundElements =  caseSensitiveTextSearch.filter(webElements, SOME_TEXT_UPPER_CASE);
        assertEquals(webElements, foundElements);
    }

    @Test
    void testTextFilterWithTransformationDiffText()
    {
        when(webElementActions.getElementText(webElement)).thenReturn(SOME_TEXT_LOWER_CASE);
        List<WebElement> foundElements =  caseSensitiveTextSearch.filter(List.of(webElement), "another text");
        assertEquals(List.of(), foundElements);
    }

    @Test
    void testTextElementFilteredOut()
    {
        when(webElementActions.getElementText(webElement)).thenReturn(SOME_TEXT_LOWER_CASE);
        when(webElementActions.getCssValue(webElement, TEXT_TRANSFORM)).thenReturn(EMPTY_STRING);
        List<WebElement> webElements = List.of(webElement);
        List<WebElement> foundElements = caseSensitiveTextSearch.filter(webElements, SOME_TEXT_UPPER_CASE);
        assertNotEquals(webElements, foundElements);
    }

    @Test
    void testTextFilterNull()
    {
        testTextFilterEmptyOrNull(null);
    }

    @Test
    void testTextFilterEmpty()
    {
        testTextFilterEmptyOrNull(EMPTY_STRING);
    }

    private void testTextFilterEmptyOrNull(String text)
    {
        List<WebElement> webElements = List.of(webElement);
        List<WebElement> filteredText = caseSensitiveTextSearch.filter(webElements, text);
        assertEquals(filteredText, webElements);
        verifyNoInteractions(webElementActions);
    }
}
