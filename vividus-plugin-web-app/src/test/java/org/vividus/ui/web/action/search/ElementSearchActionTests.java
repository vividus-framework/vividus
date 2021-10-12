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

import static com.github.valfirst.slf4jtest.LoggingEvent.info;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.vividus.ui.web.action.search.AbstractWebElementSearchAction.generateCaseInsensitiveLocator;

import java.time.Duration;
import java.util.List;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.vividus.ui.action.ElementActions;
import org.vividus.ui.action.IExpectedConditions;
import org.vividus.ui.action.IExpectedSearchContextCondition;
import org.vividus.ui.action.WaitResult;
import org.vividus.ui.action.search.AbstractElementAction;
import org.vividus.ui.action.search.SearchParameters;
import org.vividus.ui.action.search.Visibility;
import org.vividus.ui.web.action.IWebElementActions;
import org.vividus.ui.web.action.IWebWaitActions;

@ExtendWith({ TestLoggerFactoryExtension.class, MockitoExtension.class })
class ElementSearchActionTests
{
    private static final String LOWERCASE = "lowercase";
    private static final String UPPERCASE = "uppercase";
    private static final String ANY_TEXT = "*";
    private static final String CAPITALIZE = "capitalize";
    private static final String TEXT_TRANSFORM = "text-transform";
    private static final String TEXT = "Text";
    private static final String TOTAL_NUMBER_OF_ELEMENTS = "Total number of elements found {} is {}";
    private static final By ELEMENT_BY_TEXT_LOCATOR = By.xpath(".//*[contains(normalize-space(text()), 'Text')]");
    private static final Duration TIMEOUT = Duration.ofSeconds(0);

    private final TestLogger logger = TestLoggerFactory.getTestLogger(AbstractElementAction.class);

    private List<WebElement> webElements;
    private SearchParameters parameters = new SearchParameters(TEXT);
    private AbstractWebElementSearchAction spy;

    @Mock private WebElement webElement;
    @Mock private SearchContext searchContext;
    @Mock private IWebElementActions webElementActions;
    @Mock private ElementActions elementActions;
    @Mock private IWebWaitActions waitActions;
    @Mock private WaitResult<Object> result;
    @Mock private IExpectedConditions<By> expectedConditions;

    @InjectMocks
    private final AbstractWebElementSearchAction elementSearchAction = new AbstractWebElementSearchAction(
            WebLocatorType.ID) { };

    private void mockFoundElements()
    {
        By elementByTextCaseInsensitiveXpath = By
                .xpath(".//*[text()[normalize-space(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ',"
                        + " 'abcdefghijklmnopqrstuvwxyz'))='text'] or *[normalize-space(translate(.,"
                        + " 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'))='text']]");
        parameters.setVisibility(Visibility.ALL);
        lenient().when(searchContext.findElements(elementByTextCaseInsensitiveXpath)).thenReturn(webElements);
    }

    @Test
    void testFindElementsByTextCapitalizeCaseFalse()
    {
        spyElementSearchAction();
        addMockedWebElementWithEmpty();
        when(waitActions.wait(eq(searchContext), eq(TIMEOUT), any(), eq(false))).thenReturn(result);
        parameters = new SearchParameters(TEXT.toLowerCase());
        mockFoundElements();
        when(webElementActions.getCssValue(webElement, TEXT_TRANSFORM)).thenReturn(CAPITALIZE);
        List<WebElement> foundElements = spy.findElementsByText(searchContext, ELEMENT_BY_TEXT_LOCATOR,
                parameters, ANY_TEXT);
        assertNotEquals(webElements, foundElements);
        verifySearchByTextEvents();
    }

    @Test
    void testFindElementsByEmptyText()
    {
        spyElementSearchAction();
        addMockedWebElementWithEmpty();
        when(waitActions.wait(eq(searchContext), eq(TIMEOUT), any(), eq(false))).thenReturn(result);
        parameters = new SearchParameters("");
        mockFoundElements();
        List<WebElement> foundElements = spy.findElementsByText(searchContext, ELEMENT_BY_TEXT_LOCATOR,
                parameters, ANY_TEXT);
        assertEquals(webElements, foundElements);
        assertThat(logger.getLoggingEvents(), equalTo(List.of(
                info(TOTAL_NUMBER_OF_ELEMENTS, ELEMENT_BY_TEXT_LOCATOR, 0),
                info(TOTAL_NUMBER_OF_ELEMENTS, generateCaseInsensitiveLocator(StringUtils.EMPTY, ANY_TEXT), 1))));
    }

