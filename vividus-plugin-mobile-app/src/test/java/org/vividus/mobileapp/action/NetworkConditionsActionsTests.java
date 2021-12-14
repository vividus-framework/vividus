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

package org.vividus.mobileapp.action;

import static com.github.valfirst.slf4jtest.LoggingEvent.info;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.HasCapabilities;
import org.openqa.selenium.WebElement;
import org.vividus.mobileapp.action.NetworkActions.Mode;
import org.vividus.mobileapp.action.NetworkActions.NetworkToggle;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.manager.GenericWebDriverManager;
import org.vividus.steps.ui.validation.IBaseValidations;
import org.vividus.ui.action.search.Locator;
import org.vividus.ui.mobile.action.search.AppiumLocatorType;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.connection.ConnectionState;

@ExtendWith({ MockitoExtension.class, TestLoggerFactoryExtension.class })
class NetworkConditionsActionsTests
{
    private static final String VALUE = "value";
    private static final String ELEMENT_TO_CLICK = "Click an item from the menu in the device settings";
    private static final String XCUIELEMENT_TYPE_SWITCH = "**/XCUIElementTypeSwitch[`label == '%s'`]";

    private final TestLogger logger = TestLoggerFactory.getTestLogger(NetworkActions.class);

    @Mock private IWebDriverProvider webDriverProvider;
    @Mock private GenericWebDriverManager genericWebDriverManager;
    @Mock private ApplicationActions applicationActions;
    @Mock private IBaseValidations baseValidations;
    @InjectMocks private NetworkActions networkActions;

    private static Stream<Arguments> dataProviderForAndroid()
    {
        return Stream.of(
                Arguments.of(NetworkToggle.OFF, Mode.WIFI),
                Arguments.of(NetworkToggle.ON, Mode.WIFI),
                Arguments.of(NetworkToggle.OFF, Mode.MOBILE_DATA),
                Arguments.of(NetworkToggle.ON, Mode.MOBILE_DATA),
                Arguments.of(NetworkToggle.OFF, Mode.AIRPLANE_MODE),
                Arguments.of(NetworkToggle.ON, Mode.AIRPLANE_MODE),
                Arguments.of(NetworkToggle.OFF, Mode.WIFI_AND_MOBILE_DATA),
                Arguments.of(NetworkToggle.ON, Mode.WIFI_AND_MOBILE_DATA));
    }

    @ParameterizedTest
    @MethodSource("dataProviderForAndroid")
    void testChangeConnectionStateBuilderForAndroid(
            NetworkToggle toggle, Mode mode)
    {
        AndroidDriver driver = mock(AndroidDriver.class, withSettings().extraInterfaces(HasCapabilities.class));
        when(webDriverProvider.getUnwrapped(AndroidDriver.class)).thenReturn(driver);
        when(genericWebDriverManager.isAndroid()).thenReturn(true);
        networkActions.changeNetworkConnectionState(toggle, mode);
        verify(driver).setConnection(any(ConnectionState.class));
    }

    @ParameterizedTest
    @CsvSource({ "ON, MOBILE_DATA,'1'", "OFF, MOBILE_DATA,'0'", "ON, WIFI,'1'", "OFF, WIFI,'0'" })
    void testChangeConnectionStateBuilderForIOS(NetworkToggle toggle, Mode mode, String toggleState)
    {
        WebElement switchBtn = changeNetworkConnection(toggle, mode, toggleState);
        verify(switchBtn).click();
    }

    @ParameterizedTest
    @CsvSource({ "OFF, MOBILE_DATA, '1'", "ON, MOBILE_DATA, '0'", "OFF, WIFI, '1'", "ON, WIFI, '0'" })
    void testAlreadyModifiedWiFiToggleForIOS(NetworkToggle toggle, Mode mode, String toggleState)
    {
        changeNetworkConnection(toggle, mode, toggleState);
        assertThat(logger.getLoggingEvents(), is(List.of(info("{} is already {}.", mode, toggle))));
    }

    private WebElement changeNetworkConnection(NetworkToggle toggle, Mode mode, String toggleState)
    {
        WebElement wiFiElement = mock(WebElement.class);
        WebElement switchBtn = mock(WebElement.class);
        when(genericWebDriverManager.isIOS()).thenReturn(true);
        when(baseValidations.assertElementExists(ELEMENT_TO_CLICK,
                new Locator(AppiumLocatorType.ACCESSIBILITY_ID, mode.getIOSNetworkConnectionAlias()))).thenReturn(
                Optional.of(wiFiElement));
        when(baseValidations.assertElementExists(ELEMENT_TO_CLICK, new Locator(AppiumLocatorType.IOS_CLASS_CHAIN,
                String.format(XCUIELEMENT_TYPE_SWITCH,
                        mode.getIOSNetworkConnectionAlias())))).thenReturn(Optional.of(switchBtn));
        when(switchBtn.getAttribute(VALUE)).thenReturn(toggleState);
        networkActions.changeNetworkConnectionState(toggle, mode);
        return switchBtn;
    }
}
