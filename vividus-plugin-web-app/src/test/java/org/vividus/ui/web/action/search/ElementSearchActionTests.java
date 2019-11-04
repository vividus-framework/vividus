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

import static com.github.valfirst.slf4jtest.LoggingEvent.info;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

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
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.ui.web.action.IExpectedConditions;
import org.vividus.ui.web.action.IExpectedSearchContextCondition;
import org.vividus.ui.web.action.IJavascriptActions;
import org.vividus.ui.web.action.IWaitActions;
import org.vividus.ui.web.action.IWebElementActions;
import org.vividus.ui.web.action.WaitResult;

@ExtendWith({ TestLoggerFactoryExtension.class, MockitoExtension.class })
class ElementSearchActionTests
{
    private static final String LOWERCASE = "lowercase";
    private static final String UPPERCASE = "uppercase";
    private static final String ANY_TEXT = "*";
    private static final String CAPITALIZE = "capitalize";
    private static final String TEXT_TRANSFORM = "text-transform";
    private static final String TEXT = "Text";
    private static final String TOTAL_NUMBER_OF_ELEMENTS = "Total number of elements found {} is equal to {}";
    private static final By ELEMENT_BY_TEXT_LOCATOR = By.xpath(".//*[contains(normalize-space(text()), 'Text')]");
    private static final String EXCEPTION = "exception";
    private static final Duration TIMEOUT = Duration.ofSeconds(0);

    private final TestLogger logger = TestLoggerFactory.getTestLogger(AbstractElementSearchAction.class);

    private List<WebElement> webElements;
    private SearchParameters parameters = new SearchParameters(TEXT);
    private AbstractElementSearchAction spy;

    @Mock
    private IWebDriverProvider webDriverProvider;

    @Mock
    private WebElement webElement;

    @Mock
    private WebDriver webDriver;

    @Mock
    private SearchContext searchContext;

    @Mock
    private IJavascriptActions javascriptActions;

    @Mock
    private IWebElementActions webElementActions;

    @Mock
    private IWaitActions waitActions;

    @Mock
    private WaitResult<Object> result;

    @Mock
    private By locator;

    @Mock
    private IExpectedConditions<By> expectedConditions;

    @InjectMocks
    private AbstractElementSearchAction elementSearchAction = new AbstractElementSearchAction() { };

    private void mockFoundElements()
    {
        By elementByTextCaseInsensitiveXpath = By
                .xpath(".//*[text()[normalize-space(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ',"
                        + " 'abcdefghijklmnopqrstuvwxyz'))='text'] or *[normalize-space(translate(.,"
                        + " 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'))='text']]");
        parameters.setVisibility(Visibility.ALL);
        Mockito.lenient().doReturn(webElements).when(spy).findElements(searchContext,
                elementByTextCaseInsensitiveXpath, parameters);
    }

    @Test
    void testFindElementsByTextCapitalizeCaseFalse()
    {
        spyElementSearchAction();
        addMockedWebElement();
        when(waitActions.wait(eq(searchContext), eq(TIMEOUT), any(), eq(false))).thenReturn(result);
        parameters = new SearchParameters(TEXT.toLowerCase());
        mockFoundElements();
        doReturn(List.of()).when(spy).findElements(searchContext, ELEMENT_BY_TEXT_LOCATOR, parameters);
        when(webElementActions.getCssValue(webElement, TEXT_TRANSFORM)).thenReturn(CAPITALIZE);
        List<WebElement> foundElements = spy.findElementsByText(searchContext, ELEMENT_BY_TEXT_LOCATOR,
                parameters, ANY_TEXT);
        assertNotEquals(webElements, foundElements);
        assertThat(logger.getLoggingEvents(), equalTo(List.of(info(TOTAL_NUMBER_OF_ELEMENTS,
                AbstractElementSearchAction.generateCaseInsensitiveLocator(TEXT, ANY_TEXT), 1))));
    }

