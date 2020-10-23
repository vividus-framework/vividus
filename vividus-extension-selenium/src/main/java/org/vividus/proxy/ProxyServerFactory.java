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

import java.util.Set;

import com.browserup.bup.BrowserUpProxyServer;
import com.browserup.bup.proxy.CaptureType;
import com.browserup.bup.proxy.dns.AdvancedHostResolver;

import org.littleshoot.proxy.impl.ThreadPoolConfiguration;
import org.vividus.proxy.mitm.IMitmManagerFactory;
import org.vividus.proxy.mitm.MitmManagerOptions;

public class ProxyServerFactory implements IProxyServerFactory
{
    private static final int PROXY_WORKER_THREADS = 16;
    private boolean trustAllServers;
    private boolean mitmEnabled;
    private MitmManagerOptions mitmManagerOptions;
    private IMitmManagerFactory mitmManagerFactory;
    private AdvancedHostResolver advancedHostResolver;
    private Set<CaptureType> captureTypes;

    @Override
    public BrowserUpProxyServer createProxyServer()
    {
        BrowserUpProxyServer proxyServer = new BrowserUpProxyServer();
        proxyServer.setHostNameResolver(advancedHostResolver);
        proxyServer.setTrustAllServers(trustAllServers);
        proxyServer.enableHarCaptureTypes(captureTypes);
        if (mitmEnabled)
        {
            proxyServer.setMitmManager(mitmManagerFactory.createMitmManager(mitmManagerOptions));
        }
        ThreadPoolConfiguration config = new ThreadPoolConfiguration();
        config.withClientToProxyWorkerThreads(PROXY_WORKER_THREADS);
        config.withProxyToServerWorkerThreads(PROXY_WORKER_THREADS);
        proxyServer.setThreadPoolConfiguration(config);
        return proxyServer;
    }

    public void setTrustAllServers(boolean trustAllServers)
    {
        this.trustAllServers = trustAllServers;
    }

    public void setMitmEnabled(boolean mitmEnabled)
    {
        this.mitmEnabled = mitmEnabled;
    }

    public void setMitmManagerOptions(MitmManagerOptions mitmManagerOptions)
    {
        this.mitmManagerOptions = mitmManagerOptions;
    }

    public void setMitmManagerFactory(IMitmManagerFactory mitmManagerFactory)
    {
        this.mitmManagerFactory = mitmManagerFactory;
    }

    public void setAdvancedHostResolver(AdvancedHostResolver advancedHostResolver)
    {
        this.advancedHostResolver = advancedHostResolver;
    }

    public void setCaptureTypes(Set<CaptureType> captureTypes)
    {
        this.captureTypes = captureTypes;
    }
}
