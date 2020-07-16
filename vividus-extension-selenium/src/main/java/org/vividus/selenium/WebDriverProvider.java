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

import java.util.concurrent.ConcurrentLinkedQueue;

import javax.inject.Inject;

import com.google.common.eventbus.EventBus;

import org.openqa.selenium.WebDriver;
import org.vividus.selenium.event.WebDriverCreateEvent;
import org.vividus.selenium.event.WebDriverQuitEvent;
import org.vividus.testcontext.TestContext;

public class WebDriverProvider implements IWebDriverProvider
{
    private IVividusWebDriverFactory vividusWebDriverFactory;
    private final ConcurrentLinkedQueue<WebDriver> webDrivers = new ConcurrentLinkedQueue<>();
    @Inject private EventBus eventBus;
    private TestContext testContext;

    @Override
    public boolean isRemoteExecution()
    {
        return isWebDriverInitialized() && getVividusWebDriver().isRemote();
    }

    private VividusWebDriver getVividusWebDriver()
    {
        return testContext.get(VividusWebDriver.class, VividusWebDriver.class);
    }

    @Override
    public WebDriver get()
    {
        VividusWebDriver vividusWebDriver = testContext.get(VividusWebDriver.class);
        if (vividusWebDriver == null)
        {
            vividusWebDriver = vividusWebDriverFactory.create();
            testContext.put(VividusWebDriver.class, vividusWebDriver);
            WebDriver driver = vividusWebDriver.getWrappedDriver();
            webDrivers.add(driver);
            eventBus.post(new WebDriverCreateEvent(driver));
        }
        return vividusWebDriver.getWrappedDriver();
    }

    @Override
    public <T> T getUnwrapped(Class<T> clazz)
    {
        return WebDriverUtil.unwrap(get(), clazz);
    }

    @Override
    public void end()
    {
        if (isWebDriverInitialized())
        {
            WebDriver webDriver = getVividusWebDriver().getWrappedDriver();
            try
            {
                webDriver.quit();
            }
            finally
            {
                webDrivers.remove(webDriver);
                reset();
            }
        }
        else
        {
            reset();
        }
    }

    private void reset()
    {
        testContext.remove(VividusWebDriver.class);
        eventBus.post(new WebDriverQuitEvent());
    }

    @Override
    public boolean isWebDriverInitialized()
    {
        return null != testContext.get(VividusWebDriver.class);
    }

    public void destroy()
    {
        webDrivers.forEach(WebDriver::quit);
    }

    public void setVividusWebDriverFactory(IVividusWebDriverFactory vividusWebDriverFactory)
    {
        this.vividusWebDriverFactory = vividusWebDriverFactory;
    }

    public void setTestContext(TestContext testContext)
    {
        this.testContext = testContext;
    }
}