    @Test
    void testFindElementsByEmptyText()
    {
        spyElementSearchAction();
        addMockedWebElement();
        when(waitActions.wait(eq(searchContext), eq(TIMEOUT), any(), eq(false))).thenReturn(result);
        parameters = new SearchParameters("");
        mockFoundElements();
        doReturn(List.of()).when(spy).findElements(searchContext, ELEMENT_BY_TEXT_LOCATOR, parameters);
        List<WebElement> foundElements = spy.findElementsByText(searchContext, ELEMENT_BY_TEXT_LOCATOR,
                parameters, ANY_TEXT);
        assertEquals(webElements, foundElements);
        assertThat(logger.getLoggingEvents(), equalTo(List.of(info(TOTAL_NUMBER_OF_ELEMENTS,
                AbstractElementSearchAction.generateCaseInsensitiveLocator(StringUtils.EMPTY, ANY_TEXT), 1))));
    }

    @Test
    void testFindElementsByTextUpperCaseFalse()
    {
        spyElementSearchAction();
        addMockedWebElement();
        when(waitActions.wait(eq(searchContext), eq(TIMEOUT), any(), eq(false))).thenReturn(result);
        mockFoundElements();
        doReturn(List.of()).when(spy).findElements(searchContext, ELEMENT_BY_TEXT_LOCATOR, parameters);
        when(webElementActions.getCssValue(webElement, TEXT_TRANSFORM)).thenReturn(UPPERCASE);
        List<WebElement> foundElements = spy.findElementsByText(searchContext, ELEMENT_BY_TEXT_LOCATOR,
                parameters, ANY_TEXT);
        assertNotEquals(webElements, foundElements);
        assertThat(logger.getLoggingEvents(), equalTo(List.of(info(TOTAL_NUMBER_OF_ELEMENTS,
                AbstractElementSearchAction.generateCaseInsensitiveLocator(TEXT, ANY_TEXT), 1))));
    }

    @Test
    void testFindElementsByTextLowerCaseFalse()
    {
        spyElementSearchAction();
        addMockedWebElement();
        when(waitActions.wait(eq(searchContext), eq(TIMEOUT), any(), eq(false))).thenReturn(result);
        mockFoundElements();
        doReturn(List.of()).when(spy).findElements(searchContext, ELEMENT_BY_TEXT_LOCATOR, parameters);
        when(webElementActions.getCssValue(webElement, TEXT_TRANSFORM)).thenReturn(LOWERCASE);
        List<WebElement> foundElements = spy.findElementsByText(searchContext, ELEMENT_BY_TEXT_LOCATOR,
                parameters, ANY_TEXT);
        assertNotEquals(webElements, foundElements);
        assertThat(logger.getLoggingEvents(), equalTo(List.of(info(TOTAL_NUMBER_OF_ELEMENTS,
                AbstractElementSearchAction.generateCaseInsensitiveLocator(TEXT, ANY_TEXT), 1))));
    }

    @Test
    void testFindElementsByTextNoTextTransform()
    {
        spyElementSearchAction();
        addMockedWebElement();
        when(waitActions.wait(eq(searchContext), eq(TIMEOUT), any(), eq(false))).thenReturn(result);
        mockFoundElements();
        doReturn(List.of()).when(spy).findElements(searchContext, ELEMENT_BY_TEXT_LOCATOR, parameters);
        when(webElementActions.getCssValue(webElement, TEXT_TRANSFORM)).thenReturn("");
        List<WebElement> foundElements = spy.findElementsByText(searchContext, ELEMENT_BY_TEXT_LOCATOR,
                parameters, ANY_TEXT);
        assertNotEquals(webElements, foundElements);
        assertThat(logger.getLoggingEvents(), equalTo(List.of(info(TOTAL_NUMBER_OF_ELEMENTS,
                AbstractElementSearchAction.generateCaseInsensitiveLocator(TEXT, ANY_TEXT), 1))));
    }

