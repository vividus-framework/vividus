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

import static org.apache.commons.lang3.Validate.isTrue;

import org.jbehave.core.annotations.When;
import org.vividus.mobileapp.action.NetworkActions;
import org.vividus.mobileapp.action.NetworkActions.NetworkMode;
import org.vividus.mobileapp.action.NetworkActions.NetworkToggle;
import org.vividus.mobileapp.configuration.MobileEnvironment;
import org.vividus.selenium.manager.GenericWebDriverManager;

public class NetworkSteps
{
    private final MobileEnvironment mobileEnvironment;
    private final GenericWebDriverManager genericWebDriverManager;
    private final NetworkActions networkActions;

    public NetworkSteps(MobileEnvironment mobileEnvironment, GenericWebDriverManager genericWebDriverManager,
            NetworkActions networkActions)
    {
        this.mobileEnvironment = mobileEnvironment;
        this.genericWebDriverManager = genericWebDriverManager;
        this.networkActions = networkActions;
    }

    /**
     * Turn <b>ON/OFF</b> the network connection.
     * <p>The actions performed by the step for iOS:</p>
     * <ul>
     * <li>activate <code>Preferences</code> app,</li>
     * <li>open the selected mode,</li>
     * <li>switch on/off toggle if necessary.</li>
     * </ul>
     * <p>The actions performed by the step for Android:</p>
     * <ul>
     * <li>changes the selected network connection to disabled/enabled state if necessary using system commands.</li>
     * </ul>
     * @param toggle ON or OFF toggle
     * @param connectionName The name of the connection to be changed
     */
    @When("I turn $toggle `$connectionName` network connection")
    public void changeNetworkConnection(NetworkToggle toggle, NetworkMode connectionName)
    {
        if (!genericWebDriverManager.isAndroid())
        {
            boolean iosPlatform = genericWebDriverManager.isIOS();
            isTrue(iosPlatform && mobileEnvironment.isRealDevice(),
                    "Network connection can be changed only for Android emulators, Android and iOS real devices");
            isTrue(NetworkMode.MOBILE_DATA == connectionName || NetworkMode.WIFI == connectionName,
                    "%s is not supported for iOS", connectionName);
        }
        networkActions.changeNetworkConnectionState(toggle, connectionName);
    }
}
