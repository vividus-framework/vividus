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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayDeque;
import java.util.Deque;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.bdd.context.IBddRunContext;
import org.vividus.bdd.model.RunningScenario;
import org.vividus.bdd.model.RunningStory;
import org.vividus.softassert.ISoftAssert;

@ExtendWith(MockitoExtension.class)
class AssertStepsTests
{
    @Mock
    private ISoftAssert softAssert;

    @Mock
    private IBddRunContext bddRunContext;

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

    private void mockStoriesChain(RunningStory runningStory, RunningStory rootRunningStory)
    {
        Deque<RunningStory> storiesChain = new ArrayDeque<>();
        storiesChain.add(rootRunningStory);
        storiesChain.addFirst(runningStory);
        when(bddRunContext.getStoriesChain()).thenReturn(storiesChain);
        when(bddRunContext.getRunningStory()).thenReturn(runningStory);
        when(bddRunContext.getRootRunningStory()).thenReturn(rootRunningStory);
    }
}
