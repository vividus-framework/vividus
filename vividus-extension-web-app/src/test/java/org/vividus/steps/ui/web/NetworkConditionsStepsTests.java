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

import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.ui.web.cdp.CdpClient;

@ExtendWith(MockitoExtension.class)
class NetworkConditionsStepsTests
{
    private static final String OVERRIDE_NETWORK_STATE = "Network.overrideNetworkState";
    private static final String EMULATE_NETWORK_CONDITIONS_BY_RULE = "Network.emulateNetworkConditionsByRule";

    @Mock private CdpClient cdpClient;
    @InjectMocks private NetworkConditionsSteps networkConditionsSteps;

    @Test
    void shouldOverrideNetworkState()
    {
        String networkState = """
            {
                "offline": true,
                "downloadThroughput": 0,
                "uploadThroughput": 0,
                "latency": 0
            }
            """;

        networkConditionsSteps.overrideNetworkState(networkState);

        verify(cdpClient).executeCdpCommand(OVERRIDE_NETWORK_STATE, networkState);
    }

    @Test
    void shouldEmulateNetworkConditions()
    {
        String networkConditions = """
            {
                "matchedNetworkConditions": [
                    {
                        "urlPattern": "",
                        "downloadThroughput": 50000,
                        "uploadThroughput": 20000,
                        "latency": 200
                    }
                ]
            }
            """;

        networkConditionsSteps.emulateNetworkConditions(networkConditions);

        verify(cdpClient).executeCdpCommand(EMULATE_NETWORK_CONDITIONS_BY_RULE, networkConditions);
    }

    @Test
    void shouldResetNetworkConditionsEmulation()
    {
        networkConditionsSteps.resetNetworkConditionsEmulation();

        verify(cdpClient).executeCdpCommand(OVERRIDE_NETWORK_STATE, Map.of(
                "offline", false,
                "downloadThroughput", -1L,
                "uploadThroughput", -1L,
                "latency", 0L
        ));
        verify(cdpClient).executeCdpCommand(EMULATE_NETWORK_CONDITIONS_BY_RULE, Map.of(
                "matchedNetworkConditions", List.of()
        ));
    }
}
