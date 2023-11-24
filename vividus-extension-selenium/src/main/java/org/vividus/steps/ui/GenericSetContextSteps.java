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

package org.vividus.steps.ui;

import java.util.function.Supplier;

import org.jbehave.core.annotations.When;
import org.openqa.selenium.WebElement;
import org.vividus.annotation.Replacement;
import org.vividus.selenium.locator.Locator;
import org.vividus.steps.ui.validation.IBaseValidations;
import org.vividus.ui.context.IUiContext;
import org.vividus.ui.context.SearchContextSetter;
import org.vividus.ui.monitor.TakeScreenshotOnFailure;

@TakeScreenshotOnFailure
public class GenericSetContextSteps
{
    private static final String ELEMENT_TO_SET_CONTEXT = "Element to set context";
    private final IUiContext uiContext;
    private final IBaseValidations baseValidations;

    public GenericSetContextSteps(IUiContext uiContext, IBaseValidations baseValidations)
    {
        this.uiContext = uiContext;
        this.baseValidations = baseValidations;
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
     * @deprecated Use step: "When I change context to element located by `$locator`"
     */
    @Deprecated(since = "0.5.4", forRemoval = true)
    @Replacement(versionToRemoveStep = "0.7.0",
                 replacementFormatPattern = "When I change context to element located by `%1$s`")
    @When("I change context to element located `$locator`")
    public void resetAndChangeContextToElement(Locator locator)
    {
        resetContext();
        changeContext(() -> baseValidations.assertIfElementExists(ELEMENT_TO_SET_CONTEXT, locator),
            () -> resetAndChangeContextToElement(locator));
    }

    /**
     * Resets the context, finds the element by the given locator and sets the context to this element if it's found.
     *
     * @param locator The locator used to find the element to change context to.
     */
    @When("I change context to element located by `$locator`")
    public void changeContext(Locator locator)
    {
        resetContext();
        changeContext(() -> baseValidations.assertElementExists(ELEMENT_TO_SET_CONTEXT, locator).orElse(null),
            () -> changeContext(locator));
    }

    /**
     * Finds the element by the given locator in the current context and sets the context to this element if it's found.
     *
     * @param locator The locator used to find the element to change context to.
     */
    @When("I change context to element located by `$locator` in scope of current context")
    public void changeContextInScopeOfCurrentContext(Locator locator)
    {
        changeContext(() -> baseValidations.assertElementExists(ELEMENT_TO_SET_CONTEXT, locator).orElse(null),
            () -> changeContextInScopeOfCurrentContext(locator));
    }

    private void changeContext(Supplier<WebElement> locator, SearchContextSetter setter)
    {
        uiContext.putSearchContext(locator.get(), setter);
    }
}
