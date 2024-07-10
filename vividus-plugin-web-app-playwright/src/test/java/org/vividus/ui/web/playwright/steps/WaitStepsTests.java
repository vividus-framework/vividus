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

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.steps.ComparisonRule;
import org.vividus.steps.StringComparisonRule;
import org.vividus.ui.web.playwright.BrowserContextProvider;
import org.vividus.ui.web.playwright.UiContext;
import org.vividus.ui.web.playwright.action.WaitActions;
import org.vividus.ui.web.playwright.locator.PlaywrightLocator;

@ExtendWith(MockitoExtension.class)
class WaitStepsTests
{
    @Mock private UiContext uiContext;
    @Mock private BrowserContextProvider browserContextProvider;
    @Mock private WaitActions waitActions;
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
        doNothing().when(waitActions).runWithTimeoutAssertion(
                eq("number of elements located by 'css(div) with visibility: visible' to be equal to 5"),
                argThat(runnable ->
                {
                    runnable.run();
                    return true;
                }));

        waitSteps.waitForElementNumber(playwrightLocator, ComparisonRule.EQUAL_TO, numberOfElements);
        ArgumentCaptor<BooleanSupplier> conditionCaptor = ArgumentCaptor.forClass(BooleanSupplier.class);
        verify(context).waitForCondition(conditionCaptor.capture());

        BooleanSupplier condition = conditionCaptor.getValue();
        assertTrue(condition.getAsBoolean());
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldWaitUntilPageTitleIs()
    {
        String title = "Title";
        Page page = mock();
        when(uiContext.getCurrentPage()).thenReturn(page);
        when(page.title()).thenReturn(title);
        doNothing().when(waitActions)
                .runWithTimeoutAssertion((Supplier<String>) argThat(
                        s -> {
                            String value = ((Supplier<String>) s).get();
                            return "current title contains \"Title\". Current title: \"Title\"".equals(value);
                        }),
                        argThat(runnable ->
                        {
                            runnable.run();
                            return true;
                        }));

        waitSteps.waitUntilPageTitleIs(StringComparisonRule.CONTAINS, title);

        ArgumentCaptor<BooleanSupplier> conditionCaptor = ArgumentCaptor.forClass(BooleanSupplier.class);
        verify(page).waitForCondition(conditionCaptor.capture());
        BooleanSupplier condition = conditionCaptor.getValue();
        assertTrue(condition.getAsBoolean());
    }

    private void testWaitForElementStateStep(Consumer<PlaywrightLocator> step, WaitForSelectorState selectorState)
    {
        when(uiContext.locateElement(playwrightLocator)).thenReturn(locator);
        doNothing().when(waitActions)
                .runWithTimeoutAssertion(eq("element located by 'css(div) with visibility: visible' to be "
                        + selectorState.toString().toLowerCase()), argThat(runnable ->
                        {
                            runnable.run();
                            return true;
                        }));
        step.accept(playwrightLocator);
        verify(locator).waitFor(argThat(arg -> arg.state == selectorState));
    }
}
