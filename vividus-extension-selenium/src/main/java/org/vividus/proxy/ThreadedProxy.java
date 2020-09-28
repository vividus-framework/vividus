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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import com.browserup.bup.BrowserUpProxy;
import com.browserup.bup.filters.RequestFilter;

import org.apache.commons.lang3.Validate;
import org.openqa.selenium.Proxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.model.IntegerRange;

public class ThreadedProxy implements IProxy
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ThreadedProxy.class);

    private final InetAddress proxyHost;
    private final Queue<Integer> proxyPorts;
    private final boolean useEphemeralPort;
    private final ThreadLocal<IProxy> proxy;

    public ThreadedProxy(String proxyHost, IntegerRange proxyPorts, IProxyFactory proxyFactory)
            throws UnknownHostException
    {
        Set<Integer> proxyPortsRange = proxyPorts.getRange();
        if (proxyPortsRange.contains(0))
        {
            Validate.isTrue(proxyPortsRange.size() == 1,
                    "Port 0 (ephemeral port selection) can not be used with custom ports");
            useEphemeralPort = true;
        }
        else
        {
            useEphemeralPort = false;
            proxyPortsRange.forEach(ThreadedProxy::validatePort);
        }
        this.proxyPorts = new LinkedList<>(proxyPortsRange);
        this.proxy = ThreadLocal.withInitial(proxyFactory::createProxy);
        this.proxyHost = InetAddress.getByName(proxyHost);
    }

    @SuppressWarnings("MagicNumber")
    private static void validatePort(int port)
    {
        Validate.isTrue(port > 0 && port <= 65_535, "Expected ports range is 1-65535 but got: %d", port);
    }

    @Override
    public void start()
    {
        perform(() -> proxy().start(), () ->
        {
            Validate.isTrue(!proxyPorts.isEmpty(), "There are no available ports in the ports pool");
            int port = proxyPorts.poll();
            LOGGER.atInfo().addArgument(port).log("Allocate {} port from the proxy ports pool");
            logAvailablePorts();
            proxy().start(port, proxyHost);
        });
    }

    @Override
    public void start(int port, InetAddress address)
    {
        proxy().start(port, address);
    }

    @Override
    public void startRecording()
    {
        proxy().startRecording();
    }

    @Override
    public void stopRecording()
    {
        proxy().stopRecording();
    }

    @Override
    public void stop()
    {
        perform(() -> proxy().stop(), () ->
        {
            int port = proxy().getProxyServer().getPort();
            proxyPorts.add(port);
            LOGGER.atInfo().addArgument(port).log("Return {} port back to the proxy ports pool");
            logAvailablePorts();
            proxy().stop();
        });
    }

    private void perform(Runnable ephemeralPortsAction, Runnable customPortsAction)
    {
        if (useEphemeralPort)
        {
            ephemeralPortsAction.run();
            return;
        }
        synchronized (proxyPorts)
        {
            customPortsAction.run();
        }
    }

    @Override
    public boolean isStarted()
    {
        return proxy().isStarted();
    }

    @Override
    public BrowserUpProxy getProxyServer()
    {
        return proxy().getProxyServer();
    }

    @Override
    public ProxyLog getLog()
    {
        return proxy().getLog();
    }

    @Override
    public void addRequestFilter(RequestFilter requestFilter)
    {
        proxy().addRequestFilter(requestFilter);
    }

    @Override
    public void clearRequestFilters()
    {
        proxy().clearRequestFilters();
    }

    @Override
    public Proxy createSeleniumProxy()
    {
        return proxy().createSeleniumProxy();
    }

    private void logAvailablePorts()
    {
        LOGGER.atInfo().addArgument(proxyPorts::toString).log("Available ports for proxies in the pool: {}");
    }

    private IProxy proxy()
    {
        return proxy.get();
    }
}
