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

package org.vividus.bdd.parser;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jbehave.core.embedder.MatchingStepMonitor;
import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Story;
import org.jbehave.core.parsers.StoryParser;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.jbehave.core.steps.Step;
import org.jbehave.core.steps.StepCollector;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.bdd.ExtendedEmbedder;
import org.vividus.bdd.spring.Configuration;

@ExtendWith(MockitoExtension.class)
class StepExamplesTableParserTests
{
    private static final String STEP = "When I do something";

    @Mock
    private Configuration configuration;

    @Mock
    private ExtendedEmbedder embedder;

    @InjectMocks
    private StepExamplesTableParser stepExamplesTableParser;

    @Test
    void testGetListStepsNoStepsAsTable()
    {
        mockStepCreation(arg -> arg.getSteps().isEmpty());
        stepExamplesTableParser.parse(new ExamplesTable(""));
        verify(configuration, never()).storyParser();
        verify(configuration).stepMonitor();
    }

    @Test
    void testGetListSteps()
    {
        StoryParser storyParser = mock(StoryParser.class);
        Story story = mock(Story.class);
        Scenario scenario = mock(Scenario.class);
        when(configuration.storyParser()).thenReturn(storyParser);
        when(storyParser.parseStory(STEP)).thenReturn(story);
        when(story.getScenarios()).thenReturn(List.of(scenario));
        when(scenario.getSteps()).thenReturn(List.of(STEP));
        mockStepCreation(arg ->
        {
            List<String> scenarioSteps = arg.getSteps();
            return scenarioSteps.size() == 1 && STEP.equals(scenarioSteps.get(0));
        });
        ExamplesTable stepsToExecute = newStepExamplesTable(STEP);
        stepExamplesTableParser.parse(stepsToExecute);
        verify(configuration).storyParser();
        verify(configuration).stepMonitor();
    }

    private void mockStepCreation(ArgumentMatcher<Scenario> matcher)
    {
        Step step = mock(Step.class);
        List<Step> steps = List.of(step);
        when(mockStepCollector().collectScenarioSteps(eq(mockCandidateSteps()), argThat(matcher), eq(Map.of()),
                any(MatchingStepMonitor.class))).thenReturn(steps);

    }

    private ExamplesTable newStepExamplesTable(String stepAsString)
    {
        return new ExamplesTable("|step|\n|" + stepAsString + "|");
    }

    private StepCollector mockStepCollector()
    {
        StepCollector stepCollector = mock(StepCollector.class);
        when(configuration.stepCollector()).thenReturn(stepCollector);
        return stepCollector;
    }

    private List<CandidateSteps> mockCandidateSteps()
    {
        InjectableStepsFactory stepsFactory = mock(InjectableStepsFactory.class);
        when(embedder.stepsFactory()).thenReturn(stepsFactory);
        List<CandidateSteps> candidateSteps = new ArrayList<>();
        when(stepsFactory.createCandidateSteps()).thenReturn(candidateSteps);
        return candidateSteps;
    }
}
