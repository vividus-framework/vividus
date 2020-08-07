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

package org.vividus.bdd.steps.ui.web;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

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
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.vividus.bdd.steps.ComparisonRule;
import org.vividus.bdd.steps.ui.validation.IBaseValidations;
import org.vividus.bdd.steps.ui.web.model.SortingOrder;
import org.vividus.bdd.steps.ui.web.validation.IElementValidations;
import org.vividus.softassert.ISoftAssert;
import org.vividus.ui.action.SearchActions;
import org.vividus.ui.action.search.Locator;
import org.vividus.ui.context.IUiContext;
import org.vividus.ui.web.action.IWebElementActions;
import org.vividus.ui.web.action.search.WebLocatorType;
import org.vividus.ui.web.util.LocatorUtil;

@ExtendWith(MockitoExtension.class)
class WebElementsStepsTests
{
    private static final String A_FRAME_WITH_THE_ATTRIBUTE_ATTRIBUTE_TYPE_ATTRIBUTE_VALUE = "A "
            + "frame with the attribute 'attributeType'='attributeValue'";
    private static final String IFRAME = "iframe";
    private static final String ATTRIBUTE_VALUE = "attributeValue";
    private static final String ATTRIBUTE_TYPE = "attributeType";
    private static final String TEXT = "text";
    private static final String REGEX = "[a-zA-Z]+";
    private static final String TEXT_MATCHES_REGEX_MESSAGE = "The text in search context matches regular expression ";
    private static final String XPATH = LocatorUtil.getXPathByTagNameAndAttribute(IFRAME, ATTRIBUTE_TYPE,
            ATTRIBUTE_VALUE);
    private static final String THERE_IS_AN_ELEMENT_WITH_TEXT_TEXT_IN_THE_CONTEXT = "There"
            + " is an element with text=text in the context";
    private static final String ELEMENT_TEXT = "1";
    private static final String PAGE_TEXT = "no";
    private static final String FIELDS_BY_LOCATOR = "The elements to check the order: %s";
    private static final String FIELD_CONTAINS_TEXT = "The element number %d contains empty text";
    private static final String ELEMENTS_ARE_SORTED = "The elements are sorted in %s order";

    @Mock
    private IUiContext uiContext;

    @Mock
    private IBaseValidations mockedBaseValidations;

    @Mock
    private IElementValidations elementValidations;

    @Mock
    private WebElement mockedWebElement;

    @Mock
    private WebDriver webDriver;

    @Mock
    private IWebElementActions mockedWebElementActions;

    @InjectMocks
    private WebElementsSteps webElementsSteps;

    @Mock
    private SearchActions searchActions;

    @Mock
    private ISoftAssert softAssert;

    @Test
    void testCheckPageContainsTextThrowsWebDriverException()
    {
        By locator = LocatorUtil.getXPathLocatorByInnerText(TEXT);
        List<WebElement> webElementList = List.of(mockedWebElement);
        when(uiContext.getSearchContext()).thenReturn(webDriver);
        when(webDriver.findElements(locator)).thenAnswer(new Answer<List<WebElement>>()
        {
            private int count;

            @Override
            public List<WebElement> answer(InvocationOnMock invocation)
            {
                count++;
                if (count == 1)
                {
                    throw new WebDriverException();
                }

                return webElementList;
            }
        });
        webElementsSteps.ifTextExists(TEXT);
        verify(softAssert).assertTrue(THERE_IS_AN_ELEMENT_WITH_TEXT_TEXT_IN_THE_CONTEXT, true);
    }

    @Test
    void testCheckPageContainsText()
    {
        By locator = LocatorUtil.getXPathLocatorByInnerText(TEXT);
        List<WebElement> webElementList = List.of(mockedWebElement);
        when(uiContext.getSearchContext()).thenReturn(webDriver);
        when(webDriver.findElements(locator)).thenReturn(webElementList);
        webElementsSteps.ifTextExists(TEXT);
        verify(softAssert).assertTrue(THERE_IS_AN_ELEMENT_WITH_TEXT_TEXT_IN_THE_CONTEXT, true);
    }

    @Test
    void testDomElementsContainText()
    {
        when(uiContext.getSearchContext()).thenReturn(mockedWebElement);
        when(uiContext.getSearchContext(WebElement.class)).thenReturn(mockedWebElement);
        webElementsSteps.ifTextExists(TEXT);
        verify(elementValidations).assertIfElementContainsText(mockedWebElement, TEXT, true);
    }

    @Test
    void testCheckPageContainsTextInPseudoElements()
    {
        WebElementsSteps spy = Mockito.spy(webElementsSteps);
        List<String> pseudoElementsContent = List.of(TEXT);
        when(uiContext.getSearchContext()).thenReturn(webDriver);
        when(mockedWebElementActions.getAllPseudoElementsContent()).thenReturn(pseudoElementsContent);
        spy.ifTextExists(TEXT);
        verify(softAssert).assertTrue(THERE_IS_AN_ELEMENT_WITH_TEXT_TEXT_IN_THE_CONTEXT, true);
    }

