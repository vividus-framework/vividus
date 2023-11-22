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

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.steps.Parameters;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import org.vividus.annotation.Replacement;
import org.vividus.selenium.locator.Locator;
import org.vividus.softassert.ISoftAssert;
import org.vividus.steps.ui.validation.IBaseValidations;
import org.vividus.ui.monitor.TakeScreenshotOnFailure;
import org.vividus.ui.web.action.IFieldActions;
import org.vividus.ui.web.action.IWebElementActions;

@SuppressWarnings("PMD.AvoidDuplicateLiterals")
@TakeScreenshotOnFailure
public class DropdownSteps
{
    private static final String DROPDOWN = "Dropdown";
    private static final String SELECTED_OPTION_IN_DROPDOWN = "Selected option in dropdown";
    private static final String SELECTED_OPTIONS_ARE_PRESENT_IN_DROPDOWN = "Selected options are present in dropdown";
    private static final String STATE_OF_ACTUAL_OPTION_AT_POSITION = "State of actual option at position [%s]";
    private static final String TEXT_OF_ACTUAL_OPTION_AT_POSITION = "Text of actual option at position [%s]";
    private static final String EXPECTED_DROPDOWN_IS_OF_THE_SAME_SIZE_AS_ACTUAL_DROPDOWN =
            "Expected dropdown is of the same size as actual dropdown: ";
    private static final String ITEM = "item";
    private static final String STATE = "state";
    private final IWebElementActions webElementActions;
    private final IBaseValidations baseValidations;
    private final ISoftAssert softAssert;
    private final IFieldActions fieldActions;

    public DropdownSteps(IWebElementActions webElementActions, IBaseValidations baseValidations, ISoftAssert softAssert,
            IFieldActions fieldActions)
    {
        this.webElementActions = webElementActions;
        this.baseValidations = baseValidations;
        this.softAssert = softAssert;
        this.fieldActions = fieldActions;
    }

    /**
     * Checks whether dropdown list located by <b>locator</b> equals to expected list <b>options</b>
     * <b>Actions performed at this step:</b>
     * <ul>
     * <li>Checks that the dropdown list with specified <b>locator</b> is present</li>
     * <li>Checks that expected list <b>options</b> is equal to actual by size, list options sequence</li>
     * </ul>
     * @param locator Locator to locate dropdown list
     * @param options Expected list with isSelected state for list entries and entries text
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
     * @deprecated Use step: "Then dropdown located by `$locator` contains options:$options"
     */
    @Deprecated(since = "0.6.0", forRemoval = true)
    @Replacement(versionToRemoveStep = "0.7.0", replacementFormatPattern =
            "Then dropdown located by `%1$s` contains options:%2$s")
    @Then("dropdown located `$locator` contains options:$options")
    public void doesDropdownContainOptionsDeprecated(Locator locator, ExamplesTable options)
    {
        runIfDropdownExistsDeprecated(locator, dropdown -> {
            List<WebElement> actualOptions = dropdown.getOptions();
            List<Parameters> expectedOptions = options.getRowsAsParameters(true);
            if (softAssert.assertEquals(EXPECTED_DROPDOWN_IS_OF_THE_SAME_SIZE_AS_ACTUAL_DROPDOWN,
                    expectedOptions.size(), actualOptions.size()))
            {
                for (int i = 0; i < expectedOptions.size(); i++)
                {
                    WebElement option = actualOptions.get(i);
                    Map<String, String> expectedRow = expectedOptions.get(i).values();
                    softAssert.assertEquals(
                            String.format(TEXT_OF_ACTUAL_OPTION_AT_POSITION, i + 1), expectedRow.get(ITEM),
                            webElementActions.getElementText(option));
                    softAssert.assertEquals(
                            String.format(STATE_OF_ACTUAL_OPTION_AT_POSITION, i + 1),
                            Boolean.parseBoolean(expectedRow.get(STATE)), option.isSelected());
                }
            }
        });
    }

