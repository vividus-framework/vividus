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

import static org.mockito.Mockito.verify;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.ui.web.action.CdpActions;

@ExtendWith(MockitoExtension.class)
class GeolocationEmulationStepsTests
{
    @Mock private CdpActions cdpActions;
    @InjectMocks private GeolocationEmulationSteps steps;

    @Test
    void shouldEmulateGeolocation()
    {
        double latitude = 55.410014;
        double longitude = 28.628027;

        steps.emulateGeolocation(latitude, longitude);

        verify(cdpActions).executeCdpCommand("Emulation.setGeolocationOverride", Map.of(
                "latitude", latitude,
                "longitude", longitude,
                "accuracy", 1
        ));
    }

    @Test
    void shouldClearGeolocationParameters()
    {
        steps.clearGeolocationParameters();

        verify(cdpActions).executeCdpCommand("Emulation.clearGeolocationOverride", Map.of());
    }
}
