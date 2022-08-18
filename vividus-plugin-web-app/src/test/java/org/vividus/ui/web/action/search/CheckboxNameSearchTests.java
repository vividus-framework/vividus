/*
 * Copyright 2019-2022 the original author or authors.
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.vividus.selenium.element.Checkbox;
import org.vividus.ui.action.search.SearchParameters;
import org.vividus.ui.action.search.Visibility;
import org.vividus.ui.util.XpathLocatorUtils;
import org.vividus.ui.web.action.IWebElementActions;

@ExtendWith(MockitoExtension.class)
class CheckboxNameSearchTests
{
    private static final String FOR = "for";
    private static final String VALUE = "value";
    private static final String ATTRIBUTE_VALUE = "checkBoxId";
    private static final By ATTRIBUTE_LOCATOR = XpathLocatorUtils.getXPathLocator(
            ".//input[@type='checkbox' and @id=%s]", ATTRIBUTE_VALUE);
    private static final By CHECKBOX_LABEL_LOCATOR = XpathLocatorUtils.getXPathLocator(".//label[text()='" + VALUE
            + "' and (preceding-sibling::input or following-sibling::input or child::input)]");
    private static final By CHECKBOX_LABEL_DEEP_LOCATOR = XpathLocatorUtils
            .getXPathLocator("label[preceding-sibling::input or following-sibling::input or child::input]");
    private static final By PRECEDING_SIBLING_CHECKBOX_LOCATOR = XpathLocatorUtils
            .getXPathLocator("preceding-sibling::input[@type='checkbox']");
    private static final By FOLLOWING_SIBLING_CHECKBOX_LOCATOR = XpathLocatorUtils
            .getXPathLocator("following-sibling::input[@type='checkbox']");
    private static final By CHILD_CHECKBOX_LOCATOR = XpathLocatorUtils
            .getXPathLocator("child::input[@type='checkbox']");

    private final SearchParameters parameters = new SearchParameters(VALUE, Visibility.ALL, false);

    @Mock
    private IWebElementActions webElementActions;

    @Mock
    private SearchContext searchContext;

    @InjectMocks
    private CheckboxNameSearch checkboxNameSearch;

    @Test
    void testSearchSuccessByForAttribute()
    {
        WebElement checkbox = mock(WebElement.class);
        WebElement label = mock(WebElement.class);
        when(searchContext.findElements(CHECKBOX_LABEL_LOCATOR)).thenReturn(List.of(label));
        when(label.getAttribute(FOR)).thenReturn(ATTRIBUTE_VALUE);
        when(searchContext.findElements(ATTRIBUTE_LOCATOR)).thenReturn(List.of(checkbox));
        List<WebElement> foundElements = checkboxNameSearch.search(searchContext, parameters);
        assertEquals(1, foundElements.size());
        Checkbox foundElement = (Checkbox) foundElements.get(0);
        assertEquals(checkbox, foundElement.getWrappedElement());
        assertEquals(label, foundElement.getLabelElement());
    }

    @ParameterizedTest
    @MethodSource("locatorProvider")
    void testSearchSuccessByCheckboxLocators(By checkboxLocator)
    {
        WebElement checkbox = mock(WebElement.class);
        WebElement label = mock(WebElement.class);
        when(searchContext.findElements(CHECKBOX_LABEL_LOCATOR)).thenReturn(List.of(label));
        when(label.getAttribute(FOR)).thenReturn(null);
        when(label.findElements(Mockito.any(By.class))).thenReturn(List.of());
        when(label.findElements(checkboxLocator)).thenReturn(List.of(checkbox));
        List<WebElement> foundElements = checkboxNameSearch.search(searchContext, parameters);
        assertEquals(1, foundElements.size());
        Checkbox foundElement = (Checkbox) foundElements.get(0);
        assertEquals(checkbox, foundElement.getWrappedElement());
        assertEquals(label, foundElement.getLabelElement());
    }

    void testSearchSuccessMergeByCheckboxLocators(By checkboxLocator)
    {
        WebElement checkboxFollowing = mock(WebElement.class);
        WebElement checkboxPreceding = mock(WebElement.class);
        WebElement checkboxChild = mock(WebElement.class);
        final List<WebElement> expectedFoundElements = List.of(checkboxFollowing, checkboxPreceding, checkboxChild);
        WebElement label = mock(WebElement.class);
        when(searchContext.findElements(CHECKBOX_LABEL_LOCATOR)).thenReturn(List.of(label));
        when(label.getAttribute(FOR)).thenReturn(null);
        when(label.findElements(PRECEDING_SIBLING_CHECKBOX_LOCATOR)).thenReturn(List.of(checkboxPreceding));
        when(label.findElements(FOLLOWING_SIBLING_CHECKBOX_LOCATOR)).thenReturn(List.of(checkboxFollowing));
        when(label.findElements(CHILD_CHECKBOX_LOCATOR)).thenReturn(List.of(checkboxChild));
        List<WebElement> foundElements = checkboxNameSearch.search(searchContext, parameters);
        assertEquals(3, foundElements.size());
        assertTrue(foundElements.containsAll(expectedFoundElements));
        foundElements.forEach(e -> assertEquals(label, ((Checkbox) e).getLabelElement()));
    }

    @Test
    void testSearchSuccessEmptySiblings()
    {
        WebElement label = mock(WebElement.class);
        when(searchContext.findElements(CHECKBOX_LABEL_LOCATOR)).thenReturn(List.of(label));
        when(searchContext.findElements(CHECKBOX_LABEL_DEEP_LOCATOR)).thenReturn(List.of());
        when(label.findElements(PRECEDING_SIBLING_CHECKBOX_LOCATOR)).thenReturn(List.of());
        when(label.findElements(FOLLOWING_SIBLING_CHECKBOX_LOCATOR)).thenReturn(List.of());
        when(label.findElements(CHILD_CHECKBOX_LOCATOR)).thenReturn(List.of());
        assertThat(checkboxNameSearch.search(searchContext, parameters), empty());
    }

    @Test
    void testSearchSuccessByDeepLocator()
    {
        WebElement checkbox = mock(WebElement.class);
        WebElement label = mock(WebElement.class);
        when(searchContext.findElements(CHECKBOX_LABEL_LOCATOR)).thenReturn(List.of());
        when(searchContext.findElements(CHECKBOX_LABEL_DEEP_LOCATOR)).thenReturn(List.of(label));
        when(webElementActions.getElementText(label)).thenReturn(VALUE);
        when(label.findElements(PRECEDING_SIBLING_CHECKBOX_LOCATOR)).thenReturn(List.of(checkbox));
        List<WebElement> foundElements = checkboxNameSearch.search(searchContext, parameters);
        assertEquals(1, foundElements.size());
        Checkbox foundElement = (Checkbox) foundElements.get(0);
        assertEquals(checkbox, foundElement.getWrappedElement());
        assertEquals(label, foundElement.getLabelElement());
    }

    static Stream<By> locatorProvider()
    {
        return Stream.of(PRECEDING_SIBLING_CHECKBOX_LOCATOR, FOLLOWING_SIBLING_CHECKBOX_LOCATOR,
                CHILD_CHECKBOX_LOCATOR);
    }
}
