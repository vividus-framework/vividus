/*
 * Copyright 2019-2025 the original author or authors.
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

package org.vividus.steps;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.jbehave.core.model.Meta;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Story;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.context.RunContext;
import org.vividus.model.RunningScenario;
import org.vividus.model.RunningStory;
import org.vividus.selenium.IWebDriverProvider;

@ExtendWith(MockitoExtension.class)
class WebDriverSetupStepsTests
{
    @Mock private IWebDriverProvider webDriverProvider;
    @Mock private RunContext runContext;

    static Stream<Consumer<WebDriverSetupSteps>> scenarioHooks()
    {
        return Stream.of(
                WebDriverSetupSteps::beforeScenario,
                WebDriverSetupSteps::afterScenario
        );
    }

    @MethodSource("scenarioHooks")
    @ParameterizedTest
    void shouldStopWebDriverInScenarioHookIfScenarioMetaContainsControllingMeta(Consumer<WebDriverSetupSteps> test)
    {
        var steps = new WebDriverSetupSteps(WebDriverSessionScope.STORY, webDriverProvider, runContext);
        prepareRunningScenario(createMetaWithControllingTag());

        test.accept(steps);

        verify(webDriverProvider).end();
        verifyNoMoreInteractions(runContext, webDriverProvider);
    }

    @Test
    void shouldNotStopWebDriverInBeforeScenarioIfScenarioMetaDoesNotContainControllingMeta()
    {
        var steps = new WebDriverSetupSteps(WebDriverSessionScope.SCENARIO, webDriverProvider, runContext);
        prepareRunningScenario(new Meta());
        steps.beforeScenario();
        verifyNoMoreInteractions(runContext, webDriverProvider);
    }

    @Test
    void shouldNotStopWebDriverInBeforeStoryIfStoryMetaDoesNotContainControllingMeta()
    {
        var steps = new WebDriverSetupSteps(WebDriverSessionScope.SCENARIO, webDriverProvider, runContext);
        prepareRunningStory(new Meta());
        steps.beforeStory();
        verifyNoMoreInteractions(runContext, webDriverProvider);
    }

    @Test
    void shouldStopWebDriverInBeforeStoryIfStoryMetaContainsControllingMeta()
    {
        var steps = new WebDriverSetupSteps(WebDriverSessionScope.SCENARIO, webDriverProvider, runContext);
        prepareRunningStory(createMetaWithControllingTag());
        steps.beforeStory();
        verify(webDriverProvider).end();
        verifyNoMoreInteractions(runContext, webDriverProvider);
    }

    @Test
    void shouldNotStopWebDriverInAfterScenarioIfScenarioMetaDoesNotContainControllingMeta()
    {
        var steps = new WebDriverSetupSteps(WebDriverSessionScope.STORY, webDriverProvider, runContext);
        prepareRunningScenario(new Meta());
        steps.afterScenario();
        verifyNoInteractions(webDriverProvider);
    }

    @Test
    void shouldStopWebDriverInAfterStory()
    {
        var steps = new WebDriverSetupSteps(WebDriverSessionScope.STORY, webDriverProvider, runContext);
        steps.afterStory();
        verify(webDriverProvider).end();
        verifyNoMoreInteractions(webDriverProvider);
    }

    @Test
    void shouldStopWebDriverInAfterScenario()
    {
        var steps = new WebDriverSetupSteps(WebDriverSessionScope.SCENARIO, webDriverProvider, runContext);
        steps.afterScenario();
        verify(webDriverProvider).end();
        verifyNoMoreInteractions(webDriverProvider);
    }

    @Test
    void shouldNotStopWebDriverInAfterStory()
    {
        var steps = new WebDriverSetupSteps(WebDriverSessionScope.SCENARIO, webDriverProvider, runContext);
        steps.afterStory();
        verifyNoInteractions(webDriverProvider);
    }

    @Test
    void shouldNotAllowToSetNullSessionScope()
    {
        var exception = assertThrows(IllegalArgumentException.class,
            () -> new WebDriverSetupSteps(null, webDriverProvider, runContext));
        assertEquals("Application session scope is not set", exception.getMessage());
    }

    private Meta createMetaWithControllingTag()
    {
        return new Meta(List.of("capability.browserName chrome"));
    }

    private void prepareRunningStory(Meta meta)
    {
        var runningStory = new RunningStory();
        runningStory.setStory(new Story(null, null, meta, null, null));

        when(runContext.getRunningStory()).thenReturn(runningStory);
    }

    private void prepareRunningScenario(Meta scenarioMeta)
    {
        var runningScenario = new RunningScenario();
        runningScenario.setScenario(new Scenario(null, scenarioMeta));

        var runningStory = new RunningStory();
        runningStory.setRunningScenario(runningScenario);

        when(runContext.getRunningStory()).thenReturn(runningStory);
    }
}
