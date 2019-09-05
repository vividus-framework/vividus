/*
 * Copyright 2019 the original author or authors.
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import com.browserup.bup.BrowserUpProxy;
import com.browserup.bup.BrowserUpProxyServer;
import com.browserup.bup.filters.RequestFilter;
import com.browserup.bup.filters.RequestFilterAdapter;
import com.browserup.bup.filters.ResponseFilter;
import com.browserup.bup.filters.ResponseFilterAdapter;
import com.browserup.harreader.model.Har;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.littleshoot.proxy.HttpFiltersSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProxyTests
{
    private static final String PROXY_NOT_STARTED = "Proxy is not started";

    private IProxy proxy;

    @Mock
    private IProxyServerFactory proxyServerFactory;

    @Mock
    private BrowserUpProxy browserMobProxy;

    @Test
    void testIfNotStartedAfterCreation()
    {
        configureProxy();
        assertFalse(proxy.isStarted());
        verifyZeroInteractions(proxyServerFactory);
    }

    @Test
    void testStart()
    {
        configureProxy();
        when(proxyServerFactory.createProxyServer()).thenReturn(browserMobProxy);
        proxy.start();
        verify(browserMobProxy).start();
    }

    @Test
    void testStartTwice()
    {
        configureProxy();
        when(proxyServerFactory.createProxyServer()).thenReturn(browserMobProxy);
        proxy.start();
        proxy.start();
        verify(browserMobProxy, times(1)).start();
    }

    @Test
    void testGetServer()
    {
        configureProxy();
        when(proxyServerFactory.createProxyServer()).thenReturn(browserMobProxy);
        proxy.start();
        assertEquals(browserMobProxy, proxy.getProxyServer());
    }

    @Test
    void testStop()
    {
        configureProxy();
        when(proxyServerFactory.createProxyServer()).thenReturn(browserMobProxy);
        proxy.start();
        proxy.stop();
        verify(browserMobProxy).stop();
        assertNull(proxy.getProxyServer());
    }

    @Test
    void testStopTwice()
    {
        configureProxy();
        when(proxyServerFactory.createProxyServer()).thenReturn(browserMobProxy);
        proxy.start();
        proxy.stop();
        proxy.stop();
        verify(browserMobProxy, times(1)).stop();
    }

    @Test
    void testStartRecording()
    {
        configureProxy();
        when(proxyServerFactory.createProxyServer()).thenReturn(browserMobProxy);
        proxy.start();
        proxy.startRecording();
        verify(browserMobProxy).newHar();
    }

    @Test
    void testStartRecordingWhenProxyIsNotStarted()
    {
        configureProxy();
        IllegalStateException exception = assertThrows(IllegalStateException.class, proxy :: startRecording);
        assertEquals(PROXY_NOT_STARTED, exception.getMessage());
        verifyZeroInteractions(browserMobProxy);
    }

    @Test
    void testStopRecording()
    {
        configureProxy();
        when(proxyServerFactory.createProxyServer()).thenReturn(browserMobProxy);
        when(browserMobProxy.getHar()).thenReturn(new Har());
        proxy.start();
        proxy.startRecording();
        proxy.stopRecording();
        verify(browserMobProxy).endHar();
    }

    @Test
    void testStopRecordingWithoutHar()
    {
        configureProxy();
        when(proxyServerFactory.createProxyServer()).thenReturn(browserMobProxy);
        when(browserMobProxy.getHar()).thenReturn(null);
        proxy.start();
        proxy.startRecording();
        proxy.stopRecording();
        verify(browserMobProxy, never()).endHar();
    }

    @Test
    void testStopRecordingWhenProxyIsNotStarted()
    {
        configureProxy();
        IllegalStateException exception = assertThrows(IllegalStateException.class, proxy :: stopRecording);
        assertEquals(PROXY_NOT_STARTED, exception.getMessage());
        verifyZeroInteractions(browserMobProxy);
    }

    @Test
    void testGetLog()
    {
        configureProxy();
        when(proxyServerFactory.createProxyServer()).thenReturn(browserMobProxy);
        proxy.start();
        proxy.getLog();
        verify(browserMobProxy).getHar();
    }

    @Test
    void testGetLogWhenProxyIsNotStarted()
    {
        configureProxy();
        IllegalStateException exception = assertThrows(IllegalStateException.class, proxy :: getLog);
        assertEquals(PROXY_NOT_STARTED, exception.getMessage());
        verifyZeroInteractions(browserMobProxy);
    }

    @Test
    void testAddRequestFilter()
    {
        configureProxy();
        RequestFilter requestFilter = mock(RequestFilter.class);
        when(proxyServerFactory.createProxyServer()).thenReturn(browserMobProxy);
        proxy.start();
        proxy.addRequestFilter(requestFilter);
        verify(browserMobProxy).addRequestFilter(requestFilter);
    }

    @Test
    void testClearRequestFilters()
    {
        configureProxy();
        BrowserUpProxyServer browserMobProxyServer = mock(BrowserUpProxyServer.class);
        ResponseFilter responseFilter = mock(ResponseFilter.class);
        RequestFilter requestFilter = mock(RequestFilter.class);
        ResponseFilterAdapter.FilterSource fsResponse = new ResponseFilterAdapter.FilterSource(responseFilter);
        RequestFilterAdapter.FilterSource fsRequest = new RequestFilterAdapter.FilterSource(requestFilter);
        when(proxyServerFactory.createProxyServer()).thenReturn(browserMobProxyServer);
        List<HttpFiltersSource> toRemove = new ArrayList<>();
        toRemove.add(fsResponse);
        toRemove.add(fsRequest);
        when(browserMobProxyServer.getFilterFactories()).thenReturn(toRemove).thenReturn(toRemove);
        proxy.start();
        proxy.clearRequestFilters();
        assertTrue(toRemove.size() == 1 && toRemove.contains(fsResponse));
    }

    private void configureProxy()
    {
        proxy = new Proxy();
        ((Proxy) proxy).setProxyServerFactory(proxyServerFactory);
    }
}