    /**
     * Checks whether dropdown list located by <b>locator</b> equals to expected list <b>options</b>
     * <b>Actions performed at this step:</b>
     * <ul>
     * <li>Checks that the dropdown list with specified <b>locator</b> is present</li>
     * <li>Checks that expected list <b>options</b> is equal to actual by size, list options sequence</li>
     * </ul>
     * @param locator Locator to locate dropdown list
     * @param options Expected list with isSelected state for list entries and entries text
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
    @Then("dropdown located by `$locator` contains options:$options")
    public void doesDropdownContainOptions(Locator locator, ExamplesTable options)
    {
        runIfDropdownExists(locator, dropdown -> {
            List<WebElement> actualOptions = dropdown.getOptions();
            List<Parameters> expectedOptions = options.getRowsAsParameters(true);
            if (softAssert.assertEquals(EXPECTED_DROPDOWN_IS_OF_THE_SAME_SIZE_AS_ACTUAL_DROPDOWN,
                    expectedOptions.size(), actualOptions.size()))
            {
                for (int i = 0; i < expectedOptions.size(); i++)
                {
                    WebElement option = actualOptions.get(i);
                    Map<String, String> expectedRow = expectedOptions.get(i).values();
                    softAssert.assertEquals(
                            String.format(TEXT_OF_ACTUAL_OPTION_AT_POSITION, i + 1), expectedRow.get(ITEM),
                            webElementActions.getElementText(option));
                    softAssert.assertEquals(
                            String.format(STATE_OF_ACTUAL_OPTION_AT_POSITION, i + 1),
                            Boolean.parseBoolean(expectedRow.get(STATE)), option.isSelected());
                }
            }
        });
    }

    /**
     * Checks whether default value of a dropdown located by locator
     * is similar to expected
     * @param locator Locator to locate dropdown list
     * @param option Text that is displayed
     * @deprecated Use step: "Then dropdown located by `$locator` exists and selected option is `$option`"
     */
    @Deprecated(since = "0.6.0", forRemoval = true)
    @Replacement(versionToRemoveStep = "0.7.0", replacementFormatPattern =
            "Then dropdown located by `%1$s` exists and selected option is `%2$s`")
    @Then(value = "dropdown located `$locator` exists and selected option is `$option`", priority = 1)
    public void doesDropdownHaveFirstSelectedOptionDeprecated(Locator locator, String option)
    {
        findDropdownDeprecated(locator).ifPresent(dropdown -> {
            if (softAssert.assertTrue(SELECTED_OPTIONS_ARE_PRESENT_IN_DROPDOWN,
                    !dropdown.getAllSelectedOptions().isEmpty()))
            {
                softAssert.assertEquals(SELECTED_OPTION_IN_DROPDOWN, option,
                        dropdown.getFirstSelectedOption().getText().trim());
            }
        });
    }

    /**
     * Checks whether default value of a dropdown located by locator
     * is similar to expected
     * @param locator Locator to locate dropdown list
     * @param option Text that is displayed
     */
    @Then(value = "dropdown located by `$locator` exists and selected option is `$option`", priority = 1)
    public void doesDropdownHaveFirstSelectedOption(Locator locator, String option)
    {
        findDropdown(locator).ifPresent(dropdown -> {
            if (softAssert.assertTrue(SELECTED_OPTIONS_ARE_PRESENT_IN_DROPDOWN,
                    !dropdown.getAllSelectedOptions().isEmpty()))
            {
                softAssert.assertEquals(SELECTED_OPTION_IN_DROPDOWN, option,
                        dropdown.getFirstSelectedOption().getText().trim());
            }
        });
    }

    /**
     * Adds the text to any option already selected. Works for multi-select dropdown lists.
     * If you apply this step to a single select dropdown it will be <i><b>failed</b></i>
     * @param option A visible text value of the <b>option</b> in the <b>dropdown list</b>
     * @param locator Locator to locate <b>dropdown list</b>
     * @deprecated Use step: "When I add `$option` to selection in dropdown located by `$locator`"
     */
    @Deprecated(since = "0.6.0", forRemoval = true)
    @Replacement(versionToRemoveStep = "0.7.0", replacementFormatPattern =
            "When I add `%1$s` to selection in dropdown located by `%2$s`")
    @When("I add `$option` to selection in dropdown located `$locator`")
    public void addOptionInDropdownDeprecated(String option, Locator locator)
    {
        selectOptionInDropdownDeprecated(locator, option, true);
    }

