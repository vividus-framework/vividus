/*
 * Copyright 2019 the original author or authors.
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

package org.vividus.ui.web.action;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.Select;

public final class ExpectedConditions
{
    private ExpectedConditions()
    {
        // nothing to do
    }

    public static ExpectedCondition<Boolean> isMultiSelectDropDown(WebElement element, boolean multiSelect)
    {
        return new ExpectedCondition<>()
        {
            @Override
            public Boolean apply(WebDriver driver)
            {
                return isMultiSelect(element, multiSelect);
            }

            @Override
            public String toString()
            {
                return String.format("An element (%s) is %s select", element, multiSelectToString(multiSelect));
            }

            private Boolean isMultiSelect(WebElement element, boolean multiSelect)
            {
                return new Select(element).isMultiple() == multiSelect;
            }
        };
    }

    static String multiSelectToString(boolean multiSelect)
    {
        return multiSelect ? "multiple" : "single";
    }
}
