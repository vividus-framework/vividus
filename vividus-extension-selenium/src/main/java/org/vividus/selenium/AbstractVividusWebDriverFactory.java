/*
 * Copyright 2019-2023 the original author or authors.
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

package org.vividus.selenium;

import static org.vividus.selenium.DesiredCapabilitiesMerger.merge;

import java.util.Optional;
import java.util.Set;

import org.jbehave.core.model.Meta;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.vividus.context.RunContext;
import org.vividus.model.RunningScenario;
import org.vividus.model.RunningStory;
import org.vividus.proxy.IProxy;

public abstract class AbstractVividusWebDriverFactory implements IVividusWebDriverFactory
{
    private final boolean remoteExecution;
    private final WebDriverStartContext webDriverStartContext;
    private final RunContext runContext;
    private final IProxy proxy;
    private final Optional<Set<DesiredCapabilitiesConfigurer>> desiredCapabilitiesConfigurers;

    protected AbstractVividusWebDriverFactory(boolean remoteExecution, WebDriverStartContext webDriverStartContext,
            RunContext runContext, IProxy proxy,
            Optional<Set<DesiredCapabilitiesConfigurer>> desiredCapabilitiesConfigurers)
    {
        this.remoteExecution = remoteExecution;
        this.webDriverStartContext = webDriverStartContext;
        this.runContext = runContext;
        this.proxy = proxy;
        this.desiredCapabilitiesConfigurers = desiredCapabilitiesConfigurers;
    }

    @Override
    public VividusWebDriver create()
    {
        VividusWebDriver vividusWebDriver = new VividusWebDriver();
        vividusWebDriver.setWebDriver(createWebDriver(getDesiredCapabilities()));
        vividusWebDriver.setRemote(remoteExecution);
        return vividusWebDriver;
    }

    private DesiredCapabilities getDesiredCapabilities()
    {
        DesiredCapabilities desiredCapabilities = new DesiredCapabilities();

        if (proxy.isStarted())
        {
            desiredCapabilities.setCapability(CapabilityType.PROXY, proxy.createSeleniumProxy());
        }

        desiredCapabilitiesConfigurers.ifPresent(
            configurers -> configurers.forEach(configurer -> configurer.configure(desiredCapabilities)));

        DesiredCapabilities mergedCapabilities = merge(desiredCapabilities,
                webDriverStartContext.get(WebDriverStartParameters.DESIRED_CAPABILITIES));

        RunningStory runningStory = runContext.getRunningStory();
        if (runningStory != null)
        {
            Meta storyMeta = runningStory.getStory().getMeta();
            Meta mergedMeta = Optional.ofNullable(runningStory.getRunningScenario())
                                        .map(RunningScenario::getScenario)
                                        .map(scenario -> scenario.getMeta().inheritFrom(storyMeta))
                                        .orElse(storyMeta);
            ControllingMetaTag.setDesiredCapabilitiesFromMeta(mergedCapabilities, mergedMeta);
        }

        return mergedCapabilities;
    }

    protected abstract WebDriver createWebDriver(DesiredCapabilities desiredCapabilities);

    protected boolean isRemoteExecution()
    {
        return remoteExecution;
    }
}
