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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.vividus.selenium.element.Checkbox;
import org.vividus.ui.web.action.IWebElementActions;
import org.vividus.ui.web.util.LocatorUtil;

@ExtendWith(MockitoExtension.class)
class CheckboxNameSearchTests
{
    private static final String INPUT_PATTERN = "input[@type='checkbox']";
    private static final String FOR = "for";
    private static final String VALUE = "value";
    private static final String ATTRIBUTE_VALUE = "checkBoxId";
    private static final By ATTRIBUTE_LOCATOR = LocatorUtil.getXPathLocator(".//input[@type='checkbox' and @id=%s]",
            ATTRIBUTE_VALUE);
    private static final By CHECKBOX_LABEL_LOCATOR = LocatorUtil.getXPathLocator(".//label[text()='" + VALUE
            + "' and (preceding-sibling::input or following-sibling::input or child::input)]");
    private static final By CHECKBOX_LABEL_DEEP_LOCATOR = LocatorUtil
            .getXPathLocator("label[preceding-sibling::input or following-sibling::input or child::input]");
    private static final By INPUT_LOCATOR = LocatorUtil.getXPathLocator(INPUT_PATTERN);
    private static final By PRECEDING_SIBLING_CHECKBOX_LOCATOR = LocatorUtil
            .getXPathLocator("preceding-sibling::" + INPUT_PATTERN);

    private final SearchParameters parameters = new SearchParameters(VALUE);

    private CheckboxNameSearch spy;

    @Mock
    private IWebElementActions webElementActions;

    @Mock
    private SearchContext searchContext;

    @InjectMocks
    private CheckboxNameSearch checkboxNameSearch;

    @Test
    void testSearchSuccessByForAttribute()
    {
        spy = Mockito.spy(checkboxNameSearch);
        WebElement checkbox = mock(WebElement.class);
        WebElement label = mock(WebElement.class);
        when(spy.findElements(eq(searchContext), eq(CHECKBOX_LABEL_LOCATOR), any(SearchParameters.class)))
                .thenReturn(List.of(label));
        when(label.getAttribute(FOR)).thenReturn(ATTRIBUTE_VALUE);
        when(spy.findElements(eq(searchContext), eq(ATTRIBUTE_LOCATOR), any(SearchParameters.class)))
                .thenReturn(List.of(checkbox));
        List<WebElement> foundElements = spy.search(searchContext, parameters);
        assertEquals(1, foundElements.size());
        Checkbox foundElement = (Checkbox) foundElements.get(0);
        assertEquals(checkbox, foundElement.getWrappedElement());
        assertEquals(label, foundElement.getLabelElement());
    }

    @Test
    void testSearchSuccessBySibling()
    {
        spy = Mockito.spy(checkboxNameSearch);
        WebElement checkbox = mock(WebElement.class);
        WebElement label = mock(WebElement.class);
        when(spy.findElements(eq(searchContext), eq(CHECKBOX_LABEL_LOCATOR), any(SearchParameters.class)))
                .thenReturn(List.of(label));
        when(label.getAttribute(FOR)).thenReturn(null);
        when(label.findElements(PRECEDING_SIBLING_CHECKBOX_LOCATOR)).thenReturn(List.of(checkbox));
        List<WebElement> foundElements = spy.search(searchContext, parameters);
        assertEquals(1, foundElements.size());
        Checkbox foundElement = (Checkbox) foundElements.get(0);
        assertEquals(checkbox, foundElement.getWrappedElement());
        assertEquals(label, foundElement.getLabelElement());
    }

    @Test
    void testSearchSuccessByCheckboxLocator()
    {
        spy = Mockito.spy(checkboxNameSearch);
        WebElement checkbox = mock(WebElement.class);
        when(spy.findElements(eq(searchContext), eq(CHECKBOX_LABEL_LOCATOR), any(SearchParameters.class)))
                .thenReturn(List.of());
        when(spy.findElements(eq(searchContext), eq(CHECKBOX_LABEL_DEEP_LOCATOR), any(SearchParameters.class)))
                .thenReturn(List.of());
        when(spy.findElements(eq(searchContext), eq(INPUT_LOCATOR), any(SearchParameters.class)))
                .thenReturn(List.of(checkbox));
        when(webElementActions.getElementText(checkbox)).thenReturn(VALUE);
        List<WebElement> foundElements = spy.search(searchContext, parameters);
        assertEquals(1, foundElements.size());
        Checkbox foundElement = (Checkbox) foundElements.get(0);
        assertEquals(checkbox, foundElement.getWrappedElement());
        assertNull(foundElement.getLabelElement());
    }

