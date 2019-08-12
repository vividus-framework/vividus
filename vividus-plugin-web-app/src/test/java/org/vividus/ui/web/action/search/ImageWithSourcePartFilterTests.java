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

package org.vividus.ui.web.action.search;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;

@ExtendWith(MockitoExtension.class)
class ImageWithSourcePartFilterTests
{
    private static final String SRC_ATTRIBUTE = "src";
    private static final String SRC_VALUE = "/srcValue";
    private static final String SRC_PART = "/src";

    private final List<WebElement> webElements = new ArrayList<>();
    private final ImageWithSourcePartFilter search = new ImageWithSourcePartFilter();

    @Mock
    private WebElement webElement;

    @Mock
    private SearchContext searchContext;

    @BeforeEach
    void beforeEach()
    {
        webElements.add(webElement);
    }

    @Test
    void testFilterSuccess()
    {
        when(webElement.getAttribute(SRC_ATTRIBUTE)).thenReturn(SRC_VALUE);
        List<WebElement> foundElements = search.filter(webElements, SRC_PART);
        assertEquals(webElements, foundElements);
    }

    @Test
    void testFilterSrcPartNull()
    {
        search.filter(webElements, null);
        verify(webElement, never()).getAttribute(SRC_ATTRIBUTE);
    }

    @Test
    void testFilterSrcPartEmpty()
    {
        search.filter(webElements, "");
        verify(webElement, never()).getAttribute(SRC_ATTRIBUTE);
    }

    @Test
    void testFilterWrongSrcPartValue()
    {
        when(webElement.getAttribute(SRC_ATTRIBUTE)).thenReturn(SRC_VALUE);
        List<WebElement> foundElements = search.filter(webElements, "wrongValue");
        assertTrue(foundElements.isEmpty());
    }
}
