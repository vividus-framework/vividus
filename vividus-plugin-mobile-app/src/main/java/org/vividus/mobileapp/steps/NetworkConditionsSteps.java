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

import org.apache.commons.lang3.Validate;
import org.jbehave.core.annotations.When;
import org.vividus.mobileapp.action.NetworkActions;
import org.vividus.mobileapp.action.NetworkActions.Mode;
import org.vividus.mobileapp.action.NetworkActions.NetworkToggle;
import org.vividus.selenium.manager.GenericWebDriverManager;

public final class NetworkConditionsSteps
{
    private final NetworkActions networkActions;
    private final GenericWebDriverManager genericWebDriverManager;

    private NetworkConditionsSteps(GenericWebDriverManager genericWebDriverManager,
            NetworkActions networkActions)
    {
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
     * <li>changes the selected network connect to disabled/enabled state if necessary using system commands</li>
     * </ul>
     * @param toggle switch ON or OFF mode
     * @param mode is network condition to be executed
     */
    @When("I turn $toggle `$mode` network connection")
    public void changeNetworkConnection(NetworkToggle toggle, Mode mode)
    {
        Validate.isTrue(
                genericWebDriverManager.isAndroid()
                        || genericWebDriverManager.isIOS() && (Mode.MOBILE_DATA.equals(mode) || Mode.WIFI.equals(mode)),
                "%s is not supported for iOS", mode);
        networkActions.changeNetworkConnectionState(toggle, mode);
    }
}
