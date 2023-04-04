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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.vividus.selenium.KeysManager;
import org.vividus.steps.ui.web.validation.FocusValidations;
import org.vividus.ui.context.IUiContext;

@ExtendWith(MockitoExtension.class)
class KeyboardStepsTests
{
    private static final String A = "a";
    private static final String CONTROL = "CONTROL";
    private static final List<String> KEYS = List.of(CONTROL, A);

    @Mock private WebElement webElement;
    @Mock private IUiContext uiContext;
    @Mock private KeysManager keysManager;
    @Mock private FocusValidations focusValidations;
    @InjectMocks private KeyboardSteps keyboardSteps;

    @Test
    void testPressKeysContextFocused()
    {
        when(uiContext.getOptionalSearchContext()).thenReturn(Optional.of(webElement));
        when(focusValidations.isElementInFocusState(webElement, FocusState.IN_FOCUS)).thenReturn(true);
        when(keysManager.convertToKeys(KEYS)).thenReturn(new CharSequence[] { Keys.CONTROL, A });
        keyboardSteps.pressKeys(KEYS);
        verify(webElement).sendKeys(Keys.CONTROL, A);
    }

    @Test
    void testPressKeysContextNotFocused()
    {
        when(uiContext.getOptionalSearchContext()).thenReturn(Optional.of(webElement));
        when(focusValidations.isElementInFocusState(webElement, FocusState.IN_FOCUS)).thenReturn(false);
        keyboardSteps.pressKeys(KEYS);
        verify(webElement, never()).sendKeys(any());
        verifyNoInteractions(keysManager);
    }

    @Test
    void testPressKeysContextBody()
    {
        WebDriver webDriver = mock();
        when(uiContext.getOptionalSearchContext()).thenReturn(Optional.of(webDriver));
        when(focusValidations.isElementInFocusState(webElement, FocusState.IN_FOCUS)).thenReturn(true);
        when(webDriver.findElement(By.xpath("//body"))).thenReturn(webElement);
        when(keysManager.convertToKeys(KEYS)).thenReturn(new CharSequence[] { Keys.CONTROL, A });
        keyboardSteps.pressKeys(KEYS);
        verify(webElement).sendKeys(Keys.CONTROL, A);
    }
}
