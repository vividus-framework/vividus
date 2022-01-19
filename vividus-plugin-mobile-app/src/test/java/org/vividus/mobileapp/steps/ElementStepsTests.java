/*
 * Copyright 2019-2022 the original author or authors.
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

package org.vividus.mobileapp.steps;

import static com.github.valfirst.slf4jtest.LoggingEvent.info;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebElement;
import org.vividus.mobileapp.steps.ElementSteps.PickerWheelDirection;
import org.vividus.selenium.manager.IGenericWebDriverManager;
import org.vividus.steps.ui.validation.IBaseValidations;
import org.vividus.ui.action.ElementActions;
import org.vividus.ui.action.JavascriptActions;
import org.vividus.ui.action.search.Locator;

@ExtendWith({ TestLoggerFactoryExtension.class, MockitoExtension.class })
class ElementStepsTests
{
    private static final String PICKER_WHEEL = "Picker wheel";
    private static final String SLIDER = "The slider";
    private static final String SLIDER_VALUE_MESSAGE = "The slider value is set to {}";
    private static final double OFFSET = 0.1;

    @Mock private IGenericWebDriverManager genericWebDriverManager;
    @Mock private JavascriptActions javascriptActions;
    @Mock private ElementActions elementActions;
    @Mock private IBaseValidations baseValidations;
    @Mock private Locator locator;
    @InjectMocks private ElementSteps elementSteps;

    private final TestLogger logger = TestLoggerFactory.getTestLogger(ElementSteps.class);

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

    @Nested
    class IOSSliderTests
    {
        @Test
        void shouldSetValue()
        {
            when(genericWebDriverManager.isIOSNativeApp()).thenReturn(true);
            WebElement slider = mock(WebElement.class);
            when(baseValidations.assertElementExists(SLIDER, locator)).thenReturn(Optional.of(slider));
            Integer number = 50;
            when(elementActions.getElementText(slider)).thenReturn(number.toString());

            elementSteps.setIOSSliderValue(locator, number);

            verify(slider).sendKeys("0.5");
            assertThat(logger.getLoggingEvents(), is(List.of(info(SLIDER_VALUE_MESSAGE, number.toString()))));
        }

        @Test
        void shouldNotSetValueIfSliderIsMissing()
        {
            when(genericWebDriverManager.isIOSNativeApp()).thenReturn(true);
            when(baseValidations.assertElementExists(SLIDER, locator)).thenReturn(Optional.empty());

            elementSteps.setIOSSliderValue(locator, 1);

            verifyNoInteractions(elementActions);
            assertThat(logger.getLoggingEvents(), is(empty()));
        }

        @Test
        void shouldFailToSetValueIfNotIOSPlatform()
        {
            when(genericWebDriverManager.isIOSNativeApp()).thenReturn(false);
            IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> elementSteps.setIOSSliderValue(locator, 1));
            assertEquals("The step is supported only for iOS platform", thrown.getMessage());
            assertThat(logger.getLoggingEvents(), is(empty()));
        }

        @ParameterizedTest
        @ValueSource(ints = { -1, 101 })
        void shouldFailToSetValueIfItsBetweenZeroAndHundred(int value)
        {
            when(genericWebDriverManager.isIOSNativeApp()).thenReturn(true);
            IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> elementSteps.setIOSSliderValue(locator, value));
            assertEquals("The target slider percent value must be between 0 and 100 inclusively, but got " + value,
                    thrown.getMessage());
            assertThat(logger.getLoggingEvents(), is(empty()));
        }
    }

    @Nested
    class AndroidSliderTests
    {
        @Test
        void shouldSetValue()
        {
            when(genericWebDriverManager.isAndroidNativeApp()).thenReturn(true);
            WebElement slider = mock(WebElement.class);
            when(baseValidations.assertElementExists(SLIDER, locator)).thenReturn(Optional.of(slider));
            Double number = 50.0;
            when(elementActions.getElementText(slider)).thenReturn(number.toString());

            elementSteps.setAndroidSliderValue(locator, number);

            verify(slider).sendKeys(number.toString());
            assertThat(logger.getLoggingEvents(), is(List.of(info(SLIDER_VALUE_MESSAGE, number.toString()))));
        }

        @Test
        void shouldNotSetValueIfSliderIsMissing()
        {
            when(genericWebDriverManager.isAndroidNativeApp()).thenReturn(true);
            when(baseValidations.assertElementExists(SLIDER, locator)).thenReturn(Optional.empty());

            elementSteps.setAndroidSliderValue(locator, 1);

            verifyNoInteractions(elementActions);
            assertThat(logger.getLoggingEvents(), is(empty()));
        }

        @Test
        void shouldFailToSetValueIfNotAndroidPlatform()
        {
            when(genericWebDriverManager.isAndroidNativeApp()).thenReturn(false);
            IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> elementSteps.setAndroidSliderValue(locator, 1));
            assertEquals("The step is supported only for Android platform", thrown.getMessage());
            assertThat(logger.getLoggingEvents(), is(empty()));
        }

        @Test
        void shouldFailToSetValueIfItsNegative()
        {
            when(genericWebDriverManager.isAndroidNativeApp()).thenReturn(true);
            IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> elementSteps.setAndroidSliderValue(locator, -1));
            assertEquals("The target slider number must be greater than or equal to 0, but got -1.0",
                    thrown.getMessage());
            assertThat(logger.getLoggingEvents(), is(empty()));
        }
    }
}
