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
import org.vividus.bdd.monitor.TakeScreenshotOnFailure;
import org.vividus.bdd.steps.StringComparisonRule;
import org.vividus.bdd.steps.ui.GenericSetContextSteps;
import org.vividus.bdd.steps.ui.validation.IBaseValidations;
import org.vividus.bdd.steps.ui.validation.IDescriptiveSoftAssert;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.ui.State;
import org.vividus.ui.action.IWaitActions;
import org.vividus.ui.action.WaitResult;
import org.vividus.ui.action.search.Locator;
import org.vividus.ui.context.IUiContext;
import org.vividus.ui.web.action.IWindowsActions;
import org.vividus.ui.web.action.search.WebLocatorType;
import org.vividus.ui.web.util.LocatorUtil;

@TakeScreenshotOnFailure
public class SetContextSteps extends GenericSetContextSteps
{
    private static final String AN_ELEMENT_WITH_THE_ATTRIBUTE = "An element with the attribute '%1$s'='%2$s'";

    private final IWebDriverProvider webDriverProvider;
    private final IDescriptiveSoftAssert descriptiveSoftAssert;
    private final IWindowsActions windowsActions;
    private final IWaitActions waitActions;

    public SetContextSteps(IUiContext uiContext, IWebDriverProvider webDriverProvider, IBaseValidations baseValidations,
            IDescriptiveSoftAssert descriptiveSoftAssert, IWindowsActions windowsActions, IWaitActions waitActions)
    {
        super(uiContext, baseValidations);
        this.webDriverProvider = webDriverProvider;
        this.descriptiveSoftAssert = descriptiveSoftAssert;
        this.windowsActions = windowsActions;
        this.waitActions = waitActions;
    }

    /**
     * Set the context for further localization of elements to an <b>element</b> specified by the <b>name</b> and
     * <b>state</b>
     * @param name Any attribute or text value of the element
     * @param state A state value of the element
     * (<i>Possible values:</i> <b>ENABLED, DISABLED, SELECTED, NOT_SELECTED, VISIBLE, NOT_VISIBLE</b>)
    */
    @When("I change context to a [$state] element with the name '$name'")
    public void changeContextToElementWithName(State state, String name)
    {
        resetContext();
        WebElement element = getBaseValidations().assertIfElementExists(
                String.format("An element with the name '%1$s'", name),
                new Locator(WebLocatorType.ELEMENT_NAME, name)
                        .addFilter(WebLocatorType.STATE, state.toString()));
        getUiContext().putSearchContext(element, () -> changeContextToElementWithName(state, name));
    }

    /**
     * Set the context for further localization of elements to an <b>element</b>
     * with the specified <b>attribute</b>
     * @param attributeType A type of the element's attribute
     * @param attributeValue A value of the element's attribute
    */
    @When("I change context to an element with the attribute '$attributeType'='$attributeValue'")
    public void changeContextToElementWithAttribute(String attributeType, String attributeValue)
    {
        resetContext();

        WebElement element = getBaseValidations().assertIfElementExists(
                String.format(AN_ELEMENT_WITH_THE_ATTRIBUTE, attributeType, attributeValue),
                new Locator(WebLocatorType.XPATH,
                        LocatorUtil.getXPathByAttribute(attributeType, attributeValue)));
        getUiContext().putSearchContext(element, () -> changeContextToElementWithAttribute(attributeType,
                attributeValue));
    }

