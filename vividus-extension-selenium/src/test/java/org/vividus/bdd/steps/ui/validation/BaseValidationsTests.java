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

package org.vividus.bdd.steps.ui.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
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
import static org.mockito.Mockito.when;
import static org.vividus.testdouble.TestLocatorType.SEARCH;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Function;

import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WrapsElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.vividus.bdd.steps.ComparisonRule;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.ui.State;
import org.vividus.ui.action.ISearchActions;
import org.vividus.ui.action.search.Locator;
import org.vividus.ui.action.search.SearchParameters;
import org.vividus.ui.context.IUiContext;
import org.vividus.ui.validation.matcher.ExistsMatcher;
import org.vividus.ui.validation.matcher.ExpectedConditionsMatcher;
import org.vividus.ui.validation.matcher.NotExistsMatcher;

@ExtendWith(MockitoExtension.class)
class BaseValidationsTests
{
    private static final String SOME_ELEMENT = "Some element";
    private static final String PATTERN_ELEMENTS = "Number %1$s of elements found by '%2$s'";
    private static final String BUSINESS_DESCRIPTION = "Test business description";
    private static final String SYSTEM_DESCRIPTION =
            "An element with attributes: ' Search: './/xpath=1'; Visibility: VISIBLE;'";
    private static final String XPATH_INT = ".//xpath=1";
    private static final int INT_ARG = 1;
    private static final String EQUAL_TO_MATCHER = "number of elements is a value equal to <1>";

    @Mock
    private WebDriver mockedWebDriver;

    @Mock
    private SearchContext mockedSearchContext;

    private List<WebElement> webElements;

    @Mock
    private WebElement mockedWebElement;

    @Mock
    private ExpectedCondition<?> mockedExpectedCondition;

    @Mock
    private IWebDriverProvider mockedWebDriverProvider;

    @Mock
    private IUiContext uiContext;

    @Mock
    private ISearchActions searchActions;

    @Mock
    private IDescriptiveSoftAssert softAssert;

    @InjectMocks
    private BaseValidations baseValidations;

    private BaseValidations spy;

    @SuppressWarnings("unchecked")
    @Test
    void testAssertIfElementExistsWithLocator()
    {
        webElements = List.of(mockedWebElement);
        when(uiContext.getSearchContext()).thenReturn(mockedSearchContext);
        Locator attributes = mock(Locator.class);
        when(searchActions.findElements(eq(mockedSearchContext), eq(attributes))).thenReturn(webElements);
        when(attributes.toString()).thenReturn("attributes");
        when(softAssert.assertThat(eq(SOME_ELEMENT), anyString(), eq(webElements),
                any(Matcher.class))).thenReturn(true);
        WebElement foundElement = baseValidations.assertIfElementExists(SOME_ELEMENT, attributes);
        assertEquals(mockedWebElement, foundElement);
    }

    @Test
    void testAssertIfElementDoesNotExistWithLocator()
    {
        spy = Mockito.spy(baseValidations);
        when(uiContext.getSearchContext()).thenReturn(mockedSearchContext);
        SearchParameters searchParameters = new SearchParameters();
        Locator attributes = new Locator(SEARCH, searchParameters);
        when(searchActions.findElements(mockedSearchContext, attributes)).thenReturn(List.of());
        assertTrue(spy.assertIfElementDoesNotExist(BUSINESS_DESCRIPTION, attributes));
        assertFalse(searchParameters.isWaitForElement());
    }

    @Test
    void testAssertIfElementDoesNotExistWhenNoFailedAssertionRecordingIsNeeded()
    {
        spy = Mockito.spy(baseValidations);
        webElements = List.of(mockedWebElement);
        when(uiContext.getSearchContext()).thenReturn(mockedSearchContext);
        SearchParameters searchParameters = new SearchParameters();
        Locator attributes = new Locator(SEARCH, searchParameters);
        when(searchActions.findElements(mockedSearchContext, attributes)).thenReturn(webElements);
        assertFalse(spy.assertIfElementDoesNotExist(BUSINESS_DESCRIPTION, attributes, false));
        assertFalse(searchParameters.isWaitForElement());
        verifyNoInteractions(softAssert);
    }

