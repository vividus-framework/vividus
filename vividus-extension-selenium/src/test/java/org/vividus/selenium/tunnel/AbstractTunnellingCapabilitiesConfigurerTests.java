/*
 * Copyright 2019-2022 the original author or authors.
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

package org.vividus.selenium.tunnel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.function.Consumer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.vividus.context.RunContext;

@ExtendWith(MockitoExtension.class)
class AbstractTunnellingCapabilitiesConfigurerTests
{
    private static final String TUNNEL = "tunnel-id-or-tunnel-name";

    @Captor private ArgumentCaptor<TunnelOptions> optionsCaptor;
    @Mock private DesiredCapabilities capabilities;
    @Mock private Consumer<String> tunnelConsumer;
    @Mock private RunContext runContext;
    @Mock private TunnelManager<TunnelOptions> tunnelManager;
    @InjectMocks private TestTunnellingCapabilitiesConfigurer tunnellingConfigurer;

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void shouldConfigureTunnelWithProxy(boolean tunnellingEnabler) throws TunnelException
    {
        tunnellingConfigurer.setTunnellingEnabled(tunnellingEnabler);
        String httpProxy = "localhost:54688";
        Proxy proxy = mock(Proxy.class);

        when(proxy.getHttpProxy()).thenReturn(httpProxy);
        when(capabilities.getCapability(CapabilityType.PROXY)).thenReturn(proxy);
        when(tunnelManager.start(optionsCaptor.capture())).thenReturn(TUNNEL);

        tunnellingConfigurer.configureTunnel(capabilities, tunnelConsumer);

        verify(capabilities).setCapability(CapabilityType.PROXY, (Object) null);
        verify(tunnelConsumer).accept(TUNNEL);
        assertEquals(httpProxy, optionsCaptor.getValue().getProxy());
    }

    @Test
    void shouldConfigureTunnelWithoutProxy() throws TunnelException
    {
        tunnellingConfigurer.setTunnellingEnabled(true);

        when(capabilities.getCapability(CapabilityType.PROXY)).thenReturn(null);
        when(tunnelManager.start(optionsCaptor.capture())).thenReturn(TUNNEL);

        tunnellingConfigurer.configureTunnel(capabilities, tunnelConsumer);

        verify(capabilities).setCapability(CapabilityType.PROXY, (Object) null);
        verify(tunnelConsumer).accept(TUNNEL);
        assertNull(optionsCaptor.getValue().getProxy());
    }

    @Test
    void shouldNotConfigureTunnel()
    {
        tunnellingConfigurer.setTunnellingEnabled(false);

        when(capabilities.getCapability(CapabilityType.PROXY)).thenReturn(null);

        tunnellingConfigurer.configureTunnel(capabilities, tunnelConsumer);

        verifyNoMoreInteractions(capabilities);
        verifyNoInteractions(tunnelConsumer);
    }

    @Test
    void shouldRethrowTunnelException() throws TunnelException
    {
        tunnellingConfigurer.setTunnellingEnabled(true);
        TunnelException thrown = new TunnelException(null);

        when(capabilities.getCapability(CapabilityType.PROXY)).thenReturn(null);
        doThrow(thrown).when(tunnelManager).start(optionsCaptor.capture());

        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> tunnellingConfigurer.configureTunnel(capabilities, tunnelConsumer));

        assertEquals(thrown, exception.getCause());
        verifyNoMoreInteractions(capabilities);
        verifyNoInteractions(tunnelConsumer);
        assertNull(optionsCaptor.getValue().getProxy());
    }

    @Test
    void shouldStopTunnel() throws TunnelException
    {
        tunnellingConfigurer.stopTunnel(null);
        verify(tunnelManager).stop();
    }

    private static class TestTunnellingCapabilitiesConfigurer
            extends AbstractTunnellingCapabilitiesConfigurer<TunnelOptions>
    {
        TestTunnellingCapabilitiesConfigurer(RunContext runContext,
                TunnelManager<TunnelOptions> tunnelManager)
        {
            super(runContext, tunnelManager);
        }

        @Override
        public void configure(DesiredCapabilities desiredCapabilities)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        protected TunnelOptions createOptions()
        {
            return new TunnelOptions();
        }
    }
}
