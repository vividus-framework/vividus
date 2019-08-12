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
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebElement;

@ExtendWith(MockitoExtension.class)
class RelativeToParentWidthFilterTests
{
    private static final String CORRECT_ELEMENT_WIDTH = "300";
    private static final String INCORRECT_ELEMENT_WIDTH = "800";
    private static final String PARENT_WIDTH = "900";
    private static final String PARENT_PERCENTAGE_WIDTH = "32";

    private List<WebElement> webElements;

    @Mock
    private WebElement webElementWithCorrectRelativeToParentWidth;

    @Mock
    private WebElement webElementWithIncorrectRelativeToParentWidth;

    @Mock
    private WebElement webElementParent;

    @Mock
    private Dimension dimensionElementWithCorrectRelativeToParentWidth;

    @Mock
    private Dimension dimensionElementWithIncorrectRelativeToParentWidth;

    @Mock
    private Dimension dimensionParentElement;

    @InjectMocks
    private RelativeToParentWidthFilter filter;

    @Test
    void testRelativeToParentWidthFilter()
    {
        webElements = List.of(webElementWithCorrectRelativeToParentWidth);
        mockParentElement(webElementWithCorrectRelativeToParentWidth, webElementParent);
        mockWebElementWidth(webElementWithCorrectRelativeToParentWidth, CORRECT_ELEMENT_WIDTH,
                dimensionElementWithCorrectRelativeToParentWidth);
        mockWebElementWidth(webElementParent, PARENT_WIDTH, dimensionParentElement);
        List<WebElement> foundElements = filter.filter(webElements, PARENT_PERCENTAGE_WIDTH);
        assertEquals(webElements, foundElements);
    }

    @Test
    void testRelativeToParentWidthFilterWithIncorrectWidth()
    {
        webElements = List.of(webElementWithCorrectRelativeToParentWidth, webElementWithIncorrectRelativeToParentWidth);
        mockParentElement(webElementWithCorrectRelativeToParentWidth, webElementParent);
        mockParentElement(webElementWithIncorrectRelativeToParentWidth, webElementParent);
        mockWebElementWidth(webElementWithCorrectRelativeToParentWidth, CORRECT_ELEMENT_WIDTH,
                dimensionElementWithCorrectRelativeToParentWidth);
        mockWebElementWidth(webElementWithIncorrectRelativeToParentWidth, INCORRECT_ELEMENT_WIDTH,
                dimensionElementWithIncorrectRelativeToParentWidth);
        mockWebElementWidth(webElementParent, PARENT_WIDTH, dimensionParentElement);
        List<WebElement> foundElements = filter.filter(webElements, PARENT_PERCENTAGE_WIDTH);
        assertEquals(1, foundElements.size());
    }

    @Test
    void testRelativeToParentWidthFilterIncorrectRelativeToParentWidth()
    {
        webElements = List.of(webElementWithIncorrectRelativeToParentWidth);
        mockParentElement(webElementWithIncorrectRelativeToParentWidth, webElementParent);
        mockWebElementWidth(webElementParent, PARENT_WIDTH, dimensionParentElement);
        mockWebElementWidth(webElementWithIncorrectRelativeToParentWidth, INCORRECT_ELEMENT_WIDTH,
                dimensionElementWithIncorrectRelativeToParentWidth);
        List<WebElement> foundElements = filter.filter(webElements, PARENT_PERCENTAGE_WIDTH);
        assertTrue(foundElements.isEmpty());
    }

    @Test
    void testRelativeToParentWidthFilterEmptyRelativeToParentWidth()
    {
        webElements = List.of();
        List<WebElement> foundElements = filter.filter(webElements, "");
        assertEquals(List.of(), foundElements);
    }

    @Test
    void testRelativeToParentWidthFilterNullRelativeToParentWidth()
    {
        webElements = List.of();
        List<WebElement> foundElements = filter.filter(webElements, null);
        assertEquals(List.of(), foundElements);
    }

    private void mockWebElementWidth(WebElement webElement, String webElementWidth, Dimension dimension)
    {
        when(webElement.getSize()).thenReturn(dimension);
        when(dimension.getWidth()).thenReturn(Integer.parseInt(webElementWidth));
    }

    private void mockParentElement(WebElement webElement, WebElement webElementParent)
    {
        when(webElement.findElement(By.xpath(".."))).thenReturn(webElementParent);
    }
}
