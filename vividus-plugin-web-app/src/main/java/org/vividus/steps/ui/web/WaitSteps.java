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

import static org.openqa.selenium.support.ui.ExpectedConditions.alertIsPresent;
import static org.openqa.selenium.support.ui.ExpectedConditions.frameToBeAvailableAndSwitchToIt;
import static org.openqa.selenium.support.ui.ExpectedConditions.not;
import static org.openqa.selenium.support.ui.ExpectedConditions.stalenessOf;
import static org.openqa.selenium.support.ui.ExpectedConditions.titleContains;
import static org.openqa.selenium.support.ui.ExpectedConditions.titleIs;

import java.time.Duration;

import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.TimeoutConfigurer;
import org.vividus.softassert.ISoftAssert;
import org.vividus.steps.ui.validation.IBaseValidations;
import org.vividus.ui.State;
import org.vividus.ui.action.IExpectedConditions;
import org.vividus.ui.action.IWaitActions;
import org.vividus.ui.action.WaitResult;
import org.vividus.ui.action.search.Locator;
import org.vividus.ui.context.IUiContext;
import org.vividus.ui.monitor.TakeScreenshotOnFailure;
import org.vividus.ui.util.XpathLocatorUtils;
import org.vividus.ui.web.action.WebJavascriptActions;

import jakarta.inject.Inject;

@TakeScreenshotOnFailure
public class WaitSteps
{
    private static final long DIVISOR = 10;

    @Inject private IWebDriverProvider webDriverProvider;
    @Inject private IWaitActions waitActions;
    @Inject private IUiContext uiContext;
    @Inject private ISoftAssert softAssert;
    @Inject private IExpectedConditions<By> expectedSearchContextConditions;
    @Inject private IExpectedConditions<Locator> expectedSearchActionsConditions;
    @Inject private IBaseValidations baseValidations;
    @Inject private WebJavascriptActions javascriptActions;
    @Inject private TimeoutConfigurer timeoutConfigurer;

    /**
     * Waits for <b><i>an alert</i></b> appearance on the page
     * <p>
     * An <b>alert</b> is a small box that appears on the display screen to give you an information or to warn you about
     * a potentially damaging operation.
     * </p>
     */
    @When("I wait until an alert appears")
    public void waitTillAlertAppears()
    {
        waitActions.wait(getWebDriver(), alertIsPresent());
    }

    /**
     * Waits for <b><i>an alert</i></b> disappearance from the page
     * <p>
     * An <b>alert</b> is a small box that appears on the display screen to give you an information or to warn you about
     * a potentially damaging operation.
     * </p>
     */
    @When("I wait until an alert disappears")
    public void waitTillAlertDisappears()
    {
        waitActions.wait(getWebDriver(), not(alertIsPresent()));
    }

    /**
     * Waits for <b><i>an element</i></b> with the specified <b>locator</b> to take a certain <b>state</b>
     * in the specified search context
     * <p>
     * Actions performed at this step:
     * </p>
     * <ul>
     * <li><i>Finds</i> an element with the <b>specified locator</b> within search context
     * <li><i>Waits</i> until this element takes a certain state value
     * </ul>
     * @param locator to locate element
     * @param state State value of the element
     * (<i>Possible values:</i> <b>ENABLED, DISABLED, SELECTED, NOT_SELECTED, VISIBLE, NOT_VISIBLE</b>)
     */
    @When("I wait until state of element located by `$locator` is $state")
    public void waitTillElementIsSelected(Locator locator, State state)
    {
        waitActions.wait(getSearchContext(), state.getExpectedCondition(expectedSearchActionsConditions, locator));
    }

    /**
     * Waits for <b><i>a frame</i></b> with the specified <b>name</b> to appear and then switch to this frame
     * <p>
     * <b>Frame</b> is a block (part) of the page concluded in the tag <i>&lt;iframe&gt;</i>
     * </p>
     * <p>
     * Actions performed at this step:
     * </p>
     * <ul>
     * <li><i>Finds</i> a frame with the <b>specified name</b> on the page
     * <li>Switch to this frame
     * <li><i>Waits</i> until this frame appears on the page
     * </ul>
     * @param frameName Any attribute value or text value of the frame tag
     */
    @When("I wait until a frame with the name '$frameName' appears and I switch to it")
    public void waitTillFrameAppearsAndSwitchToIt(String frameName)
    {
        waitActions.wait(getWebDriver(), frameToBeAvailableAndSwitchToIt(frameName));
    }

    /**
     * Waits for <b><i>an element</i></b> with specified <b>locator</b>
     * in the specified search context becomes stale
     * <p>
     * When an element becomes <b>stale</b>:
     * </p>
     * <ul>
     * <li>The element has been deleted entirely.
     * <li>The element is no longer attached to the DOM.
     * </ul>
     * <p>
     * Actions performed at this step:
     * </p>
     * <ul>
     * <li><i>Finds</i> an element with the <b>specified locator</b> within search context
     * <li><i>Waits</i> until this element becomes stale
     * </ul>
     * @param locator to locate element
     */
    @When("I wait until element located by `$locator` is stale")
    public void waitTillElementIsStale(Locator locator)
    {
        WebElement element = baseValidations.assertIfElementExists("Required element", locator);
        waitActions.wait(getWebDriver(), stalenessOf(element));
    }

