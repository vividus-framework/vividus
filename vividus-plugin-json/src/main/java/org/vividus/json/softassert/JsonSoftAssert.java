/*
 * Copyright 2019-2021 the original author or authors.
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

package org.vividus.json.softassert;

import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;
import org.vividus.softassert.SoftAssert;

import net.javacrumbs.jsonunit.ConfigurableJsonMatcher;

public class JsonSoftAssert extends SoftAssert
{
    @Override
    public <T> String getAssertionDescriptionString(T actual, Matcher<? super T> matcher)
    {
        if (matcher instanceof ConfigurableJsonMatcher)
        {
            if (matcher.matches(actual))
            {
                return "Condition is true";
            }
            StringDescription mismatchStringDescription = new StringDescription();
            matcher.describeMismatch(actual, mismatchStringDescription);
            return mismatchStringDescription.toString();
        }
        return super.getAssertionDescriptionString(actual, matcher);
    }
}
