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

package org.vividus.ui.web.cdp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import com.google.common.eventbus.EventBus;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.chromium.HasCdp;
import org.openqa.selenium.remote.Browser;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.manager.IWebDriverManager;
import org.vividus.ui.web.event.DeviceMetricsOverrideEvent;
import org.vividus.util.json.JsonUtils;

@ExtendWith(MockitoExtension.class)
class SeleniumCdpClientTests
{
    @Mock private IWebDriverProvider webDriverProvider;
    @Mock private IWebDriverManager webDriverManager;
    @Mock private HasCdp havingCdpDriver;
    @Mock private EventBus eventBus;
    private SeleniumCdpClient cdpClient;

    @BeforeEach
    void init()
    {
        this.cdpClient = new SeleniumCdpClient(webDriverProvider, webDriverManager, new JsonUtils(), eventBus);
    }

    @Test
    void shouldSendCdpCommandWithNoArgs()
    {
        when(webDriverManager.isBrowserAnyOf(Browser.CHROME)).thenReturn(true);
        when(webDriverProvider.getUnwrapped(HasCdp.class)).thenReturn(havingCdpDriver);

        String command = "Emulation.clearDeviceMetricsOverride";
        cdpClient.executeCdpCommand(command);

        verify(havingCdpDriver).executeCdpCommand(eq(command), eq(Map.of()));
        verify(eventBus).post(any(DeviceMetricsOverrideEvent.class));
    }

    @Test
    void shouldSendCdpCommandDeviceMetricsOverrideEventNotPosted()
    {
        when(webDriverManager.isBrowserAnyOf(Browser.CHROME)).thenReturn(true);
        when(webDriverProvider.getUnwrapped(HasCdp.class)).thenReturn(havingCdpDriver);

        String command = "Emulation.clearGeolocationOverride";
        cdpClient.executeCdpCommand(command);

        verify(havingCdpDriver).executeCdpCommand(eq(command), eq(Map.of()));
        verifyNoInteractions(eventBus);
    }

    @Test
    void shouldSendCdpCommandWithArgs()
    {
        when(webDriverManager.isBrowserAnyOf(Browser.CHROME)).thenReturn(true);
        when(webDriverProvider.getUnwrapped(HasCdp.class)).thenReturn(havingCdpDriver);

        String command = "Emulation.setDeviceMetricsOverride";
        String metrics = """
            {
                "width": 430,
                "height": 932,
                "deviceScaleFactor": 3,
                "mobile": true
            }
            """;
        cdpClient.executeCdpCommand(command, metrics);

        verify(havingCdpDriver).executeCdpCommand(command, Map.of(
                "width", 430,
                "height", 932,
                "deviceScaleFactor", 3,
                "mobile", true
        ));
        verify(eventBus).post(any(DeviceMetricsOverrideEvent.class));
    }

    @Test
    void shouldFailIfBrowserIsNotChrome()
    {
        Map<String, Object> params = new HashMap<>();
        when(webDriverManager.isBrowserAnyOf(Browser.CHROME)).thenReturn(false);

        var exception = assertThrows(IllegalArgumentException.class,
                () -> cdpClient.executeCdpCommand("command", params));
        assertEquals("The step is only supported by Chrome browser.", exception.getMessage());
    }
}
