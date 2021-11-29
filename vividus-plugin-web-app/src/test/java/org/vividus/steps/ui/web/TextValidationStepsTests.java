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

package org.vividus.steps.ui.web;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.vividus.softassert.ISoftAssert;
import org.vividus.steps.ui.validation.IBaseValidations;
import org.vividus.steps.ui.web.validation.IElementValidations;
import org.vividus.ui.action.SearchActions;
import org.vividus.ui.action.search.Locator;
import org.vividus.ui.context.IUiContext;
import org.vividus.ui.web.action.IWebElementActions;
import org.vividus.ui.web.action.search.WebLocatorType;
import org.vividus.ui.web.util.LocatorUtil;

@ExtendWith(MockitoExtension.class)
class TextValidationStepsTests
{
    private static final String TEXT = "text";
    private static final Pattern REGEX = Pattern.compile("[a-zA-Z]+");
    private static final String TEXT_MATCHES_REGEX_MESSAGE = "The text in search context matches regular expression ";
    private static final String THERE_IS_AN_ELEMENT_WITH_TEXT_TEXT_IN_THE_CONTEXT = "There"
            + " is an element with text=text in the context";
    private static final String ELEMENT_TEXT = "1";

    @Mock private IUiContext uiContext;
    @Mock private IBaseValidations baseValidations;
    @Mock private IElementValidations elementValidations;
    @Mock private IWebElementActions webElementActions;
    @Mock private SearchActions searchActions;
    @Mock private ISoftAssert softAssert;
    @InjectMocks private TextValidationSteps textValidationSteps;
    @Mock private WebElement webElement;
    @Mock private WebDriver webDriver;

