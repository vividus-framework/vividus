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

package org.vividus.steps.ui.web;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

import java.time.Duration;
import java.util.function.BooleanSupplier;

import org.hamcrest.Matcher;
import org.jbehave.core.annotations.When;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.vividus.annotation.Replacement;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.steps.StringComparisonRule;
import org.vividus.steps.ui.validation.IBaseValidations;
import org.vividus.steps.ui.validation.IDescriptiveSoftAssert;
import org.vividus.ui.action.IWaitActions;
import org.vividus.ui.action.WaitResult;
import org.vividus.ui.action.search.Locator;
import org.vividus.ui.context.IUiContext;
import org.vividus.ui.monitor.TakeScreenshotOnFailure;
import org.vividus.ui.web.action.FrameActions;
import org.vividus.ui.web.action.IWindowsActions;

@TakeScreenshotOnFailure
public class SetContextSteps
{
    private static final String NEW_WINDOW_OR_BROWSER_TAB_NAME_IS = "New window or browser tab name is ";

    private final IUiContext uiContext;
    private final IWebDriverProvider webDriverProvider;
    private final IDescriptiveSoftAssert descriptiveSoftAssert;
    private final IWindowsActions windowsActions;
    private final FrameActions frameActions;
    private final IWaitActions waitActions;
    private final IBaseValidations baseValidations;

    public SetContextSteps(IUiContext uiContext, IWebDriverProvider webDriverProvider,
            IDescriptiveSoftAssert descriptiveSoftAssert, IWindowsActions windowsActions, FrameActions frameActions,
            IWaitActions waitActions, IBaseValidations baseValidations)
    {
        this.uiContext = uiContext;
        this.webDriverProvider = webDriverProvider;
        this.descriptiveSoftAssert = descriptiveSoftAssert;
        this.windowsActions = windowsActions;
        this.frameActions = frameActions;
        this.waitActions = waitActions;
        this.baseValidations = baseValidations;
    }

    /**
     * Switching to the default content of the page
     * <p>
     * Actions performed at this step:
     * <ul>
     * <li>Switches focus to the root tag of the page, this is as a rule {@code <html>} tag.
     * </ul>
     */
    @When("I switch back to the page")
    public void switchingToDefault()
    {
        resetContext();
        frameActions.switchToRoot();
    }

    /**
     * Switching to the frame using one of supported locators
     * <p>
     * A <b>frame</b> is used for splitting browser page into several segments, each of which can show a different
     * document (content). This enables updates of parts of a website while the user browses without making them reload
     * the whole page (this is now largely replaced by AJAX).
     * <p>
     * <b>Frames</b> are located inside {@code <iframe>} tag.
     * <p>
     * Actions performed at this step:
     * <ul>
     * <li>Finds the frame with desired parameters;
     * <li>If frame is found, switches focus to it.
     * </ul>
     * @see <a href="https://en.wikipedia.org/wiki/HTML_element#Frames"><i>Frames</i></a>
     * @see <a href="https://www.w3schools.com/tags/default.asp"><i>HTML Element Reference</i></a>
     * @param locator to locate frame element
     * <p>
     * <b>Example:</b>
     * <pre>
     * {@code <iframe attributeType=}<b>'attributeValue'</b>{@code > some iframe content</iframe>}
     * </pre>
     */
    @When("I switch to frame located `$locator`")
    public void switchingToFrame(Locator locator)
    {
        resetContext();
        WebElement element = baseValidations.assertIfElementExists("A frame", locator);
        if (element != null)
        {
            frameActions.switchToFrame(element);
        }
    }

    /**
     * Switch the focus of future browser commands to the new <b>window object</b>.
     * <p>
     * Each browser <b>window</b> or <b>tab</b> is considered to be a separate <b>window object</b>. This object holds
     * corresponding <b>Document</b> object, which itself is a html page.
     * <p>
     * Actions performed at this step:
     * <ul>
     * <li>Gets identifier of the currently active window (tab);
     * <li>Switches focus to the first available window with another identifier. So if currently there are 3 opened
     * windows #1, #2, #3 and window #2 is active one, using this step will switch focus to the window #3;
     * </ul>
     * @see <a href="https://html.spec.whatwg.org/#browsing-context"><i>Browsing context (Window &amp; Document)
     * reference</i></a>
     * @deprecated Use step: "When I switch to new tab" instead
     */
    @When("I switch to a new window")
    @Deprecated(since = "0.5.11", forRemoval = true)
    @Replacement(versionToRemoveStep = "0.7.0", replacementFormatPattern = "When I switch to new tab")
    public void switchingToWindow()
    {
        String currentWindow = getWebDriver().getWindowHandle();
        String newWindow = windowsActions.switchToNewTab(currentWindow);
        if (descriptiveSoftAssert.assertThat(String.format("New window '%s' is found", newWindow),
                "New window is found", newWindow, not(equalTo(currentWindow))))
        {
            resetContext();
        }
    }

    /**
     * Switch the focus of future browser commands to the new <b>tab</b>.
     * Actions performed at this step:
     * <ul>
     * <li>Gets identifier of the currently active tab;
     * <li>Switches focus to the first available tab with another identifier. So if currently there are 3 opened
     * tabs #1, #2, #3 and tab #2 is active one, using this step will switch focus to the tab #3;
     * </ul>
     * @see <a href="https://html.spec.whatwg.org/#browsing-context"><i>Browsing context reference</i></a>
     */
    @When("I switch to new tab")
    public void switchToTab()
    {
        String currentTab = getWebDriver().getWindowHandle();
        String newTab = windowsActions.switchToNewTab(currentTab);
        if (descriptiveSoftAssert.recordAssertion(!newTab.equals(currentTab), "New tab is found"))
        {
            resetContext();
        }
    }

