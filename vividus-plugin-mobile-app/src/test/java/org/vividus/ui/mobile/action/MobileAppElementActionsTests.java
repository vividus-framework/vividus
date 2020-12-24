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

package org.vividus.ui.mobile.action;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebElement;
import org.vividus.selenium.manager.GenericWebDriverManager;

@ExtendWith(MockitoExtension.class)
class MobileAppElementActionsTests
{
    @Mock private WebElement webElement;
    @Mock private GenericWebDriverManager genericWebDriverManager;
    @InjectMocks private MobileAppElementActions elementActions;

    @Test
    void shouldGetElementText()
    {
        String text = "text";

        when(webElement.getText()).thenReturn(text);

        assertEquals(text, elementActions.getElementText(webElement));
        verify(webElement).getText();
    }

    @Test
    void shouldCheckIfElementIsVisible()
    {
        when(webElement.isDisplayed()).thenReturn(true);

        assertTrue(elementActions.isElementVisible(webElement));
    }

    @Test
    void shouldNotCheckVisibilityUsingAttributeOnNotIOSNativeApps()
    {
        when(webElement.isDisplayed()).thenReturn(false);
        when(genericWebDriverManager.isIOSNativeApp()).thenReturn(false);

        assertFalse(elementActions.isElementVisible(webElement));
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void shouldCheckIfElementIsVisibleOnIOSNativeAppsUsingAttribute(Boolean visible)
    {
        when(webElement.isDisplayed()).thenReturn(false);
        when(genericWebDriverManager.isIOSNativeApp()).thenReturn(true);
        when(webElement.getAttribute("visible")).thenReturn(visible.toString());

        assertEquals(visible, elementActions.isElementVisible(webElement));
    }
}
