/*
 * Copyright 2019-2021 the original author or authors.
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.util.List;

import com.codeborne.selenide.selector.ByShadow;
import com.codeborne.selenide.selector.ByShadow.ByShadowCss;

import org.junit.Test;
import org.mockito.MockedStatic;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.How;
import org.vividus.ui.action.search.SearchParameters;
import org.vividus.ui.action.search.Visibility;

public class ShadowCssSelectorSearchTests
{
    private static final String TARGET_VALUE = "h1";
    private static final String UPPER_HOST = "div1";
    private static final String INNER_HOST = "div2";

    @Test
    public void searchAll()
    {
        try (MockedStatic<ByShadow> mock = mockStatic(ByShadow.class))
        {
            List<WebElement> webElements = List.of(mock(WebElement.class));
            SearchContext searchContext = mock(SearchContext.class);
            ShadowDomSearchParameters searchParameters = new ShadowDomSearchParameters(TARGET_VALUE, UPPER_HOST,
                    INNER_HOST);
            searchParameters.setVisibility(Visibility.ALL);
            ShadowCssSelectorSearch search = new ShadowCssSelectorSearch(WebLocatorType.SHADOW_CSS_SELECTOR, How.CSS);
            ByShadowCss shadowSelector = mock(ByShadowCss.class);
            when(ByShadow.cssSelector(TARGET_VALUE, UPPER_HOST, INNER_HOST)).thenReturn(shadowSelector);
            when(shadowSelector.findElements(searchContext)).thenReturn(webElements);
            assertEquals(webElements, search.search(searchContext, searchParameters));
        }
    }

    @Test
    public void searchWithEmptyResult()
    {
        try (MockedStatic<ByShadow> mock = mockStatic(ByShadow.class))
        {
            SearchContext searchContext = mock(SearchContext.class);
            ShadowDomSearchParameters searchParameters = new ShadowDomSearchParameters(TARGET_VALUE, UPPER_HOST);
            ShadowCssSelectorSearch search = new ShadowCssSelectorSearch(WebLocatorType.SHADOW_CSS_SELECTOR, How.CSS);
            ByShadowCss shadowSelector = mock(ByShadowCss.class);
            when(ByShadow.cssSelector(TARGET_VALUE, UPPER_HOST)).thenReturn(shadowSelector);
            when(shadowSelector.findElements(searchContext)).thenReturn(List.of());
            assertEquals(List.of(), search.search(searchContext, searchParameters));
        }
    }

    @Test
    public void searchUsingSearchParameters()
    {
        SearchContext searchContext = mock(SearchContext.class);
        ShadowCssSelectorSearch search = new ShadowCssSelectorSearch(WebLocatorType.SHADOW_CSS_SELECTOR, How.CSS);
        assertEquals(List.of(), search.search(searchContext, new SearchParameters(TARGET_VALUE)));
    }
}
