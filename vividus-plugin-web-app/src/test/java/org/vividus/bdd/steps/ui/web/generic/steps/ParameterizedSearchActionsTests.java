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

package org.vividus.bdd.steps.ui.web.generic.steps;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.steps.Parameters;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.ui.web.action.ISearchActions;
import org.vividus.ui.web.action.IWebElementActions;
import org.vividus.ui.web.action.search.ActionAttributeType;
import org.vividus.ui.web.action.search.IElementFilterAction;
import org.vividus.ui.web.action.search.SearchAttributes;
import org.vividus.ui.web.action.search.SearchParameters;
import org.vividus.ui.web.action.search.Visibility;

@ExtendWith(MockitoExtension.class)
class ParameterizedSearchActionsTests
{
    private static final String ATTRIBUTE_NAME = "|attributeName|\n|name|";
    private static final String XPATH_BODY = "|xpath|\n|//body|";
    private static final String DELIMITER = "|";
    private static final String PROPERTY_NAME = "propertyName";
    private static final String BODY_XPATH = "//body";
    private static final String VALUE = "value";
    private static final String NAME = "name";

    @Mock
    private ISearchActions searchActions;

    @Mock
    private SearchContext searchContext;

    @Mock
    private WebElement webElement;

    @Mock
    private IWebDriverProvider webDriverProvider;

    @Mock
    private IElementFilterAction linkFilterAction;

    @Mock
    private IWebElementActions webElementActions;

    @InjectMocks
    private ParameterizedSearchActions parameterizedSearchActions;

    static Stream<Arguments> inputDataForTestFilterElementsWithCssProperty()
    {
        String cssValue = "cssValue";
        String cssValuePart = "cssValuePart";
        String valuePart = "valuePart";
        // @formatter:off
        return Stream.of(
                Arguments.of(List.of(),                       cssValuePart, valuePart, null),
                Arguments.of(List.of(),                       cssValuePart, valuePart, "another"),
                Arguments.of(List.of(mock(WebElement.class)), cssValuePart, valuePart, valuePart),
                Arguments.of(List.of(),                       cssValue,     VALUE,     null),
                Arguments.of(List.of(),                       cssValue,     VALUE,     "another_value"),
                Arguments.of(List.of(mock(WebElement.class)), cssValue,     VALUE,     VALUE)
        );
        // @formatter:on
    }

    @Test
    void testFindElements()
    {
        SearchInputData searchInputData = new SearchInputData(createParameters(XPATH_BODY), ActionAttributeType.XPATH);
        parameterizedSearchActions.findElements(searchContext, searchInputData);
        ArgumentCaptor<SearchAttributes> argument = ArgumentCaptor.forClass(SearchAttributes.class);
        verify(searchActions).findElements(eq(searchContext), argument.capture());
        assertEquals(BODY_XPATH, argument.getValue().getSearchParameters().getValue());
    }

    @Test
    void testFindElementsNoSearchAttributes()
    {
        SearchInputData searchInputData = new SearchInputData(createParameters("||\n||"), ActionAttributeType.XPATH);
        List<WebElement> actual = parameterizedSearchActions.findElements(searchContext, searchInputData);
        verifyNoInteractions(searchActions);
        assertEquals(List.of(), actual);
    }

    @Test
    void testFilterElementsWithAttributes()
    {
        List<WebElement> elements = List.of(webElement);
        when(webElement.getAttribute(NAME)).thenReturn(VALUE);
        assertEquals(elements, mockFindElementsWithAttribute(createParameters(NAME, VALUE), elements));
    }

    @Test
    void testFindElementsWithAttributes()
    {
        mockFindElementsWithAttribute(createParameters(NAME, VALUE), List.of());
        verify(searchActions).findElements(searchContext,
                new SearchAttributes(ActionAttributeType.XPATH, ".//*[normalize-space(@name)=\"value\"]"));
    }

    @Test
    void testFindElementsWithAttributeName()
    {
        mockFindElementsWithAttribute(createParameters(ATTRIBUTE_NAME), List.of());
        verify(searchActions).findElements(searchContext,
                new SearchAttributes(ActionAttributeType.XPATH, ".//*[@name]"));
    }

    @Test
    void testFilterElementsWithAttributesNotPerformed()
    {
        List<WebElement> elements = List.of(webElement);
        assertEquals(elements, mockFindElementsWithAttribute(createParameters(XPATH_BODY), elements));
    }

    @Test
    void testFilterElementsWithAdditionalAttributes()
    {
        String name2 = "name2";
        String value2 = "value2";
        String table = "|attributeName|attributeValue|attributeName2|attributeValue2|\n|name|value|name2|value2|";
        when(webElement.getAttribute(name2)).thenReturn(value2);
        List<WebElement> elements = List.of(webElement);
        when(webElement.getAttribute(NAME)).thenReturn(VALUE);
        assertEquals(elements, mockFindElementsWithAttribute(createParameters(table), elements));
    }

    @Test
    void testFindElementsWithAttributeHref()
    {
        List<WebElement> elements = List.of(webElement);
        mockFindElementsWithAttribute(createParameters("href", VALUE), elements);
        verify(linkFilterAction).filter(elements, VALUE);
    }

    @Test
    void testFilterElementsWithAbsWidth()
    {
        mockBodySearch();
        when(webElement.getSize()).thenReturn(new Dimension(50, 50));
        testFilterElementsWithAbsWidth(List.of(mock(WebElement.class)));
    }

