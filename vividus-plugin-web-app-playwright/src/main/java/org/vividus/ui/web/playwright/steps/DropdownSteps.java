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
import com.microsoft.playwright.assertions.PlaywrightAssertions;
import com.microsoft.playwright.options.AriaRole;

import org.jbehave.core.annotations.When;
import org.vividus.softassert.ISoftAssert;
import org.vividus.ui.web.playwright.UiContext;
import org.vividus.ui.web.playwright.assertions.PlaywrightSoftAssert;
import org.vividus.ui.web.playwright.locator.PlaywrightLocator;

public class DropdownSteps
{
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
        Locator dropdown = uiContext.locateElement(locator);

        playwrightSoftAssert.runAssertion("One unique dropdown by the locator must be present in the context", () ->
        {
            PlaywrightAssertions.assertThat(dropdown).hasCount(1);
            boolean optionPresent = dropdown.getByRole(AriaRole.OPTION).all().stream()
                    .anyMatch(o -> {
                        String value = o.getAttribute("value");
                        return value != null && value.equalsIgnoreCase(option);
                    });
            softAssert.assertTrue(String.format("The option \"%s\" is present in dropdown", option), optionPresent);
            if (optionPresent)
            {
                dropdown.selectOption(option);
            }
        });
    }
}
