/*
 * Copyright 2019-2022 the original author or authors.
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

package org.vividus.steps;

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
import org.jbehave.core.failures.IgnoringStepsFailure;
import org.jbehave.core.failures.PendingStepFound;
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
    private static final Optional<Supplier<String>> EMPTY_STEP_CONTEXT_INFO_PROVIDER = Optional.empty();

    @Mock private StoryReporter storyReporter;
    @Mock private RunContext context;
    @Mock private Configuration configuration;
    @Mock private Keywords keywords;
    @Mock private Embedder embedder;
    private SubSteps subSteps;

    @BeforeEach
    void beforeEach()
    {
        var storyManager = mock(StoryManager.class);
        when(storyManager.getContext()).thenReturn(context);
        when(embedder.storyManager()).thenReturn(storyManager);

        when(context.configuration()).thenReturn(configuration);
        when(configuration.keywords()).thenReturn(keywords);
    }

    @Test
    void testExecuteSubStepWithParameters()
    {
        var stepResult = mock(StepResult.class);
        testExecuteSubSteps(stepResult, step ->
        {
            when(configuration.keywords()).thenReturn(keywords);
            when(step.asString(keywords)).thenReturn("step");

            subSteps.execute(Optional.of(() -> "parameters"));
        }, ordered -> ordered.verify(stepResult).withParameterValues("step [parameters]"));
    }

    @Test
    void testExecuteSubStepWithParametersThrowingException()
    {
        var stepResult = mock(StepResult.class);
        testExecuteSubSteps(stepResult, step ->
        {
            when(configuration.keywords()).thenReturn(keywords);
            when(step.asString(keywords)).thenThrow(new IllegalArgumentException());

            subSteps.execute(Optional.of(() -> "anything"));
        }, ordered -> ordered.verify(stepResult, times(0)).withParameterValues(any()));
    }

    @Test
    void testExecuteSubStepWithoutParameters()
    {
        var stepResult = mock(StepResult.class);
        testExecuteSubSteps(stepResult, step -> subSteps.execute(EMPTY_STEP_CONTEXT_INFO_PROVIDER), ordered -> { });
    }

    @Test
    void testExecuteSubStepsWithFirstFailed()
    {
        var stepResult1 = mock(StepResult.class);
        UUIDExceptionWrapper exception = new UUIDExceptionWrapper(new IllegalArgumentException());
        when(stepResult1.getFailure()).thenReturn(exception);
        var step1 = mock(Step.class);
        when(step1.perform(storyReporter, null)).thenReturn(stepResult1);

        var stepResult2 = mock(StepResult.class);
        var step2 = mock(Step.class);
        when(step2.doNotPerform(storyReporter, null)).thenReturn(stepResult2);

        var state1 = mock(State.class);
        var state2 = mockStepRun(state1, s -> s.perform(storyReporter, null), stepResult1);
        var state3 = mockStepRun(state2, s -> s.doNotPerform(storyReporter, null), stepResult2);
        when(context.state()).thenReturn(state1, state2, state3);

        subSteps = new SubSteps(configuration, storyReporter, embedder, List.of(step1, step2));
        var actual = assertThrows(UUIDExceptionWrapper.class, () -> subSteps.execute(EMPTY_STEP_CONTEXT_INFO_PROVIDER));
        assertEquals(exception, actual);

        var ordered = inOrder(step1, step2, state3, context);
        ordered.verify(step1).perform(storyReporter, null);
        ordered.verify(context).stateIs(state2);
        ordered.verify(step2).doNotPerform(storyReporter, null);
        ordered.verify(context).stateIs(state3);
        ordered.verify(context).state();
        ordered.verify(state3).getFailure();
        ordered.verifyNoMoreInteractions();
    }

    @Test
    void testExecuteSubStepsWithIgnoringStepsFailure()
    {
        var stepResult1 = mock(StepResult.class);
        var step1 = mock(Step.class);
        when(step1.perform(storyReporter, null)).thenReturn(stepResult1);

        var state1 = mock(State.class);
        var state2 = mockStepRun(state1, s -> s.perform(storyReporter, null), stepResult1);
        var ignoringStepsFailure = new IgnoringStepsFailure("scenario fail-fast");
        when(state2.getFailure()).thenReturn(ignoringStepsFailure);
        when(context.state()).thenReturn(state1, state2);

        subSteps = new SubSteps(configuration, storyReporter, embedder, List.of(step1));
        var actual = assertThrows(IgnoringStepsFailure.class, () -> subSteps.execute(EMPTY_STEP_CONTEXT_INFO_PROVIDER));
        assertEquals(ignoringStepsFailure, actual);

        var ordered = inOrder(step1, state2, context);
        ordered.verify(step1).perform(storyReporter, null);
        ordered.verify(context).stateIs(state2);
        ordered.verify(context, times(2)).state();
        ordered.verify(state2).getFailure();
        ordered.verifyNoMoreInteractions();
    }

    @Test
    void testExecuteSubStepInterruptedException()
    {
        var stepResult = mock(StepResult.class);
        testExecuteSubSteps(stepResult, step ->
        {
            when(stepResult.getFailure()).thenReturn(new UUIDExceptionWrapper(new InterruptedException()));
            assertThrows(IllegalStateException.class, () -> subSteps.execute(EMPTY_STEP_CONTEXT_INFO_PROVIDER));
        }, ordered -> { });
    }

    @Test
    void testExecuteSubStepWithError()
    {
        var stepResult = mock(StepResult.class);
        testExecuteSubSteps(stepResult, step ->
        {
            var error = new AssertionError();
            when(stepResult.getFailure()).thenReturn(new UUIDExceptionWrapper(error));
            var actual = assertThrows(AssertionError.class, () -> subSteps.execute(EMPTY_STEP_CONTEXT_INFO_PROVIDER));
            assertEquals(error, actual);
        }, ordered -> { });
    }

    @Test
    void testExecuteSubStepWithAnyException()
    {
        var stepResult = mock(StepResult.class);
        testExecuteSubSteps(stepResult, step ->
        {
            var exception = new UUIDExceptionWrapper(new IllegalArgumentException());
            when(stepResult.getFailure()).thenReturn(exception);
            var actual = assertThrows(UUIDExceptionWrapper.class,
                    () -> subSteps.execute(EMPTY_STEP_CONTEXT_INFO_PROVIDER));
            assertEquals(exception, actual);
        }, ordered -> { });
    }

    @Test
    void testExecuteSubStepWithPendingStepException()
    {
        var stepResult = mock(StepResult.class);
        testExecuteSubSteps(stepResult, step ->
        {
            var pendingStepFound = new PendingStepFound("When I perform pending step");
            when(stepResult.getFailure()).thenReturn(pendingStepFound);
            var actual = assertThrows(UUIDExceptionWrapper.class,
                    () -> subSteps.execute(EMPTY_STEP_CONTEXT_INFO_PROVIDER));
            assertEquals(pendingStepFound, actual.getCause());
        }, ordered -> { });
    }

    private void testExecuteSubSteps(StepResult stepResult, Consumer<Step> test, Consumer<InOrder> stepResultVerifier)
    {
        var step = mock(Step.class);
        when(step.perform(storyReporter, null)).thenReturn(stepResult);

        var state = mock(State.class);
        var newState = mockStepRun(state, s -> s.perform(storyReporter, null), stepResult);
        when(context.state()).thenReturn(state, newState);

        subSteps = new SubSteps(configuration, storyReporter, embedder, List.of(step));
        test.accept(step);

        var ordered = inOrder(step, stepResult, context);
        ordered.verify(step).perform(storyReporter, null);
        stepResultVerifier.accept(ordered);
        ordered.verify(context).stateIs(newState);
    }

    private State mockStepRun(State state, Consumer<Step> stepRunner, StepResult stepResult)
    {
        var newState = mock(State.class);
        doReturn(newState).when(state).run(argThat(s ->
        {
            s.getComposedSteps().forEach(stepRunner);
            stepRunner.accept(s);
            return true;
        }), argThat(results -> results.add(stepResult)), eq(keywords), eq(storyReporter));
        return newState;
    }
}
