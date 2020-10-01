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

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.events.EventFiringWebDriver;
import org.openqa.selenium.support.events.WebDriverEventListener;
import org.vividus.bdd.context.IBddRunContext;
import org.vividus.proxy.IProxy;
import org.vividus.selenium.manager.IWebDriverManager;
import org.vividus.selenium.manager.IWebDriverManagerContext;

public class VividusWebDriverFactory extends AbstractVividusWebDriverFactory
{
    private final IWebDriverFactory webDriverFactory;
    private final IWebDriverManager webDriverManager;
    private final IBrowserWindowSizeProvider browserWindowSizeProvider;
    private final IProxy proxy;

    private List<WebDriverEventListener> webDriverEventListeners;

    public VividusWebDriverFactory(boolean remoteExecution, IWebDriverManagerContext webDriverManagerContext,
            IBddRunContext bddRunContext, Optional<Set<DesiredCapabilitiesConfigurer>> desiredCapabilitiesConfigurers,
            IWebDriverFactory webDriverFactory, IWebDriverManager webDriverManager,
            IBrowserWindowSizeProvider browserWindowSizeProvider, IProxy proxy)
    {
        super(remoteExecution, webDriverManagerContext, bddRunContext, desiredCapabilitiesConfigurers);
        this.webDriverFactory = webDriverFactory;
        this.browserWindowSizeProvider = browserWindowSizeProvider;
        this.webDriverManager = webDriverManager;
        this.proxy = proxy;
    }

    @Override
    protected void setDesiredCapabilities(DesiredCapabilities desiredCapabilities)
    {
        if (proxy.isStarted())
        {
            desiredCapabilities.setCapability(CapabilityType.PROXY, proxy.createSeleniumProxy());
            desiredCapabilities.setCapability(CapabilityType.ACCEPT_INSECURE_CERTS, true);
        }
        super.setDesiredCapabilities(desiredCapabilities);
    }

    @Override
    protected WebDriver createWebDriver(DesiredCapabilities desiredCapabilities)
    {
        WebDriver webDriver = isRemoteExecution()
                ? webDriverFactory.getRemoteWebDriver(desiredCapabilities)
                : webDriverFactory.getWebDriver(desiredCapabilities);

        EventFiringWebDriver eventFiringWebDriver = new EventFiringWebDriver(webDriver);
        webDriverEventListeners.forEach(eventFiringWebDriver::register);

        webDriverManager.resize(webDriver, browserWindowSizeProvider.getBrowserWindowSize(isRemoteExecution()));
        return eventFiringWebDriver;
    }

    public void setWebDriverEventListeners(List<WebDriverEventListener> webDriverEventListeners)
    {
        this.webDriverEventListeners = Collections.unmodifiableList(webDriverEventListeners);
    }
}
