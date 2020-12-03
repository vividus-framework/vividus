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

import org.openqa.selenium.remote.DesiredCapabilities;
import org.vividus.bdd.context.IBddRunContext;
import org.vividus.selenium.tunnel.AbstractTunnellingCapabilitiesConfigurer;
import org.vividus.selenium.tunnel.TunnelOptions;

public class BrowserStackCapabilitiesConfigurer
        extends AbstractTunnellingCapabilitiesConfigurer<TunnelOptions>
{
    private static final String BSTACK_OPTIONS = "bstack:options";

    private boolean browserStackEnabled;

    public BrowserStackCapabilitiesConfigurer(IBddRunContext bddRunContext,
            BrowserStackLocalManager browserStackLocalManager)
    {
        super(bddRunContext, browserStackLocalManager);
    }

    @Override
    public void configure(DesiredCapabilities desiredCapabilities)
    {
        if (browserStackEnabled)
        {
            configureTunnel(desiredCapabilities, tunnelId ->
            {
                putNestedCapability(desiredCapabilities, BSTACK_OPTIONS, "local", true);
                putNestedCapability(desiredCapabilities, BSTACK_OPTIONS, "localIdentifier", tunnelId);
            });

            configureTestName(desiredCapabilities, BSTACK_OPTIONS, "sessionName");
        }
    }

    @Override
    protected TunnelOptions createOptions()
    {
        return new TunnelOptions();
    }

    public void setBrowserStackEnabled(boolean browserStackEnabled)
    {
        this.browserStackEnabled = browserStackEnabled;
    }
}
