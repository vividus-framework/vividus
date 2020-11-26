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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import com.browserup.bup.BrowserUpProxyServer;
import com.browserup.bup.filters.RequestFilter;
import com.browserup.bup.filters.RequestFilterAdapter;
import com.browserup.bup.filters.ResponseFilter;
import com.browserup.bup.filters.ResponseFilterAdapter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.littleshoot.proxy.HttpFiltersSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.Proxy.ProxyType;

@ExtendWith(MockitoExtension.class)
class ProxyTests
{
    @Mock private IProxyServerFactory proxyServerFactory;
    @Mock private BrowserUpProxyServer browserUpProxyServer;
    @InjectMocks private Proxy proxy;

    @Test
    void testIfNotStartedAfterCreation()
    {
        assertFalse(proxy.isStarted());
        verifyNoInteractions(proxyServerFactory);
    }

    @Test
    void testStart()
    {
        when(proxyServerFactory.createProxyServer()).thenReturn(browserUpProxyServer);
        proxy.start();
        verify(browserUpProxyServer).start();
    }

    @Test
    void testStartOnAddr() throws UnknownHostException
    {
        when(proxyServerFactory.createProxyServer()).thenReturn(browserUpProxyServer);
        InetAddress address = InetAddress.getLocalHost();
        int port = 8080;
        proxy.start(port, address);
        verify(browserUpProxyServer).start(port, address);
    }

    @Test
    void testStartTwice()
    {
        when(proxyServerFactory.createProxyServer()).thenReturn(browserUpProxyServer);
        proxy.start();
        proxy.start();
        verify(browserUpProxyServer, times(1)).start();
    }

    @Test
    void testGetServer()
    {
        when(proxyServerFactory.createProxyServer()).thenReturn(browserUpProxyServer);
        proxy.start();
        assertEquals(browserUpProxyServer, proxy.getProxyServer());
    }

    @Test
    void testStop()
    {
        when(proxyServerFactory.createProxyServer()).thenReturn(browserUpProxyServer);
        proxy.start();
        proxy.stop();
        verify(browserUpProxyServer).stop();
        assertNull(proxy.getProxyServer());
    }

    @Test
    void testStopTwice()
    {
        when(proxyServerFactory.createProxyServer()).thenReturn(browserUpProxyServer);
        proxy.start();
        proxy.stop();
        proxy.stop();
        verify(browserUpProxyServer, times(1)).stop();
    }

    @Test
    void testStartRecording()
    {
        when(proxyServerFactory.createProxyServer()).thenReturn(browserUpProxyServer);
        proxy.start();
        proxy.startRecording();
        verify(browserUpProxyServer).newHar();
    }

    @Test
    void testClearRecordedData()
    {
        when(proxyServerFactory.createProxyServer()).thenReturn(browserUpProxyServer);
        proxy.start();
        proxy.clearRecordedData();
        verify(browserUpProxyServer).newHar();
    }

    @Test
    void testGetRecordedData()
    {
        when(proxyServerFactory.createProxyServer()).thenReturn(browserUpProxyServer);
        proxy.start();
        proxy.getRecordedData();
        verify(browserUpProxyServer).getHar();
    }

    @Test
    void testStopRecording()
    {
        when(proxyServerFactory.createProxyServer()).thenReturn(browserUpProxyServer);
        proxy.start();
        proxy.startRecording();
        proxy.stopRecording();
        verify(browserUpProxyServer).endHar();
    }

    @Test
    void testAddRequestFilter()
    {
        RequestFilter requestFilter = mock(RequestFilter.class);
        when(proxyServerFactory.createProxyServer()).thenReturn(browserUpProxyServer);
        proxy.start();
        proxy.addRequestFilter(requestFilter);
        verify(browserUpProxyServer).addRequestFilter(requestFilter);
    }

    @Test
    void testClearRequestFilters()
    {
        BrowserUpProxyServer browserUpProxyServer = mock(BrowserUpProxyServer.class);
        ResponseFilter responseFilter = mock(ResponseFilter.class);
        RequestFilter requestFilter = mock(RequestFilter.class);
        ResponseFilterAdapter.FilterSource fsResponse = new ResponseFilterAdapter.FilterSource(responseFilter);
        RequestFilterAdapter.FilterSource fsRequest = new RequestFilterAdapter.FilterSource(requestFilter);
        when(proxyServerFactory.createProxyServer()).thenReturn(browserUpProxyServer);
        List<HttpFiltersSource> toRemove = new ArrayList<>();
        toRemove.add(fsResponse);
        toRemove.add(fsRequest);
        when(browserUpProxyServer.getFilterFactories()).thenReturn(toRemove).thenReturn(toRemove);
        proxy.start();
        proxy.clearRequestFilters();
        assertTrue(toRemove.size() == 1 && toRemove.contains(fsResponse));
    }

    @Test
    void shouldCreateSeleniumProxy()
    {
        when(proxyServerFactory.createProxyServer()).thenReturn(browserUpProxyServer);
        proxy.start();
        when(browserUpProxyServer.getPort()).thenReturn(101);
        org.openqa.selenium.Proxy seleniumProxy = proxy.createSeleniumProxy();
        assertEquals(InetAddress.getLoopbackAddress().getHostName() + ":101", seleniumProxy.getHttpProxy());
    }

    @Test
    void shouldUseProvidedHostForASeleniumProxy()
    {
        Proxy proxy = new Proxy(proxyServerFactory, "host.docker.internal");
        when(proxyServerFactory.createProxyServer()).thenReturn(browserUpProxyServer);
        proxy.start();
        when(browserUpProxyServer.getPort()).thenReturn(101);
        org.openqa.selenium.Proxy seleniumProxy = proxy.createSeleniumProxy();
        String proxyHostAndPort = "host.docker.internal:101";
        assertEquals(proxyHostAndPort, seleniumProxy.getHttpProxy());
        assertEquals(proxyHostAndPort, seleniumProxy.getSslProxy());
        assertEquals(ProxyType.MANUAL, seleniumProxy.getProxyType());
    }

    static Stream<Consumer<Proxy>> proxyActions()
    {
        return Stream.of(
                Proxy::startRecording,
                Proxy::stopRecording,
                Proxy::getRecordedData,
                Proxy::clearRecordedData,
                Proxy::clearRequestFilters,
                Proxy::createSeleniumProxy,
                proxy -> proxy.addRequestFilter(mock(RequestFilter.class))
        );
    }

    @ParameterizedTest
    @MethodSource("proxyActions")
    void shouldNotRunProxyActionWhenProxyIsNotStarted(Consumer<Proxy> proxyAction)
    {
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> proxyAction.accept(proxy));
        assertEquals("Proxy is not started", exception.getMessage());
        verifyNoInteractions(browserUpProxyServer);
    }
}
