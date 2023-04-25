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

package org.vividus.steps.ui.web;

import static org.mockito.Mockito.mock;
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
import org.vividus.steps.ui.validation.BaseValidations;
import org.vividus.steps.ui.web.validation.FocusValidations;
import org.vividus.ui.action.search.Locator;
import org.vividus.ui.context.UiContext;
import org.vividus.ui.web.action.WebJavascriptActions;

@ExtendWith(MockitoExtension.class)
class FocusStepsTests
{
    private static final String JS_SCRIPT = "arguments[0].focus()";
    private static final String ELEMENT_TO_FOCUS_ON = "Element to set focus on";
    private static final String ELEMENT_TO_CHECK_FOCUS_STATE = "Element to check focus state";

    @Mock private UiContext uiContext;
    @Mock private WebJavascriptActions javaScriptActions;
    @Mock private BaseValidations baseValidations;
    @Mock private FocusValidations focusValidations;
    @InjectMocks private FocusSteps focusSteps;

    @Test
    void shouldSetFocusOnContextElementSuccessfully()
    {
        WebElement webElement = mock();
        when(uiContext.getSearchContext(WebElement.class)).thenReturn(Optional.of(webElement));
        focusSteps.setFocusOnContextElement();
        verify(javaScriptActions).executeScript(JS_SCRIPT, webElement);
    }

    @Test
    void shouldSkipSettingFocusOnMissingContextElement()
    {
        when(uiContext.getSearchContext(WebElement.class)).thenReturn(Optional.empty());
        focusSteps.setFocusOnContextElement();
        verifyNoInteractions(javaScriptActions);
    }

    @Test
    void shouldSetFocusOnFoundElementSuccessfully()
    {
        Locator locator = mock();
        WebElement webElement = mock();
        when(baseValidations.assertElementExists(ELEMENT_TO_FOCUS_ON, locator)).thenReturn(Optional.of(webElement));
        focusSteps.setFocusOnElement(locator);
        verify(javaScriptActions).executeScript(JS_SCRIPT, webElement);
    }

    @Test
    void shouldSkipSettingFocusOnMissingElement()
    {
        Locator locator = mock();
        when(baseValidations.assertElementExists(ELEMENT_TO_FOCUS_ON, locator)).thenReturn(Optional.empty());
        focusSteps.setFocusOnElement(locator);
        verifyNoInteractions(javaScriptActions);
    }

    @Test
    void shouldCheckIfContextElementIsInFocusState()
    {
        WebElement webElement = mock();
        when(uiContext.getSearchContext(WebElement.class)).thenReturn(Optional.of(webElement));
        focusSteps.isContextElementInFocusState(FocusState.IN_FOCUS);
        verify(focusValidations).isElementInFocusState(webElement, FocusState.IN_FOCUS);
    }

    @Test
    void shouldSkipCheckOfMissingContextElementFocusState()
    {
        when(uiContext.getSearchContext(WebElement.class)).thenReturn(Optional.empty());
        focusSteps.isContextElementInFocusState(FocusState.IN_FOCUS);
        verifyNoInteractions(focusValidations);
    }

    @Test
    void shouldCheckIfElementIsNotInFocusState()
    {
        Locator locator = mock();
        WebElement webElement = mock();
        when(baseValidations.assertElementExists(ELEMENT_TO_CHECK_FOCUS_STATE, locator)).thenReturn(
                Optional.of(webElement));
        focusSteps.isElementInFocusState(locator, FocusState.NOT_IN_FOCUS);
        verify(focusValidations).isElementInFocusState(webElement, FocusState.NOT_IN_FOCUS);
    }

    @Test
    void shouldSkipCheckOfContextElementFocusState()
    {
        Locator locator = mock();
        when(baseValidations.assertElementExists(ELEMENT_TO_CHECK_FOCUS_STATE, locator)).thenReturn(Optional.empty());
        focusSteps.isElementInFocusState(locator, FocusState.NOT_IN_FOCUS);
        verifyNoInteractions(focusValidations);
    }
}
