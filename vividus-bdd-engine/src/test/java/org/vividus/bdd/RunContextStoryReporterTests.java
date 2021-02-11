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

package org.vividus.bdd;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbehave.core.embedder.PerformableTree.Status;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Story;
import org.jbehave.core.reporters.StoryReporter;
import org.jbehave.core.steps.Timing;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.bdd.context.BddRunContext;
import org.vividus.bdd.model.RunningScenario;
import org.vividus.bdd.model.RunningStory;

@ExtendWith(MockitoExtension.class)
class RunContextStoryReporterTests
{
    private static final String SCENARIO_TITLE = "scenario title";
    private static final String TITLE_PATTERN = "%s [%d]";

    @Mock
    private BddRunContext bddRunContext;

    @Mock
    private Scenario scenario;

    @Mock
    private StoryReporter next;

    @InjectMocks
    private RunContextStoryReporter runContextStoryReporter;

    @BeforeEach
    void beforeEach()
    {
        runContextStoryReporter.setNext(next);
    }

    @ParameterizedTest
    @CsvSource({"NOT_ALLOWED, false", ", true"})
    void testBeforeStory(Status status, boolean allowed)
    {
        Story story = mock(Story.class);
        boolean givenStory = false;
        when(bddRunContext.getStoryStatus(story)).thenReturn(status);
        runContextStoryReporter.beforeStory(story, givenStory);
        InOrder ordered = inOrder(bddRunContext, next);
        ordered.verify(bddRunContext).putRunningStory(argThat(runningStory -> runningStory.getStory().equals(story)
                && runningStory.isAllowed() == allowed), eq(givenStory));
        ordered.verify(next).beforeStory(story, givenStory);
    }

    @Test
    void testAfterStory()
    {
        boolean givenStory = false;
        runContextStoryReporter.afterStory(givenStory);
        InOrder ordered = inOrder(bddRunContext, next);
        ordered.verify(next).afterStory(givenStory);
        ordered.verify(bddRunContext).removeRunningStory(givenStory);
    }

    @Test
    void testBeforeStep()
    {
        String step = "step";
        RunningStory runningStory = mockGetRunningStory();
        runContextStoryReporter.beforeStep(step);
        assertEquals(step, runningStory.removeRunningStep());
        verify(next).beforeStep(step);
    }

    @Test
    void testSuccessful()
    {
        String step = "successful";
        RunningStory runningStory = mockGetRunningStory();
        runContextStoryReporter.beforeStep(step);
        runContextStoryReporter.successful(step);
        assertEquals(List.of(), runningStory.getRunningSteps());
        verify(next).successful(step);
    }

    @Test
    void testFailed()
    {
        String step = "failed";
        Throwable cause = mock(Throwable.class);
        RunningStory runningStory = mockGetRunningStory();
        runContextStoryReporter.beforeStep(step);
        runContextStoryReporter.failed(step, cause);
        assertEquals(List.of(), runningStory.getRunningSteps());
        verify(next).failed(step, cause);
    }

    @Test
    void testScenarioExamplesTable()
    {
        RunningScenario runningScenario = spy(RunningScenario.class);
        runningScenario.setScenario(scenario);
        RunningStory runningStory = mockGetRunningStory();
        runningStory.setRunningScenario(runningScenario);
        Map<String, String> tableRow = new HashMap<>();
        when(scenario.getTitle()).thenReturn(SCENARIO_TITLE);
        runContextStoryReporter.example(tableRow, 1);
        assertEquals(1, runningScenario.getIndex());
        assertEquals(String.format(TITLE_PATTERN, SCENARIO_TITLE, 2), runningScenario.getTitle());
        verify(next).example(tableRow, 1);
        verify(runningScenario).setExample(tableRow);
    }

    @Test
    void testStoryLevelExampleOnly()
    {
        RunningScenario runningScenario = new RunningScenario();
        runningScenario.setScenario(scenario);
        RunningStory runningStory = mockGetRunningStory();
        runningStory.setRunningScenario(runningScenario);
        Map<String, String> tableRow = new HashMap<>();
        when(scenario.getTitle()).thenReturn(SCENARIO_TITLE);
        runContextStoryReporter.example(tableRow, -1);
        assertEquals(-1, runningScenario.getIndex());
        assertEquals(SCENARIO_TITLE, runningScenario.getTitle());
        verify(next).example(tableRow, -1);
    }

    @Test
    void testBeforeScenario()
    {
        RunningStory runningStory = mockGetRunningStory();
        Scenario scenario = mock(Scenario.class);
        runContextStoryReporter.beforeScenario(scenario);
        assertEquals(scenario, runningStory.getRunningScenario().getScenario());
        verify(next).beforeScenario(scenario);
    }

    @Test
    void testAfterScenario()
    {
        RunningStory runningStory = mockGetRunningStory();
        Timing timing = mock(Timing.class);
        runContextStoryReporter.afterScenario(timing);
        assertNull(runningStory.getRunningScenario());
        verify(next).afterScenario(timing);
    }

    @Test
    void testStoryExcluded()
    {
        Story story = mock(Story.class);
        String filter = "groovy: !skip";
        runContextStoryReporter.storyExcluded(story, filter);
        verify(bddRunContext).setStoryStatus(story, Status.NOT_ALLOWED);
        verify(next).storyExcluded(story, filter);
    }

    @Test
    void testDryRun()
    {
        runContextStoryReporter.dryRun();
        verify(bddRunContext).setDryRun(true);
    }

    private RunningStory mockGetRunningStory()
    {
        RunningStory runningStory = new RunningStory();
        when(bddRunContext.getRunningStory()).thenReturn(runningStory);
        return runningStory;
    }
}
