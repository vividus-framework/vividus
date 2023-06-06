/*
 * Copyright 2019-2023 the original author or authors.
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

package org.vividus.selenium.sauce;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.saucelabs.ci.sauceconnect.SauceTunnelManager;
import com.saucelabs.saucerest.DataCenter;

import org.vividus.selenium.tunnel.TunnelManager;
import org.vividus.testcontext.TestContext;

public class SauceConnectManager implements TunnelManager<SauceConnectOptions>
{
    private static final Object KEY = SauceConnectDescriptor.class;

    private final String sauceLabsUsername;
    private final String sauceLabsAccessKey;
    private final DataCenter sauceLabsDataCenter;

    private final SauceTunnelManager sauceTunnelManager;
    private final TestContext testContext;

    private final Map<SauceConnectOptions, SauceConnectDescriptor> activeConnections = new HashMap<>();

    public SauceConnectManager(String sauceLabsUsername, String sauceLabsAccessKey, DataCenter sauceLabsDataCenter,
            SauceTunnelManager sauceTunnelManager, TestContext testContext)
    {
        this.sauceLabsUsername = sauceLabsUsername;
        this.sauceLabsAccessKey = sauceLabsAccessKey;
        this.sauceLabsDataCenter = sauceLabsDataCenter;
        this.sauceTunnelManager = sauceTunnelManager;
        this.testContext = testContext;
    }

    @Override
    public String start(SauceConnectOptions sauceConnectOptions)
    {
        SauceConnectDescriptor sauceConnectDescriptor = activeConnections.get(sauceConnectOptions);
        SauceConnectDescriptor currentConnectionDescriptor = getSauceConnectDescriptor();
        if (currentConnectionDescriptor == null)
        {
            try
            {
                if (sauceConnectDescriptor == null)
                {
                    synchronized (activeConnections)
                    {
                        sauceConnectDescriptor = activeConnections.get(sauceConnectOptions);
                        if (sauceConnectDescriptor == null)
                        {
                            sauceConnectDescriptor = new SauceConnectDescriptor(sauceConnectOptions);
                            activeConnections.put(sauceConnectOptions, sauceConnectDescriptor);
                        }
                    }
                }
                synchronized (sauceTunnelManager)
                {
                    sauceTunnelManager.openConnection(sauceLabsUsername, sauceLabsAccessKey, sauceLabsDataCenter,
                            sauceConnectDescriptor.getPort(), null, sauceConnectDescriptor.getOptions(), null,
                            Boolean.TRUE, null);
                }
            }
            catch (IOException e)
            {
                throw new IllegalStateException(e);
            }
            putSauceConnectDescriptor(sauceConnectDescriptor);
        }
        else if (!currentConnectionDescriptor.equals(sauceConnectDescriptor))
        {
            throw new IllegalArgumentException("Only one SauceConnect tunnel is allowed within one thread");
        }
        return sauceConnectDescriptor.getTunnelName();
    }

    @Override
    public void stop()
    {
        if (isStarted())
        {
            synchronized (activeConnections)
            {
                if (isStarted())
                {
                    closeTunnelsForPlan(getSauceConnectDescriptor());
                    putSauceConnectDescriptor(null);
                }
            }
        }
    }

    @Override
    public boolean isStarted()
    {
        return activeConnections.containsValue(getSauceConnectDescriptor());
    }

    private void closeTunnelsForPlan(SauceConnectDescriptor descriptor)
    {
        synchronized (sauceTunnelManager)
        {
            sauceTunnelManager.closeTunnelsForPlan(sauceLabsUsername, descriptor.getOptions(), null);
        }
    }

    public SauceConnectDescriptor getSauceConnectDescriptor()
    {
        return testContext.get(KEY);
    }

    private void putSauceConnectDescriptor(SauceConnectDescriptor sauceConnectDescriptor)
    {
        testContext.put(KEY, sauceConnectDescriptor);
    }

    class SauceConnectDescriptor
    {
        private final String tunnelName;
        private final int port;
        private final String options;

        SauceConnectDescriptor(SauceConnectOptions sauceConnectOptions) throws IOException
        {
            tunnelName = UUID.randomUUID().toString();
            options = sauceConnectOptions.build(tunnelName);
            port = getFreePort();
        }

        String getTunnelName()
        {
            return tunnelName;
        }

        int getPort()
        {
            return port;
        }

        String getOptions()
        {
            return options;
        }

        private int getFreePort() throws IOException
        {
            try (ServerSocket socket = new ServerSocket(0))
            {
                return socket.getLocalPort();
            }
        }
    }
}
