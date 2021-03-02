/*
 * Copyright 2019-2021 the original author or authors.
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

import static org.apache.commons.lang3.Validate.isTrue;

import org.jbehave.core.annotations.AfterScenario;
import org.jbehave.core.annotations.AfterStory;
import org.jbehave.core.annotations.BeforeScenario;
import org.jbehave.core.annotations.BeforeStory;
import org.jbehave.core.model.Meta;
import org.vividus.bdd.context.IBddRunContext;
import org.vividus.selenium.ControllingMetaTag;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.manager.IWebDriverManagerContext;

public class WebDriverSetupSteps
{
    private final WebDriverSessionScope webDriverSessionScope;
    private final IWebDriverProvider webDriverProvider;
    private final IWebDriverManagerContext webDriverManagerContext;
    private final IBddRunContext bddRunContext;

    public WebDriverSetupSteps(WebDriverSessionScope webDriverSessionScope, IWebDriverProvider webDriverProvider,
            IWebDriverManagerContext webDriverManagerContext, IBddRunContext bddRunContext)
    {
        isTrue(webDriverSessionScope != null, "Application session scope is not set");
        this.webDriverSessionScope = webDriverSessionScope;
        this.webDriverProvider = webDriverProvider;
        this.webDriverManagerContext = webDriverManagerContext;
        this.bddRunContext = bddRunContext;
    }

    @BeforeScenario
    public void beforeScenario()
    {
        processMeta(bddRunContext.getRunningStory().getRunningScenario().getScenario().getMeta());
    }

    @BeforeStory
    public void beforeStory()
    {
        processMeta(bddRunContext.getRunningStory().getStory().getMeta());
    }

    @AfterScenario(order = Integer.MAX_VALUE)
    public void afterScenario()
    {
        if (webDriverSessionScope == WebDriverSessionScope.SCENARIO)
        {
            stopWebDriver();
        }
    }

    @AfterStory
    public void afterStory()
    {
        if (webDriverSessionScope == WebDriverSessionScope.STORY)
        {
            stopWebDriver();
        }
    }

    private void stopWebDriver()
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
}
