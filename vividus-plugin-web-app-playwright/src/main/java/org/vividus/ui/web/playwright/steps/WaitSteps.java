/*
 * Copyright 2019-2025 the original author or authors.
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

import java.time.Duration;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import org.apache.commons.lang3.Validate;
import org.hamcrest.Matcher;
import org.jbehave.core.annotations.When;
import org.vividus.softassert.ISoftAssert;
import org.vividus.steps.ComparisonRule;
import org.vividus.steps.StringComparisonRule;
import org.vividus.ui.web.playwright.BrowserContextProvider;
import org.vividus.ui.web.playwright.UiContext;
import org.vividus.ui.web.playwright.action.WaitActions;
import org.vividus.ui.web.playwright.assertions.PlaywrightLocatorAssertions;
import org.vividus.ui.web.playwright.locator.PlaywrightLocator;
import org.vividus.ui.web.playwright.locator.Visibility;
import org.vividus.util.wait.DurationBasedWaiter;

public class WaitSteps
{
    private final UiContext uiContext;
    private final BrowserContextProvider browserContextProvider;
    private final WaitActions waitActions;
    private final ISoftAssert softAssert;

    public WaitSteps(UiContext uiContext, BrowserContextProvider browserContextProvider, WaitActions waitActions,
            ISoftAssert softAssert)
    {
        this.uiContext = uiContext;
        this.browserContextProvider = browserContextProvider;
        this.waitActions = waitActions;
        this.softAssert = softAssert;
    }

    /**
     * Waits for appearance of an <b><i>element</i></b> with the specified <b>locator</b>
     * Step supports only <b>VISIBLE</b> elements waiting. If locator will be configured to <b>ALL</b> exception will
     * be thrown.
     * @param locator locator to locate element
     */
    @When("I wait until element located by `$locator` appears")
    public void waitForElementAppearance(PlaywrightLocator locator)
    {
        waitForElementStateValidatingVisibility(locator, ElementState.VISIBLE);
    }

    /**
     * Waits for disappearance of an <b><i>element</i></b> with the specified <b>locator</b>
     * Step supports only <b>VISIBLE</b> elements waiting. If locator will be configured to <b>ALL</b> exception will
     * be thrown.
     * @param locator locator to locate element
     */
    @When("I wait until element located by `$locator` disappears")
    public void waitForElementDisappearance(PlaywrightLocator locator)
    {
        waitForElementStateValidatingVisibility(locator, ElementState.NOT_VISIBLE);
    }

    /**
     * Waits for appearance of an <b><i>element</i></b> with the specified <b>locator</b> in viewport
     * @param locator locator to locate element
     */
    @When("I wait until element located by `$locator` appears in viewport")
    public void waitForElementAppearanceInViewport(PlaywrightLocator locator)
    {
        Supplier<String> conditionDescription = () -> "The element located by `%s` is visible in viewport"
                .formatted(locator);
        waitActions.runWithTimeoutAssertion(conditionDescription, () ->
        {
            Locator element = uiContext.locateElement(locator);
            PlaywrightLocatorAssertions.assertElementInViewport(element, true);
        });
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

    /**
     * Waits for <b><i>an element</i></b> with the specified <b>locator</b> to take a certain <b>state</b>
     * in the specified search context
     * @param locator to locate element
     * @param state State value of the element
     * (<i>Possible values:</i> <b>ENABLED, DISABLED, SELECTED, NOT_SELECTED, VISIBLE, NOT_VISIBLE</b>)
     */
    @When("I wait until state of element located by `$locator` is $state")
    public void waitForElementState(PlaywrightLocator locator, ElementState state)
    {
        Supplier<String> conditionDescription = () -> String.format("element located by `%s` to be %s", locator, state);
        waitActions.runTimeoutPlaywrightAssertion(conditionDescription, () -> {
            Locator element = uiContext.locateElement(locator);
            state.waitForElementState(element);
        });
    }

    /**
     * Waits <b>duration</b> with <b>pollingDuration</b> until <b>an element</b> by the specified <b>locator</b>
     * becomes a <b>state</b> in the specified search context
     * <br>Example:<br>
     * <code>When I wait 'PT30S' with 'PT10S' polling until element located `id(text)` becomes NOT_VISIBLE</code>
     * - wait until all elements with id=text becomes not visible for 30 seconds, polling every 10 seconds
     *
     * @param duration        Total waiting time according to
     *                        <a href="https://en.wikipedia.org/wiki/ISO_8601">ISO 8601</a> standard
     * @param pollingDuration Defines the timeout between attempts according to
     *                        <a href="https://en.wikipedia.org/wiki/ISO_8601">ISO 8601</a> standard
     * @param locator         Locator to search for elements
     * @param state           State value of the element
     * (<i>Possible values:</i> <b>ENABLED, DISABLED, SELECTED, NOT_SELECTED, VISIBLE, NOT_VISIBLE</b>)
     */
    @When("I wait `$duration` with `$pollingDuration` polling until element located by `$locator` becomes $state")
    public void waitDurationWithPollingTillElementState(Duration duration, Duration pollingDuration,
            PlaywrightLocator locator, ElementState state)
    {
        Locator element = uiContext.locateElement(locator);
        boolean isElementInState = new DurationBasedWaiter(duration, pollingDuration).wait(
                () -> state.isElementState(element), result -> result);
        softAssert.assertTrue(String.format("The element located by `%s` has become %s", locator, state),
                isElementInState);
    }

    /**
     * Waits until an element with the specified locator has text that matches the provided regular expression.
     *
     * @param locator The locator of the element which text to check
     * @param regex   The regular expression used to validate the text of the element
     */
    @When("I wait until element located by `$locator` has text matching `$regex`")
    public void waitUntilElementHasTextMatchingRegex(PlaywrightLocator locator, Pattern regex)
    {
        Supplier<String> conditionDescription = () -> "The element located by `%s` has text matching regex '%s'"
                .formatted(locator, regex);
        waitActions.runWithTimeoutAssertion(conditionDescription, () ->
        {
            Locator element = uiContext.locateElement(locator);
            PlaywrightLocatorAssertions.assertElementHasTextMatchingRegex(element, regex, true);
        });
    }

    private void waitForElementStateValidatingVisibility(PlaywrightLocator locator,
            ElementState state)
    {
        Validate.isTrue(Visibility.VISIBLE == locator.getVisibility(),
                "The step supports locators with VISIBLE visibility settings only, but the locator is `%s`",
                locator.toString());
        waitForElementState(locator, state);
    }
}
