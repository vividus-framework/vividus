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

package org.vividus.bdd.mobileapp.steps;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.remote.RemoteWebElement;
import org.vividus.bdd.mobileapp.steps.ElementSteps.PickerWheelDirection;
import org.vividus.bdd.steps.ui.validation.IBaseValidations;
import org.vividus.selenium.manager.IGenericWebDriverManager;
import org.vividus.ui.action.JavascriptActions;
import org.vividus.ui.action.search.Locator;

@ExtendWith(MockitoExtension.class)
class ElementStepsTests
{
    private static final String PICKER_WHEEL = "Picker wheel";
    private static final double OFFSET = 0.1;

    @Mock private IGenericWebDriverManager genericWebDriverManager;
    @Mock private JavascriptActions javascriptActions;
    @Mock private IBaseValidations baseValidations;
    @Mock private Locator locator;
    @InjectMocks private ElementSteps elementSteps;

    @Test
    void shouldSelectPickerWheelValue()
    {
        String elementId = "element-id";
        RemoteWebElement remoteElement = mock(RemoteWebElement.class);

        when(genericWebDriverManager.isIOSNativeApp()).thenReturn(true);
        when(baseValidations.assertElementExists(PICKER_WHEEL, locator)).thenReturn(Optional.of(remoteElement));
        when(remoteElement.getId()).thenReturn(elementId);
        when(remoteElement.getTagName()).thenReturn("XCUIElementTypePickerWheel");

        elementSteps.selectPickerWheelValue(PickerWheelDirection.NEXT, OFFSET, locator);

        verify(javascriptActions).executeScript("mobile: selectPickerWheelValue", Map.of("order", "next", "element",
                elementId, "offset", OFFSET));
    }

    @Test
    void shouldNotSelectPickerWheelValueIfElementIsNotPickerWheel()
    {
        RemoteWebElement remoteElement = mock(RemoteWebElement.class);

        when(genericWebDriverManager.isIOSNativeApp()).thenReturn(true);
        when(baseValidations.assertElementExists(PICKER_WHEEL, locator)).thenReturn(Optional.of(remoteElement));
        when(remoteElement.getTagName()).thenReturn("XCUIElementTypeOther");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> elementSteps.selectPickerWheelValue(PickerWheelDirection.NEXT, OFFSET, locator));
        assertEquals("Located element must have XCUIElementTypePickerWheel type, but got XCUIElementTypeOther",
                exception.getMessage());
    }

    @Test
    void shouldNotSelectPickerWheelValueOnAndroid()
    {
        when(genericWebDriverManager.isIOSNativeApp()).thenReturn(false);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> elementSteps.selectPickerWheelValue(PickerWheelDirection.NEXT, OFFSET, locator));
        assertEquals("Picker wheel selection is supported only for iOS platform", exception.getMessage());
    }
}