    @Test
    void testIfTextExists()
    {
        WebElementsSteps spy = Mockito.spy(webElementsSteps);
        List<String> pseudoElementsContent = List.of();
        when(uiContext.getSearchContext()).thenReturn(webDriver);
        when(mockedWebElementActions.getAllPseudoElementsContent()).thenReturn(pseudoElementsContent);
        when(mockedWebElementActions.getPageText()).thenReturn(PAGE_TEXT);
        spy.ifTextExists(TEXT);
        verify(softAssert).assertTrue(THERE_IS_AN_ELEMENT_WITH_TEXT_TEXT_IN_THE_CONTEXT, false);
    }

    @Test
    void testIfTextExistsOnPage()
    {
        WebElementsSteps spy = Mockito.spy(webElementsSteps);
        List<String> pseudoElementsContent = List.of();
        when(uiContext.getSearchContext()).thenReturn(webDriver);
        when(mockedWebElementActions.getAllPseudoElementsContent()).thenReturn(pseudoElementsContent);
        when(mockedWebElementActions.getPageText()).thenReturn(TEXT);
        spy.ifTextExists(TEXT);
        verify(softAssert).assertTrue(THERE_IS_AN_ELEMENT_WITH_TEXT_TEXT_IN_THE_CONTEXT, true);
    }

    @Test
    void testIfTextDoesNotExist()
    {
        when(uiContext.getSearchContext()).thenReturn(mockedWebElement);
        when(uiContext.getSearchContext(WebElement.class)).thenReturn(mockedWebElement);
        when(elementValidations.assertIfElementContainsText(mockedWebElement, TEXT, false)).thenReturn(true);
        assertTrue(webElementsSteps.textDoesNotExist(TEXT));
    }

    @Test
    void testTextDoesNotExist()
    {
        when(uiContext.getSearchContext()).thenReturn(webDriver);
        when(mockedBaseValidations.assertIfElementDoesNotExist("An element with text 'text'",
                new Locator(WebLocatorType.CASE_SENSITIVE_TEXT, TEXT))).thenReturn(true);
        assertTrue(webElementsSteps.textDoesNotExist(TEXT));
    }

    @Test
    void testIfTextMatchesRegexWebElementContext()
    {
        when(uiContext.getSearchContext()).thenReturn(mockedWebElement);
        when(mockedWebElementActions.getElementText(mockedWebElement)).thenReturn(TEXT);
        webElementsSteps.ifTextMatchesRegex(REGEX);
        verify(softAssert).assertTrue(TEXT_MATCHES_REGEX_MESSAGE + REGEX, true);
    }

    @Test
    void testIfTextMatchesRegexWebDriverContextFirefox()
    {
        when(uiContext.getSearchContext()).thenReturn(webDriver);
        when(mockedWebElementActions.getPageText()).thenReturn(TEXT);
        webElementsSteps.ifTextMatchesRegex(REGEX);
        verify(softAssert).assertTrue(TEXT_MATCHES_REGEX_MESSAGE + REGEX, true);
    }

    @Test
    void testIfTextMatchesRegexEmptyText()
    {
        when(uiContext.getSearchContext()).thenReturn(mockedWebElement);
        when(mockedWebElementActions.getElementText(mockedWebElement)).thenReturn("");
        webElementsSteps.ifTextMatchesRegex(REGEX);
        verify(softAssert).assertTrue(TEXT_MATCHES_REGEX_MESSAGE + REGEX, false);
    }

    @Test
    void testIfTextMatchesRegexWebDriverContextChrome()
    {
        when(uiContext.getSearchContext()).thenReturn(webDriver);
        when(mockedWebElementActions.getPageText()).thenReturn(TEXT);
        webElementsSteps.ifTextMatchesRegex(REGEX);
        verify(softAssert).assertTrue(TEXT_MATCHES_REGEX_MESSAGE + REGEX, true);
    }

    @Test
    void testIfTextDoesntMatchRegexWebElementContext()
    {
        when(uiContext.getSearchContext()).thenReturn(mockedWebElement);
        when(mockedWebElementActions.getElementText(mockedWebElement)).thenReturn(ELEMENT_TEXT);
        when(mockedWebElementActions.getPseudoElementContent(mockedWebElement)).thenReturn(ELEMENT_TEXT);
        webElementsSteps.ifTextMatchesRegex(REGEX);
        verify(softAssert).assertTrue(TEXT_MATCHES_REGEX_MESSAGE + REGEX, false);
    }

