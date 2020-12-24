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

package org.vividus.softassert;

import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.google.common.eventbus.EventBus;

import org.jbehave.core.embedder.StoryControls;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.softassert.event.AssertionFailedEvent;
import org.vividus.softassert.model.SoftAssertionError;

@ExtendWith(MockitoExtension.class)
class AssertionManagerTests
{
    @Mock private ISoftAssert softAssert;
    @Mock private StoryControls storyControls;

    private AssertionManager assertionManager;

    @BeforeEach
    void beforeEach()
    {
        EventBus eventBus = new EventBus();
        assertionManager = new AssertionManager(eventBus, softAssert, storyControls);
        eventBus.register(assertionManager);
    }

    @Test
    void shouldNotFailFast()
    {
        assertionManager.setFailFast(false);
        assertionManager.onAssertionFailure(createEventWithError(false, false, false));
        verifyNoInteractions(softAssert);
    }

    @Test
    void shouldNotFailFastIfErrorIsKnownIssue()
    {
        assertionManager.setFailFast(true);
        assertionManager.onAssertionFailure(createEventWithError(true, false, false));
        verifyNoInteractions(softAssert);
    }

    @Test
    void shouldFailFastIfErrorIsNotKnownIssue()
    {
        assertionManager.setFailFast(true);
        assertionManager.onAssertionFailure(createEventWithError(false, false, false));
        verify(softAssert).verify();
    }

    @Test
    void shouldFailFastIfFailTestCaseFast()
    {
        assertionManager.setFailFast(false);
        AssertionFailedEvent event = createEventWithError(true, true, false);
        assertionManager.onAssertionFailure(event);
        verify(softAssert).verify();
    }

    @Test
    void shouldNotResetStateBeforeScenario()
    {
        assertionManager.setFailFast(false);
        when(storyControls.currentStoryControls()).thenReturn(storyControls);
        AssertionFailedEvent event = createEventWithError(true, false, true);
        assertionManager.onAssertionFailure(event);
        verify(storyControls).doResetStateBeforeScenario(false);
    }

    private AssertionFailedEvent createEventWithError(boolean knownIssue, boolean failTestCaseFast,
            boolean failTestSuiteFast)
    {
        SoftAssertionError softAssertionError = mock(SoftAssertionError.class);
        lenient().when(softAssertionError.isKnownIssue()).thenReturn(knownIssue);
        lenient().when(softAssertionError.isFailTestCaseFast()).thenReturn(failTestCaseFast);
        lenient().when(softAssertionError.isFailTestSuiteFast()).thenReturn(failTestSuiteFast);
        return new AssertionFailedEvent(softAssertionError);
    }
}
