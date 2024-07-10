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

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserContext.WaitForConditionOptions;
import com.microsoft.playwright.FrameLocator;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.assertions.PlaywrightAssertions;

import org.hamcrest.Matcher;
import org.jbehave.core.annotations.When;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.steps.StringComparisonRule;
import org.vividus.ui.web.playwright.BrowserContextProvider;
import org.vividus.ui.web.playwright.UiContext;
import org.vividus.ui.web.playwright.action.WaitActions;
import org.vividus.ui.web.playwright.assertions.PlaywrightSoftAssert;
import org.vividus.ui.web.playwright.locator.PlaywrightLocator;

public class SetContextSteps
{
    private static final Logger LOGGER = LoggerFactory.getLogger(SetContextSteps.class);

    private final BrowserContextProvider browserContextProvider;
    private final UiContext uiContext;
    private final WaitActions waitActions;
    private final PlaywrightSoftAssert playwrightSoftAssert;

    public SetContextSteps(BrowserContextProvider browserContextProvider, UiContext uiContext, WaitActions waitActions,
            PlaywrightSoftAssert playwrightSoftAssert)
    {
        this.browserContextProvider = browserContextProvider;
        this.uiContext = uiContext;
        this.waitActions = waitActions;
        this.playwrightSoftAssert = playwrightSoftAssert;
    }

    /**
     * Resets the context if it was set previously.
     */
    @When("I reset context")
    public void resetContext()
    {
        uiContext.resetContext();
    }

    /**
     * Resets the context, finds the element by the given locator and sets the context to this element if it's found.
     *
     * @param locator The locator used to find the element to change context to.
     */
    @When("I change context to element located by `$locator`")
    public void changeContext(PlaywrightLocator locator)
    {
        resetContext();
        changeContextInScopeOfCurrentContext(locator);
    }

    /**
     * Finds the element by the given locator in the current context and sets the context to this element if it's found.
     *
     * @param locator The locator used to find the element to change context to.
     */
    @When("I change context to element located by `$locator` in scope of current context")
    public void changeContextInScopeOfCurrentContext(PlaywrightLocator locator)
    {
        Locator context = uiContext.locateElement(locator);
        playwrightSoftAssert.runAssertion("The element to set context is not found", () -> {
            PlaywrightAssertions.assertThat(context).hasCount(1);
            uiContext.setContext(context);
            LOGGER.info("The context is successfully changed");
        });
    }

    /**
     * Switches to a frame found by locator.
     * <p>
     * A <b>frame</b> is used for splitting browser page into several segments, each of which can show a different
     * document (content). This enables updates of parts of a website while the user browses without making them reload
     * the whole page (this is now largely replaced by AJAX).
     * <p>
     * <b>Frame</b> elements are specified by {@code <iframe>} tag as the following example shows:
     * <pre>
     * {@code <iframe attributeType=}<b>'attributeValue'</b>{@code > some iframe content</iframe>}
     * </pre>
     * <p>
     * Actions performed at this step:
     * <ul>
     * <li>Resets the context;
     * <li>Finds a frame using specified locator;
     * <li>If the frame is found, performs switch to it.
     * </ul>
     * @see <a href="https://en.wikipedia.org/wiki/HTML_element#Frames"><i>Frames</i></a>
     * @see <a href="https://www.w3schools.com/tags/default.asp"><i>HTML Element Reference</i></a>
     * @param locator The locator to locate frame element
     */
    @When("I switch to frame located by `$locator`")
    public void switchToFrame(PlaywrightLocator locator)
    {
        resetContext();
        FrameLocator frameLocator = getFrameLocator(locator.getLocator());
        playwrightSoftAssert.runAssertion("The frame to switch is not found", () -> {
            PlaywrightAssertions.assertThat(frameLocator.locator(":root")).hasCount(1);
            uiContext.setCurrentFrame(frameLocator);
            LOGGER.info("Successfully switched to frame");
        });
    }

    /**
     * Switching to the default content of the page
     * <p>
     * Actions performed at this step:
     * <ul>
     * <li>Switches focus to the root tag of the page, this is as a rule {@code <html>} tag.
     * </ul>
     */
    @When("I switch back to page")
    public void switchingToDefault()
    {
        uiContext.reset();
    }

    /**
     * Wait for a tab and switches the focus of future browser commands to the new
     * <b>tab</b> with the specified <b>title</b>.
     * <b>Title</b> references to the page title, which is set by {@code <title>} tag.
     * <p>
     * Actions performed at this step:
     * <ul>
     * <li>Searches among currently opened tabs for a tab with the specified <b>tabName</b></li>
     * <li>If such tab is found switches focus to it. If tab is not found current focus stays unchanged</li>
     * </ul>
     * @param duration Duration in <a href="https://en.wikipedia.org/wiki/ISO_8601">ISO 8601 format</a>
     * @param comparisonRule String comparison rule: is equal to, contains, does not contain, matches
     * @param title Value of the {@code <title>} tag of a desired tab
     */
    @When("I wait `$duration` until tab with title that $comparisonRule `$title` appears and switch to it")
    public void waitForTabAndSwitch(Duration duration, StringComparisonRule comparisonRule, String title)
    {
        BrowserContext browserContext = browserContextProvider.get();
        WaitForConditionOptions options = new WaitForConditionOptions();
        options.setTimeout(duration.toMillis());
        Matcher<String> titleMatcher = comparisonRule.createMatcher(title);

        BooleanSupplier waitCondition = () ->
        {
            List<Page> pages = browserContext.pages();
            for (Page page : pages)
            {
                if (titleMatcher.matches(page.title()))
                {
                    uiContext.reset();
                    uiContext.setCurrentPage(page);
                    page.bringToFront();
                    return true;
                }
            }
            return false;
        };
        Supplier<String> assertionMessage = () -> String.format("switch to the tab where title %s \"%s\"",
                comparisonRule, title);

        waitActions.runWithTimeoutAssertion(assertionMessage,
                () -> browserContext.waitForCondition(waitCondition, options));
    }

    private FrameLocator getFrameLocator(String locator)
    {
        return Optional.ofNullable(uiContext.getCurrentFrame()).map(frame -> frame.frameLocator(locator))
                .orElseGet(() -> uiContext.getCurrentPage().frameLocator(locator));
    }
}
