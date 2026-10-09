/*
 * Copyright 2019-2026 the original author or authors.
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

import java.util.List;
import java.util.Map;

import org.jbehave.core.annotations.When;
import org.vividus.ui.web.cdp.CdpClient;

public class NetworkConditionsSteps
{
    private static final String OVERRIDE_NETWORK_STATE = "Network.overrideNetworkState";
    private static final String EMULATE_NETWORK_CONDITIONS_BY_RULE = "Network.emulateNetworkConditionsByRule";

    private final CdpClient cdpClient;

    public NetworkConditionsSteps(CdpClient cdpClient)
    {
        this.cdpClient = cdpClient;
    }

    /**
     * Overrides browser network state (for example, online/offline status reported to the page).
     *
     * <p>
     * <strong>The step is only supported by Chrome browser.</strong>
     * </p>
     *
     * @param jsonConfiguration The JSON containing network state to override.
     */
    @When("I override network state with configuration:`$jsonConfiguration`")
    public void overrideNetworkState(String jsonConfiguration)
    {
        cdpClient.executeCdpCommand(OVERRIDE_NETWORK_STATE, jsonConfiguration);
    }

    /**
     * Emulates network conditions for matching requests (for example, latency and throughput limits).
     *
     * <p>
     * <strong>The step is only supported by Chrome browser.</strong>
     * </p>
     *
     * @param jsonConfiguration The JSON containing network conditions rules to apply.
     */
    @When("I emulate network conditions with configuration:`$jsonConfiguration`")
    public void emulateNetworkConditions(String jsonConfiguration)
    {
        cdpClient.executeCdpCommand(EMULATE_NETWORK_CONDITIONS_BY_RULE, jsonConfiguration);
    }

    /**
     * Resets overridden network state and emulated network conditions.
     *
     * <p>
     * <strong>The step is only supported by Chrome browser.</strong>
     * </p>
     */
    @When("I reset network conditions emulation")
    public void resetNetworkConditionsEmulation()
    {
        cdpClient.executeCdpCommand(OVERRIDE_NETWORK_STATE, Map.of(
                "offline", false,
                "downloadThroughput", -1L,
                "uploadThroughput", -1L,
                "latency", 0L
        ));
        cdpClient.executeCdpCommand(EMULATE_NETWORK_CONDITIONS_BY_RULE, Map.of(
                "matchedNetworkConditions", List.of()
        ));
    }
}
