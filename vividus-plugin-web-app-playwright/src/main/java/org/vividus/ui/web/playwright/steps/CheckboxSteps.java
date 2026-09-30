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

import com.microsoft.playwright.Locator;

import org.jbehave.core.annotations.When;
import org.vividus.ui.web.playwright.UiContext;
import org.vividus.ui.web.playwright.locator.PlaywrightLocator;
import org.vividus.ui.web.playwright.model.CheckboxAction;

public class CheckboxSteps
{
    private final UiContext uiContext;

    public CheckboxSteps(UiContext uiContext)
    {
        this.uiContext = uiContext;
    }

    /**
     * Performs an action (check or uncheck) on the checkbox found by locator. The action is not performed if the
     * checkbox is already in the desired state.
     *
     * @param checkBoxAction  The action to perform on the checkbox: <b>CHECK</b> or <b>UNCHECK</b>.
     * @param checkboxLocator The locator used to find a checkbox.
     */
    @When("I $checkBoxAction checkbox located by `$checkboxLocator`")
    public void changeStateOfCheckbox(CheckboxAction checkBoxAction, PlaywrightLocator checkboxLocator)
    {
        Locator checkbox = uiContext.locateElement(checkboxLocator);
        checkbox.setChecked(checkBoxAction.isSelected());
    }
}
