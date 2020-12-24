/*
 * Copyright 2019-2020 the original author or authors.
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

package org.vividus.proxy;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.EnumSet;
import java.util.Set;

import com.browserup.bup.BrowserUpProxyServer;
import com.browserup.bup.proxy.CaptureType;
import com.browserup.bup.proxy.dns.AdvancedHostResolver;

import org.junit.jupiter.api.Test;
import org.littleshoot.proxy.MitmManager;
import org.littleshoot.proxy.impl.ThreadPoolConfiguration;
import org.mockito.MockedConstruction;
import org.vividus.proxy.mitm.IMitmManagerFactory;
import org.vividus.proxy.mitm.MitmManagerOptions;

class ProxyServerFactoryTests
{
    private final ProxyServerFactory proxyServerFactory = new ProxyServerFactory();

    @Test
    void testCreateProxyServer()
    {
        Set<CaptureType> expectedHarCaptureTypes = EnumSet.copyOf(CaptureType.getAllContentCaptureTypes());

        proxyServerFactory.setCaptureTypes(CaptureType.getAllContentCaptureTypes());
        assertThat(proxyServerFactory.createProxyServer().getHarCaptureTypes(),
                hasItems(expectedHarCaptureTypes.toArray(new CaptureType[0])));
    }

    @Test
    void testCreateProxyServerConfig()
    {
        MitmManagerOptions mitmManagerOptions = mock(MitmManagerOptions.class);
        IMitmManagerFactory mitmManagerFactory = mock(IMitmManagerFactory.class);
        MitmManager mitmManager = mock(MitmManager.class);
        when(mitmManagerFactory.createMitmManager(mitmManagerOptions)).thenReturn(mitmManager);

        try (MockedConstruction<BrowserUpProxyServer> mockedServer = mockConstruction(BrowserUpProxyServer.class);
                MockedConstruction<ThreadPoolConfiguration> mockedConfig = mockConstruction(
                        ThreadPoolConfiguration.class))
        {
            AdvancedHostResolver hostNameResolver = mock(AdvancedHostResolver.class);

            boolean trustAllServers = true;
            proxyServerFactory.setMitmManagerOptions(mitmManagerOptions);
            proxyServerFactory.setMitmManagerFactory(mitmManagerFactory);
            proxyServerFactory.setTrustAllServers(trustAllServers);
            proxyServerFactory.setMitmEnabled(true);
            proxyServerFactory.setAdvancedHostResolver(hostNameResolver);
            proxyServerFactory.setCaptureTypes(CaptureType.getAllContentCaptureTypes());
            proxyServerFactory.createProxyServer();
            int expectedThreadsCount = 16;

            assertEquals(1, mockedConfig.constructed().size());
            ThreadPoolConfiguration config = mockedConfig.constructed().get(0);
            verify(config).withClientToProxyWorkerThreads(expectedThreadsCount);
            verify(config).withProxyToServerWorkerThreads(expectedThreadsCount);

            assertEquals(1, mockedServer.constructed().size());
            BrowserUpProxyServer server = mockedServer.constructed().get(0);
            verify(server).setTrustAllServers(trustAllServers);
            verify(server).setMitmManager(mitmManager);
            verify(server).setThreadPoolConfiguration(config);
            verify(server).setHostNameResolver(hostNameResolver);
            verify(server).enableHarCaptureTypes(CaptureType.getAllContentCaptureTypes());
        }
    }

    @Test
    void testCreateProxyServerConfigDisableMitm()
    {
        try (MockedConstruction<BrowserUpProxyServer> mockedServer = mockConstruction(BrowserUpProxyServer.class))
        {
            proxyServerFactory.setMitmEnabled(false);
            proxyServerFactory.createProxyServer();

            assertEquals(1, mockedServer.constructed().size());
            BrowserUpProxyServer server = mockedServer.constructed().get(0);
            verify(server, never()).setMitmManager(any());
        }
    }
}
