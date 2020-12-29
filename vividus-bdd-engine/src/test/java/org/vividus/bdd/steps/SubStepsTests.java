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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.embedder.Embedder;
import org.jbehave.core.embedder.PerformableTree.RunContext;
import org.jbehave.core.embedder.PerformableTree.State;
import org.jbehave.core.embedder.StoryManager;
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
    @Mock private StoryReporter storyReporter;
    @Mock private RunContext context;
    @Mock private Configuration configuration;
    @Mock private Embedder embedder;
    private SubSteps subSteps;

    @BeforeEach
    void beforeEach()
    {
        StoryManager storyManager = mock(StoryManager.class);
        when(storyManager.getContext()).thenReturn(context);
        when(embedder.storyManager()).thenReturn(storyManager);
    }

    @Test
    void testExecuteSubStepWithParameters()
    {
        StepResult stepResult = mock(StepResult.class);
        testExecuteSubSteps(stepResult, step ->
        {
            Keywords keywords = mock(Keywords.class);
            when(configuration.keywords()).thenReturn(keywords);
            when(step.asString(keywords)).thenReturn("step");

            subSteps.execute(Optional.of(() -> "parameters"));
        }, ordered -> ordered.verify(stepResult).withParameterValues("step [parameters]"));
    }

    @Test
    void testExecuteSubStepWithParametersThrowingException()
    {
        StepResult stepResult = mock(StepResult.class);
        testExecuteSubSteps(stepResult, step ->
        {
            Keywords keywords = mock(Keywords.class);
            when(configuration.keywords()).thenReturn(keywords);
            when(step.asString(keywords)).thenThrow(new IllegalArgumentException());

            subSteps.execute(Optional.of(() -> "anything"));
        }, ordered -> ordered.verify(stepResult, times(0)).withParameterValues(any()));
    }

    @Test
    void testExecuteSubStepWithoutParameters()
    {
        StepResult stepResult = mock(StepResult.class);
        testExecuteSubSteps(stepResult, step -> subSteps.execute(Optional.empty()), ordered -> { });
    }

    @Test
    void testExecuteSubStepsWithFirstFailed()
    {
        StepResult stepResult1 = mock(StepResult.class);
        UUIDExceptionWrapper exception = new UUIDExceptionWrapper(new IllegalArgumentException());
        when(stepResult1.getFailure()).thenReturn(exception);
        Step step1 = mock(Step.class);
        when(step1.perform(storyReporter, null)).thenReturn(stepResult1);

        StepResult stepResult2 = mock(StepResult.class);
        Step step2 = mock(Step.class);
        when(step2.doNotPerform(storyReporter, null)).thenReturn(stepResult2);

        State state1 = mock(State.class);
        State state2 = mockStepRun(state1, s -> s.perform(storyReporter, null), stepResult1);
        State state3 = mockStepRun(state2, s -> s.doNotPerform(storyReporter, null), stepResult2);
        when(context.state()).thenReturn(state1, state2, state3);

        subSteps = new SubSteps(configuration, storyReporter, embedder, List.of(step1, step2));
        Optional<Supplier<String>> stepContextInfoProvider = Optional.empty();
        UUIDExceptionWrapper actual = assertThrows(UUIDExceptionWrapper.class,
                () -> subSteps.execute(stepContextInfoProvider));
        assertEquals(exception, actual);

        InOrder ordered = inOrder(step1, step2, context);
        ordered.verify(step1).perform(storyReporter, null);
        ordered.verify(context).stateIs(state2);
        ordered.verify(step2).doNotPerform(storyReporter, null);
        ordered.verify(context).stateIs(state3);
        ordered.verifyNoMoreInteractions();
    }

    @Test
    void testExecuteSubStepInterruptedException()
    {
        StepResult stepResult = mock(StepResult.class);
        testExecuteSubSteps(stepResult, step ->
        {
            when(stepResult.getFailure()).thenReturn(new UUIDExceptionWrapper(new InterruptedException()));
            assertThrows(IllegalStateException.class, () -> subSteps.execute(Optional.empty()));
        }, ordered -> { });
    }

    @Test
    void testExecuteSubStepWithError()
    {
        StepResult stepResult = mock(StepResult.class);
        testExecuteSubSteps(stepResult, step ->
        {
            AssertionError error = new AssertionError();
            when(stepResult.getFailure()).thenReturn(new UUIDExceptionWrapper(error));
            AssertionError actual = assertThrows(AssertionError.class, () -> subSteps.execute(Optional.empty()));
            assertEquals(error, actual);
        }, ordered -> { });
    }

    @Test
    void testExecuteSubStepWithAnyException()
    {
        StepResult stepResult = mock(StepResult.class);
        testExecuteSubSteps(stepResult, step ->
        {
            UUIDExceptionWrapper exception = new UUIDExceptionWrapper(new IllegalArgumentException());
            when(stepResult.getFailure()).thenReturn(exception);
            Optional<Supplier<String>> stepContextInfoProvider = Optional.empty();
            UUIDExceptionWrapper actual = assertThrows(UUIDExceptionWrapper.class,
                    () -> subSteps.execute(stepContextInfoProvider));
            assertEquals(exception, actual);
        }, ordered -> { });
    }

    private void testExecuteSubSteps(StepResult stepResult, Consumer<Step> test, Consumer<InOrder> stepResultVerifier)
    {
        Step step = mock(Step.class);
        when(step.perform(storyReporter, null)).thenReturn(stepResult);

        State state = mock(State.class);
        State newState = mockStepRun(state, s -> s.perform(storyReporter, null), stepResult);
        when(context.state()).thenReturn(state, newState);

        subSteps = new SubSteps(configuration, storyReporter, embedder, List.of(step));
        test.accept(step);

        InOrder ordered = inOrder(step, stepResult, context);
        ordered.verify(step).perform(storyReporter, null);
        stepResultVerifier.accept(ordered);
        ordered.verify(context).stateIs(newState);
    }

    private State mockStepRun(State state, Consumer<Step> stepRunner, StepResult stepResult)
    {
        State newState = mock(State.class);
        doReturn(newState).when(state).run(argThat(s ->
        {
            s.getComposedSteps().forEach(stepRunner);
            stepRunner.accept(s);
            return true;
        }), argThat(results -> results.add(stepResult)), eq(storyReporter));
        return newState;
    }
}
