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

package org.vividus.steps;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.context.VariableContext;
import org.vividus.variable.VariableScope;

@ExtendWith(MockitoExtension.class)
class ExecutableStepsTests
{
    private static final String KEY = "key";
    private static final String ITERATION_VARIABLE = "iterationVariable";
    private static final String ONE = "1";
    private static final String THREE = "3";
    private static final String X = "x";
    private static final String Y = "y";

    @Mock private VariableContext variableContext;
    @Mock private SubSteps subSteps;
    @InjectMocks private ExecutableSteps executableSteps;

    @Test
    void testTrue()
    {
        executableSteps.performAllStepsIfConditionIsTrue(true, subSteps);
        verify(subSteps).execute(Optional.empty());
    }

    @Test
    void testFalse()
    {
        executableSteps.performAllStepsIfConditionIsTrue(false, subSteps);
        verifyNoInteractions(subSteps);
    }

    @Test
    void shouldPerformAllStepsUnconditionally()
    {
        executableSteps.performAllStepsUnconditionally(subSteps);
        verify(subSteps).execute(Optional.empty());
    }

    @Test
    void shouldRunStepsIfVariableIsNotSet()
    {
        executableSteps.ifVariableNotSetPerformSteps(KEY, subSteps);
        verify(subSteps).execute(Optional.empty());
    }

    @Test
    void shouldNotRunStepsIfVariableIsSet()
    {
        when(variableContext.getVariable(KEY)).thenReturn("value");
        executableSteps.ifVariableNotSetPerformSteps(KEY, subSteps);
        verifyNoInteractions(subSteps);
    }

    @Test
    void shouldPerformTheStepsAndStopWhenSimpleVariableValueMatches()
    {
        when(variableContext.getVariable(KEY)).thenReturn(null).thenReturn(ONE).thenReturn(2).thenReturn(THREE);
        executableSteps.executeStepsWhile(10, KEY, ComparisonRule.LESS_THAN, 3, subSteps);
        verify(subSteps, times(3)).execute(Optional.empty());
    }

    @Test
    void shouldPerformTheStepsAndStopWhenMapVariableValueMatches()
    {
        Map<String, String> expectedValue = Map.of(X, Y);
        when(variableContext.getVariable(KEY)).thenReturn(List.of(expectedValue)).thenReturn(List.of());
        executableSteps.executeStepsWhile(3, KEY, ComparisonRule.EQUAL_TO, List.of(expectedValue), subSteps);
        verify(subSteps, times(1)).execute(Optional.empty());
    }

    @Test
    void shouldPerformTheStepsAndStopWhenLimitReached()
    {
        executableSteps.executeStepsWhile(10, KEY, ComparisonRule.LESS_THAN, 1, subSteps);
        verify(subSteps, times(10)).execute(Optional.empty());
    }

    @Test
    void shouldPerformTheStepsWithPollingIntervalAndStopWhenSimpleVariableValueMatches()
    {
        when(variableContext.getVariable(KEY)).thenReturn(null).thenReturn(ONE).thenReturn(2).thenReturn(THREE);
        executableSteps.executeStepsWithPollingInterval(Duration.ZERO, 10, KEY, ComparisonRule.LESS_THAN, 3, subSteps);
        verify(subSteps, times(3)).execute(Optional.empty());
    }

    @Test
    void shouldPerformTheStepsWithPollingIntervalAndStopWhenMapVariableValueMatches()
    {
        Map<String, String> expectedValue = Map.of(X, Y);
        when(variableContext.getVariable(KEY)).thenReturn(List.of(expectedValue)).thenReturn(List.of());
        executableSteps.executeStepsWithPollingInterval(Duration.ZERO, 3, KEY, ComparisonRule.EQUAL_TO,
                List.of(expectedValue), subSteps);
        verify(subSteps, times(1)).execute(Optional.empty());
    }

    @Test
    void shouldPerformTheStepsWithPollingIntervalAndStopWhenLimitReached()
    {
        executableSteps.executeStepsWithPollingInterval(Duration.ZERO, 10, KEY, ComparisonRule.LESS_THAN, 1, subSteps);
        verify(subSteps, times(10)).execute(Optional.empty());
    }

    @Test
    void testPerformStepsNumberTimes()
    {
        executableSteps.performStepsNumberTimes(2, subSteps);
        verify(subSteps, times(2)).execute(Optional.empty());
    }

    @ParameterizedTest
    @ValueSource(ints = { -1, 1001 })
    void testPerformStepsNumberTimesWrongExecutionsNumber(int number)
    {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> executableSteps.performStepsNumberTimes(number, subSteps));
        assertEquals("Please, specify executions number in the range from 0 to 1000", exception.getMessage());
        verifyNoInteractions(subSteps);
    }

    @Test
    void testExecuteStepsWhileConditionIsTrueWithStep()
    {
        executableSteps.executeStepsWhileConditionIsTrueWithStep(ComparisonRule.LESS_THAN, 5, 2, 1, subSteps);
        verify(subSteps, times(2)).execute(Optional.empty());
        verify(variableContext).putVariable(VariableScope.STEP, ITERATION_VARIABLE, 1);
        verify(variableContext).putVariable(VariableScope.STEP, ITERATION_VARIABLE, 3);
        verifyNoMoreInteractions(variableContext);
    }

    @Test
    void testExecuteStepsWhileConditionIsTrueWithStepException()
    {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> executableSteps
                .executeStepsWhileConditionIsTrueWithStep(ComparisonRule.LESS_THAN, 5, -2, 1, subSteps));
        assertEquals("Number of iterations has exceeded allowable limit 1000", exception.getMessage());
        verifyNoInteractions(variableContext, subSteps);
    }
}
