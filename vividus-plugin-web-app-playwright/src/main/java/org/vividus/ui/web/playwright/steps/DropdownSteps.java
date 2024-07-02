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

public class DropdownSteps
{
    private final UiContext uiContext;

    public DropdownSteps(UiContext uiContext)
    {
        this.uiContext = uiContext;
    }

    /**
     * Selects option in dropdown.
     *
     * @param option option to select.
     * @param locator The locator used to find a dropdown.
     */
    @When("I select `$option` in dropdown located by `$locator`")
    public void selectOptionInDropdown(String option, PlaywrightLocator locator)
    {
        Locator dropdown = uiContext.locateElement(locator);
        dropdown.selectOption(option);
    }
}
