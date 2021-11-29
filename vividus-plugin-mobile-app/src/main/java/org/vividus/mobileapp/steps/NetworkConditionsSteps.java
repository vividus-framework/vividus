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
     * Turn <b>toggle</b> <b>mode</b> network connection
     * <p>The actions performed by the step for iOS:</p>
     * <ul>
     * <li>activate Preferences app</li>
     * <li>open the selected mode</li>
     * <li>switch toggle if necessary</li>
     * </ul>
     * <p>The actions performed by the step for Android:</p>
     * <ul>
     * <li>Initializes connection state builder and sets selected mode to disabled/enabled state if necessary</li>
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
