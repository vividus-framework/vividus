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

import org.jbehave.core.annotations.AfterStory;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.vividus.bdd.context.IBddRunContext;
import org.vividus.selenium.IWebDriverProvider;

public class SauceLabsSteps
{
    private IWebDriverProvider webDriverProvider;
    private IBddRunContext bddRunContext;

    public SauceLabsSteps(IWebDriverProvider webDriverProvider, IBddRunContext bddRunContext)
    {
        this.webDriverProvider = webDriverProvider;
        this.bddRunContext = bddRunContext;
    }

    @AfterStory
    public void updateStatusAfterStory() throws WebDriverException
    {
        if (webDriverProvider.isWebDriverInitialized())
        {
            String result = isStoryFailed() ? "failed" : "passed";
            webDriverProvider.getUnwrapped(RemoteWebDriver.class).executeScript("sauce:job-result=" + result);
        }
    }

    private boolean isStoryFailed()
    {
        return bddRunContext.getRunningStory().isFailed();
    }
}
