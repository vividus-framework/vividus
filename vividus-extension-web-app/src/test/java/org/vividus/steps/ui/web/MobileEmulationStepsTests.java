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

import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.ui.web.cdp.CdpClient;

@ExtendWith(MockitoExtension.class)
public class MobileEmulationStepsTests
{
    @Mock private CdpClient cdpClient;
    @InjectMocks private MobileEmulationSteps mobileEmulationSteps;

    @Test
    void shouldClearDeviceMetrics()
    {
        mobileEmulationSteps.clearDeviceMetrics();
        verify(cdpClient).executeCdpCommand("Emulation.clearDeviceMetricsOverride");
    }

    @Test
    void shouldOverrideDeviceMetrics()
    {
        String metrics = """
            {
                "width": 430,
                "height": 932,
                "deviceScaleFactor": 3,
                "mobile": true
            }
            """;
        mobileEmulationSteps.overrideDeviceMetrics(metrics);
        verify(cdpClient).executeCdpCommand("Emulation.setDeviceMetricsOverride", metrics);
    }
}