    @SuppressWarnings("unchecked")
    @Test
    void testFindAndScroll()
    {
        spyElementSearchAction();
        when(waitActions.wait(eq(searchContext), eq(TIMEOUT), any(), eq(false))).thenReturn(result);
        WebElement element1 = mock(WebElement.class);
        List<WebElement> elements = List.of(element1);
        IExpectedSearchContextCondition<List<WebElement>> condition = mock(IExpectedSearchContextCondition.class);
        when(expectedConditions.presenceOfAllElementsLocatedBy(any(By.class))).thenReturn(condition);
        when(result.getData()).thenReturn(elements);
        when(element1.isDisplayed()).thenAnswer(new Answer<Boolean>()
        {
            private int count;

            @Override
            public Boolean answer(InvocationOnMock invocation)
            {
                return ++count == 1 ? Boolean.FALSE : Boolean.TRUE;
            }
        });
        List<WebElement> foundElements = elementSearchAction.findElements(searchContext, locator,
                new SearchParameters());
        assertEquals(1, foundElements.size());
        assertEquals(element1, foundElements.get(0));
        verify(waitActions).wait(searchContext, TIMEOUT, condition, false);
        assertThat(logger.getLoggingEvents(), equalTo(List.of(info(TOTAL_NUMBER_OF_ELEMENTS, locator, 1))));
    }

    @Test
    void testFindElementsByTextNull()
    {
        parameters.setVisibility(Visibility.VISIBLE);
        List<WebElement> foundElements = elementSearchAction.findElementsByText(null, ELEMENT_BY_TEXT_LOCATOR,
                parameters, ANY_TEXT);
        assertEquals(List.of(), foundElements);
        verifyNoInteractions(waitActions);
        assertThat(logger.getLoggingEvents(), equalTo(List.of()));
    }

    @Test
    void testFindElementsByTextWithCaseSensitive()
    {
        spyElementSearchAction();
        addMockedWebElement();
        when(waitActions.wait(eq(searchContext), eq(TIMEOUT), any(), eq(false))).thenReturn(result);
        parameters.setVisibility(Visibility.VISIBLE);
        when(webElements.get(0).isDisplayed()).thenReturn(Boolean.TRUE);
        List<WebElement> foundElements = elementSearchAction.findElementsByText(searchContext, ELEMENT_BY_TEXT_LOCATOR,
                parameters, ANY_TEXT);
        assertEquals(webElements, foundElements);
        assertThat(logger.getLoggingEvents(),
                equalTo(List.of(info(TOTAL_NUMBER_OF_ELEMENTS, ELEMENT_BY_TEXT_LOCATOR, 1))));
    }

    @Test
    void testFindElementsByTextNullHeight()
    {
        spyElementSearchAction();
        addMockedWebElement();
        when(waitActions.wait(eq(searchContext), eq(TIMEOUT), any(), eq(false))).thenReturn(result);
        parameters.setVisibility(Visibility.VISIBLE);
        when(webElements.get(0).isDisplayed()).thenReturn(Boolean.TRUE);
        List<WebElement> foundElements = elementSearchAction.findElementsByText(searchContext, ELEMENT_BY_TEXT_LOCATOR,
                parameters, ANY_TEXT);
        assertEquals(webElements, foundElements);
        assertThat(logger.getLoggingEvents(),
                equalTo(List.of(info(TOTAL_NUMBER_OF_ELEMENTS, ELEMENT_BY_TEXT_LOCATOR, 1))));
    }

