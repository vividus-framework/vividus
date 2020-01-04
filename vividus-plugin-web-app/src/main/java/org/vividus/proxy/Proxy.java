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

import java.util.ArrayList;
import java.util.List;

import com.browserup.bup.BrowserUpProxy;
import com.browserup.bup.BrowserUpProxyServer;
import com.browserup.bup.filters.RequestFilter;
import com.browserup.bup.filters.RequestFilterAdapter.FilterSource;

import org.littleshoot.proxy.HttpFiltersSource;

public class Proxy implements IProxy
{
    private static final String PROXY_NOT_STARTED = "Proxy is not started";

    private IProxyServerFactory proxyServerFactory;
    private BrowserUpProxy proxyServer;

    @Override
    public void start()
    {
        if (!isStarted())
        {
            proxyServer = proxyServerFactory.createProxyServer();
            proxyServer.start();
        }
    }

    @Override
    public void startRecording()
    {
        if (isStarted())
        {
            proxyServer.newHar();
        }
        else
        {
            throw new IllegalStateException(PROXY_NOT_STARTED);
        }
    }

    @Override
    public void stopRecording()
    {
        if (isStarted())
        {
            if (proxyServer.getHar() != null)
            {
                proxyServer.endHar();
            }
        }
        else
        {
            throw new IllegalStateException(PROXY_NOT_STARTED);
        }
    }

    @Override
    public void stop()
    {
        if (isStarted())
        {
            proxyServer.stop();
            proxyServer = null;
        }
    }

    @Override
    public boolean isStarted()
    {
        return proxyServer != null;
    }

    @Override
    public BrowserUpProxy getProxyServer()
    {
        return proxyServer;
    }

    @Override
    public ProxyLog getLog()
    {
        if (isStarted())
        {
            return new ProxyLog(proxyServer.getHar());
        }
        throw new IllegalStateException(PROXY_NOT_STARTED);
    }

    @Override
    public void addRequestFilter(RequestFilter requestFilter)
    {
        proxyServer.addRequestFilter(requestFilter);
    }

    @Override
    public void clearRequestFilters()
    {
        List<HttpFiltersSource> toRemove = new ArrayList<>();
        for (HttpFiltersSource source : ((BrowserUpProxyServer) proxyServer).getFilterFactories())
        {
            if (source instanceof FilterSource)
            {
                toRemove.add(source);
            }
        }
        ((BrowserUpProxyServer) proxyServer).getFilterFactories().removeAll(toRemove);
    }

    public void setProxyServerFactory(IProxyServerFactory proxyServerFactory)
    {
        this.proxyServerFactory = proxyServerFactory;
    }
}
