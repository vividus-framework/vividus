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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Locator.ClickOptions;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.TimeoutError;
import com.microsoft.playwright.options.MouseButton;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.softassert.ISoftAssert;
import org.vividus.ui.web.playwright.UiContext;
import org.vividus.ui.web.playwright.locator.PlaywrightLocator;

@ExtendWith(MockitoExtension.class)
class MouseStepsTests
{
    private static final PlaywrightLocator LOCATOR = new PlaywrightLocator("xpath", "div");

    @Mock private UiContext uiContext;
    @Mock private ISoftAssert softAssert;
    @InjectMocks private MouseSteps mouseSteps;

    @Test
    void shouldClickElement()
    {
        Locator locator = mock();
        Page page = mock();
        when(uiContext.locateElement(LOCATOR)).thenReturn(locator);
        when(uiContext.getCurrentPage()).thenReturn(page);
        mouseSteps.clickOnElement(LOCATOR);
        var ordered = inOrder(uiContext, locator, page);
        ordered.verify(locator).click();
        ordered.verify(page).waitForLoadState();
        ordered.verify(uiContext).resetToActiveFrame();
        verifyNoInteractions(softAssert);
    }

    @Test
    void shouldRecordAssertionIfElementToClickIsNotFound()
    {
        Locator locator = mock();
        when(uiContext.locateElement(LOCATOR)).thenReturn(locator);
        TimeoutError timeoutError = mock();
        doThrow(timeoutError).when(locator).click();
        mouseSteps.clickOnElement(LOCATOR);
        verify(softAssert).recordFailedAssertion("The element to click is not found", timeoutError);
    }

    @Test
    void shouldRightClickElement()
    {
        Locator locator = mock();
        when(uiContext.locateElement(LOCATOR)).thenReturn(locator);
        mouseSteps.rightClickOnElement(LOCATOR);
        var clickOptionsCaptor = ArgumentCaptor.forClass(ClickOptions.class);
        verify(locator).click(clickOptionsCaptor.capture());
        assertEquals(MouseButton.RIGHT, clickOptionsCaptor.getValue().button);
        verifyNoInteractions(softAssert);
    }

    @Test
    void shouldRecordAssertionIfElementToRightClickIsNotFound()
    {
        Locator locator = mock();
        when(uiContext.locateElement(LOCATOR)).thenReturn(locator);
        TimeoutError timeoutError = mock();
        doThrow(timeoutError).when(locator).click(argThat(clickOptions -> MouseButton.RIGHT == clickOptions.button));
        mouseSteps.rightClickOnElement(LOCATOR);
        verify(softAssert).recordFailedAssertion("The element to right-click is not found", timeoutError);
    }

    @Test
    void shouldHoverMouseOverElement()
    {
        Locator locator = mock();
        when(uiContext.locateElement(LOCATOR)).thenReturn(locator);
        mouseSteps.hoverMouseOverElement(LOCATOR);
        verify(locator).hover();
        verifyNoInteractions(softAssert);
    }

    @Test
    void shouldRecordAssertionIfElementToHoverMouseOverIsNotFound()
    {
        Locator locator = mock();
        when(uiContext.locateElement(LOCATOR)).thenReturn(locator);
        TimeoutError timeoutError = mock();
        doThrow(timeoutError).when(locator).hover();
        mouseSteps.hoverMouseOverElement(LOCATOR);
        verify(softAssert).recordFailedAssertion("The element to hover mouse over is not found", timeoutError);
    }
}
