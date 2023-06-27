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

package org.vividus.steps.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.Point;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.vividus.softassert.ISoftAssert;
import org.vividus.steps.ComparisonRule;
import org.vividus.steps.ui.validation.IBaseValidations;
import org.vividus.testdouble.TestLocatorType;
import org.vividus.ui.State;
import org.vividus.ui.action.IExpectedConditions;
import org.vividus.ui.action.IExpectedSearchContextCondition;
import org.vividus.ui.action.ISearchActions;
import org.vividus.ui.action.IWaitActions;
import org.vividus.ui.action.WaitResult;
import org.vividus.ui.action.search.Locator;
import org.vividus.ui.action.search.SearchParameters;
import org.vividus.ui.action.search.Visibility;
import org.vividus.ui.context.IUiContext;

@ExtendWith(MockitoExtension.class)
class GenericWaitStepsTests
{
    private static final String VALUE = "value";
    private static final Locator LOCATOR = new Locator(TestLocatorType.SEARCH, VALUE);
    private static final String ELEMENT_TO_VALIDATE_EXISTENCE = "The element to validate existence";
    private static final String ELEMENT_TO_VALIDATE_STOP_MOVEMENT = "The element to validate stop of its movement";

    @Mock private IWaitActions waitActions;
    @Mock private IUiContext uiContext;
    @Mock private IExpectedConditions<Locator> expectedSearchActionsConditions;
    @Mock private ISearchActions searchActions;
    @Mock private ISoftAssert softAssert;
    @Mock private IBaseValidations baseValidations;
    @InjectMocks private GenericWaitSteps waitSteps;

    @AfterEach
    void verifyMocks()
    {
        verifyNoMoreInteractions(waitActions, expectedSearchActionsConditions, searchActions, baseValidations,
                uiContext, softAssert);
    }

    @SuppressWarnings("unchecked")
    @Test
    void testWaitTillElementAppears()
    {
        SearchContext searchContext = mock(SearchContext.class);
        when(uiContext.getSearchContext()).thenReturn(searchContext);
        IExpectedSearchContextCondition<WebElement> condition = mock(IExpectedSearchContextCondition.class);
        when(expectedSearchActionsConditions.visibilityOfElement(LOCATOR)).thenReturn(condition);
        waitSteps.waitForElementAppearance(LOCATOR);
        verify(waitActions).wait(searchContext, condition);
    }

    @Test
    void shouldThrowAnExceptionInCaseOfIncorrectVisibilityUsedForAppearanceWait()
    {
        Locator locator = new Locator(TestLocatorType.SEARCH, new SearchParameters(VALUE, Visibility.ALL));
        var iae = assertThrows(IllegalArgumentException.class, () -> waitSteps.waitForElementAppearance(locator));
        assertEquals("The step supports locators with VISIBLE visibility settings only, but the locator is "
                + "`search 'value' (visible or invisible)`", iae.getMessage());
        verifyNoInteractions(expectedSearchActionsConditions, waitActions);
    }

    @Test
    void shouldThrowAnExceptionInCaseOfIncorrectVisibilityUsedForDisappearanceWait()
    {
        Locator locator = new Locator(TestLocatorType.SEARCH, new SearchParameters(VALUE, Visibility.INVISIBLE));
        var iae = assertThrows(IllegalArgumentException.class, () -> waitSteps.waitForElementAppearance(locator));
        assertEquals("The step supports locators with VISIBLE visibility settings only, but the locator is `search"
                + " 'value' (invisible)`", iae.getMessage());
        verifyNoInteractions(expectedSearchActionsConditions, waitActions);
    }

    @SuppressWarnings("unchecked")
    @Test
    void testWaitTillElementDisappears()
    {
        SearchContext searchContext = mock(SearchContext.class);
        when(uiContext.getSearchContext()).thenReturn(searchContext);
        IExpectedSearchContextCondition<Boolean> condition = mock(IExpectedSearchContextCondition.class);
        when(expectedSearchActionsConditions.invisibilityOfElement(LOCATOR)).thenReturn(condition);
        waitSteps.waitForElementDisappearance(LOCATOR);
        verify(waitActions).wait(searchContext, condition);
    }

