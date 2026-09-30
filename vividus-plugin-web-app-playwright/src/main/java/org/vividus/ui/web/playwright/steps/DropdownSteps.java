/*
 * Copyright 2019-2024 the original author or authors.
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

package org.vividus.ui.web.playwright.steps;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.assertions.PlaywrightAssertions;
import com.microsoft.playwright.options.AriaRole;

import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.steps.Parameters;
import org.vividus.softassert.ISoftAssert;
import org.vividus.ui.web.playwright.UiContext;
import org.vividus.ui.web.playwright.assertions.PlaywrightSoftAssert;
import org.vividus.ui.web.playwright.locator.PlaywrightLocator;

public class DropdownSteps
{
    private static final String ITEM = "item";
    private static final String STATE = "state";
    private static final String ONE_DROPDOWN_ASSERTION_MESSAGE =
            "One unique dropdown by the locator must be present in the context";
    private static final String OPTION_SELECTED_EVALUATE_SCRIPT = "option => option.selected";
    private static final String VALUE_ATTRIBUTE = "value";
    private static final String OPTION_TAG = "option";

    private final UiContext uiContext;
    private final PlaywrightSoftAssert playwrightSoftAssert;
    private final ISoftAssert softAssert;

    public DropdownSteps(UiContext uiContext, PlaywrightSoftAssert playwrightSoftAssert, ISoftAssert softAssert)
    {
        this.uiContext = uiContext;
        this.playwrightSoftAssert = playwrightSoftAssert;
        this.softAssert = softAssert;
    }

    /**
     * Selects option in dropdown.
     *
     * @param option The option to select.
     * @param locator The locator used to find a dropdown.
     */
    @When("I select `$option` in dropdown located by `$locator`")
    public void selectOptionInDropdown(String option, PlaywrightLocator locator)
    {
        selectOption(locator, option, false);
    }

    /**
     * Adds the option to already selected ones. Works only for multi-select dropdown lists. If the step is applied
     * to a single-select dropdown, the step will fail.
     *
     * @param option The option to add to the selection.
     * @param locator The locator used to find a dropdown.
     */
    @When("I add `$option` to selection in dropdown located by `$locator`")
    public void addOptionInDropdown(String option, PlaywrightLocator locator)
    {
        selectOption(locator, option, true);
    }

    /**
     * Checks whether dropdown list located by <b>locator</b> equals to expected list <b>options</b>.
     * <p>Actions performed at this step:</p>
     * <ul>
     * <li>Checks that the dropdown list with specified <b>locator</b> is present</li>
     * <li>Checks that expected list <b>options</b> is equal to actual by size, list options sequence</li>
     * </ul>
     *
     * @param locator Locator used to find a dropdown.
     * @param options Expected list with isSelected state for list entries and entries text.
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
     * </tbody>
     * </table>
     */
    @Then("dropdown located by `$locator` contains options:$options")
    public void doesDropdownContainOptions(PlaywrightLocator locator, ExamplesTable options)
    {
        runIfDropdownExists(locator, dropdown -> {
            List<Locator> actualOptions = dropdown.locator(OPTION_TAG).all();
            List<Parameters> expectedOptions = options.getRowsAsParameters(true);
            if (softAssert.assertEquals("Expected dropdown is of the same size as actual dropdown: ",
                    expectedOptions.size(), actualOptions.size()))
            {
                for (int i = 0; i < expectedOptions.size(); i++)
                {
                    Locator option = actualOptions.get(i);
                    Map<String, String> expectedRow = expectedOptions.get(i).values();
                    softAssert.assertEquals(String.format("Text of actual option at position [%s]", i + 1),
                            expectedRow.get(ITEM), option.textContent().trim());
                    softAssert.assertEquals(String.format("State of actual option at position [%s]", i + 1),
                            Boolean.parseBoolean(expectedRow.get(STATE)), isOptionSelected(option));
                }
            }
        });
    }

    /**
     * Checks whether the currently selected option of a dropdown located by <b>locator</b> is equal to expected
     * <b>option</b>.
     *
     * @param locator Locator used to find a dropdown.
     * @param option The expected visible text of the selected option.
     */
    @Then("dropdown located by `$locator` exists and selected option is `$option`")
    public void doesDropdownHaveFirstSelectedOption(PlaywrightLocator locator, String option)
    {
        runIfDropdownExists(locator, dropdown -> {
            List<Locator> selectedOptions = dropdown.locator(OPTION_TAG).all().stream()
                    .filter(this::isOptionSelected)
                    .toList();
            if (softAssert.assertTrue("Selected options are present in dropdown", !selectedOptions.isEmpty()))
            {
                softAssert.assertEquals("Selected option in dropdown", option,
                        selectedOptions.get(0).textContent().trim());
            }
        });
    }

    private void selectOption(PlaywrightLocator locator, String option, boolean addition)
    {
        Locator dropdown = uiContext.locateElement(locator);

        playwrightSoftAssert.runAssertion(ONE_DROPDOWN_ASSERTION_MESSAGE, () ->
        {
            PlaywrightAssertions.assertThat(dropdown).hasCount(1);
            List<Locator> allOptions = dropdown.getByRole(AriaRole.OPTION).all();
            Optional<String> matchedValue = allOptions.stream()
                    .map(o -> o.getAttribute(VALUE_ATTRIBUTE))
                    .filter(value -> value != null && value.equalsIgnoreCase(option))
                    .findFirst();
            softAssert.assertTrue(String.format("The option \"%s\" is present in dropdown", option),
                    matchedValue.isPresent());
            matchedValue.ifPresent(value -> {
                if (addition)
                {
                    List<String> valuesToSelect = new ArrayList<>(allOptions.stream()
                            .filter(this::isOptionSelected)
                            .map(o -> o.getAttribute(VALUE_ATTRIBUTE))
                            .toList());
                    if (!valuesToSelect.contains(value))
                    {
                        valuesToSelect.add(value);
                    }
                    dropdown.selectOption(valuesToSelect.toArray(new String[0]));
                }
                else
                {
                    dropdown.selectOption(value);
                }
            });
        });
    }

    private boolean isOptionSelected(Locator option)
    {
        return Boolean.TRUE.equals(option.evaluate(OPTION_SELECTED_EVALUATE_SCRIPT));
    }

    private void runIfDropdownExists(PlaywrightLocator locator, Consumer<Locator> toRun)
    {
        Locator dropdown = uiContext.locateElement(locator);
        playwrightSoftAssert.runAssertion(ONE_DROPDOWN_ASSERTION_MESSAGE, () ->
        {
            PlaywrightAssertions.assertThat(dropdown).hasCount(1);
            toRun.accept(dropdown);
        });
    }
}
