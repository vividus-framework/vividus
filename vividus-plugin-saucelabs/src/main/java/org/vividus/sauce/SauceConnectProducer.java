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

import com.google.common.eventbus.Subscribe;

import org.jbehave.core.model.Meta;
import org.vividus.bdd.context.IBddRunContext;
import org.vividus.bdd.proxy.ProxyEvent;
import org.vividus.selenium.ControllingMetaTag;
import org.vividus.selenium.VividusWebDriverFactory;
import org.vividus.ui.web.configuration.WebApplicationConfiguration;

public class SauceConnectProducer
{
    private static final int DEFAULT_HTTPS_PORT = 443;
    private static final String SAUCE_CONNECT_META_TAG = "sauceConnect";

    private final WebApplicationConfiguration webApplicationConfiguration;
    private final IBddRunContext bddRunContext;
    private final VividusWebDriverFactory vividusWebDriverFactory;
    private final SauceConnectManager sauceConnectManager;

    private boolean sauceConnectEnabled;

    public SauceConnectProducer(WebApplicationConfiguration webApplicationConfiguration, IBddRunContext bddRunContext,
            VividusWebDriverFactory vividusWebDriverFactory, SauceConnectManager sauceConnectManager)
    {
        this.webApplicationConfiguration = webApplicationConfiguration;
        this.bddRunContext = bddRunContext;
        this.vividusWebDriverFactory = vividusWebDriverFactory;
        this.sauceConnectManager = sauceConnectManager;
    }

    @Subscribe
    public void handleProxyEvent(ProxyEvent event)
    {
        if (event.isProxyStarted())
        {
            startSauceConnect();
        }
        else
        {
            sauceConnectManager.stop();
        }
    }

    private void startSauceConnect()
    {
        if (isSauceConnectEnabled() && !sauceConnectManager.isStarted())
        {
            SauceConnectOptions sauceConnectOptions = new SauceConnectOptions();
            sauceConnectOptions.setHost(webApplicationConfiguration.getHost());
            if (isSecureCommunication())
            {
                sauceConnectOptions.setPort(DEFAULT_HTTPS_PORT);
            }
            sauceConnectOptions.setBasicAuthUser(webApplicationConfiguration.getBasicAuthUser());
            sauceConnectOptions.setProxy(vividusWebDriverFactory.createSeleniumProxy(false).getHttpProxy());
            sauceConnectManager.start(sauceConnectOptions);
        }
    }

    private boolean isSecureCommunication()
    {
        return "https".equals(webApplicationConfiguration.getMainApplicationPageUrl().getScheme());
    }

    private boolean isSauceConnectEnabled()
    {
        return sauceConnectEnabled || isSauceConnectEnabledInMeta();
    }

    private boolean isSauceConnectEnabledInMeta()
    {
        return isSauceConnectEnabledInStoryMeta() || isSauceConnectEnabledInScenarioMeta();
    }

    private boolean isSauceConnectEnabledInStoryMeta()
    {
        return isSauceConnectMetaTagContainedIn(bddRunContext.getRunningStory().getStory().getMeta());
    }

    private boolean isSauceConnectEnabledInScenarioMeta()
    {
        return isSauceConnectMetaTagContainedIn(
                bddRunContext.getRunningStory().getRunningScenario().getScenario().getMeta());
    }

    private boolean isSauceConnectMetaTagContainedIn(Meta meta)
    {
        return ControllingMetaTag.isContainedIn(meta, SAUCE_CONNECT_META_TAG);
    }

    public void setSauceConnectEnabled(boolean sauceConnectEnabled)
    {
        this.sauceConnectEnabled = sauceConnectEnabled;
    }
}
