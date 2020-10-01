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

import java.util.HashMap;
import java.util.Map;

import com.google.common.eventbus.Subscribe;

import org.openqa.selenium.remote.DesiredCapabilities;
import org.vividus.bdd.context.IBddRunContext;
import org.vividus.bdd.model.MetaWrapper;
import org.vividus.bdd.model.RunningStory;
import org.vividus.proxy.IProxy;
import org.vividus.selenium.DesiredCapabilitiesConfigurer;
import org.vividus.selenium.event.WebDriverQuitEvent;
import org.vividus.ui.web.configuration.WebApplicationConfiguration;

public class SauceLabsCapabilitiesConfigurer implements DesiredCapabilitiesConfigurer
{
    private static final String SAUCE_OPTIONS = "sauce:options";
    private static final int DEFAULT_HTTPS_PORT = 443;
    private static final String SAUCE_CONNECT_META_TAG = "sauceConnect";

    private final WebApplicationConfiguration webApplicationConfiguration;
    private final IBddRunContext bddRunContext;
    private final SauceConnectManager sauceConnectManager;
    private final IProxy proxy;
    private boolean sauceConnectEnabled;
    private String restUrl;

    public SauceLabsCapabilitiesConfigurer(WebApplicationConfiguration webApplicationConfiguration,
            IBddRunContext bddRunContext, SauceConnectManager sauceConnectManager, IProxy proxy)
    {
        this.webApplicationConfiguration = webApplicationConfiguration;
        this.bddRunContext = bddRunContext;
        this.sauceConnectManager = sauceConnectManager;
        this.proxy = proxy;
    }

    @Subscribe
    public void stopSauceConnect(WebDriverQuitEvent event)
    {
        sauceConnectManager.stop();
    }

    @Override
    public void addCapabilities(DesiredCapabilities desiredCapabilities)
    {
        RunningStory runningStory = bddRunContext.getRunningStory();
        if (sauceConnectEnabled || isSauceConnectEnabledInStoryMeta(runningStory))
        {
            if (!sauceConnectManager.isStarted())
            {
                sauceConnectManager.start(createSauceConnectOptions());
            }
            addSauceOption(desiredCapabilities, "tunnelIdentifier", sauceConnectManager.getTunnelId());
        }
        if (runningStory != null)
        {
            addSauceOption(desiredCapabilities, "name", runningStory.getName());
        }
    }

    @SuppressWarnings("unchecked")
    private void addSauceOption(DesiredCapabilities desiredCapabilities, String capabilityName, Object value)
    {
        Map<String, Object> sauceOptions = (Map<String, Object>) desiredCapabilities.getCapability(SAUCE_OPTIONS);
        if (sauceOptions == null)
        {
            sauceOptions = new HashMap<>();
            desiredCapabilities.setCapability(SAUCE_OPTIONS, sauceOptions);
        }
        sauceOptions.put(capabilityName, value);
    }

    private SauceConnectOptions createSauceConnectOptions()
    {
        SauceConnectOptions sauceConnectOptions = new SauceConnectOptions();
        sauceConnectOptions.setHost(webApplicationConfiguration.getHost());
        if ("https".equals(webApplicationConfiguration.getMainApplicationPageUrl().getScheme()))
        {
            sauceConnectOptions.setPort(DEFAULT_HTTPS_PORT);
        }
        sauceConnectOptions.setBasicAuthUser(webApplicationConfiguration.getBasicAuthUser());
        if (proxy.isStarted())
        {
            sauceConnectOptions.setProxy(proxy.createSeleniumProxy().getHttpProxy());
        }
        sauceConnectOptions.setRestUrl(restUrl);
        return sauceConnectOptions;
    }

    private boolean isSauceConnectEnabledInStoryMeta(RunningStory runningStory)
    {
        return runningStory != null && new MetaWrapper(runningStory.getStory().getMeta()).getOptionalPropertyValue(
                SAUCE_CONNECT_META_TAG).isPresent();
    }

    public void setSauceConnectEnabled(boolean sauceConnectEnabled)
    {
        this.sauceConnectEnabled = sauceConnectEnabled;
    }

    public void setRestUrl(String restUrl)
    {
        this.restUrl = restUrl;
    }
}
