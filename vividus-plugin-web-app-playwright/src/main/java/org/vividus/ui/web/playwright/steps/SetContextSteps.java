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

package org.vividus.ui.web.playwright.steps;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.assertions.PlaywrightAssertions;

import org.jbehave.core.annotations.When;
import org.opentest4j.AssertionFailedError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.softassert.ISoftAssert;
import org.vividus.ui.web.playwright.UiContext;
import org.vividus.ui.web.playwright.locator.PlaywrightLocator;

public class SetContextSteps
{
    private static final Logger LOGGER = LoggerFactory.getLogger(SetContextSteps.class);

    private final UiContext uiContext;
    private final ISoftAssert softAssert;

    public SetContextSteps(UiContext uiContext, ISoftAssert softAssert)
    {
        this.uiContext = uiContext;
        this.softAssert = softAssert;
    }

    /**
     * Resets the context if it was set previously.
     */
    @When("I reset context")
    public void resetContext()
    {
        uiContext.reset();
    }

    /**
     * Resets the context, finds the element by the given locator and sets the context to this element if it's found.
     *
     * @param locator The locator used to find the element to change context to.
     */
    @When("I change context to element located by `$locator`")
    public void changeContext(PlaywrightLocator locator)
    {
        resetContext();
        changeContextInScopeOfCurrentContext(locator);
    }

    /**
     * Finds the element by the given locator in the current context and sets the context to this element if it's found.
     *
     * @param locator The locator used to find the element to change context to.
     */
    @When("I change context to element located by `$locator` in scope of current context")
    public void changeContextInScopeOfCurrentContext(PlaywrightLocator locator)
    {
        Locator context = uiContext.locateElement(locator);
        try
        {
            PlaywrightAssertions.assertThat(context).hasCount(1);
            uiContext.setContext(context);
            LOGGER.info("The context is successfully changed");
        }
        catch (AssertionFailedError e)
        {
            softAssert.recordFailedAssertion("The element to set context is not found. " + e.getMessage(), e);
        }
    }
}
