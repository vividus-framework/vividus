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

import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;

import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.steps.Parameters;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import org.vividus.bdd.monitor.TakeScreenshotOnFailure;
import org.vividus.bdd.steps.ui.validation.IBaseValidations;
import org.vividus.bdd.steps.ui.validation.IDescriptiveSoftAssert;
import org.vividus.ui.action.search.Locator;
import org.vividus.ui.action.search.Visibility;
import org.vividus.ui.web.DropDownState;
import org.vividus.ui.web.action.IFieldActions;
import org.vividus.ui.web.action.IWebElementActions;
import org.vividus.ui.web.action.search.WebLocatorType;
import org.vividus.ui.web.util.LocatorUtil;

@TakeScreenshotOnFailure
public class DropdownSteps
{
    private static final String DROP_DOWN_WITH_NAME = "Drop down with the name '%s'";

    @Inject private IWebElementActions webElementActions;
    @Inject private IBaseValidations baseValidations;
    @Inject private IDescriptiveSoftAssert descriptiveSoftAssert;
    @Inject private IFieldActions fieldActions;

    /**
     * Checks that previously set searchContext contains a drop down with the expected <b>name</b>
     * <p>
     * <b>Drop down</b> is a drop down list with the specified options;</p>
     * <p>
     * Is created by the <i>&lt;select&gt;</i> tag. The <i>&lt;option&gt;</i> tags inside the <i>
     * &lt;select&gt;</i> define the available options in the list.</p>
     * <b>Example:</b>
     * <pre>
     *  &lt;select&gt;
     *   &lt;option&gt;visible text 1&lt;/option&gt;
     *   &lt;option&gt;visible text 2&lt;/option&gt;
     *   &lt;option&gt;visible text 3&lt;/option&gt;
     *  &lt;/select&gt;
     * </pre>
     * @param dropDownName Text value of any attribute of the drop down tag
     * @return WebElement
     */
    @Then("a drop down with the name '$dropDownName' exists")
    public Select isDropDownWithNameFound(String dropDownName)
    {
        return findDropDownList(dropDownName);
    }

    /**
     * Checks whether dropDown list with <b>dropDownName</b> equals to expected list <b>dropDownItems</b>
     * <p>
     * <b>dropDownName</b> is a <i>&lt;... name="any name"&gt;</i> attribute value identifying the dropDown list.
     * </p>
     * <b>Actions performed at this step:</b>
     * <ul>
     * <li>Checks that the dropDown list with specified <b>dropDownName</b> is present and returns it
     * <li>Checks that expected list <b>dropDownItems</b> is equal to actual by size, list items sequence
     * </ul>
     * @param dropDownName Value of <b>name</b> attribute of actual dropDown list
     * @param dropDownItems Expected list with isSelected state for list entries and entries text
     * <table>
     * <caption>A table of attributes</caption>
     * <thead>
     * <tr>
     * <th><b>state </b></th><th><b>item</b></th>
     * </tr>
     * </thead> <tbody>
     * <tr>
     * <td>$state </td><td>$item1</td>
     * </tr>
     * <tr>
     * <td>$state </td><td>$item2</td>
     * </tr>
     * <tr>
     * <td>$state </td><td>$item3</td>
     * </tr>
     * </tbody>
     * </table>
     */
    @Then("a drop down with the name '$dropDownName' contains the items: $dropDownItems")
    public void doesDropDownListContainItems(String dropDownName, ExamplesTable dropDownItems)
    {
        Select dropDown = isDropDownWithNameFound(dropDownName);
        if (dropDown != null)
        {
            List<WebElement> actualItems = dropDown.getOptions();
            List<Parameters> expectedItems = dropDownItems.getRowsAsParameters(true);
            if (descriptiveSoftAssert.assertEquals("Expected dropdown is of the same size as actual dropdown: ",
                    expectedItems.size(), actualItems.size()))
            {
                for (int i = 0; i < expectedItems.size(); i++)
                {
                    WebElement option = actualItems.get(i);
                    Map<String, String> expectedRow = expectedItems.get(i).values();
                    descriptiveSoftAssert.assertEquals(
                            String.format("Text of actual item at position [%s]", i + 1), expectedRow.get("item"),
                            webElementActions.getElementText(option));
                    descriptiveSoftAssert.assertEquals(
                            String.format("State of actual item at position [%s]", i + 1),
                            Boolean.parseBoolean(expectedRow.get("state")), option.isSelected());
                }
            }
        }
    }

    /**
     * Checks if a drop down with the specified <b>name</b> does not exist in context
     * @param dropDownName Any attribute value of the <i>&lt;select&gt;</i> tag
    */
    @Then("a drop down with the name '$dropDownName' does not exist")
    public void doesNotDropDownExist(String dropDownName)
    {
        Locator locator = createLocator(dropDownName);
        locator.getSearchParameters().setVisibility(Visibility.ALL);
        baseValidations.assertIfElementDoesNotExist(String.format(DROP_DOWN_WITH_NAME, dropDownName), locator);
    }

    /**
     * Checks that previously set searchContext contains a drop down with the expected <b>name</b>
     * and expected State
     * <p>
     * <b>Drop down</b> is a drop down list with the specified options;
     * </p>
     * <p>
     * Is created by the <i>&lt;select&gt;</i> tag. The <i>&lt;option&gt;</i> tags inside the <i>
     * &lt;select&gt;</i> define the available options in the list.</p>
     * <b>Example:</b>
     * <pre>
     *  &lt;select&gt;
     *   &lt;option&gt;visible text 1&lt;/option&gt;
     *   &lt;option&gt;visible text 2&lt;/option&gt;
     *   &lt;option&gt;visible text 3&lt;/option&gt;
     *  &lt;/select&gt;
     * </pre>
     * @param state A state value of the link
     * (<i>Possible values:</i> <b>ENABLED, DISABLED, SELECTED, NOT_SELECTED, VISIBLE, NOT_VISIBLE, SINGLE_SELECT,
     * MULTI_SELECT</b>)
     * @param dropDownName Text value of any attribute of the drop down tag
     */
    @Then("a [$state] drop down with the name '$dropDownName' exists")
    public void isDropDownWithNameFound(DropDownState state, String dropDownName)
    {
        Select select = findDropDownList(dropDownName);
        baseValidations.assertElementState("The found drop down is " + state, state, select);
    }

