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

package org.vividus.proxy;

import java.net.InetAddress;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import com.browserup.bup.BrowserUpProxy;
import com.browserup.bup.BrowserUpProxyServer;
import com.browserup.bup.client.ClientUtil;

import de.sstoehr.harreader.model.Har;

public class Proxy implements IProxy
{
    private final IProxyServerFactory proxyServerFactory;
    private final String proxyHost;
    private BrowserUpProxyServer proxyServer;
    private MockRequestFilter mockRequestFilter;

    public Proxy(IProxyServerFactory proxyServerFactory, String proxyHost)
    {
        this.proxyServerFactory = proxyServerFactory;
        this.proxyHost = proxyHost;
    }

    @Override
    public void start()
    {
        startProxy(BrowserUpProxy::start);
    }

    public void start(int port, InetAddress address)
    {
        startProxy(proxy -> proxy.start(port, address));
    }

    private void startProxy(Consumer<BrowserUpProxy> starter)
    {
        if (!isStarted())
        {
            proxyServer = proxyServerFactory.createProxyServer();
            mockRequestFilter = new MockRequestFilter();
            proxyServer.addRequestFilter(mockRequestFilter);
            starter.accept(proxyServer);
        }
    }

    @Override
    public void startRecording()
    {
        getIfProxyStarted(BrowserUpProxy::newHar);
    }

    @Override
    public void clearRecordedData()
    {
        startRecording();
    }

    @Override
    public void stopRecording()
    {
        getIfProxyStarted(BrowserUpProxy::endHar);
    }

    @Override
    public void stop()
    {
        if (isStarted())
        {
            proxyServer.stop();
            proxyServer = null;
            mockRequestFilter = null;
        }
    }

    @Override
    public boolean isStarted()
    {
        return proxyServer != null;
    }

    @Override
    public Har getRecordedData()
    {
        return getIfProxyStarted(BrowserUpProxy::getHar);
    }

    @Override
    public void addMock(ProxyMock proxyMock)
    {
        executeIfProxyStarted(proxy -> mockRequestFilter.getProxyMocks().add(proxyMock));
    }

    @Override
    public void clearMocks()
    {
        executeIfProxyStarted(proxy -> mockRequestFilter.getProxyMocks().clear());
    }

    @Override
    public org.openqa.selenium.Proxy createSeleniumProxy()
    {
        return getIfProxyStarted(proxy ->
                Optional.ofNullable(proxyHost)
                       .map(hostName -> createSeleniumProxy(hostName, proxy.getPort()))
                       .orElseGet(() -> ClientUtil.createSeleniumProxy(proxy, InetAddress.getLoopbackAddress()))
        );
    }

    private org.openqa.selenium.Proxy createSeleniumProxy(String hostName, int port)
    {
        String proxyAddress = hostName + ':' + port;
        return new org.openqa.selenium.Proxy().setHttpProxy(proxyAddress)
                                              .setSslProxy(proxyAddress)
                                              .setProxyType(org.openqa.selenium.Proxy.ProxyType.MANUAL);
    }

    BrowserUpProxy getProxyServer()
    {
        return proxyServer;
    }

    private void executeIfProxyStarted(Consumer<BrowserUpProxyServer> proxyAction)
    {
        getIfProxyStarted(proxy -> {
            proxyAction.accept(proxy);
            return null;
        });
    }

    private <R> R getIfProxyStarted(Function<BrowserUpProxyServer, R> proxyAction)
    {
        if (isStarted())
        {
            return proxyAction.apply(proxyServer);
        }
        throw new IllegalStateException("Proxy is not started");
    }
}
