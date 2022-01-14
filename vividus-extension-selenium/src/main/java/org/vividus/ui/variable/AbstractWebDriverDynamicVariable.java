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

import java.util.function.Function;

import org.openqa.selenium.WebDriver;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.variable.DynamicVariable;
import org.vividus.variable.DynamicVariableCalculationResult;

public abstract class AbstractWebDriverDynamicVariable implements DynamicVariable
{
    private final IWebDriverProvider webDriverProvider;
    private final Function<WebDriver, String> valueMapper;
    private final String errorOnNotStartedSession;

    protected AbstractWebDriverDynamicVariable(IWebDriverProvider webDriverProvider,
            Function<WebDriver, String> valueMapper, String errorOnNotStartedSession)
    {
        this.webDriverProvider = webDriverProvider;
        this.valueMapper = valueMapper;
        this.errorOnNotStartedSession = errorOnNotStartedSession;
    }

    protected AbstractWebDriverDynamicVariable(IWebDriverProvider webDriverProvider,
            Function<WebDriver, String> valueMapper)
    {
        this(webDriverProvider, valueMapper, "application is not started");
    }

    @Override
    public DynamicVariableCalculationResult calculateValue()
    {
        if (webDriverProvider.isWebDriverInitialized())
        {
            return DynamicVariableCalculationResult.withValue(valueMapper.apply(webDriverProvider.get()));
        }
        return DynamicVariableCalculationResult.withError(errorOnNotStartedSession);
    }
}
