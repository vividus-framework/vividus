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

import java.util.List;
import java.util.Optional;
import java.util.Properties;

import org.jbehave.core.model.Meta;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Story;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.HasCapabilities;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WrapsDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.events.WebDriverEventListener;
import org.powermock.reflect.Whitebox;
import org.vividus.bdd.context.IBddRunContext;
import org.vividus.bdd.model.RunningScenario;
import org.vividus.bdd.model.RunningStory;
import org.vividus.proxy.IProxy;
import org.vividus.selenium.manager.IWebDriverManager;
import org.vividus.selenium.manager.IWebDriverManagerContext;
import org.vividus.selenium.manager.WebDriverManagerParameter;

@ExtendWith(MockitoExtension.class)
class VividusWebDriverFactoryTests
{
    private static final String FIREFOX = "firefox";
    private static final String TEST = "Test";
    private static final String STORY_FILE = TEST + ".story";

    @Mock private IBddRunContext bddRunContext;
    @Mock private IWebDriverManagerContext webDriverManagerContext;
    @Mock private IWebDriverFactory webDriverFactory;
    @Mock private IBrowserWindowSizeProvider browserWindowSizeProvider;
    @Mock private IWebDriverManager webDriverManager;
    @Mock private WebDriverEventListener webDriverEventListener;
    @Mock private IProxy proxy;
    private VividusWebDriverFactory vividusWebDriverFactory;

    @Mock(extraInterfaces = HasCapabilities.class)
    private WebDriver driver;

    @BeforeEach
    public void beforeEach()
    {
        vividusWebDriverFactory = new VividusWebDriverFactory(true, webDriverManagerContext, bddRunContext,
                Optional.empty(), webDriverFactory, webDriverManager, browserWindowSizeProvider, proxy);
    }

    private void runCreateTest(boolean remoteExecution, String browserName) throws Exception
    {
        runCreateTest(remoteExecution, browserName, createRunningStory(browserName));
    }

    private void runCreateTest(boolean remoteExecution, String browserName, RunningStory runningStory) throws Exception
    {
        List<WebDriverEventListener> eventListeners = List.of(webDriverEventListener);
        vividusWebDriverFactory.setWebDriverEventListeners(eventListeners);

        when(bddRunContext.getRunningStory()).thenReturn(runningStory);
        BrowserWindowSize windowSize = new BrowserWindowSize("1920x1080");
        when(browserWindowSizeProvider.getBrowserWindowSize(remoteExecution))
                .thenReturn(windowSize);
        VividusWebDriver vividusWebDriver = vividusWebDriverFactory.create();
        WebDriver eventFiringDriver = vividusWebDriver.getWrappedDriver();
        assertEquals(driver, ((WrapsDriver) eventFiringDriver).getWrappedDriver());
        assertEquals(eventListeners, Whitebox.getInternalState(eventFiringDriver, "eventListeners"));
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
        runningStory.setStory(new Story(STORY_FILE, null, new Meta(), null, null));
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

    @Test
    void testCreateWebDriverRemoteWithProxy() throws Exception
    {
        when(webDriverFactory.getRemoteWebDriver(any(DesiredCapabilities.class))).thenReturn(driver);
        runCreateTest(true, null);
    }

    @Test
    void testCreateWebDriverRemoteNoProxy() throws Exception
    {
        when(webDriverFactory.getRemoteWebDriver(any(DesiredCapabilities.class))).thenReturn(driver);
        runCreateTest(true, null);
    }

    @Test
    void testCreateWebDriverNoRunningScenario() throws Exception
    {
        when(webDriverFactory.getRemoteWebDriver(any(DesiredCapabilities.class))).thenReturn(driver);
        RunningStory runningStory = new RunningStory();
        runningStory.setStory(new Story(STORY_FILE, null, new Meta(), null, null));
        runCreateTest(true, null, runningStory);
    }

    @Test
    void testCreateWebDriverLocalWithProxy() throws Exception
    {
        notRemoteExecution();
        when(webDriverFactory.getWebDriver(any(DesiredCapabilities.class))).thenReturn(driver);
        runCreateTest(false, FIREFOX);
    }

    private void notRemoteExecution()
    {
        Whitebox.setInternalState(vividusWebDriverFactory, "remoteExecution", false);
    }

    @Test
    void testCreateWebDriverLocalNoProxy() throws Exception
    {
        notRemoteExecution();
        when(webDriverFactory.getWebDriver(any(DesiredCapabilities.class))).thenReturn(driver);
        runCreateTest(false, FIREFOX);
    }

    @Test
    void shouldAddProxyCapabilitiesWhenProxyStarted()
    {
        DesiredCapabilities desiredCapabilities = mock(DesiredCapabilities.class);
        when(proxy.isStarted()).thenReturn(true);
        Proxy seleniumProxy = mock(Proxy.class);
        when(proxy.createSeleniumProxy()).thenReturn(seleniumProxy);
        vividusWebDriverFactory.setDesiredCapabilities(desiredCapabilities);
        verify(desiredCapabilities).setCapability(CapabilityType.PROXY, seleniumProxy);
        verify(desiredCapabilities).setCapability(CapabilityType.ACCEPT_INSECURE_CERTS, true);
    }
}
