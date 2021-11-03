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

import org.apache.commons.lang3.Validate;
import org.openqa.selenium.HasCapabilities;
import org.vividus.selenium.IWebDriverProvider;

import io.appium.java_client.InteractsWithApps;
import io.appium.java_client.appmanagement.ApplicationState;

public class ApplicationActions
{
    private final IWebDriverProvider webDriverProvider;

    public ApplicationActions(IWebDriverProvider webDriverProvider)
    {
        this.webDriverProvider = webDriverProvider;
    }

    /**
     * Activates the application if it's installed, but not running or if it is running in the
     * background.
     * @param bundleId bundle identifier of the application to activate.
     */
    public void activateApp(String bundleId)
    {
        InteractsWithApps interactor = webDriverProvider.getUnwrapped(InteractsWithApps.class);
        Validate.isTrue(interactor.isAppInstalled(bundleId),
                "Application with the bundle identifier '%s' is not installed on the device", bundleId);
        interactor.activateApp(bundleId);
    }

    /**
     * Terminate the application if it's running.
     * @param bundleId bundle identifier of the application to terminate.
     */
    public void terminateApp(String bundleId)
    {
        InteractsWithApps interactor = webDriverProvider.getUnwrapped(InteractsWithApps.class);
        ApplicationState appState = interactor.queryAppState(bundleId);
        Validate.isTrue(appState != ApplicationState.NOT_INSTALLED && appState != ApplicationState.NOT_RUNNING,
                "Application with the bundle identifier '%s' is not installed or not running on the device",
                bundleId);
        Validate.isTrue(interactor.terminateApp(bundleId),
                "Application with the bundle identifier '%s' hasn't been successfully terminated", bundleId);
    }

    /**
     * Reinstall the application.
     * @param bundleId bundle identifier of the application to reinstall.
     */
    public void reinstallApplication(String bundleId)
    {
        InteractsWithApps interactor = webDriverProvider.getUnwrapped(InteractsWithApps.class);
        HasCapabilities hasCapabilities = webDriverProvider.getUnwrapped(HasCapabilities.class);
        String appPath = hasCapabilities.getCapabilities().getCapability("app").toString();
        Validate.isTrue(interactor.removeApp(bundleId),
                "Unable to remove mobile application with the bundle identifier '%s'", bundleId);
        interactor.installApp(appPath);
    }
}
