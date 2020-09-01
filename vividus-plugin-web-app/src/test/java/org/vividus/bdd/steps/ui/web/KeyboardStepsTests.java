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

package org.vividus.bdd.steps.ui.web;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.vividus.bdd.steps.ui.web.validation.IFocusValidations;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.ui.context.IUiContext;

@ExtendWith(MockitoExtension.class)
class KeyboardStepsTests
{
    private static final String A = "a";
    private static final List<String> KEYS = List.of("CONTROL", A);

    @Mock
    private IWebDriverProvider webDriverProvider;

    @Mock
    private WebDriver webDriver;

    @Mock
    private IUiContext uiContext;

    @Mock
    private WebElement webElement;

    @Mock
    private IFocusValidations focusValidations;

    @InjectMocks
    private KeyboardSteps keyboardSteps;

    @Test
    void testPressKeysContextFocused()
    {
        Mockito.lenient().when(uiContext.getSearchContext(WebElement.class)).thenReturn(webElement);
        when(focusValidations.isElementInFocusState(webElement, FocusState.IN_FOCUS)).thenReturn(true);
        keyboardSteps.pressKeys(KEYS);
        Mockito.verify(webElement).sendKeys(Keys.CONTROL, A);
    }

    @Test
    void testPressKeysContextNotFocused()
    {
        Mockito.lenient().when(uiContext.getSearchContext(WebElement.class)).thenReturn(webElement);
        keyboardSteps.pressKeys(KEYS);
        Mockito.verify(webElement, never()).sendKeys(Keys.CONTROL, A);
    }

    @Test
    void testPressKeysContextBody()
    {
        when(webDriverProvider.get()).thenReturn(webDriver);
        when(uiContext.getSearchContext()).thenReturn(webDriver);
        when(focusValidations.isElementInFocusState(webElement, FocusState.IN_FOCUS)).thenReturn(true);
        when(webDriver.findElement(By.xpath("//body"))).thenReturn(webElement);
        keyboardSteps.pressKeys(KEYS);
        Mockito.verify(webElement).sendKeys(Keys.CONTROL, A);
    }
}