    @Test
    void testFindElementsByTextUpperCaseFalse()
    {
        spyElementSearchAction();
        addMockedWebElementWithEmpty();
        when(waitActions.wait(eq(searchContext), eq(TIMEOUT), any(), eq(false))).thenReturn(result);
        mockFoundElements();
        when(webElementActions.getCssValue(webElement, TEXT_TRANSFORM)).thenReturn(UPPERCASE);
        List<WebElement> foundElements = spy.findElementsByText(searchContext, ELEMENT_BY_TEXT_LOCATOR,
                parameters, ANY_TEXT);
        assertNotEquals(webElements, foundElements);
        verifySearchByTextEvents();
    }

    @Test
    void testFindElementsByTextLowerCaseFalse()
    {
        spyElementSearchAction();
        addMockedWebElementWithEmpty();
        when(waitActions.wait(eq(searchContext), eq(TIMEOUT), any(), eq(false))).thenReturn(result);
        mockFoundElements();
        when(webElementActions.getCssValue(webElement, TEXT_TRANSFORM)).thenReturn(LOWERCASE);
        List<WebElement> foundElements = spy.findElementsByText(searchContext, ELEMENT_BY_TEXT_LOCATOR,
                parameters, ANY_TEXT);
        assertNotEquals(webElements, foundElements);
        verifySearchByTextEvents();
    }

    @Test
    void testFindElementsByTextNoTextTransform()
    {
        spyElementSearchAction();
        addMockedWebElementWithEmpty();
        when(waitActions.wait(eq(searchContext), eq(TIMEOUT), any(), eq(false))).thenReturn(result);
        mockFoundElements();
        when(webElementActions.getCssValue(webElement, TEXT_TRANSFORM)).thenReturn("");
        List<WebElement> foundElements = spy.findElementsByText(searchContext, ELEMENT_BY_TEXT_LOCATOR,
                parameters, ANY_TEXT);
        assertNotEquals(webElements, foundElements);
        verifySearchByTextEvents();
    }

    @Test
    void testFindElementsByTextNull()
    {
        parameters.setVisibility(Visibility.VISIBLE);
        List<WebElement> foundElements = elementSearchAction.findElementsByText(null, ELEMENT_BY_TEXT_LOCATOR,
                parameters, ANY_TEXT);
        assertEquals(List.of(), foundElements);
        verifyNoInteractions(waitActions);
    }

    @Test
    void testFindElementsByTextWithCaseSensitive()
    {
        spyElementSearchAction();
        addMockedWebElement();
        when(waitActions.wait(eq(searchContext), eq(TIMEOUT), any(), eq(false))).thenReturn(result);
        parameters.setVisibility(Visibility.VISIBLE);
        when(elementActions.isElementVisible(webElements.get(0))).thenReturn(Boolean.TRUE);
        List<WebElement> foundElements = elementSearchAction.findElementsByText(searchContext, ELEMENT_BY_TEXT_LOCATOR,
                parameters, ANY_TEXT);
        assertEquals(webElements, foundElements);
        verifyLogging();
    }

    @Test
    void testFindElementsByTextNullHeight()
    {
        spyElementSearchAction();
        addMockedWebElement();
        when(waitActions.wait(eq(searchContext), eq(TIMEOUT), any(), eq(false))).thenReturn(result);
        parameters.setVisibility(Visibility.VISIBLE);
        when(elementActions.isElementVisible(webElements.get(0))).thenReturn(Boolean.TRUE);
        List<WebElement> foundElements = elementSearchAction.findElementsByText(searchContext, ELEMENT_BY_TEXT_LOCATOR,
                parameters, ANY_TEXT);
        assertEquals(webElements, foundElements);
        verifyLogging();
    }

    @Test
    void testFindElementsByTextNullWidth()
    {
        spyElementSearchAction();
        addMockedWebElement();
        when(waitActions.wait(eq(searchContext), eq(TIMEOUT), any(), eq(false))).thenReturn(result);
        parameters.setVisibility(Visibility.VISIBLE);
        when(elementActions.isElementVisible(webElements.get(0))).thenReturn(Boolean.TRUE);
        List<WebElement> foundElements = elementSearchAction.findElementsByText(searchContext, ELEMENT_BY_TEXT_LOCATOR,
                parameters, ANY_TEXT);
        assertEquals(webElements, foundElements);
        verifyLogging();
    }

