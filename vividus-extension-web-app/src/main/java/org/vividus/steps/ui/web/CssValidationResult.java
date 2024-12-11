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

package org.vividus.steps.ui.web;

import org.vividus.steps.StringComparisonRule;

public class CssValidationResult
{
    private CssValidationParameters cssValidationParameters;
    private String actualValue;
    private boolean passed;

    public CssValidationResult(CssValidationParameters cssValidationParameters, String actualValue, boolean passed)
    {
        this.cssValidationParameters = cssValidationParameters;
        this.actualValue = actualValue;
        this.passed = passed;
    }

    public String getCssProperty()
    {
        return cssValidationParameters.getCssProperty();
    }

    public StringComparisonRule getComparisonRule()
    {
        return cssValidationParameters.getComparisonRule();
    }

    public String getExpectedValue()
    {
        return cssValidationParameters.getExpectedValue();
    }

    public String getActualValue()
    {
        return actualValue;
    }

    public void setActualValue(String actualValue)
    {
        this.actualValue = actualValue;
    }

    public boolean isPassed()
    {
        return passed;
    }

    public void setPassed(boolean passed)
    {
        this.passed = passed;
    }
}
