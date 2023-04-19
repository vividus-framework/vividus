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

import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.openqa.selenium.WebElement;
import org.vividus.steps.ui.web.validation.FocusValidations;
import org.vividus.ui.context.IUiContext;
import org.vividus.ui.monitor.TakeScreenshotOnFailure;
import org.vividus.ui.web.action.WebJavascriptActions;

@TakeScreenshotOnFailure
public class FocusSteps
{
    private final IUiContext uiContext;
    private final WebJavascriptActions javascriptActions;
    private final FocusValidations focusValidations;

    public FocusSteps(IUiContext uiContext, WebJavascriptActions javascriptActions, FocusValidations focusValidations)
    {
        this.uiContext = uiContext;
        this.javascriptActions = javascriptActions;
        this.focusValidations = focusValidations;
    }

    /**
     * Sets the focus on the context element, if it can be focused. The focused element is the element that will
     * receive keyboard and similar events by default.
     */
    @When("I set focus on context element")
    public void setFocus()
    {
        uiContext.getSearchContext(WebElement.class).ifPresent(
                elementToCheck -> javascriptActions.executeScript("arguments[0].focus()", elementToCheck));
    }

    /**
     * Checks if the context element is in the provided focus state by comparing the context element and the active
     * element
     *
     * @param focusState The state to verify: "in focus" or "not in focus".
     */
    @Then("context element is $focusState")
    public void isElementInFocusState(FocusState focusState)
    {
        uiContext.getSearchContext(WebElement.class).ifPresent(
                elementToCheck -> focusValidations.isElementInFocusState(elementToCheck, focusState)
        );
    }
}
