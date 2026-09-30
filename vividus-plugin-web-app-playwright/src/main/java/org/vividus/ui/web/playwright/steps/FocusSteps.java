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

import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.vividus.softassert.ISoftAssert;
import org.vividus.ui.web.playwright.UiContext;
import org.vividus.ui.web.playwright.locator.PlaywrightLocator;
import org.vividus.ui.web.playwright.model.FocusState;
import org.vividus.util.EnumUtils;

public class FocusSteps
{
    private static final String IS_ACTIVE_ELEMENT_SCRIPT = "el => el === document.activeElement";

    private final UiContext uiContext;
    private final ISoftAssert softAssert;

    public FocusSteps(UiContext uiContext, ISoftAssert softAssert)
    {
        this.uiContext = uiContext;
        this.softAssert = softAssert;
    }

    /**
     * Sets the focus on the context element, if it can be focused. The focused element is the element that will
     * receive keyboard and similar events by default. The step does nothing if the context is not set.
     */
    @When("I set focus on context element")
    public void setFocusOnContextElement()
    {
        Locator context = uiContext.getContext();
        if (context != null)
        {
            context.focus();
        }
    }

    /**
     * Sets the focus on the element found by the specified locator, if it can be focused. The focused element is the
     * element that will receive keyboard and similar events by default.
     *
     * @param locator The locator used to find an element.
     */
    @When("I set focus on element located by `$locator`")
    public void setFocusOnElement(PlaywrightLocator locator)
    {
        uiContext.locateElement(locator).focus();
    }

    /**
     * Checks if the context element is in the provided focus state by comparing the context element and the active
     * element. The step does nothing if the context is not set.
     *
     * @param focusState The state to verify: <b>IN_FOCUS</b> or <b>NOT_IN_FOCUS</b>.
     */
    @Then("context element is $focusState")
    public void isContextElementInFocusState(FocusState focusState)
    {
        Locator context = uiContext.getContext();
        if (context != null)
        {
            assertFocusState(context, focusState);
        }
    }

    /**
     * Checks if the element found by the specified locator is in the provided focus state by comparing the found
     * element and the active element.
     *
     * @param locator    The locator used to find an element.
     * @param focusState The state to verify: <b>IN_FOCUS</b> or <b>NOT_IN_FOCUS</b>.
     */
    @Then("element located by `$locator` is $focusState")
    public void isElementInFocusState(PlaywrightLocator locator, FocusState focusState)
    {
        assertFocusState(uiContext.locateElement(locator), focusState);
    }

    private void assertFocusState(Locator element, FocusState focusState)
    {
        boolean elementInFocus = (boolean) element.evaluate(IS_ACTIVE_ELEMENT_SCRIPT);
        softAssert.assertTrue("The element is " + EnumUtils.toHumanReadableForm(focusState),
                focusState.matches(elementInFocus));
    }
}