    @Test
    void testAssertIfElementDoesNotExistWithSystemDescription()
    {
        spy = Mockito.spy(baseValidations);
        webElements = List.of(mockedWebElement);
        when(uiContext.getSearchContext()).thenReturn(mockedSearchContext);
        SearchParameters searchParameters = new SearchParameters();
        Locator attributes = new Locator(SEARCH, searchParameters);
        when(searchActions.findElements(mockedSearchContext, attributes)).thenReturn(webElements);
        assertFalse(spy.assertIfElementDoesNotExist(BUSINESS_DESCRIPTION, SYSTEM_DESCRIPTION, attributes));
        assertFalse(searchParameters.isWaitForElement());
        verify(softAssert).assertThat(eq(BUSINESS_DESCRIPTION), eq(SYSTEM_DESCRIPTION), eq(webElements),
                argThat(matcher -> matcher instanceof NotExistsMatcher));
    }

    @Test
    void testAssertIfElementDoesNotExistWithSystemDescriptionWhenNoFailedAssertionRecordingIsNeeded()
    {
        spy = Mockito.spy(baseValidations);
        webElements = List.of(mockedWebElement);
        when(uiContext.getSearchContext()).thenReturn(mockedSearchContext);
        SearchParameters searchParameters = new SearchParameters();
        Locator attributes = new Locator(SEARCH, searchParameters);
        when(searchActions.findElements(mockedSearchContext, attributes)).thenReturn(webElements);
        assertFalse(spy.assertIfElementDoesNotExist(BUSINESS_DESCRIPTION, SYSTEM_DESCRIPTION, attributes, false));
        assertFalse(searchParameters.isWaitForElement());
        verifyNoInteractions(softAssert);
    }

    @Test
    void testAssertIfElementExistsWithEmptyList()
    {
        spy = Mockito.spy(baseValidations);
        assertNull(spy.assertIfElementExists(BUSINESS_DESCRIPTION, SYSTEM_DESCRIPTION, List.of()));
    }

    @Test
    void testAssertIfElementExistsWithSeveralElementsInList()
    {
        spy = Mockito.spy(baseValidations);
        List<WebElement> elements = Arrays.asList(mockedWebElement, mockedWebElement);
        mockAssertingWebElements(elements);
        assertNull(spy.assertIfElementExists(BUSINESS_DESCRIPTION, SYSTEM_DESCRIPTION, elements));
        verify(softAssert).assertThat(eq(BUSINESS_DESCRIPTION), eq(SYSTEM_DESCRIPTION),
                eq(elements), argThat(e -> EQUAL_TO_MATCHER.equals(e.toString())));
    }

    @Test
    void testAssertElementStateNullWebElement()
    {
        State state = mock(State.class);
        boolean result = baseValidations.assertElementState(BUSINESS_DESCRIPTION, state, (WebElement) null);
        verifyNoInteractions(state);
        assertFalse(result);
    }

    @SuppressWarnings("unchecked")
    @Test
    void testAssertWebElementStateSuccess()
    {
        when(mockedWebDriverProvider.get()).thenReturn(mockedWebDriver);
        String mockedExpectedConditionToString = mockedExpectedCondition.toString();
        State state = mock(State.class);
        doReturn(mockedExpectedCondition).when(state).getExpectedCondition(mockedWebElement);
        baseValidations.assertElementState(BUSINESS_DESCRIPTION, state, mockedWebElement);
        verify(softAssert).assertThat(eq(BUSINESS_DESCRIPTION), eq(mockedExpectedConditionToString),
                eq(mockedWebDriver), any(Matcher.class));
    }

    @Test
    void testAssertElementStateNullWrapsElement()
    {
        State state = mock(State.class);
        boolean result = baseValidations.assertElementState(BUSINESS_DESCRIPTION, state, (WrapsElement) null);
        verifyNoInteractions(state);
        assertFalse(result);
    }

    @Test
    void testAssertElementStateNWhenWrapsNullElement()
    {
        WrapsElement wrapsElement = mock(WrapsElement.class);
        when(wrapsElement.getWrappedElement()).thenReturn(null);
        State state = mock(State.class);
        boolean result = baseValidations.assertElementState(BUSINESS_DESCRIPTION, state, wrapsElement);
        verifyNoInteractions(state);
        assertFalse(result);
    }

