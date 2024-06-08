/*
 * Copyright 2019-2024 the original author or authors.
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

import static io.appium.java_client.CommandExecutionHelper.execute;
import static java.util.Map.entry;
import static org.vividus.selenium.type.CapabilitiesValueTypeAdjuster.adjustType;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.math.NumberUtils;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.When;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.HasCapabilities;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.mobileapp.action.ApplicationActions;
import org.vividus.mobileapp.model.NamedEntry;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.WebDriverStartContext;
import org.vividus.selenium.WebDriverStartParameters;
import org.vividus.util.property.PropertyParser;

import io.appium.java_client.ExecutesMethod;
import io.appium.java_client.InteractsWithApps;
import io.appium.java_client.internal.CapabilityHelpers;

public class ApplicationSteps
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationSteps.class);

    private final IWebDriverProvider webDriverProvider;
    private final WebDriverStartContext webDriverStartContext;
    private final ApplicationActions applicationActions;

    public ApplicationSteps(IWebDriverProvider webDriverProvider, WebDriverStartContext webDriverStartContext,
            ApplicationActions applicationActions)
    {
        this.webDriverProvider = webDriverProvider;
        this.webDriverStartContext = webDriverStartContext;
        this.applicationActions = applicationActions;
    }

    /**
     * Starts mobile application with the capabilities
     *
     * @param capabilities capabilities to start mobile application with
     */
    @Given("I start mobile application with capabilities:$capabilities")
    public void startMobileApplicationWithCapabilities(List<NamedEntry> capabilities)
    {
        DesiredCapabilities desiredCapabilities = webDriverStartContext
                .get(WebDriverStartParameters.DESIRED_CAPABILITIES);
        Map<String, Object> capabilitiesContainer = new HashMap<>(desiredCapabilities.asMap());
        capabilities.forEach(c -> PropertyParser.putByPath(capabilitiesContainer, c.getName(), c.getValue()));
        webDriverStartContext.put(WebDriverStartParameters.DESIRED_CAPABILITIES,
                new DesiredCapabilities(capabilitiesContainer));
        startMobileApplication();
    }

    /**
     * Starts mobile application
     */
    @Given("I start mobile application")
    public void startMobileApplication()
    {
        Capabilities capabilities = webDriverProvider.getUnwrapped(HasCapabilities.class).getCapabilities();
        String appCapability = CapabilityHelpers.getCapability(capabilities, "app", String.class);
        if (appCapability != null)
        {
            LOGGER.info("Started application located at {}", appCapability);
        }
    }

    /**
     * Closes mobile application and quits the session
     */
    @When("I close mobile application")
    public void closeMobileApplication()
    {
        webDriverProvider.end();
    }

    /**
     * Removes a mobile application from device and installs it again
     * @param bundleId bundle identifier of the mobile application to reinstall.
     *  BundleId should indicate the application used in the current session.
     */
    @When("I reinstall mobile application with bundle identifier `$bundleId`")
    public void reinstallMobileApplication(String bundleId)
    {
        applicationActions.reinstallApplication(bundleId);
        applicationActions.activateApp(bundleId);
    }

    /**
     * Terminates the running application on the device/emulator/simulator and navigates to the home device screen
     * (the remote session will not be closed). If the application is not running, new failed assertion is recorded.
     *
     * @param bundleId "Package name" for Android or "Bundle identifier" from "Plist.info" for iOS of the application
     *                to terminate.
     */
    @When("I terminate application with bundle identifier `$bundleId`")
    public void terminateApp(String bundleId)
    {
        applicationActions.terminateApp(bundleId);
    }

    /**
     * Activates the application if it's installed, but not running or if it is running in the
     * background.
     * @param bundleId bundle identifier of the application to activate.
     */
    @When("I activate application with bundle identifier `$bundleId`")
    public void activateApp(String bundleId)
    {
        applicationActions.activateApp(bundleId);
    }

    /**
     * Change the behavior of the Appium session
     *
     * @param settings The <a href="https://appium.github.io/appium-xcuitest-driver/5.6/settings/">iOS</a>
     *                 or <a href="https://github.com/appium/appium-uiautomator2-driver#settings-api">Android</a>
     *                 settings to set.
     *                 Example:
     *                 <code>
     *                 <br>|name                  |value|
     *                 <br>|ignoreUnimportantViews|true |
     *                 <br>|snapshotMaxDepth      |100  |
     *                 </code>
     */
    @When("I change Appium session settings:$settings")
    public void changeAppiumSettings(List<NamedEntry> settings)
    {
        ExecutesMethod executesMethod = webDriverProvider.getUnwrapped(ExecutesMethod.class);

        Map<String, Object> settingsMap = settings.stream().collect(
                Collectors.toMap(NamedEntry::getName, namedEntry -> {
                    String value = namedEntry.getValue();
                    return NumberUtils.isDigits(value) ? Long.valueOf(value) : adjustType(value);
                }));

        execute(executesMethod, entry("setSettings", Map.of("settings", settingsMap)));
    }

    /**
     * Sends an application to background for given period of time.
     * @param period  Total background run time according to
     *                <a href="https://en.wikipedia.org/wiki/ISO_8601">ISO 8601</a> standard
     */
    @When("I send mobile application to background for `$period` period")
    public void sendToBackgroundFor(Duration period)
    {
        webDriverProvider.getUnwrapped(InteractsWithApps.class).runAppInBackground(period);
    }
}
