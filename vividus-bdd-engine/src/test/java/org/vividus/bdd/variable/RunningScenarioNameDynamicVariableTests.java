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

package org.vividus.bdd.variable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.bdd.context.IBddRunContext;
import org.vividus.bdd.model.RunningScenario;
import org.vividus.bdd.model.RunningStory;

@ExtendWith(MockitoExtension.class)
class RunningScenarioNameDynamicVariableTests
{
    @Mock private IBddRunContext bddRunContext;
    @Mock private RunningStory runningStory;
    @InjectMocks private RunningScenarioNameDynamicVariable runningScenarioNameDynamicVariable;

    @Test
    void shouldReturnStoryName()
    {
        when(bddRunContext.getRunningStory()).thenReturn(runningStory);
        String value = "name";
        RunningScenario runningScenario = mock(RunningScenario.class);
        when(runningStory.getRunningScenario()).thenReturn(runningScenario);
        when(runningScenario.getTitle()).thenReturn(value);
        assertEquals(value, runningScenarioNameDynamicVariable.getValue());
    }

    @Test
    void shouldFailIfThereIsNoRunningScenario()
    {
        when(bddRunContext.getRunningStory()).thenReturn(runningStory);
        when(runningStory.getRunningScenario()).thenReturn(null);

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                runningScenarioNameDynamicVariable::getValue);
        assertEquals("No scenario is running", exception.getMessage());
    }
}
