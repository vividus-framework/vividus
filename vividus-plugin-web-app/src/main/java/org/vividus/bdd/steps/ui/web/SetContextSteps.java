/*
 * Copyright 2019 the original author or authors.
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

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.not;
import static org.vividus.ui.validation.matcher.WebElementMatchers.elementNumber;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.hamcrest.Matcher;
import org.jbehave.core.annotations.When;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.vividus.bdd.monitor.TakeScreenshotOnFailure;
import org.vividus.bdd.steps.ui.web.validation.IBaseValidations;
import org.vividus.bdd.steps.ui.web.validation.IHighlightingSoftAssert;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.ui.web.State;
import org.vividus.ui.web.action.IWindowsActions;
import org.vividus.ui.web.action.SearchActions;
import org.vividus.ui.web.action.WebElementActions;
import org.vividus.ui.web.action.search.ActionAttributeType;
import org.vividus.ui.web.action.search.SearchAttributes;
import org.vividus.ui.web.action.search.Visibility;
import org.vividus.ui.web.context.IWebUiContext;
import org.vividus.ui.web.util.LocatorUtil;

@SuppressWarnings("checkstyle:methodcount")
@TakeScreenshotOnFailure
public class SetContextSteps
{
    private static final String THE_FOUND_ELEMENT_IS = "The found element is ";
    private static final String POP_UP_CSS_SELECTOR = "*[style*='z-index:']";
    private static final String AN_ELEMENT_WITH_THE_ATTRIBUTE = "An element with the attribute '%1$s'='%2$s'";
    private static final String AN_ELEMENT_WITH_THE_NAME = "An element with the name '%1$s'";

    @Inject private IWebDriverProvider webDriverProvider;
    @Inject private IWebUiContext webUiContext;
    @Inject private IBaseValidations baseValidations;
    @Inject private SearchActions searchActions;
    @Inject private WebElementActions webElementActions;
    @Inject private IHighlightingSoftAssert highlightingSoftAssert;
    @Inject private IWindowsActions windowsActions;

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
     * Set the context to the <b>row containing cell with the particular <i>text</i></b>in a table
     * <p>
     * <b>Cell</b> is a {@literal
     * <td>
     * } tag in a <i>row</i> tag.
     * <b>Row</b> is a {@literal
     * <tr>
     * } tag in a {@literal
     * <table>
     * } tag
     * <p>
     * @param cellText is the text in a table cell
     */
    @When("I change context to the table row containing cell with the text '$text'")
    public void changeContextToRowContainingCellWithTextInTable(String cellText)
    {
        changeContextToPage();
        SearchAttributes attributes = new SearchAttributes(ActionAttributeType.TAG_NAME, "td").addFilter(
                ActionAttributeType.CASE_SENSITIVE_TEXT, cellText);
        WebElement td = baseValidations.assertIfElementExists(String.format("Cell with the text '%s'", cellText),
                attributes);
        WebElement row = baseValidations.assertIfElementExists(
                String.format("Table row containing cell with the text '%s'", cellText), td,
                new SearchAttributes(ActionAttributeType.XPATH, "./ancestor::tr[1]"));
        webUiContext.putSearchContext(row, () -> changeContextToRowContainingCellWithTextInTable(cellText));
    }

    /**
     * Set the context for further localization of elements to an <b>element</b> specified by <b>locator</b>
     * @param searchAttributes Locator used to find an element
     */
    @When("I change context to an element by $locator")
    public void changeContextToElement(SearchAttributes searchAttributes)
    {
        changeContextToPage();
        WebElement element = baseValidations.assertIfElementExists("Element to set context", searchAttributes);
        webUiContext.putSearchContext(element, () -> changeContextToElement(searchAttributes));
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
        WebElement element = baseValidations.assertIfElementExists(String.format(AN_ELEMENT_WITH_THE_NAME, name),
                new SearchAttributes(ActionAttributeType.ELEMENT_NAME, name).addFilter(ActionAttributeType.STATE,
                        state.toString()));
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
        if (!baseValidations.assertElementState(THE_FOUND_ELEMENT_IS + state, state, element))
        {
            element = null;
        }
        webUiContext.putSearchContext(element, () -> changeContextToStateElementWithAttribute(state,
                attributeType, attributeValue));
    }

    /**
     * Set the context to a pop-up
     * A <b>pop-up</b> is an element which is displayed on the top of any other element
     * (it has the highest z-index in its CSS style value).
     */
    @When("I change context to a pop-up")
    public void changeContextToAPopUp()
    {
        changeContextToPage();
        List<WebElement> popUpList = searchActions.findElements(webUiContext.getSearchContext(),
                By.cssSelector(POP_UP_CSS_SELECTOR));
        List<WebElement> popUps = new ArrayList<>();
        int maxZ = Integer.MIN_VALUE;
        for (WebElement popUp : popUpList)
        {
            int currentZ = Integer.parseInt(webElementActions.getCssValue(popUp, "z-index"));
            if (currentZ > maxZ)
            {
                popUps.clear();
                popUps.add(popUp);
                maxZ = currentZ;
            }
            else if (currentZ == maxZ)
            {
                popUps.add(popUp);
            }
        }
        if (baseValidations.assertElementNumber("The number of Pop-ups on the page",
                String.format("The number of elements found by css selector '%s'", POP_UP_CSS_SELECTOR), popUps, 1))
        {
            webUiContext.putSearchContext(popUps.get(0), this::changeContextToAPopUp);
        }
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
     * @see <a href="http://www.w3schools.com/tags/default.asp"><i>HTML Element Reference</i></a>
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
        switchingToDefault();
        WebElement frame = baseValidations.assertIfElementExists(
                String.format("Frame with the attribute '%1$s'='%2$s'", attributeType, attributeValue),
                ElementPattern.getFrameSearchAttributes(attributeType, attributeValue));
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
     * @see <a href="http://www.w3schools.com/tags/default.asp"><i>HTML Element Reference</i></a>
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
        switchingToDefault();
        SearchAttributes frameSearchAttributes = ElementPattern.getFrameSearchAttributes(attributeType, attributeValue);
        frameSearchAttributes.getSearchParameters().setVisibility(Visibility.ALL);
        List<WebElement> frames = searchActions.findElements(getSearchContext(), frameSearchAttributes);
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
     * @see <a href="http://www.w3schools.com/tags/default.asp"><i>HTML Element Reference</i></a>
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
     * corresponding <b>Document</b> object, which itself is a html page.
     * </p>
     * <b>WindowName</b> references to the page title, which is set by {@code <title>} tag.
     * <p>
     * Actions performed at this step:
     * <ul>
     * <li>Searches among currently opened windows (and tabs) for a window with the specified <b>windowName</b>.</li>
     * <li>If such window is found switches focus to it. If window is not found current focus stays unchanged;</li>
     * </ul>
     * @param windowName Value of the {@code <title>} tag of a desired window
     * @see <a href="https://html.spec.whatwg.org/#browsing-context"><i>Browsing context (Window &amp; Document)</i></a>
     * @see <a href="http://www.w3schools.com/tags/default.asp"><i>HTML Element Reference</i></a>
     */
    @When("I switch to a window with the name '$windowName'")
    public void switchingToWindow(String windowName)
    {
        switchToWindow(equalTo(windowName));
    }

    /**
     * Switch the focus of future browser commands to the new <b>window object</b> with the specified part of
     * <b>windowName</b>.
     * <p>
     * Each browser <b>window</b> or <b>tab</b> is considered to be a separate <b>window object</b>. This object holds
     * corresponding <b>Document</b> object, which itself is a html page.
     * </p>
     * <b>WindowName</b> references to the page title, which is set by {@code <title>} tag.
     * <p>
     * Actions performed at this step:
     * <ul>
     * <li>Searches among currently opened windows (and tabs) for a window which has the specified part of the
     * <b>windowName</b>.</li>
     * <li>If such window is found switches focus to it. If window is not found current focus stays unchanged;</li>
     * </ul>
     * @param windowPartName Any part of the value of the {@code <title>} tag of a desired window
     * @see <a href="https://html.spec.whatwg.org/#browsing-context"><i>Browsing context (Window &amp; Document)</i></a>
     * @see <a href="http://www.w3schools.com/tags/default.asp"><i>HTML Element Reference</i></a>
     */
    @When("I switch to a window with the name containing '$windowPartName'")
    public void switchingToWindowPartName(String windowPartName)
    {
        switchToWindow(containsString(windowPartName));
    }

    private void switchToWindow(Matcher<String> matcher)
    {
        String titleAfterSwitch = windowsActions.switchToWindowWithMatchingTitle(matcher);
        if (highlightingSoftAssert.assertThat("New window or browser tab name is ", "Window or tab name is ",
                titleAfterSwitch, matcher))
        {
            changeContextToPage();
        }
    }

    private void switchToFrame(WebElement frame)
    {
        if (frame != null)
        {
            getWebDriver().switchTo().frame(frame);
            changeContextToPage();
        }
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
