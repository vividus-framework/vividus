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

package org.vividus.reportportal.listener;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.epam.reportportal.jbehave.JBehaveContext;
import com.epam.reportportal.jbehave.JBehaveContext.Story;
import com.epam.reportportal.listeners.Statuses;

import org.junit.Test;
import org.vividus.softassert.event.AssertionFailedEvent;

public class AssertionFailureListenerTests
{
    @Test
    public void testOnAssertionFailure()
    {
        AssertionFailureListener listener = new AssertionFailureListener();
        AssertionFailedEvent event = mock(AssertionFailedEvent.class);
        Story story = mock(Story.class);
        JBehaveContext.setCurrentStory(story);
        listener.onAssertionFailure(event);
        verify(story).setCurrentStepStatus(Statuses.FAILED);
        verifyNoMoreInteractions(event, story);
    }
}
