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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.List;

import org.jbehave.core.model.ExamplesTable;
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
import org.springframework.util.ReflectionUtils;
import org.vividus.bdd.context.IBddRunContext;
import org.vividus.bdd.model.RunningStory;
import org.vividus.bdd.parser.IStepExamplesTableParser;
import org.vividus.bdd.spring.Configuration;

@ExtendWith(MockitoExtension.class)
class SubStepExecutorFactoryTest
{
    @Mock
    private Configuration configuration;
    @Mock
    private IBddRunContext bddRunContext;
    @Mock
    private ISubStepsListener subStepsListener;
    @Mock
    private IStepExamplesTableParser stepExamplesTableParser;
    @Mock
    private ExamplesTable stepsToExecute;

    @InjectMocks
    private SubStepExecutorFactory subStepExecutorFactory;

    @Test
    void testCreateSubStepExecutor()
    {
        Step step = mock(Step.class);
        List<Step> steps = List.of(step);
        when(stepExamplesTableParser.parse(stepsToExecute)).thenReturn(steps);
        StoryReporter storyReporter = mockStoryReporter();
        SubStepExecutor executor = subStepExecutorFactory.createSubStepExecutor(stepsToExecute);
        assertEquals(steps, getFieldValue("steps", executor));
        DelegatingStoryReporter actualStoryReporter = getFieldValue("storyReporter", executor);
        assertNotEquals(storyReporter, actualStoryReporter);
        assertEquals(1, actualStoryReporter.getDelegates().size());
        assertFalse(actualStoryReporter.getDelegates().iterator().next() instanceof ConcurrentStoryReporter);
        assertEquals(configuration, getFieldValue("configuration", executor));
        assertEquals(subStepsListener, getFieldValue("subStepsListener", executor));
    }

    @SuppressWarnings("unchecked")
    private <T> T getFieldValue(String fieldName, Object objectToExtractValue)
    {
        Field field = ReflectionUtils.findField(SubStepExecutor.class, fieldName);
        field.setAccessible(true);
        return (T) ReflectionUtils.getField(field, objectToExtractValue);
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
