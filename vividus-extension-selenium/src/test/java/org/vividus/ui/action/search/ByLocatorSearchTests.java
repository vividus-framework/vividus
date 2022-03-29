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

package org.vividus.ui.action.search;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.vividus.testdouble.TestLocatorType;

class ByLocatorSearchTests
{
    @Test
    void testSearch()
    {
        TestLocatorType locatorType = TestLocatorType.ADDITIONAL_SEARCH;
        List<WebElement> webElements = List.of(mock(WebElement.class));
        SearchContext searchContext = mock(SearchContext.class);
        SearchParameters parameters = new SearchParameters("locator", Visibility.ALL, false);
        ByLocatorSearch searchAction = new ByLocatorSearch(locatorType);

        when(searchContext.findElements(locatorType.buildBy(parameters.getValue()))).thenReturn(webElements);

        assertEquals(webElements, searchAction.search(searchContext, parameters, Map.of()));
    }
}
