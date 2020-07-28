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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Properties;

import org.jbehave.core.model.Meta;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Story;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.remote.CapabilityType;
import org.vividus.bdd.context.IBddRunContext;
import org.vividus.bdd.model.RunningScenario;
import org.vividus.bdd.model.RunningStory;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.manager.IWebDriverManagerContext;

@ExtendWith(MockitoExtension.class)
class VividusWebDriverSetupStepsTests
{
    @Mock private IWebDriverProvider webDriverProvider;
    @Mock private IWebDriverManagerContext webDriverManagerContext;
    @Mock private IBddRunContext bddRunContext;
    @InjectMocks private VividusWebDriverSetupSteps steps;

    @Test
    void testBeforeScenario()
    {
        RunningStory runningStory = mock(RunningStory.class);
        RunningScenario runningScenario = mock(RunningScenario.class);
        Scenario scenario = mock(Scenario.class);
        Meta meta = new Meta();

        when(bddRunContext.getRunningStory()).thenReturn(runningStory);
        when(runningStory.getRunningScenario()).thenReturn(runningScenario);
        when(runningScenario.getScenario()).thenReturn(scenario);
        when(scenario.getMeta()).thenReturn(meta);

        steps.beforeScenario();

        verifyNoMoreInteractions(bddRunContext, runningStory, runningScenario, scenario, webDriverProvider);
    }

    @Test
    void testBeforeStory()
    {
        RunningStory runningStory = mock(RunningStory.class);
        Story story = mock(Story.class);
        Meta meta = new Meta();

        when(bddRunContext.getRunningStory()).thenReturn(runningStory);
        when(runningStory.getStory()).thenReturn(story);
        when(story.getMeta()).thenReturn(meta);

        steps.beforeStory();

        verifyNoMoreInteractions(bddRunContext, runningStory, story, webDriverProvider);
    }

    @Test
    void testBeforeStoryWithControllingMeta()
    {
        RunningStory runningStory = mock(RunningStory.class);
        Story story = mock(Story.class);
        Properties properties = new Properties();
        properties.put(CapabilityType.BROWSER_NAME, "chrome");
        Meta meta = new Meta(properties);

        when(bddRunContext.getRunningStory()).thenReturn(runningStory);
        when(runningStory.getStory()).thenReturn(story);
        when(story.getMeta()).thenReturn(meta);

        steps.beforeStory();

        verify(webDriverProvider).end();
        verifyNoMoreInteractions(bddRunContext, runningStory, story, webDriverProvider);
    }

    @Test
    void testAfterStory()
    {
        steps.afterStory();

        verify(webDriverProvider).end();
        verify(webDriverManagerContext).reset();
        verifyNoMoreInteractions(webDriverProvider, webDriverManagerContext);
    }

    @Test
    void testGetWebDriverProvider()
    {
        assertEquals(webDriverProvider, steps.getWebDriverProvider());
    }

    @Test
    void testGetBddRunContext()
    {
        assertEquals(bddRunContext, steps.getBddRunContext());
    }
}
