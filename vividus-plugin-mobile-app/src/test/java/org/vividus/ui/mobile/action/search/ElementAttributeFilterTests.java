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

package org.vividus.ui.mobile.action.search;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.when;

import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebElement;

@ExtendWith(MockitoExtension.class)
class ElementAttributeFilterTests
{
    private static final String KEY = "key";
    private static final String EQUAL = "=";
    private static final String VALUE = "value";

    @Mock private WebElement webElement;

    private final ElementAttributeFilter filter = new ElementAttributeFilter();

    static Stream<Arguments> filterByAttribute()
    {
        return Stream.of(
            arguments(KEY,                 VALUE,             true),
            arguments(KEY,                 StringUtils.EMPTY, true),
            arguments(KEY,                 null,              false),
            arguments(KEY + EQUAL,         StringUtils.EMPTY, true),
            arguments(KEY + EQUAL,         null,              false),
            arguments(KEY + EQUAL + VALUE, StringUtils.EMPTY, false),
            arguments(KEY + EQUAL + VALUE, VALUE,             true),
            arguments(KEY + EQUAL + VALUE, null,              false)
        );
    }

    @MethodSource("filterByAttribute")
    @ParameterizedTest
    void shouldFilterByAttribute(String filterValue, String attributeValue, boolean expected)
    {
        when(webElement.getAttribute(KEY)).thenReturn(attributeValue);
        assertEquals(expected, filter.matches(webElement, filterValue));
    }

    @Test
    void shouldNotFilterIfValueIsEmpty()
    {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> filter.matches(webElement, StringUtils.EMPTY));
        assertEquals("Attribute value can not be empty, expected formats are:"
                + "\n* attribute - an element has the attribute with any value"
                + "\n* attribute= - an element has the attribute with an empty value"
                + "\n* attribute=value - an element has the attribute with the value",
                exception.getMessage());
    }

    @Test
    void shouldReturnLocatorType()
    {
        assertEquals(AppiumLocatorType.ATTRIBUTE, filter.getType());
    }
}
