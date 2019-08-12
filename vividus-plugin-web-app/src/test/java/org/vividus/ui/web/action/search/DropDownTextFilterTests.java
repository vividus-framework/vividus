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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

@ExtendWith(MockitoExtension.class)
class DropDownTextFilterTests
{
    private static final String SOME_TEXT = "Some text";
    private static final String SELECT = "select";
    private static final String OPTION = "option";

    @Mock
    private WebElement webElement;

    @InjectMocks
    private DropDownTextFilter fieldTextFilter;

    @Test
    void testTextFilter()
    {
        stubDropDown();
        List<WebElement> webElements = List.of(webElement);
        List<WebElement> foundElements = fieldTextFilter.filter(webElements, SOME_TEXT);
        assertEquals(webElements, foundElements);
    }

    @Test
    void testTextFilterNoSelectedOptions()
    {
        WebElement option = mock(WebElement.class);
        List<WebElement> options = List.of(option);
        when(webElement.getTagName()).thenReturn(SELECT);
        when(webElement.findElements(By.tagName(OPTION))).thenReturn(options);
        when(option.isSelected()).thenReturn(false);
        List<WebElement> webElements = List.of(webElement);
        List<WebElement> foundElements = fieldTextFilter.filter(webElements, SOME_TEXT);
        assertNotEquals(webElements, foundElements);
    }

    @Test
    void testTextFilterWrongOptionText()
    {
        stubDropDown();
        List<WebElement> webElements = List.of(webElement);
        List<WebElement> foundElements = fieldTextFilter.filter(webElements, "Wrong text");
        assertNotEquals(webElements, foundElements);
    }

    @Test
    void testTextFilterEmptyText()
    {
        List<WebElement> webElements = List.of(webElement);
        List<WebElement> foundElements = fieldTextFilter.filter(webElements, "");
        assertEquals(webElements, foundElements);
    }

    @Test
    void testTextFilterNullText()
    {
        List<WebElement> webElements = List.of(webElement);
        List<WebElement> foundElements = fieldTextFilter.filter(webElements, null);
        assertEquals(webElements, foundElements);
    }

    private void stubDropDown()
    {
        WebElement option = mock(WebElement.class);
        List<WebElement> options = Collections.singletonList(option);
        when(webElement.getTagName()).thenReturn(SELECT);
        when(webElement.findElements(By.tagName(OPTION))).thenReturn(options);
        when(option.getText()).thenReturn(SOME_TEXT);
        when(option.isSelected()).thenReturn(true);
    }
}
