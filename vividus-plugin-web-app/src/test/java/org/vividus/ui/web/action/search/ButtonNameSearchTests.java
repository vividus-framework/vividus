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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;

import java.util.List;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.vividus.ui.web.action.IWebElementActions;
import org.vividus.ui.web.util.LocatorUtil;

@ExtendWith({TestLoggerFactoryExtension.class, MockitoExtension.class})
class ButtonNameSearchTests
{
    private static final String VALUE = "value";
    private static final String VALUE_WITH_CAPITAL_LETTER = "Value";
    private static final String BUTTON_WITH_ANY_ATTRIBUTE_NAME_PATTERN = "*[(local-name()='button' and "
            + "(@*=%1$s or text()=%1$s)) or (local-name()='input' and ((@type='submit' or "
            + "@type='button') and (@*=%1$s or text()=%1$s)))]";
    private static final String CASE_INSENSITIVE_LOCATOR = "[text()[normalize-space"
            + "(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'))=%1$s] or @*["
            + "normalize-space(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'))=%1$s] or "
            + "*[normalize-space(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'))"
            + "=%1$s]";
    private static final String BUTTON_TEXT_LOCATOR = ".//button" + CASE_INSENSITIVE_LOCATOR;
    private static final String INPUT_TEXT_LOCATOR = ".//input" + CASE_INSENSITIVE_LOCATOR;

    private static final String AND_NOT = " and not(";
    private static final String CLOSING_BRACKETS = "])]";
    private static final String BUTTON_WITH_TEXT_TRANSFORM_CSS_PROPERTY_PATTERN =
            BUTTON_TEXT_LOCATOR + AND_NOT + BUTTON_TEXT_LOCATOR + CLOSING_BRACKETS + "|"
                    + INPUT_TEXT_LOCATOR + AND_NOT + INPUT_TEXT_LOCATOR + CLOSING_BRACKETS;
    private static final String BUTTON_WITH_ANY_ATTRIBUTE_NAME_XPATH = ".//" + BUTTON_WITH_ANY_ATTRIBUTE_NAME_PATTERN;
    private static final By BUTTON_LOCATOR = LocatorUtil.getXPathLocator(BUTTON_WITH_ANY_ATTRIBUTE_NAME_XPATH,
            VALUE);
    private static final By BUTTON_LOCATOR_WITH_TEXT_TRANSFORM_CSS_PROPERTY = LocatorUtil
            .getXPathLocator(BUTTON_WITH_TEXT_TRANSFORM_CSS_PROPERTY_PATTERN, VALUE);
    private static final String TEXT_TRANSFORM = "text-transform";
    private static final String CAPITALIZE = "capitalize";

    private final TestLogger logger = TestLoggerFactory.getTestLogger(AbstractElementSearchAction.class);

    private List<WebElement> webElements;
    private SearchParameters buttonParameters;

    @Mock
    private WebElement webElement;

    @Mock
    private SearchContext searchContext;

    @Mock
    private IWebElementActions webElementActions;

    @InjectMocks
    @Spy
    private ButtonNameSearch buttonNameSearch;

    @Test
    void testSearchSuccess()
    {
        webElements = List.of(webElement);
        buttonParameters = new SearchParameters(VALUE);
        doReturn(webElements).when(buttonNameSearch).findElements(searchContext, BUTTON_LOCATOR, buttonParameters);
        List<WebElement> foundElements = buttonNameSearch.search(searchContext, buttonParameters);
        assertEquals(webElements, foundElements);
    }

    @Test
    void testSearchByUpperDivSuccess()
    {
        webElements = List.of(webElement);
        buttonParameters = new SearchParameters(VALUE);
        doReturn(webElements).when(buttonNameSearch).findElements(searchContext, BUTTON_LOCATOR, buttonParameters);
        List<WebElement> foundElements = buttonNameSearch.search(searchContext, buttonParameters);
        assertEquals(webElements, foundElements);
    }

    @Test
    void testSearchByUpperDivNotFound()
    {
        buttonParameters = new SearchParameters(VALUE);
        doReturn(List.of()).when(buttonNameSearch).findElements(searchContext, BUTTON_LOCATOR, buttonParameters);
        doReturn(List.of()).when(buttonNameSearch).findElements(searchContext,
                BUTTON_LOCATOR_WITH_TEXT_TRANSFORM_CSS_PROPERTY, buttonParameters);
        List<WebElement> foundElements = buttonNameSearch.search(searchContext, buttonParameters);
        assertEquals(List.of(), foundElements);
    }

    @Test
    void testSearchEmptyContext()
    {
        buttonParameters = new SearchParameters(VALUE);
        List<WebElement> foundElements = buttonNameSearch.search(null, buttonParameters);
        assertTrue(foundElements.isEmpty());
        assertThat(logger.getLoggingEvents(), equalTo(List.of()));
    }

    @Test
    void testSearchButtonsWithTextTransformCssProperty()
    {
        webElements = List.of(webElement);
        buttonParameters = new SearchParameters(VALUE);
        SearchParameters parametersWithCapitalLetter = new SearchParameters(VALUE_WITH_CAPITAL_LETTER);
        SearchParameters buttonParametersWithCapitalLetter = new SearchParameters(
                parametersWithCapitalLetter.getValue());
        doReturn(webElements).when(buttonNameSearch).findElements(searchContext, BUTTON_LOCATOR, buttonParameters);
        List<WebElement> foundElements = buttonNameSearch.search(searchContext, buttonParameters);
        doReturn(CAPITALIZE).when(webElementActions).getCssValue(webElement, TEXT_TRANSFORM);
        doReturn(List.of()).when(buttonNameSearch).findElements(searchContext,
                LocatorUtil.getXPathLocator(BUTTON_WITH_ANY_ATTRIBUTE_NAME_XPATH, VALUE_WITH_CAPITAL_LETTER),
                buttonParametersWithCapitalLetter);
        doReturn(webElements).when(buttonNameSearch).findElements(searchContext,
                BUTTON_LOCATOR_WITH_TEXT_TRANSFORM_CSS_PROPERTY, buttonParametersWithCapitalLetter);
        List<WebElement> elementsWithTextTransformCssProperty = buttonNameSearch.search(searchContext,
                buttonParametersWithCapitalLetter);
        assertEquals(elementsWithTextTransformCssProperty, foundElements);
    }
}
