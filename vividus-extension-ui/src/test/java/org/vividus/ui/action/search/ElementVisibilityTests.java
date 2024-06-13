/*
 * Copyright 2019-2024 the original author or authors.
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

package org.vividus.ui.action.search;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

class ElementVisibilityTests
{
    enum TestVisibility implements ElementVisibility
    {
        VISIBLE, HIDDEN, ANY
    }

    @Test
    void shouldGetElementType()
    {
        TestVisibility visibility = ElementVisibility.getElementType("visible", TestVisibility.class);
        assertEquals(TestVisibility.VISIBLE, visibility);
    }

    @Test
    void shouldThrowExceptionWhenInputIsEmpty()
    {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> ElementVisibility.getElementType(StringUtils.EMPTY, TestVisibility.class));
        String expectedMessage = "Visibility type can not be empty. Expected one of 'visible', 'hidden', 'any'";
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenIllegalVisibilityType()
    {
        String illegalVisibilityType = "illegal";
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> ElementVisibility.getElementType(illegalVisibilityType, TestVisibility.class));
        String expectedMessage = String.format(
                "Illegal visibility type '%s'. Expected one of 'visible', 'hidden', 'any'", illegalVisibilityType);
        assertEquals(expectedMessage, exception.getMessage());
    }
}
