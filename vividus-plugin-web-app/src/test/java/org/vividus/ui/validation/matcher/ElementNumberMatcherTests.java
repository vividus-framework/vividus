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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebElement;

@ExtendWith(MockitoExtension.class)
class ElementNumberMatcherTests
{
    @Mock
    private List<WebElement> item;

    @Mock
    private Matcher<? super Integer> matcher;

    @Mock
    private WebElement webElement;

    @Mock
    private Description description;

    @InjectMocks
    private ElementNumberMatcher elementNumberMatcher;

    @Test
    void testDescribeTo()
    {
        when(description.appendText("number of elements is ")).thenReturn(description);
        elementNumberMatcher.describeTo(description);
        verify(description).appendDescriptionOf(matcher);
    }

    @Test
    void testDescribeMismatchSafely()
    {
        elementNumberMatcher.describeMismatchSafely(item, description);
        verify(description).appendValue(item.size());
    }

    @Test
    void testMatchesSafely()
    {
        item.add(webElement);
        when(matcher.matches(item.size())).thenReturn(true);
        assertTrue(elementNumberMatcher.matchesSafely(item));
    }

    @Test
    void testElementNumber()
    {
        assertNotNull(ElementNumberMatcher.elementNumber(matcher));
    }
}
