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

package org.vividus.ui.web.playwright.steps.devtools;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.microsoft.playwright.CDPSession;
import com.microsoft.playwright.Page;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.ui.web.playwright.BrowserContextProvider;
import org.vividus.ui.web.playwright.UiContext;

@ExtendWith(MockitoExtension.class)
public class MobileEmulationStepsTests
{
    @Mock private BrowserContextProvider browserContextProvider;
    @Mock private UiContext uiContext;
    @InjectMocks private MobileEmulationSteps mobileEmulationSteps;

    @Test
    void shouldOverrideDeviceMetrics()
    {
        Page page = mock();
        when(uiContext.getCurrentPage()).thenReturn(page);
        CDPSession cdpSession =  mock(CDPSession.class);
        when(browserContextProvider.getCdpSession(page)).thenReturn(cdpSession);

        String metrics = """
            {
                "width": 430,
                "height": 932,
                "deviceScaleFactor": 3,
                "mobile": true
            }
            """;
        mobileEmulationSteps.overrideDeviceMetrics(metrics);
        JsonObject expectedArgs = JsonParser.parseString(metrics).getAsJsonObject();
        verify(cdpSession).send(eq("Emulation.setDeviceMetricsOverride"), eq(expectedArgs));
    }

    @Test
    void shouldClearDeviceMetrics()
    {
        Page page = mock();
        when(uiContext.getCurrentPage()).thenReturn(page);
        CDPSession cdpSession =  mock(CDPSession.class);
        when(browserContextProvider.getCdpSession(page)).thenReturn(cdpSession);

        mobileEmulationSteps.clearDeviceMetrics();
        verify(cdpSession).send(eq("Emulation.clearDeviceMetricsOverride"));
    }
}
