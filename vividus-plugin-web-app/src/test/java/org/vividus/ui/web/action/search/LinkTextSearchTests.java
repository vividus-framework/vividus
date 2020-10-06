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
import static org.mockito.Mockito.doReturn;
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
import org.vividus.ui.action.search.Visibility;

@ExtendWith(MockitoExtension.class)
class LinkTextSearchTests
{
    private static final String LINK_TEXT = "linkText";
    private static final By DEFAULT_LOCATOR = By.linkText(LINK_TEXT);
    private static final String LINK_TAG = "a";
    private static final By LINK_TEXT_LOCATOR = By.xpath(".//a[text()[normalize-space()=\"" + LINK_TEXT
            + "\"] or @*[normalize-space()=\"" + LINK_TEXT + "\"] or *[normalize-space()=\"" + LINK_TEXT + "\"]]");

    private List<WebElement> webElements;

    @Mock
    private WebElement webElement;

    @Mock
    private SearchContext searchContext;

    @InjectMocks
    private LinkTextSearch search;

    @Test
    void testFindLinksByTextByLinkTextNull()
    {
        search = new LinkTextSearch();
        LinkTextSearch spy = Mockito.spy(search);
        SearchParameters parameters = new SearchParameters(LINK_TEXT, Visibility.ALL, false);
        when(searchContext.findElements(DEFAULT_LOCATOR)).thenReturn(List.of());
        doReturn(List.of()).when(spy).findElementsByText(searchContext, LINK_TEXT_LOCATOR, parameters, LINK_TAG);
        List<WebElement> foundElements = spy.search(searchContext, parameters);
        assertEquals(List.of(), foundElements);
    }

    @Test
    void testFindLinksByTextByLinkText()
    {
        search = new LinkTextSearch();
        webElements = List.of(webElement);
        SearchParameters parameters = new SearchParameters(LINK_TEXT, Visibility.ALL, false);
        when(searchContext.findElements(DEFAULT_LOCATOR)).thenReturn(webElements);
        List<WebElement> foundElements = search.search(searchContext, parameters);
        assertEquals(webElements, foundElements);
    }

    @Test
    void testFindLinksByTextByLinkTextOrAttributeLocator()
    {
        search = new LinkTextSearch();
        LinkTextSearch spy = Mockito.spy(search);
        SearchParameters parameters = new SearchParameters(LINK_TEXT, Visibility.ALL, false);
        when(searchContext.findElements(DEFAULT_LOCATOR)).thenReturn(List.of());
        doReturn(webElements).when(spy).findElementsByText(searchContext, LINK_TEXT_LOCATOR, parameters, LINK_TAG);
        List<WebElement> foundElements = spy.search(searchContext, parameters);
        assertEquals(webElements, foundElements);
    }

    @Test
    void testFindLinksByTextNullSearchContext()
    {
        search = new LinkTextSearch();
        SearchParameters parameters = new SearchParameters(LINK_TEXT);
        List<WebElement> foundElements = search.search(null, parameters);
        assertEquals(List.of(), foundElements);
    }
}
