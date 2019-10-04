/*
 * Copyright 2019 the original author or authors.
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

package org.vividus.bdd;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.jbehave.core.model.Meta;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Story;
import org.jbehave.core.reporters.StoryReporter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.vividus.bdd.context.IBddRunContext;
import org.vividus.bdd.model.RunningScenario;
import org.vividus.bdd.model.RunningStory;
import org.vividus.bdd.spring.Configuration;
import org.vividus.proxy.IProxy;

@ExtendWith(MockitoExtension.class)
class ProxyAgentStoryReporterTests
{
    private static final String STORY_NAME = "someName";
    private static final List<String> PROXY_META = List.of("proxy");
    private static final String SCENARIO_TITLE = "scenario";

    @Mock
    private IProxy proxy;

    @Mock
    private IBddRunContext bddRunContext;

    @Mock
    private Configuration configuration;

    @Mock
    private StoryReporter next;

    @InjectMocks
    private ProxyAgentStoryReporter proxyAgentStoryReporter;

    @BeforeEach
    void beforeEach()
    {
        proxyAgentStoryReporter.setNext(next);
    }

    @Test
    void testBeforeGivenStory()
    {
        RunningStory runningStory = mockRunningStoryWithName(STORY_NAME);
        proxyAgentStoryReporter.beforeStory(runningStory.getStory(), true);
        verifyNoInteractions(proxy);
        verify(next).beforeStory(runningStory.getStory(), true);
    }

    @Test
    void testBeforeStoryWhichIsBeforeStories()
    {
        RunningStory runningStory = mockRunningStoryWithName("BeforeStories");
        proxyAgentStoryReporter.beforeStory(runningStory.getStory(), false);
        verifyNoInteractions(proxy);
        verify(next).beforeStory(runningStory.getStory(), false);
    }

    @Test
    void testBeforeStoryWhichIsAfterStories()
    {
        RunningStory runningStory =  mockRunningStoryWithName("AfterStories");
        proxyAgentStoryReporter.beforeStory(runningStory.getStory(), false);
        verifyNoInteractions(proxy);
        verify(next).beforeStory(runningStory.getStory(), false);
    }

    @Test
    void testBeforeStoryProxyDisabled()
    {
        RunningStory runningStory =  mockRunningStoryWithName(STORY_NAME);
        when(runningStory.getStory().getMeta()).thenReturn(new Meta(Collections.emptyList()));
        proxyAgentStoryReporter.beforeStory(runningStory.getStory(), false);
        verifyNoInteractions(proxy);
        verify(next).beforeStory(runningStory.getStory(), false);
    }

    @Test
    void testBeforeStoryProxyEnabled()
    {
        RunningStory runningStory =  mockRunningStoryWithName(STORY_NAME);
        proxyAgentStoryReporter.setProxyEnabled(true);
        proxyAgentStoryReporter.beforeStory(runningStory.getStory(), false);
        verify(proxy).start();
        verify(next).beforeStory(runningStory.getStory(), false);
    }

    @Test
    void testBeforeStoryProxyEnabledFromStoryMeta()
    {
        RunningStory runningStory =  mockRunningStoryWithName(STORY_NAME);
        when(runningStory.getStory().getMeta()).thenReturn(new Meta(PROXY_META));
        proxyAgentStoryReporter.beforeStory(runningStory.getStory(), false);
        verify(proxy).start();
        verify(next).beforeStory(runningStory.getStory(), false);
    }

    @Test
    void testBeforeStoryDryRun()
    {
        when(configuration.dryRun()).thenReturn(true);
        RunningStory runningStory = mockRunningStoryWithName(STORY_NAME);
        proxyAgentStoryReporter.beforeStory(runningStory.getStory(), false);
        verifyNoInteractions(proxy);
        verify(next).beforeStory(runningStory.getStory(), false);
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
        proxyAgentStoryReporter.afterScenario();
        verifyProxyInactivity();
        verify(next).afterScenario();
    }

    @Test
    void testAfterScenarioWhenProxyIsStartedButNoCleanUpIsNeeded()
    {
        mockRunningScenarioAndStoryWithMeta(Collections.emptyList(), Collections.emptyList());
        proxyAgentStoryReporter.setProxyEnabled(true);
        when(proxy.isStarted()).thenReturn(Boolean.TRUE);
        proxyAgentStoryReporter.afterScenario();
        verifyProxyInactivity();
        verify(next).afterScenario();
    }

    @Test
    void testAfterScenarioWhenProxyIsStartedAndStopRecordingIsNeededButProxyStopIsNot()
    {
        mockRunningScenarioWithMetaAndEmptyStory(Collections.emptyList());
        proxyAgentStoryReporter.setProxyRecordingEnabled(true);
        when(proxy.isStarted()).thenReturn(Boolean.TRUE);
        proxyAgentStoryReporter.afterScenario();
        verify(proxy).stopRecording();
        verifyNoMoreInteractions(proxy);
        verify(next).afterScenario();
    }

    @Test
    void testAfterScenarioWhenProxyIsStartedAndStopRecordingAndStopProxyAreNeeded()
    {
        mockRunningScenarioWithMetaAndEmptyStory(PROXY_META);
        proxyAgentStoryReporter.setProxyRecordingEnabled(true);
        when(proxy.isStarted()).thenReturn(Boolean.TRUE);
        proxyAgentStoryReporter.afterScenario();
        verify(proxy).stopRecording();
        verify(proxy).stop();
        verify(next).afterScenario();
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

    private RunningStory mockRunningStoryInBddRunContext(Story story)
    {
        RunningStory runningStory = new RunningStory();
        runningStory.setStory(story);
        when(bddRunContext.getRunningStory()).thenReturn(runningStory);
        return runningStory;
    }

    private RunningStory mockRunningStoryWithName(String storyName)
    {
        Story story = mock(Story.class);
        when(story.getName()).thenReturn(storyName);
        return mockRunningStoryInBddRunContext(story);
    }

    private Scenario mockRunningScenario(List<String> scenarioMeta, RunningStory runningStory)
    {
        Scenario scenario = new Scenario(SCENARIO_TITLE, new Meta(scenarioMeta));
        RunningScenario runningScenario = new RunningScenario();
        runningScenario.setScenario(scenario);
        runningStory.setRunningScenario(runningScenario);
        return scenario;
    }

    private Scenario mockRunningScenarioAndStoryWithMeta(List<String> storyMeta, List<String> scenarioMeta)
    {
        Story story = mock(Story.class);
        when(story.getMeta()).thenReturn(new Meta(storyMeta));
        RunningStory runningStory = mockRunningStoryInBddRunContext(story);
        return mockRunningScenario(scenarioMeta, runningStory);
    }

    private Scenario mockRunningScenarioWithMetaAndEmptyStory(List<String> scenarioMeta)
    {
        RunningStory runningStory = mockRunningStoryInBddRunContext(mock(Story.class));
        return mockRunningScenario(scenarioMeta, runningStory);
    }

    private void verifyProxyInactivity()
    {
        verify(proxy).isStarted();
        verifyNoMoreInteractions(proxy);
    }
}