    /**
     * Set the context for further localization of elements to an <b>element</b>
     * with the specified <b>attribute</b> and <b>state</b>
     * @param attributeType A type of the element's attribute
     * @param attributeValue A value of the element's attribute
     * @param state A state value of the element
     * (<i>Possible values:</i> <b>ENABLED, DISABLED, SELECTED, NOT_SELECTED, VISIBLE, NOT_VISIBLE</b>)
    */
    @When("I change context to a [$state] element with the attribute '$attributeType'='$attributeValue'")
    public void changeContextToStateElementWithAttribute(State state, String attributeType, String attributeValue)
    {
        resetContext();
        WebElement element = getBaseValidations().assertIfElementExists(String.format(AN_ELEMENT_WITH_THE_ATTRIBUTE,
                attributeType, attributeValue), new Locator(WebLocatorType.XPATH,
                        LocatorUtil.getXPathByAttribute(attributeType, attributeValue)));
        if (!getBaseValidations().assertElementState("The found element is " + state, state, element))
        {
            element = null;
        }
        getUiContext().putSearchContext(element, () -> changeContextToStateElementWithAttribute(state,
                attributeType, attributeValue));
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
        getWebDriver().switchTo().defaultContent();
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
        WebElement element = getBaseValidations().assertIfElementExists("A frame", locator);
        switchToFrame(element);
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
     * windows #1, #2, #3 and window #2 is active one, using this method will switch focus to the window #3;
     * </ul>
     * @see <a href="https://html.spec.whatwg.org/#browsing-context"><i>Browsing context (Window &amp; Document)
     * reference</i></a>
     */
    @When("I switch to a new window")
    public void switchingToWindow()
    {
        String currentWindow = getWebDriver().getWindowHandle();
        String newWindow = windowsActions.switchToNewWindow(currentWindow);
        if (descriptiveSoftAssert.assertThat(String.format("New window '%s' is found", newWindow),
                "New window is found", newWindow, not(equalTo(currentWindow))))
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
    */
    @When("I switch to window with title that $stringComparisonRule `$windowName`")
    public void switchingToWindow(StringComparisonRule comparisonRule, String windowName)
    {
        Matcher<String> matcher = comparisonRule.createMatcher(windowName);
        String titleAfterSwitch = windowsActions.switchToWindowWithMatchingTitle(matcher);
        resetContextIf(() ->
            descriptiveSoftAssert.assertThat("New window or browser tab name is ", "Window or tab name is ",
                titleAfterSwitch, matcher));
    }

    /**
    * Wait for a window and switches the focus of future browser commands to the new
    * <b>window object</b> with the specified <b>title</b>.
    * <p>
    * Each browser <b>window</b> or <b>tab</b> is considered to be a separate <b>window object</b>. This object holds
    * corresponding <b>Document</b> object, which itself is a HTML page.
    * </p>
    * <b>Title</b> references to the page title, which is set by {@code <title>} tag.
    * <p>
    * Actions performed at this step:
    * <ul>
    * <li>Searches among currently opened windows (and tabs) for a window with the specified <b>windowName</b>.</li>
    * <li>If such window is found switches focus to it. If window is not found current focus stays unchanged;</li>
    * </ul>
    * @param comparisonRule is equal to, contains, does not contain
    * @param title Value of the {@code <title>} tag of a desired window
    * @param duration in format <a href="https://en.wikipedia.org/wiki/ISO_8601">Duration Format</a>
    * @see <a href="https://html.spec.whatwg.org/#browsing-context"><i>Browsing context (Window &amp; Document)</i></a>
    * @see <a href="https://www.w3schools.com/tags/default.asp"><i>HTML Element Reference</i></a>
    */
    @When("I wait `$duration` until window with title that $comparisonRule `$title` appears and switch to it")
    public void waitForWindowAndSwitch(Duration duration, StringComparisonRule comparisonRule, String title)
    {
        Matcher<String> expected = comparisonRule.createMatcher(title);
        WaitResult<Boolean> result = waitActions.wait(webDriverProvider.get(), duration, new ExpectedCondition<>()
        {
            @Override
            public Boolean apply(WebDriver driver)
            {
                try
                {
                    return expected.matches(windowsActions.switchToWindowWithMatchingTitle(expected));
                }
                catch (WebDriverException webDriverException)
                {
                    return false;
                }
            }

            @Override
            public String toString()
            {
                return String.format("switch to a window where title %s \"%s\"", comparisonRule, title);
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

    private void switchToFrame(WebElement frame)
    {
        if (frame != null)
        {
            getWebDriver().switchTo().frame(frame);
        }
    }

    private WebDriver getWebDriver()
    {
        return webDriverProvider.get();
    }
}
