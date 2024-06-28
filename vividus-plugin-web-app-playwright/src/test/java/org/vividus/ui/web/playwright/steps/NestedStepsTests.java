/*
 * Copyright 2019-2024 the original author or authors.
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

package org.vividus.ui.web.playwright.steps;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.assertions.LocatorAssertions;
import com.microsoft.playwright.assertions.PlaywrightAssertions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.softassert.ISoftAssert;
import org.vividus.steps.ComparisonRule;
import org.vividus.steps.SubSteps;
import org.vividus.ui.web.playwright.UiContext;
import org.vividus.ui.web.playwright.assertions.PlaywrightSoftAssert;
import org.vividus.ui.web.playwright.locator.PlaywrightLocator;

@ExtendWith(MockitoExtension.class)
class NestedStepsTests
{
    private static final PlaywrightLocator LOCATOR = new PlaywrightLocator("css", "div");

    private static final String ELEMENTS_NUMBER = "Elements number";
    private static final String ITERATION_ELEMENT_NOT_FOUND_MESSAGE = "An element for iteration %d is not found";

    @Mock private UiContext uiContext;

    @Mock private ISoftAssert softAssert;

    @Mock private SubSteps subSteps;

    @Mock private PlaywrightSoftAssert playwrightSoftAssert;

    @InjectMocks private NestedSteps nestedSteps;

    @Test
    void shouldPerformAllStepsForElementIfFound()
    {
        Locator originalContext = mock();
        Locator elementsLocator = mock();
        Locator first = mock();
        Locator second = mock();
        when(uiContext.locateElement(LOCATOR)).thenReturn(elementsLocator);
        when(elementsLocator.all()).thenReturn(Arrays.asList(first, second));
        when(uiContext.getContext()).thenReturn(originalContext);
        try (var playwrightAssertionsStaticMock = mockStatic(PlaywrightAssertions.class))
        {
            LocatorAssertions locatorAssertions = mock();
            Stream.of(first, second).forEach(
                    locator -> playwrightAssertionsStaticMock.when(() -> PlaywrightAssertions.assertThat(locator))
                            .thenReturn(locatorAssertions));
            IntStream.rangeClosed(1, 2).forEach(this::mockPlaywrightAssertionForIteratedElement);
            nestedSteps.executeStepsForAllLocatedElements(ComparisonRule.EQUAL_TO, 1, LOCATOR, subSteps);
            var ordered = inOrder(uiContext, subSteps);
            Stream.of(first, second).forEach(locator -> {
                ordered.verify(uiContext).getContext();
                ordered.verify(uiContext).setContext(locator);
                ordered.verify(subSteps).execute(Optional.empty());
                ordered.verify(uiContext).setContext(originalContext);
            });
            verifyNoMoreInteractions(uiContext, subSteps);
        }
    }

    @Test
    void shouldNotPerformStepsIfElementNotFound()
    {
        Locator locator = mock();
        when(uiContext.locateElement(LOCATOR)).thenReturn(locator);
        when(locator.all()).thenReturn(List.of());
        nestedSteps.executeStepsForAllLocatedElements(ComparisonRule.GREATER_THAN_OR_EQUAL_TO, 0, LOCATOR, subSteps);
        verifyNoMoreInteractions(uiContext);
        verifyNoInteractions(subSteps);
    }

    @Test
    void shouldStopWhenQuantityChangedAndIterationLimitNotReached()
    {
        Locator locator = mock();
        when(uiContext.locateElement(LOCATOR)).thenReturn(locator);
        when(locator.count()).thenReturn(1).thenReturn(1).thenReturn(0);
        when(softAssert.assertThat(eq(ELEMENTS_NUMBER), eq(1), argThat(m ->
                ComparisonRule.EQUAL_TO.getComparisonRule(1).toString().equals(m.toString())))).thenReturn(true);
        nestedSteps.performAllStepsWhileElementsExist(ComparisonRule.EQUAL_TO, 1, LOCATOR, 5, subSteps);
        verify(subSteps, times(2)).execute(Optional.empty());
        verify(softAssert).assertThat(eq(ELEMENTS_NUMBER), eq(1),
                argThat(m -> ComparisonRule.EQUAL_TO.getComparisonRule(1).toString().equals(m.toString())));
    }

    @Test
    void shouldStopAndRecordFailureWhenExceedsIterationLimit()
    {
        Locator locator = mock();
        when(uiContext.locateElement(LOCATOR)).thenReturn(locator);
        when(locator.count()).thenReturn(1);
        when(softAssert.assertThat(eq(ELEMENTS_NUMBER), eq(1), argThat(m ->
                ComparisonRule.EQUAL_TO.getComparisonRule(1).toString().equals(m.toString())))).thenReturn(true);
        nestedSteps.performAllStepsWhileElementsExist(ComparisonRule.EQUAL_TO, 1, LOCATOR, 2, subSteps);
        verify(subSteps, times(2)).execute(Optional.empty());
        verify(softAssert).recordFailedAssertion("Elements number a value equal to <1>"
                                                 + " was not changed after 2 iteration(s)");
    }

    @Test
    void shouldNotExecuteStepsForNegativeIterationLimit()
    {
        nestedSteps.performAllStepsWhileElementsExist(ComparisonRule.EQUAL_TO, 1, LOCATOR, -1, subSteps);
        verifyNoInteractions(uiContext, softAssert, subSteps);
    }

    @Test
    void shouldNotExecuteStepsIfInitialElementsNumberIsNotValid()
    {
        Locator locator = mock();
        when(uiContext.locateElement(LOCATOR)).thenReturn(locator);
        when(locator.count()).thenReturn(1);
        nestedSteps.performAllStepsWhileElementsExist(ComparisonRule.EQUAL_TO, 2, LOCATOR, 3, subSteps);
        verify(softAssert).assertThat(eq(ELEMENTS_NUMBER), eq(1), argThat(m ->
                ComparisonRule.EQUAL_TO.getComparisonRule(2).toString().equals(m.toString())));
        verifyNoMoreInteractions(uiContext, softAssert);
        verifyNoInteractions(subSteps);
    }

    private void mockPlaywrightAssertionForIteratedElement(int iteration)
    {
        doNothing().when(playwrightSoftAssert)
                .runAssertion(eq(String.format(ITERATION_ELEMENT_NOT_FOUND_MESSAGE, iteration)), argThat(runnable -> {
                    runnable.run();
                    return true;
                }));
    }
}
