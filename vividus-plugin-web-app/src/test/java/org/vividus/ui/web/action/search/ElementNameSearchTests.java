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
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;

@ExtendWith(MockitoExtension.class)
class ElementNameSearchTests
{
    private static final String ELEMENT_NAME = "Element name";

    private final SearchParameters searchParameters = new SearchParameters(ELEMENT_NAME);

    @InjectMocks
    private ElementNameSearch elementNameSearch;

    @Test
    void searchTest()
    {
        SearchContext searchContext = mock(SearchContext.class);
        ElementNameSearch spy = Mockito.spy(elementNameSearch);
        List<WebElement> webElements = new ArrayList<>();
        webElements.add(mock(WebElement.class));
        doReturn(webElements).when(spy).findElementsByText(searchContext,
                By.xpath(".//*[@*[normalize-space()=\"Element name\"] or text()[normalize-space()=\"Element name\"]]"),
                searchParameters, "*");
        List<WebElement> foundElements = spy.search(searchContext, searchParameters);
        assertEquals(webElements, foundElements);
    }

    @Test
    void searchTestContextNull()
    {
        ElementNameSearch spy = Mockito.spy(elementNameSearch);
        List<WebElement> foundElements = spy.search(null, searchParameters);
        assertEquals(List.of(), foundElements);
    }
}
