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

package org.vividus.ui.web.playwright.cdp;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.microsoft.playwright.CDPSession;
import com.microsoft.playwright.Page;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.ui.web.playwright.BrowserContextProvider;
import org.vividus.ui.web.playwright.UiContext;

@ExtendWith(MockitoExtension.class)
class PlaywrightCdpClientTests
{
    @Mock private BrowserContextProvider browserContextProvider;
    @Mock private UiContext uiContext;
    @Mock private Page page;
    @Mock private CDPSession cdpSession;
    @InjectMocks private PlaywrightCdpClient cdpClient;

    @BeforeEach
    void setUp()
    {
        when(uiContext.getCurrentPage()).thenReturn(page);
        when(browserContextProvider.getCdpSession(page)).thenReturn(cdpSession);
    }

    @Test
    void shouldExecuteCdpCommandWithArgsString()
    {
        String command = "Emulation.setAutoDarkModeOverride";
        String argsAsJsonString = "{\"enabled\": true}";
        cdpClient.executeCdpCommand(command, argsAsJsonString);

        JsonObject expectedArgs = JsonParser.parseString(argsAsJsonString).getAsJsonObject();
        verify(cdpSession).send(eq(command), eq(expectedArgs));
    }

    @Test
    void shouldExecuteCdpCommandWithArgsMap()
    {
        String key = "value";
        String command = "Emulation.setScriptExecutionDisabled";
        cdpClient.executeCdpCommand(command, Map.of(key, true));

        JsonObject expectedArgs = new JsonObject();
        expectedArgs.addProperty(key, true);
        verify(cdpSession).send(eq(command), eq(expectedArgs));
    }

    @Test
    void shouldExecuteCdpCommandWithNoArgs()
    {
        String command = "Emulation.clearDeviceMetricsOverride";
        cdpClient.executeCdpCommand(command);
        verify(cdpSession).send(eq(command));
    }
}
