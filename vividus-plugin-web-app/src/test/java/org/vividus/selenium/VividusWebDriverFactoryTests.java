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

import org.apache.commons.lang3.reflect.FieldUtils;
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
import org.vividus.bdd.context.IBddRunContext;
import org.vividus.bdd.model.RunningScenario;
import org.vividus.bdd.model.RunningStory;
import org.vividus.proxy.IProxy;
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
    @Mock private WebDriverEventListener webDriverEventListener;
    @Mock private IProxy proxy;
    private VividusWebDriverFactory vividusWebDriverFactory;

    @Mock(extraInterfaces = HasCapabilities.class)
    private WebDriver driver;

    @BeforeEach
    public void beforeEach()
    {
        vividusWebDriverFactory = new VividusWebDriverFactory(true, webDriverManagerContext, bddRunContext,
                Optional.empty(), webDriverFactory, proxy);
    }

    private void runCreateTest(boolean remoteExecution, String browserName) throws IllegalAccessException
    {
        runCreateTest(remoteExecution, createRunningStory(browserName));
    }

    private void runCreateTest(boolean remoteExecution, RunningStory runningStory) throws IllegalAccessException
    {
        List<WebDriverEventListener> eventListeners = List.of(webDriverEventListener);
        vividusWebDriverFactory.setWebDriverEventListeners(eventListeners);

        when(bddRunContext.getRunningStory()).thenReturn(runningStory);
        VividusWebDriver vividusWebDriver = vividusWebDriverFactory.create();
        WebDriver eventFiringDriver = vividusWebDriver.getWrappedDriver();
        assertEquals(driver, ((WrapsDriver) eventFiringDriver).getWrappedDriver());
        assertEquals(eventListeners, FieldUtils.readField(eventFiringDriver, "eventListeners", true));
        assertEquals(remoteExecution, vividusWebDriver.isRemote());
        verify(webDriverManagerContext).reset(WebDriverManagerParameter.DESIRED_CAPABILITIES);
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
    void testCreateWebDriverRemoteWithProxy() throws IllegalAccessException
    {
        when(webDriverFactory.getRemoteWebDriver(any(DesiredCapabilities.class))).thenReturn(driver);
        runCreateTest(true, (String) null);
    }

    @Test
    void testCreateWebDriverRemoteNoProxy() throws IllegalAccessException
    {
        when(webDriverFactory.getRemoteWebDriver(any(DesiredCapabilities.class))).thenReturn(driver);
        runCreateTest(true, (String) null);
    }

    @Test
    void testCreateWebDriverNoRunningScenario() throws IllegalAccessException
    {
        when(webDriverFactory.getRemoteWebDriver(any(DesiredCapabilities.class))).thenReturn(driver);
        RunningStory runningStory = new RunningStory();
        runningStory.setStory(new Story(STORY_FILE, null, new Meta(), null, null));
        runCreateTest(true, runningStory);
    }

    @Test
    void testCreateWebDriverLocalWithProxy() throws IllegalAccessException
    {
        notRemoteExecution();
        when(webDriverFactory.getWebDriver(any(DesiredCapabilities.class))).thenReturn(driver);
        runCreateTest(false, FIREFOX);
    }

    private void notRemoteExecution() throws IllegalAccessException
    {
        FieldUtils.writeField(vividusWebDriverFactory, "remoteExecution", false, true);
    }

    @Test
    void testCreateWebDriverLocalNoProxy() throws IllegalAccessException
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