    /**
    * Switch the focus of future browser commands to the new <b>window object</b> with the specified <b>windowName</b>.
    * <p>
    * Each browser <b>window</b> or <b>tab</b> is considered to be a separate <b>window object</b>. This object holds
    * corresponding <b>Document</b> object, which itself is a HTML page.
    * </p>
    * <b>WindowName</b> references to the page title, which is set by {@code <title>} tag.
    * <p>
    * Actions performed at this step:
    * <ul>
    * <li>Searches among currently opened windows (and tabs) for a window with the specified <b>windowName</b>.</li>
    * <li>If such window is found switches focus to it. If window is not found current focus stays unchanged;</li>
    * </ul>
    * @param comparisonRule is equal to, contains, does not contain
    * @param windowName Value of the {@code <title>} tag of a desired window
    * @see <a href="https://html.spec.whatwg.org/#browsing-context"><i>Browsing context (Window &amp; Document)</i></a>
    * @see <a href="https://www.w3schools.com/tags/default.asp"><i>HTML Element Reference</i></a>
    * @deprecated Use step: "When I switch to tab with title that $stringComparisonRule `$tabName`" instead
    */
    @Deprecated(since = "0.5.11", forRemoval = true)
    @Replacement(versionToRemoveStep = "0.7.0", replacementFormatPattern =
            "When I switch to tab with title that %1$s `%2$s`")
    @When("I switch to window with title that $stringComparisonRule `$windowName`")
    public void switchingToWindow(StringComparisonRule comparisonRule, String windowName)
    {
        Matcher<String> matcher = comparisonRule.createMatcher(windowName);
        String titleAfterSwitch = windowsActions.switchToTabWithMatchingTitle(matcher);
        resetContextIf(() ->
            descriptiveSoftAssert.assertThat(NEW_WINDOW_OR_BROWSER_TAB_NAME_IS, "Window or tab name is ",
                titleAfterSwitch, matcher));
    }

    /**
     * Switch the focus of future browser commands to the new tab with the specified <b>tabName</b>.
     * <p>
     * The <b>tabName</b> references to the page title, which is set by {@code <title>} tag.
     * </p>
     * Actions performed at this step:
     * <ul>
     * <li>Searches among currently opened tabs for a tab with the specified <b>tabName</b>.</li>
     * <li>If such tab is found switches focus to it. If tab is not found current focus stays unchanged;</li>
     * </ul>
     * @param comparisonRule is equal to, contains, does not contain
     * @param tabName Value of the {@code <title>} tag of a desired tab
     * @see <a href="https://html.spec.whatwg.org/#browsing-context"><i>Browsing context</i></a>
     * @see <a href="https://www.w3schools.com/tags/default.asp"><i>HTML Element Reference</i></a>
     */
    @When("I switch to tab with title that $stringComparisonRule `$tabName`")
    public void switchToTab(StringComparisonRule comparisonRule, String tabName)
    {
        Matcher<String> matcher = comparisonRule.createMatcher(tabName);
        String titleAfterSwitch = windowsActions.switchToTabWithMatchingTitle(matcher);
        resetContextIf(() ->
                descriptiveSoftAssert.assertThat(NEW_WINDOW_OR_BROWSER_TAB_NAME_IS,
                        titleAfterSwitch, matcher));
    }

    /**
    * Wait for a tab and switches the focus of future browser commands to the new
    * <b>tab</b> with the specified <b>title</b>.
    * <b>Title</b> references to the page title, which is set by {@code <title>} tag.
    * <p>
    * Actions performed at this step:
    * <ul>
    * <li>Searches among currently opened tabs for a tab with the specified <b>tabName</b>.</li>
    * <li>If such tab is found switches focus to it. If tab is not found current focus stays unchanged;</li>
    * </ul>
    * @param comparisonRule is equal to, contains, does not contain
    * @param title Value of the {@code <title>} tag of a desired tab
    * @param duration in format <a href="https://en.wikipedia.org/wiki/ISO_8601">Duration Format</a>
    * @see <a href="https://html.spec.whatwg.org/#browsing-context"><i>Browsing context</i></a>
    * @see <a href="https://www.w3schools.com/tags/default.asp"><i>HTML Element Reference</i></a>
    */
    @When("I wait `$duration` until tab with title that $comparisonRule `$title` appears and switch to it")
    public void waitForTabAndSwitch(Duration duration, StringComparisonRule comparisonRule, String title)
    {
        Matcher<String> expected = comparisonRule.createMatcher(title);
        WaitResult<Boolean> result = waitActions.wait(webDriverProvider.get(), duration, new ExpectedCondition<>()
        {
            @Override
            public Boolean apply(WebDriver driver)
            {
                try
                {
                    return expected.matches(windowsActions.switchToTabWithMatchingTitle(expected));
                }
                catch (WebDriverException webDriverException)
                {
                    return false;
                }
            }

            @Override
            public String toString()
            {
                return String.format("switch to the tab where title %s \"%s\"", comparisonRule, title);
            }
        });
        resetContextIf(result::isWaitPassed);
    }

    private void resetContextIf(BooleanSupplier condition)
    {
        if (condition.getAsBoolean())
        {
            resetContext();
        }
    }

    private WebDriver getWebDriver()
    {
        return webDriverProvider.get();
    }

    private void resetContext()
    {
        uiContext.reset();
    }
}
