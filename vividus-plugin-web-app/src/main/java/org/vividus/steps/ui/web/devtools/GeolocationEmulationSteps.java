/*
 * Copyright 2019-2025 the original author or authors.
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

package org.vividus.steps.ui.web.devtools;

import java.util.List;
import java.util.Map;

import org.jbehave.core.annotations.When;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.selenium.cdp.BrowserPermissions;
import org.vividus.selenium.manager.WebDriverManager;
import org.vividus.ui.web.cdp.CdpClient;

public class GeolocationEmulationSteps
{
    private static final Logger LOGGER = LoggerFactory.getLogger(GeolocationEmulationSteps.class);

    private final CdpClient cdpClient;
    private final WebDriverManager webDriverManager;

    public GeolocationEmulationSteps(CdpClient cdpClient, WebDriverManager webDriverManager)
    {
        this.cdpClient = cdpClient;
        this.webDriverManager = webDriverManager;
    }

    /**
     * Set the current Geolocation to coordinates with the specified latitude and longitude.
     *
     * <p>
     * <strong>The step is only supported by Chrome browser.</strong>
     * </p>
     *
     * @param latitude The <a href="https://en.wikipedia.org/wiki/Latitude"><i>latitude</i></a>.
     * @param longitude The <a href="https://en.wikipedia.org/wiki/Longitude"><i>longitude</i></a>.
     */
    @When("I emulate Geolocation using coordinates with latitude `$latitude` and longitude `$longitude`")
    public void emulateGeolocation(double latitude, double longitude)
    {
        BrowserPermissions permissions = webDriverManager.getBrowserPermissions();
        if (!permissions.isGeolocationEnabled())
        {
            cdpClient.executeCdpCommand("Browser.grantPermissions", Map.of("permissions", List.of("geolocation")));
            permissions.setGeolocationEnabled(true);
            LOGGER.info("The browser has been granted the permission to track geolocation");
        }

        cdpClient.executeCdpCommand("Emulation.setGeolocationOverride",
                Map.of("latitude", latitude, "longitude", longitude, "accuracy", 1));
    }

    /**
     * Resets the emulated Geolocation position.
     *
     * <p>
     * <strong>The step is only supported by Chrome browser.</strong>
     * </p>
     */
    @When("I reset Geolocation emulation")
    public void clearGeolocationParameters()
    {
        cdpClient.executeCdpCommand("Emulation.clearGeolocationOverride");
    }
}
