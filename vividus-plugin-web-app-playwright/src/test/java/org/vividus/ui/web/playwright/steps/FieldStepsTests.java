/*
 * Copyright 2019-2023 the original author or authors.
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

package org.vividus.ui.web.playwright.steps;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.microsoft.playwright.Locator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.ui.web.playwright.UiContext;
import org.vividus.ui.web.playwright.locator.PlaywrightLocator;

@ExtendWith(MockitoExtension.class)
class FieldStepsTests
{
    @Mock private UiContext uiContext;
    @Mock private PlaywrightLocator locator;
    @Mock private Locator field;
    @InjectMocks private FieldSteps steps;

    @Test
    void shouldEnterTextInField()
    {
        var text = "text to enter";
        when(uiContext.locateElement(locator)).thenReturn(field);
        steps.enterTextInField(text, locator);
        verify(field).fill(text);
    }

    @Test
    void shouldAddTextToField()
    {
        var text = " text to add";
        when(uiContext.locateElement(locator)).thenReturn(field);
        var currentValue = "current value";
        when(field.inputValue()).thenReturn(currentValue);
        steps.addTextToField(text, locator);
        verify(field).fill(currentValue + text);
    }

    @Test
    void shouldClearField()
    {
        when(uiContext.locateElement(locator)).thenReturn(field);
        steps.clearField(locator);
        verify(field).clear();
    }
}
