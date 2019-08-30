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

import javax.inject.Inject;

import com.browserup.bup.BrowserUpProxy;
import com.browserup.bup.filters.RequestFilter;

public class ThreadedProxy implements IProxy
{
    @Inject private IProxyServerFactory proxyServerFactory;

    private final ThreadLocal<IProxy> proxy = ThreadLocal.withInitial(() ->
    {
        Proxy proxy = new Proxy();
        proxy.setProxyServerFactory(proxyServerFactory);
        return proxy;
    });

    @Override
    public void start()
    {
        proxy.get().start();
    }

    @Override
    public void startRecording()
    {
        proxy.get().startRecording();
    }

    @Override
    public void stopRecording()
    {
        proxy.get().stopRecording();
    }

    @Override
    public void stop()
    {
        proxy.get().stop();
    }

    @Override
    public boolean isStarted()
    {
        return proxy.get().isStarted();
    }

    @Override
    public BrowserUpProxy getProxyServer()
    {
        return proxy.get().getProxyServer();
    }

    @Override
    public ProxyLog getLog()
    {
        return proxy.get().getLog();
    }

    @Override
    public void addRequestFilter(RequestFilter requestFilter)
    {
        proxy.get().addRequestFilter(requestFilter);
    }

    @Override
    public void clearRequestFilters()
    {
        proxy.get().clearRequestFilters();
    }
}
