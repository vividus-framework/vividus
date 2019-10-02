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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebElement;

@ExtendWith(MockitoExtension.class)
class PlaceholderFilterTests
{
    private static final String TEST_PLACEHOLDER = "testPlaceholder";

    private List<WebElement> webElements;

    @Mock
    private WebElement webElement;

    @InjectMocks
    private PlaceholderFilter filter;

    @BeforeEach
    void beforeEach()
    {
        webElements = List.of(webElement);
    }

    @Test
    void testPlaceholderFilter()
    {
        when(webElement.getAttribute("placeholder")).thenReturn(TEST_PLACEHOLDER);
        List<WebElement> foundElements = filter.filter(webElements, TEST_PLACEHOLDER);
        assertEquals(webElements, foundElements);
    }

    @Test
    void testPlaceholderFilteredOut()
    {
        List<WebElement> foundElements = filter.filter(webElements, "anyText");
        assertNotEquals(webElements, foundElements);
    }

    @Test
    void testPlaceholderFilterNull()
    {
        List<WebElement> foundElements = filter.filter(webElements, null);
        verifyNoInteractions(webElement);
        assertEquals(webElements, foundElements);
    }

    @Test
    void testPlaceholderFilterEmpty()
    {
        List<WebElement> foundElements = filter.filter(webElements, "");
        verifyNoInteractions(webElement);
        assertEquals(webElements, foundElements);
    }
}
