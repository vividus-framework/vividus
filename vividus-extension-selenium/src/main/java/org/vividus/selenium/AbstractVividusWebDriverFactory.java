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

import org.jbehave.core.model.Meta;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Story;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.vividus.bdd.context.IBddRunContext;
import org.vividus.bdd.model.MetaWrapper;
import org.vividus.bdd.model.RunningScenario;
import org.vividus.bdd.model.RunningStory;
import org.vividus.selenium.manager.IWebDriverManagerContext;
import org.vividus.selenium.manager.WebDriverManagerParameter;

public abstract class AbstractVividusWebDriverFactory implements IVividusWebDriverFactory
{
    private final IBddRunContext bddRunContext;
    private final IWebDriverManagerContext webDriverManagerContext;

    public AbstractVividusWebDriverFactory(IBddRunContext bddRunContext,
            IWebDriverManagerContext webDriverManagerContext)
    {
        this.bddRunContext = bddRunContext;
        this.webDriverManagerContext = webDriverManagerContext;
    }

    @Override
    public VividusWebDriver create()
    {
        VividusWebDriver vividusWebDriver = createVividusWebDriver(bddRunContext.getRunningStory());
        configureVividusWebDriver(vividusWebDriver);
        return vividusWebDriver;
    }

    protected abstract void configureVividusWebDriver(VividusWebDriver vividusWebDriver);

    private VividusWebDriver createVividusWebDriver(RunningStory runningStory)
    {
        VividusWebDriver vividusWebDriver = new VividusWebDriver();
        setBaseDesiredCapabilities(vividusWebDriver, runningStory);

        return vividusWebDriver;
    }

    private void setBaseDesiredCapabilities(VividusWebDriver vividusWebDriver, RunningStory runningStory)
    {
        DesiredCapabilities desiredCapabilities = vividusWebDriver.getDesiredCapabilities();
        desiredCapabilities.merge(webDriverManagerContext.getParameter(
                WebDriverManagerParameter.DESIRED_CAPABILITIES));
        webDriverManagerContext.reset(WebDriverManagerParameter.DESIRED_CAPABILITIES);
        if (runningStory != null)
        {
            Scenario scenario = Optional.ofNullable(runningStory.getRunningScenario())
                                        .map(RunningScenario::getScenario)
                                        .orElse(null);
            Meta mergedMeta = mergeMeta(runningStory.getStory(), scenario);
            MetaWrapper metaWrapper = new MetaWrapper(mergedMeta);
            ControllingMetaTag.setDesiredCapabilitiesFromMeta(desiredCapabilities, metaWrapper);
            setDesiredCapabilities(desiredCapabilities, runningStory, scenario, metaWrapper);
        }
    }

    protected abstract void setDesiredCapabilities(DesiredCapabilities desiredCapabilities, RunningStory runningStory,
            Scenario scenario, MetaWrapper metaWrapper);

    private static Meta mergeMeta(Story story, Scenario scenario)
    {
        Meta storyMeta = story.getMeta();
        return scenario == null ? storyMeta : scenario.getMeta().inheritFrom(storyMeta);
    }
}
