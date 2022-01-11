/*
 * Copyright 2019-2021 the original author or authors.
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

package org.vividus.selenium.browserstack;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.vividus.context.RunContext;
import org.vividus.model.RunningStory;
import org.vividus.selenium.tunnel.TunnelException;
import org.vividus.selenium.tunnel.TunnelOptions;

@ExtendWith(MockitoExtension.class)
class BrowserStackCapabilitiesConfigurerTests
{
    private static final String BSTACK_OPTIONS = "bstack:options";

    @Captor private ArgumentCaptor<TunnelOptions> optionsCaptor;
    @Mock private RunContext runContext;
    @Mock private BrowserStackLocalManager browserStackLocalManager;
    @InjectMocks private BrowserStackCapabilitiesConfigurer configurer;

    @Test
    void shouldConfigureTestName()
    {
        String name = "name";
        RunningStory runningStory = mock(RunningStory.class);
        DesiredCapabilities desiredCapabilities = new DesiredCapabilities();

        when(runContext.getRootRunningStory()).thenReturn(runningStory);
        when(runningStory.getName()).thenReturn(name);

        configurer.configure(desiredCapabilities);

        assertEquals(Map.of(BSTACK_OPTIONS, Map.of("sessionName", name)), desiredCapabilities.asMap());
        verifyNoMoreInteractions(runContext, runningStory);
    }

    @Test
    void shouldConfigureTunnel() throws TunnelException
    {
        configurer.setTunnellingEnabled(true);
        String proxyHost = "localhost:52745";
        String localIdentifier = "local-identifier";

        Proxy proxy = mock(Proxy.class);
        DesiredCapabilities desiredCapabilities = new DesiredCapabilities();

        when(proxy.getHttpProxy()).thenReturn(proxyHost);
        desiredCapabilities.setCapability(CapabilityType.PROXY, proxy);
        when(browserStackLocalManager.start(optionsCaptor.capture())).thenReturn(localIdentifier);

        configurer.configure(desiredCapabilities);

        TunnelOptions options = optionsCaptor.getValue();
        assertEquals(Map.of(BSTACK_OPTIONS, Map.of("localIdentifier", localIdentifier, "local", true)),
                desiredCapabilities.asMap());
        assertEquals(proxyHost, options.getProxy());
    }
}
