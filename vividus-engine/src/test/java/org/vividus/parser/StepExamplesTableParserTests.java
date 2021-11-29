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

package org.vividus.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.embedder.MatchingStepMonitor;
import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Story;
import org.jbehave.core.parsers.StoryParser;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.jbehave.core.steps.Step;
import org.jbehave.core.steps.StepCandidate;
import org.jbehave.core.steps.StepCollector;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StepExamplesTableParserTests
{
    private static final String ERROR_MESSAGE_PART = "The steps examples table must have only one column with the name"
        + " 'step', but got ";

    @Mock private Configuration configuration;
    @Mock private InjectableStepsFactory stepsFactory;
    @InjectMocks private StepExamplesTableParser stepExamplesTableParser;

    @Test
    void testMoreThanOneColumnInStepsExampleTable()
    {
        ExamplesTable table = new ExamplesTable("|step|col2|col3|\n|val1|val2|val3|");
        Map<String, String> currentExample = Map.of();
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> stepExamplesTableParser.parse(table, currentExample));
        assertEquals(ERROR_MESSAGE_PART + "step, col2, col3", exception.getMessage());
        verifyNoInteractions(stepsFactory, configuration);
    }

    @Test
    void testGetListStepsNoStepsAsTable()
    {
        Map<String, String> currentExample = Map.of();
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> stepExamplesTableParser.parse(ExamplesTable.EMPTY, currentExample));
        assertEquals(ERROR_MESSAGE_PART + "empty table", exception.getMessage());
        verifyNoInteractions(stepsFactory, configuration);
    }

    @Test
    void testGetListSteps()
    {
        CandidateSteps candidateSteps = mock(CandidateSteps.class);
        List<StepCandidate> regularSteps = List.of(mock(StepCandidate.class));
        when(candidateSteps.listCandidates()).thenReturn(regularSteps);
        when(stepsFactory.createCandidateSteps()).thenReturn(List.of(candidateSteps));

        String step = "When I do something";
        Map<String, String> parameters = Map.of("key", "value");

        Scenario scenario = when(mock(Scenario.class).getSteps()).thenReturn(List.of(step)).getMock();
        Story story = when(mock(Story.class).getScenarios()).thenReturn(List.of(scenario)).getMock();
        StoryParser storyParser = when(mock(StoryParser.class).parseStory(step)).thenReturn(story).getMock();
        when(configuration.storyParser()).thenReturn(storyParser);

        List<Step> steps = List.of(mock(Step.class));
        StepCollector stepCollector = mock(StepCollector.class);
        when(configuration.stepCollector()).thenReturn(stepCollector);
        when(stepCollector.collectScenarioSteps(eq(regularSteps), argThat(arg -> List.of(step).equals(arg.getSteps())),
                eq(parameters), any(MatchingStepMonitor.class))).thenReturn(steps);
        ExamplesTable stepsToExecute = new ExamplesTable("|step|\n|" + step + "|");
        stepExamplesTableParser.parse(stepsToExecute, parameters);
        verify(configuration).storyParser();
        verify(configuration).stepMonitor();
    }
}
