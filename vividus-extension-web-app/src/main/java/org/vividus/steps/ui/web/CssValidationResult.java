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
    private String cssName;
    private String cssActualValue;
    private StringComparisonRule comparisonRule;
    private String cssExpectedValue;
    private boolean passed;

    public CssValidationResult(String cssName, String cssActualValue, StringComparisonRule comparisonRule,
                               String cssExpectedValue, boolean passed)
    {
        this.cssName = cssName;
        this.cssActualValue = cssActualValue;
        this.comparisonRule = comparisonRule;
        this.cssExpectedValue = cssExpectedValue;
        this.passed = passed;
    }

    public String getCssName()
    {
        return cssName;
    }

    public void setCssName(String cssName)
    {
        this.cssName = cssName;
    }

    public String getCssActualValue()
    {
        return cssActualValue;
    }

    public void setCssActualValue(String cssActualValue)
    {
        this.cssActualValue = cssActualValue;
    }

    public StringComparisonRule getComparisonRule()
    {
        return comparisonRule;
    }

    public void setComparisonRule(StringComparisonRule comparisonRule)
    {
        this.comparisonRule = comparisonRule;
    }

    public String getCssExpectedValue()
    {
        return cssExpectedValue;
    }

    public void setCssExpectedValue(String cssExpectedValue)
    {
        this.cssExpectedValue = cssExpectedValue;
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
