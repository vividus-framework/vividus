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

import java.util.List;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.vividus.bdd.monitor.TakeScreenshotOnFailure;
import org.vividus.bdd.steps.ui.web.validation.IBaseValidations;
import org.vividus.bdd.steps.ui.web.validation.IElementValidations;
import org.vividus.ui.web.State;
import org.vividus.ui.web.action.IClickActions;
import org.vividus.ui.web.action.IMouseActions;
import org.vividus.ui.web.action.ISearchActions;
import org.vividus.ui.web.action.search.ActionAttributeType;
import org.vividus.ui.web.action.search.SearchAttributes;
import org.vividus.ui.web.context.IWebUiContext;
import org.vividus.ui.web.util.LocatorUtil;

@TakeScreenshotOnFailure
public class ButtonSteps
{
    private static final String RADIO_BUTTON = "Radio button";

    @Inject private IWebUiContext webUiContext;
    @Inject private ISearchActions searchActions;
    @Inject private IClickActions clickActions;
    @Inject private IBaseValidations baseValidations;
    @Inject private IElementValidations elementValidations;
    @Inject private IMouseActions mouseActions;

    /**
     * Sets a cursor on the <b>button</b> specified by the 'name' attribute
     * <p>Actions performed at this step:</p>
     * <ul>
     * <li>Finds a <b>button</b>;</li>
     * <li>Sets a cursor on the <b>button</b>;</li>
     * </ul>
     * @param buttonName Any attribute value or a <b>button's</b> text.
     * <b>Example:</b>
     * <pre>
     * {@code <div id=}<b>'sectionName'</b>{@code >
     *      <div class='someClass'>
     *          <h2>Log In</h2>
     *      </div>
     *      <button name = }<b>'buttonName'</b>{@code />
     * </div>}</pre>
     * @see <a href="http://www.w3schools.com/tags/default.asp"><i>HTML Element Reference</i></a>
     */
    @When("I hover a mouse over a button with the name '$buttonName'")
    public void mouseOverButton(String buttonName)
    {
        WebElement button = ifButtonWithNameExists(buttonName);
        mouseActions.moveToElement(button);
    }

    /**
     * Clicks on a <b>button</b> with the specified name in the search context
     * <p>Actions performed at this step:</p>
     * <ul>
     * <li>Finds a <b>button</b> with the specified name in the search context
     * <li>Click on the found <b>button</b>;</li>
     * </ul>
     * @param buttonName Any attribute value or button text
     */
    @When("I click on a button with the name '$buttonName'")
    public void clickButtonWithName(String buttonName)
    {
        WebElement button = ifButtonWithNameExists(buttonName);
        clickActions.click(button);
    }

    /**
     * Clicks on a button with the image having the particular URL in the <i>'src'</i> attribute
     * <p>Actions performed at this step:</p>
     * <ul>
     * <li>Verifies, that there is <b>exactly one</b> button with the image which
     * <i>'src'</i> attribute value equals to;</li>
     * <li>Click on the found <b>button</b>;</li>
     * </ul>
     * @param imageSrc 'src' attribute value of the image element;
     * @see <a href="http://www.w3schools.com/tags/default.asp"><i>HTML Element Reference</i></a>
     */
    @When("I click on a button with the image src '$imageSrc'")
    public void clickButtonWithImageSrc(String imageSrc)
    {
        WebElement button = isButtonWithImageSrcFound(imageSrc);
        clickActions.click(button);
    }

    /**
     * Checks that previously set searchContext contains a button with the
     * <b>name</b> and has expected State
     * <p>Actions performed at this step: </p>
     * <ul>
     * <li>Checks that the <b>button</b> specified by <b>name</b> exists in context;</li>
     * <li>Compares an actual button 'state' with expected;</li>
     * </ul>
     * @param state A state value of the link
     * (<i>Possible values:</i> <b>ENABLED, DISABLED, SELECTED, NOT_SELECTED, VISIBLE, NOT_VISIBLE</b>)
     * @param buttonName Any attribute value of the button tag and expected State
     */
    @Then("a [$state] button with the name '$buttonName' exists")
    public void ifButtonWithNameExists(State state, String buttonName)
    {
        WebElement webElement = ifButtonWithNameExists(buttonName);
        baseValidations.assertElementState("The found button is " + state, state, webElement);
    }