    private void verifyLogging()
    {
        assertThat(logger.getLoggingEvents(), equalTo(List.of(
            info(TOTAL_NUMBER_OF_ELEMENTS, ELEMENT_BY_TEXT_LOCATOR, 1),
            info("Number of {} elements is {}", Visibility.VISIBLE.getDescription(), 1)
        )));
    }

    @SuppressWarnings("unchecked")
    @Test
    void testFindElementsByTextWaitNotPassed()
    {
        spyElementSearchAction();
        when(waitActions.wait(eq(searchContext), eq(TIMEOUT), any(), eq(false))).thenReturn(result);
        parameters.setVisibility(Visibility.VISIBLE);
        when(result.getData()).thenReturn(null);
        IExpectedSearchContextCondition<List<WebElement>> condition = mock(IExpectedSearchContextCondition.class);
        when(expectedConditions.presenceOfAllElementsLocatedBy(any(By.class))).thenReturn(condition);
        List<WebElement> foundElements = elementSearchAction.findElementsByText(searchContext, ELEMENT_BY_TEXT_LOCATOR,
                parameters, ANY_TEXT);
        assertNotEquals(webElements, foundElements);
        verify(waitActions, times(2)).wait(searchContext, TIMEOUT, condition, false);
        assertThat(logger.getLoggingEvents(), equalTo(List.of(
                info(TOTAL_NUMBER_OF_ELEMENTS, ELEMENT_BY_TEXT_LOCATOR, 0),
                info(TOTAL_NUMBER_OF_ELEMENTS, generateCaseInsensitiveLocator(TEXT, ANY_TEXT), 0))));
    }

    @Test
    void testFindElementsByTextCapitalizeCase()
    {
        spyElementSearchAction();
        addMockedWebElementWithEmpty();
        when(waitActions.wait(eq(searchContext), eq(TIMEOUT), any(), eq(false))).thenReturn(result);
        mockFoundElements();
        when(webElementActions.getCssValue(webElement, TEXT_TRANSFORM)).thenReturn(CAPITALIZE);
        List<WebElement> foundElements = spy.findElementsByText(searchContext, ELEMENT_BY_TEXT_LOCATOR,
                parameters, ANY_TEXT);
        assertEquals(webElements, foundElements);
        verifySearchByTextEvents();
    }

    @Test
    void testFindElementsByTextUpperCase()
    {
        spyElementSearchAction();
        addMockedWebElementWithEmpty();
        when(waitActions.wait(eq(searchContext), eq(TIMEOUT), any(), eq(false))).thenReturn(result);
        parameters = new SearchParameters(TEXT.toUpperCase());
        mockFoundElements();
        when(webElementActions.getCssValue(webElement, TEXT_TRANSFORM)).thenReturn(UPPERCASE);
        List<WebElement> foundElements = spy.findElementsByText(searchContext, ELEMENT_BY_TEXT_LOCATOR,
                parameters, ANY_TEXT);
        assertEquals(webElements, foundElements);
        verifySearchByTextEvents();
    }

    @Test
    void testFindElementsByTextLowerCase()
    {
        spyElementSearchAction();
        addMockedWebElementWithEmpty();
        when(waitActions.wait(eq(searchContext), eq(TIMEOUT), any(), eq(false))).thenReturn(result);
        parameters = new SearchParameters(TEXT.toLowerCase());
        mockFoundElements();
        when(webElementActions.getCssValue(webElement, TEXT_TRANSFORM)).thenReturn(LOWERCASE);
        List<WebElement> foundElements = spy.findElementsByText(searchContext, ELEMENT_BY_TEXT_LOCATOR,
                parameters, ANY_TEXT);
        assertEquals(webElements, foundElements);
        verifySearchByTextEvents();
    }

    private void verifySearchByTextEvents()
    {
        assertThat(logger.getLoggingEvents(), equalTo(List.of(
                info(TOTAL_NUMBER_OF_ELEMENTS, ELEMENT_BY_TEXT_LOCATOR, 0),
                info(TOTAL_NUMBER_OF_ELEMENTS, generateCaseInsensitiveLocator(TEXT, ANY_TEXT), 1))));
    }

    private void spyElementSearchAction()
    {
        elementSearchAction.setWaitForElementTimeout(TIMEOUT);
        spy = Mockito.spy(elementSearchAction);
    }

    private void addMockedWebElement()
    {
        webElements = List.of(webElement);
        when(result.getData()).thenReturn(webElements);
    }

    private void addMockedWebElementWithEmpty()
    {
        webElements = List.of(webElement);
        when(result.getData()).thenReturn(List.of()).thenReturn(webElements);
    }
}