    @Test
    void testSearchSuccessWrongText()
    {
        spy = Mockito.spy(checkboxNameSearch);
        WebElement checkbox = mock(WebElement.class);
        WebElement label = mock(WebElement.class);
        when(spy.findElements(eq(searchContext), eq(CHECKBOX_LABEL_LOCATOR), any(SearchParameters.class)))
                .thenReturn(List.of());
        when(spy.findElements(eq(searchContext), eq(CHECKBOX_LABEL_DEEP_LOCATOR), any(SearchParameters.class)))
                .thenReturn(List.of(label));
        when(webElementActions.getElementText(label)).thenReturn("wrongText");
        when(webElementActions.getElementText(checkbox)).thenReturn(VALUE);
        when(spy.findElements(eq(searchContext), eq(INPUT_LOCATOR), any(SearchParameters.class)))
                .thenReturn(List.of(checkbox));
        List<WebElement> foundElements = spy.search(searchContext, parameters);
        assertEquals(1, foundElements.size());
        Checkbox foundElement = (Checkbox) foundElements.get(0);
        assertEquals(checkbox, foundElement.getWrappedElement());
        assertNull(foundElement.getLabelElement());
    }

    @Test
    void testSearchSuccessEmptySiblings()
    {
        spy = Mockito.spy(checkboxNameSearch);
        WebElement checkbox = mock(WebElement.class);
        WebElement label = mock(WebElement.class);
        when(spy.findElements(eq(searchContext), eq(CHECKBOX_LABEL_LOCATOR), any(SearchParameters.class)))
                .thenReturn(List.of(label));
        when(spy.findElements(eq(searchContext), eq(CHECKBOX_LABEL_DEEP_LOCATOR), any(SearchParameters.class)))
                .thenReturn(List.of());
        when(webElementActions.getElementText(checkbox)).thenReturn(VALUE);
        when(label.findElements(PRECEDING_SIBLING_CHECKBOX_LOCATOR)).thenReturn(List.of());
        when(spy.findElements(eq(searchContext), eq(INPUT_LOCATOR), any(SearchParameters.class)))
                .thenReturn(List.of(checkbox));
        List<WebElement> foundElements = spy.search(searchContext, parameters);
        assertEquals(1, foundElements.size());
        Checkbox foundElement = (Checkbox) foundElements.get(0);
        assertEquals(checkbox, foundElement.getWrappedElement());
        assertNull(foundElement.getLabelElement());
    }

    @Test
    void testSearchSuccessByDeepLocator()
    {
        spy = Mockito.spy(checkboxNameSearch);
        WebElement checkbox = mock(WebElement.class);
        WebElement label = mock(WebElement.class);
        when(spy.findElements(eq(searchContext), eq(CHECKBOX_LABEL_LOCATOR), any(SearchParameters.class)))
                .thenReturn(List.of());
        when(spy.findElements(eq(searchContext), eq(CHECKBOX_LABEL_DEEP_LOCATOR), any(SearchParameters.class)))
                .thenReturn(List.of(label));
        when(webElementActions.getElementText(label)).thenReturn(VALUE);
        when(label.findElements(PRECEDING_SIBLING_CHECKBOX_LOCATOR)).thenReturn(List.of(checkbox));
        List<WebElement> foundElements = spy.search(searchContext, parameters);
        assertEquals(1, foundElements.size());
        Checkbox foundElement = (Checkbox) foundElements.get(0);
        assertEquals(checkbox, foundElement.getWrappedElement());
        assertEquals(label, foundElement.getLabelElement());
    }
}
