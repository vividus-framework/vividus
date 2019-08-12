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

package org.vividus.bdd;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import org.jbehave.core.reporters.StoryReporter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.bdd.context.IBddVariableContext;
import org.vividus.bdd.variable.VariableScope;

@ExtendWith(MockitoExtension.class)
class BddVariableStoryReporterTests
{
    private static final String STEP = "step";

    @Mock
    private StoryReporter nextStoryReporter;

    @Mock
    private IBddVariableContext bddVariableContext;

    @InjectMocks
    private BddVariableStoryReporter bddVariableStoryReporter;

    @BeforeEach
    void beforeEach()
    {
        bddVariableStoryReporter.setBddVariableContext(bddVariableContext);
        bddVariableStoryReporter.setNext(nextStoryReporter);
    }

    @Test
    void testBeforeStory()
    {
        bddVariableStoryReporter.beforeStory(null, false);
        verify(bddVariableContext).initVariables();
    }

    @Test
    void testOnSubStepsPublishingStart()
    {
        bddVariableStoryReporter.onSubStepsPublishingStart(null);
        bddVariableStoryReporter.successful(STEP);
        verifyZeroInteractions(bddVariableContext);
    }

    @Test
    void testOnSubStepsPublishingFinish()
    {
        bddVariableStoryReporter.onSubStepsPublishingFinish(null);

        bddVariableStoryReporter.successful(STEP);
        verify(bddVariableContext).clearVariables(VariableScope.STEP);
    }

    @Test
    void testBeforeSubSteps()
    {
        bddVariableStoryReporter.beforeSubSteps();
        bddVariableStoryReporter.successful(STEP);
        verifyZeroInteractions(bddVariableContext);
    }

    @Test
    void testAfterSubSteps()
    {
        bddVariableStoryReporter.afterSubSteps();
        bddVariableStoryReporter.successful(STEP);
        verify(bddVariableContext).clearVariables(VariableScope.STEP);
    }

    @Test
    void testSuccessful()
    {
        bddVariableStoryReporter.successful(STEP);
        verify(bddVariableContext).clearVariables(VariableScope.STEP);
        verify(nextStoryReporter).successful(STEP);
    }

    @Test
    void testFailed()
    {
        Throwable cause = mock(Throwable.class);
        bddVariableStoryReporter.failed(STEP, cause);
        verify(bddVariableContext).clearVariables(VariableScope.STEP);
        verify(nextStoryReporter).failed(STEP, cause);
    }

    @Test
    void testIgnorable()
    {
        bddVariableStoryReporter.ignorable(STEP);
        verify(bddVariableContext).clearVariables(VariableScope.STEP);
        verify(nextStoryReporter).ignorable(STEP);
    }

    @Test
    void testPending()
    {
        bddVariableStoryReporter.pending(STEP);
        verify(bddVariableContext).clearVariables(VariableScope.STEP);
        verify(nextStoryReporter).pending(STEP);
    }

    @Test
    void testNotPerformed()
    {
        bddVariableStoryReporter.notPerformed(STEP);
        verify(bddVariableContext).clearVariables(VariableScope.STEP);
        verify(nextStoryReporter).notPerformed(STEP);
    }

    @Test
    void testRestarted()
    {
        Throwable cause = mock(Throwable.class);
        bddVariableStoryReporter.restarted(STEP, cause);
        verify(bddVariableContext).clearVariables(VariableScope.STEP);
        verify(nextStoryReporter).restarted(STEP, cause);
    }
}
