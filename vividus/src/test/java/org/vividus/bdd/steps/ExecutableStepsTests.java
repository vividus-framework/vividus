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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.powermock.api.mockito.PowerMockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.bdd.context.IBddVariableContext;
import org.vividus.bdd.variable.VariableScope;

@ExtendWith(MockitoExtension.class)
class ExecutableStepsTests
{
    private static final String KEY = "key";
    private static final String ITERATION_VARIABLE = "iterationVariable";

    @Mock
    private IBddVariableContext bddVariableContext;

    @Mock
    private SubSteps subSteps;

    @InjectMocks
    private ExecutableSteps executableSteps;

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
    void shouldRunStepsIfVariableIsNotSet()
    {
        executableSteps.ifVariableNotSetPerformSteps(KEY, subSteps);
        verify(subSteps).execute(Optional.empty());
    }

    @Test
    void shouldNotRunStepsIfVariableIsSet()
    {
        when(bddVariableContext.getVariable(KEY)).thenReturn("value");
        executableSteps.ifVariableNotSetPerformSteps(KEY, subSteps);
        verifyNoInteractions(subSteps);
    }

    @Test
    void shouldPerformTheStepsAndWhenVariableValueFitsExit()
    {
        String three = "3";
        when(bddVariableContext.getVariable(KEY)).thenReturn(null).thenReturn("1").thenReturn("2").thenReturn(three);
        executableSteps.executeStepsWhile(10, KEY, ComparisonRule.LESS_THAN, three, subSteps);
        verify(subSteps, times(3)).execute(Optional.empty());
    }

    @Test
    void shouldPerformTheStepsAndExitWhenLimitReached()
    {
        executableSteps.executeStepsWhile(10, KEY, ComparisonRule.LESS_THAN, KEY, subSteps);
        verify(subSteps, times(10)).execute(Optional.empty());
    }

    @Test
    void testPerformStepsNumberTimes()
    {
        executableSteps.performStepsNumberTimes(2, subSteps);
        verify(subSteps, times(2)).execute(Optional.empty());
    }

    @ParameterizedTest
    @ValueSource(ints = { -1, 51 })
    void testPerformStepsNumberTimesWrongExecutionsNumber(int number)
    {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> executableSteps.performStepsNumberTimes(number, subSteps));
        assertEquals("Please, specify executions number in the range from 0 to 50", exception.getMessage());
        verifyNoInteractions(subSteps);
    }

    @Test
    void testExecuteStepsWhileConditionIsTrueWithStep()
    {
        executableSteps.executeStepsWhileConditionIsTrueWithStep(ComparisonRule.LESS_THAN, 5, 2, 1, subSteps);
        verify(subSteps, times(2)).execute(Optional.empty());
        verify(bddVariableContext).putVariable(VariableScope.STEP, ITERATION_VARIABLE, 1);
        verify(bddVariableContext).putVariable(VariableScope.STEP, ITERATION_VARIABLE, 3);
        verifyNoMoreInteractions(bddVariableContext);
    }

    @Test
    void testExecuteStepsWhileConditionIsTrueWithStepException()
    {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> executableSteps
                .executeStepsWhileConditionIsTrueWithStep(ComparisonRule.LESS_THAN, 5, -2, 1, subSteps));
        assertEquals("Number of iterations has exceeded allowable limit 50", exception.getMessage());
        verifyNoInteractions(bddVariableContext, subSteps);
    }
}
