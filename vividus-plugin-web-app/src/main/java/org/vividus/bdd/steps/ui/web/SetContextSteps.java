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
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.not;
import static org.vividus.ui.validation.matcher.WebElementMatchers.elementNumber;

import java.time.Duration;
import java.util.List;
import java.util.function.BooleanSupplier;

import javax.inject.Inject;

import org.hamcrest.Matcher;
import org.jbehave.core.annotations.When;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.vividus.bdd.monitor.TakeScreenshotOnFailure;
import org.vividus.bdd.steps.StringComparisonRule;
import org.vividus.bdd.steps.ui.web.validation.IBaseValidations;
import org.vividus.bdd.steps.ui.web.validation.IHighlightingSoftAssert;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.ui.web.State;
import org.vividus.ui.web.action.IWaitActions;
import org.vividus.ui.web.action.IWindowsActions;
import org.vividus.ui.web.action.SearchActions;
import org.vividus.ui.web.action.WaitResult;
import org.vividus.ui.web.action.search.ActionAttributeType;
import org.vividus.ui.web.action.search.SearchAttributes;
import org.vividus.ui.web.action.search.Visibility;
import org.vividus.ui.web.context.IWebUiContext;
import org.vividus.ui.web.util.LocatorUtil;

@TakeScreenshotOnFailure
public class SetContextSteps
{
    private static final String AN_ELEMENT_WITH_THE_ATTRIBUTE = "An element with the attribute '%1$s'='%2$s'";

    @Inject private IWebDriverProvider webDriverProvider;
    @Inject private IWebUiContext webUiContext;
    @Inject private IBaseValidations baseValidations;
    @Inject private SearchActions searchActions;
    @Inject private IHighlightingSoftAssert highlightingSoftAssert;
    @Inject private IWindowsActions windowsActions;
    @Inject private IWaitActions waitActions;

    /**
     * Set the context for further localization of elements to the <b>page</b> itself
     * <p>
     * Actions performed at this step:
     * <ul>
     * <li>Switches focus to the root tag of the page, this is as a rule {@code <html>} tag.
     * </ul>
     */
    @When("I change context to the page")
    public void changeContextToPage()
    {
        webUiContext.reset();
    }

