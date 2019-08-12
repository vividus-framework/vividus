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

import org.hamcrest.Matcher;
import org.openqa.selenium.WebElement;

public final class WebElementMatchers
{
    private WebElementMatchers()
    {
    }

    public static ElementNumberMatcher elementNumber(Matcher<? super Integer> matcher)
    {
        return ElementNumberMatcher.elementNumber(matcher);
    }

    public static DescribingElementNumberMatcher describingElementNumber(Matcher<? super Integer> matcher,
            Function<List<? extends WebElement>, String> elementsDescriptionProvider)
    {
        return DescribingElementNumberMatcher.elementNumber(matcher, elementsDescriptionProvider);
    }
}
