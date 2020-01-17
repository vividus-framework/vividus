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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.inOrder;
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
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SubStepsTests
{
    @Mock
    private StoryReporter storyReporter;

    @Mock
    private Step step;

    @Mock
    private Configuration configuration;

    @Mock
    private ISubStepsListener subStepsListener;

    private SubSteps subSteps;

    @BeforeEach
    void beforeEach()
    {
        subSteps = new SubSteps(configuration, storyReporter, List.of(step), subStepsListener);
    }

    @Test
    void shouldExecuteSubStepWithParameters()
    {
        @SuppressWarnings("unchecked")
        Supplier<String> parameterProvider = mock(Supplier.class);
        when(parameterProvider.get()).thenReturn("parameters");
        StepResult stepResult = mock(StepResult.class);
        Step compositeStep = mock(Step.class);
        when(step.getComposedSteps()).thenReturn(List.of(compositeStep, compositeStep));
        when(compositeStep.perform(storyReporter, null)).thenReturn(stepResult);
        InOrder ordered = inOrder(subStepsListener, step, parameterProvider, stepResult);
        when(step.perform(storyReporter, null)).thenReturn(stepResult);
        Keywords keywords = mock(Keywords.class);
        when(configuration.keywords()).thenReturn(keywords);
        when(step.asString(keywords)).thenReturn("step");
        subSteps.execute(Optional.of(parameterProvider));

        ordered.verify(subStepsListener).beforeSubSteps();
        ordered.verify(parameterProvider).get();
        ordered.verify(stepResult).withParameterValues("step [parameters]");
        ordered.verify(stepResult).describeTo(storyReporter);
        ordered.verify(subStepsListener).afterSubSteps();
        ordered.verifyNoMoreInteractions();
    }

    @Test
    void shouldExecuteSubStepWithoutParameters()
    {
        StepResult stepResult = mock(StepResult.class);
        InOrder ordered = inOrder(subStepsListener, step,  stepResult);
        when(step.perform(storyReporter, null)).thenReturn(stepResult);
        subSteps.execute(Optional.empty());

        ordered.verify(subStepsListener).beforeSubSteps();
        ordered.verify(stepResult).describeTo(storyReporter);
        ordered.verify(subStepsListener).afterSubSteps();
        ordered.verifyNoMoreInteractions();
    }

    @Test
    void shouldExecuteSubStepAndThrowWrappedInterruptedException()
    {
        StepResult stepResult = mock(StepResult.class);
        InOrder ordered = inOrder(subStepsListener, step, stepResult);
        when(step.perform(storyReporter, null)).thenReturn(stepResult);
        when(stepResult.getFailure()).thenReturn(new UUIDExceptionWrapper(new InterruptedException()));
        assertThrows(IllegalStateException.class, () -> subSteps.execute(Optional.empty()));

        ordered.verify(subStepsListener).beforeSubSteps();
        ordered.verify(stepResult).describeTo(storyReporter);
        ordered.verify(stepResult).getFailure();
        ordered.verifyNoMoreInteractions();
    }

    @Test
    void shouldExecuteSubStepAndRethrowError()
    {
        StepResult stepResult = mock(StepResult.class);
        InOrder ordered = inOrder(step, stepResult);
        when(step.perform(storyReporter, null)).thenReturn(stepResult);
        AssertionError error = new AssertionError();
        when(stepResult.getFailure()).thenReturn(new UUIDExceptionWrapper(error));
        AssertionError actual = assertThrows(AssertionError.class, () -> subSteps.execute(Optional.empty()));
        assertEquals(error, actual);

        ordered.verify(stepResult).describeTo(storyReporter);
        ordered.verify(stepResult).getFailure();
        ordered.verifyNoMoreInteractions();
    }
}
