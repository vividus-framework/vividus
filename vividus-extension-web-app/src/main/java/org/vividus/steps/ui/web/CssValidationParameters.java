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

import org.jbehave.core.annotations.AsParameters;
import org.vividus.steps.StringComparisonRule;

@AsParameters
public class CssValidationParameters
{
    private String cssProperty;
    private StringComparisonRule comparisonRule;
    private String expectedValue;

    public CssValidationParameters()
    {
    }

    public CssValidationParameters(String cssProperty, StringComparisonRule comparisonRule, String expectedValue)
    {
        this.cssProperty = cssProperty;
        this.comparisonRule = comparisonRule;
        this.expectedValue = expectedValue;
    }

    public String getCssProperty()
    {
        return cssProperty;
    }

    public void setCssProperty(String cssProperty)
    {
        this.cssProperty = cssProperty;
    }

    public StringComparisonRule getComparisonRule()
    {
        return comparisonRule;
    }

    public void setComparisonRule(StringComparisonRule comparisonRule)
    {
        this.comparisonRule = comparisonRule;
    }

    public String getExpectedValue()
    {
        return expectedValue;
    }

    public void setExpectedValue(String expectedValue)
    {
        this.expectedValue = expectedValue;
    }
}
