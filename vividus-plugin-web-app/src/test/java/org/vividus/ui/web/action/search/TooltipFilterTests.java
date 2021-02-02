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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebElement;

@ExtendWith(MockitoExtension.class)
class TooltipFilterTests
{
    private static final String TITLE = "title";
    private static final String TOOLTIP = "tooltip";

    private final TooltipFilter search = new TooltipFilter();
    private final List<WebElement> webElements = new ArrayList<>();

    @Mock
    private WebElement webElement;

    @BeforeEach
    void beforeEach()
    {
        webElements.add(webElement);
    }

    @Test
    void testSearchLinksByUrlPart()
    {
        when(webElement.getAttribute(TITLE)).thenReturn(TOOLTIP);
        List<WebElement> foundElements = search.filter(webElements, TOOLTIP);
        assertEquals(webElements, foundElements);
    }

    @Test
    void testSearchWithTooltipNull()
    {
        List<WebElement> foundElements = search.filter(webElements, null);
        assertEquals(webElements, foundElements);
    }

    @Test
    void testSearchWithTooltipIsEmpty()
    {
        List<WebElement> foundElements = search.filter(webElements, "");
        assertEquals(webElements, foundElements);
    }

    @Test
    void testSearchLinksByUrlPartNotMatch()
    {
        when(webElement.getAttribute(TITLE)).thenReturn("otherTooltip");
        List<WebElement> foundElements = search.filter(webElements, TOOLTIP);
        assertTrue(foundElements.isEmpty());
    }

    @Test
    void testSearchLinksByUrlPartNoHref()
    {
        when(webElement.getAttribute(TITLE)).thenReturn(null);
        List<WebElement> foundElements = search.filter(webElements, TOOLTIP);
        assertTrue(foundElements.isEmpty());
    }
}
