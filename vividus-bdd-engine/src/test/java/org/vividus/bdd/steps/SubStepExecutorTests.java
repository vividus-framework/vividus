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

package org.vividus.bdd.steps;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.failures.UUIDExceptionWrapper;
import org.jbehave.core.reporters.StoryReporter;
import org.jbehave.core.steps.Step;
import org.jbehave.core.steps.StepResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SubStepExecutorTests
{
    private static final String PARAMETERS = "parameters";

    @Mock
    private StoryReporter storyReporter;
    @Mock
    private Step step;
    @Mock
    private Configuration configuration;
    @Mock
    private ISubStepsListener subStepsListener;

    private SubStepExecutor subStepExecutor;

    @BeforeEach
    void beforeEach()
    {
        subStepExecutor = new SubStepExecutor(configuration, storyReporter,
                List.of(step), subStepsListener);
    }

    @Test
    void testExecuteSubStepWithParameters()
    {
        @SuppressWarnings("unchecked")
        Supplier<String> parameterProvider = mock(Supplier.class);
        when(parameterProvider.get()).thenReturn(PARAMETERS);
        StepResult stepResult = mock(StepResult.class);
        Step compositeStep = mock(Step.class);
        when(step.getComposedSteps()).thenReturn(List.of(compositeStep, compositeStep));
        when(compositeStep.perform(storyReporter, null)).thenReturn(stepResult);
        InOrder inOrder = Mockito.inOrder(subStepsListener, step, parameterProvider, stepResult);
        when(step.perform(storyReporter, null)).thenReturn(stepResult);
        Keywords keywords = mock(Keywords.class);
        when(configuration.keywords()).thenReturn(keywords);
        when(step.asString(keywords)).thenReturn("step");
        subStepExecutor.execute(Optional.of(parameterProvider));

        inOrder.verify(subStepsListener).beforeSubSteps();
        inOrder.verify(parameterProvider).get();
        inOrder.verify(stepResult).withParameterValues("step [parameters]");
        inOrder.verify(stepResult).describeTo(storyReporter);
        inOrder.verify(subStepsListener).afterSubSteps();
    }

    @Test
    void testExecuteSubStepWithoutParameters()
    {
        StepResult stepResult = mock(StepResult.class);
        InOrder inOrder = Mockito.inOrder(subStepsListener, step,  stepResult);
        when(step.perform(storyReporter, null)).thenReturn(stepResult);
        subStepExecutor.execute(Optional.empty());

        inOrder.verify(subStepsListener).beforeSubSteps();
        inOrder.verify(stepResult).describeTo(storyReporter);
        inOrder.verify(subStepsListener).afterSubSteps();
    }

    @Test
    void testExecuteSubStepInterruptedException()
    {
        StepResult stepResult = mock(StepResult.class);
        InOrder inOrder = Mockito.inOrder(subStepsListener, step, stepResult);
        when(step.perform(storyReporter, null)).thenReturn(stepResult);
        when(stepResult.getFailure()).thenReturn(new UUIDExceptionWrapper(new InterruptedException()));
        assertThrows(IllegalStateException.class, () -> subStepExecutor.execute(Optional.empty()));

        inOrder.verify(subStepsListener).beforeSubSteps();
        inOrder.verify(stepResult).describeTo(storyReporter);
        Mockito.verifyNoMoreInteractions(subStepsListener);
    }
}