    /**
     * Adds the text to any option already selected. Works for multi-select dropdown lists.
     * If you apply this step to a single select dropdown it will be <i><b>failed</b></i>
     * @param option A visible text value of the <b>option</b> in the <b>dropdown list</b>
     * @param locator Locator to locate <b>dropdown list</b>
     */
    @When("I add `$option` to selection in dropdown located by `$locator`")
    public void addOptionInDropdown(String option, Locator locator)
    {
        selectOptionInDropdown(locator, option, true);
    }

    /**
     * Selects a desired <b>option</b> by a visible <b>text</b> from a dropdown element specified by <b>locator</b>.A
     * <b>dropdown list</b> is created by the <i>&lt;select&gt;</i> tag. The <i>&lt;option&gt;</i> tags inside the<i>
     * &lt;select&gt;</i> define the available options in the list.
     * <p>Actions performed at this step:</p>
     * <ul>
     * <li>Find an element by locator</li>
     * <li>Select an <b>option</b> in the <b>dropdown list</b> by it's visible 'text'</li>
     * <li>Wait for the page to load</li>
     * </ul>
     * @param option A visible text value of the <b>option</b> in the <b>dropdown list</b>
     * @param locator Locator of a dropdown element
     * @deprecated Use step: "When I select `$option` in dropdown located by `$locator`"
     */
    @Deprecated(since = "0.6.0", forRemoval = true)
    @Replacement(versionToRemoveStep = "0.7.0", replacementFormatPattern =
            "When I select `%1$s` in dropdown located by `%2$s`")
    @When("I select `$option` in dropdown located `$locator`")
    public void selectOptionInDropdownDeprecated(String option, Locator locator)
    {
        selectOptionInDropdownDeprecated(locator, option, false);
    }

    /**
     * Selects a desired <b>option</b> by a visible <b>text</b> from a dropdown element specified by <b>locator</b>.A
     * <b>dropdown list</b> is created by the <i>&lt;select&gt;</i> tag. The <i>&lt;option&gt;</i> tags inside the<i>
     * &lt;select&gt;</i> define the available options in the list.
     * <p>Actions performed at this step:</p>
     * <ul>
     * <li>Find an element by locator</li>
     * <li>Select an <b>option</b> in the <b>dropdown list</b> by it's visible 'text'</li>
     * <li>Wait for the page to load</li>
     * </ul>
     * @param option A visible text value of the <b>option</b> in the <b>dropdown list</b>
     * @param locator Locator of a dropdown element
     */
    @When("I select `$option` in dropdown located by `$locator`")
    public void selectOptionInDropdown(String option, Locator locator)
    {
        selectOptionInDropdown(locator, option, false);
    }

    private void runIfDropdownExistsDeprecated(Locator locator, Consumer<Select> toRun)
    {
        findDropdownDeprecated(locator).ifPresent(toRun);
    }

    private Optional<Select> findDropdownDeprecated(Locator locator)
    {
        return Optional.ofNullable(baseValidations.assertIfElementExists(DROPDOWN, locator)).map(Select::new);
    }

    private void selectOptionInDropdownDeprecated(Locator locator, String text, boolean addition)
    {
        runIfDropdownExistsDeprecated(locator, select -> fieldActions.selectItemInDropDownList(select, text, addition));
    }

    private void runIfDropdownExists(Locator locator, Consumer<Select> toRun)
    {
        findDropdown(locator).ifPresent(toRun);
    }

    private Optional<Select> findDropdown(Locator locator)
    {
        return baseValidations.assertElementExists(DROPDOWN, locator).map(Select::new);
    }

    private void selectOptionInDropdown(Locator locator, String text, boolean addition)
    {
        runIfDropdownExists(locator, select -> fieldActions.selectItemInDropDownList(select, text, addition));
    }
}
