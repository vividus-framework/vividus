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

import org.jbehave.core.annotations.AfterScenario;
import org.jbehave.core.annotations.ScenarioType;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.ui.web.action.IWindowsActions;

public class WebUiVividusSetupSteps
{
    private final IWindowsActions windowsActions;
    private final IWebDriverProvider webDriverProvider;
    private WindowsStrategy windowsStrategy;

    public WebUiVividusSetupSteps(IWebDriverProvider webDriverProvider, IWindowsActions windowsActions)
    {
        this.webDriverProvider = webDriverProvider;
        this.windowsActions = windowsActions;
    }

    @AfterScenario(uponType = ScenarioType.ANY)
    public void afterScenario()
    {
        if (webDriverProvider.isWebDriverInitialized())
        {
            windowsStrategy.apply(windowsActions);
        }
    }

    public void setWindowsStrategy(WindowsStrategy windowsStrategy)
    {
        this.windowsStrategy = windowsStrategy;
    }
}
