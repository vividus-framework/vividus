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

package org.vividus.bdd.variable;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

import org.jbehave.core.model.Story;
import org.jbehave.core.reporters.StoryReporter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.bdd.context.IBddVariableContext;

@ExtendWith(MockitoExtension.class)
class VariableStoryReporterTests
{
    private static final String STEP = "step";

    @Mock private StoryReporter nextStoryReporter;
    @Mock private IBddVariableContext bddVariableContext;
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
        InOrder ordered = inOrder(bddVariableContext, nextStoryReporter);
        ordered.verify(bddVariableContext, times(numberOfInvocations)).initVariables();
        ordered.verify(nextStoryReporter).beforeStory(story, givenStory);
        ordered.verifyNoMoreInteractions();
    }

    @Test
    void testBeforeStep()
    {
        variableStoryReporter.beforeStep(STEP);
        InOrder ordered = inOrder(bddVariableContext, nextStoryReporter);
        ordered.verify(bddVariableContext).initStepVariables();
        ordered.verify(nextStoryReporter).beforeStep(STEP);
        ordered.verifyNoMoreInteractions();
    }

    @Test
    void testSuccessful()
    {
        variableStoryReporter.successful(STEP);
        InOrder ordered = inOrder(bddVariableContext, nextStoryReporter);
        ordered.verify(nextStoryReporter).successful(STEP);
        ordered.verify(bddVariableContext).clearStepVariables();
        ordered.verifyNoMoreInteractions();
    }

    @Test
    void testFailed()
    {
        Throwable cause = mock(Throwable.class);
        variableStoryReporter.failed(STEP, cause);
        InOrder ordered = inOrder(bddVariableContext, nextStoryReporter);
        ordered.verify(nextStoryReporter).failed(STEP, cause);
        ordered.verify(bddVariableContext).clearStepVariables();
        ordered.verifyNoMoreInteractions();
    }

    @Test
    void testAfterScenario()
    {
        variableStoryReporter.afterScenario();
        InOrder ordered = inOrder(bddVariableContext, nextStoryReporter);
        ordered.verify(nextStoryReporter).afterScenario();
        ordered.verify(bddVariableContext).clearScenarioVariables();
        ordered.verifyNoMoreInteractions();
    }
}
