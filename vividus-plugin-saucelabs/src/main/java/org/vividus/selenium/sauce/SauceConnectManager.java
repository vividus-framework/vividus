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

package org.vividus.selenium.sauce;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.saucelabs.ci.sauceconnect.SauceTunnelManager;

import org.vividus.testcontext.TestContext;

public class SauceConnectManager implements ISauceConnectManager
{
    private static final Object KEY = SauceConnectDescriptor.class;

    private SauceTunnelManager sauceTunnelManager;
    private String sauceLabsUsername;
    private String sauceLabsAccessKey;

    private final Map<SauceConnectOptions, SauceConnectDescriptor> activeConnections = new HashMap<>();
    private TestContext testContext;

    @Override
    public void start(SauceConnectOptions sauceConnectOptions)
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
                    sauceTunnelManager.openConnection(sauceLabsUsername, sauceLabsAccessKey,
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
    public String getTunnelId()
    {
        return getSauceConnectDescriptor().getTunnelId();
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

    public void setSauceTunnelManager(SauceTunnelManager sauceTunnelManager)
    {
        this.sauceTunnelManager = sauceTunnelManager;
    }

    public void setSauceLabsUsername(String sauceLabsUsername)
    {
        this.sauceLabsUsername = sauceLabsUsername;
    }

    public void setSauceLabsAccessKey(String sauceLabsAccessKey)
    {
        this.sauceLabsAccessKey = sauceLabsAccessKey;
    }

    public void setTestContext(TestContext testContext)
    {
        this.testContext = testContext;
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
        private final String tunnelId;
        private final int port;
        private final String options;

        SauceConnectDescriptor(SauceConnectOptions sauceConnectOptions) throws IOException
        {
            tunnelId = UUID.randomUUID().toString();
            options = sauceConnectOptions.build(tunnelId);
            port = getFreePort();
        }

        String getTunnelId()
        {
            return tunnelId;
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
