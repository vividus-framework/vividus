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

package org.vividus.variable;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

import org.jbehave.core.model.Step;
import org.jbehave.core.model.Story;
import org.jbehave.core.reporters.StoryReporter;
import org.jbehave.core.steps.StepCreator.StepExecutionType;
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
import org.vividus.context.VariableContext;

@ExtendWith(MockitoExtension.class)
class VariableStoryReporterTests
{
    private static final String STEP = "step";

    @Mock private StoryReporter nextStoryReporter;
    @Mock private VariableContext variableContext;
    @InjectMocks private VariableStoryReporter variableStoryReporter;

    @BeforeEach
    void beforeEach()
    {
        variableStoryReporter.setNext(nextStoryReporter);
    }

    @ParameterizedTest
    @CsvSource({
        "false, 1",
        "true,  0"
    })
    void testBeforeStory(boolean givenStory, int numberOfInvocations)
    {
        Story story = mock(Story.class);
        variableStoryReporter.beforeStory(story, givenStory);
        InOrder ordered = inOrder(variableContext, nextStoryReporter);
        ordered.verify(variableContext, times(numberOfInvocations)).initVariables();
        ordered.verify(nextStoryReporter).beforeStory(story, givenStory);
        ordered.verifyNoMoreInteractions();
    }

    @Test
    void testBeforeStep()
    {
        Step step = new Step(StepExecutionType.EXECUTABLE, STEP);
        variableStoryReporter.beforeStep(step);
        InOrder ordered = inOrder(variableContext, nextStoryReporter);
        ordered.verify(variableContext).initStepVariables();
        ordered.verify(nextStoryReporter).beforeStep(step);
        ordered.verifyNoMoreInteractions();
    }

    @Test
    void testSuccessful()
    {
        variableStoryReporter.successful(STEP);
        InOrder ordered = inOrder(variableContext, nextStoryReporter);
        ordered.verify(nextStoryReporter).successful(STEP);
        ordered.verify(variableContext).clearStepVariables();
        ordered.verifyNoMoreInteractions();
    }

    @Test
    void testFailed()
    {
        Throwable cause = mock(Throwable.class);
        variableStoryReporter.failed(STEP, cause);
        InOrder ordered = inOrder(variableContext, nextStoryReporter);
        ordered.verify(nextStoryReporter).failed(STEP, cause);
        ordered.verify(variableContext).clearStepVariables();
        ordered.verifyNoMoreInteractions();
    }

    @Test
    void testAfterScenario()
    {
        Timing timing = mock(Timing.class);
        variableStoryReporter.afterScenario(timing);
        InOrder ordered = inOrder(variableContext, nextStoryReporter);
        ordered.verify(nextStoryReporter).afterScenario(timing);
        ordered.verify(variableContext).clearScenarioVariables();
        ordered.verifyNoMoreInteractions();
    }
}
