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
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebElement;
import org.vividus.ui.web.action.IJavascriptActions;

@ExtendWith(MockitoExtension.class)
class FieldTextFilterTests
{
    private static final String SOME_TEXT = "Some text";

    private List<WebElement> webElements;

    @Mock
    private WebElement webElement;

    @Mock
    private IJavascriptActions javascriptActions;

    @InjectMocks
    private FieldTextFilter fieldTextFilter;

    @BeforeEach
    void beforeEach()
    {
        webElements = new ArrayList<>();
        webElements.add(webElement);
    }

    @Test
    void testTextFilter()
    {
        when(javascriptActions.getElementValue(webElement)).thenReturn(SOME_TEXT);
        List<WebElement> foundElements = fieldTextFilter.filter(webElements, SOME_TEXT);
        assertEquals(webElements, foundElements);
    }

    @Test
    void testTextElementFilteredOut()
    {
        when(javascriptActions.getElementValue(webElement)).thenReturn(SOME_TEXT);
        List<WebElement> foundElements = fieldTextFilter.filter(webElements, "any text");
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
        testTextFilterEmptyOrNull("");
    }

    private void testTextFilterEmptyOrNull(String text)
    {
        List<WebElement> filteredText = fieldTextFilter.filter(webElements, text);
        assertEquals(filteredText, webElements);
        verifyNoInteractions(javascriptActions);
    }
}