    /**
     * Waits for <b><i>an element</i></b> with specified <b>locator</b> in the specified search context
     * contains the certain <b>text</b>
     * <p>
     * Actions performed at this step:
     * </p>
     * <ul>
     * <li><i>Finds</i> an element <b>with the specified locator</b> within search context
     * <li><i>Waits</i> until <b>the text</b> in this element becomes visible
     * </ul>
     * @param locator to locate element
     * @param text Desired text to be present in the element
     */
    @When("I wait until element located by `$locator` contains text `$text`")
    public void waitTillElementContainsText(Locator locator, String text)
    {
        waitActions.wait(getSearchContext(),
              expectedSearchActionsConditions.textToBePresentInElementLocated(locator, text));
    }

    /**
     * Waits for the specified <b><i>page title</i></b> contains the certain <b>text</b>
     * <p>
     * <b>The page title</b> is a value of a <i>&lt;title&gt;</i> tag
     * </p>
     * @param text Desired text to be present in the element
     */
    @When("I wait until the page title contains the text '$text'")
    public void waitTillPageContainsTitle(String text)
    {
        waitActions.wait(getWebDriver(), titleContains(text));
    }

    /**
     * Waits for <b><i>the page</i></b> contains the certain <b>title</b>
     * <p>
     * <b>The page title</b> is a value of a <i>&lt;title&gt;</i> tag
     * </p>
     * @param title Expected title of the page
     */
    @When("I wait until the page has the title '$title'")
    public void waitTillPageHasTitle(String title)
    {
        waitActions.wait(getWebDriver(), titleIs(title));
    }

    /**
     * Waits for <b><i>a frame</i></b> with specified <b>name</b> becomes appearance on the page
     * <p>
     * <b>Frame</b> is a block (part) of the page concluded in the tag <i>&lt;iframe&gt;</i>
     * </p>
     * <p>
     * Actions performed at this step:
     * </p>
     * <ul>
     * <li><i>Finds</i> a frame with the <b>specified name</b> on the page
     * <li><i>Waits</i> until this frame becomes visible
     * </ul>
     * @param frameName Any attribute value or text value of the frame tag
     */
    @When("I wait until a frame with the name '$frameName' appears")
    public void waitTillFrameAppears(String frameName)
    {
        uiContext.getSearchContext(WebDriver.class).ifPresent(
                searchContext -> {
                    By locator = XpathLocatorUtils.getXPathLocator(
                            "*[(local-name()='frame' or local-name()='iframe') and @*='%s']", frameName);
                    waitForElementAppearance(searchContext, locator);
                }
        );
    }

    /**
     * Checks that no alert displayed during the <b><i>timeout</i></b>.
     * Makes {@value #DIVISOR} attempts to confirm alert displaying at regular intervals
     * <br>Example:<br>
     * <code> Then alert does not appear in `PT30S`</code>
     * - checks that no alert displayed for 30 seconds, polling every 3 seconds
     *
     * @param timeout Desired timeout according to
     * <a href="https://en.wikipedia.org/wiki/ISO_8601">ISO 8601</a> standard
     * @return true if alert does not appeared, false otherwise
     */
    @Then("alert does not appear in `$timeout`")
    public boolean waitAlertDoesNotAppear(Duration timeout)
    {
        WaitResult<Alert> wait = waitActions.wait(getWebDriver(), timeout, timeout.dividedBy(DIVISOR), alertIsPresent(),
                false);
        return softAssert.assertFalse("Alert does not appear", wait.isWaitPassed());
    }

    /**
     * Waits for element disappearance with desired timeout
     * @param locator The locating mechanism to use
     * @param timeout Desired timeout
     * @return true if element disappeared, false otherwise
     */
    @Then("element located by `$locator` disappears in `$timeout`")
    public boolean waitForElementDisappearance(Locator locator, Duration timeout)
    {
        return waitActions.wait(getSearchContext(), timeout,
                expectedSearchActionsConditions.invisibilityOfElement(locator)).isWaitPassed();
    }

    private boolean waitForElementAppearance(SearchContext searchContext, By by)
    {
        return waitActions.wait(searchContext,
                expectedSearchContextConditions.visibilityOfAllElementsLocatedBy(by)).isWaitPassed();
    }

    /**
     * Waits for the scroll finish; Could be useful for the cases when you have very slow scroll
     * and need to synchronize the tests with the scroll
     */
    @When("I wait until scroll is finished")
    public void waitForScroll()
    {
        javascriptActions.waitUntilScrollFinished();
    }

    /**
     * Sets a custom timeout for loading all following pages
     * <br>
     * Usage example:
     * <code>
     * <br>Given I am on page with URL `https://example.com/`
     * <br>When I set page load timeout to `PT15S`
     * <br>When I open URL `https://example.com/super-heavy-page` in new window
     * <br>When I set page load timeout to `PT10S`
     * </code>
     * <br>
     * @param duration timeout for pages loading
     */
    @When("I set page load timeout to `$duration`")
    public void configurePageLoadTimeout(Duration duration)
    {
        timeoutConfigurer.configurePageLoadTimeout(duration, getWebDriver().manage().timeouts());
    }

    private WebDriver getWebDriver()
    {
        return webDriverProvider.get();
    }

    private SearchContext getSearchContext()
    {
        return uiContext.getSearchContext();
    }
}
