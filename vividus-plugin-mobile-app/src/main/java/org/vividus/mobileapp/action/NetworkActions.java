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
import io.appium.java_client.android.connection.ConnectionState;
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

    public void changeNetworkConnectionState(NetworkToggle networkToggle, NetworkMode mode)
    {
        if (genericWebDriverManager.isAndroid())
        {
            webDriverProvider.getUnwrapped(AndroidDriver.class).setConnection(
                    networkToggle.createConnectionState(mode));
        }
        if (genericWebDriverManager.isIOS())
        {
            changeNetworkConnectionStateForIOS(networkToggle, mode);
        }
    }

    private void changeNetworkConnectionStateForIOS(NetworkToggle networkToggle, NetworkMode mode)
    {
        applicationActions.activateApp(IOS_PREFERENCES_BUNDLE_ID);
        findMenuItemInSettings(new Locator(AppiumLocatorType.ACCESSIBILITY_ID, mode.getIOSNetworkConnectionAlias()))
                .ifPresent(item ->
                {
                    item.click();
                    findMenuItemInSettings(new Locator(AppiumLocatorType.IOS_CLASS_CHAIN, String
                            .format("**/XCUIElementTypeSwitch[`label == '%s'`]", mode.getIOSNetworkConnectionAlias())))
                                    .ifPresent(switchButton ->
                                    {
                                        if (!networkToggle.isActive(switchButton.getAttribute("value")))
                                        {
                                            switchButton.click();
                                        }
                                        else
                                        {
                                            LOGGER.atInfo().addArgument(mode).addArgument(networkToggle)
                                                    .log("{} is already {}.");
                                        }
                                    });
                });
    }

    private Optional<WebElement> findMenuItemInSettings(Locator locator)
    {
        return baseValidations.assertElementExists("Menu item in iOS Preferences", locator);
    }

    public enum NetworkMode
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

        NetworkMode(String iOSNetworkConnectionAlias)
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
        ON("0")
        {
            @Override
            public ConnectionState createConnectionState(NetworkMode networkMode)
            {
                return networkMode.getEnablingConnectionStateBuilder().build();
            }
        },
        OFF("1")
        {
            @Override
            public ConnectionState createConnectionState(NetworkMode networkMode)
            {
                return networkMode.getDisablingConnectionStateBuilder().build();
            }
        };

        private final String value;

        NetworkToggle(String value)
        {
            this.value = value;
        }

        public boolean isActive(String state)
        {
            return this.value.equals(state);
        }

        public abstract ConnectionState createConnectionState(NetworkMode networkMode);
    }
}
