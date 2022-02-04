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

package org.vividus.softassert;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.jbehave.core.embedder.StoryControls;
import org.jbehave.core.failures.IgnoringStepsFailure;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JBehaveFailTestFastHandlerTests
{
    @Mock private StoryControls storyControls;
    @InjectMocks private JBehaveFailTestFastHandler jbehaveFailTestFastHandler;

    @Test
    void shouldThrowIgnoringStepsFailure()
    {
        var failure = assertThrows(IgnoringStepsFailure.class, jbehaveFailTestFastHandler::failTestCaseFast);
        assertEquals("Failing scenario fast", failure.getMessage());
        verifyNoInteractions(storyControls);
    }

    @Test
    void shouldDisableResetOfStateBeforeScenario()
    {
        when(storyControls.currentStoryControls()).thenReturn(storyControls);
        jbehaveFailTestFastHandler.failTestSuiteFast();
        verify(storyControls).doResetStateBeforeScenario(false);
    }
}
