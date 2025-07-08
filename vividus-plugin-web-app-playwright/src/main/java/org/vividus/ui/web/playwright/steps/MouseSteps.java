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

import java.util.function.Consumer;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Locator.ClickOptions;
import com.microsoft.playwright.TimeoutError;
import com.microsoft.playwright.options.MouseButton;

import org.jbehave.core.annotations.When;
import org.vividus.softassert.ISoftAssert;
import org.vividus.ui.web.playwright.UiContext;
import org.vividus.ui.web.playwright.locator.PlaywrightLocator;

public class MouseSteps
{
    private final UiContext uiContext;
    private final ISoftAssert softAssert;

    public MouseSteps(UiContext uiContext, ISoftAssert softAssert)
    {
        this.uiContext = uiContext;
        this.softAssert = softAssert;
    }

    /**
     * Finds the element by the given locator and performs a click in the center of the element if it's found
     * (at first moves mouse to the location of the element).
     *
     * @param locator The locator used to find the element to click
     */
    @When("I click on element located by `$locator`")
    public void clickOnElement(PlaywrightLocator locator)
    {
        Consumer<Locator> clickAction = element -> {
            element.click();
            afterClick();
        };
        runMouseAction(locator, clickAction, "click");
    }

    /**
     * Finds the element by the given locator and performs a right-click in the center of the element if it's found
     * (at first moves mouse to the location of the element).
     *
     * @param locator The locator used to find the element to right-click
     */
    @When("I perform right-click on element located by `$locator`")
    public void rightClickOnElement(PlaywrightLocator locator)
    {
        runMouseAction(locator, l -> l.click(new ClickOptions().setButton(MouseButton.RIGHT)), "right-click");
    }

    /**
     * Finds the element by the given locator and moves a mouse cursor over the center of the element, if it's found.
     *
     * @param locator The locator used to find the element to hover mouse over
     */
    @When("I hover mouse over element located by `$locator`")
    public void hoverMouseOverElement(PlaywrightLocator locator)
    {
        runMouseAction(locator, Locator::hover, "hover mouse over");
    }

    private void runMouseAction(PlaywrightLocator locator, Consumer<Locator> mouseAction, String actionDescription)
    {
        try
        {
            mouseAction.accept(uiContext.locateElement(locator));
        }
        catch (TimeoutError timeoutError)
        {
            softAssert.recordFailedAssertion("The element to " + actionDescription + " is not found", timeoutError);
        }
    }

    private void afterClick()
    {
        uiContext.getCurrentPage().waitForLoadState();
        uiContext.resetToActiveFrame();
    }
}
