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

package org.vividus.ui.web.playwright.steps;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import com.microsoft.playwright.Keyboard;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.ui.web.playwright.UiContext;

@ExtendWith(MockitoExtension.class)
class KeyboardStepsTests
{
    private static final String TAB = "Tab";
    private static final String ENTER = "Enter";

    @Mock private UiContext uiContext;
    @Mock private Locator context;
    @Mock private Page page;
    @Mock private Keyboard keyboard;
    @InjectMocks private KeyboardSteps steps;

    @Test
    void shouldPressKeysOnContextElement()
    {
        when(uiContext.getContext()).thenReturn(context);
        steps.pressKeys(List.of(TAB, ENTER));
        verify(context).press(TAB);
        verify(context).press(ENTER);
        verifyNoMoreInteractions(context);
    }

    @Test
    void shouldPressKeysOnPageWhenContextIsNotSet()
    {
        when(uiContext.getContext()).thenReturn(null);
        when(uiContext.getCurrentPage()).thenReturn(page);
        when(page.keyboard()).thenReturn(keyboard);
        steps.pressKeys(List.of(TAB));
        verify(keyboard).press(TAB);
    }
}
