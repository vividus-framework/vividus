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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.jbehave.core.model.Meta;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Story;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.vividus.bdd.context.IBddRunContext;
import org.vividus.bdd.model.RunningScenario;
import org.vividus.bdd.model.RunningStory;
import org.vividus.selenium.manager.IWebDriverManagerContext;
import org.vividus.selenium.manager.WebDriverManagerParameter;

@ExtendWith(MockitoExtension.class)
class VividusWebDriverFactoryTests
{
    private static final String KEY5 = "key5";
    private static final String KEY4 = "key4";
    private static final String VALUE_FROM_CONFIGURER3 = "valueFromConfigurer3";
    private static final String KEY3 = "key3";
    private static final String KEY2 = "key2";
    private static final String KEY1 = "key1";
    @Mock
    private WebDriver webDriver;
    @Mock
    private IWebDriverManagerContext webDriverManagerContext;
    @Mock
    private IBddRunContext bddRunContext;

    @Test
    void shouldCreateWebDriverWithCapabilitiesFromConfigurersAndFromMeta()
    {
        DesiredCapabilitiesConfigurer firstCapabilitiesConfigurer = caps -> caps.setCapability(KEY1,
                "valueFromConfigurer1");
        DesiredCapabilitiesConfigurer secondCapabilitiesConfigurer = caps -> caps.setCapability(KEY2,
                "valueFromConfigurer2");
        DesiredCapabilitiesConfigurer thirdCapabilitiesConfigurer = caps -> caps.setCapability(KEY3,
                VALUE_FROM_CONFIGURER3);
        String valueWebDriverManager1 = "valueWebDriverManager1";
        String valueFromWebDriverManager5 = "valueFromWebDriverManager5";
        when(webDriverManagerContext.getParameter(WebDriverManagerParameter.DESIRED_CAPABILITIES))
                .thenReturn(new DesiredCapabilities(Map.of(KEY1, valueWebDriverManager1, KEY4,
                        "valueFromWebDriverManager4", KEY5, valueFromWebDriverManager5)));

        RunningStory runningStory = mock(RunningStory.class);
        when(bddRunContext.getRunningStory()).thenReturn(runningStory);
        Story story = mock(Story.class);
        when(runningStory.getStory()).thenReturn(story);
        when(story.getMeta())
                .thenReturn(metaOf("capability.key2 valueFromStoryMeta2", "capability.key4 valueFromStoryMeta4",
                        "capability.key6 valueFromStoryMeta6", "capability.key7 valueFromStoryMeta7"));
        RunningScenario runningScenario = mock(RunningScenario.class);
        when(runningStory.getRunningScenario()).thenReturn(runningScenario);
        Scenario scenario = mock(Scenario.class);
        when(runningScenario.getScenario()).thenReturn(scenario);
        when(scenario.getMeta())
                .thenReturn(metaOf("capability.key6 valueFromScenarioMeta6", "capability.key7 valueFromScenarioMeta7"));

        TestVividusWebDriverFactory factory = new TestVividusWebDriverFactory(true, webDriverManagerContext,
                bddRunContext, Optional.of(Set.of(firstCapabilitiesConfigurer, secondCapabilitiesConfigurer,
                        thirdCapabilitiesConfigurer)));

        VividusWebDriver vividusWebDriver = factory.create();

        assertTrue(vividusWebDriver.isRemote());
        assertEquals(Map.of(KEY1,   valueWebDriverManager1,
                            KEY2,   "valueFromStoryMeta2",
                            KEY3,   VALUE_FROM_CONFIGURER3,
                            KEY4,   "valueFromStoryMeta4",
                            KEY5,   valueFromWebDriverManager5,
                            "key6", "valueFromScenarioMeta6",
                            "key7", "valueFromScenarioMeta7"), vividusWebDriver.getDesiredCapabilities().asMap());
        InOrder ordered = Mockito.inOrder(webDriverManagerContext, bddRunContext);
        ordered.verify(webDriverManagerContext).getParameter(WebDriverManagerParameter.DESIRED_CAPABILITIES);
        ordered.verify(webDriverManagerContext).reset(WebDriverManagerParameter.DESIRED_CAPABILITIES);
        ordered.verify(bddRunContext).getRunningStory();
        verifyNoMoreInteractions(webDriverManagerContext);
        assertSame(webDriver, vividusWebDriver.getWrappedDriver());
    }

    @Test
    void shouldCreateDriverWithoutMetaCapabilities()
    {
        String value = "value1";
        when(webDriverManagerContext.getParameter(WebDriverManagerParameter.DESIRED_CAPABILITIES))
                .thenReturn(new DesiredCapabilities(Map.of(KEY1, value)));

        TestVividusWebDriverFactory factory = new TestVividusWebDriverFactory(false, webDriverManagerContext,
                bddRunContext, Optional.empty());

        VividusWebDriver vividusWebDriver = factory.create();

        assertFalse(vividusWebDriver.isRemote());
        assertEquals(Map.of(KEY1, value), vividusWebDriver.getDesiredCapabilities().asMap());
        InOrder ordered = Mockito.inOrder(webDriverManagerContext, bddRunContext);
        ordered.verify(webDriverManagerContext).getParameter(WebDriverManagerParameter.DESIRED_CAPABILITIES);
        ordered.verify(webDriverManagerContext).reset(WebDriverManagerParameter.DESIRED_CAPABILITIES);
        ordered.verify(bddRunContext).getRunningStory();
        verifyNoMoreInteractions(webDriverManagerContext);
        assertSame(webDriver, vividusWebDriver.getWrappedDriver());
        verify(bddRunContext).getRunningStory();
    }

    @Test
    void shouldReturnRemoteExecution()
    {
        TestVividusWebDriverFactory factory = new TestVividusWebDriverFactory(true, webDriverManagerContext,
                bddRunContext, Optional.empty());
        assertTrue(factory.isRemoteExecution());
    }

    private Meta metaOf(String... properties)
    {
        return new Meta(List.of(properties));
    }

    private final class TestVividusWebDriverFactory extends AbstractVividusWebDriverFactory
    {
        private TestVividusWebDriverFactory(boolean remoteExecution, IWebDriverManagerContext webDriverManagerContext,
                IBddRunContext bddRunContext,
                Optional<Set<DesiredCapabilitiesConfigurer>> desiredCapabilitiesConfigurers)
        {
            super(remoteExecution, webDriverManagerContext, bddRunContext, desiredCapabilitiesConfigurers);
        }

        @Override
        protected WebDriver createWebDriver(DesiredCapabilities desiredCapabilities)
        {
            return webDriver;
        }
    }
}
