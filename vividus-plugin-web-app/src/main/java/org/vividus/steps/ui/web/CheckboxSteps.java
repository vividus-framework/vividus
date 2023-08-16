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

import org.jbehave.core.annotations.When;
import org.openqa.selenium.WebElement;
import org.vividus.selenium.element.Checkbox;
import org.vividus.steps.ui.validation.IBaseValidations;
import org.vividus.ui.action.search.Locator;
import org.vividus.ui.monitor.TakeScreenshotOnFailure;
import org.vividus.ui.web.action.CheckboxAction;
import org.vividus.ui.web.action.IMouseActions;

@TakeScreenshotOnFailure
public class CheckboxSteps
{
    private static final String CHECKBOX = "Checkbox";

    private final IBaseValidations baseValidations;
    private final IMouseActions mouseActions;

    public CheckboxSteps(IBaseValidations baseValidations, IMouseActions mouseActions)
    {
        this.baseValidations = baseValidations;
        this.mouseActions = mouseActions;
    }

    /**
     * Performs action on checkbox found by locator
     * @param checkBoxAction Actions to be performed (CHECK, UNCHECK)
     * @param checkboxLocator Locator to locate checkboxes
     */
    @When("I $checkBoxAction checkbox located by `$checkboxLocator`")
    public void changeStateOfCheckbox(CheckboxAction checkBoxAction, Locator checkboxLocator)
    {
        WebElement checkboxElement = baseValidations.assertIfElementExists(CHECKBOX, checkboxLocator);
        if (checkboxElement != null)
        {
            Checkbox checkbox = checkboxElement instanceof Checkbox ? (Checkbox) checkboxElement : new Checkbox(
                    checkboxElement);
            if (checkbox.getWrappedElement() != null && checkbox.isSelected() != checkBoxAction.isSelected())
            {
                WebElement elementToClick = checkbox.isDisplayed() ? checkbox : checkbox.getLabelElement();
                mouseActions.click(elementToClick);
            }
        }
    }
}
