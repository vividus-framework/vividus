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

package org.vividus.bdd.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.jbehave.core.embedder.Embedder;
import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.model.ExamplesTableFactory;
import org.jbehave.core.model.Story;
import org.jbehave.core.reporters.ConcurrentStoryReporter;
import org.jbehave.core.reporters.DelegatingStoryReporter;
import org.jbehave.core.reporters.StoryReporter;
import org.jbehave.core.steps.Step;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.bdd.context.IBddRunContext;
import org.vividus.bdd.model.RunningScenario;
import org.vividus.bdd.model.RunningStory;
import org.vividus.bdd.parser.IStepExamplesTableParser;
import org.vividus.bdd.spring.ExtendedConfiguration;
import org.vividus.bdd.steps.SubSteps;

@ExtendWith(MockitoExtension.class)
class SubStepsConverterTests
{
    private static final String STEPS_TO_EXECUTE = "stepsToExecute";

    @Mock private ExtendedConfiguration configuration;
    @Mock private IBddRunContext bddRunContext;
    @Mock private Embedder embedder;
    @Mock private IStepExamplesTableParser stepExamplesTableParser;
    @Mock private RunningStory runningStory;
    @Mock private Step step;
    @InjectMocks private SubStepsConverter subStepsConverter;

    @Test
    void shouldCreateSubSteps() throws IllegalAccessException
    {
        ExamplesTable stepsToExecuteTable = mockStepsToExecute();
        when(bddRunContext.getRunningStory()).thenReturn(runningStory);
        RunningScenario runningScenario = mock(RunningScenario.class);
        Map<String, String> examples = Map.of("key", "value");
        when(runningScenario.getExample()).thenReturn(examples);
        when(runningStory.getRunningScenario()).thenReturn(runningScenario);
        when(stepExamplesTableParser.parse(stepsToExecuteTable, examples)).thenReturn(List.of(step));
        StoryReporter storyReporter = mockStoryReporter();
        SubSteps executor = subStepsConverter.convertValue(STEPS_TO_EXECUTE, SubSteps.class);
        assertResults(storyReporter, executor);
    }

    @Test
    void shouldCreateLifecycleSubStepsPerformedBeforeStory() throws IllegalAccessException
    {
        ExamplesTable stepsToExecuteTable = mockStepsToExecute();
        when(bddRunContext.getRunningStory()).thenReturn(runningStory);
        when(runningStory.getRunningScenario()).thenReturn(null);
        when(stepExamplesTableParser.parse(stepsToExecuteTable, Map.of())).thenReturn(List.of(step));
        StoryReporter storyReporter = mockStoryReporter();
        SubSteps executor = subStepsConverter.convertValue(STEPS_TO_EXECUTE, SubSteps.class);
        assertResults(storyReporter, executor);
    }

    private ExamplesTable mockStepsToExecute()
    {
        ExamplesTableFactory examplesTableFactory = mock(ExamplesTableFactory.class);
        when(configuration.examplesTableFactory()).thenReturn(examplesTableFactory);
        ExamplesTable stepsToExecuteTable = mock(ExamplesTable.class);
        when(examplesTableFactory.createExamplesTable(STEPS_TO_EXECUTE)).thenReturn(stepsToExecuteTable);
        return stepsToExecuteTable;
    }

    private void assertResults(StoryReporter storyReporter, SubSteps executor) throws IllegalAccessException
    {
        assertEquals(List.of(step), getFieldValue("steps", executor));
        DelegatingStoryReporter actualStoryReporter = getFieldValue("storyReporter", executor);
        assertNotEquals(storyReporter, actualStoryReporter);
        assertEquals(1, actualStoryReporter.getDelegates().size());
        assertFalse(actualStoryReporter.getDelegates().iterator().next() instanceof ConcurrentStoryReporter);
        assertEquals(configuration, getFieldValue("configuration", executor));
        assertEquals(embedder, getFieldValue("embedder", executor));
    }

    @SuppressWarnings("unchecked")
    private <T> T getFieldValue(String fieldName, Object objectToExtractValue) throws IllegalAccessException
    {
        return (T) FieldUtils.readField(objectToExtractValue, fieldName, true);
    }

    private StoryReporter mockStoryReporter()
    {
        String storyPath = "path";
        RunningStory runningStory = new RunningStory();
        runningStory.setStory(new Story(storyPath));
        when(bddRunContext.getRootRunningStory()).thenReturn(runningStory);
        DelegatingStoryReporter storyReporter = mock(DelegatingStoryReporter.class);
        List<StoryReporter> delegates = List.of(mock(ConcurrentStoryReporter.class), mock(StoryReporter.class));
        when(storyReporter.getDelegates()).thenReturn(delegates);
        when(configuration.storyReporter(storyPath)).thenReturn(storyReporter);
        return storyReporter;
    }
}
