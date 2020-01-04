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

import javax.inject.Inject;

import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.openqa.selenium.WebElement;
import org.vividus.bdd.monitor.TakeScreenshotOnFailure;
import org.vividus.bdd.steps.ui.web.validation.IFocusValidations;
import org.vividus.ui.web.action.IJavascriptActions;
import org.vividus.ui.web.context.IWebUiContext;

@TakeScreenshotOnFailure
public class FocusSteps
{
    @Inject private IWebUiContext webUiContext;
    @Inject private IJavascriptActions javascriptActions;
    @Inject private IFocusValidations focusValidations;

    /**
     * Step sets focus to an element in context
     * <p>
     * Actions performed at this step:
     * </p>
     * <ul>
     * <li>Sets focus by performing javascript method focus() to found element.</li>
     * </ul>
     */
    @When("I set focus to the context element")
    public void setFocus()
    {
        WebElement elementToCheck = getWebElementFromContext();
        if (null != elementToCheck)
        {
            javascriptActions.executeScript("arguments[0].focus()", elementToCheck);
        }
    }

    /**
     * Step checks if the context element in given focus state
     * <p>
     * Actions performed at this step:
     * </p>
     * <ul>
     * <li>Gets element from context</li>
     * <li>Checks focus state of element by comparing given the element and the element
     *  returned by activeElement javascript method</li>
     * </ul>
     * @param focusState state to verify
     */
    @Then("the context element is $focusState")
    public void isElementInFocusState(FocusState focusState)
    {
        focusValidations.isElementInFocusState(getWebElementFromContext(), focusState);
    }

    private WebElement getWebElementFromContext()
    {
        return webUiContext.getSearchContext(WebElement.class);
    }
}