    @Test
    void testAssertWrapsElementStateSuccess()
    {
        when(mockedWebDriverProvider.get()).thenReturn(mockedWebDriver);
        String mockedExpectedConditionToString = mockedExpectedCondition.toString();
        WrapsElement wrapsElement = mock(WrapsElement.class);
        when(wrapsElement.getWrappedElement()).thenReturn(mockedWebElement);
        State state = mock(State.class);
        doReturn(mockedExpectedCondition).when(state).getExpectedCondition(mockedWebElement);
        when(softAssert.assertThat(eq(BUSINESS_DESCRIPTION), eq(mockedExpectedConditionToString),
                eq(mockedWebDriver), argThat(matcher -> matcher instanceof ExpectedConditionsMatcher)))
                        .thenReturn(Boolean.TRUE);
        assertTrue(baseValidations.assertElementState(BUSINESS_DESCRIPTION, state, wrapsElement));
    }

    @Test
    void testAssertElementStateResultNotBoolean()
    {
        spy = Mockito.spy(baseValidations);
        when(mockedWebDriverProvider.get()).thenReturn(mockedWebDriver);
        String mockedExpectedConditionToString = mockedExpectedCondition.toString();
        State state = mock(State.class);
        doReturn(mockedExpectedCondition).when(state).getExpectedCondition(mockedWebElement);
        spy.assertElementState(BUSINESS_DESCRIPTION, state, mockedWebElement);
        verify(softAssert).assertThat(eq(BUSINESS_DESCRIPTION), eq(mockedExpectedConditionToString),
                eq(mockedWebDriver), argThat(matcher -> matcher instanceof ExpectedConditionsMatcher));
    }

    @Test
    void testAssertIfExactNumberOfElementsFound()
    {
        when(uiContext.getSearchContext()).thenReturn(mockedSearchContext);
        Locator locator = new Locator(SEARCH, XPATH_INT);
        String systemDescription = String.format(PATTERN_ELEMENTS, INT_ARG, locator);
        when(searchActions.findElements(mockedSearchContext, locator)).thenReturn(webElements);
        mockAssertingWebElements(webElements);
        baseValidations.assertIfExactNumberOfElementsFound(BUSINESS_DESCRIPTION, locator, INT_ARG);
        verify(softAssert).assertThat(eq(BUSINESS_DESCRIPTION), eq(systemDescription),
                eq(webElements), argThat(e -> EQUAL_TO_MATCHER.equals(e.toString())));
    }

    @Test
    void testAssertIfExactNumberOfElementsFoundNullList()
    {
        when(uiContext.getSearchContext()).thenReturn(mockedSearchContext);
        Locator locator = new Locator(SEARCH, XPATH_INT);
        when(searchActions.findElements(mockedSearchContext, locator)).thenReturn(webElements);
        String systemDescription = String.format(PATTERN_ELEMENTS, INT_ARG, locator);
        mockAssertingWebElements(webElements);
        assertFalse(
                baseValidations.assertIfExactNumberOfElementsFound(BUSINESS_DESCRIPTION, locator, INT_ARG));
        verify(softAssert).assertThat(eq(BUSINESS_DESCRIPTION), eq(systemDescription),
                eq(webElements), argThat(e -> EQUAL_TO_MATCHER.equals(e.toString())));
    }

    @Test
    void testAssertIfElementsExist()
    {
        webElements = List.of(mockedWebElement);
        when(uiContext.getSearchContext()).thenReturn(mockedSearchContext);
        Locator locator = new Locator(SEARCH, XPATH_INT);
        when(searchActions.findElements(mockedSearchContext, locator)).thenReturn(webElements);
        baseValidations.assertIfElementsExist(BUSINESS_DESCRIPTION, locator);
        verify(softAssert).assertThat(eq(BUSINESS_DESCRIPTION),
                eq("There is at least one element with attributes ' Search: './/xpath=1'; Visibility: VISIBLE;'"),
                eq(webElements), argThat(e -> "number of elements is a value greater than <0>".equals(e.toString())));
        assertEquals(1, webElements.size());
    }

    @Test
    void testAssertIfAtLeastNumberOfElementsExistSuccess()
    {
        mockAssertingWebElements(webElements);
        List<WebElement> result = testAssertIfAtLeastNumberOfElementsExist(true);
        assertEquals(webElements, result);
    }

    @Test
    void testAssertIfAtLeastNumberOfElementsExistFailed()
    {
        mockAssertingWebElements(webElements);
        List<WebElement> result = testAssertIfAtLeastNumberOfElementsExist(false);
        assertTrue(result.isEmpty());
    }

