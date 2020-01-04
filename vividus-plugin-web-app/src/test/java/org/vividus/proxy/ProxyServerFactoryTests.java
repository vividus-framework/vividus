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

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.EnumSet;
import java.util.Set;

import com.browserup.bup.BrowserUpProxyServer;
import com.browserup.bup.proxy.CaptureType;
import com.browserup.bup.proxy.dns.AdvancedHostResolver;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.littleshoot.proxy.impl.ThreadPoolConfiguration;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore("javax.net.ssl.*")
public class ProxyServerFactoryTests
{
    private final ProxyServerFactory proxyServerFactory = new ProxyServerFactory();

    @Test
    public void testCreateProxyServer()
    {
        Set<CaptureType> expectedHarCaptureTypes = EnumSet.copyOf(CaptureType.getAllContentCaptureTypes());

        proxyServerFactory.setCaptureTypes(CaptureType.getAllContentCaptureTypes());
        assertThat(proxyServerFactory.createProxyServer().getHarCaptureTypes(),
                hasItems(expectedHarCaptureTypes.toArray(new CaptureType[0])));
    }

    @Test
    @PrepareForTest({BrowserUpProxyServer.class, ThreadPoolConfiguration.class, ProxyServerFactory.class})
    public void testCreateProxyServerConfig() throws Exception
    {
        AdvancedHostResolver hostNameResolver = mock(AdvancedHostResolver.class);
        BrowserUpProxyServer mockedServer = mock(BrowserUpProxyServer.class);
        PowerMockito.whenNew(BrowserUpProxyServer.class).withNoArguments().thenReturn(mockedServer);
        ThreadPoolConfiguration mockedConfig = mock(ThreadPoolConfiguration.class);
        PowerMockito.whenNew(ThreadPoolConfiguration.class).withNoArguments().thenReturn(mockedConfig);

        boolean trustAllServers = true;
        proxyServerFactory.setTrustAllServers(trustAllServers);
        proxyServerFactory.setAdvancedHostResolver(hostNameResolver);
        proxyServerFactory.setCaptureTypes(CaptureType.getAllContentCaptureTypes());
        proxyServerFactory.createProxyServer();
        int expectedThreadsCount = 16;
        verify(mockedConfig).withClientToProxyWorkerThreads(expectedThreadsCount);
        verify(mockedConfig).withProxyToServerWorkerThreads(expectedThreadsCount);
        verify(mockedServer).setTrustAllServers(trustAllServers);
        verify(mockedServer).setThreadPoolConfiguration(mockedConfig);
        verify(mockedServer).setHostNameResolver(hostNameResolver);
        verify(mockedServer).enableHarCaptureTypes(CaptureType.getAllContentCaptureTypes());
    }
}
