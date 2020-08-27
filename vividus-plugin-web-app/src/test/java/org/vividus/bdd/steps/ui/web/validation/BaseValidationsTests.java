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

package org.vividus.bdd.steps.ui.web.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

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
import org.vividus.ui.validation.matcher.ExistsMatcher;
import org.vividus.ui.validation.matcher.ExpectedConditionsMatcher;
import org.vividus.ui.validation.matcher.NotExistsMatcher;
import org.vividus.ui.web.State;
import org.vividus.ui.web.action.ISearchActions;
import org.vividus.ui.web.action.search.ActionAttributeType;
import org.vividus.ui.web.action.search.SearchAttributes;
import org.vividus.ui.web.action.search.SearchParameters;
import org.vividus.ui.web.context.IWebUiContext;

@ExtendWith(MockitoExtension.class)
class BaseValidationsTests
{
    private static final String SOME_ELEMENT = "Some element";
    private static final String PATTERN_ELEMENTS = "Number %1$s of elements found by '%2$s'";
    private static final String BUSINESS_DESCRIPTION = "Test business description";
    private static final String SYSTEM_DESCRIPTION =
            "An element with attributes: ' XPath: './/xpath=1'; Visibility: VISIBLE;'";
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
    private IWebUiContext webUiContext;

    @Mock
    private ISearchActions searchActions;

    @Mock
    private IDescriptiveSoftAssert softAssert;

    @InjectMocks
    private BaseValidations baseValidations;

    private BaseValidations spy;

    @SuppressWarnings("unchecked")
    @Test
    void testAssertIfElementExistsWithSearchAttributes()
    {
        webElements = List.of(mockedWebElement);
        when(webUiContext.getSearchContext()).thenReturn(mockedSearchContext);
        SearchAttributes attributes = mock(SearchAttributes.class);
        when(searchActions.findElements(eq(mockedSearchContext), eq(attributes))).thenReturn(webElements);
        when(attributes.toString()).thenReturn("attributes");
        when(softAssert.assertThat(eq(SOME_ELEMENT), anyString(), eq(webElements),
                any(Matcher.class))).thenReturn(true);
        WebElement foundElement = baseValidations.assertIfElementExists(SOME_ELEMENT, attributes);
        assertEquals(mockedWebElement, foundElement);
    }

    @Test
    void testAssertIfElementDoesNotExistWithSearchAttributes()
    {
        spy = Mockito.spy(baseValidations);
        when(webUiContext.getSearchContext()).thenReturn(mockedSearchContext);
        SearchParameters searchParameters = new SearchParameters();
        SearchAttributes attributes = new SearchAttributes(ActionAttributeType.LINK_TEXT, searchParameters);
        when(searchActions.findElements(mockedSearchContext, attributes)).thenReturn(List.of());
        assertTrue(spy.assertIfElementDoesNotExist(BUSINESS_DESCRIPTION, attributes));
        assertFalse(searchParameters.isWaitForElement());
    }

    @Test
    void testAssertIfElementDoesNotExistWhenNoFailedAssertionRecordingIsNeeded()
    {
        spy = Mockito.spy(baseValidations);
        webElements = List.of(mockedWebElement);
        when(webUiContext.getSearchContext()).thenReturn(mockedSearchContext);
        SearchParameters searchParameters = new SearchParameters();
        SearchAttributes attributes = new SearchAttributes(ActionAttributeType.LINK_TEXT, searchParameters);
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
        when(webUiContext.getSearchContext()).thenReturn(mockedSearchContext);
        SearchParameters searchParameters = new SearchParameters();
        SearchAttributes attributes = new SearchAttributes(ActionAttributeType.LINK_TEXT, searchParameters);
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
        when(webUiContext.getSearchContext()).thenReturn(mockedSearchContext);
        SearchParameters searchParameters = new SearchParameters();
        SearchAttributes attributes = new SearchAttributes(ActionAttributeType.LINK_TEXT, searchParameters);
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
        when(webUiContext.getSearchContext()).thenReturn(mockedSearchContext);
        SearchAttributes searchAttributes = new SearchAttributes(ActionAttributeType.XPATH, XPATH_INT);
        String systemDescription = String.format(PATTERN_ELEMENTS, INT_ARG, searchAttributes);
        when(searchActions.findElements(mockedSearchContext, searchAttributes)).thenReturn(webElements);
        mockAssertingWebElements(webElements);
        baseValidations.assertIfExactNumberOfElementsFound(BUSINESS_DESCRIPTION, searchAttributes, INT_ARG);
        verify(softAssert).assertThat(eq(BUSINESS_DESCRIPTION), eq(systemDescription),
                eq(webElements), argThat(e -> EQUAL_TO_MATCHER.equals(e.toString())));
    }

    @Test
    void testAssertIfExactNumberOfElementsFoundNullList()
    {
        when(webUiContext.getSearchContext()).thenReturn(mockedSearchContext);
        SearchAttributes searchAttributes = new SearchAttributes(ActionAttributeType.XPATH, XPATH_INT);
        when(searchActions.findElements(mockedSearchContext, searchAttributes)).thenReturn(webElements);
        String systemDescription = String.format(PATTERN_ELEMENTS, INT_ARG, searchAttributes);
        mockAssertingWebElements(webElements);
        assertFalse(
                baseValidations.assertIfExactNumberOfElementsFound(BUSINESS_DESCRIPTION, searchAttributes, INT_ARG));
        verify(softAssert).assertThat(eq(BUSINESS_DESCRIPTION), eq(systemDescription),
                eq(webElements), argThat(e -> EQUAL_TO_MATCHER.equals(e.toString())));
    }

