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

package org.vividus.softassert;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.google.common.eventbus.EventBus;

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
    @Mock
    private ISoftAssert softAssert;

    private AssertionManager assertionManager;

    @BeforeEach
    void beforeEach()
    {
        assertionManager = new AssertionManager(new EventBus(), softAssert);
    }

    @Test
    void shouldNotFailFast()
    {
        assertionManager.setFailFast(false);
        assertionManager.onAssertionFailure(mock(AssertionFailedEvent.class));
        verifyNoInteractions(softAssert);
    }

    @Test
    void shouldNotFailFastIfErrorIsKnownIssue()
    {
        assertionManager.setFailFast(true);
        assertionManager.onAssertionFailure(createEventWithError(true));
        verifyNoInteractions(softAssert);
    }

    @Test
    void shouldFailFastIfErrorIsNotKnownIssue()
    {
        assertionManager.setFailFast(true);
        assertionManager.onAssertionFailure(createEventWithError(false));
        verify(softAssert).verify();
    }

    private AssertionFailedEvent createEventWithError(boolean knownIssue)
    {
        SoftAssertionError softAssertionError = mock(SoftAssertionError.class);
        when(softAssertionError.isKnownIssue()).thenReturn(knownIssue);
        return new AssertionFailedEvent(softAssertionError);
    }
}
