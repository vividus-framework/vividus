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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.powermock.api.mockito.PowerMockito.when;

import java.util.Optional;

import org.jbehave.core.model.ExamplesTable;
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
    private ISubStepExecutorFactory subStepExecutorFactory;

    @Mock
    private IBddVariableContext bddVariableContext;

    @Mock
    private ExamplesTable examplesTable;

    @InjectMocks
    private ExecutableSteps executableSteps;

    @Test
    void testTrue()
    {
        ISubStepExecutor subStepExecutor = mockSubStepExecutor(examplesTable);
        executableSteps.performAllStepsIfConditionIsTrue(true, examplesTable);
        verify(subStepExecutor).execute(Optional.empty());
    }

    @Test
    void testFalse()
    {
        executableSteps.performAllStepsIfConditionIsTrue(false, examplesTable);
        verifyNoInteractions(subStepExecutorFactory);
    }

    @Test
    void shouldRunStepsIfVariableIsNotSet()
    {
        ISubStepExecutor executor = mockSubStepExecutor(examplesTable);
        executableSteps.ifVariableNotSetPerformSteps(KEY, examplesTable);
        verify(executor).execute(Optional.empty());
    }

    @Test
    void shouldNotRunStepsIfVariableIsSet()
    {
        when(bddVariableContext.getVariable(KEY)).thenReturn("value");
        executableSteps.ifVariableNotSetPerformSteps(KEY, examplesTable);
        verifyNoInteractions(subStepExecutorFactory);
    }

    @Test
    void testPerformStepsNumberTimes()
    {
        ISubStepExecutor executor = mockSubStepExecutor(examplesTable);
        executableSteps.performStepsNumberTimes(2, examplesTable);
        verify(executor, times(2)).execute(Optional.empty());
    }

    @ParameterizedTest
    @ValueSource(ints = { -1, 51 })
    void testPerformStepsNumberTimesWrongExecutionsNumber(int number)
    {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> executableSteps.performStepsNumberTimes(number, examplesTable));
        assertEquals("Please, specify executions number in the range from 0 to 50", exception.getMessage());
    }

    @Test
    void testExecuteStepsWhileConditionIsTrueWithStep()
    {
        ISubStepExecutor executor = mockSubStepExecutor(examplesTable);
        executableSteps.executeStepsWhileConditionIsTrueWithStep(ComparisonRule.LESS_THAN, 5, 2, 1, examplesTable);
        verify(executor, times(2)).execute(Optional.empty());
        verify(bddVariableContext).putVariable(VariableScope.STEP, ITERATION_VARIABLE, 1);
        verify(bddVariableContext).putVariable(VariableScope.STEP, ITERATION_VARIABLE, 3);
        verifyNoMoreInteractions(bddVariableContext);
    }

    @Test
    void testExecuteStepsWhileConditionIsTrueWithStepException()
    {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> executableSteps
                .executeStepsWhileConditionIsTrueWithStep(ComparisonRule.LESS_THAN, 5, -2, 1, examplesTable));
        assertEquals("Number of iterations has exceeded allowable limit 50", exception.getMessage());
        verifyNoInteractions(bddVariableContext, examplesTable, subStepExecutorFactory);
    }

    private ISubStepExecutor mockSubStepExecutor(ExamplesTable table)
    {
        ISubStepExecutor executor = mock(ISubStepExecutor.class);
        when(subStepExecutorFactory.createSubStepExecutor(table)).thenReturn(executor);
        return executor;
    }
}