    /**
     * Set the context for further localization of elements to an <b>element</b> specified by <b>locator</b>
     * @param locator Locator used to find an element
     */
    @When("I change context to element located `$locator`")
    public void changeContextToElement(SearchAttributes locator)
    {
        changeContextToPage();
        WebElement element = baseValidations.assertIfElementExists("Element to set context", locator);
        webUiContext.putSearchContext(element, () -> changeContextToElement(locator));
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
        changeContextToPage();
        WebElement element = baseValidations.assertIfElementExists(
                String.format("An element with the name '%1$s'", name),
                new SearchAttributes(ActionAttributeType.ELEMENT_NAME, name)
                        .addFilter(ActionAttributeType.STATE, state.toString()));
        webUiContext.putSearchContext(element, () -> changeContextToElementWithName(state, name));
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
        changeContextToPage();

        WebElement element = baseValidations.assertIfElementExists(
                String.format(AN_ELEMENT_WITH_THE_ATTRIBUTE, attributeType, attributeValue),
                new SearchAttributes(ActionAttributeType.XPATH,
                        LocatorUtil.getXPathByAttribute(attributeType, attributeValue)));
        webUiContext.putSearchContext(element, () -> changeContextToElementWithAttribute(attributeType,
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
        changeContextToPage();
        WebElement element = baseValidations.assertIfElementExists(String.format(AN_ELEMENT_WITH_THE_ATTRIBUTE,
                attributeType, attributeValue), new SearchAttributes(ActionAttributeType.XPATH,
                        LocatorUtil.getXPathByAttribute(attributeType, attributeValue)));
        if (!baseValidations.assertElementState("The found element is " + state, state, element))
        {
            element = null;
        }
        webUiContext.putSearchContext(element, () -> changeContextToStateElementWithAttribute(state,
                attributeType, attributeValue));
    }

    /**
     * Switching to the frame with specified <b>attribute type</b> and <b>attribute value</b>
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
     * @param attributeType attribute type of {@code <iframe>} tag
     * @param attributeValue attribute value of the specified attributeType
     * <p>
     * <b>Example:</b>
     * <pre>
     * {@code <iframe attributeType=}<b>'attributeValue'</b>{@code >some iframe content</iframe>}
     * </pre>
     */
    @When("I switch to a frame with the attribute '$attributeType'='$attributeValue'")
    public void switchingToFrame(String attributeType, String attributeValue)
    {
        changeContextToPage();
        WebElement frame = baseValidations.assertIfElementExists(
                String.format("Frame with the attribute '%1$s'='%2$s'", attributeType, attributeValue),
                getFrameSearchAttributes(attributeType, attributeValue));
        switchToFrame(frame);
    }

    /**
     * Switching to the frame with specified <b>attribute type</b> and <b>attribute value</b> by frame index
     * <p>
     * A <b>frame</b> is used for splitting browser page into several segments, each of which can show a different
     * document (content). This enables updates of parts of a website while the user browses without making them reload
     * the whole page (this is now largely replaced by AJAX).
     * <p>
     * <b>Frames</b> are located inside {@code <iframe>} tag.
     * <p>
     * Actions performed at this step:
     * <ul>
     * <li>Finds the frames with desired parameters;
     * <li>If frame is found, switches focus to it.
     * </ul>
     * @see <a href="https://en.wikipedia.org/wiki/HTML_element#Frames"><i>Frames</i></a>
     * @see <a href="https://www.w3schools.com/tags/default.asp"><i>HTML Element Reference</i></a>
     * @param numberValue index of the frame, starts from 1. So if there are two frames found the second one will have
     * numberValue = 2
     * @param attributeType attribute type of {@code <iframe>} tag
     * @param attributeValue attribute value of the specified attributeType
     * <p>
     * <b>Example:</b>
     * <pre>
     * {@code <iframe attributeType=}<b>'attributeValue'</b>{@code >some iframe content</iframe>}
     * </pre>
     */
    @When("I switch to a frame number '$numberValue' with the attribute '$attributeType'='$attributeValue'")
    public void switchToFrame(int numberValue, String attributeType, String attributeValue)
    {
        changeContextToPage();
        SearchAttributes frameSearchAttributes = getFrameSearchAttributes(attributeType, attributeValue);
        frameSearchAttributes.getSearchParameters().setVisibility(Visibility.ALL);
        List<WebElement> frames = searchActions.findElements(webUiContext.getSearchContext(), frameSearchAttributes);
        if (highlightingSoftAssert.assertThat(
                "Number of frames found", String.format("Frame with attribute %1$s = %2$s and number %3$s is found",
                        attributeType, attributeValue, numberValue),
                frames, elementNumber(greaterThanOrEqualTo(numberValue))))
        {
            switchToFrame(frames.get(numberValue - 1));
        }
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
        changeContextToPage();
        getWebDriver().switchTo().defaultContent();
    }

    /**
     * Switching to the frame with xpath
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
     * @param xpathExpression 'xpath' expression to locate frame element
     * <p>
     * <b>Example:</b>
     * <pre>
     * {@code <iframe attributeType=}<b>'attributeValue'</b>{@code > some iframe content</iframe>}
     * </pre>
     */
    @When("I switch to a frame by the xpath '$xpath'")
    public void switchingToFramebyXpath(String xpathExpression)
    {
        changeContextToPage();
        WebElement element = baseValidations.assertIfElementExists("A frame",
                new SearchAttributes(ActionAttributeType.XPATH, LocatorUtil.getXPath(xpathExpression)));
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
        if (highlightingSoftAssert.assertThat(String.format("New window '%s' is found", newWindow),
                "New window is found", newWindow, not(equalTo(currentWindow))))
        {
            changeContextToPage();
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
            highlightingSoftAssert.assertThat("New window or browser tab name is ", "Window or tab name is ",
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
            changeContextToPage();
        }
    }

    private void switchToFrame(WebElement frame)
    {
        if (frame != null)
        {
            getWebDriver().switchTo().frame(frame);
        }
    }

    private static SearchAttributes getFrameSearchAttributes(String attributeType, String attributeValue)
    {
        String xPath = LocatorUtil.getXPath(
                ".//*[(local-name()='frame' or local-name()='iframe') and @" + attributeType + "=%s]", attributeValue);
        return new SearchAttributes(ActionAttributeType.XPATH, xPath);
    }

    private WebDriver getWebDriver()
    {
        return webDriverProvider.get();
    }
}
