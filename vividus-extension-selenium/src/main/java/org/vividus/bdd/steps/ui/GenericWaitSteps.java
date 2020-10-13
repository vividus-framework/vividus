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

package org.vividus.bdd.steps.ui;

import org.jbehave.core.annotations.When;
import org.vividus.bdd.monitor.TakeScreenshotOnFailure;
import org.vividus.ui.action.IExpectedConditions;
import org.vividus.ui.action.IExpectedSearchContextCondition;
import org.vividus.ui.action.IWaitActions;
import org.vividus.ui.action.search.Locator;
import org.vividus.ui.context.IUiContext;

@TakeScreenshotOnFailure
public class GenericWaitSteps
{
    private final IWaitActions waitActions;
    private final IUiContext uiContext;
    private final IExpectedConditions<Locator> expectedSearchActionsConditions;

    public GenericWaitSteps(IWaitActions waitActions, IUiContext uiContext,
            IExpectedConditions<Locator> expectedSearchActionsConditions)
    {
        this.waitActions = waitActions;
        this.uiContext = uiContext;
        this.expectedSearchActionsConditions = expectedSearchActionsConditions;
    }

    /**
     * Waits for appearance of an <b><i>element</i></b> with the specified <b>locator</b>
     * @param locator locator to locate element
     */
    @When("I wait until element located `$locator` appears")
    public void waitForElementAppearance(Locator locator)
    {
        waitForCondition(expectedSearchActionsConditions.visibilityOfElement(locator));
    }

    /**
     * Waits for disappearance of an <b><i>element</i></b> with the specified <b>locator</b>
     * @param locator locator to locate element
     */
    @When("I wait until element located `$locator` disappears")
    public void waitForElementDisappearance(Locator locator)
    {
        waitForCondition(expectedSearchActionsConditions.invisibilityOfElement(locator));
    }

    private void waitForCondition(IExpectedSearchContextCondition<?> condition)
    {
        waitActions.wait(uiContext.getSearchContext(), condition);
    }
}
