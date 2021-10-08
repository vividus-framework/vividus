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
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import com.codeborne.selenide.selector.ByShadow;
import com.codeborne.selenide.selector.ByShadow.ByShadowCss;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.How;
import org.vividus.ui.action.ElementActions;
import org.vividus.ui.action.search.SearchParameters;
import org.vividus.ui.action.search.Visibility;

@ExtendWith(MockitoExtension.class)
class ShadowCssSelectorSearchTests
{
    private static final String TARGET_VALUE = "h1";
    private static final String UPPER_HOST = "div1";
    private static final String INNER_HOST = "div2";

    @Mock private ElementActions elementActions;
    @InjectMocks private final ShadowCssSelectorSearch searchAction = new ShadowCssSelectorSearch(
            WebLocatorType.SHADOW_CSS_SELECTOR, How.CSS);

    @Test
    void shouldFindAllShadowElements()
    {
        try (MockedStatic<ByShadow> byShadowMock = mockStatic(ByShadow.class))
        {
            var webElements = List.of(mock(WebElement.class));
            var searchContext = mock(SearchContext.class);
            var searchParameters = new ShadowDomSearchParameters(TARGET_VALUE, UPPER_HOST, INNER_HOST);
            searchParameters.setVisibility(Visibility.ALL);
            var byShadowCss = mock(ByShadowCss.class);
            byShadowMock.when(() -> ByShadow.cssSelector(TARGET_VALUE, UPPER_HOST, INNER_HOST)).thenReturn(byShadowCss);
            when(byShadowCss.findElements(searchContext)).thenReturn(webElements);
            assertEquals(webElements, searchAction.search(searchContext, searchParameters));
            verifyNoInteractions(elementActions);
        }
    }

    @Test
    void shouldFindOnlyVisibleShadowElements()
    {
        try (MockedStatic<ByShadow> byShadowMock = mockStatic(ByShadow.class))
        {
            var visibleElement = mock(WebElement.class);
            when(elementActions.isElementVisible(visibleElement)).thenReturn(true);
            var invisibleElement = mock(WebElement.class);
            when(elementActions.isElementVisible(invisibleElement)).thenReturn(false);
            var searchContext = mock(SearchContext.class);
            var searchParameters = new ShadowDomSearchParameters(TARGET_VALUE, UPPER_HOST);
            searchParameters.setVisibility(Visibility.VISIBLE);
            var byShadowCss = mock(ByShadowCss.class);
            byShadowMock.when(() -> ByShadow.cssSelector(TARGET_VALUE, UPPER_HOST)).thenReturn(byShadowCss);
            when(byShadowCss.findElements(searchContext)).thenReturn(List.of(visibleElement, invisibleElement));
            assertEquals(List.of(visibleElement), searchAction.search(searchContext, searchParameters));
        }
    }

    @Test
    void shouldNotPerformFilteringOfEmptyResult()
    {
        try (MockedStatic<ByShadow> byShadowMock = mockStatic(ByShadow.class))
        {
            var searchContext = mock(SearchContext.class);
            var searchParameters = new ShadowDomSearchParameters(TARGET_VALUE, UPPER_HOST);
            var byShadowCss = mock(ByShadowCss.class);
            byShadowMock.when(() -> ByShadow.cssSelector(TARGET_VALUE, UPPER_HOST)).thenReturn(byShadowCss);
            when(byShadowCss.findElements(searchContext)).thenReturn(List.of());
            assertEquals(List.of(), searchAction.search(searchContext, searchParameters));
            verifyNoInteractions(elementActions);
        }
    }

    @Test
    void shouldFindNothingWithNonShadowDomSearchParameters()
    {
        SearchContext searchContext = mock(SearchContext.class);
        ShadowCssSelectorSearch search = new ShadowCssSelectorSearch(WebLocatorType.SHADOW_CSS_SELECTOR, How.CSS);
        assertEquals(List.of(), search.search(searchContext, new SearchParameters(TARGET_VALUE)));
    }
}
