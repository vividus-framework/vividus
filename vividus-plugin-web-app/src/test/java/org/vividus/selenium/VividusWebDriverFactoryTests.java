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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Properties;

import com.browserup.bup.BrowserUpProxy;
import com.browserup.bup.client.ClientUtil;

import org.jbehave.core.model.Meta;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Story;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.HasCapabilities;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriver.Options;
import org.openqa.selenium.WebDriver.Window;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.events.EventFiringWebDriver;
import org.openqa.selenium.support.events.WebDriverEventListener;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.vividus.bdd.context.IBddRunContext;
import org.vividus.bdd.model.RunningScenario;
import org.vividus.bdd.model.RunningStory;
import org.vividus.proxy.IProxy;
import org.vividus.selenium.manager.IWebDriverManager;
import org.vividus.selenium.manager.IWebDriverManagerContext;
import org.vividus.selenium.manager.WebDriverManagerParameter;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ ClientUtil.class, InetAddress.class, VividusWebDriverFactory.class })
public class VividusWebDriverFactoryTests
{
    private static final String LOOPBACK_ADDRESS = "127.0.0.1:0";
    private static final String FIREFOX = "firefox";
    private static final String TEST = "Test";

    @Mock
    private IBddRunContext bddRunContext;

    @Mock
    private IWebDriverManagerContext webDriverManagerContext;

    @Mock
    private IProxy proxy;

    @Mock
    private IWebDriverFactory webDriverFactory;

    @Mock
    private IBrowserWindowSizeProvider browserWindowSizeProvider;

    @Mock
    private IWebDriverManager webDriverManager;

    @InjectMocks
    private VividusWebDriverFactory vividusWebDriverFactory;

    @Mock(extraInterfaces = HasCapabilities.class)
    private WebDriver driver;

    @Mock
    private BrowserUpProxy browserMobProxy;

    @Mock
    private Proxy seleniumProxy;

    @Mock
    private WebDriverEventListener webDriverEventListener;

    private void runCreateTest(boolean remoteExecution, String browserName) throws Exception
    {
        vividusWebDriverFactory.setRemoteExecution(remoteExecution);
        vividusWebDriverFactory.setWebDriverEventListeners(List.of(webDriverEventListener));

        when(bddRunContext.getRunningStory()).thenReturn(createRunningStory(browserName));
        EventFiringWebDriver eventFiringWebDriver = mock(EventFiringWebDriver.class);
        PowerMockito.whenNew(EventFiringWebDriver.class).withArguments(driver).thenReturn(eventFiringWebDriver);
        Options options = mock(Options.class);
        when(eventFiringWebDriver.manage()).thenReturn(options);
        Window window = mock(Window.class);
        when(options.window()).thenReturn(window);
        when(eventFiringWebDriver.getCapabilities()).thenReturn(mock(Capabilities.class));
        BrowserWindowSize windowSize = new BrowserWindowSize("1920x1080");
        when(browserWindowSizeProvider.getBrowserWindowSize(remoteExecution))
                .thenReturn(windowSize);
        VividusWebDriver vividusWebDriver = vividusWebDriverFactory.create();
        assertEquals(eventFiringWebDriver, vividusWebDriver.getWrappedDriver());
        verify(eventFiringWebDriver).register(webDriverEventListener);
        assertEquals(remoteExecution, vividusWebDriver.isRemote());
        verify(webDriverManagerContext).reset(WebDriverManagerParameter.DESIRED_CAPABILITIES);
        verify(webDriverManager).resize(driver, windowSize);
    }

    private static RunningStory createRunningStory(String browserName)
    {
        RunningScenario runningScenario = new RunningScenario();
        runningScenario.setScenario(createScenario(browserName));

        RunningStory runningStory = new RunningStory();
        runningStory.setRunningScenario(runningScenario);
        runningStory.setStory(new Story(TEST + ".story", null, new Meta(), null, null));
        return runningStory;
    }

    private static Scenario createScenario(String browserName)
    {
        Properties scenarioMetaProperties = new Properties();
        if (browserName != null)
        {
            scenarioMetaProperties.setProperty(CapabilityType.BROWSER_NAME, FIREFOX);
        }
        return new Scenario(TEST, new Meta(scenarioMetaProperties));
    }

    private void mockSeleniumProxyCreation(boolean remote) throws UnknownHostException
    {
        when(proxy.isStarted()).thenReturn(true);
        InetAddress inetAddress = mock(InetAddress.class);
        PowerMockito.mockStatic(InetAddress.class);
        if (remote)
        {
            when(InetAddress.getLocalHost()).thenReturn(inetAddress);
        }
        else
        {
            when(InetAddress.getLoopbackAddress()).thenReturn(inetAddress);
        }
        PowerMockito.mockStatic(ClientUtil.class);
        when(ClientUtil.createSeleniumProxy(browserMobProxy, inetAddress)).thenReturn(seleniumProxy);
    }

    private void verifyProxyCreation(boolean remote) throws UnknownHostException
    {
        PowerMockito.verifyStatic(InetAddress.class);
        if (remote)
        {
            InetAddress.getLocalHost();
        }
        else
        {
            InetAddress.getLoopbackAddress();
        }
    }

    @Test
    public void testCreateWebDriverRemoteWithProxy() throws Exception
    {
        when(proxy.getProxyServer()).thenReturn(browserMobProxy);
        mockSeleniumProxyCreation(true);
        when(seleniumProxy.getHttpProxy()).thenReturn(LOOPBACK_ADDRESS);
        when(webDriverFactory.getRemoteWebDriver(any(DesiredCapabilities.class))).thenReturn(driver);
        runCreateTest(true, null);
        verifyProxyCreation(true);
    }

    @Test
    public void testCreateWebDriverRemoteNoProxy() throws Exception
    {
        when(proxy.isStarted()).thenReturn(false);
        when(webDriverFactory.getRemoteWebDriver(any(DesiredCapabilities.class))).thenReturn(driver);
        runCreateTest(true, null);
    }

    @Test
    public void testCreateWebDriverLocalWithProxy() throws Exception
    {
        mockSeleniumProxyCreation(false);
        when(webDriverFactory.getWebDriver(any(DesiredCapabilities.class))).thenReturn(driver);
        runCreateTest(false, FIREFOX);
        verifyProxyCreation(false);
    }

    @Test
    public void testCreateWebDriverLocalNoProxy() throws Exception
    {
        when(proxy.isStarted()).thenReturn(false);
        when(webDriverFactory.getWebDriver(any(DesiredCapabilities.class))).thenReturn(driver);
        runCreateTest(false, FIREFOX);
    }
}
