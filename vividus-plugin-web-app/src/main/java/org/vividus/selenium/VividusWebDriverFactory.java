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

import org.jbehave.core.model.Scenario;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.events.EventFiringWebDriver;
import org.openqa.selenium.support.events.WebDriverEventListener;
import org.vividus.bdd.context.IBddRunContext;
import org.vividus.bdd.model.MetaWrapper;
import org.vividus.bdd.model.RunningStory;
import org.vividus.proxy.IProxy;
import org.vividus.selenium.manager.IWebDriverManager;
import org.vividus.selenium.manager.IWebDriverManagerContext;

public class VividusWebDriverFactory extends AbstractVividusWebDriverFactory
{
    private final IWebDriverFactory webDriverFactory;
    private final IBrowserWindowSizeProvider browserWindowSizeProvider;
    private final IWebDriverManager webDriverManager;
    private final IProxy proxy;
    private final ProxyStarter proxyStarter;

    private boolean remoteExecution;
    private List<WebDriverEventListener> webDriverEventListeners;

    public VividusWebDriverFactory(IWebDriverFactory webDriverFactory, IBddRunContext bddRunContext,
        IWebDriverManagerContext webDriverManagerContext, IProxy proxy,
        IBrowserWindowSizeProvider browserWindowSizeProvider, IWebDriverManager webDriverManager,
        ProxyStarter proxyStarter)
    {
        super(bddRunContext, webDriverManagerContext);
        this.proxy = proxy;
        this.webDriverFactory = webDriverFactory;
        this.browserWindowSizeProvider = browserWindowSizeProvider;
        this.webDriverManager = webDriverManager;
        this.proxyStarter = proxyStarter;
    }

    @Override
    protected void configureVividusWebDriver(VividusWebDriver vividusWebDriver)
    {
        DesiredCapabilities desiredCapabilities = vividusWebDriver.getDesiredCapabilities();
        configureProxy(desiredCapabilities);
        WebDriver webDriver = remoteExecution
                ? webDriverFactory.getRemoteWebDriver(desiredCapabilities)
                : webDriverFactory.getWebDriver(desiredCapabilities);

        EventFiringWebDriver eventFiringWebDriver = new EventFiringWebDriver(webDriver);
        webDriverEventListeners.forEach(eventFiringWebDriver::register);

        webDriverManager.resize(webDriver, browserWindowSizeProvider.getBrowserWindowSize(remoteExecution));
        vividusWebDriver.setWebDriver(eventFiringWebDriver);
        vividusWebDriver.setRemote(remoteExecution);
    }

    protected void configureProxy(DesiredCapabilities desiredCapabilities)
    {
        if (proxy.isStarted())
        {
            desiredCapabilities.setCapability(CapabilityType.PROXY, proxyStarter.createSeleniumProxy(remoteExecution));
            desiredCapabilities.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
        }
    }

    @Override
    protected void setDesiredCapabilities(DesiredCapabilities desiredCapabilities, RunningStory runningStory,
            Scenario scenario, MetaWrapper metaWrapper)
    {
        if (remoteExecution)
        {
            desiredCapabilities.setCapability(SauceLabsCapabilityType.NAME, runningStory.getName());
        }
    }

    public void setRemoteExecution(boolean remoteExecution)
    {
        this.remoteExecution = remoteExecution;
    }

    public void setWebDriverEventListeners(List<WebDriverEventListener> webDriverEventListeners)
    {
        this.webDriverEventListeners = Collections.unmodifiableList(webDriverEventListeners);
    }
}
