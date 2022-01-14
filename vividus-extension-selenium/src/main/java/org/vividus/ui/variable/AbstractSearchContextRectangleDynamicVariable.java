/*
 * Copyright 2019-2022 the original author or authors.
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

import java.util.Optional;
import java.util.function.ToIntFunction;

import org.openqa.selenium.Rectangle;
import org.openqa.selenium.WebElement;
import org.vividus.ui.context.UiContext;
import org.vividus.variable.DynamicVariable;
import org.vividus.variable.DynamicVariableCalculationResult;

public abstract class AbstractSearchContextRectangleDynamicVariable implements DynamicVariable
{
    private final UiContext uiContext;
    private final ToIntFunction<Rectangle> valueProvider;

    protected AbstractSearchContextRectangleDynamicVariable(UiContext uiContext, ToIntFunction<Rectangle> valueProvider)
    {
        this.uiContext = uiContext;
        this.valueProvider = valueProvider;
    }

    @Override
    public DynamicVariableCalculationResult calculateValue()
    {
        return DynamicVariableCalculationResult.withValueOrError(
                Optional.ofNullable(uiContext.getSearchContext(WebElement.class))
                        .map(WebElement::getRect)
                        .map(valueProvider::applyAsInt)
                        .map(String::valueOf),
                () -> "the search context is not set"
        );
    }
}
