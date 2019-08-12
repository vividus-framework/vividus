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

package org.vividus.bdd.steps.ui.web;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.jbehave.core.model.ExamplesTable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.vividus.bdd.steps.ComparisonRule;
import org.vividus.bdd.steps.ISubStepExecutor;
import org.vividus.bdd.steps.ISubStepExecutorFactory;
import org.vividus.bdd.steps.ui.web.validation.IBaseValidations;
import org.vividus.softassert.ISoftAssert;
import org.vividus.ui.web.action.ICssSelectorFactory;
import org.vividus.ui.web.action.ISearchActions;
import org.vividus.ui.web.action.search.ActionAttributeType;
import org.vividus.ui.web.action.search.SearchAttributes;
import org.vividus.ui.web.context.IWebUiContext;
import org.vividus.ui.web.context.SearchContextSetter;

@ExtendWith(MockitoExtension.class)
class NestedStepsTests
{
    private static final String ELEMENTS_NUMBER = "Elements number";
    private static final String FIRST_XPATH = "//first";
    private static final String SECOND_XPATH = "//second";

    private static final String ELEMENTS_TO_PERFORM_STEPS = "Elements to iterate with steps";

    @Mock
    private IBaseValidations baseValidations;
    @Mock
    private IWebUiContext webUiContext;
    @Mock
    private ISubStepExecutorFactory subStepExecutorFactory;
    @Mock
    private ISubStepExecutor subStepExecutor;
    @Mock
    private ISearchActions searchActions;
    @Mock
    private ISoftAssert softAssert;
    @Mock
    private ICssSelectorFactory cssSelectorFactory;

    @InjectMocks
    private NestedSteps nestedSteps;

    @Test
    void testPerformAllStepsForElementIfFound()
    {
        ExamplesTable stepsAsTable = mock(ExamplesTable.class);
        SearchAttributes searchAttributes = mock(SearchAttributes.class);
        when(subStepExecutorFactory.createSubStepExecutor(stepsAsTable)).thenReturn(subStepExecutor);
        doNothing().when(subStepExecutor).execute(eq(Optional.empty()));
        WebElement first = mock(WebElement.class);
        WebElement second = mock(WebElement.class);
        when(baseValidations.assertIfNumberOfElementsFound(ELEMENTS_TO_PERFORM_STEPS, searchAttributes, 1,
                ComparisonRule.EQUAL_TO)).thenReturn(Arrays.asList(first, second));
        when(cssSelectorFactory.getCssSelectors(List.of(first, second)))
            .thenReturn(List.of(FIRST_XPATH, SECOND_XPATH).stream());
        SearchAttributes secondSearchAttributes = new SearchAttributes(ActionAttributeType.CSS_SELECTOR, SECOND_XPATH);
        when(baseValidations.assertIfElementExists("An element for iteration 2",
                secondSearchAttributes)).thenReturn(second);
        SearchContextSetter searchContextSetter = mockSearchContextSetter();
        nestedSteps.performAllStepsForElementIfFound(ComparisonRule.EQUAL_TO, 1, searchAttributes, stepsAsTable);
        verify(webUiContext).putSearchContext(eq(first), any(SearchContextSetter.class));
        verify(webUiContext).putSearchContext(eq(second), any(SearchContextSetter.class));
        verify(searchContextSetter).setSearchContext();
    }

    @Test
    void testNotPerformStepsIfElementNotFound()
    {
        ExamplesTable stepsAsTable = mock(ExamplesTable.class);
        SearchAttributes searchAttributes = mock(SearchAttributes.class);
        when(baseValidations.assertIfNumberOfElementsFound(ELEMENTS_TO_PERFORM_STEPS, searchAttributes, 0,
                ComparisonRule.GREATER_THAN_OR_EQUAL_TO)).thenReturn(List.of());
        nestedSteps.performAllStepsForElementIfFound(ComparisonRule.GREATER_THAN_OR_EQUAL_TO, 0,
                searchAttributes, stepsAsTable);
        verifyZeroInteractions(cssSelectorFactory);
        verifyZeroInteractions(webUiContext);
        verifyZeroInteractions(subStepExecutor);
    }

    @Test
    void testPerformAllStepsForElementIfFoundExceptionDuringRun()
    {
        ExamplesTable stepsAsTable = mock(ExamplesTable.class);
        SearchAttributes searchAttributes = mock(SearchAttributes.class);
        WebElement element = mock(WebElement.class);
        when(baseValidations.assertIfNumberOfElementsFound(ELEMENTS_TO_PERFORM_STEPS, searchAttributes, 1,
                ComparisonRule.EQUAL_TO)).thenReturn(List.of(element));
        when(cssSelectorFactory.getCssSelectors(List.of(element))).thenReturn(List.of(FIRST_XPATH).stream());
        SearchContextSetter searchContextSetter = mockSearchContextSetter();
        doThrow(new StaleElementReferenceException("stale element")).when(webUiContext).putSearchContext(eq(element),
                any(SearchContextSetter.class));
        assertThrows(StaleElementReferenceException.class, () -> nestedSteps
                .performAllStepsForElementIfFound(ComparisonRule.EQUAL_TO, 1, searchAttributes, stepsAsTable));
        verify(searchContextSetter).setSearchContext();
    }

