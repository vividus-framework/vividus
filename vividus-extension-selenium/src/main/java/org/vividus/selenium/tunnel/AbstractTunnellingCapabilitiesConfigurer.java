/*
 * Copyright 2019-2022 the original author or authors.
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

package org.vividus.selenium.tunnel;

import java.util.function.Consumer;

import com.google.common.eventbus.Subscribe;

import org.openqa.selenium.Proxy;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.vividus.context.RunContext;
import org.vividus.selenium.AbstractDesiredCapabilitiesConfigurer;
import org.vividus.selenium.event.AfterWebDriverQuitEvent;

public abstract class AbstractTunnellingCapabilitiesConfigurer<T extends TunnelOptions>
        extends AbstractDesiredCapabilitiesConfigurer
{
    private final TunnelManager<T> tunnelManager;

    private boolean tunnellingEnabled;

    protected AbstractTunnellingCapabilitiesConfigurer(RunContext runContext, TunnelManager<T> tunnelManager)
    {
        super(runContext);
        this.tunnelManager = tunnelManager;
    }

    protected void configureTunnel(DesiredCapabilities desiredCapabilities, Consumer<String> tunnelConsumer)
    {
        Proxy proxy = (Proxy) desiredCapabilities.getCapability(CapabilityType.PROXY);
        if (tunnellingEnabled || proxy != null)
        {
            T options = createOptions();
            if (proxy != null)
            {
                options.setProxy(proxy.getHttpProxy());
            }

            try
            {
                String tunnel = tunnelManager.start(options);
                desiredCapabilities.setCapability(CapabilityType.PROXY, (Object) null);

                tunnelConsumer.accept(tunnel);
            }
            catch (TunnelException e)
            {
                throw new IllegalStateException(e);
            }
        }
    }

    protected abstract T createOptions();

    @Subscribe
    public void stopTunnel(AfterWebDriverQuitEvent event) throws TunnelException
    {
        tunnelManager.stop();
    }

    public void setTunnellingEnabled(boolean tunnellingEnabled)
    {
        this.tunnellingEnabled = tunnellingEnabled;
    }
}