    private List<WebElement> testAssertIfAtLeastNumberOfElementsExist(boolean assertionResult)
    {
        when(uiContext.getSearchContext()).thenReturn(mockedSearchContext);
        Locator locator = new Locator(SEARCH, XPATH_INT);
        when(searchActions.findElements(mockedSearchContext, locator)).thenReturn(webElements);
        int leastCount = 1;
        String systemDescription = String.format("There are at least %d elements with attributes '%s'", leastCount,
                locator);
        when(softAssert.assertThat(eq(BUSINESS_DESCRIPTION), eq(systemDescription), eq(webElements),
                argThat(e -> "number of elements is a value equal to or greater than <1>".equals(e.toString()))))
                        .thenReturn(assertionResult);
        return baseValidations.assertIfAtLeastNumberOfElementsExist(BUSINESS_DESCRIPTION, locator, leastCount);
    }

    @SuppressWarnings("unchecked")
    @Test
    void testAssertIfElementExists()
    {
        webElements = List.of(mockedWebElement);
        baseValidations.assertIfElementExists(SOME_ELEMENT, webElements);
        verify(softAssert).assertThat(eq(SOME_ELEMENT), eq("Exactly one element within Search context"),
                eq(webElements), any(Matcher.class));
    }

    @Test
    void testAssertIfAtLeastOneElementExistsNull()
    {
        spy = Mockito.spy(baseValidations);
        when(uiContext.getSearchContext()).thenReturn(mockedSearchContext);
        Locator locator = new Locator(SEARCH, XPATH_INT);
        WebElement element = spy.assertIfAtLeastOneElementExists(BUSINESS_DESCRIPTION, locator);
        verify(spy).assertIfElementsExist(BUSINESS_DESCRIPTION, mockedSearchContext, locator);
        assertNull(element);
    }

    @Test
    void testAssertIfAtLeastOneElementExists()
    {
        spy = Mockito.spy(baseValidations);
        webElements = List.of(mockedWebElement);
        when(uiContext.getSearchContext()).thenReturn(mockedSearchContext);
        Locator locator = new Locator(SEARCH, XPATH_INT);
        doReturn(webElements).when(spy).assertIfElementsExist(BUSINESS_DESCRIPTION, mockedSearchContext,
                locator);
        WebElement element = spy.assertIfAtLeastOneElementExists(BUSINESS_DESCRIPTION, locator);
        assertNotNull(element);
    }

    @Test
    void testAssertIfZeroOrOneElementFound()
    {
        webElements = List.of(mockedWebElement);
        when(uiContext.getSearchContext()).thenReturn(mockedSearchContext);
        Locator attributes = new Locator(SEARCH, XPATH_INT);
        when(searchActions.findElements(mockedSearchContext, attributes)).thenReturn(webElements);
        when(softAssert.assertThat(eq(BUSINESS_DESCRIPTION),
                eq(SYSTEM_DESCRIPTION), eq(webElements), argThat(matcher -> matcher instanceof ExistsMatcher)))
                .thenReturn(true);
        Optional<WebElement> element = baseValidations.assertIfZeroOrOneElementFound(BUSINESS_DESCRIPTION, attributes);
        verify(softAssert).assertThat(eq(BUSINESS_DESCRIPTION),
                eq(SYSTEM_DESCRIPTION), eq(webElements), argThat(matcher ->
                matcher instanceof ExistsMatcher));
        assertEquals(Optional.of(mockedWebElement), element);
    }

    @Test
    void testAssertIfZeroOrOneElementFoundZero()
    {
        Locator attributes = new Locator(SEARCH, XPATH_INT);
        Optional<WebElement> element = baseValidations.assertIfZeroOrOneElementFound(BUSINESS_DESCRIPTION, attributes);
        verify(softAssert).recordPassedAssertion(SYSTEM_DESCRIPTION + " not found");
        assertEquals(Optional.empty(), element);
    }

    @Test
    void testAssertIfNumberOfElementsFoundWithoutContext()
    {
        when(uiContext.getSearchContext()).thenReturn(mockedSearchContext);
        testAssertIfNumberOfElementsFound(attributes -> baseValidations
                .assertIfNumberOfElementsFound(BUSINESS_DESCRIPTION, attributes, 1, ComparisonRule.EQUAL_TO), true,
                List.of(mockedWebElement));
    }

