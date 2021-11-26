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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.context.RunContext;
import org.vividus.model.RunningScenario;
import org.vividus.model.RunningStory;
import org.vividus.softassert.ISoftAssert;

@ExtendWith(MockitoExtension.class)
class AssertStepsTests
{
    @Mock
    private ISoftAssert softAssert;

    @Mock
    private RunContext runContext;

    @Mock
    private RunningStory runningStory;

    @Mock
    private RunningStory rootRunningStory;

    @InjectMocks
    private AssertSteps assertSteps;

    @Test
    void testVerifyIfAssertionsPassed()
    {
        mockStoriesChain(runningStory, runningStory);
        assertSteps.verifyIfAssertionsPassed();
        verify(softAssert).verify();
    }

    @Test
    void testVerifyIfAssertionsPassedGivenOnStory()
    {
        mockStoriesChain(runningStory, rootRunningStory);
        assertSteps.verifyIfAssertionsPassed();
        verify(softAssert).verify();
    }

    @Test
    void testVerifyIfAssertionsPassedGivenOnScenario()
    {
        mockStoriesChain(runningStory, rootRunningStory);
        when(rootRunningStory.getRunningScenario()).thenReturn(mock(RunningScenario.class));
        assertSteps.verifyIfAssertionsPassed();
        verify(softAssert, never()).verify();
    }

    @Test
    void shouldVerifyAssertionUsingPattern()
    {
        Pattern compile = Pattern.compile(".+");
        assertSteps.verifyIfAssertionMatcherPatter(compile);
        verify(softAssert).verify(compile);
    }

    private void mockStoriesChain(RunningStory runningStory, RunningStory rootRunningStory)
    {
        Deque<RunningStory> storiesChain = new ArrayDeque<>();
        storiesChain.add(rootRunningStory);
        storiesChain.addFirst(runningStory);
        when(runContext.getStoriesChain()).thenReturn(storiesChain);
        when(runContext.getRunningStory()).thenReturn(runningStory);
        when(runContext.getRootRunningStory()).thenReturn(rootRunningStory);
    }
}
