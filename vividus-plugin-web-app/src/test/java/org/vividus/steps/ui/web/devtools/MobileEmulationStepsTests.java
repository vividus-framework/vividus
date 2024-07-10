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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.chromium.HasCdp;
import org.openqa.selenium.remote.Browser;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.manager.IWebDriverManager;
import org.vividus.util.json.JsonUtils;

@ExtendWith(MockitoExtension.class)
class MobileEmulationStepsTests
{
    @Mock private IWebDriverProvider webDriverProvider;
    @Mock private IWebDriverManager webDriverManager;
    @Mock private HasCdp havingCdpDriver;
    private MobileEmulationSteps steps;

    @BeforeEach
    void init()
    {
        this.steps = new MobileEmulationSteps(webDriverProvider, webDriverManager, new JsonUtils());
    }

    @Test
    void shouldOverrideDeviceMetrics()
    {
        when(webDriverManager.isBrowserAnyOf(Browser.CHROME)).thenReturn(true);
        when(webDriverProvider.getUnwrapped(HasCdp.class)).thenReturn(havingCdpDriver);

        String metrics = """
            {
                "width": 430,
                "height": 932,
                "deviceScaleFactor": 3,
                "mobile": true
            }
            """;

        steps.overrideDeviceMetrics(metrics);

        verify(havingCdpDriver).executeCdpCommand("Emulation.setDeviceMetricsOverride", Map.of(
            "width", 430,
            "height", 932,
            "deviceScaleFactor", 3,
            "mobile", true
        ));
    }

    @Test
    void shouldClearDeviceMetrics()
    {
        when(webDriverManager.isBrowserAnyOf(Browser.CHROME)).thenReturn(true);
        when(webDriverProvider.getUnwrapped(HasCdp.class)).thenReturn(havingCdpDriver);

        steps.clearDeviceMetrics();

        verify(havingCdpDriver).executeCdpCommand("Emulation.clearDeviceMetricsOverride", Map.of());
    }

    @Test
    void shouldFailIfBrowserIsNotChrome()
    {
        when(webDriverManager.isBrowserAnyOf(Browser.CHROME)).thenReturn(false);

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, steps::clearDeviceMetrics);

        assertEquals("The step is only supported by Chrome browser.", thrown.getMessage());
    }
}