    /**
     * Checks that the <b>button</b> specified by <b>name</b> exists in context
     * @param buttonName Any attribute or text value of the button
     * @return <b>WebElement</b> An element (button) matching the name,
     * <b> null</b> - if there are no desired elements
    */
    @Then("a button with the name '$buttonName' exists")
    public WebElement ifButtonWithNameExists(String buttonName)
    {
        List<WebElement> elements = searchActions.findElements(getSearchContext(),
                new SearchAttributes(ActionAttributeType.BUTTON_NAME, buttonName));
        String description = String.format("There are %s buttons with the name '%s' in the %s", elements.size(),
                buttonName, getSearchContext());
        return baseValidations.assertIfElementExists("A button with the name", description, elements);
    }

    /**
     * Checks that the <b>button</b> specified by <b>name</b> does not exist in context
     * @param buttonName Any attribute or text value of the button
    */
    @Then("a button with the name '$buttonName' does not exist")
    public void doesNotButtonExist(String buttonName)
    {
        baseValidations.assertIfElementDoesNotExist(String.format("A button with the name '%s'", buttonName),
                "An element with <button> tag or <input> tag with attribute type = button| reset| submit",
                new SearchAttributes(ActionAttributeType.BUTTON_NAME, buttonName));
    }

    /**
     * Checks that previously set searchContext contains <b>exactly one</b> button image
     * with the particular URL in the <i>'src'</i> attribute and the specified
     * <i>'title'</i> attribute value.
     * <p>Actions performed at this step:</p>
     * <ul>
     * <li>Verifies, that there is <b>exactly one</b> button with the image which <i>'src'</i> attribute
     * value equals to <b>'imageSrc'</b>;</li>
     * <li>Verifies, that image has an expected <b>tooltip</b>;</li>
     * </ul>
     * @param imageSrc 'src' attribute value of the image element;
     * @param tooltip <i>'title'</i> attribute value of the image
     * @see <a href="http://www.w3schools.com/tags/default.asp"><i>HTML Element Reference</i></a>
     */
    @Then("a button with the tooltip '$tooltip' and image with the src '$imageSrc' exists")
    public void
            isButtonWithImageURLAndTooltipFound(String tooltip, String imageSrc)
    {
        WebElement element = isButtonWithImageSrcFound(imageSrc);
        elementValidations.assertIfElementContainsTooltip(element, tooltip);
    }

    /**
     * Checks that previously set searchContext contains <b>exactly one</b> button image
     * with the particular URL in the <i>'src'</i> attribute
     * <p>Actions performed at this step:</p>
     * <ul>
     * <li>Verifies, that there is <b>exactly one</b> button with the image which <i>'src'</i> attribute
     * value equals to <b>'imageSrc'</b>;</li>
     * </ul>
     * @param imageSrc 'src' attribute value of the image element;
     * @return <b>Button</b> element
     * @see <a href="http://www.w3schools.com/tags/default.asp"><i>HTML Element Reference</i></a>
     */
    @Then("a button with image with the src '$imageSrc' exists")
    public WebElement isButtonWithImageSrcFound(String imageSrc)
    {
        return baseValidations.assertIfElementExists("A button with image", new SearchAttributes(
                ActionAttributeType.XPATH, LocatorUtil.getXPath("button[./img[@src=%s]]", imageSrc)));
    }

    /**
     * Checks that a <b>radio button</b> specified by the <b>name</b> exists within
     * the previously specified <b>search context</b>
     * <p>A <b>radio button</b> is an <i><code>&lt;input&gt;</code></i> element with an attribute
     * 'type' = 'radio' and a <b>name</b> for it is a 'text' or any 'attribute value' of
     * it's <i><code>&lt;label&gt;</code></i> element (<i><code>&lt;label&gt;</code></i>
     * with an attribute 'for' = radio button id).
     * </p>
     * <p>Actions performed at this step:</p>
     * <ul>
     * <li>Finds the <b>radio button</b> specified by the <b>name</b> in the radio group;</li>
     * </ul>
     * @param radioOption A name of the <b>radio button</b>
     * @return WebElement An <b>element</b> matching the requirements,
     * <b> null</b> - if there are no desired elements.
     * <b>Example:</b>
     * <pre>
     * {@code <div>
     *      <label>}<b>'radioGroup'</b>{@code </label>
     *      <div>
     *          <input id="radioButtonId" type="radio" />
     *          <label for="radioButtonId">}<b>'radioOption'</b>{@code </label>
     *      </div>
     * </div>}</pre>

     *      */
    @Then("a radio button with the name '$radioOption' exists")
    public WebElement ifRadioOptionExists(String radioOption)
    {
        return assertIfRadioOptionExists(radioOption);
    }

