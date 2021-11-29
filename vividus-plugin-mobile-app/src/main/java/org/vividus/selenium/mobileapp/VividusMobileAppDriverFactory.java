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

package org.vividus.selenium.mobileapp;

import java.util.Optional;
import java.util.Set;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.vividus.context.RunContext;
import org.vividus.proxy.IProxy;
import org.vividus.selenium.AbstractVividusWebDriverFactory;
import org.vividus.selenium.DesiredCapabilitiesConfigurer;
import org.vividus.selenium.IGenericWebDriverFactory;
import org.vividus.selenium.manager.IWebDriverManagerContext;

public class VividusMobileAppDriverFactory extends AbstractVividusWebDriverFactory
{
    private final IGenericWebDriverFactory driverFactory;

    public VividusMobileAppDriverFactory(IWebDriverManagerContext webDriverManagerContext, RunContext runContext,
            IProxy proxy, Optional<Set<DesiredCapabilitiesConfigurer>> desiredCapabilitiesConfigurers,
            IGenericWebDriverFactory driverFactory)
    {
        super(true, webDriverManagerContext, runContext, proxy, desiredCapabilitiesConfigurers);
        this.driverFactory = driverFactory;
    }

    @Override
    protected WebDriver createWebDriver(DesiredCapabilities desiredCapabilities)
    {
        return driverFactory.getRemoteWebDriver(desiredCapabilities);
    }
}