    @Test
    void testAssertIfElementsExist()
    {
        webElements = List.of(mockedWebElement);
        when(webUiContext.getSearchContext()).thenReturn(mockedSearchContext);
        SearchAttributes searchAttributes = new SearchAttributes(ActionAttributeType.XPATH, XPATH_INT);
        when(searchActions.findElements(mockedSearchContext, searchAttributes)).thenReturn(webElements);
        baseValidations.assertIfElementsExist(BUSINESS_DESCRIPTION, searchAttributes);
        verify(softAssert).assertThat(eq(BUSINESS_DESCRIPTION),
                eq("There is at least one element with attributes ' XPath: './/xpath=1'; Visibility: VISIBLE;'"),
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
        when(webUiContext.getSearchContext()).thenReturn(mockedSearchContext);
        SearchAttributes searchAttributes = new SearchAttributes(ActionAttributeType.XPATH, XPATH_INT);
        when(searchActions.findElements(mockedSearchContext, searchAttributes)).thenReturn(webElements);
        int leastCount = 1;
        String systemDescription = String.format("There are at least %d elements with attributes '%s'", leastCount,
                searchAttributes);
        when(softAssert.assertThat(eq(BUSINESS_DESCRIPTION), eq(systemDescription), eq(webElements),
                argThat(e -> "number of elements is a value equal to or greater than <1>".equals(e.toString()))))
                        .thenReturn(assertionResult);
        return baseValidations.assertIfAtLeastNumberOfElementsExist(BUSINESS_DESCRIPTION, searchAttributes, leastCount);
    }

    @SuppressWarnings("unchecked")
    @Test
    void testAssertPageWithURLPartIsLoaded()
    {
        when(mockedWebDriverProvider.get()).thenReturn(mockedWebDriver);
        String urlValue = "http://example.com/";
        when(mockedWebDriver.getCurrentUrl()).thenReturn(urlValue);
        baseValidations.assertPageWithURLPartIsLoaded(urlValue);
        verify(softAssert).assertThat(eq("Page with the URLpart '" + urlValue + "' is loaded"),
                eq("Page url '" + urlValue + "' contains part '" + urlValue + "'"), eq(urlValue),
                (Matcher<String>) isA(Matcher.class));
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
        when(webUiContext.getSearchContext()).thenReturn(mockedSearchContext);
        SearchAttributes searchAttributes = new SearchAttributes(ActionAttributeType.XPATH, XPATH_INT);
        WebElement element = spy.assertIfAtLeastOneElementExists(BUSINESS_DESCRIPTION, searchAttributes);
        verify(spy).assertIfElementsExist(BUSINESS_DESCRIPTION, mockedSearchContext, searchAttributes);
        assertNull(element);
    }

    @Test
    void testAssertIfAtLeastOneElementExists()
    {
        spy = Mockito.spy(baseValidations);
        webElements = List.of(mockedWebElement);
        when(webUiContext.getSearchContext()).thenReturn(mockedSearchContext);
        SearchAttributes searchAttributes = new SearchAttributes(ActionAttributeType.XPATH, XPATH_INT);
        doReturn(webElements).when(spy).assertIfElementsExist(BUSINESS_DESCRIPTION, mockedSearchContext,
                searchAttributes);
        WebElement element = spy.assertIfAtLeastOneElementExists(BUSINESS_DESCRIPTION, searchAttributes);
        assertNotNull(element);
    }

    @Test
    void testAssertIfZeroOrOneElementFound()
    {
        webElements = List.of(mockedWebElement);
        when(webUiContext.getSearchContext()).thenReturn(mockedSearchContext);
        SearchAttributes attributes = new SearchAttributes(ActionAttributeType.XPATH, XPATH_INT);
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
        SearchAttributes attributes = new SearchAttributes(ActionAttributeType.XPATH, XPATH_INT);
        Optional<WebElement> element = baseValidations.assertIfZeroOrOneElementFound(BUSINESS_DESCRIPTION, attributes);
        verify(softAssert).recordPassedAssertion(SYSTEM_DESCRIPTION + " not found");
        assertEquals(Optional.empty(), element);
    }

    @Test
    void testAssertIfNumberOfElementsFoundWithoutContext()
    {
        when(webUiContext.getSearchContext()).thenReturn(mockedSearchContext);
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
        when(webUiContext.getSearchContext()).thenReturn(mockedSearchContext);
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

    private void testAssertIfNumberOfElementsFound(Function<SearchAttributes, List<WebElement>> actualCall,
            boolean checkPassed, List<WebElement> foundElements)
    {
        SearchAttributes attributes = new SearchAttributes(ActionAttributeType.XPATH, XPATH_INT);
        when(searchActions.findElements(mockedSearchContext, attributes)).thenReturn(foundElements);
        when(softAssert.assertThat(eq(BUSINESS_DESCRIPTION),
                eq("Number of elements found by ' XPath: './/xpath=1'; Visibility: VISIBLE;' is equal to 1"),
                eq(foundElements), argThat(m -> EQUAL_TO_MATCHER.equals(m.toString())))).thenReturn(checkPassed);
        assertEquals(foundElements, actualCall.apply(attributes));
    }

    private void mockAssertingWebElements(List<WebElement> elements)
    {
        doAnswer(a ->
        {
            BooleanSupplier supplier = a.getArgument(1, BooleanSupplier.class);
            return supplier.getAsBoolean();
        }).when(webUiContext).withAssertingWebElements(eq(elements), any());
    }
}
