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

package org.vividus.selenium;

import java.util.Optional;
import java.util.Set;

import org.jbehave.core.model.Meta;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.vividus.bdd.context.IBddRunContext;
import org.vividus.bdd.model.MetaWrapper;
import org.vividus.bdd.model.RunningScenario;
import org.vividus.bdd.model.RunningStory;
import org.vividus.selenium.manager.IWebDriverManagerContext;
import org.vividus.selenium.manager.WebDriverManagerParameter;

public abstract class AbstractVividusWebDriverFactory implements IVividusWebDriverFactory
{
    private final boolean remoteExecution;
    private final IWebDriverManagerContext webDriverManagerContext;
    private final IBddRunContext bddRunContext;
    private final Set<DesiredCapabilitiesConfigurer> desiredCapabilitiesConfigurers;

    public AbstractVividusWebDriverFactory(boolean remoteExecution, IWebDriverManagerContext webDriverManagerContext,
            IBddRunContext bddRunContext, Set<DesiredCapabilitiesConfigurer> desiredCapabilitiesConfigurers)
    {
        this.remoteExecution = remoteExecution;
        this.webDriverManagerContext = webDriverManagerContext;
        this.bddRunContext = bddRunContext;
        this.desiredCapabilitiesConfigurers = desiredCapabilitiesConfigurers;
    }

    @Override
    public VividusWebDriver create()
    {
        VividusWebDriver vividusWebDriver = new VividusWebDriver();
        setDesiredCapabilities(vividusWebDriver.getDesiredCapabilities());
        vividusWebDriver.setWebDriver(createWebDriver(vividusWebDriver.getDesiredCapabilities()));
        vividusWebDriver.setRemote(remoteExecution);
        return vividusWebDriver;
    }

    protected void setDesiredCapabilities(DesiredCapabilities desiredCapabilities)
    {
        desiredCapabilitiesConfigurers.forEach(configurer -> configurer.addCapabilities(desiredCapabilities));
        desiredCapabilities.merge(webDriverManagerContext.getParameter(WebDriverManagerParameter.DESIRED_CAPABILITIES));
        webDriverManagerContext.reset(WebDriverManagerParameter.DESIRED_CAPABILITIES);
        RunningStory runningStory = bddRunContext.getRunningStory();
        if (runningStory != null)
        {
            Meta storyMeta = runningStory.getStory().getMeta();
            Meta mergedMeta = Optional.ofNullable(runningStory.getRunningScenario())
                                        .map(RunningScenario::getScenario)
                                        .map(scenario -> scenario.getMeta().inheritFrom(storyMeta))
                                        .orElse(storyMeta);
            MetaWrapper metaWrapper = new MetaWrapper(mergedMeta);
            ControllingMetaTag.setDesiredCapabilitiesFromMeta(desiredCapabilities, metaWrapper);
        }
    }

    protected abstract WebDriver createWebDriver(DesiredCapabilities desiredCapabilities);

    protected boolean isRemoteExecution()
    {
        return remoteExecution;
    }
}