    @Test
    void testIfTextDoesntMatchRegexWebElementContextPseudoElementContentEmpty()
    {
        when(uiContext.getSearchContext()).thenReturn(mockedWebElement);
        when(mockedWebElementActions.getElementText(mockedWebElement)).thenReturn(ELEMENT_TEXT);
        when(mockedWebElementActions.getPseudoElementContent(mockedWebElement)).thenReturn("");
        webElementsSteps.ifTextMatchesRegex(REGEX);
        verify(softAssert).assertTrue(TEXT_MATCHES_REGEX_MESSAGE + REGEX, false);
    }

    @Test
    void testIfTextDoesntMatchRegexWebDriverContextChrome()
    {
        when(uiContext.getSearchContext()).thenReturn(webDriver);
        when(mockedWebElementActions.getPageText()).thenReturn(ELEMENT_TEXT);
        webElementsSteps.ifTextMatchesRegex(REGEX);
        verify(softAssert).assertTrue(TEXT_MATCHES_REGEX_MESSAGE + REGEX, false);
    }

    @Test
    void testIfTextExistsFirefox()
    {
        List<WebElement> webElementList = List.of();
        By locator = LocatorUtil.getXPathLocatorByInnerText(TEXT);
        when(uiContext.getSearchContext()).thenReturn(webDriver);
        Mockito.lenient().when(webDriver.findElements(locator)).thenReturn(webElementList);
        when(searchActions.findElements(eq(webDriver), any(Locator.class)))
                .thenReturn(List.of(mockedWebElement));
        webElementsSteps.ifTextExists(TEXT.toUpperCase());
        verify(softAssert).assertTrue("There is an element with text=TEXT in the context", true);
    }

    @Test
    void testAreElementSorted()
    {
        WebElement webElement = mock(WebElement.class);
        List<WebElement> elements = List.of(webElement, webElement);
        Locator locator = new Locator(WebLocatorType.XPATH, XPATH);
        when(mockedBaseValidations.assertIfNumberOfElementsFound(String.format(FIELDS_BY_LOCATOR, locator),
                locator, 2, ComparisonRule.GREATER_THAN_OR_EQUAL_TO)).thenReturn(elements);
        when(mockedWebElementActions.getElementText(webElement)).thenReturn(TEXT);
        when(softAssert.assertTrue(String.format(FIELD_CONTAINS_TEXT, 1), true)).thenReturn(true);
        when(softAssert.assertTrue(String.format(FIELD_CONTAINS_TEXT, 2), true)).thenReturn(true);
        webElementsSteps.areElementSorted(locator, SortingOrder.DESCENDING);
        List<String> textOfElements = List.of(TEXT, TEXT);
        verify(softAssert).assertEquals(String.format(ELEMENTS_ARE_SORTED, SortingOrder.DESCENDING),
                textOfElements, textOfElements);
    }

    @Test
    void testAreElementSortedWithNullList()
    {
        WebElement webElement = mock(WebElement.class);
        List<WebElement> elements = List.of(webElement, webElement);
        Locator locator = new Locator(WebLocatorType.XPATH, XPATH);
        when(mockedBaseValidations.assertIfNumberOfElementsFound(String.format(FIELDS_BY_LOCATOR,
                locator), locator, 2, ComparisonRule.GREATER_THAN_OR_EQUAL_TO)).thenReturn(elements);
        when(mockedWebElementActions.getElementText(webElement)).thenReturn(null);
        webElementsSteps.areElementSorted(locator, SortingOrder.ASCENDING);
        verify(softAssert).assertTrue(String.format(FIELD_CONTAINS_TEXT, 1), false);
        verify(softAssert).assertTrue(String.format(FIELD_CONTAINS_TEXT, 2), false);
    }

    @Test
    void testAreElementSortedWithEmptyTexts()
    {
        WebElement webElement = mock(WebElement.class);
        WebElement webElement2 = mock(WebElement.class);
        List<WebElement> elements = List.of(webElement, webElement2);
        Locator locator = new Locator(WebLocatorType.XPATH, XPATH);
        when(mockedBaseValidations.assertIfNumberOfElementsFound(String.format(FIELDS_BY_LOCATOR, locator),
                locator, 2, ComparisonRule.GREATER_THAN_OR_EQUAL_TO)).thenReturn(elements);
        when(mockedWebElementActions.getElementText(webElement)).thenReturn(TEXT);
        when(mockedWebElementActions.getElementText(webElement2)).thenReturn(StringUtils.EMPTY);
        when(softAssert.assertTrue(String.format(FIELD_CONTAINS_TEXT, 1), true)).thenReturn(true);
        when(softAssert.assertTrue(String.format(FIELD_CONTAINS_TEXT, 2), false)).thenReturn(false);
        List<String> sortedTextOfElements = List.of(TEXT);
        webElementsSteps.areElementSorted(locator, SortingOrder.ASCENDING);
        verify(softAssert).assertEquals(String.format(ELEMENTS_ARE_SORTED, SortingOrder.ASCENDING),
                sortedTextOfElements, sortedTextOfElements);
    }
}
