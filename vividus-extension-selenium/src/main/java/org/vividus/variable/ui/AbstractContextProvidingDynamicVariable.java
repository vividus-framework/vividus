/*
 * Copyright 2019-2021 the original author or authors.
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

package org.vividus.variable.ui;

import java.util.function.Function;

import org.openqa.selenium.Rectangle;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.ui.context.UiContext;
import org.vividus.variable.DynamicVariable;

public abstract class AbstractContextProvidingDynamicVariable implements DynamicVariable
{
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractContextProvidingDynamicVariable.class);

    private final UiContext uiContext;

    AbstractContextProvidingDynamicVariable(UiContext uiContext)
    {
        this.uiContext = uiContext;
    }

    protected String getContextRectValue(Function<Rectangle, Integer> valueProvider)
    {
        return  uiContext.getSearchContext(WebElement.class)
                        .map(WebElement::getRect)
                        .map(valueProvider)
                        .map(String::valueOf)
                        .orElseGet(() -> {
                            LOGGER.atError().log("Unable to get coordinate, context is not set");
                            return "-1";
                        });
    }
}