    @Test
    void testAssertIfNumberOfElementsFoundWithContext()
    {
        testAssertIfNumberOfElementsFound(
            attributes -> baseValidations.assertIfNumberOfElementsFound(BUSINESS_DESCRIPTION, mockedSearchContext,
                    attributes, 1, ComparisonRule.EQUAL_TO), true, List.of(mockedWebElement));
    }

    @Test
    void testAssertIfNumberOfElementsFoundWithoutContextNotDesiredQuantity()
    {
        when(uiContext.getSearchContext()).thenReturn(mockedSearchContext);
        testAssertIfNumberOfElementsFound(attributes -> baseValidations
                .assertIfNumberOfElementsFound(BUSINESS_DESCRIPTION, attributes, 1, ComparisonRule.EQUAL_TO), false,
                List.of());
    }

    @Test
    void testAssertIfNumberOfElementsFoundWithContextNotDesiredQuantity()
    {
        testAssertIfNumberOfElementsFound(
            attributes -> baseValidations.assertIfNumberOfElementsFound(BUSINESS_DESCRIPTION, mockedSearchContext,
                    attributes, 1, ComparisonRule.EQUAL_TO), false, List.of());
    }

    @Test
    void testAssertElementExists()
    {
        webElements = List.of(mockedWebElement);
        when(uiContext.getSearchContext()).thenReturn(mockedSearchContext);
        Locator locator = new Locator(SEARCH, XPATH_INT);
        when(searchActions.findElements(mockedSearchContext, locator)).thenReturn(webElements);
        when(softAssert.recordAssertion(true, BUSINESS_DESCRIPTION + " is found by the locator " + locator))
                .thenReturn(true);
        Optional<WebElement> element = baseValidations.assertElementExists(BUSINESS_DESCRIPTION, locator);
        assertTrue(element.isPresent());
        assertEquals(mockedWebElement, element.get());
    }

    @Test
    void testAssertElementExistsMoreThanOne()
    {
        webElements = List.of(mockedWebElement, mockedWebElement);
        when(uiContext.getSearchContext()).thenReturn(mockedSearchContext);
        Locator locator = new Locator(SEARCH, XPATH_INT);
        when(searchActions.findElements(mockedSearchContext, locator)).thenReturn(webElements);
        Optional<WebElement> element = baseValidations.assertElementExists(BUSINESS_DESCRIPTION, locator);
        assertTrue(element.isEmpty());
        verify(softAssert).recordFailedAssertion(
                "The number of elements found by the locator " + locator + " is 2, but expected 1");
    }

    @Test
    void testAssertElementExistsEmptyElements()
    {
        List<WebElement> elements = List.of();
        when(uiContext.getSearchContext()).thenReturn(mockedSearchContext);
        Locator locator = new Locator(SEARCH, XPATH_INT);
        when(searchActions.findElements(mockedSearchContext, locator)).thenReturn(elements);
        when(softAssert.recordAssertion(false,
                BUSINESS_DESCRIPTION + " is not found by the locator " + locator.toString())).thenReturn(false);
        Optional<WebElement> element = baseValidations.assertElementExists(BUSINESS_DESCRIPTION, locator);
        assertTrue(element.isEmpty());
    }

    private void testAssertIfNumberOfElementsFound(Function<Locator, List<WebElement>> actualCall,
            boolean checkPassed, List<WebElement> foundElements)
    {
        Locator attributes = new Locator(SEARCH, XPATH_INT);
        when(searchActions.findElements(mockedSearchContext, attributes)).thenReturn(foundElements);
        when(softAssert.assertThat(eq(BUSINESS_DESCRIPTION),
                eq("Number of elements found by ' Search: './/xpath=1'; Visibility: VISIBLE;' is equal to 1"),
                eq(foundElements), argThat(m -> EQUAL_TO_MATCHER.equals(m.toString())))).thenReturn(checkPassed);
        assertEquals(foundElements, actualCall.apply(attributes));
    }

    private void mockAssertingWebElements(List<WebElement> elements)
    {
        doAnswer(a ->
        {
            BooleanSupplier supplier = a.getArgument(1, BooleanSupplier.class);
            return supplier.getAsBoolean();
        }).when(uiContext).withAssertingWebElements(eq(elements), any());
    }
}
