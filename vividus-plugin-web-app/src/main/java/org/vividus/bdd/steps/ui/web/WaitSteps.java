/*
 * Copyright 2019-2020 the original author or authors.
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
import java.util.function.Function;

import javax.inject.Inject;

import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.vividus.bdd.monitor.TakeScreenshotOnFailure;
import org.vividus.bdd.steps.ui.web.validation.IBaseValidations;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.softassert.ISoftAssert;
import org.vividus.ui.web.State;
import org.vividus.ui.web.action.IExpectedConditions;
import org.vividus.ui.web.action.INavigateActions;
import org.vividus.ui.web.action.ISearchActions;
import org.vividus.ui.web.action.IWaitActions;
import org.vividus.ui.web.action.WaitResult;
import org.vividus.ui.web.action.search.ActionAttributeType;
import org.vividus.ui.web.action.search.SearchAttributes;
import org.vividus.ui.web.context.IWebUiContext;
import org.vividus.ui.web.util.LocatorUtil;

@TakeScreenshotOnFailure
public class WaitSteps
{
    private static final long DIVISOR = 10;

    @Inject private IWebDriverProvider webDriverProvider;
    @Inject private IWaitActions waitActions;
    @Inject private ISearchActions searchActions;
    @Inject private INavigateActions navigateActions;
    @Inject private IWebUiContext webUiContext;
    @Inject private ISoftAssert softAssert;
    @Inject private IExpectedConditions<By> expectedSearchContextConditions;
    @Inject private IExpectedConditions<SearchAttributes> expectedSearchActionsConditions;
    @Inject private IBaseValidations baseValidations;

    public boolean waitTillElementWithTextDisappearsPageRefresh(String text, Duration timeout)
    {
        WebDriver driver = webDriverProvider.get();
        return waitActions.wait(driver, timeout,
                getFunction(false, text, "Waiting disappearance of all elements with text: ")).isWaitPassed();
    }

    public boolean waitTillElementWithTextAppearsPageRefresh(String text, Duration timeout)
    {
        WebDriver driver = webDriverProvider.get();
        return waitActions.wait(driver, timeout,
                getFunction(true, text, "Waiting presence of any element with text: ")).isWaitPassed();
    }

    /**
     * Waits <b>duration</b> with <b>pollingDuration</b> until <b>an element</b> by the specified <b>locator</b>
     * appears in the specified search context
     * <p>
     * Actions performed at this step:
     * </p>
     * <ul>
     * <li><i>Finds</i> an element by the specified <b>locator</b> within search context
     * <li><i>Waits</i> <b>duration</b> with <b>pollingDuration</b> until this element becomes visible
     * </ul>
     * @param duration Total waiting time for an element becomes visible
     * @param pollingDuration Defines the timeout between attempts
     * @param locator Locator to search for elements
     * @return True if element appears, otherwise false
     */
    @When("I wait '$duration' with '$pollingDuration' polling until an element located by $locator appears")
    public boolean waitDurationWithPollingDurationTillElementAppears(Duration duration, Duration pollingDuration,
            SearchAttributes locator)
    {
        return waitActions.wait(getSearchContext(), duration, pollingDuration,
                expectedSearchActionsConditions.visibilityOfAllElementsLocatedBy(locator)).isWaitPassed();
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
        SearchAttributes attributes = new SearchAttributes(ActionAttributeType.XPATH, elementXpath);
        String assertionDescription = "with the tag '%s' and attribute '%s'='%s'";
        waitForElementDisappearance(attributes, assertionDescription, elementTag, attributeType, attributeValue);
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
    public void waitTillElementIsSelected(SearchAttributes locator, State state)
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
    public void waitTillElementIsStale(SearchAttributes locator)
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
    public void waitTillElementContainsText(SearchAttributes locator, String text)
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
     * Waits for <b>the appearance</b> of an element with the specified <b>name</b>
     * in the specified search context
     * <p>
     * Actions performed at this step:
     * </p>
     * <ul>
     * <li><i>Finds</i> an element with the <b>specified name</b> within search context
     * <li><i>Waits</i> until this element becomes visible
     * </ul>
     * @param elementName Any attribute value or text value of the element
     */
    @When("I wait until elements with the name '$elementName' appear")
    public void waitTillElementsAreVisible(String elementName)
    {
        SearchAttributes attributes = new SearchAttributes(ActionAttributeType.ELEMENT_NAME, elementName);
        waitForElementAppearance(attributes);
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
        WebDriver searchContext = webUiContext.getSearchContext(WebDriver.class);
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
     * Checks that element exists during the timeout
     * @param seconds timeout in seconds
     * @param xpath XPath value of the element
     */
    @Then("the element with the xpath '$xpath' exists for '$seconds' seconds")
    public void doesElementExistsForTimePeriod(String xpath, long seconds)
    {
        By elementXpath = LocatorUtil.getXPathLocator(xpath);
        WaitResult<Boolean> result = waitActions.wait(getSearchContext(), Duration.ofSeconds(seconds),
                expectedSearchContextConditions.not(
                        expectedSearchContextConditions.presenceOfAllElementsLocatedBy(elementXpath)), false);
        softAssert.assertFalse(String.format("Element with xpath '%s' has existed during '%d' seconds",
                    xpath, seconds), result.isWaitPassed());
    }

    @Then("alert does not appear in `$timeout`")
    public boolean waitAlertDoesNotAppear(Duration timeout)
    {
        WaitResult<Alert> wait = waitActions.wait(getWebDriver(), timeout, timeout.dividedBy(DIVISOR), alertIsPresent(),
                false);
        return softAssert.assertFalse("Alert does not appear", wait.isWaitPassed());
    }

    /**
     * Waits for element disappearance with timeout
     * @param searchContext Search context
     * @param by The locating mechanism to use
     * @return true if element disappeared, false otherwise
     */
    public boolean waitForElementDisappearance(SearchContext searchContext, By by)
    {
        return waitActions
                .wait(searchContext, State.NOT_VISIBLE.getExpectedCondition(expectedSearchContextConditions, by))
                .isWaitPassed();
    }

    /**
     * Waits for element disappearance with desired timeout in seconds
     * @param searchContext Search context
     * @param by The locating mechanism to use
     * @param timeout Desired timeout
     * @return true if element disappeared, false otherwise
     */
    public boolean waitForElementDisappearance(SearchContext searchContext, By by, Duration timeout)
    {
        return waitActions.wait(searchContext, timeout,
                State.NOT_VISIBLE.getExpectedCondition(expectedSearchContextConditions, by)).isWaitPassed();
    }

    /**
     * Waits for element disappearance with desired timeout in seconds
     * @param locator The locating mechanism to use
     * @param timeout Desired timeout
     * @return true if element disappeared, false otherwise
     */
    @Then("element located '$locator' disappears in '$timeout'")
    public boolean waitForElementDisappearance(SearchAttributes locator, Duration timeout)
    {
        return waitActions.wait(getSearchContext(), timeout,
                expectedSearchActionsConditions.invisibilityOfElement(locator)).isWaitPassed();
    }

    public boolean waitForElementAppearance(SearchContext searchContext, By by)
    {
        return waitActions.wait(searchContext,
                expectedSearchContextConditions.visibilityOfAllElementsLocatedBy(by)).isWaitPassed();
    }

    /**
     * Waits for <b><i>an element</i></b> with the specified <b>locator</b> appearance
     * in the specified search context
     * <p>
     * Actions performed at this step:
     * </p>
     * <ul>
     * <li><i>Finds</i> an element with the <b>specified locator</b> within search context
     * <li><i>Waits</i> until this element becomes visible
     * </ul>
     * @param locator to locate element
     * @return true if element appeared, false otherwise
     */
    @When("I wait until element located `$locator` appears")
    public boolean waitForElementAppearance(SearchAttributes locator)
    {
        return waitActions.wait(getSearchContext(),
                expectedSearchActionsConditions.visibilityOfElement(locator)).isWaitPassed();
    }

    /**
     * Waits for element disappearance with timeout
     * @param locator The locating mechanism to use
     * @return true if element disappeared, false otherwise
     */
    @When("I wait until element located `$locator` disappears")
    public boolean waitForElementDisappearance(SearchAttributes locator)
    {
        return waitActions.wait(getSearchContext(), expectedSearchActionsConditions.invisibilityOfElement(locator))
                .isWaitPassed();
    }

    /**
     * Waits for element presence
     * @param searchContext Search context
     * @param by The locating mechanism to use
     * @return true if element is presented, false otherwise
     */
    public boolean waitForElementPresence(SearchContext searchContext, By by)
    {
        return waitActions.wait(searchContext, expectedSearchContextConditions.presenceOfAllElementsLocatedBy(by))
                .isWaitPassed();
    }

    private Function<WebDriver, Boolean> getFunction(boolean displayed, String textToFind, String message)
    {
        return new Function<>()
        {
            @Override
            public Boolean apply(WebDriver driver)
            {
                navigateActions.refresh(driver);
                SearchAttributes attributes = new SearchAttributes(ActionAttributeType.CASE_SENSITIVE_TEXT, textToFind);
                List<WebElement> elements = searchActions.findElements(webUiContext.getSearchContext(WebDriver.class),
                        attributes);
                return displayed == !elements.isEmpty();
            }

            @Override
            public String toString()
            {
                return message + textToFind;
            }
        };
    }

    private void waitForElementDisappearance(SearchAttributes attributes, String assertionDescription,
            Object... specifiedValues)
    {
        List<WebElement> elements = searchActions.findElements(getSearchContext(), attributes);
        if (!elements.isEmpty())
        {
            waitActions.wait(getWebDriver(), State.NOT_VISIBLE.getExpectedCondition(elements.get(0)));
            return;
        }
        softAssert.recordPassedAssertion(
                "There is no element present ".concat(String.format(assertionDescription, specifiedValues)));
    }

    private WebDriver getWebDriver()
    {
        return webDriverProvider.get();
    }

    private SearchContext getSearchContext()
    {
        return webUiContext.getSearchContext();
    }
}
