/*
 * Copyright 2019-2023 the original author or authors.
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

package org.vividus.steps.ui.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.vividus.testdouble.TestLocatorType.SEARCH;
import static org.vividus.ui.action.search.IElementAction.NOT_SET_CONTEXT;

import java.util.List;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Function;

import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.steps.ComparisonRule;
import org.vividus.ui.IState;
import org.vividus.ui.action.ISearchActions;
import org.vividus.ui.action.search.Locator;
import org.vividus.ui.action.search.SearchParameters;
import org.vividus.ui.context.IUiContext;
import org.vividus.ui.validation.matcher.ExistsMatcher;
import org.vividus.ui.validation.matcher.ExpectedConditionsMatcher;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("MethodCount")
class BaseValidationsTests
{
    private static final String IS_NOT_FOUND_BY = " is not found by ";
    private static final String IS_NOT_FOUND_BY_THE_LOCATOR = IS_NOT_FOUND_BY + "the locator ";
    private static final String SOME_ELEMENT = "Some element";
    private static final String BUSINESS_DESCRIPTION = "Test business description";
    private static final String XPATH_INT = ".//xpath=1";
    private static final String EQUAL_TO_MATCHER = "number of elements is a value equal to <1>";
    private static final String AT_LEAST_ONE_ELEMENT_ASSERTION = "There is at least one element with attributes ' "
            + "Search: './/xpath=1'; Visibility: VISIBLE;'";
    private static final String NUMBER_OF_ELEMENTS_IS_GREATER_THAN_0 = "number of elements is a value greater"
            + " than <0>";

    @Mock private WebDriver mockedWebDriver;
    @Mock private SearchContext mockedSearchContext;
    @Mock private WebElement mockedWebElement;
    @Mock private ExpectedCondition<?> mockedExpectedCondition;
    @Mock private IWebDriverProvider mockedWebDriverProvider;
    @Mock private IUiContext uiContext;
    @Mock private ISearchActions searchActions;
    @Mock private IDescriptiveSoftAssert softAssert;
    @InjectMocks private BaseValidations baseValidations;

    private List<WebElement> webElements;
    private BaseValidations spy;

    @SuppressWarnings("unchecked")
    @Test
    void testAssertIfElementExistsWithLocator()
    {
        webElements = List.of(mockedWebElement);
        when(uiContext.getSearchContext()).thenReturn(mockedSearchContext);
        var attributes = mock(Locator.class);
        when(searchActions.findElements(mockedSearchContext, attributes)).thenReturn(webElements);
        when(attributes.toString()).thenReturn("attributes");
        when(softAssert.assertThat(eq(SOME_ELEMENT), anyString(), eq(webElements),
                any(Matcher.class))).thenReturn(true);
        var foundElement = baseValidations.assertIfElementExists(SOME_ELEMENT, attributes);
        assertEquals(mockedWebElement, foundElement);
    }

    @Test
    void shouldRecordFailedAssertionInCaseOfNullContext()
    {
        var attributes = mock(Locator.class);
        assertNull(baseValidations.assertIfElementExists(SOME_ELEMENT, attributes));
        verify(softAssert).recordFailedAssertion(NOT_SET_CONTEXT);
        verifyNoMoreInteractions(softAssert);
        verifyNoInteractions(searchActions);
    }

    @Test
    void shouldNotSearchIfContextNullAssertIfElementExists()
    {
        var locator = new Locator(SEARCH, XPATH_INT);
        assertNull(baseValidations.assertIfElementExists(BUSINESS_DESCRIPTION, null, locator));
        verify(softAssert).recordFailedAssertion(NOT_SET_CONTEXT);
        verifyNoMoreInteractions(softAssert);
        verifyNoInteractions(searchActions);
    }

    @Test
    void shouldNotSearchIfContextNotPresentAssertIfElementsExist()
    {
        var locator = new Locator(SEARCH, XPATH_INT);
        when(uiContext.getOptionalSearchContext()).thenReturn(Optional.empty());
        assertEquals(List.of(), baseValidations.assertIfElementsExist(BUSINESS_DESCRIPTION, locator));
        verifyNoInteractions(searchActions, softAssert);
    }

    @Test
    void shouldNotSearchIfContextNullAssertIfExactNumberOfElementsFound()
    {
        var locator = new Locator(SEARCH, XPATH_INT);
        assertFalse(baseValidations.assertIfExactNumberOfElementsFound(BUSINESS_DESCRIPTION, null, locator, 1));
        verify(softAssert).recordFailedAssertion(NOT_SET_CONTEXT);
        verifyNoMoreInteractions(softAssert);
        verifyNoInteractions(searchActions);
    }

    @Test
    void shouldNotSearchIfContextNotPresentAssertIfNumberOfElementsFound()
    {
        var locator = new Locator(SEARCH, XPATH_INT);
        when(uiContext.getOptionalSearchContext()).thenReturn(Optional.empty());
        assertEquals(List.of(), baseValidations.assertIfNumberOfElementsFound(BUSINESS_DESCRIPTION, locator, 0,
                ComparisonRule.EQUAL_TO));
        verifyNoInteractions(searchActions, softAssert);
    }

    @Test
    void testAssertIfElementDoesNotExistWithLocator()
    {
        spy = Mockito.spy(baseValidations);
        when(uiContext.getOptionalSearchContext()).thenReturn(Optional.of(mockedSearchContext));
        var searchParameters = new SearchParameters();
        var attributes = new Locator(SEARCH, searchParameters);
        when(searchActions.findElements(mockedSearchContext, attributes)).thenReturn(List.of());
        assertTrue(spy.assertIfElementDoesNotExist(BUSINESS_DESCRIPTION, attributes));
        assertFalse(searchParameters.isWaitForElement());
    }

    @Test
    void testAssertElementDoesNotExistWithLocator()
    {
        spy = Mockito.spy(baseValidations);
        when(uiContext.getOptionalSearchContext()).thenReturn(Optional.of(mockedSearchContext));
        var searchParameters = new SearchParameters();
        var attributes = new Locator(SEARCH, searchParameters);
        when(softAssert.recordAssertion(true,
                BUSINESS_DESCRIPTION + IS_NOT_FOUND_BY + attributes.toHumanReadableString()))
                        .thenReturn(true);
        when(searchActions.findElements(mockedSearchContext, attributes)).thenReturn(List.of());
        assertTrue(spy.assertElementDoesNotExist(BUSINESS_DESCRIPTION, attributes));
        assertFalse(searchParameters.isWaitForElement());
    }

    @Test
    void testAssertIfElementDoesNotExistWhenNoFailedAssertionRecordingIsNeeded()
    {
        spy = Mockito.spy(baseValidations);
        when(uiContext.getOptionalSearchContext()).thenReturn(Optional.of(mockedSearchContext));
        var searchParameters = new SearchParameters();
        var attributes = new Locator(SEARCH, searchParameters);
        when(searchActions.findElements(mockedSearchContext, attributes)).thenReturn(List.of(mockedWebElement));
        assertFalse(spy.assertIfElementDoesNotExist(BUSINESS_DESCRIPTION, attributes));
        assertFalse(searchParameters.isWaitForElement());
        verifyNoInteractions(softAssert);
    }

    @Test
    void testAssertElementDoesNotExistWhenNoFailedAssertionRecordingIsNeeded()
    {
        spy = Mockito.spy(baseValidations);
        when(uiContext.getOptionalSearchContext()).thenReturn(Optional.of(mockedSearchContext));
        var searchParameters = new SearchParameters();
        var attributes = new Locator(SEARCH, searchParameters);
        when(searchActions.findElements(mockedSearchContext, attributes)).thenReturn(List.of(mockedWebElement));
        assertFalse(spy.assertElementDoesNotExist(BUSINESS_DESCRIPTION, attributes));
        assertFalse(searchParameters.isWaitForElement());
        String expectedAssertionMsg = String
                .format("The number of elements found by %s is 1, but expected 0",
                        attributes.toHumanReadableString());
        verify(softAssert).recordAssertion(false, expectedAssertionMsg);
    }

    @Test
    void testAssertElementStateNullWebElement()
    {
        var state = mock(IState.class);
        var result = baseValidations.assertElementState(BUSINESS_DESCRIPTION, state, null);
        verifyNoInteractions(state);
        assertFalse(result);
    }

    @SuppressWarnings("unchecked")
    @Test
    void testAssertWebElementStateSuccess()
    {
        when(mockedWebDriverProvider.get()).thenReturn(mockedWebDriver);
        var mockedExpectedConditionToString = mockedExpectedCondition.toString();
        var state = mock(IState.class);
        doReturn(mockedExpectedCondition).when(state).getExpectedCondition(mockedWebElement);
        when(softAssert.assertThat(eq(BUSINESS_DESCRIPTION), eq(mockedExpectedConditionToString),
                eq(mockedWebDriver), argThat(matcher -> matcher instanceof ExpectedConditionsMatcher)))
                .thenReturn(Boolean.TRUE);
        assertTrue(baseValidations.assertElementState(BUSINESS_DESCRIPTION, state, mockedWebElement));
    }

    @Test
    void testAssertElementStateResultNotBoolean()
    {
        spy = Mockito.spy(baseValidations);
        when(mockedWebDriverProvider.get()).thenReturn(mockedWebDriver);
        var mockedExpectedConditionToString = mockedExpectedCondition.toString();
        var state = mock(IState.class);
        doReturn(mockedExpectedCondition).when(state).getExpectedCondition(mockedWebElement);
        spy.assertElementState(BUSINESS_DESCRIPTION, state, mockedWebElement);
        verify(softAssert).assertThat(eq(BUSINESS_DESCRIPTION), eq(mockedExpectedConditionToString),
                eq(mockedWebDriver), argThat(matcher -> matcher instanceof ExpectedConditionsMatcher));
    }

    @Test
    void testAssertIfElementsExist()
    {
        webElements = List.of(mockedWebElement);
        when(uiContext.getOptionalSearchContext()).thenReturn(Optional.of(mockedSearchContext));
        var locator = new Locator(SEARCH, XPATH_INT);
        when(searchActions.findElements(mockedSearchContext, locator)).thenReturn(webElements);
        baseValidations.assertIfElementsExist(BUSINESS_DESCRIPTION, locator);
        verify(softAssert).assertThat(eq(BUSINESS_DESCRIPTION), eq(AT_LEAST_ONE_ELEMENT_ASSERTION), eq(webElements),
                argThat(e -> NUMBER_OF_ELEMENTS_IS_GREATER_THAN_0.equals(e.toString())));
        assertEquals(1, webElements.size());
    }

    @Test
    void testAssertNoElementFound()
    {
        List<WebElement> webElements = List.of();
        when(uiContext.getSearchContext()).thenReturn(mockedSearchContext);
        var attributes = new Locator(SEARCH, XPATH_INT);
        when(searchActions.findElements(mockedSearchContext, attributes)).thenReturn(webElements);
        when(softAssert.assertThat(eq(BUSINESS_DESCRIPTION),
                eq("An element with attributes: ' Search: './/xpath=1'; Visibility: VISIBLE;'"), eq(webElements),
                argThat(matcher -> matcher instanceof ExistsMatcher))).thenReturn(false);
        var element = baseValidations.assertIfElementExists(BUSINESS_DESCRIPTION, attributes);
        assertNull(element);
    }

    @Test
    void testAssertIfAtLeastNumberOfElementsExistSuccess()
    {
        var webElements = List.of(mock(WebElement.class));
        mockAssertingWebElements(webElements);
        var result = testAssertIfAtLeastNumberOfElementsExist(webElements, true);
        assertEquals(webElements, result);
    }

    @Test
    void testAssertIfAtLeastNumberOfElementsExistFailed()
    {
        var webElements = List.of(mock(WebElement.class));
        mockAssertingWebElements(webElements);
        var result = testAssertIfAtLeastNumberOfElementsExist(webElements, false);
        assertTrue(result.isEmpty());
    }

    private List<WebElement> testAssertIfAtLeastNumberOfElementsExist(List<WebElement> webElements,
            boolean assertionResult)
    {
        when(uiContext.getOptionalSearchContext()).thenReturn(Optional.of(mockedSearchContext));
        var locator = new Locator(SEARCH, XPATH_INT);
        when(searchActions.findElements(mockedSearchContext, locator)).thenReturn(webElements);
        var leastCount = 1;
        var systemDescription = String.format("There are at least %d elements with attributes '%s'", leastCount,
                locator);
        when(softAssert.assertThat(eq(BUSINESS_DESCRIPTION), eq(systemDescription), eq(webElements),
                argThat(e -> "number of elements is a value equal to or greater than <1>".equals(e.toString()))))
                .thenReturn(assertionResult);
        return baseValidations.assertIfAtLeastNumberOfElementsExist(BUSINESS_DESCRIPTION, locator, leastCount);
    }

    @Test
    void testAssertIfNumberOfElementsFoundWithoutContext()
    {
        when(uiContext.getOptionalSearchContext()).thenReturn(Optional.of(mockedSearchContext));
        testAssertIfNumberOfElementsFound(
                attributes -> baseValidations.assertIfNumberOfElementsFound(BUSINESS_DESCRIPTION, attributes, 1,
                        ComparisonRule.EQUAL_TO), true, List.of(mockedWebElement));
    }

    @Test
    void testAssertIfNumberOfElementsFoundWithContext()
    {
        when(uiContext.getOptionalSearchContext()).thenReturn(Optional.of(mockedSearchContext));
        testAssertIfNumberOfElementsFound(
                attributes -> baseValidations.assertIfNumberOfElementsFound(BUSINESS_DESCRIPTION, attributes, 1,
                        ComparisonRule.EQUAL_TO), true, List.of(mockedWebElement));
    }

    @Test
    void testAssertIfNumberOfElementsFoundWithoutContextNotDesiredQuantity()
    {
        when(uiContext.getOptionalSearchContext()).thenReturn(Optional.of(mockedSearchContext));
        testAssertIfNumberOfElementsFound(
                attributes -> baseValidations.assertIfNumberOfElementsFound(BUSINESS_DESCRIPTION, attributes, 1,
                        ComparisonRule.EQUAL_TO), false, List.of());
    }

    @Test
    void testAssertIfNumberOfElementsFoundWithContextNotDesiredQuantity()
    {
        when(uiContext.getOptionalSearchContext()).thenReturn(Optional.of(mockedSearchContext));
        testAssertIfNumberOfElementsFound(
                attributes -> baseValidations.assertIfNumberOfElementsFound(BUSINESS_DESCRIPTION, attributes, 1,
                        ComparisonRule.EQUAL_TO), false, List.of());
    }

    @Test
    void testAssertElementExists()
    {
        when(uiContext.getOptionalSearchContext()).thenReturn(Optional.of(mockedSearchContext));
        var locator = mockSuccessfulElementSearch();
        var element = baseValidations.assertElementExists(BUSINESS_DESCRIPTION, locator);
        assertEquals(Optional.of(mockedWebElement), element);
    }

    @Test
    void shouldReturnElementIfItExists()
    {
        var locator = mockSuccessfulElementSearch();
        var element = baseValidations.assertElementExists(BUSINESS_DESCRIPTION, mockedSearchContext, locator);
        assertEquals(Optional.of(mockedWebElement), element);
    }

    private Locator mockSuccessfulElementSearch()
    {
        var locator = new Locator(SEARCH, XPATH_INT);
        when(searchActions.findElements(mockedSearchContext, locator)).thenReturn(List.of(mockedWebElement));
        when(softAssert.recordAssertion(true, BUSINESS_DESCRIPTION + " is found by the locator " + locator)).thenReturn(
                true);
        return locator;
    }

    @Test
    void testAssertElementsNumber()
    {
        var elements = List.of(mockedWebElement);
        mockAssertingWebElements(elements);
        when(softAssert.assertThat(eq(BUSINESS_DESCRIPTION), eq(elements.size()),
                argThat(e -> "a value equal to <1>".equals(e.toString())))).thenReturn(true);
        assertTrue(baseValidations.assertElementsNumber(BUSINESS_DESCRIPTION, elements, ComparisonRule.EQUAL_TO, 1));
    }

    @Test
    void testAssertElementExistsMoreThanOne()
    {
        webElements = List.of(mockedWebElement, mockedWebElement);
        when(uiContext.getOptionalSearchContext()).thenReturn(Optional.of(mockedSearchContext));
        var locator = new Locator(SEARCH, XPATH_INT);
        when(searchActions.findElements(mockedSearchContext, locator)).thenReturn(webElements);
        var element = baseValidations.assertElementExists(BUSINESS_DESCRIPTION, locator);
        assertTrue(element.isEmpty());
        verify(softAssert).recordFailedAssertion(
                "The number of elements found by the locator " + locator + " is 2, but expected 1");
    }

    @Test
    void testAssertElementExistsEmptyElements()
    {
        List<WebElement> elements = List.of();
        when(uiContext.getOptionalSearchContext()).thenReturn(Optional.of(mockedSearchContext));
        var locator = new Locator(SEARCH, XPATH_INT);
        when(searchActions.findElements(mockedSearchContext, locator)).thenReturn(elements);
        when(softAssert.recordAssertion(false,
                BUSINESS_DESCRIPTION + IS_NOT_FOUND_BY_THE_LOCATOR + locator.toString())).thenReturn(false);
        var element = baseValidations.assertElementExists(BUSINESS_DESCRIPTION, locator);
        assertTrue(element.isEmpty());
    }

    @Test
    void shouldFailIfExpectedNumberOfElementsIsLessThanZero()
    {
        when(uiContext.getOptionalSearchContext()).thenReturn(Optional.of(mockedSearchContext));
        var exception = assertThrows(IllegalArgumentException.class, () -> baseValidations
                .assertIfNumberOfElementsFound(BUSINESS_DESCRIPTION, null, 0, ComparisonRule.LESS_THAN));
        assertEquals("Invalid input rule: the number of elements can not be less than 0", exception.getMessage());
    }

    @ParameterizedTest
    @CsvSource({
            "EQUAL_TO, number of elements is a value equal to <0>, is equal to 0",
            "LESS_THAN_OR_EQUAL_TO, number of elements is a value less than or equal to <0>, is less than or equal to 0"
    })
    void shouldNotWaitForElementsIfExpectedNumberOfElementsIsEqualToOrLessThanOrEqualToZero(ComparisonRule rule,
            String matcherAsString, String comparison)
    {
        when(uiContext.getOptionalSearchContext()).thenReturn(Optional.of(mockedSearchContext));
        testAssertIfNumberOfElementsFound(
                attributes -> baseValidations.assertIfNumberOfElementsFound(BUSINESS_DESCRIPTION, attributes, 0, rule),
                false, List.of(), comparison, matcherAsString, false);
    }

    @Test
    void shouldWaitForElementIfExpectedNumberOfElementsIsGreaterThanOrEqualToZero()
    {
        when(uiContext.getOptionalSearchContext()).thenReturn(Optional.of(mockedSearchContext));
        testAssertIfNumberOfElementsFound(
                attributes -> baseValidations.assertIfNumberOfElementsFound(BUSINESS_DESCRIPTION, attributes, 0,
                        ComparisonRule.GREATER_THAN_OR_EQUAL_TO), true, List.of(), "is greater than or equal to 0",
                "number of elements is a value equal to or greater than <0>", true);
    }

    @Test
    void shouldAssertNumberOfElementsFound()
    {
        spy = Mockito.spy(baseValidations);
        var locator = mock(Locator.class);
        var webElements = List.of(mockedWebElement);

        when(uiContext.getOptionalSearchContext()).thenReturn(Optional.of(mockedSearchContext));
        when(searchActions.findElements(mockedSearchContext, locator)).thenReturn(webElements);
        doReturn(true).when(spy).assertElementsNumber(BUSINESS_DESCRIPTION, webElements, ComparisonRule.GREATER_THAN,
                1);

        assertEquals(webElements,
                spy.assertNumberOfElementsFound(BUSINESS_DESCRIPTION, locator, 1, ComparisonRule.GREATER_THAN));
    }

    private void testAssertIfNumberOfElementsFound(Function<Locator, List<WebElement>> actualCall, boolean checkPassed,
            List<WebElement> foundElements)
    {
        testAssertIfNumberOfElementsFound(actualCall, checkPassed, foundElements, "is equal to 1", EQUAL_TO_MATCHER,
                true);
    }

    private void testAssertIfNumberOfElementsFound(Function<Locator, List<WebElement>> actualCall,
            boolean checkPassed, List<WebElement> foundElements, String comparison, String marcher, boolean wait)
    {
        var attributes = new Locator(SEARCH, XPATH_INT);
        when(searchActions.findElements(mockedSearchContext, attributes)).thenReturn(foundElements);
        when(softAssert.assertThat(eq(BUSINESS_DESCRIPTION),
                eq("Number of elements found by ' Search: './/xpath=1'; Visibility: VISIBLE;' " + comparison),
                eq(foundElements), argThat(m -> marcher.equals(m.toString())))).thenReturn(checkPassed);
        assertTrue(attributes.getSearchParameters().isWaitForElement());
        assertEquals(foundElements, actualCall.apply(attributes));
        assertEquals(wait, attributes.getSearchParameters().isWaitForElement());
    }

    private void mockAssertingWebElements(List<WebElement> elements)
    {
        doAnswer(a ->
        {
            var supplier = a.getArgument(1, BooleanSupplier.class);
            return supplier.getAsBoolean();
        }).when(uiContext).withAssertingWebElements(eq(elements), any());
    }
}
