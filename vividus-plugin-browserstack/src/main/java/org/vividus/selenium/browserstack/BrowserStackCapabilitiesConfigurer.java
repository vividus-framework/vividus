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

package org.vividus.selenium.browserstack;

import java.util.Optional;

import org.openqa.selenium.remote.DesiredCapabilities;
import org.vividus.bdd.context.IBddRunContext;
import org.vividus.bdd.model.RunningStory;
import org.vividus.selenium.AbstractDesiredCapabilitiesConfigurer;

public class BrowserStackCapabilitiesConfigurer extends AbstractDesiredCapabilitiesConfigurer
{
    private final IBddRunContext bddRunContext;
    private boolean browserStackEnabled;

    public BrowserStackCapabilitiesConfigurer(IBddRunContext bddRunContext)
    {
        this.bddRunContext = bddRunContext;
    }

    @Override
    public void configure(DesiredCapabilities desiredCapabilities)
    {
        if (browserStackEnabled)
        {
            Optional.ofNullable(bddRunContext.getRootRunningStory())
                    .map(RunningStory::getName)
                    .ifPresent(name -> putNestedCapability(desiredCapabilities, "bstack:options", "sessionName", name));
        }
    }

    public void setBrowserStackEnabled(boolean browserStackEnabled)
    {
        this.browserStackEnabled = browserStackEnabled;
    }
}
