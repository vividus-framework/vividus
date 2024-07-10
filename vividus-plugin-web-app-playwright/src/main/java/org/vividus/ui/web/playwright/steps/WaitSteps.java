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

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;

import org.hamcrest.Matcher;
import org.jbehave.core.annotations.When;
import org.vividus.steps.ComparisonRule;
import org.vividus.steps.StringComparisonRule;
import org.vividus.ui.web.playwright.BrowserContextProvider;
import org.vividus.ui.web.playwright.UiContext;
import org.vividus.ui.web.playwright.action.WaitActions;
import org.vividus.ui.web.playwright.locator.PlaywrightLocator;

public class WaitSteps
{
    private final UiContext uiContext;
    private final BrowserContextProvider browserContextProvider;
    private final WaitActions waitActions;

    public WaitSteps(UiContext uiContext, BrowserContextProvider browserContextProvider, WaitActions waitActions)
    {
        this.uiContext = uiContext;
        this.browserContextProvider = browserContextProvider;
        this.waitActions = waitActions;
    }

    /**
     * Waits for appearance of an <b><i>element</i></b> with the specified <b>locator</b>
     * @param locator locator to locate element
     */
    @When("I wait until element located by `$locator` appears")
    public void waitForElementAppearance(PlaywrightLocator locator)
    {
        waitForElementState(locator, WaitForSelectorState.VISIBLE);
    }

    /**
     * Waits for disappearance of an <b><i>element</i></b> with the specified <b>locator</b>
     * @param locator locator to locate element
     */
    @When("I wait until element located by `$locator` disappears")
    public void waitForElementDisappearance(PlaywrightLocator locator)
    {
        waitForElementState(locator, WaitForSelectorState.HIDDEN);
    }

    /**
     * Waits for expected number of elements.
     * @param locator        The locator to find elements.
     * @param comparisonRule The rule to match the variable value. The supported rules:
     *                       <ul>
     *                       <li>less than (&lt;)</li>
     *                       <li>less than or equal to (&lt;=)</li>
     *                       <li>greater than (&gt;)</li>
     *                       <li>greater than or equal to (&gt;=)</li>
     *                       <li>equal to (=)</li>
     *                       <li>not equal to (!=)</li>
     *                       </ul>
     * @param number         The expected number of elements.
     */
    @When("I wait until number of elements located by `$locator` is $comparisonRule $number")
    public void waitForElementNumber(PlaywrightLocator locator, ComparisonRule comparisonRule, int number)
    {
        BooleanSupplier waitCondition = () -> comparisonRule.getComparisonRule(number)
                .matches(uiContext.locateElement(locator).count());
        String assertionMessage = String.format("number of elements located by '%s' to be %s %d", locator,
                comparisonRule, number);
        waitActions.runWithTimeoutAssertion(assertionMessage,
                () -> browserContextProvider.get().waitForCondition(waitCondition));
    }

    /**
     * Waits until the current page title matches the certain title using specified comparison rule.
     * @param comparisonRule String comparison rule: "is equal to", "contains", "does not contain", "matches"
     * @param pattern The expected title pattern of the current page
     */
    @When("I wait until page title $comparisonRule `$pattern`")
    public void waitUntilPageTitleIs(StringComparisonRule comparisonRule, String pattern)
    {
        Page currentPage = uiContext.getCurrentPage();
        Matcher<String> matcher = comparisonRule.createMatcher(pattern);
        BooleanSupplier waitCondition = () -> matcher.matches(currentPage.title());
        Supplier<String> assertionMessage = () -> String.format("current title %s \"%s\". Current title: \"%s\"",
                comparisonRule, pattern, currentPage.title());
        waitActions.runWithTimeoutAssertion(assertionMessage, () -> currentPage.waitForCondition(waitCondition));
    }

    private void waitForElementState(PlaywrightLocator locator, WaitForSelectorState state)
    {
        Locator.WaitForOptions waitOption = new Locator.WaitForOptions().setState(state);
        String assertionMessage = String.format("element located by '%s' to be %s", locator,
                state.toString().toLowerCase());
        waitActions.runWithTimeoutAssertion(assertionMessage,
                () -> uiContext.locateElement(locator).waitFor(waitOption));
    }
}
