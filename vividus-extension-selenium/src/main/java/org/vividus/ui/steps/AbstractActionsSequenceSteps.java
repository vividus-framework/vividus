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

package org.vividus.ui.steps;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.steps.ui.validation.IBaseValidations;
import org.vividus.ui.action.AtomicAction;
import org.vividus.ui.action.search.Locator;

public class AbstractActionsSequenceSteps
{
    private final IWebDriverProvider webDriverProvider;
    private final IBaseValidations baseValidations;

    protected AbstractActionsSequenceSteps(IWebDriverProvider webDriverProvider, IBaseValidations baseValidations)
    {
        this.webDriverProvider = webDriverProvider;
        this.baseValidations = baseValidations;
    }

    protected <T extends Actions> void execute(Function<WebDriver, T> actionsBuilderConstructor,
            List<AtomicAction<T>> actions)
    {
        T actionBuilder = actionsBuilderConstructor.apply(webDriverProvider.get());
        for (AtomicAction<T> action : actions)
        {
            Object argument = action.getArgument();
            if (argument != null && argument.getClass().equals(Locator.class))
            {
                Optional<WebElement> webElement = baseValidations.assertElementExists("Element for interaction",
                        (Locator) argument);
                if (webElement.isEmpty())
                {
                    return;
                }
                argument = webElement.get();
            }
            action.getActionFactory().addAction(actionBuilder, argument);
        }
        actionBuilder.perform();
    }
}
