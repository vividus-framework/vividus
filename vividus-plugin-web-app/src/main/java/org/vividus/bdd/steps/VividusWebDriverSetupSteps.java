/*
 * Copyright 2019 the original author or authors.
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

package org.vividus.bdd.steps;

import javax.inject.Inject;

import org.jbehave.core.annotations.AfterStory;
import org.jbehave.core.annotations.BeforeScenario;
import org.jbehave.core.annotations.BeforeStory;
import org.jbehave.core.annotations.ScenarioType;
import org.jbehave.core.model.Meta;
import org.vividus.bdd.context.IBddRunContext;
import org.vividus.selenium.ControllingMetaTag;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.manager.IWebDriverManagerContext;

public class VividusWebDriverSetupSteps
{
    @Inject private IWebDriverProvider webDriverProvider;
    @Inject private IWebDriverManagerContext webDriverManagerContext;
    @Inject private IBddRunContext bddRunContext;

    @BeforeScenario(uponType = ScenarioType.ANY)
    public void beforeScenario()
    {
        processMeta(bddRunContext.getRunningStory().getRunningScenario().getScenario().getMeta());
    }

    @BeforeStory
    public void beforeStory()
    {
        processMeta(bddRunContext.getRunningStory().getStory().getMeta());
    }

    @AfterStory
    public void afterStory()
    {
        webDriverProvider.end();
        webDriverManagerContext.reset();
    }

    private void processMeta(Meta meta)
    {
        if (ControllingMetaTag.isAnyContainedIn(meta))
        {
            webDriverProvider.end();
        }
    }

    protected IWebDriverProvider getWebDriverProvider()
    {
        return webDriverProvider;
    }

    protected IBddRunContext getBddRunContext()
    {
        return bddRunContext;
    }
}
