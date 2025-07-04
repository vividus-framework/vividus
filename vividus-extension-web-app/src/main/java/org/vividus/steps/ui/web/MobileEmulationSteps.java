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

package org.vividus.steps.ui.web;

import org.jbehave.core.annotations.When;
import org.vividus.ui.web.cdp.CdpClient;

public class MobileEmulationSteps
{
    private final CdpClient cdpClient;

    public MobileEmulationSteps(CdpClient cdpClient)
    {
        this.cdpClient = cdpClient;
    }

    /**
     * Emulates mobile device using the provided configuration.
     * <p>
     * <strong>The step is only supported by Chrome browser.</strong>
     * </p>
     * @param jsonConfiguration The JSON containing device metrics to override.
     */
    @When("I emulate mobile device with configuration:`$jsonConfiguration`")
    public void overrideDeviceMetrics(String jsonConfiguration)
    {
        cdpClient.executeCdpCommand("Emulation.setDeviceMetricsOverride", jsonConfiguration);
    }

    /**
     * Resets the mobile device emulation returning the browser to its initial state.
     * <p>
     * <strong>The step is only supported by Chrome browser.</strong>
     * </p>
     */
    @When("I reset mobile device emulation")
    public void clearDeviceMetrics()
    {
        cdpClient.executeCdpCommand("Emulation.clearDeviceMetricsOverride");
    }
}
