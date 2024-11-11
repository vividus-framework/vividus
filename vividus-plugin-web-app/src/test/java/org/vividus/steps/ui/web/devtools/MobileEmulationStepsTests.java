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

package org.vividus.steps.ui.web.devtools;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import java.util.Map;

import com.google.common.eventbus.EventBus;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.ui.web.action.CdpActions;
import org.vividus.ui.web.event.DeviceMetricsOverrideEvent;
import org.vividus.util.json.JsonUtils;

@ExtendWith(MockitoExtension.class)
class MobileEmulationStepsTests
{
    @Mock private CdpActions cdpActions;
    @Mock private EventBus eventBus;
    private MobileEmulationSteps steps;

    @BeforeEach
    void init()
    {
        this.steps = new MobileEmulationSteps(cdpActions, new JsonUtils(), eventBus);
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

        steps.overrideDeviceMetrics(metrics);

        verify(cdpActions).executeCdpCommand("Emulation.setDeviceMetricsOverride", Map.of(
            "width", 430,
            "height", 932,
            "deviceScaleFactor", 3,
            "mobile", true
        ));
        verify(eventBus).post(any(DeviceMetricsOverrideEvent.class));
    }

    @Test
    void shouldClearDeviceMetrics()
    {
        steps.clearDeviceMetrics();

        verify(cdpActions).executeCdpCommand("Emulation.clearDeviceMetricsOverride", Map.of());
        verify(eventBus).post(any(DeviceMetricsOverrideEvent.class));
    }
}
