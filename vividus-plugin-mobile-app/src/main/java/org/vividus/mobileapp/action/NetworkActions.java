/*
 * Copyright 2019-2021 the original author or authors.
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

import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.manager.GenericWebDriverManager;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.connection.ConnectionStateBuilder;
import io.appium.java_client.ios.IOSDriver;

public class NetworkActions
{
    private static final Logger LOGGER = LoggerFactory.getLogger(NetworkActions.class);

    private static final String OFF = "1";
    private static final String ON = "0";

    private static final String PREFERENCES = "com.apple.Preferences";

    private final IWebDriverProvider webDriverProvider;
    private final GenericWebDriverManager genericWebDriverManager;
    private final ApplicationActions applicationActions;

    public NetworkActions(IWebDriverProvider webDriverProvider, GenericWebDriverManager genericWebDriverManager,
            ApplicationActions applicationActions)
    {
        this.webDriverProvider = webDriverProvider;
        this.genericWebDriverManager = genericWebDriverManager;
        this.applicationActions = applicationActions;
    }

    public void changeNetworkConnectionState(NetworkToggle networkToggle, Mode mode)
    {
        if (genericWebDriverManager.isAndroid())
        {
            webDriverProvider.getUnwrapped(AndroidDriver.class)
                    .setConnection(NetworkToggle.ON == networkToggle
                            ? mode.enableConnectionStateBuilder().build()
                            : mode.disableConnectionStateBuilder().build());
        }
        if (genericWebDriverManager.isIOS())
        {
            changeNetworkConnectionStateForIOS(webDriverProvider, networkToggle, mode);
        }
    }

    private void changeNetworkConnectionStateForIOS(IWebDriverProvider webDriverProvider, NetworkToggle networkToggle,
            Mode mode)
    {
        IOSDriver driver = webDriverProvider.getUnwrapped(IOSDriver.class);
        applicationActions.activateApp(PREFERENCES);
        driver.findElementByAccessibilityId(mode.getiOSNetworkConnectionAlias()).click();
        WebElement switchButton = driver.findElementByIosClassChain(
                String.format("**/XCUIElementTypeSwitch[`label == \"%s\"`]", mode.getiOSNetworkConnectionAlias()));
        String switchStatus = switchButton.getAttribute("value");
        boolean toBeTurnedOn = NetworkToggle.ON == networkToggle;
        if (OFF.equals(switchStatus) && toBeTurnedOn || ON.equals(switchStatus) && !toBeTurnedOn)
        {
            switchButton.click();
        }
        else
        {
            LOGGER.atInfo().addArgument(mode).addArgument(networkToggle).log("{} is already {}.");
        }
    }

    public enum Mode
    {
        WIFI("Wi-Fi")
        {
            @Override
            public ConnectionStateBuilder enableConnectionStateBuilder()
            {
                return new ConnectionStateBuilder().withWiFiEnabled();
            }

            @Override
            public ConnectionStateBuilder disableConnectionStateBuilder()
            {
                return new ConnectionStateBuilder().withWiFiDisabled();
            }
        },
        MOBILE_DATA("Mobile Data")
        {
            @Override
            public ConnectionStateBuilder enableConnectionStateBuilder()
            {
                return new ConnectionStateBuilder().withDataEnabled();
            }

            @Override
            public ConnectionStateBuilder disableConnectionStateBuilder()
            {
                return new ConnectionStateBuilder().withDataDisabled();
            }
        },
        AIRPLANE_MODE(null)
        {
            @Override
            public ConnectionStateBuilder enableConnectionStateBuilder()
            {
                return new ConnectionStateBuilder().withAirplaneModeEnabled();
            }

            @Override
            public ConnectionStateBuilder disableConnectionStateBuilder()
            {
                return new ConnectionStateBuilder().withAirplaneModeDisabled();
            }
        },
        WIFI_AND_MOBILE_DATA(null)
        {
            @Override
            public ConnectionStateBuilder enableConnectionStateBuilder()
            {
                return new ConnectionStateBuilder().withWiFiEnabled().withDataEnabled();
            }

            @Override
            public ConnectionStateBuilder disableConnectionStateBuilder()
            {
                return new ConnectionStateBuilder().withWiFiDisabled().withDataDisabled();
            }
        };

        private final String iOSNetworkConnectionAlias;

        Mode(String iOSNetworkConnectionAlias)
        {
            this.iOSNetworkConnectionAlias = iOSNetworkConnectionAlias;
        }

        public abstract ConnectionStateBuilder enableConnectionStateBuilder();

        public abstract ConnectionStateBuilder disableConnectionStateBuilder();

        public String getiOSNetworkConnectionAlias()
        {
            return iOSNetworkConnectionAlias;
        }
    }

    public enum NetworkToggle
    {
        ON, OFF
    }
}
