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

package org.vividus.selenium.browserstack;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import com.browserstack.local.Local;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.selenium.tunnel.TunnelException;
import org.vividus.selenium.tunnel.TunnelManager;
import org.vividus.selenium.tunnel.TunnelOptions;
import org.vividus.testcontext.TestContext;
import org.vividus.util.ResourceUtils;

public class BrowserStackLocalManager implements TunnelManager<TunnelOptions>
{
    private static final Logger LOGGER = LoggerFactory.getLogger(BrowserStackLocalManager.class);
    private static final Object KEY = BrowserStackLocalConnection.class;

    private final String browserStackAccessKey;
    private final TestContext testContext;

    private final Map<TunnelOptions, BrowserStackLocalConnection> activeConnections = new HashMap<>();

    public BrowserStackLocalManager(String browserStackAccessKey, TestContext testContext)
    {
        this.browserStackAccessKey = browserStackAccessKey;
        this.testContext = testContext;
    }

    @Override
    public String start(TunnelOptions options) throws TunnelException
    {
        BrowserStackLocalConnection connection = getCurrentConnection();
        if (connection == null)
        {
            try
            {
                connection = activeConnections.get(options);
                if (connection == null)
                {
                    synchronized (activeConnections)
                    {
                        connection = activeConnections.get(options);
                        if (connection == null)
                        {
                            connection = new BrowserStackLocalConnection(options);
                            connection.startConnection();
                            activeConnections.put(options, connection);
                        }
                    }
                }
                putCurrentConnection(connection);
            }
            catch (Exception e)
            {
                throw new TunnelException(e);
            }
        }
        return connection.getLocalIdentifier();
    }

    @Override
    public void stop() throws TunnelException
    {
        if (isStarted())
        {
            synchronized (activeConnections)
            {
                if (isStarted())
                {
                    BrowserStackLocalConnection connection = getCurrentConnection();
                    testContext.remove(KEY);
                    if (connection.decrementSessionCount() == 0)
                    {
                        try
                        {
                            activeConnections.values().remove(connection);
                            connection.stopConnection();
                        }
                        catch (Exception e)
                        {
                            throw new TunnelException(e);
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean isStarted()
    {
        return activeConnections.containsValue(getCurrentConnection());
    }

    private BrowserStackLocalConnection getCurrentConnection()
    {
        return testContext.get(KEY);
    }

    private void putCurrentConnection(BrowserStackLocalConnection connection)
    {
        connection.incrementSessionCount();
        testContext.put(KEY, connection);
    }

    private class BrowserStackLocalConnection
    {
        private static final String PAC_FORMAT = "function FindProxyForURL(url, host) "
                + "{ if (shExpMatch(host, \"*.browserstack.com\")) { return \"DIRECT\"; }return \"PROXY %s\"; }";

        private final String localIdentifier;
        private final AtomicInteger sessionCount;
        private final Map<String, String> localParameters;
        private final Local local;

        BrowserStackLocalConnection(TunnelOptions options) throws IOException
        {
            this.localIdentifier = UUID.randomUUID().toString();
            this.sessionCount = new AtomicInteger(0);

            Map<String, String> parameters = new HashMap<>();
            parameters.put("localIdentifier", localIdentifier);
            parameters.put("forcelocal", "true");
            parameters.put("key", browserStackAccessKey);

            if (options.getProxy() != null)
            {
                String pac = ResourceUtils.createTempFile("pac-browserstack-" + localIdentifier, ".js",
                        String.format(PAC_FORMAT, options.getProxy())).toString();
                parameters.put("-pac-file", pac);
            }

            this.localParameters = parameters;
            this.local = new Local();
        }

        String getLocalIdentifier()
        {
            return localIdentifier;
        }

        void startConnection() throws Exception
        {
            LOGGER.atInfo().addArgument(localIdentifier)
                           .log("Starting BrowserStack Local connection with {} identifier");
            local.start(localParameters);
        }

        void stopConnection() throws Exception
        {
            LOGGER.atInfo().addArgument(localIdentifier)
                           .log("Stopping BrowserStack Local connection with {} identifier");
            local.stop(localParameters);
        }

        void incrementSessionCount()
        {
            sessionCount.incrementAndGet();
        }

        int decrementSessionCount()
        {
            return sessionCount.decrementAndGet();
        }
    }
}