    @Test
    void shouldWaitDurationWithPollingDurationTillElementDisappears()
    {
        var duration = Duration.ofSeconds(30);
        var pollingDuration = Duration.ofSeconds(10);
        var searchContext = mock(SearchContext.class);
        when(uiContext.getSearchContext()).thenReturn(searchContext);
        var waitResult = new WaitResult<Boolean>();
        waitResult.setWaitPassed(true);
        var condition = mock(IExpectedSearchContextCondition.class);
        when(waitActions.wait(searchContext, duration, pollingDuration, condition)).thenReturn(waitResult);
        when(expectedSearchActionsConditions.invisibilityOfElement(LOCATOR)).thenReturn(condition);
        assertTrue(waitSteps.waitDurationWithPollingDurationTillElementState(duration, pollingDuration, LOCATOR,
                State.NOT_VISIBLE));
    }

    @CsvSource({
        "1408   , 1 seconds 408 millis,             true",
        "2000   , 2 seconds,                        true",
        "73     , 73 millis,                        true",
        "3034159, 50 minutes 34 seconds 159 millis, false"
    })
    @ParameterizedTest
    void shouldCheckThatElementByLocatorExistsForDuration(long millis, String assertDuration, boolean passed)
    {
        var waitResult = new WaitResult<Boolean>();
        waitResult.setWaitPassed(!passed);
        var duration = Duration.ofMillis(millis);

        var searchContext = mock(SearchContext.class);
        when(uiContext.getOptionalSearchContext()).thenReturn(Optional.of(searchContext));

        var locator = new Locator(TestLocatorType.SEARCH, VALUE);
        WebElement webElement = mock(WebElement.class);
        when(baseValidations.assertElementExists(ELEMENT_TO_VALIDATE_EXISTENCE, searchContext, locator)).thenReturn(
                Optional.of(webElement));

        when(searchActions.findElements(searchContext, locator)).thenReturn(List.of());
        when(waitActions.wait(eq(searchContext), eq(duration),
                argThat((ArgumentMatcher<Function<SearchContext, Boolean>>) function -> {
                    function.apply(searchContext);
                    return true;
                }), eq(false))).thenReturn(waitResult);

        waitSteps.doesElementByLocatorExistsForDuration(locator, duration);

        assertFalse(locator.getSearchParameters().isWaitForElement());
        verify(softAssert).assertTrue(
                "Element located by locator search 'value' (visible) has existed for " + assertDuration, passed);
    }

    @Test
    void shouldNotCheckThatElementByLocatorExistsForDurationIfSearchContextIsNotSet()
    {
        when(uiContext.getOptionalSearchContext()).thenReturn(Optional.empty());
        waitSteps.doesElementByLocatorExistsForDuration(LOCATOR, Duration.ofSeconds(1));
        verifyNoInteractions(baseValidations, waitActions, searchActions, softAssert);
    }

    @Test
    void shouldNotCheckThatElementByLocatorExistsForDurationIfElementIsNotFound()
    {
        var searchContext = mock(SearchContext.class);
        when(uiContext.getOptionalSearchContext()).thenReturn(Optional.of(searchContext));

        var locator = new Locator(TestLocatorType.SEARCH, VALUE);
        when(baseValidations.assertElementExists(ELEMENT_TO_VALIDATE_EXISTENCE, searchContext, locator)).thenReturn(
                Optional.empty());

        waitSteps.doesElementByLocatorExistsForDuration(LOCATOR, Duration.ofSeconds(1));

        assertTrue(locator.getSearchParameters().isWaitForElement());
        verifyNoMoreInteractions(waitActions, searchActions, softAssert);
    }

    @Test
    void shouldThrownAnErrorIfDurationInNanos()
    {
        var nanos = Duration.ofNanos(1);
        var exception = assertThrows(IllegalArgumentException.class,
            () -> waitSteps.doesElementByLocatorExistsForDuration(LOCATOR, nanos));
        assertEquals("Unable to convert duration PT0.000000001S", exception.getMessage());
    }

    @Test
    void shouldNotWaitIfContextIsNotSet()
    {
        when(uiContext.getOptionalSearchContext()).thenReturn(Optional.empty());
        waitSteps.waitForElementNumber(null, null, 0);
        verifyNoInteractions(waitActions, searchActions);
    }

    @Test
    void shouldWaitForNumberOfElements()
    {
        var searchParameters = new SearchParameters(VALUE, Visibility.VISIBLE, true);
        var searchContext = mockSearchContext();
        var locator = new Locator(TestLocatorType.SEARCH, searchParameters);
        when(searchActions.findElements(searchContext, locator)).thenReturn(List.of(mock(WebElement.class)));
        waitSteps.waitForElementNumber(locator, ComparisonRule.EQUAL_TO, 1);
        assertFalse(searchParameters.isWaitForElement());
        verify(waitActions).wait(eq(searchContext), argThat(f -> {
            assertEquals("number of elements located by \"search 'value' (visible)\" is equal to 1", f.toString());
            assertTrue((boolean) f.apply(searchContext));
            return true;
        }));
    }