    @Test
    void shouldExecuteStepsAndExitWhenQuantityChangedAndIterationLimitNotReached()
    {
        SearchContext searchContext = mockWebUiContext();
        ExamplesTable stepsAsTable = mock(ExamplesTable.class);
        SearchAttributes searchAttributes = mock(SearchAttributes.class);
        List<WebElement> elements = List.of(mock(WebElement.class));
        when(subStepExecutorFactory.createSubStepExecutor(stepsAsTable)).thenReturn(subStepExecutor);
        when(searchActions.findElements(searchContext, searchAttributes)).thenReturn(elements).thenReturn(elements)
            .thenReturn(List.of());
        when(softAssert.assertThat(eq(ELEMENTS_NUMBER), eq(1), argThat(m ->
            ComparisonRule.EQUAL_TO.getComparisonRule(1).toString().equals(m.toString())))).thenReturn(true);
        SearchContextSetter searchContextSetter = mockSearchContextSetter();

        nestedSteps.performAllStepsWhileElementsExist(ComparisonRule.EQUAL_TO, 1, searchAttributes, 5, stepsAsTable);

        verify(subStepExecutor, times(2)).execute(Optional.empty());
        verify(searchContextSetter, times(2)).setSearchContext();
        verify(searchActions, times(3)).findElements(searchContext, searchAttributes);
        verify(softAssert).assertThat(eq(ELEMENTS_NUMBER), eq(1),
                argThat(m -> ComparisonRule.EQUAL_TO.getComparisonRule(1).toString().equals(m.toString())));
    }

    private SearchContext mockWebUiContext()
    {
        SearchContext searchContext = mock(SearchContext.class);
        when(webUiContext.getSearchContext()).thenReturn(searchContext);
        return searchContext;
    }

    private SearchContextSetter mockSearchContextSetter()
    {
        SearchContextSetter searchContextSetter = mock(SearchContextSetter.class);
        when(webUiContext.getSearchContextSetter()).thenReturn(searchContextSetter);
        return searchContextSetter;
    }

    @Test
    void shouldExecuteStepsAndExitAndRecordFailedAssertionWhenIterationLimitReached()
    {
        SearchContext searchContext = mockWebUiContext();
        ExamplesTable stepsAsTable = mock(ExamplesTable.class);
        SearchAttributes searchAttributes = mock(SearchAttributes.class);
        List<WebElement> elements = List.of(mock(WebElement.class));
        when(subStepExecutorFactory.createSubStepExecutor(stepsAsTable)).thenReturn(subStepExecutor);
        when(searchActions.findElements(searchContext, searchAttributes)).thenReturn(elements);
        when(softAssert.assertThat(eq(ELEMENTS_NUMBER), eq(1), argThat(m ->
            ComparisonRule.EQUAL_TO.getComparisonRule(1).toString().equals(m.toString())))).thenReturn(true);
        SearchContextSetter searchContextSetter = mockSearchContextSetter();

        nestedSteps.performAllStepsWhileElementsExist(ComparisonRule.EQUAL_TO, 1, searchAttributes, 2, stepsAsTable);

        verify(subStepExecutor, times(2)).execute(Optional.empty());
        verify(searchContextSetter, times(2)).setSearchContext();
        verify(searchActions, times(2)).findElements(searchContext, searchAttributes);
        verify(softAssert).recordFailedAssertion("Elements number a value equal to <1>"
                + " was not changed after 2 iteration(s)");
    }

    @Test
    void shouldDoNothingForNegativeIterationsLimit()
    {
        ExamplesTable stepsAsTable = mock(ExamplesTable.class);
        SearchAttributes searchAttributes = mock(SearchAttributes.class);

        nestedSteps.performAllStepsWhileElementsExist(ComparisonRule.EQUAL_TO, 1, searchAttributes, -1, stepsAsTable);

        verifyZeroInteractions(webUiContext, softAssert, subStepExecutor, searchActions);
    }

    @Test
    void shouldNotExecuteStepsIfInitialElementsNumberIsNotValid()
    {
        SearchContext searchContext = mockWebUiContext();
        ExamplesTable stepsAsTable = mock(ExamplesTable.class);
        SearchAttributes searchAttributes = mock(SearchAttributes.class);
        when(subStepExecutorFactory.createSubStepExecutor(stepsAsTable)).thenReturn(subStepExecutor);
        when(searchActions.findElements(searchContext, searchAttributes)).thenReturn(List.of(mock(WebElement.class)));

        nestedSteps.performAllStepsWhileElementsExist(ComparisonRule.EQUAL_TO, 2, searchAttributes, 5, stepsAsTable);

        verifyZeroInteractions(subStepExecutor);
        verify(searchActions, times(1)).findElements(searchContext, searchAttributes);
        verify(softAssert).assertThat(eq(ELEMENTS_NUMBER), eq(1), argThat(m ->
            ComparisonRule.EQUAL_TO.getComparisonRule(2).toString().equals(m.toString())));
        verify(softAssert, never()).recordFailedAssertion(anyString());
        verify(webUiContext, never()).getSearchContextSetter();
    }
}
