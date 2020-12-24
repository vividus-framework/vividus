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

package org.vividus.bdd.steps;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Properties;

import org.jbehave.core.model.Meta;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Story;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.remote.CapabilityType;
import org.vividus.bdd.context.IBddRunContext;
import org.vividus.bdd.model.RunningScenario;
import org.vividus.bdd.model.RunningStory;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.manager.IWebDriverManagerContext;

@ExtendWith(MockitoExtension.class)
class WebDriverSetupStepsTests
{
    @Mock private IWebDriverProvider webDriverProvider;
    @Mock private IWebDriverManagerContext webDriverManagerContext;
    @Mock private IBddRunContext bddRunContext;

    @Test
    void shouldNotStopWebDriverInBeforeScenarioIfScenarioMetaDoesNotContainControllingMeta()
    {
        WebDriverSetupSteps steps = new WebDriverSetupSteps(WebDriverSessionScope.SCENARIO, webDriverProvider,
                webDriverManagerContext, bddRunContext);
        prepareRunningScenario(new Meta());
        steps.beforeScenario();
        verifyNoMoreInteractions(bddRunContext, webDriverProvider);
    }

    @Test
    void shouldStopWebDriverInBeforeScenarioIfScenarioMetaContainsControllingMeta()
    {
        WebDriverSetupSteps steps = new WebDriverSetupSteps(WebDriverSessionScope.SCENARIO, webDriverProvider,
                webDriverManagerContext, bddRunContext);
        prepareRunningScenario(createMetaWithControllingTag());
        steps.beforeScenario();
        verify(webDriverProvider).end();
        verifyNoMoreInteractions(bddRunContext, webDriverProvider);
    }

    @Test
    void shouldNotStopWebDriverInBeforeStoryIfStoryMetaDoesNotContainControllingMeta()
    {
        WebDriverSetupSteps steps = new WebDriverSetupSteps(WebDriverSessionScope.SCENARIO, webDriverProvider,
                webDriverManagerContext, bddRunContext);
        prepareRunningStory(new Meta());
        steps.beforeStory();
        verifyNoMoreInteractions(bddRunContext, webDriverProvider);
    }

    @Test
    void shouldStopWebDriverInBeforeStoryIfStoryMetaContainsControllingMeta()
    {
        WebDriverSetupSteps steps = new WebDriverSetupSteps(WebDriverSessionScope.SCENARIO, webDriverProvider,
                webDriverManagerContext, bddRunContext);
        prepareRunningStory(createMetaWithControllingTag());
        steps.beforeStory();
        verify(webDriverProvider).end();
        verifyNoMoreInteractions(bddRunContext, webDriverProvider);
    }

    @Test
    void shouldNotStopWebDriverInAfterScenario()
    {
        WebDriverSetupSteps steps = new WebDriverSetupSteps(WebDriverSessionScope.STORY, webDriverProvider,
                webDriverManagerContext, bddRunContext);
        steps.afterScenario();
        verifyNoInteractions(webDriverProvider, webDriverManagerContext);
    }

    @Test
    void shouldStopWebDriverInAfterStory()
    {
        WebDriverSetupSteps steps = new WebDriverSetupSteps(WebDriverSessionScope.STORY, webDriverProvider,
                webDriverManagerContext, bddRunContext);
        steps.afterStory();
        verify(webDriverProvider).end();
        verify(webDriverManagerContext).reset();
        verifyNoMoreInteractions(webDriverProvider, webDriverManagerContext);
    }

    @Test
    void shouldStopWebDriverInAfterScenario()
    {
        WebDriverSetupSteps steps = new WebDriverSetupSteps(WebDriverSessionScope.SCENARIO, webDriverProvider,
                webDriverManagerContext, bddRunContext);
        steps.afterScenario();
        verify(webDriverProvider).end();
        verify(webDriverManagerContext).reset();
        verifyNoMoreInteractions(webDriverProvider, webDriverManagerContext);
    }

    @Test
    void shouldNotStopWebDriverInAfterStory()
    {
        WebDriverSetupSteps steps = new WebDriverSetupSteps(WebDriverSessionScope.SCENARIO, webDriverProvider,
                webDriverManagerContext, bddRunContext);
        steps.afterStory();
        verifyNoInteractions(webDriverProvider, webDriverManagerContext);
    }

    @Test
    void shouldNotAllowToSetNullSessionScope()
    {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> new WebDriverSetupSteps(null, webDriverProvider, webDriverManagerContext, bddRunContext));
        assertEquals("Application session scope is not set", exception.getMessage());
    }

    private Meta createMetaWithControllingTag()
    {
        Properties properties = new Properties();
        properties.put(CapabilityType.BROWSER_NAME, "chrome");
        return new Meta(properties);
    }

    private void prepareRunningStory(Meta meta)
    {
        RunningStory runningStory = new RunningStory();
        runningStory.setStory(new Story(null, null, meta, null, null));

        when(bddRunContext.getRunningStory()).thenReturn(runningStory);
    }

    private void prepareRunningScenario(Meta scenarioMeta)
    {
        RunningScenario runningScenario = new RunningScenario();
        runningScenario.setScenario(new Scenario(null, scenarioMeta));
        RunningStory runningStory = new RunningStory();
        runningStory.setRunningScenario(runningScenario);

        when(bddRunContext.getRunningStory()).thenReturn(runningStory);
    }
}
