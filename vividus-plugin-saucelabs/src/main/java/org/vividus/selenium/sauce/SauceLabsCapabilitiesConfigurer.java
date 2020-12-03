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

import org.openqa.selenium.remote.DesiredCapabilities;
import org.vividus.bdd.context.IBddRunContext;
import org.vividus.selenium.tunnel.AbstractTunnellingCapabilitiesConfigurer;

public class SauceLabsCapabilitiesConfigurer extends AbstractTunnellingCapabilitiesConfigurer<SauceConnectOptions>
{
    private static final String SAUCE_OPTIONS = "sauce:options";

    private boolean sauceLabsEnabled;
    private String sauceConnectArguments;
    private String restUrl;

    public SauceLabsCapabilitiesConfigurer(IBddRunContext bddRunContext, SauceConnectManager sauceConnectManager)
    {
        super(bddRunContext, sauceConnectManager);
    }

    @Override
    public void configure(DesiredCapabilities desiredCapabilities)
    {
        if (sauceLabsEnabled)
        {
            configureTunnel(desiredCapabilities,
                    tunnelId -> putNestedCapability(desiredCapabilities, SAUCE_OPTIONS, "tunnelIdentifier", tunnelId));

            configureTestName(desiredCapabilities, SAUCE_OPTIONS, "name");
        }
    }

    @Override
    protected SauceConnectOptions createOptions()
    {
        SauceConnectOptions sauceConnectOptions = new SauceConnectOptions();
        sauceConnectOptions.setCustomArguments(sauceConnectArguments);
        sauceConnectOptions.setRestUrl(restUrl);
        return sauceConnectOptions;
    }

    public void setSauceLabsEnabled(boolean sauceLabsEnabled)
    {
        this.sauceLabsEnabled = sauceLabsEnabled;
    }

    public void setRestUrl(String restUrl)
    {
        this.restUrl = restUrl;
    }

    public void setSauceConnectArguments(String sauceConnectArguments)
    {
        this.sauceConnectArguments = sauceConnectArguments;
    }
}
