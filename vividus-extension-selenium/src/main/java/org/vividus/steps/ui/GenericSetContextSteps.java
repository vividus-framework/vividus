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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.steps.ui.validation.IBaseValidations;
import org.vividus.ui.action.search.Locator;
import org.vividus.ui.context.IUiContext;
import org.vividus.ui.context.SearchContextSetter;
import org.vividus.ui.monitor.TakeScreenshotOnFailure;

@TakeScreenshotOnFailure
public class GenericSetContextSteps
{
    public static final String ELEMENT_TO_SET_CONTEXT = "Element to set context";
    private static final Logger LOGGER = LoggerFactory.getLogger(GenericSetContextSteps.class);
    private final IUiContext uiContext;
    private final IBaseValidations baseValidations;

    public GenericSetContextSteps(IUiContext uiContext, IBaseValidations baseValidations)
    {
        this.uiContext = uiContext;
        this.baseValidations = baseValidations;
    }

    /**
     * Resets the context
     */
    @When("I reset context")
    public void resetContext()
    {
        getUiContext().reset();
    }

    /**
     * Resets currently set context and
     * sets the context for further localization of elements to an <b>element</b> located by <b>locator</b>
     * @param locator locator to find an element
     * @deprecated Use step: "When I change context to element located by `$locator`"
     */
    @Deprecated(since = "0.5.4", forRemoval = true)
    @When("I change context to element located `$locator`")
    public void resetAndChangeContextToElement(Locator locator)
    {
        LOGGER.warn("The step: \"When I change context to element located `$locator`\" is deprecated "
                + "and will be removed in VIVIDUS 0.7.0. "
                + "Use step: \"When I change context to element located by `$locator`\"");
        resetContext();
        changeContext(() -> getBaseValidations().assertIfElementExists(ELEMENT_TO_SET_CONTEXT, locator),
            () -> resetAndChangeContextToElement(locator));
    }

    /**
     * Resets currently set context and
     * sets the context for further localization of elements to an <b>element</b> located by <b>locator</b>
     * @param locator locator to find an element
     */
    @When("I change context to element located by `$locator`")
    public void resetAndSetContextToElement(Locator locator)
    {
        resetContext();
        changeContext(() -> getBaseValidations().assertElementExists(ELEMENT_TO_SET_CONTEXT, locator).orElse(null),
            () -> resetAndSetContextToElement(locator));
    }

    /**
     * Sets the context for further localization of elements to an <b>element</b> located by <b>locator</b>
     * in scope of the current context.
     * @param locator locator to find an element
     */
    @When("I change context to element located by `$locator` in scope of current context")
    public void changeContextToElement(Locator locator)
    {
        changeContext(() -> getBaseValidations().assertElementExists(ELEMENT_TO_SET_CONTEXT, locator).orElse(null),
            () -> changeContextToElement(locator));
    }

    private void changeContext(Supplier<WebElement> locator, SearchContextSetter setter)
    {
        getUiContext().putSearchContext(locator.get(), setter);
    }

    protected IUiContext getUiContext()
    {
        return uiContext;
    }

    protected IBaseValidations getBaseValidations()
    {
        return baseValidations;
    }
}
