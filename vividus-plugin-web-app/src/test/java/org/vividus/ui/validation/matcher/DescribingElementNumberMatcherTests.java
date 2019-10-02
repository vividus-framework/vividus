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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.function.Function;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebElement;

@ExtendWith(MockitoExtension.class)
class DescribingElementNumberMatcherTests
{
    @Mock
    private Matcher<? super Integer> matcher;

    @Mock
    private Description description;

    @Mock
    private Function<List<? extends WebElement>, String> elementLocationProvider;

    @InjectMocks
    private DescribingElementNumberMatcher locatingElementNumberMatcher;

    private final List<WebElement> item = List.of(mock(WebElement.class));

    @Test
    void testDescribeTo()
    {
        when(description.appendText("number of elements is ")).thenReturn(description);
        locatingElementNumberMatcher.describeTo(description);
        verify(description).appendDescriptionOf(matcher);
    }

    @Test
    void testDescribeMismatchSafely()
    {
        when(elementLocationProvider.apply(item)).thenReturn("\nlocation");
        locatingElementNumberMatcher.describeMismatchSafely(item, description);
        InOrder inOrder = Mockito.inOrder(description, elementLocationProvider);
        inOrder.verify(description).appendValue(item.size());
        inOrder.verify(elementLocationProvider).apply(item);
        inOrder.verify(description).appendText(".\nFound elements\nlocation\n");
    }

    @Test
    void testDescribeMismatchSafelyNoElements()
    {
        locatingElementNumberMatcher.describeMismatchSafely(List.of(), description);
        verify(description).appendValue(0);
        verifyNoInteractions(elementLocationProvider);
        verify(description, never()).appendText(anyString());
    }

    @Test
    void testDescribeMismatchSafelyNotAppendsLocationWhenMatcherMatches()
    {
        when(matcher.matches(1)).thenReturn(true);
        locatingElementNumberMatcher.describeMismatchSafely(item, description);
        verify(description).appendValue(1);
        verifyNoInteractions(elementLocationProvider);
        verify(description, never()).appendText(anyString());
    }

    @Test
    void testMatchesSafely()
    {
        when(matcher.matches(1)).thenReturn(true);
        assertTrue(locatingElementNumberMatcher.matchesSafely(item));
    }

    @Test
    void testElementNumber()
    {
        assertNotNull(ElementNumberMatcher.elementNumber(matcher));
    }
}
