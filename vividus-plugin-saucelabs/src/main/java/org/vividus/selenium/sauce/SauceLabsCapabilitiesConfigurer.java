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

import com.google.common.eventbus.Subscribe;

import org.openqa.selenium.Proxy;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.vividus.bdd.context.IBddRunContext;
import org.vividus.bdd.model.RunningStory;
import org.vividus.selenium.AbstractDesiredCapabilitiesConfigurer;
import org.vividus.selenium.event.WebDriverQuitEvent;

public class SauceLabsCapabilitiesConfigurer extends AbstractDesiredCapabilitiesConfigurer
{
    private static final String SAUCE_OPTIONS = "sauce:options";

    private final IBddRunContext bddRunContext;
    private final SauceConnectManager sauceConnectManager;
    private boolean sauceLabsEnabled;
    private boolean sauceConnectEnabled;
    private String sauceConnectArguments;
    private String restUrl;

    public SauceLabsCapabilitiesConfigurer(IBddRunContext bddRunContext, SauceConnectManager sauceConnectManager)
    {
        this.bddRunContext = bddRunContext;
        this.sauceConnectManager = sauceConnectManager;
    }

    @Subscribe
    public void stopSauceConnect(WebDriverQuitEvent event)
    {
        sauceConnectManager.stop();
    }

    @Override
    public void configure(DesiredCapabilities desiredCapabilities)
    {
        if (sauceLabsEnabled)
        {
            Proxy proxy = (Proxy) desiredCapabilities.getCapability(CapabilityType.PROXY);
            if (sauceConnectEnabled || proxy != null)
            {
                SauceConnectOptions options = createSauceConnectOptions(proxy);
                sauceConnectManager.start(options);
                putNestedCapability(desiredCapabilities, SAUCE_OPTIONS, "tunnelIdentifier",
                        sauceConnectManager.getTunnelId());
                desiredCapabilities.setCapability(CapabilityType.PROXY, (Object) null);
            }
            RunningStory runningStory = bddRunContext.getRunningStory();
            if (runningStory != null)
            {
                putNestedCapability(desiredCapabilities, SAUCE_OPTIONS, "name", runningStory.getName());
            }
        }
    }

    private SauceConnectOptions createSauceConnectOptions(Proxy proxy)
    {
        SauceConnectOptions sauceConnectOptions = new SauceConnectOptions();
        sauceConnectOptions.setCustomArguments(sauceConnectArguments);
        if (proxy != null)
        {
            sauceConnectOptions.setProxy(proxy.getHttpProxy());
        }
        sauceConnectOptions.setRestUrl(restUrl);
        return sauceConnectOptions;
    }

    public void setSauceLabsEnabled(boolean sauceLabsEnabled)
    {
        this.sauceLabsEnabled = sauceLabsEnabled;
    }

    public void setSauceConnectEnabled(boolean sauceConnectEnabled)
    {
        this.sauceConnectEnabled = sauceConnectEnabled;
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
