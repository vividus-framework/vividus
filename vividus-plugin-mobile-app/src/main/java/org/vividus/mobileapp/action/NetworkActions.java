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
import java.util.function.BiConsumer;
import java.util.function.Consumer;

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
        WIFI("Wi-Fi", ConnectionStateBuilder::withWiFiEnabled, ConnectionStateBuilder::withWiFiDisabled),
        MOBILE_DATA("Mobile Data", ConnectionStateBuilder::withDataEnabled, ConnectionStateBuilder::withDataDisabled),
        AIRPLANE_MODE(null, ConnectionStateBuilder::withAirplaneModeEnabled,
                ConnectionStateBuilder::withAirplaneModeDisabled),
        WIFI_AND_MOBILE_DATA(null, WIFI.enabler.andThen(MOBILE_DATA.enabler),
                WIFI.disabler.andThen(MOBILE_DATA.disabler));

        private final String iOSNetworkConnectionAlias;
        private final Consumer<ConnectionStateBuilder> enabler;
        private final Consumer<ConnectionStateBuilder> disabler;

        NetworkMode(String iOSNetworkConnectionAlias, Consumer<ConnectionStateBuilder> enabler,
                Consumer<ConnectionStateBuilder> disabler)
        {
            this.iOSNetworkConnectionAlias = iOSNetworkConnectionAlias;
            this.enabler = enabler;
            this.disabler = disabler;
        }

        public void enableConnectionState(ConnectionStateBuilder builder)
        {
            enabler.accept(builder);
        }

        public void disableConnectionState(ConnectionStateBuilder builder)
        {
            disabler.accept(builder);
        }

        public String getIOSNetworkConnectionAlias()
        {
            return iOSNetworkConnectionAlias;
        }
    }

    public enum NetworkToggle
    {
        ON("0", NetworkMode::enableConnectionState),
        OFF("1", NetworkMode::disableConnectionState);

        private final String value;
        private final BiConsumer<NetworkMode, ConnectionStateBuilder> stateToggle;

        NetworkToggle(String value, BiConsumer<NetworkMode, ConnectionStateBuilder> stateToggle)
        {
            this.value = value;
            this.stateToggle = stateToggle;
        }

        public boolean isActive(String state)
        {
            return this.value.equals(state);
        }

        public ConnectionState createConnectionState(NetworkMode networkMode)
        {
            ConnectionStateBuilder builder = new ConnectionStateBuilder();
            stateToggle.accept(networkMode, builder);
            return builder.build();
        }
    }
}
