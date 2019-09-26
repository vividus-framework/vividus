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
import java.util.function.Function;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.openqa.selenium.WebElement;

public class DescribingElementNumberMatcher extends ElementNumberMatcher
{
    private final Function<List<? extends WebElement>, String> elementsDescriptionProvider;

    public DescribingElementNumberMatcher(Matcher<? super Integer> matcher,
            Function<List<? extends WebElement>, String> elementsDescriptionProvider)
    {
        super(matcher);
        this.elementsDescriptionProvider = elementsDescriptionProvider;
    }

    @Override
    protected void describeMismatchSafely(List<? extends WebElement> item, Description mismatchDescription)
    {
        mismatchDescription.appendValue(item.size());
        // Workaround for passed cases:
        // SoftAssert uses "describeMismatch" to generate successful assertion description
        if (!item.isEmpty() && !matchesSafely(item))
        {
            mismatchDescription.appendText(".\nFound elements" + elementsDescriptionProvider.apply(item) + "\n");
        }
    }

    public static DescribingElementNumberMatcher elementNumber(Matcher<? super Integer> matcher,
            Function<List<? extends WebElement>, String> elementsDescriptionProvider)
    {
        return new DescribingElementNumberMatcher(matcher, elementsDescriptionProvider);
    }
}