    @Test
    void testFindElementsByTextNullWidth()
    {
        spyElementSearchAction();
        addMockedWebElement();
        when(waitActions.wait(eq(searchContext), eq(TIMEOUT), any(), eq(false))).thenReturn(result);
        parameters.setVisibility(Visibility.VISIBLE);
        when(webElements.get(0).isDisplayed()).thenReturn(Boolean.TRUE);
        List<WebElement> foundElements = elementSearchAction.findElementsByText(searchContext, ELEMENT_BY_TEXT_LOCATOR,
                parameters, ANY_TEXT);
        assertEquals(webElements, foundElements);
        assertThat(logger.getLoggingEvents(),
                equalTo(List.of(info(TOTAL_NUMBER_OF_ELEMENTS, ELEMENT_BY_TEXT_LOCATOR, 1))));
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
        assertThat(logger.getLoggingEvents(), equalTo(List
                .of(info(TOTAL_NUMBER_OF_ELEMENTS, ELEMENT_BY_TEXT_LOCATOR, 0), info(TOTAL_NUMBER_OF_ELEMENTS,
                        AbstractElementSearchAction.generateCaseInsensitiveLocator(TEXT, ANY_TEXT), 0))));
    }

    @Test
    void testFindElementsDisplayedOnly()
    {
        WebElement element1 = mock(WebElement.class);
        WebElement element2 = mock(WebElement.class);
        List<WebElement> elementsList = List.of(element1, element2);
        when(searchContext.findElements(locator)).thenReturn(elementsList);
        when(element1.isDisplayed()).thenReturn(Boolean.TRUE);
        when(element2.isDisplayed()).thenReturn(Boolean.FALSE);
        List<WebElement> foundElements = elementSearchAction.findElements(searchContext, locator,
                new SearchParameters().setWaitForElement(false));
        assertEquals(1, foundElements.size());
        assertEquals(element1, foundElements.get(0));
        assertThat(logger.getLoggingEvents(), equalTo(List.of(info(TOTAL_NUMBER_OF_ELEMENTS, locator, 2))));
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldFindAllElements()
    {
        spyElementSearchAction();
        when(waitActions.wait(eq(searchContext), eq(TIMEOUT), any(), eq(false))).thenReturn(result);
        WebElement element1 = mock(WebElement.class);
        WebElement element2 = mock(WebElement.class);
        List<WebElement> elements = List.of(element1, element2);
        when(result.getData()).thenReturn(elements);
        IExpectedSearchContextCondition<List<WebElement>> condition = mock(IExpectedSearchContextCondition.class);
        when(expectedConditions.presenceOfAllElementsLocatedBy(any(By.class))).thenReturn(condition);
        List<WebElement> foundElements = elementSearchAction.findElements(searchContext, locator,
                new SearchParameters().setVisibility(Visibility.ALL));
        assertEquals(2, foundElements.size());
        assertEquals(element1, foundElements.get(0));
        assertEquals(element2, foundElements.get(1));
        verify(waitActions).wait(searchContext, TIMEOUT, condition, false);
        assertThat(logger.getLoggingEvents(), equalTo(List.of(info(TOTAL_NUMBER_OF_ELEMENTS, locator, 2))));
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldFindInvisibleElements()
    {
        spyElementSearchAction();
        when(waitActions.wait(eq(searchContext), eq(TIMEOUT), any(), eq(false))).thenReturn(result);
        WebElement element1 = mock(WebElement.class);
        WebElement element2 = mock(WebElement.class);
        List<WebElement> elements = List.of(element1, element2);
        when(result.getData()).thenReturn(elements);
        when(element1.isDisplayed()).thenReturn(Boolean.TRUE);
        when(element2.isDisplayed()).thenReturn(Boolean.FALSE);
        IExpectedSearchContextCondition<List<WebElement>> condition = mock(IExpectedSearchContextCondition.class);
        when(expectedConditions.presenceOfAllElementsLocatedBy(any(By.class))).thenReturn(condition);
        List<WebElement> foundElements = elementSearchAction.findElements(searchContext, locator,
                new SearchParameters().setVisibility(Visibility.INVISIBLE));
        assertEquals(1, foundElements.size());
        assertEquals(List.of(element2), foundElements);
        verify(waitActions).wait(searchContext, TIMEOUT, condition, false);
        assertThat(logger.getLoggingEvents(), equalTo(List.of(info(TOTAL_NUMBER_OF_ELEMENTS, locator, 2))));
    }

    @Test
    void testFindAllElementsWithException()
    {
        WebElement element = mock(WebElement.class);
        List<WebElement> elements = List.of(element);
        Mockito.doThrow(new StaleElementReferenceException(EXCEPTION)).when(element).isDisplayed();
        when(searchContext.findElements(locator)).thenReturn(elements);
        List<WebElement> foundElements = elementSearchAction.findElements(searchContext, locator,
                new SearchParameters().setWaitForElement(false));
        assertEquals(0, foundElements.size());
        verify(element, Mockito.never()).getSize();
        verifyNoInteractions(waitActions);
        assertThat(logger.getLoggingEvents().get(0), equalTo(info(TOTAL_NUMBER_OF_ELEMENTS, locator, 1)));
    }

    @Test
    void testFindElementsByTextCapitalizeCase()
    {
        spyElementSearchAction();
        addMockedWebElement();
        when(waitActions.wait(eq(searchContext), eq(TIMEOUT), any(), eq(false))).thenReturn(result);
        mockFoundElements();
        doReturn(List.of()).when(spy).findElements(searchContext, ELEMENT_BY_TEXT_LOCATOR, parameters);
        when(webElementActions.getCssValue(webElement, TEXT_TRANSFORM)).thenReturn(CAPITALIZE);
        List<WebElement> foundElements = spy.findElementsByText(searchContext, ELEMENT_BY_TEXT_LOCATOR,
                parameters, ANY_TEXT);
        assertEquals(webElements, foundElements);
        assertThat(logger.getLoggingEvents(), equalTo(List.of(info(TOTAL_NUMBER_OF_ELEMENTS,
                AbstractElementSearchAction.generateCaseInsensitiveLocator(TEXT, ANY_TEXT), 1))));
    }

    @Test
    void testFindElementsByTextUpperCase()
    {
        spyElementSearchAction();
        addMockedWebElement();
        when(waitActions.wait(eq(searchContext), eq(TIMEOUT), any(), eq(false))).thenReturn(result);
        parameters = new SearchParameters(TEXT.toUpperCase());
        mockFoundElements();
        doReturn(List.of()).when(spy).findElements(searchContext, ELEMENT_BY_TEXT_LOCATOR, parameters);
        when(webElementActions.getCssValue(webElement, TEXT_TRANSFORM)).thenReturn(UPPERCASE);
        List<WebElement> foundElements = spy.findElementsByText(searchContext, ELEMENT_BY_TEXT_LOCATOR,
                parameters, ANY_TEXT);
        assertEquals(webElements, foundElements);
        assertThat(logger.getLoggingEvents(), equalTo(List.of(info(TOTAL_NUMBER_OF_ELEMENTS,
                AbstractElementSearchAction.generateCaseInsensitiveLocator(TEXT, ANY_TEXT), 1))));
    }

    @Test
    void testFindElementsByTextLowerCase()
    {
        spyElementSearchAction();
        addMockedWebElement();
        when(waitActions.wait(eq(searchContext), eq(TIMEOUT), any(), eq(false))).thenReturn(result);
        parameters = new SearchParameters(TEXT.toLowerCase());
        mockFoundElements();
        doReturn(List.of()).when(spy).findElements(searchContext, ELEMENT_BY_TEXT_LOCATOR, parameters);
        when(webElementActions.getCssValue(webElement, TEXT_TRANSFORM)).thenReturn(LOWERCASE);
        List<WebElement> foundElements = spy.findElementsByText(searchContext, ELEMENT_BY_TEXT_LOCATOR,
                parameters, ANY_TEXT);
        assertEquals(webElements, foundElements);
        assertThat(logger.getLoggingEvents(), equalTo(List.of(info(TOTAL_NUMBER_OF_ELEMENTS,
                AbstractElementSearchAction.generateCaseInsensitiveLocator(TEXT, ANY_TEXT), 1))));
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
}
