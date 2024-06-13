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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.TimeoutError;
import com.microsoft.playwright.options.WaitForSelectorState;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.softassert.ISoftAssert;
import org.vividus.steps.ComparisonRule;
import org.vividus.ui.web.playwright.BrowserContextProvider;
import org.vividus.ui.web.playwright.UiContext;
import org.vividus.ui.web.playwright.locator.PlaywrightLocator;

@ExtendWith(MockitoExtension.class)
class WaitStepsTests
{
    private static final String TIMEOUT_ERROR_MESSAGE = "Timeout error message";

    @Mock private UiContext uiContext;
    @Mock private ISoftAssert softAssert;
    @Mock private BrowserContextProvider browserContextProvider;
    @Mock private Locator locator;
    @Mock private BrowserContext context;
    @InjectMocks private WaitSteps waitSteps;

    private final PlaywrightLocator playwrightLocator = new PlaywrightLocator("css", "div");

    @Test
    void shouldWaitForElementAppearance()
    {
        testWaitForElementStateStep(waitSteps::waitForElementAppearance, WaitForSelectorState.VISIBLE);
    }

    @Test
    void shouldWaitForElementDisappearance()
    {
        testWaitForElementStateStep(waitSteps::waitForElementDisappearance, WaitForSelectorState.HIDDEN);
    }

    @Test
    void shouldWaitForNumberOfElements()
    {
        int numberOfElements = 5;
        when(browserContextProvider.get()).thenReturn(context);
        when(uiContext.locateElement(playwrightLocator)).thenReturn(locator);
        when(locator.count()).thenReturn(numberOfElements);

        waitSteps.waitForElementNumber(playwrightLocator, ComparisonRule.EQUAL_TO, numberOfElements);
        ArgumentCaptor<BooleanSupplier> conditionCaptor = ArgumentCaptor.forClass(BooleanSupplier.class);
        verify(context).waitForCondition(conditionCaptor.capture());

        BooleanSupplier condition = conditionCaptor.getValue();
        Assertions.assertTrue(condition.getAsBoolean());
        verify(softAssert).recordPassedAssertion(
                "Passed wait condition: number of elements located by 'css(div) with visibility: visible'"
                + " to be equal to 5");
        verifyNoMoreInteractions(softAssert);
    }

    @Test
    void shouldRecordFailedAssertionOnElementAppearanceTimeout()
    {
        testStepRecordFailedAssertionOnElementStateTimeout(waitSteps::waitForElementAppearance,
                WaitForSelectorState.VISIBLE);
    }

    @Test
    void shouldRecordFailedAssertionOnElementDisappearanceTimeout()
    {
        testStepRecordFailedAssertionOnElementStateTimeout(waitSteps::waitForElementDisappearance,
                WaitForSelectorState.HIDDEN);
    }

    @Test
    void shouldRecordFailedAssertionOnWaitNumberOfElementsTimeout()
    {
        var timeoutError = new TimeoutError(TIMEOUT_ERROR_MESSAGE);
        when(browserContextProvider.get()).thenReturn(context);
        doThrow(timeoutError).when(context).waitForCondition(any(BooleanSupplier.class));
        waitSteps.waitForElementNumber(playwrightLocator, ComparisonRule.EQUAL_TO, 3);
        verify(softAssert).recordFailedAssertion(
                "Failed wait condition: number of elements located by 'css(div) with visibility: visible'"
                + " to be equal to 3. " + TIMEOUT_ERROR_MESSAGE, timeoutError);
        verifyNoMoreInteractions(softAssert);
    }

    private void testWaitForElementStateStep(Consumer<PlaywrightLocator> step, WaitForSelectorState selectorState)
    {
        when(uiContext.locateElement(playwrightLocator)).thenReturn(locator);
        step.accept(playwrightLocator);
        verify(locator).waitFor(argThat(arg -> arg.state == selectorState));
        String assertionMessage = String.format(
                "Passed wait condition: element located by 'css(div) with visibility: visible' to be %s",
                selectorState.toString().toLowerCase());
        verify(softAssert).recordPassedAssertion(assertionMessage);
        verifyNoMoreInteractions(softAssert);
    }

    private void testStepRecordFailedAssertionOnElementStateTimeout(Consumer<PlaywrightLocator> step,
            WaitForSelectorState selectorState)
    {
        when(uiContext.locateElement(playwrightLocator)).thenReturn(locator);
        var timeoutError = new TimeoutError(TIMEOUT_ERROR_MESSAGE);
        doAnswer(invocation -> {
            throw timeoutError;
        }).when(locator).waitFor(argThat(arg -> arg.state == selectorState));
        step.accept(playwrightLocator);
        String assertionMessage = String.format(
                "Failed wait condition: element located by 'css(div) with visibility: visible' to be %s. %s",
                selectorState.toString().toLowerCase(), TIMEOUT_ERROR_MESSAGE);
        verify(softAssert).recordFailedAssertion(assertionMessage, timeoutError);
    }
}
