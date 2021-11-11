/*
 * Copyright 2019-2021 the original author or authors.
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

import static org.openqa.selenium.support.ui.ExpectedConditions.alertIsPresent;
import static org.openqa.selenium.support.ui.ExpectedConditions.frameToBeAvailableAndSwitchToIt;
import static org.openqa.selenium.support.ui.ExpectedConditions.not;
import static org.openqa.selenium.support.ui.ExpectedConditions.stalenessOf;
import static org.openqa.selenium.support.ui.ExpectedConditions.titleContains;
import static org.openqa.selenium.support.ui.ExpectedConditions.titleIs;

import java.time.Duration;
import java.util.List;

import javax.inject.Inject;

import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.vividus.bdd.monitor.TakeScreenshotOnFailure;
import org.vividus.bdd.steps.ui.validation.IBaseValidations;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.TimeoutConfigurer;
import org.vividus.softassert.ISoftAssert;
import org.vividus.ui.State;
import org.vividus.ui.action.IExpectedConditions;
import org.vividus.ui.action.ISearchActions;
import org.vividus.ui.action.IWaitActions;
import org.vividus.ui.action.WaitResult;
import org.vividus.ui.action.search.Locator;
import org.vividus.ui.context.IUiContext;
import org.vividus.ui.web.action.WebJavascriptActions;
import org.vividus.ui.web.action.search.WebLocatorType;
import org.vividus.ui.web.util.LocatorUtil;

@TakeScreenshotOnFailure
public class WaitSteps
{
    private static final long DIVISOR = 10;

    @Inject private IWebDriverProvider webDriverProvider;
    @Inject private IWaitActions waitActions;
    @Inject private ISearchActions searchActions;
    @Inject private IUiContext uiContext;
    @Inject private ISoftAssert softAssert;
    @Inject private IExpectedConditions<By> expectedSearchContextConditions;
    @Inject private IExpectedConditions<Locator> expectedSearchActionsConditions;
    @Inject private IBaseValidations baseValidations;
    @Inject private WebJavascriptActions javascriptActions;
    @Inject private TimeoutConfigurer timeoutConfigurer;

    /**
     * Waits <b>duration</b> with <b>pollingDuration</b> until <b>an element</b> by the specified <b>locator</b>
     * becomes a <b>state</b> in the specified search context
     * <p>
     * Actions performed at this step:
     * </p>
     * <ul>
     * <li><i>Finds</i> an element by the specified <b>locator</b> within search context
     * <li><i>Waits</i> <b>duration</b> with <b>pollingDuration</b> until this element becomes <b>state</b>
     * </ul>
     * <br>Example:<br>
     * <code>When I wait 'PT30S' with 'PT10S' polling until element located `By.id(text)` becomes NOT_VISIBLE</code>
     * - wait until all elements with id=text becomes not visible for 30 seconds, polling every 10 seconds
     *
     * @param duration        Total waiting time according to
     *                        <a href="https://en.wikipedia.org/wiki/ISO_8601">ISO 8601</a> standard
     * @param pollingDuration Defines the timeout between attempts according to
     *                        <a href="https://en.wikipedia.org/wiki/ISO_8601">ISO 8601</a> standard
     * @param locator         Locator to search for elements
     * @param state           State value of the element
     * (<i>Possible values:</i> <b>ENABLED, DISABLED, SELECTED, NOT_SELECTED, VISIBLE, NOT_VISIBLE</b>)
     * @return True if element becomes a <b>state</b>, otherwise false
     */
    @When("I wait `$duration` with `$pollingDuration` polling until element located `$locator` becomes $state")
    public boolean waitDurationWithPollingDurationTillElementState(Duration duration, Duration pollingDuration,
            Locator locator, State state)
    {
        return waitActions.wait(getSearchContext(), duration, pollingDuration,
                state.getExpectedCondition(expectedSearchActionsConditions, locator)).isWaitPassed();
    }

    /**
     * Waits for <b><i>an element</i></b> with the specified <b>tag</b> and <b>attribute type and value</b>
     * disappearance in the specified search context
     * <p>
     * Actions performed at this step:
     * </p>
     * <ul>
     * <li><b>Searches</b> for an element with the <b>specified attribute and tag</b> within search context</li>
     * <li><i>If there are <b>no elements</b> by specified attribute and tag</i>, step provides passed assertion and
     * don't check disappearance of an element</li>
     * <li><i>If <b>there are elements</b> by specified attribute and tag, </i><b>waits</b> until this element becomes
     * not visible</li>
     * </ul>
     * @param elementTag Type of the html tag (for ex. &lt;div&gt;, &lt;iframe&gt;)
     * @param attributeType Type of the tag attribute (for ex. 'name', 'id')
     * @param attributeValue Value of the attribute
     */
    @When("I wait until an element with the tag '$elementTag' and attribute"
            + " '$attributeType'='$attributeValue' disappears")
    public void waitTillElementDisappears(String elementTag, String attributeType, String attributeValue)
    {
        String elementXpath = LocatorUtil.getXPathByTagNameAndAttribute(elementTag, attributeType, attributeValue);
        Locator locator = new Locator(WebLocatorType.XPATH, elementXpath);
        List<WebElement> elements = searchActions.findElements(getSearchContext(), locator);
        if (!elements.isEmpty())
        {
            waitActions.wait(getWebDriver(), State.NOT_VISIBLE.getExpectedCondition(elements.get(0)));
        }
        else
        {
            String assertionDescription = "There is no element present with the tag '%s' and attribute '%s'='%s'";
            String description = String.format(assertionDescription, elementTag, attributeType, attributeValue);
            softAssert.recordPassedAssertion(description);
        }
    }

    /**
     * Waits for <b><i>an element</i></b> with the specified <b>tag</b> and <b>attribute type and value</b>
     * appearance in the specified search context
     * <p>
     * Actions performed at this step:
     * </p>
     * <ul>
     * <li><i>Finds</i> an element with the <b>specified attribute and tag</b> within search context
     * <li><i>Waits</i> until this element becomes visible
     * </ul>
     * @param elementTag Type of the html tag (for ex. &lt;div&gt;, &lt;iframe&gt;)
     * @param attributeType Type of the tag attribute (for ex. 'name', 'id')
     * @param attributeValue Value of the attribute
     */
    @When("I wait until an element with the tag '$elementTag' and attribute"
            + " '$attributeType'='$attributeValue' appears")
    public void waitTillElementAppears(String elementTag, String attributeType, String attributeValue)
    {
        waitForElementAppearance(getSearchContext(),
                By.xpath(LocatorUtil.getXPathByTagNameAndAttribute(elementTag, attributeType, attributeValue)));
    }

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
    @When("I wait until state of element located `$locator` is $state")
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
    @When("I wait until element located `$locator` is stale")
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
    @When("I wait until element located `$locator` contains text '$text'")
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
        WebDriver searchContext = uiContext.getSearchContext(WebDriver.class);
        waitForElementAppearance(searchContext, LocatorUtil
                .getXPathLocator("*[(local-name()='frame' or local-name()='iframe') and @*='%s']", frameName));
    }

    /**
     * Waits for <b><i>an element</i></b> with the specified <b>id</b> disappearance
     * in the specified search context
     * @param id Value of the 'id' attribute of the element
     */
    @Then("an element with the id '$id' disappears")
    public void elementByIdDisappears(String id)
    {
        By locator = By.xpath(LocatorUtil.getXPathByAttribute("id", id));
        waitActions.wait(getSearchContext(),
                State.NOT_VISIBLE.getExpectedCondition(expectedSearchContextConditions, locator));
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
     * Waits for element disappearance with desired timeout in seconds
     * @param locator The locating mechanism to use
     * @param timeout Desired timeout
     * @return true if element disappeared, false otherwise
     */
    @Then("element located '$locator' disappears in '$timeout'")
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
     * <br>Given I am on a page with the URL 'https://example.com/'
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