    @Test
    void testFilterElementsWithAbsWidthNotFiltered()
    {
        mockBodySearch();
        when(webElement.getSize()).thenReturn(new Dimension(100, 100));
        testFilterElementsWithAbsWidth(List.of());
    }

    @Test
    void testFilterElementsWithAbsWidthNoParent()
    {
        testFilterElementsWithAbsWidth(List.of(mock(WebElement.class)));
    }

    private void testFilterElementsWithAbsWidth(List<WebElement> expectedList)
    {
        List<WebElement> elements = List.of(webElement);
        SearchInputData searchInputData = new SearchInputData(createParameters("|absWidth|\n|50|"),
                ActionAttributeType.XPATH);
        List<WebElement> actual = parameterizedSearchActions.filterElementsWithCssProperties(searchInputData, elements);
        assertEquals(expectedList.size(), actual.size());
    }

    @Test
    void testFilterElementsExceptionNoAttributeName()
    {
        SearchInputData searchInputData = new SearchInputData(createParameters("|attributeValue|\n|value|"),
                ActionAttributeType.XPATH);
        UnsupportedOperationException exception = assertThrows(UnsupportedOperationException.class,
            () -> parameterizedSearchActions.findElementsWithAttributes(searchContext, searchInputData, List.of()));
        assertEquals("Attribute value cannot be checked. No attribute name was set", exception.getMessage());
    }

    @Test
    void testFilterElementsExceptionNoPropertyName()
    {
        testFilterElementsExceptionNoPropertyName("|cssValue|\n|value|");
    }

    @Test
    void testFilterElementsExceptionNoPartialPropertyName()
    {
        testFilterElementsExceptionNoPropertyName("|cssValuePart|\n|valuePart|");
    }

    private void testFilterElementsExceptionNoPropertyName(String table)
    {
        SearchInputData searchInputData = new SearchInputData(createParameters(table), ActionAttributeType.XPATH);
        UnsupportedOperationException exception = assertThrows(UnsupportedOperationException.class,
            () -> parameterizedSearchActions.filterElementsWithCssProperties(searchInputData,
                    List.of(mock(WebElement.class))));
        assertEquals("CSS property value cannot be checked. No property name was set", exception.getMessage());
    }

    @ParameterizedTest
    @MethodSource("inputDataForTestFilterElementsWithCssProperty")
    void testFilterElementsWithCssProperty(List<WebElement> expectedList, String key, String expectedValue,
            String actualValue)
    {
        Parameters inputParameters = createParameters(
                "|cssProperty|" + key + DELIMITER + "\n|propertyName|" + expectedValue);
        List<WebElement> elements = List.of(webElement);
        SearchInputData searchInputData = new SearchInputData(inputParameters, ActionAttributeType.XPATH);
        when(webElementActions.getCssValue(webElement, PROPERTY_NAME)).thenReturn(actualValue);
        List<WebElement> actual = parameterizedSearchActions.filterElementsWithCssProperties(searchInputData, elements);
        assertEquals(expectedList.size(), actual.size());
    }

    @Test
    void testFilterElementsWithCssPropertyNoExpectedValue()
    {
        Parameters parameters = createParameters("|cssProperty|\n|propertyName|");
        List<WebElement> elements = List.of(webElement);
        SearchInputData searchInputData = new SearchInputData(parameters, ActionAttributeType.XPATH);
        when(webElementActions.getCssValue(webElement, PROPERTY_NAME)).thenReturn(VALUE);
        List<WebElement> actual = parameterizedSearchActions.filterElementsWithCssProperties(searchInputData, elements);
        assertTrue(actual.isEmpty());
    }

    @Test
    void testFilterElementsWithAttributeName()
    {
        Parameters inputParameters = createParameters(ATTRIBUTE_NAME);
        List<WebElement> elements = List.of(webElement);
        when(webElement.getAttribute(NAME)).thenReturn(VALUE);
        assertEquals(elements, mockFindElementsWithAttribute(inputParameters, List.of(webElement)));
    }

    private Parameters createParameters(String attrName, String attrValue)
    {
        return createParameters("|attributeName|attributeValue|\n|" + attrName + DELIMITER + attrValue + DELIMITER);
    }

    private Parameters createParameters(String tableAsString)
    {
        return new ExamplesTable(tableAsString).getRowAsParameters(0);
    }

    private void mockBodySearch()
    {
        WebDriver webDriver = mock(WebDriver.class);
        when(webDriverProvider.get()).thenReturn(webDriver);
        WebElement body = mock(WebElement.class);
        SearchParameters parentSearchParameters = new SearchParameters(BODY_XPATH).setVisibility(Visibility.ALL);
        SearchAttributes searchAttributes = new SearchAttributes(ActionAttributeType.XPATH, parentSearchParameters);
        when(searchActions.findElements(webDriver, searchAttributes)).thenReturn(Collections.singletonList(body));
        when(body.getSize()).thenReturn(new Dimension(100, 100));
    }

    private List<WebElement> mockFindElementsWithAttribute(Parameters inputParameters, List<WebElement> elements)
    {
        SearchInputData searchInputData = new SearchInputData(inputParameters, ActionAttributeType.XPATH);
        return parameterizedSearchActions.findElementsWithAttributes(searchContext, searchInputData, elements);
    }
}
