/*
 * Copyright 2019-2022 the original author or authors.
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

import org.jbehave.core.annotations.When;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.selenium.element.Checkbox;
import org.vividus.steps.ui.validation.IBaseValidations;
import org.vividus.ui.action.search.Locator;
import org.vividus.ui.monitor.TakeScreenshotOnFailure;
import org.vividus.ui.web.action.CheckboxAction;
import org.vividus.ui.web.action.IMouseActions;

@TakeScreenshotOnFailure
public class CheckboxSteps
{
    private static final Logger LOGGER = LoggerFactory.getLogger(CheckboxSteps.class);

    private static final String CHECKBOX = "Checkbox";

    private final IBaseValidations baseValidations;
    private final IMouseActions mouseActions;

    public CheckboxSteps(IBaseValidations baseValidations, IMouseActions mouseActions)
    {
        this.baseValidations = baseValidations;
        this.mouseActions = mouseActions;
    }

    /**
     * @deprecated Use steps: "When I $checkBoxAction checkbox located by `$checkboxLocator`" and
     * "When I find $comparisonRule `$number` elements by `$locator` and for each element do$stepsToExecute".
     *
     * Performs action on all the checkboxes found by locator
     * @param checkBoxAction Actions to be performed (CHECK, UNCHECK)
     * @param checkboxesLocator Locator to locate checkboxes
     */
    @Deprecated(since = "0.5.0", forRemoval = true)
    @When("I $checkboxAction all checkboxes located by `$checkboxesLocator`")
    public void changeStateOfAllCheckboxes(CheckboxAction checkBoxAction, Locator checkboxesLocator)
    {
        LOGGER.warn("The step: \"I $checkboxAction all checkboxes located by `$checkboxesLocator`\" is deprecated and "
                + "will be removed in VIVIDUS 0.6.0. Use steps: \"When I $checkBoxAction checkbox located by "
                + "`$checkboxLocator`\" and \"When I find $comparisonRule `$number` elements by `$locator` and for each"
                + " element do$stepsToExecute\"");
        List<WebElement> checkboxes = baseValidations.assertIfElementsExist("Checkboxes", checkboxesLocator);
        checkboxes.stream().map(this::createCheckbox).forEach(checkbox -> changeCheckboxState(checkbox,
                checkBoxAction));
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
        Checkbox checkbox = createCheckbox(checkboxElement);
        changeCheckboxState(checkbox, checkBoxAction);
    }

    private Checkbox createCheckbox(WebElement checkbox)
    {
        return checkbox == null || checkbox instanceof Checkbox ? (Checkbox) checkbox : new Checkbox(checkbox);
    }

    private void changeCheckboxState(Checkbox checkbox, CheckboxAction action)
    {
        if (checkbox != null && checkbox.getWrappedElement() != null && checkbox.isSelected() != action.isSelected())
        {
            WebElement elementToClick = getClickableElement(checkbox);
            mouseActions.click(elementToClick);
        }
    }

    private WebElement getClickableElement(Checkbox checkbox)
    {
        return checkbox.isDisplayed() ? checkbox : checkbox.getLabelElement();
    }
}
