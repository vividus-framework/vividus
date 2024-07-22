/*
 * Copyright 2019-2024 the original author or authors.
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

package org.vividus.ui.web.playwright.steps;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.ui.web.playwright.UiContext;
import org.vividus.ui.web.playwright.action.AbstractPlaywrightActions;
import org.vividus.ui.web.playwright.locator.PlaywrightLocator;

@ExtendWith(MockitoExtension.class)
class ActionsSequenceStepsTests
{
    @Mock private UiContext uiContext;

    @Test
    void shouldExecuteSequenceOfActions()
    {
        var page = mock(Page.class);
        var locator = mock(Locator.class);
        var playwrightLocator = mock(PlaywrightLocator.class);

        when(uiContext.getCurrentPage()).thenReturn(page);
        var actionWithLocator = mock(AbstractPlaywrightActions.class);
        when(actionWithLocator.getArgumentType()).thenReturn(PlaywrightLocator.class);
        when(actionWithLocator.getArgument()).thenReturn(playwrightLocator);
        when(uiContext.locateElement(playwrightLocator)).thenReturn(locator);
        var actionWithEmptyLocator = mock(AbstractPlaywrightActions.class);
        when(actionWithEmptyLocator.getArgumentType()).thenReturn(PlaywrightLocator.class);
        var actionWithStringArgument = mock(AbstractPlaywrightActions.class);
        when(actionWithStringArgument.getArgumentType()).thenReturn(String.class);

        ActionsSequenceSteps steps = new ActionsSequenceSteps(uiContext);
        steps.executeSequenceOfActions(List.of(actionWithLocator, actionWithEmptyLocator, actionWithStringArgument));

        verify(actionWithLocator).execute(locator, page);
        verify(actionWithEmptyLocator).execute(null, page);
        verify(actionWithStringArgument).execute(null, page);
    }
}