    /**
     * Checks whether default value of a drop-down with the specific name
     * is similar to expected
     * @param dropDownName Text value of any attribute of the drop down tag
     * @param dropDownText Text that is displayed
     */
    @Then(value = "a drop down with the name '$dropDownName' and text '$dropDownText' exists", priority = 1)
    public void ifDropDownWithNameExists(String dropDownName, String dropDownText)
    {
        Select dropDown = isDropDownWithNameFound(dropDownName);
        if (dropDown != null && descriptiveSoftAssert.assertTrue("Selected options are present in drop down",
                !dropDown.getAllSelectedOptions().isEmpty()))
        {
            descriptiveSoftAssert.assertEquals("Selected option in drop down", dropDownText,
                    dropDown.getFirstSelectedOption().getText().trim());
        }
    }

    /**
     * Selects a desired <b>option</b> by a visible 'text' from a <b>drop-down list</b> with
     * a specified 'name' attribute. If any option was already selected - reselects it.
     * If there are several equal values in a drop-down - selects them all.
     * <p>
     * A <b>drop-down list</b> is created by the <i>&lt;select&gt;</i> tag. The <i>&lt;option&gt;</i> tags inside the<i>
     * &lt;select&gt;</i> define the available options in the list.</p>
     * <b>Example:</b>
     * <pre>
     *  &lt;select&gt;
     *   &lt;option&gt;visible text 1&lt;/option&gt;
     *   &lt;option&gt;visible text 2&lt;/option&gt;
     *   &lt;option&gt;visible text 3&lt;/option&gt;
     *  &lt;/select&gt;
     * </pre>
     * Actions performed at this step:
     * <ul>
     * <li>Finds the <b>drop-down list</b> by it's 'name' attribute
     * <li>Selects an <b>option</b> in the <b>drop-down list</b> by it's visible 'text'
     * <li>Waits for the page to load
     * </ul>
     * @param text A visible text value of the <b>option</b> in the <b>drop-down list</b>
     * @param dropDownListName A name attribute of the <b>drop-down list</b>
     * @see <a href="https://www.w3schools.com/tags/tag_select.asp"><i>HTML &lt;select&gt; Tag</i></a>
     */
    @When("I select '$text' from a drop down with the name '$dropDownListName'")
    public void selectItemInDDL(String text, String dropDownListName)
    {
        selectItemInDDL(dropDownListName, text, false);
    }

    /**
     * Step works similarly to
     * <ul>
     * <li><i><b>When</b> I select '$text' from a drop down with the name '$dropDownListName'</i>
     * </ul>
     * step. The difference is that if any option was already selected - it remains selected.
     * If you apply this step to a single select drop-down it will be <i><b>failed</b></i>
     * @param text A visible text value of the <b>option</b> in the <b>drop-down list</b>
     * @param dropDownListName A name attribute of the <b>drop-down list</b>
     * @see DropdownSteps#selectItemInDDL(String text, String dropDownListName)
     */
    @When("I add '$text' to selection in a drop down with the name '$dropDownListName'")
    public void addItemInDDL(String text, String dropDownListName)
    {
        selectItemInDDL(dropDownListName, text, true);
    }

    /**
     * Selects a desired <b>option</b> by a visible <b>text</b> from a drop-down element specified by <b>locator</b>.A
     * <b>drop-down list</b> is created by the <i>&lt;select&gt;</i> tag. The <i>&lt;option&gt;</i> tags inside the<i>
     * &lt;select&gt;</i> define the available options in the list.
     * <p>Actions performed at this step:</p>
     * <ul>
     * <li>Find an element by locator</li>
     * <li>Select an <b>option</b> in the <b>drop-down list</b> by it's visible 'text'</li>
     * <li>Wait for the page to load</li>
     * </ul>
     * @param text A visible text value of the <b>option</b> in the <b>drop-down list</b>
     * @param locator Locator of a drop down element
     */
    @When("I select `$text` from drop down located `$locator`")
    public void selectTextFromDropDownByLocator(String text, Locator locator)
    {
        findDropDownList("A drop down", locator)
            .ifPresent(s -> fieldActions.selectItemInDropDownList(s, text, false));
    }

    private Optional<Select> findDropDownList(String businessDescription, Locator locator)
    {
        return Optional.ofNullable(baseValidations.assertIfElementExists(businessDescription, locator))
                .map(Select::new);
    }

    private Select findDropDownList(String dropDownListName)
    {
        WebElement element = baseValidations.assertIfElementExists(String.format(DROP_DOWN_WITH_NAME, dropDownListName),
                createLocator(dropDownListName));
        return element != null ? new Select(element) : null;
    }

    private void selectItemInDDL(String dropDownListName, String text, boolean isAddition)
    {
        Select select = findDropDownList(dropDownListName);
        fieldActions.selectItemInDropDownList(select, text, isAddition);
    }

    private Locator createLocator(String dropDownListName)
    {
        return new Locator(WebLocatorType.XPATH, LocatorUtil.getXPath(".//select[@*=%s]", dropDownListName));
    }
}
