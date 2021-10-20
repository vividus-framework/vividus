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

package org.vividus.bdd.mobileapp.steps;

import static io.appium.java_client.CommandExecutionHelper.execute;
import static io.appium.java_client.MobileCommand.prepareArguments;
import static java.util.Map.entry;
import static org.vividus.selenium.type.CapabilitiesValueTypeAdjuster.adjustType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.math.NumberUtils;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.When;
import org.openqa.selenium.HasCapabilities;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.bdd.mobileapp.model.NamedEntry;
import org.vividus.mobileapp.action.ApplicationActions;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.manager.IWebDriverManagerContext;
import org.vividus.selenium.manager.WebDriverManagerParameter;
import org.vividus.util.property.PropertyParser;

import io.appium.java_client.ExecutesMethod;
import io.appium.java_client.internal.CapabilityHelpers;
import io.appium.java_client.HasSessionDetails;
import io.appium.java_client.InteractsWithApps;

public class ApplicationSteps
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationSteps.class);

    private final IWebDriverProvider webDriverProvider;
    private final IWebDriverManagerContext webDriverManagerContext;
    private final ApplicationActions applicationActions;

    public ApplicationSteps(IWebDriverProvider webDriverProvider, IWebDriverManagerContext webDriverManagerContext,
            ApplicationActions applicationActions)
    {
        this.webDriverProvider = webDriverProvider;
        this.webDriverManagerContext = webDriverManagerContext;
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
        DesiredCapabilities desiredCapabilities = webDriverManagerContext
                .getParameter(WebDriverManagerParameter.DESIRED_CAPABILITIES);
        Map<String, Object> capabilitiesContainer = new HashMap<>(desiredCapabilities.asMap());
        capabilities.forEach(c -> PropertyParser.putByPath(capabilitiesContainer, c.getName(), c.getValue()));
        webDriverManagerContext.putParameter(WebDriverManagerParameter.DESIRED_CAPABILITIES,
                new DesiredCapabilities(capabilitiesContainer));
        startMobileApplication();
    }

    /**
     * Starts mobile application
     */
    @Given("I start mobile application")
    public void startMobileApplication()
    {
        HasCapabilities driverWithCapabilities = webDriverProvider.getUnwrapped(HasCapabilities.class);
        LOGGER.atInfo()
                .addArgument(
                        () -> CapabilityHelpers.getCapability(driverWithCapabilities.getCapabilities(), "app",
                                String.class))
                .log("Started application located at {}");
    }

    /**
     * Closes mobile application
     */
    @When("I close mobile application")
    public void closeMobileApplication()
    {
        webDriverProvider.end();
    }

    /**
     * Restart mobile application
     */
    @When("I restart mobile application")
    public void restartMobileApplication()
    {
        InteractsWithApps interactor = webDriverProvider.getUnwrapped(InteractsWithApps.class);
        interactor.closeApp();
        interactor.launchApp();
    }

    /**
     * Terminates the application if it's running
     * @param bundleId bundle identifier of the application to terminate.
     */
    @When("I terminate application with bundle identifier `$bundleId`")
    public void terminateApp(String bundleId)
    {
        InteractsWithApps interactor = webDriverProvider.getUnwrapped(InteractsWithApps.class);
        interactor.terminateApp(bundleId);
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
     * @param settings Appium session settings
     * <br><a href="https://appium.io/docs/en/advanced-concepts/settings/#settings">Settings</a> example:
     * <code>
     * <br>|name                  |value|
     * <br>|ignoreUnimportantViews|true |
     * <br>|snapshotMaxDepth      |100  |
     * </code>
     */
    @When("I change Appium session settings:$settings")
    public void changeAppiumSettings(List<NamedEntry> settings)
    {
        ExecutesMethod executesMethod = webDriverProvider.getUnwrapped(ExecutesMethod.class);

        String[] params = settings.stream()
                                  .map(NamedEntry::getName)
                                  .toArray(String[]::new);
        Object[] values = settings.stream()
                                  .map(NamedEntry::getValue)
                                  .map(v -> NumberUtils.isDigits(v) ? Long.valueOf(v) : adjustType(v))
                                  .toArray();

        execute(executesMethod, entry("setSettings", prepareArguments("settings", prepareArguments(params, values))));
    }
}
