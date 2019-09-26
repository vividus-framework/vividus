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

package org.vividus.ui.validation.matcher;

import java.util.List;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.openqa.selenium.WebElement;

public class ExistsMatcher extends TypeSafeMatcher<List<WebElement>>
{
    @Override
    protected boolean matchesSafely(List<WebElement> elements)
    {
        return elements.size() == 1;
    }

    @Override
    public void describeTo(Description description)
    {
        description.appendText("exists");
    }

    @Override
    public void describeMismatchSafely(List<WebElement> elements, Description mismatchDescription)
    {
        mismatchDescription.appendText("does not exist");
    }

    public static ExistsMatcher exists()
    {
        return new ExistsMatcher();
    }
}
