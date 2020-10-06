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

package org.vividus.mobileapp.action;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebElement;
import org.vividus.selenium.IWebDriverProvider;

import io.appium.java_client.HidesKeyboard;

@ExtendWith(MockitoExtension.class)
class KeyboardActionsTests
{
    @Mock private WebElement element;
    @Mock private HidesKeyboard hidesKeyboard;
    @Mock private IWebDriverProvider webDriverProvider;
    @InjectMocks private KeyboardActions keyboardActions;

    @Test
    void testTypeText()
    {
        String text = "text";
        when(webDriverProvider.getUnwrapped(HidesKeyboard.class)).thenReturn(hidesKeyboard);

        keyboardActions.typeText(element, text);

        verify(element).sendKeys(text);
        verify(hidesKeyboard).hideKeyboard();
    }
}
