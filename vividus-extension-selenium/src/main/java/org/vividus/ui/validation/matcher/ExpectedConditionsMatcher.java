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

package org.vividus.ui.validation.matcher;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;

public class ExpectedConditionsMatcher extends TypeSafeMatcher<WebDriver>
{
    private static final String CONDITION_IS_NOT_MET = "[Condition is not met]";
    private static final String CONDITION_IS_MET = "[Condition is met]";

    private final ExpectedCondition<?> expectedCondition;

    public ExpectedConditionsMatcher(ExpectedCondition<?> expectedCondition)
    {
        this.expectedCondition = expectedCondition;
    }

    @Override
    public void describeTo(Description description)
    {
        description.appendText(CONDITION_IS_MET);
    }

    @Override
    public void describeMismatchSafely(WebDriver driver, Description mismatchDescription)
    {
        mismatchDescription.appendText(CONDITION_IS_NOT_MET);
    }

    @Override
    protected boolean matchesSafely(WebDriver driver)
    {
        Object result = expectedCondition.apply(driver);
        if (result instanceof Boolean)
        {
            return ((Boolean) result).booleanValue();
        }
        return result != null;
    }

    public static ExpectedConditionsMatcher expectedCondition(ExpectedCondition<?> expectedCondition)
    {
        return new ExpectedConditionsMatcher(expectedCondition);
    }
}
