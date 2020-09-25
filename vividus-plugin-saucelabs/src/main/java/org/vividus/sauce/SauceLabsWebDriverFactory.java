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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.jbehave.core.model.Scenario;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.vividus.bdd.context.IBddRunContext;
import org.vividus.bdd.model.MetaWrapper;
import org.vividus.bdd.model.RunningStory;
import org.vividus.proxy.IProxy;
import org.vividus.selenium.IBrowserWindowSizeProvider;
import org.vividus.selenium.IWebDriverFactory;
import org.vividus.selenium.ProxyStarter;
import org.vividus.selenium.VividusWebDriver;
import org.vividus.selenium.VividusWebDriverFactory;
import org.vividus.selenium.manager.IWebDriverManager;
import org.vividus.selenium.manager.IWebDriverManagerContext;

public class SauceLabsWebDriverFactory extends VividusWebDriverFactory
{
    private static final String SAUCE_OPTION = "sauce:options";

    private final SauceConnectProducer sauceConnectProducer;
    private boolean sauceConnectEnabled;

    public SauceLabsWebDriverFactory(IWebDriverFactory webDriverFactory, IBddRunContext bddRunContext,
            IWebDriverManagerContext webDriverManagerContext, IProxy proxy,
            IBrowserWindowSizeProvider browserWindowSizeProvider, IWebDriverManager webDriverManager,
            ProxyStarter proxyStarter, SauceConnectProducer sauceConnectProducer)
    {
        super(webDriverFactory, bddRunContext, webDriverManagerContext, proxy, browserWindowSizeProvider,
                webDriverManager, proxyStarter);
        this.sauceConnectProducer = sauceConnectProducer;
    }

    @Override protected void configureProxy(DesiredCapabilities desiredCapabilities)
    {
    }

    @Override
    protected void configureVividusWebDriver(VividusWebDriver vividusWebDriver)
    {
        DesiredCapabilities desiredCapabilities = vividusWebDriver.getDesiredCapabilities();
        if (sauceConnectEnabled)
        {
            ISauceConnectManager sauceConnectManager = sauceConnectProducer.getSauceConnectManager();
            if (!sauceConnectManager.isStarted())
            {
                sauceConnectProducer.startSauceConnect();
            }

            addSauceOption(desiredCapabilities, SauceLabsCapabilityName.TUNNEL_IDENTIFIER,
                    sauceConnectManager.getTunnelId());
        }
        super.configureVividusWebDriver(vividusWebDriver);
    }

    @Override
    protected void setDesiredCapabilities(DesiredCapabilities desiredCapabilities, RunningStory runningStory,
            Scenario scenario, MetaWrapper metaWrapper)
    {
        addSauceOption(desiredCapabilities, SauceLabsCapabilityName.NAME, runningStory.getName());
    }

    /**
     * Add only dynamic sauce:options. Others are situated in profile
     *
     * @param desiredCapabilities - driver's desired capabilities
     * @param key                 - sauce-option key
     * @param value               - sauce-option value
     */
    @SuppressWarnings("unchecked")
    private void addSauceOption(DesiredCapabilities desiredCapabilities,
            SauceLabsCapabilityName key, Object value)
    {
        // @formatter:off
        Optional.ofNullable((HashMap<String, Object>) desiredCapabilities.getCapability(SAUCE_OPTION))
            .ifPresentOrElse(sauceOptions ->
                sauceOptions.put(key.getOptionName(), value),
                () -> desiredCapabilities.setCapability(SAUCE_OPTION, adjustNewSauceOptionsCapability(key, value)));
        // @formatter:on
    }

    private Map<String, Object> adjustNewSauceOptionsCapability(SauceLabsCapabilityName key, Object value)
    {
        Map<String, Object> sauceOptions = new HashMap<>();
        sauceOptions.put(key.getOptionName(), value);
        return sauceOptions;
    }

    public void setSauceConnectEnabled(boolean sauceConnectEnabled)
    {
        this.sauceConnectEnabled = sauceConnectEnabled;
    }
}
