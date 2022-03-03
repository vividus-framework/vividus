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

package org.vividus.steps.ui;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.vividus.softassert.ISoftAssert;
import org.vividus.steps.ComparisonRule;
import org.vividus.steps.SubSteps;
import org.vividus.ui.action.ISearchActions;
import org.vividus.ui.action.search.Locator;
import org.vividus.ui.context.IUiContext;
import org.vividus.ui.context.SearchContextSetter;

@ExtendWith(MockitoExtension.class)
class GenericNestedStepsTests
{
    private static final String ELEMENTS_NUMBER = "Elements number";

    @Mock private IUiContext uiContext;
    @Mock private SubSteps subSteps;
    @Mock private ISearchActions searchActions;
    @Mock private ISoftAssert softAssert;
    @InjectMocks private GenericNestedSteps nestedSteps;

    @Test
    void shouldExecuteStepsAndExitWhenQuantityChangedAndIterationLimitNotReached()
    {
        SearchContext searchContext = mockUiContext();
        Locator locator = mock(Locator.class);
        List<WebElement> elements = List.of(mock(WebElement.class));
        when(searchActions.findElements(searchContext, locator)).thenReturn(elements).thenReturn(elements)
            .thenReturn(List.of());
        when(softAssert.assertThat(eq(ELEMENTS_NUMBER), eq(1), argThat(m ->
            ComparisonRule.EQUAL_TO.getComparisonRule(1).toString().equals(m.toString())))).thenReturn(true);
        SearchContextSetter searchContextSetter = mockSearchContextSetter();

        nestedSteps.performAllStepsWhileElementsExist(ComparisonRule.EQUAL_TO, 1, locator, 5, subSteps);

        verify(subSteps, times(2)).execute(Optional.empty());
        verify(searchContextSetter, times(2)).setSearchContext();
        verify(searchActions, times(3)).findElements(searchContext, locator);
        verify(softAssert).assertThat(eq(ELEMENTS_NUMBER), eq(1),
                argThat(m -> ComparisonRule.EQUAL_TO.getComparisonRule(1).toString().equals(m.toString())));
    }

    @Test
    void shouldExecuteStepsAndExitAndRecordFailedAssertionWhenIterationLimitReached()
    {
        SearchContext searchContext = mockUiContext();
        Locator locator = mock(Locator.class);
        List<WebElement> elements = List.of(mock(WebElement.class));
        when(searchActions.findElements(searchContext, locator)).thenReturn(elements);
        when(softAssert.assertThat(eq(ELEMENTS_NUMBER), eq(1), argThat(m ->
            ComparisonRule.EQUAL_TO.getComparisonRule(1).toString().equals(m.toString())))).thenReturn(true);
        SearchContextSetter searchContextSetter = mockSearchContextSetter();

        nestedSteps.performAllStepsWhileElementsExist(ComparisonRule.EQUAL_TO, 1, locator, 2, subSteps);

        verify(subSteps, times(2)).execute(Optional.empty());
        verify(searchContextSetter, times(2)).setSearchContext();
        verify(searchActions, times(2)).findElements(searchContext, locator);
        verify(softAssert).recordFailedAssertion("Elements number a value equal to <1>"
                + " was not changed after 2 iteration(s)");
    }

    @Test
    void shouldDoNothingForNegativeIterationsLimit()
    {
        Locator locator = mock(Locator.class);

        nestedSteps.performAllStepsWhileElementsExist(ComparisonRule.EQUAL_TO, 1, locator, -1, subSteps);

        verifyNoInteractions(uiContext, softAssert, subSteps, searchActions);
    }

    @Test
    void shouldNotExecuteStepsIfInitialElementsNumberIsNotValid()
    {
        SearchContext searchContext = mockUiContext();
        Locator locator = mock(Locator.class);
        when(searchActions.findElements(searchContext, locator)).thenReturn(List.of(mock(WebElement.class)));

        nestedSteps.performAllStepsWhileElementsExist(ComparisonRule.EQUAL_TO, 2, locator, 5, subSteps);

        verifyNoInteractions(subSteps);
        verify(searchActions, times(1)).findElements(searchContext, locator);
        verify(softAssert).assertThat(eq(ELEMENTS_NUMBER), eq(1), argThat(m ->
            ComparisonRule.EQUAL_TO.getComparisonRule(2).toString().equals(m.toString())));
        verify(softAssert, never()).recordFailedAssertion(anyString());
        verify(uiContext, never()).getSearchContextSetter();
    }

    @Test
    void shouldNotExecuteStepsIfNoElementsFoundButConditionMatches()
    {
        SearchContext searchContext = mockUiContext();
        Locator locator = mock(Locator.class);
        when(searchActions.findElements(searchContext, locator)).thenReturn(List.of());

        nestedSteps.performAllStepsWhileElementsExist(ComparisonRule.GREATER_THAN_OR_EQUAL_TO, 1, locator, 5, subSteps);

        verifyNoInteractions(subSteps);
        verify(searchActions, times(1)).findElements(searchContext, locator);
        verify(softAssert).assertThat(eq(ELEMENTS_NUMBER), eq(0), argThat(m ->
            ComparisonRule.GREATER_THAN_OR_EQUAL_TO.getComparisonRule(1).toString().equals(m.toString())));
        verify(softAssert, never()).recordFailedAssertion(anyString());
        verify(uiContext, never()).getSearchContextSetter();
    }

    private SearchContext mockUiContext()
    {
        SearchContext searchContext = mock(SearchContext.class);
        when(uiContext.getSearchContext()).thenReturn(searchContext);
        return searchContext;
    }

    private SearchContextSetter mockSearchContextSetter()
    {
        SearchContextSetter searchContextSetter = mock(SearchContextSetter.class);
        when(uiContext.getSearchContextSetter()).thenReturn(searchContextSetter);
        return searchContextSetter;
    }
}
