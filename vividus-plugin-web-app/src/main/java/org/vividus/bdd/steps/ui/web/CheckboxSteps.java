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

import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WrapsElement;
import org.vividus.bdd.monitor.TakeScreenshotOnFailure;
import org.vividus.bdd.steps.ui.web.validation.IBaseValidations;
import org.vividus.selenium.element.Checkbox;
import org.vividus.ui.web.State;
import org.vividus.ui.web.action.CheckboxAction;
import org.vividus.ui.web.action.IMouseActions;
import org.vividus.ui.web.action.search.ActionAttributeType;
import org.vividus.ui.web.action.search.SearchAttributes;
import org.vividus.ui.web.action.search.SearchParameters;
import org.vividus.ui.web.util.LocatorUtil;

@TakeScreenshotOnFailure
public class CheckboxSteps
{
    private static final String THE_FOUND_CHECKBOX_IS = "The found checkbox is ";
    private static final String CHECKBOX_WITH_NAME = "Checkbox with name '%s'";
    private static final String CHECKBOX = "Checkbox";
    private static final String CHECKBOX_LOCATOR = LocatorUtil.getXPath("input[@type='checkbox']");
    private static final String CHECKBOX_WITH_ATTRIBUTE = "Checkbox with the attribute '%1$s'='%2$s'";

    @Inject private IBaseValidations baseValidations;
    @Inject private IMouseActions mouseActions;

    /**
     * Checks checkbox within the context
     * <p>Actions performed at this step:</p>
     * <ul>
     * <li>Finds a checkbox within the context;</li>
     * <li>Checks its state, if it's <b><i>not selected</i></b> then changes its state
     * <li>Waits for page to load;</li>
     * </ul>
     */
    @When("I check a checkbox")
    public void checkCheckBox()
    {
        WebElement checkBox = baseValidations.assertIfElementExists(CHECKBOX, new SearchAttributes(
                ActionAttributeType.XPATH, CHECKBOX_LOCATOR));
        changeCheckboxState(new Checkbox(checkBox), true);
    }

    /**
     * Checks all enabled checkboxes within the set context
     * <p>Actions performed at this step:</p>
     * <ul>
     * <li> Verifies that at least 1 checkbox is found within the search context;</li>
     * <li> Checks the state of each checkbox found on the page, if it's <b><i>not selected</i></b>
     * then change it to <b><i>selected</i></b>;</li>
     * <li>Waits for page to load;</li>
     * </ul>
     */
    @When("I check all the checkboxes")
    public void checkAllCheckboxes()
    {
        List<WebElement> checkBoxes = baseValidations.assertIfElementsExist("Checkboxes number",
                new SearchAttributes(ActionAttributeType.XPATH, CHECKBOX_LOCATOR));
        checkBoxes.stream().map(Checkbox::new).forEach(checkbox -> changeCheckboxState(checkbox, true));
    }

    /**
     * Check (uncheck) a checkbox with the specified text
     * <p>
     * Actions performed at this step:
     * </p>
     * <ul>
     * <li>Finds a checkbox with the specified text on the page;</li>
     * <li><b><i>Checks(unchecks)</i></b> a checkbox, if it's <b><i>not selected(selected)</i></b>. Do nothing if a
     * checkbox is already <i>checked(unchecked)</i>;
     * <li>Waits for the page to load;</li>
     * </ul>
     * @param checkBoxAction <b>CHECK</b> or <b>UNCHECK</b>
     * @param checkBoxName Checkbox text (the text in the (<i><code>&lt;label&gt;</code></i>) tag
     */
    @When(value = "I $checkBoxAction a checkbox with the name '$checkBoxName'", priority = 1)
    public void processCheckboxItem(CheckboxAction checkBoxAction, String checkBoxName)
    {
        Checkbox checkBox = ifCheckboxExists(checkBoxName);
        changeCheckboxState(checkBox, checkBoxAction.isSelected());
    }

