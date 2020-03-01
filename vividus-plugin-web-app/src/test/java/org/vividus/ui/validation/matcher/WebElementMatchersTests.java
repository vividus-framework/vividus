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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;

import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WebElementMatchersTests
{
    @Mock
    private Matcher<? super Integer> matcher;

    @Test
    void testElementNumber()
    {
        assertThat(WebElementMatchers.elementNumber(matcher), instanceOf(ElementNumberMatcher.class));
    }

    @Test
    void testDescribingElementNumber()
    {
        assertThat(WebElementMatchers.describingElementNumber(matcher, e -> ""),
                instanceOf(DescribingElementNumberMatcher.class));
    }
}