    /**
     * Checks a <b>radio button</b> specified by the <b>name</b>
     * within the specified <b>search context</b>
     * <p>A <b>radio group</b> is some <i><code>&lt;div&gt;</code></i> that contains a <b>radio buttons</b>,
     * and it's <b>name</b> is a 'text' containing in one of it's child elements.
     * </p>
     * <p>A <b>radio button</b> is an <i><code>&lt;input&gt;</code></i> element with an attribute
     * 'type' = 'radio' and a <b>name</b> for it is a 'text' or any 'attribute value' of
     * it's <i><code>&lt;label&gt;</code></i> element (<i><code>&lt;label&gt;</code></i>
     * with an attribute 'for' = radio button id).
     * </p>
     * <p>Actions performed at this step:</p>
     * <ul>
     * <li>Finds the <b>radio button</b> specified by the <b>name</b>;</li>
     * <li>Clicks on the <b>radio button</b>;</li>
     * <li>Waits for the page to load;</li>
     * </ul>
     * @param radioOption A name of the <b>radio button</b>.
     * <b>Example:</b>
     * <pre>
     * {@code <div>
     *      <label>}<b>'radioGroup'</b>{@code </label>
     *      <div>
     *          <input id="radioButtonId" type="radio" />
     *          <label for="radioButtonId">}<b>'radioOption'</b>{@code </label>
     *      </div>
     * </div>}</pre>
     * @see <a href="http://www.w3schools.com/tags/default.asp"><i>HTML Element Reference</i></a>
     */
    @When("I select a radio button with the name '$radioOption'")
    public void  checkRadioOption(String radioOption)
    {
        WebElement element = ifRadioOptionExists(radioOption);
        clickActions.click(element);
    }

    /**
     * Checks that a <b>radio button</b> specified by the <b>name</b> exists within
     * the previously specified <b>search context</b>
     * <p>A <b>radio button</b> is an <i><code>&lt;input&gt;</code></i> element with an attribute
     * 'type' = 'radio' and a <b>name</b> for it is a 'text' or any 'attribute value' of
     * it's <i><code>&lt;label&gt;</code></i> element (<i><code>&lt;label&gt;</code></i>
     * with an attribute 'for' = radio button id).
     * </p>
     * <p>Actions performed at this step:</p>
     * <ul>
     * <li>Finds the <b>radio button</b> specified by the <b>name</b> in the <b>radio group</b> ;
     * <li>Compares an actual radio button 'state' with expected;
     * </ul>
     * @param state A state value of the element
     * (<i>Possible values:</i> <b>ENABLED, DISABLED, SELECTED, NOT_SELECTED, VISIBLE, NOT_VISIBLE</b>)
     * @param radioOption A name of the <b>radio button</b>.
     * <b>Example:</b>
     * <pre>
     * {@code <div>
     *      <label>}<b>'radioGroup'</b>{@code </label>
     *      <div>
     *          <input id="radioButtonId" type="radio" />
     *          <label for="radioButtonId">}<b>'radioOption'</b>{@code </label>
     *      </div>
     * </div>}</pre>
     */
    @Then("a [$state] radio button with the name '$radioOption' exists")
    public void ifRadioOptionExists(State state, String radioOption)
    {
        WebElement radioButton = ifRadioOptionExists(radioOption);
        baseValidations.assertElementState("The found radio button is " + state, state, radioButton);
    }

    private WebElement assertIfRadioOptionLabelExists(String radioOption)
    {
        return baseValidations.assertIfElementExists(String.format("A radio button label with text '%s'", radioOption),
                new SearchAttributes(ActionAttributeType.XPATH,
                        LocatorUtil.getXPath(ElementPattern.LABEL_PATTERN, radioOption)));
    }

    private WebElement assertIfRadioOptionExists(String radioOption)
    {
        WebElement labelElement = assertIfRadioOptionLabelExists(radioOption);
        if (labelElement != null)
        {
            String labelForAtr = labelElement.getAttribute("for");
            return StringUtils.isNotEmpty(labelForAtr)
                    ? baseValidations.assertIfElementExists(RADIO_BUTTON, new SearchAttributes(
                            ActionAttributeType.XPATH, LocatorUtil.getXPath(ElementPattern.RADIO_OPTION_INPUT_PATTERN,
                                    labelForAtr)))
                    : baseValidations.assertIfElementExists(RADIO_BUTTON, labelElement,
                            new SearchAttributes(ActionAttributeType.XPATH, "input[@type='radio']"));
        }
        return null;
    }

    private SearchContext getSearchContext()
    {
        return webUiContext.getSearchContext();
    }
}