    /**
     * Unchecks a checkbox specified by an <b>attribute type</b> with an <b>attribute value</b>
     * <p>
     * A <b>checkbox</b> is an <i><code>&lt;input&gt;</code></i> element with an attribute 'type' = 'checkbox'.
     * </p>
     * <p>Actions performed at this step:</p>
     * <ul>
     * <li>Finds a <b>checkbox</b> specified by an <b>attribute type</b> with an <b>attribute value</b>;</li>
     * <li>Checks its state, if it's <b><i>selected</i></b> then changes its state
     * <li>Waits for page to load;</li>
     * </ul>
     * @param attributeType An attribute type of the <b>checkbox</b>
     * @param attributeValue An attribute value of the <b>checkbox</b>.
     * <b>Example:</b>
     * <pre>
     * {@code <input }<b>'attributeType'</b>=<b>'attributeValue'</b> {@code type="checkbox" />
     * }
     * </pre>
     * @see <a href="http://www.w3schools.com/tags/att_input_checked.asp"><i>HTML </i>&lt;input&gt;<i> checked
     * Attribute</i></a>
     * @see <a href="http://www.w3schools.com/tags/default.asp"><i>HTML Element Reference</i></a>
     */
    @When("I uncheck a checkbox with the attribute '$attributeType'='$attributeValue'")
    public void uncheckCheckboxItem(String attributeType, String attributeValue)
    {
        changeCheckboxState(attributeType, attributeValue, false);
    }

    /**
     * Checks if a checkbox with the specified <b>name</b> exists in context
     * @param checkboxName Checkbox text (the text in the (<i><code>&lt;label&gt;</code></i>) tag
     * @return <b>WebElement</b> An element (checkbox) matching the requirements,
     * <b> null</b> - if there are no desired elements
     */
    @Then("a checkbox with the name '$checkboxName' exists")
    public Checkbox ifCheckboxExists(String checkboxName)
    {
        return (Checkbox) baseValidations.assertIfElementExists(String.format(CHECKBOX_WITH_NAME, checkboxName),
                new SearchAttributes(ActionAttributeType.CHECKBOX_NAME, checkboxName));
    }

    /**
     * Checks if a checkbox with the specified <b>name</b> exists in context and it has expected state
     * <p>Actions performed at this step:</p>
     * <ul>
     * <li>Finds a checkbox with the specified <b>name</b>;</li>
     * <li>Compares an actual checkbox 'state' with expected;</li>
     * </ul>
     * @param state A state value of the element
     * (<i>Possible values:</i> <b>ENABLED, DISABLED, SELECTED, NOT_SELECTED, VISIBLE, NOT_VISIBLE</b>)
     * @param checkboxName Checkbox text (the text in the (<i><code>&lt;label&gt;</code></i>) tag
     */
    @Then("a [$state] checkbox with the name '$checkboxName' exists")
    public void ifCheckboxExists(State state, String checkboxName)
    {
        WebElement checkbox = ifCheckboxExists(checkboxName);
        baseValidations.assertElementState(THE_FOUND_CHECKBOX_IS + state, state, checkbox);
    }

    /**
     * Checks if a checkbox with the specified <b>attribute</b> exists in context and it has expected state
     * <p>Actions performed at this step:</p>
     * <ul>
     * <li>Finds a checkbox specified by an <b>attribute type</b> with an <b>attribute value</b>;</li>
     * </ul>
     * @param attributeType A type of the attribute (for ex. <i>'name', 'id', 'title'</i>)
     * @param attributeValue A value of the attribute
     * @return Web element - a <b>checkbox</b> that meets the requirements,
     * <b> null</b> - if there are no expected elements.
     */
    @Then("a checkbox with the attribute '$attributeType'='$attributeValue' exists")
    public Checkbox ifCheckboxWithAttributeExists(String attributeType, String attributeValue)
    {
        WebElement checkbox = baseValidations.assertIfElementExists(
                String.format(CHECKBOX_WITH_ATTRIBUTE, attributeType, attributeValue), new SearchAttributes(
                        ActionAttributeType.XPATH, getCheckboxXpathByAttributeAndValue(attributeType, attributeValue)));
        return new Checkbox(checkbox);
    }

    /**
     * Checks if a checkbox with the specified <b>attribute</b> exists in context and it has expected state
     * <p>Actions performed at this step:</p>
     * <ul>
     * <li>Finds a checkbox specified by an <b>attribute type</b> with an <b>attribute value</b>;</li>
     * <li>Compares an actual checkbox 'state' with expected;</li>
     * </ul>
     * @param state A state value of the element (<i>Possible values:</i>
     * <b>ENABLED, DISABLED, SELECTED, NOT_SELECTED, VISIBLE,
     * NOT_VISIBLE</b>)
     * @param attributeType A type of the attribute (for ex. <i>'name', 'id', 'title'</i>)
     * @param attributeValue A value of the attribute
     * @return Web element - a <b>checkbox</b> that meets the requirements,
     * <b> null</b> - if there are no expected elements.
     */
    @Then("a [$state] checkbox with the attribute '$attributeType'='$attributeValue' exists")
    public WebElement ifCheckboxWithAttributeExists(State state, String attributeType, String attributeValue)
    {
        Checkbox checkbox = ifCheckboxWithAttributeExists(attributeType, attributeValue);
        baseValidations.assertElementState(THE_FOUND_CHECKBOX_IS + state, state, (WrapsElement) checkbox);
        return checkbox;
    }

