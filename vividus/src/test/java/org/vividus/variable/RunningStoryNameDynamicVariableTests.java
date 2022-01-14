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

package org.vividus.variable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.context.RunContext;
import org.vividus.model.RunningStory;

@ExtendWith(MockitoExtension.class)
class RunningStoryNameDynamicVariableTests
{
    @Mock private RunContext runContext;
    @InjectMocks private RunningStoryNameDynamicVariable dynamicVariable;

    @Test
    void shouldReturnStoryName()
    {
        var runningStory = mock(RunningStory.class);
        when(runContext.getRunningStory()).thenReturn(runningStory);
        var name = "name";
        when(runningStory.getName()).thenReturn(name);
        assertEquals(DynamicVariableCalculationResult.withValue(name), dynamicVariable.calculateValue());
    }

    @Test
    void shouldBeEmptyIfThereIsNoRunningStory()
    {
        when(runContext.getRunningStory()).thenReturn(null);
        assertEquals(DynamicVariableCalculationResult.withError("no story is running"),
                dynamicVariable.calculateValue());
    }
}
