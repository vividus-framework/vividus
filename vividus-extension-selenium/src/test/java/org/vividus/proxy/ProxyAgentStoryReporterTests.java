/*
 * Copyright 2019-2021 the original author or authors.
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

package org.vividus.proxy;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.model.Meta;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Story;
import org.jbehave.core.reporters.StoryReporter;
import org.jbehave.core.steps.Timing;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.vividus.context.RunContext;
import org.vividus.model.RunningScenario;
import org.vividus.model.RunningStory;

@ExtendWith(MockitoExtension.class)
class ProxyAgentStoryReporterTests
{
    private static final List<String> PROXY_META = List.of("proxy");
    private static final String SCENARIO_TITLE = "scenario";

    @Mock private IProxy proxy;
    @Mock private RunContext runContext;
    @Mock private Configuration configuration;
    @Mock private StoryReporter next;
    @InjectMocks private ProxyAgentStoryReporter proxyAgentStoryReporter;

    @BeforeEach
    void beforeEach()
    {
        proxyAgentStoryReporter.setNext(next);
    }

    @Test
    void testBeforeGivenStory()
    {
        Story story = mock(Story.class);
        proxyAgentStoryReporter.beforeStory(story, true);
        verifyNoInteractions(proxy);
        verify(next).beforeStory(story, true);
    }

    @Test
    void testBeforeStoryProxyDisabled()
    {
        Story story = mock(Story.class);
        mockRunningStoryInRunContext(story);
        when(story.getMeta()).thenReturn(new Meta(Collections.emptyList()));
        proxyAgentStoryReporter.beforeStory(story, false);
        verifyNoInteractions(proxy);
        verify(next).beforeStory(story, false);
    }

    @Test
    void testBeforeStoryProxyEnabled()
    {
        Story story = mock(Story.class);
        proxyAgentStoryReporter.setProxyEnabled(true);
        proxyAgentStoryReporter.beforeStory(story, false);
        verify(proxy).start();
        verify(next).beforeStory(story, false);
    }

    @Test
    void testBeforeStoryProxyEnabledFromStoryMeta()
    {
        Story story = mock(Story.class);
        RunningStory runningStory = new RunningStory();
        runningStory.setStory(story);
        when(runContext.getRunningStory()).thenReturn(runningStory);
        when(runningStory.getStory().getMeta()).thenReturn(new Meta(PROXY_META));
        proxyAgentStoryReporter.beforeStory(runningStory.getStory(), false);
        verify(proxy).start();
        verify(next).beforeStory(runningStory.getStory(), false);
    }

    @Test
    void testBeforeStoryDryRun()
    {
        when(configuration.dryRun()).thenReturn(true);
        Story story = mock(Story.class);
        proxyAgentStoryReporter.beforeStory(story, false);
        verifyNoInteractions(proxy);
        verify(next).beforeStory(story, false);
    }

    @Test
    void testBeforeScenarioDryRun()
    {
        when(configuration.dryRun()).thenReturn(true);
        Scenario scenario = new Scenario(SCENARIO_TITLE, Collections.emptyList());
        proxyAgentStoryReporter.beforeScenario(scenario);
        verifyNoInteractions(proxy);
        verify(next).beforeScenario(scenario);
    }

    @Test
    void testBeforeScenarioStartProxyRecordingByProperty()
    {
        proxyAgentStoryReporter.setProxyRecordingEnabled(true);
        when(proxy.isStarted()).thenReturn(Boolean.TRUE);
        Scenario scenario = new Scenario(SCENARIO_TITLE, Collections.emptyList());
        proxyAgentStoryReporter.beforeScenario(scenario);
        verify(proxy).clearRequestFilters();
        verify(proxy).startRecording();
        verify(next).beforeScenario(scenario);
    }

    @Test
    void testBeforeScenarioStartProxyRecordingByScenarioMeta()
    {
        Scenario scenario = mockRunningScenarioAndStoryWithMeta(Collections.emptyList(), PROXY_META);
        proxyAgentStoryReporter.setProxyRecordingEnabled(false);
        when(proxy.isStarted()).thenReturn(Boolean.TRUE);
        proxyAgentStoryReporter.beforeScenario(scenario);
        verify(proxy).clearRequestFilters();
        verify(proxy).startRecording();
        verify(next).beforeScenario(scenario);
    }

    @Test
    void testBeforeScenarioNoProxyRecordingStart()
    {
        Scenario scenario = mockRunningScenarioAndStoryWithMeta(Collections.emptyList(), Collections.emptyList());
        proxyAgentStoryReporter.setProxyRecordingEnabled(false);
        when(proxy.isStarted()).thenReturn(Boolean.TRUE);
        proxyAgentStoryReporter.beforeScenario(scenario);
        verify(proxy, never()).clearRequestFilters();
        verify(proxy, never()).startRecording();
        verify(next).beforeScenario(scenario);
    }

    @Test
    void testBeforeScenarioStartProxy()
    {
        Scenario scenario = mockRunningScenarioAndStoryWithMeta(Collections.emptyList(), PROXY_META);
        proxyAgentStoryReporter.beforeScenario(scenario);
        verify(proxy).start();
        verify(next).beforeScenario(scenario);
    }

    @Test
    void testBeforeScenarioNoProxyStart()
    {
        Scenario scenario = mockRunningScenarioAndStoryWithMeta(Collections.emptyList(), Collections.emptyList());
        proxyAgentStoryReporter.beforeScenario(scenario);
        verify(proxy, never()).start();
        verify(next).beforeScenario(scenario);
    }

    @Test
    void testBeforeScenarioStartProxyAndRecording()
    {
        Scenario scenario = mockRunningScenarioAndStoryWithMeta(PROXY_META, Collections.emptyList());
        when(proxy.isStarted()).thenAnswer(new Answer<Boolean>()
        {
            private int count;

            @Override
            public Boolean answer(InvocationOnMock invocation)
            {
                return ++count != 1;
            }
        });
        proxyAgentStoryReporter.beforeScenario(scenario);
        verify(proxy).start();
        verify(proxy).clearRequestFilters();
        verify(proxy).startRecording();
        verify(next).beforeScenario(scenario);
    }

    @Test
    void testBeforeScenarioStartProxyEnabledInStoryMeta()
    {
        Scenario scenario = mockRunningScenarioAndStoryWithMeta(PROXY_META, Collections.emptyList());
        proxyAgentStoryReporter.beforeScenario(scenario);
        verify(proxy).start();
        verify(next).beforeScenario(scenario);
    }

    @Test
    void testAfterScenarioWhenNoProxyIsStarted()
    {
        Timing timing = mock(Timing.class);
        proxyAgentStoryReporter.afterScenario(timing);
        verifyProxyInactivity();
        verify(next).afterScenario(timing);
    }

    @Test
    void testAfterScenarioWhenProxyIsStartedButNoCleanUpIsNeeded()
    {
        mockRunningScenarioAndStoryWithMeta(Collections.emptyList(), Collections.emptyList());
        proxyAgentStoryReporter.setProxyEnabled(true);
        when(proxy.isStarted()).thenReturn(Boolean.TRUE);
        Timing timing = mock(Timing.class);
        proxyAgentStoryReporter.afterScenario(timing);
        verifyProxyInactivity();
        verify(next).afterScenario(timing);
    }

    @Test
    void testAfterScenarioWhenProxyIsStartedAndStopRecordingIsNeededButProxyStopIsNot()
    {
        mockRunningScenario(mock(Story.class), Collections.emptyList());
        proxyAgentStoryReporter.setProxyRecordingEnabled(true);
        when(proxy.isStarted()).thenReturn(Boolean.TRUE);
        Timing timing = mock(Timing.class);
        proxyAgentStoryReporter.afterScenario(timing);
        verify(proxy).stopRecording();
        verifyNoMoreInteractions(proxy);
        verify(next).afterScenario(timing);
    }

    @Test
    void testAfterScenarioWhenProxyIsStartedAndStopRecordingAndStopProxyAreNeeded()
    {
        mockRunningScenario(mock(Story.class), PROXY_META);
        proxyAgentStoryReporter.setProxyRecordingEnabled(true);
        when(proxy.isStarted()).thenReturn(Boolean.TRUE);
        Timing timing = mock(Timing.class);
        proxyAgentStoryReporter.afterScenario(timing);
        verify(proxy).stopRecording();
        verify(proxy).stop();
        verify(next).afterScenario(timing);
    }

    @Test
    void testAfterGivenStory()
    {
        proxyAgentStoryReporter.afterStory(true);
        verifyNoInteractions(proxy);
        verify(next).afterStory(true);
    }

    @Test
    void testAfterStoryWhenNoCleanUpIsNeeded()
    {
        proxyAgentStoryReporter.afterStory(false);
        verifyProxyInactivity();
        verify(next).afterStory(false);
    }

    @Test
    void testAfterStoryWhenProxyIsStarted()
    {
        when(proxy.isStarted()).thenReturn(Boolean.TRUE);
        proxyAgentStoryReporter.afterStory(false);
        verify(proxy).stop();
        verify(next).afterStory(false);
    }

    private RunningStory mockRunningStoryInRunContext(Story story)
    {
        RunningStory runningStory = new RunningStory();
        runningStory.setStory(story);
        when(runContext.getRunningStory()).thenReturn(runningStory);
        return runningStory;
    }

    private Scenario mockRunningScenarioAndStoryWithMeta(List<String> storyMeta, List<String> scenarioMeta)
    {
        Story story = mock(Story.class);
        when(story.getMeta()).thenReturn(new Meta(storyMeta));
        return mockRunningScenario(story, scenarioMeta);
    }

    private Scenario mockRunningScenario(Story story, List<String> scenarioMeta)
    {
        Scenario scenario = new Scenario(SCENARIO_TITLE, new Meta(scenarioMeta));

        RunningScenario runningScenario = new RunningScenario();
        runningScenario.setScenario(scenario);

        RunningStory runningStory = mockRunningStoryInRunContext(story);
        runningStory.setRunningScenario(runningScenario);

        return scenario;
    }

    private void verifyProxyInactivity()
    {
        verify(proxy).isStarted();
        verifyNoMoreInteractions(proxy);
    }
}
