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

package org.vividus.softassert;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.jbehave.core.embedder.StoryControls;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.softassert.event.FailTestFastEvent;

@ExtendWith(MockitoExtension.class)
class FailFastManagerTests
{
    @Mock private ISoftAssert softAssert;
    @Mock private StoryControls storyControls;
    @InjectMocks private FailFastManager failFastManager;

    @Test
    void shouldVerifyAssertions()
    {
        failFastManager.onFailTestFast(new FailTestFastEvent(true, false));
        verify(softAssert).verify();
        verifyNoInteractions(storyControls);
    }

    @Test
    void shouldDisableResetOfStateBeforeScenario()
    {
        when(storyControls.currentStoryControls()).thenReturn(storyControls);
        failFastManager.onFailTestFast(new FailTestFastEvent(false, true));
        verify(storyControls).doResetStateBeforeScenario(false);
        verifyNoInteractions(softAssert);
    }

    @Test
    void shouldDisableResetOfStateBeforeScenarioAndVerifyAssertions()
    {
        when(storyControls.currentStoryControls()).thenReturn(storyControls);
        failFastManager.onFailTestFast(new FailTestFastEvent(true, true));
        var ordered = inOrder(storyControls, softAssert);
        ordered.verify(storyControls).doResetStateBeforeScenario(false);
        ordered.verify(softAssert).verify();
        ordered.verifyNoMoreInteractions();
    }
}
