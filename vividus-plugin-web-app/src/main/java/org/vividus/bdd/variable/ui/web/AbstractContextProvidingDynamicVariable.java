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

package org.vividus.bdd.variable.ui.web;

import java.util.function.Function;

import org.openqa.selenium.Rectangle;
import org.openqa.selenium.WebElement;
import org.vividus.bdd.variable.DynamicVariable;
import org.vividus.ui.web.context.WebUiContext;

public abstract class AbstractContextProvidingDynamicVariable implements DynamicVariable
{
    private final WebUiContext webUiContext;

    public AbstractContextProvidingDynamicVariable(WebUiContext webUiContext)
    {
        this.webUiContext = webUiContext;
    }

    protected String getContextRectValue(Function<Rectangle, Integer> valueProvider)
    {
        WebElement searchContext = webUiContext.getSearchContext(WebElement.class);
        return String.valueOf(valueProvider.apply(searchContext.getRect()));
    }
}
