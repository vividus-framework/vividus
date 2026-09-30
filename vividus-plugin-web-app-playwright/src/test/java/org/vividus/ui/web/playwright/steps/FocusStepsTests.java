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

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.microsoft.playwright.Locator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.softassert.ISoftAssert;
import org.vividus.ui.web.playwright.UiContext;
import org.vividus.ui.web.playwright.locator.PlaywrightLocator;
import org.vividus.ui.web.playwright.model.FocusState;

@ExtendWith(MockitoExtension.class)
class FocusStepsTests
{
    private static final String IS_ACTIVE_ELEMENT_SCRIPT = "el => el === document.activeElement";

    @Mock private UiContext uiContext;
    @Mock private ISoftAssert softAssert;
    @Mock private PlaywrightLocator playwrightLocator;
    @Mock private Locator element;
    @InjectMocks private FocusSteps steps;

    @Test
    void shouldSetFocusOnContextElementWhenContextIsSet()
    {
        when(uiContext.getContext()).thenReturn(element);
        steps.setFocusOnContextElement();
        verify(element).focus();
    }

    @Test
    void shouldNotFailToSetFocusOnContextElementWhenContextIsNotSet()
    {
        when(uiContext.getContext()).thenReturn(null);
        steps.setFocusOnContextElement();
        verifyNoInteractions(softAssert);
    }

    @Test
    void shouldSetFocusOnElement()
    {
        when(uiContext.locateElement(playwrightLocator)).thenReturn(element);
        steps.setFocusOnElement(playwrightLocator);
        verify(element).focus();
    }

    @Test
    void shouldAssertContextElementIsInFocusState()
    {
        when(uiContext.getContext()).thenReturn(element);
        when(element.evaluate(IS_ACTIVE_ELEMENT_SCRIPT)).thenReturn(true);
        steps.isContextElementInFocusState(FocusState.IN_FOCUS);
        verify(softAssert).assertTrue("The element is in focus", true);
    }

    @Test
    void shouldNotAssertContextElementFocusStateWhenContextIsNotSet()
    {
        when(uiContext.getContext()).thenReturn(null);
        steps.isContextElementInFocusState(FocusState.IN_FOCUS);
        verifyNoInteractions(softAssert);
        verify(element, never()).evaluate(IS_ACTIVE_ELEMENT_SCRIPT);
    }

    @Test
    void shouldAssertElementIsNotInFocusState()
    {
        when(uiContext.locateElement(playwrightLocator)).thenReturn(element);
        when(element.evaluate(IS_ACTIVE_ELEMENT_SCRIPT)).thenReturn(false);
        steps.isElementInFocusState(playwrightLocator, FocusState.NOT_IN_FOCUS);
        verify(softAssert).assertTrue("The element is not in focus", true);
    }
}