    @Test
    void testWaitTillElementStopsMoving()
    {
        var searchParameters = new SearchParameters(VALUE, Visibility.VISIBLE, true);
        var locator = new Locator(TestLocatorType.SEARCH, searchParameters);
        var searchContext = mockSearchContext();
        var webElement = mock(WebElement.class);
        when(baseValidations.assertElementExists(ELEMENT_TO_VALIDATE_STOP_MOVEMENT, searchContext, locator)).thenReturn(
                Optional.of(webElement));
        Point point = new Point(10, 10);
        when(webElement.getLocation()).thenReturn(new Point(0, 0))
                                      .thenReturn(point)
                                      .thenReturn(point);
        waitSteps.waitForElementStopsMoving(locator);
        assertFalse(searchParameters.isWaitForElement());
        verify(waitActions).wait(eq(searchContext), argThat(function -> {
            function.apply(searchContext);
            function.apply(searchContext);
            assertTrue((boolean) function.apply(searchContext));
            return true;
        }));
    }

    @Test
    void testWaitTillElementStopsMovingElementIsNotPresent()
    {
        var searchParameters = new SearchParameters(VALUE, Visibility.VISIBLE, true);
        var searchContext = mockSearchContext();
        var locator = new Locator(TestLocatorType.SEARCH, searchParameters);
        when(baseValidations.assertElementExists(ELEMENT_TO_VALIDATE_STOP_MOVEMENT, searchContext, locator))
                .thenReturn(Optional.empty());
        waitSteps.waitForElementStopsMoving(locator);
    }

    @SuppressWarnings("unchecked")
    @Test
    void testWaitTillElementStopsMovingThrowsStaleElementReferenceException()
    {
        var searchParameters = new SearchParameters(VALUE, Visibility.VISIBLE, true);
        var searchContext = mockSearchContext();
        var locator = new Locator(TestLocatorType.SEARCH, searchParameters);
        var webElement = mock(WebElement.class);
        when(baseValidations.assertElementExists(ELEMENT_TO_VALIDATE_STOP_MOVEMENT, searchContext, locator))
                .thenReturn(Optional.of(webElement));
        when(webElement.getLocation()).thenThrow(StaleElementReferenceException.class);
        when(searchActions.findElements(searchContext, locator)).thenReturn(List.of());
        waitSteps.waitForElementStopsMoving(locator);
        var conditionCaptor = ArgumentCaptor.forClass(IExpectedSearchContextCondition.class);
        verify(waitActions).wait(eq(searchContext), conditionCaptor.capture());
        assertFalse(searchParameters.isWaitForElement());
        var capturedCondition = conditionCaptor.getValue();
        assertEquals("element located by \"search 'value' (visible)\" stopped moving", capturedCondition.toString());
        assertThrows(StaleElementReferenceException.class, () -> capturedCondition.apply(searchContext));
    }

    @SuppressWarnings("unchecked")
    @Test
    void testWaitTillElementStopsMovingThrowsTwoStaleElementReferenceExceptions()
    {
        var searchParameters = new SearchParameters(VALUE, Visibility.VISIBLE, true);
        var searchContext = mockSearchContext();
        var locator = new Locator(TestLocatorType.SEARCH, searchParameters);
        var webElement = mock(WebElement.class);
        when(baseValidations.assertElementExists(ELEMENT_TO_VALIDATE_STOP_MOVEMENT, searchContext, locator))
                .thenReturn(Optional.of(webElement));
        when(webElement.getLocation()).thenThrow(StaleElementReferenceException.class);
        when(searchActions.findElements(searchContext, locator)).thenReturn(List.of(webElement));
        waitSteps.waitForElementStopsMoving(locator);
        var conditionCaptor = ArgumentCaptor.forClass(IExpectedSearchContextCondition.class);
        verify(waitActions).wait(eq(searchContext), conditionCaptor.capture());
        assertFalse(searchParameters.isWaitForElement());
        var capturedCondition = conditionCaptor.getValue();
        assertFalse((boolean) capturedCondition.apply(searchContext));
        assertThrows(StaleElementReferenceException.class, () -> capturedCondition.apply(searchContext));
    }

    private SearchContext mockSearchContext()
    {
        var searchContext = mock(SearchContext.class);
        when(uiContext.getOptionalSearchContext()).thenReturn(Optional.of(searchContext));
        return searchContext;
    }
}
