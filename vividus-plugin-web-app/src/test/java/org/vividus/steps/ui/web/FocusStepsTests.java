/*
 * Copyright 2019-2021 the original author or authors.
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

package org.vividus.steps.ui.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebElement;
import org.vividus.steps.ui.web.validation.FocusValidations;
import org.vividus.ui.context.UiContext;
import org.vividus.ui.web.action.WebJavascriptActions;

@ExtendWith(MockitoExtension.class)
class FocusStepsTests
{
    private static final String JS_SCRIPT = "arguments[0].focus()";

    @Mock private WebElement webElement;
    @Mock private UiContext uiContext;
    @Mock private WebJavascriptActions javaScriptActions;
    @Mock private FocusValidations focusValidations;
    @InjectMocks private FocusSteps focusSteps;

    @Test
    void testSetFocusPositive()
    {
        when(uiContext.getSearchContext(WebElement.class)).thenReturn(Optional.of(webElement));
        focusSteps.setFocus();
        verify(javaScriptActions).executeScript(JS_SCRIPT, webElement);
    }

    @Test
    void testSetFocusNullableElementToCheck()
    {
        when(uiContext.getSearchContext(WebElement.class)).thenReturn(Optional.empty());
        focusSteps.setFocus();
        verifyNoInteractions(javaScriptActions);
    }

    @Test
    void testIsInFocusStatePositive()
    {
        when(uiContext.getSearchContext(WebElement.class)).thenReturn(Optional.of(webElement));
        mockJavaScriptActions(true);
        focusSteps.isElementInFocusState(FocusState.IN_FOCUS);
        verifyFocusValidations(webElement, FocusState.IN_FOCUS);
    }

    @Test
    void testIsInFocusStateNegative()
    {
        when(uiContext.getSearchContext(WebElement.class)).thenReturn(Optional.of(webElement));
        mockJavaScriptActions(false);
        focusSteps.isElementInFocusState(FocusState.IN_FOCUS);
        verifyFocusValidations(webElement, FocusState.IN_FOCUS);
    }

    @Test
    void testIsNotInFocusStatePositive()
    {
        when(uiContext.getSearchContext(WebElement.class)).thenReturn(Optional.of(webElement));
        mockJavaScriptActions(false);
        focusSteps.isElementInFocusState(FocusState.NOT_IN_FOCUS);
        verifyFocusValidations(webElement, FocusState.NOT_IN_FOCUS);
    }

    @Test
    void testIsNotInFocusStateNegative()
    {
        when(uiContext.getSearchContext(WebElement.class)).thenReturn(Optional.of(webElement));
        mockJavaScriptActions(true);
        focusSteps.isElementInFocusState(FocusState.NOT_IN_FOCUS);
        verifyFocusValidations(webElement, FocusState.NOT_IN_FOCUS);
    }

    private void mockJavaScriptActions(boolean isInState)
    {
        when(focusValidations.isElementInFocusState(eq(webElement), any(FocusState.class))).thenReturn(isInState);
    }

    private void verifyFocusValidations(WebElement webElement, FocusState focusState)
    {
        verify(focusValidations).isElementInFocusState(webElement, focusState);
    }
}
