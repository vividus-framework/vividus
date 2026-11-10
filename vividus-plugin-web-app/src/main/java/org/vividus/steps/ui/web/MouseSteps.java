/*
 * Copyright 2019-2025 the original author or authors.
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
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.vividus.annotation.Replacement;
import org.vividus.selenium.locator.Locator;
import org.vividus.softassert.ISoftAssert;
import org.vividus.steps.ui.validation.IBaseValidations;
import org.vividus.ui.monitor.TakeScreenshotOnFailure;
import org.vividus.ui.web.action.ClickResult;
import org.vividus.ui.web.action.IMouseActions;

@TakeScreenshotOnFailure
public class MouseSteps
{
    private static final String AN_ELEMENT_TO_CLICK = "An element to click";
    private static final String PAGE_NOT_REFRESHED_AFTER_CLICKING_ON_ELEMENT_LOCATED = "Page has not been refreshed"
            + " after clicking on the element located by";

    private final IMouseActions mouseActions;
    private final ISoftAssert softAssert;
    private final IBaseValidations baseValidations;

    public MouseSteps(IMouseActions mouseActions, ISoftAssert softAssert, IBaseValidations baseValidations)
    {
        this.mouseActions = mouseActions;
        this.softAssert = softAssert;
        this.baseValidations = baseValidations;
    }

    /**
     * Clicks on the element found by the given locator.
     * <p>The atomic actions performed are:</p>
     * <ul>
     * <li>find the element by the locator;</li>
     * <li>click on the element if it is found, otherwise the whole step is failed and its execution stops;</li>
     * <li>the first two actions are retried once if the field becomes stale during actions execution in other words if
     * <a href="https://www.selenium.dev/exceptions/#stale_element_reference">StaleElementReferenceException</a>
     * is occurred at any atomic action.</li>
     * </ul>
     * @param locator The locator used to find the element to click
     */
    @When("I click on element located by `$locator`")
    public void clickOnElement(Locator locator)
    {
        try
        {
            findAndClick(locator);
        }
        catch (StaleElementReferenceException thrown)
        {
            findAndClick(locator);
        }
    }

    /**
     * Finds the element by the given locator and performs a right-click  in the center of the element if it's found
     * (at first moves mouse to the location of the element).
     *
     * @param locator The locator used to find the element to right-click
     * @deprecated Use step: "When I perform right click on element located by `$locator`" instead
     */
    @Deprecated(since = "0.6.0", forRemoval = true)
    @Replacement(versionToRemoveStep = "0.7.0",
            replacementFormatPattern = "When I perform right-click on element located by `%1$s`")
    @When("I perform right click on element located `$locator`")
    public void contextClickElementByLocator(Locator locator)
    {
        WebElement element = baseValidations.assertIfElementExists(AN_ELEMENT_TO_CLICK, locator);
        mouseActions.contextClick(element);
    }

    /**
     * Finds the element by the given locator and performs a right-click  in the center of the element if it's found
     * (at first moves mouse to the location of the element).
     *
     * @param locator The locator used to find the element to right-click
     */
    @When("I perform right-click on element located by `$locator`")
    public void rightClickOnElement(Locator locator)
    {
        baseValidations.assertElementExists(AN_ELEMENT_TO_CLICK, locator).ifPresent(mouseActions::contextClick);
    }

    /**
     * Clicks on the element with the provided search attributes and verify that page has not been reloaded
     * <p>Actions performed at this step:</p>
     * <ul>
     * <li>Assert that element with specified search attributes is found on the page;</li>
     * <li>Clicks on the element;</li>
     * <li>Assert that page has not been refreshed after click</li>
     * </ul>
     * @param locator The locator used to find the element to click
     * @deprecated Use step: "When I click on element located by `$locator` then page does not refresh" instead
     */
    @Deprecated(since = "0.6.0", forRemoval = true)
    @Replacement(versionToRemoveStep = "0.7.0",
            replacementFormatPattern = "When I click on element located by `%1$s` then page does not refresh")
    @When("I click on an element '$locator' then the page does not refresh")
    public void clickElementPageNotRefresh(Locator locator)
    {
        WebElement element = baseValidations.assertIfElementExists(AN_ELEMENT_TO_CLICK, locator);
        ClickResult clickResult = mouseActions.click(element);
        softAssert.assertTrue(
                PAGE_NOT_REFRESHED_AFTER_CLICKING_ON_ELEMENT_LOCATED + locator, !clickResult.isNewPageLoaded());
    }

    /**
     * Clicks on the element with the provided search attributes and verify that page has not been reloaded
     * <p>Actions performed at this step:</p>
     * <ul>
     * <li>Assert that element with specified search attributes is found on the page;</li>
     * <li>Clicks on the element;</li>
     * <li>Assert that page has not been refreshed after click</li>
     * </ul>
     * @param locator The locator used to find the element to click
     */
    @When("I click on element located by `$locator` then page does not refresh")
    public void clickElementAndPageNotRefresh(Locator locator)
    {
        baseValidations.assertElementExists(AN_ELEMENT_TO_CLICK, locator).ifPresent(element ->
            {
                ClickResult clickResult = mouseActions.click(element);
                softAssert.assertTrue(PAGE_NOT_REFRESHED_AFTER_CLICKING_ON_ELEMENT_LOCATED + locator,
                        !clickResult.isNewPageLoaded());
            }
        );
    }

    /**
     * Finds the element by the given locator and moves a mouse cursor over the center of the element, if it's found.
     *
     * @param locator The locator used to find the element to hover mouse over
     * @deprecated Use step: "When I hover mouse over element located by `$locator`" instead
     */
    @Deprecated(since = "0.6.0", forRemoval = true)
    @Replacement(versionToRemoveStep = "0.7.0",
            replacementFormatPattern = "When I hover mouse over element located by `%1$s`")
    @When("I hover mouse over element located `$locator`")
    public void hoverMouseOverElementByLocator(Locator locator)
    {
        WebElement element = baseValidations.assertIfElementExists(
                String.format("An element with attributes%1$s", locator), locator);
        mouseActions.moveToElement(element);
    }

    /**
     * Finds the element by the given locator and moves a mouse cursor over the center of the element, if it's found.
     *
     * @param locator The locator used to find the element to hover mouse over
     */
    @When("I hover mouse over element located by `$locator`")
    public void hoverMouseOverElement(Locator locator)
    {
        baseValidations.assertElementExists(
                        String.format("An element to hover mouse over%s", locator), locator)
                .ifPresent(mouseActions::moveToElement);
    }

    private void findAndClick(Locator locator)
    {
        baseValidations.assertElementExists("Element to click", locator).ifPresent(mouseActions::click);
    }
}
