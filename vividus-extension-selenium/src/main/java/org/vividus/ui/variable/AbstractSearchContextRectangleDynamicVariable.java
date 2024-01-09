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

package org.vividus.ui.variable;

import java.util.function.ToIntFunction;

import org.openqa.selenium.Rectangle;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.ui.context.UiContext;
import org.vividus.variable.DynamicVariable;
import org.vividus.variable.DynamicVariableCalculationResult;

@Deprecated(forRemoval = true, since = "0.6.6")
public abstract class AbstractSearchContextRectangleDynamicVariable implements DynamicVariable
{
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractSearchContextRectangleDynamicVariable.class);

    private final String expression;
    private final String attribute;
    private final UiContext uiContext;
    private final ToIntFunction<Rectangle> valueProvider;

    protected AbstractSearchContextRectangleDynamicVariable(String expression, String attribute, UiContext uiContext,
            ToIntFunction<Rectangle> valueProvider)
    {
        this.expression = expression;
        this.attribute = attribute;
        this.uiContext = uiContext;
        this.valueProvider = valueProvider;
    }

    @Override
    public DynamicVariableCalculationResult calculateValue()
    {
        LOGGER.atWarn().addArgument(expression)
                       .addArgument("When I save coordinates and size of element located by `$locator` to $scopes "
                               + "variable `$variableName`")
                       .addArgument(attribute)
                       .log("The '{}' dynamic variable is deprecated and will be removed in VIVIDUS 0.7.0, please use"
                               + " '{}' step to save location adn coodinates, and then use '{}' property to get target "
                               + "value.");
        return DynamicVariableCalculationResult.withValueOrError(
                uiContext.getSearchContext(WebElement.class)
                        .map(WebElement::getRect)
                        .map(valueProvider::applyAsInt)
                        .map(String::valueOf),
                () -> "the search context is not set"
        );
    }
}
