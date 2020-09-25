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

package org.vividus.sauce;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.saucelabs.ci.sauceconnect.SauceTunnelManager;

import org.vividus.testcontext.TestContext;

public class SauceConnectManager implements ISauceConnectManager
{
    private static final String DESCRIPTOR = "DESCRIPTOR";
    private String sauceLabsUsername;
    private String sauceLabsAccessKey;

    private SauceTunnelManager sauceTunnelManager;

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

    private void closeTunnelsForPlan(SauceConnectDescriptor desciptor)
    {
        synchronized (sauceTunnelManager)
        {
            sauceTunnelManager.closeTunnelsForPlan(sauceLabsUsername, desciptor.getOptions(), null);
        }
    }

    public SauceConnectDescriptor getSauceConnectDescriptor()
    {
        return testContext.get(DESCRIPTOR);
    }

    private void putSauceConnectDescriptor(SauceConnectDescriptor sauceConnectDescriptor)
    {
        testContext.put(DESCRIPTOR, sauceConnectDescriptor);
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

    public void setContext(TestContext testContext)
    {
        this.testContext = testContext;
    }

    private static final class SauceConnectDescriptor
    {
        private final String tunnelId;
        private final int port;
        private String options;

        private SauceConnectDescriptor(SauceConnectOptions sauceConnectOptions) throws IOException
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
