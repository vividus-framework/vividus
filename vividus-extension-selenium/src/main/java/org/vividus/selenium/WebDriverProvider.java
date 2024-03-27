/*
 * Copyright 2019-2024 the original author or authors.
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

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.google.common.eventbus.EventBus;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.vividus.selenium.event.AfterWebDriverQuitEvent;
import org.vividus.selenium.event.BeforeWebDriverQuitEvent;
import org.vividus.selenium.event.WebDriverCreateEvent;
import org.vividus.testcontext.TestContext;

import jakarta.inject.Inject;

public class WebDriverProvider implements IWebDriverProvider
{
    private IVividusWebDriverFactory vividusWebDriverFactory;
    private final Queue<WebDriver> webDrivers = new ConcurrentLinkedQueue<>();
    @Inject private EventBus eventBus;
    private final TestContext testContext;

    public WebDriverProvider(TestContext testContext)
    {
        this.testContext = testContext;
    }

    private WebDriver getWebDriver()
    {
        return testContext.get(WebDriver.class, WebDriver.class);
    }

    @Override
    public WebDriver get()
    {
        WebDriver webDriver = testContext.get(WebDriver.class);
        if (webDriver == null)
        {
            webDriver = vividusWebDriverFactory.createWebDriver();
            testContext.put(WebDriver.class, webDriver);
            webDrivers.add(webDriver);
            eventBus.post(new WebDriverCreateEvent(webDriver));
        }
        return webDriver;
    }

    @Override
    public <T> T getUnwrapped(Class<T> clazz)
    {
        return WebDriverUtils.unwrap(get(), clazz);
    }

    @Override
    public void end()
    {
        if (isWebDriverInitialized())
        {
            WebDriver webDriver = getWebDriver();
            String sessionId = WebDriverUtils.unwrap(webDriver, RemoteWebDriver.class).getSessionId().toString();
            try
            {
                eventBus.post(new BeforeWebDriverQuitEvent(sessionId));
                webDriver.quit();
            }
            finally
            {
                webDrivers.remove(webDriver);
                testContext.remove(WebDriver.class);
                eventBus.post(new AfterWebDriverQuitEvent(sessionId));
            }
        }
    }

    @Override
    public boolean isWebDriverInitialized()
    {
        return null != testContext.get(WebDriver.class);
    }

    public void destroy()
    {
        webDrivers.forEach(WebDriver::quit);
    }

    public void setVividusWebDriverFactory(IVividusWebDriverFactory vividusWebDriverFactory)
    {
        this.vividusWebDriverFactory = vividusWebDriverFactory;
    }
}