    @Test
    void testCheckPageContainsTextThrowsWebDriverException()
    {
        By locator = LocatorUtil.getXPathLocatorByInnerText(TEXT);
        when(uiContext.getSearchContext()).thenReturn(webDriver);
        when(uiContext.getSearchContext(SearchContext.class)).thenReturn(webDriver);
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

                return List.of(webElement);
            }
        });
        textValidationSteps.ifTextExists(TEXT);
        verify(softAssert).assertTrue(THERE_IS_AN_ELEMENT_WITH_TEXT_TEXT_IN_THE_CONTEXT, true);
    }

    @Test
    void testCheckPageContainsText()
    {
        when(uiContext.getSearchContext()).thenReturn(webDriver);
        when(uiContext.getSearchContext(SearchContext.class)).thenReturn(webDriver);
        when(webDriver.findElements(LocatorUtil.getXPathLocatorByInnerText(TEXT))).thenReturn(List.of(webElement));
        textValidationSteps.ifTextExists(TEXT);
        verify(softAssert).assertTrue(THERE_IS_AN_ELEMENT_WITH_TEXT_TEXT_IN_THE_CONTEXT, true);
    }

    @Test
    void testDomElementsContainText()
    {
        when(uiContext.getSearchContext()).thenReturn(webElement);
        textValidationSteps.ifTextExists(TEXT);
        verify(elementValidations).assertIfElementContainsText(webElement, TEXT, true);
    }

    @Test
    void testCheckPageContainsTextInPseudoElements()
    {
        when(uiContext.getSearchContext()).thenReturn(webDriver);
        when(uiContext.getSearchContext(SearchContext.class)).thenReturn(webDriver);
        when(webElementActions.getAllPseudoElementsContent()).thenReturn(List.of(TEXT));
        textValidationSteps.ifTextExists(TEXT);
        verify(softAssert).assertTrue(THERE_IS_AN_ELEMENT_WITH_TEXT_TEXT_IN_THE_CONTEXT, true);
    }

    @Test
    void testIfTextExists()
    {
        when(uiContext.getSearchContext()).thenReturn(webDriver);
        when(uiContext.getSearchContext(SearchContext.class)).thenReturn(webDriver);
        when(webElementActions.getAllPseudoElementsContent()).thenReturn(List.of());
        when(webElementActions.getPageText()).thenReturn("no");
        textValidationSteps.ifTextExists(TEXT);
        verify(softAssert).assertTrue(THERE_IS_AN_ELEMENT_WITH_TEXT_TEXT_IN_THE_CONTEXT, false);
    }

    @Test
    void testIfTextExistsOnPage()
    {
        when(uiContext.getSearchContext()).thenReturn(webDriver);
        when(uiContext.getSearchContext(SearchContext.class)).thenReturn(webDriver);
        when(webElementActions.getAllPseudoElementsContent()).thenReturn(List.of());
        when(webElementActions.getPageText()).thenReturn(TEXT);
        textValidationSteps.ifTextExists(TEXT);
        verify(softAssert).assertTrue(THERE_IS_AN_ELEMENT_WITH_TEXT_TEXT_IN_THE_CONTEXT, true);
    }

    @Test
    void testIfTextDoesNotExist()
    {
        when(uiContext.getSearchContext(SearchContext.class)).thenReturn(webElement);
        when(elementValidations.assertIfElementContainsText(webElement, TEXT, false)).thenReturn(true);
        assertTrue(textValidationSteps.textDoesNotExist(TEXT));
    }

    @Test
    void testTextDoesNotExist()
    {
        when(uiContext.getSearchContext(SearchContext.class)).thenReturn(webDriver);
        when(baseValidations.assertIfElementDoesNotExist("An element with text 'text'", webDriver,
                new Locator(WebLocatorType.CASE_SENSITIVE_TEXT, TEXT))).thenReturn(true);
        assertTrue(textValidationSteps.textDoesNotExist(TEXT));
    }

    @ParameterizedTest
    @CsvSource({
            "text, true",
            "'',   false"
    })
    void testIfTextMatchesRegexWebElementContext(String elementText, boolean actual)
    {
        when(uiContext.getSearchContext(SearchContext.class)).thenReturn(webElement);
        when(webElementActions.getElementText(webElement)).thenReturn(elementText);
        textValidationSteps.ifTextMatchesRegex(REGEX);
        verify(softAssert).assertTrue(TEXT_MATCHES_REGEX_MESSAGE + REGEX, actual);
    }

    @ParameterizedTest
    @CsvSource({
            "text, true",
            "1,    false"
    })
    void testIfTextMatchesRegexWebDriverContext(String pageText, boolean actual)
    {
        when(uiContext.getSearchContext(SearchContext.class)).thenReturn(webDriver);
        when(webElementActions.getPageText()).thenReturn(pageText);
        textValidationSteps.ifTextMatchesRegex(REGEX);
        verify(softAssert).assertTrue(TEXT_MATCHES_REGEX_MESSAGE + REGEX, actual);
    }

    @ParameterizedTest
    @CsvSource({
            "1,   false",
            "'',  false",
            "abc, true"
    })
    void testIfTextDoesntMatchRegexWebElementContext(String pseudoElementContent, boolean actual)
    {
        when(uiContext.getSearchContext(SearchContext.class)).thenReturn(webElement);
        when(webElementActions.getElementText(webElement)).thenReturn(ELEMENT_TEXT);
        when(webElementActions.getPseudoElementContent(webElement)).thenReturn(pseudoElementContent);
        textValidationSteps.ifTextMatchesRegex(REGEX);
        verify(softAssert).assertTrue(TEXT_MATCHES_REGEX_MESSAGE + REGEX, actual);
    }

    @Test
    void testIfTextExistsFirefox()
    {
        By locator = LocatorUtil.getXPathLocatorByInnerText(TEXT);
        when(uiContext.getSearchContext()).thenReturn(webDriver);
        when(uiContext.getSearchContext(SearchContext.class)).thenReturn(webDriver);
        Mockito.lenient().when(webDriver.findElements(locator)).thenReturn(List.of());
        when(searchActions.findElements(eq(webDriver), any(Locator.class))).thenReturn(List.of(webElement));
        textValidationSteps.ifTextExists(TEXT.toUpperCase());
        verify(softAssert).assertTrue("There is an element with text=TEXT in the context", true);
    }
}
