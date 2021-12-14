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

import java.util.Optional;

import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.manager.GenericWebDriverManager;
import org.vividus.steps.ui.validation.IBaseValidations;
import org.vividus.ui.action.search.Locator;
import org.vividus.ui.mobile.action.search.AppiumLocatorType;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.connection.ConnectionStateBuilder;

public class NetworkActions
{
    private static final Logger LOGGER = LoggerFactory.getLogger(NetworkActions.class);

    private static final String IOS_PREFERENCES_BUNDLE_ID = "com.apple.Preferences";

    private final IWebDriverProvider webDriverProvider;
    private final GenericWebDriverManager genericWebDriverManager;
    private final ApplicationActions applicationActions;
    private final IBaseValidations baseValidations;

    public NetworkActions(IWebDriverProvider webDriverProvider, GenericWebDriverManager genericWebDriverManager,
            ApplicationActions applicationActions, IBaseValidations baseValidations)
    {
        this.webDriverProvider = webDriverProvider;
        this.genericWebDriverManager = genericWebDriverManager;
        this.applicationActions = applicationActions;
        this.baseValidations = baseValidations;
    }

    public void changeNetworkConnectionState(NetworkToggle networkToggle, Mode mode)
    {
        if (genericWebDriverManager.isAndroid())
        {
            webDriverProvider.getUnwrapped(AndroidDriver.class)
                    .setConnection(NetworkToggle.ON == networkToggle ? mode.getEnablingConnectionStateBuilder().build()
                            : mode.getDisablingConnectionStateBuilder().build());
        }
        if (genericWebDriverManager.isIOS())
        {
            changeNetworkConnectionStateForIOS(networkToggle, mode);
        }
    }

    private void changeNetworkConnectionStateForIOS(NetworkToggle networkToggle, Mode mode)
    {
        applicationActions.activateApp(IOS_PREFERENCES_BUNDLE_ID);
        findMenuItemInSettings(new Locator(AppiumLocatorType.ACCESSIBILITY_ID, mode.getIOSNetworkConnectionAlias()))
                .ifPresent(WebElement::click);
        findMenuItemInSettings(new Locator(AppiumLocatorType.IOS_CLASS_CHAIN,
                String.format("**/XCUIElementTypeSwitch[`label == '%s'`]", mode.getIOSNetworkConnectionAlias())))
                        .ifPresent(switchButton ->
                        {
                            if (!networkToggle.active(switchButton.getAttribute("value")))
                            {
                                switchButton.click();
                            }
                            else
                            {
                                LOGGER.atInfo().addArgument(mode).addArgument(networkToggle).log("{} is already {}.");
                            }
                        });
    }

    private Optional<WebElement> findMenuItemInSettings(Locator locator)
    {
        return baseValidations.assertElementExists("Click an item from the menu in the device settings", locator);
    }

    public enum Mode
    {
        WIFI("Wi-Fi")
        {
            @Override
            public ConnectionStateBuilder getEnablingConnectionStateBuilder()
            {
                return new ConnectionStateBuilder().withWiFiEnabled();
            }

            @Override
            public ConnectionStateBuilder getDisablingConnectionStateBuilder()
            {
                return new ConnectionStateBuilder().withWiFiDisabled();
            }
        },
        MOBILE_DATA("Mobile Data")
        {
            @Override
            public ConnectionStateBuilder getEnablingConnectionStateBuilder()
            {
                return new ConnectionStateBuilder().withDataEnabled();
            }

            @Override
            public ConnectionStateBuilder getDisablingConnectionStateBuilder()
            {
                return new ConnectionStateBuilder().withDataDisabled();
            }
        },
        AIRPLANE_MODE(null)
        {
            @Override
            public ConnectionStateBuilder getEnablingConnectionStateBuilder()
            {
                return new ConnectionStateBuilder().withAirplaneModeEnabled();
            }

            @Override
            public ConnectionStateBuilder getDisablingConnectionStateBuilder()
            {
                return new ConnectionStateBuilder().withAirplaneModeDisabled();
            }
        },
        WIFI_AND_MOBILE_DATA(null)
        {
            @Override
            public ConnectionStateBuilder getEnablingConnectionStateBuilder()
            {
                return new ConnectionStateBuilder().withWiFiEnabled().withDataEnabled();
            }

            @Override
            public ConnectionStateBuilder getDisablingConnectionStateBuilder()
            {
                return new ConnectionStateBuilder().withWiFiDisabled().withDataDisabled();
            }
        };

        private final String iOSNetworkConnectionAlias;

        Mode(String iOSNetworkConnectionAlias)
        {
            this.iOSNetworkConnectionAlias = iOSNetworkConnectionAlias;
        }

        public abstract ConnectionStateBuilder getEnablingConnectionStateBuilder();

        public abstract ConnectionStateBuilder getDisablingConnectionStateBuilder();

        public String getIOSNetworkConnectionAlias()
        {
            return iOSNetworkConnectionAlias;
        }
    }

    public enum NetworkToggle
    {
        ON("0"), OFF("1");

        private final String value;

        NetworkToggle(String value)
        {
            this.value = value;
        }

        public boolean active(String state)
        {
            return this.value.equals(state);
        }
    }
}
