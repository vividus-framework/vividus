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

package org.vividus.bdd;

import javax.inject.Inject;

import com.google.common.eventbus.EventBus;

import org.jbehave.core.model.Meta;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Story;
import org.vividus.bdd.context.IBddRunContext;
import org.vividus.bdd.proxy.ProxyEvent;
import org.vividus.bdd.spring.ExtendedConfiguration;
import org.vividus.proxy.IProxy;
import org.vividus.selenium.ControllingMetaTag;

public class ProxyAgentStoryReporter extends ChainedStoryReporter
{
    private boolean proxyEnabled;
    private boolean proxyRecordingEnabled;

    @Inject private IProxy proxy;
    @Inject private IBddRunContext bddRunContext;
    @Inject private ExtendedConfiguration configuration;
    private EventBus eventBus;

    @Override
    public void beforeStory(Story story, boolean givenStory)
    {
        String name = bddRunContext.getRunningStory().getName();
        if (!configuration.dryRun())
        {
            if (!givenStory && !"BeforeStories".equals(name) && !"AfterStories".equals(name))
            {
                if (proxyEnabled || isProxyEnabledInStoryMeta())
                {
                    startProxy();
                }
            }
        }
        super.beforeStory(story, givenStory);
    }

    @Override
    public void beforeScenario(Scenario scenario)
    {
        if (!configuration.dryRun())
        {
            if (!proxy.isStarted() && isProxyEnabled())
            {
                startProxy();
            }
            if (proxy.isStarted() && isProxyRecordingEnabled())
            {
                proxy.clearRequestFilters();
                proxy.startRecording();
            }
        }
        super.beforeScenario(scenario);
    }

    @Override
    public void afterScenario()
    {
        super.afterScenario();
        if (proxy.isStarted())
        {
            if (isProxyRecordingEnabled())
            {
                proxy.stopRecording();
            }
            if (!proxyEnabled && isProxyEnabledInScenarioMeta())
            {
                stopProxy();
            }
        }
    }

    @Override
    public void afterStory(boolean givenStory)
    {
        super.afterStory(givenStory);
        if (!givenStory)
        {
            if (proxy.isStarted())
            {
                stopProxy();
            }
        }
    }

    public void startProxy()
    {
        proxy.start();
        eventBus.post(new ProxyEvent(proxy));
    }

    public void stopProxy()
    {
        proxy.stop();
        eventBus.post(new ProxyEvent(proxy));
    }

    private boolean isProxyRecordingEnabled()
    {
        return proxyRecordingEnabled || isProxyEnabledInStoryMeta() || isProxyEnabledInScenarioMeta();
    }

    private boolean isProxyEnabled()
    {
        return isProxyEnabledInStoryMeta() || isProxyEnabledInScenarioMeta();
    }

    private boolean isProxyEnabledInScenarioMeta()
    {
        return isProxyMetaTagContainedIn(bddRunContext.getRunningStory().getRunningScenario().getScenario().getMeta());
    }

    private boolean isProxyEnabledInStoryMeta()
    {
        return isProxyMetaTagContainedIn(bddRunContext.getRunningStory().getStory().getMeta());
    }

    private boolean isProxyMetaTagContainedIn(Meta meta)
    {
        return ControllingMetaTag.PROXY.isContainedIn(meta);
    }

    public void setProxyEnabled(boolean proxyEnabled)
    {
        this.proxyEnabled = proxyEnabled;
    }

    public void setProxyRecordingEnabled(boolean proxyRecordingEnabled)
    {
        this.proxyRecordingEnabled = proxyRecordingEnabled;
    }

    public void setEventBus(EventBus eventBus)
    {
        this.eventBus = eventBus;
    }
}