    /**
     * Checks if a checkbox with the specified <b>name</b> does not exist in context
     * @param checkboxName Checkbox text (the text in the (<i><code>&lt;label&gt;</code></i>) tag
    */
    @Then("a checkbox with the name '$checkBox' does not exist")
    public void doesNotCheckboxExist(String checkboxName)
    {
        SearchParameters parameters = new SearchParameters(checkboxName).setWaitForElement(false);
        baseValidations.assertIfElementDoesNotExist(String.format(CHECKBOX_WITH_NAME, checkboxName),
                new SearchAttributes(ActionAttributeType.CHECKBOX_NAME, parameters));
    }

    /**
     * Check (uncheck) a checkbox by the xpath
     * <p>
     * Actions performed at this step:
     * </p>
     * <ul>
     * <li>Finds a checkbox by <b>xpath</b>;</li>
     * <li><b><i>Checks(unchecks)</i></b> a checkbox, if it's <b><i>not selected(selected)</i></b>. Do nothing if a
     * checkbox is already <i>checked(unchecked)</i>;
     * <li>Waits for the page to load;</li>
     * </ul>
     * @param checkBoxAction <b>CHECK</b> or <b>UNCHECK</b>
     * @param xpath Xpath to the checkbox element
     */
    @When("I $checkBoxAction a checkbox by the xpath '$xpath'")
    public void processCheckboxByXpath(CheckboxAction checkBoxAction, String xpath)
    {
        changeCheckboxState(LocatorUtil.getXPath(xpath), checkBoxAction.isSelected());
    }

    /**
     * Checks a checkbox specified by an <b>attribute type</b> with an <b>attribute value</b>
     * <p>A <b>checkbox</b> is an <i><code>&lt;input&gt;</code></i> element with an attribute 'type' = 'checkbox'.
     * </p>
     * <p>Actions performed at this step:</p>
     * <ul>
     * <li>Finds a checkbox specified by an <b>attribute type</b> with an <b>attribute value</b>;</li>
     * <li>Checks its state, if it's <b><i>not selected</i></b> then changes its state
     * <li>Waits for page to load;</li>
     * </ul>
     * @param attributeType An attribute type of the <b>checkbox</b>
     * @param attributeValue An attribute value of the <b>checkbox</b>
     * <b>Example:</b>
     * <pre>
     * {@code <input }<b>'attributeType'</b>=<b>'attributeValue'</b> {@code type="checkbox">
     * }</pre>
     * @see <a href="http://www.w3schools.com/tags/att_input_checked.asp"><i>HTML </i>&lt;input&gt;<i> checked
     * Attribute</i></a>
     * @see <a href="http://www.w3schools.com/tags/default.asp"><i>HTML Element Reference</i></a>
     */
    @When("I check a checkbox with the attribute '$attributeType'='$attributeValue'")
    public void checkCheckboxItem(String attributeType, String attributeValue)
    {
        changeCheckboxState(attributeType, attributeValue, true);
    }

    private void changeCheckboxState(String attributeType, String attributeValue, boolean selected)
    {
        changeCheckboxState(getCheckboxXpathByAttributeAndValue(attributeType, attributeValue), selected);
    }

    private void changeCheckboxState(String xpath, boolean isSelected)
    {
        WebElement checkboxEl = baseValidations.assertIfElementExists(CHECKBOX,
                new SearchAttributes(ActionAttributeType.XPATH, xpath));
        changeCheckboxState(new Checkbox(checkboxEl), isSelected);
    }

    private void changeCheckboxState(Checkbox checkbox, boolean selected)
    {
        if (checkbox != null && checkbox.getWrappedElement() != null && checkbox.isSelected() != selected)
        {
            WebElement elementToClick = checkbox.isDisplayed() ? checkbox : checkbox.getLabelElement();
            mouseActions.click(elementToClick);
        }
    }

    public String getCheckboxXpathByAttributeAndValue(String attributeType, String attributeValue)
    {
        return LocatorUtil.getXPath("input[@type=\"checkbox\" and @" + attributeType + "=%s]", attributeValue);
    }
}
