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

import static com.github.valfirst.slf4jtest.LoggingEvent.error;
import static com.github.valfirst.slf4jtest.LoggingEvent.info;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import static org.vividus.ui.web.action.search.AbstractWebElementSearchAction.generateCaseInsensitiveXpath;

import java.util.List;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.vividus.ui.action.search.AbstractElementAction;
import org.vividus.ui.action.search.SearchParameters;
import org.vividus.ui.action.search.Visibility;
import org.vividus.ui.web.action.IWebElementActions;

@ExtendWith({ TestLoggerFactoryExtension.class, MockitoExtension.class })
class ElementSearchActionTests
{
    private static final String ANY_TEXT = "*";
    private static final String TEXT_TRANSFORM = "text-transform";
    private static final String TEXT = "Text";
    private static final String ELEMENT_BY_TEXT_XPATH = ".//*[contains(normalize-space(text()), 'Text')]";
    private static final By ELEMENT_BY_TEXT_LOCATOR = By.xpath(ELEMENT_BY_TEXT_XPATH);
    private static final String TOTAL_NUMBER_OF_ELEMENTS = "The total number of elements found by \"{}\" is {}";
    private static final String NOT_SET_CONTEXT = "Unable to locate elements, because search context is not set";
    private static final String XPATH_LOCATOR_PREFIX = "xpath: ";

    private final TestLogger logger = TestLoggerFactory.getTestLogger(AbstractElementAction.class);

    @Mock private WebElement webElement;
    @Mock private SearchContext searchContext;
    @Mock private IWebElementActions webElementActions;

    @InjectMocks
    private final AbstractWebElementSearchAction elementSearchAction = new AbstractWebElementSearchAction(
            WebLocatorType.ID) { };

    @Test
    void shouldFindNoElementsByTextWhenSearchContextIsNull()
    {
        var foundElements = elementSearchAction.findElementsByText(null, ELEMENT_BY_TEXT_LOCATOR,
                new SearchParameters(TEXT), ANY_TEXT);
        assertEquals(List.of(), foundElements);
        assertThat(logger.getLoggingEvents(), equalTo(List.of(
                error(NOT_SET_CONTEXT),
                error(NOT_SET_CONTEXT)
        )));
    }

    @Test
    void shouldFindElementsByText()
    {
        when(searchContext.findElements(ELEMENT_BY_TEXT_LOCATOR)).thenReturn(List.of(webElement));
        var foundElements = elementSearchAction.findElementsByText(searchContext, ELEMENT_BY_TEXT_LOCATOR,
                new SearchParameters(TEXT).setVisibility(Visibility.ALL).setWaitForElement(false), ANY_TEXT);
        assertEquals(List.of(webElement), foundElements);
        assertThat(logger.getLoggingEvents(), equalTo(List.of(
                info(TOTAL_NUMBER_OF_ELEMENTS, XPATH_LOCATOR_PREFIX + ELEMENT_BY_TEXT_XPATH, 1)
        )));
    }

    @ParameterizedTest
    @CsvSource({
            "Text,            capitalize",
            "Text  And Text,  capitalize",
            "TEXT,            uppercase",
            "text,            lowercase"
    })
    void shouldFindElementsByTextWithValidTextTransform(String value, String cssTextTransform)
    {
        List<WebElement> expected = List.of(webElement);
        testFindElementsByTextWithEmptyTextTransform(value, cssTextTransform, expected);
    }

    @Test
    void shouldFindNoElementsByTextWithEmptyTextTransform()
    {
        testFindElementsByTextWithEmptyTextTransform(TEXT, "", List.of());
    }

    private void testFindElementsByTextWithEmptyTextTransform(String value, String cssTextTransform,
            List<WebElement> expected)
    {
        lenient().when(searchContext.findElements(ELEMENT_BY_TEXT_LOCATOR)).thenReturn(List.of());
        String caseInsensitiveXpath = generateCaseInsensitiveXpath(value, ANY_TEXT);
        lenient().when(searchContext.findElements(By.xpath(caseInsensitiveXpath))).thenReturn(List.of(webElement));
        when(webElementActions.getCssValue(webElement, TEXT_TRANSFORM)).thenReturn(cssTextTransform);
        var foundElements = elementSearchAction.findElementsByText(searchContext, ELEMENT_BY_TEXT_LOCATOR,
                new SearchParameters(value).setVisibility(Visibility.ALL).setWaitForElement(false), ANY_TEXT);
        assertEquals(expected, foundElements);
        assertThat(logger.getLoggingEvents(), equalTo(List.of(
                info(TOTAL_NUMBER_OF_ELEMENTS, XPATH_LOCATOR_PREFIX + ELEMENT_BY_TEXT_XPATH, 0),
                info(TOTAL_NUMBER_OF_ELEMENTS, XPATH_LOCATOR_PREFIX + caseInsensitiveXpath, 1)
        )));
    }
}
