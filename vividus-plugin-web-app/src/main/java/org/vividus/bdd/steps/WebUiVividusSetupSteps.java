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

package org.vividus.bdd.steps;

import javax.inject.Inject;

import org.jbehave.core.annotations.AfterScenario;
import org.jbehave.core.annotations.BeforeScenario;
import org.jbehave.core.annotations.ScenarioType;
import org.vividus.selenium.IBrowserWindowSizeProvider;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.manager.IWebDriverManager;
import org.vividus.ui.web.action.IWindowsActions;

public class WebUiVividusSetupSteps extends VividusWebDriverSetupSteps
{
    @Inject private IWindowsActions windowsActions;
    @Inject private IBrowserWindowSizeProvider browserWindowSizeProvider;
    @Inject private IWebDriverManager webDriverManager;
    private WindowsStrategy windowsStrategy;

    @Override
    @BeforeScenario(uponType = ScenarioType.ANY)
    public void beforeScenario()
    {
        super.beforeScenario();
        IWebDriverProvider webDriverProvider = getWebDriverProvider();
        if (webDriverProvider.isWebDriverInitialized())
        {
            webDriverManager
                    .resize(browserWindowSizeProvider.getBrowserWindowSize(webDriverProvider.isRemoteExecution()));
        }
    }

    @AfterScenario(uponType = ScenarioType.ANY)
    public void afterScenario()
    {
        if (getWebDriverProvider().isWebDriverInitialized())
        {
            windowsStrategy.apply(windowsActions);
        }
    }

    public void setWindowsStrategy(WindowsStrategy windowsStrategy)
    {
        this.windowsStrategy = windowsStrategy;
    }
}
