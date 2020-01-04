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

package org.vividus.selenium.element;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebElement;

@ExtendWith(MockitoExtension.class)
class CheckboxTests
{
    @Mock
    private WebElement checkboxElement;

    @Test
    void testCreateCheckboxWithLabel()
    {
        WebElement labelElement = mock(WebElement.class);
        Checkbox checkbox = new Checkbox(checkboxElement, labelElement);
        assertEquals(checkboxElement, checkbox.getWrappedElement());
        assertEquals(labelElement, checkbox.getLabelElement());
    }

    @Test
    void tesCreateCheckboxWithoutLabel()
    {
        Checkbox checkbox = new Checkbox(checkboxElement);
        assertEquals(checkboxElement, checkbox.getWrappedElement());
        assertNull(checkbox.getLabelElement());
    }
}
