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

package org.vividus.mobileapp.steps;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.WebElement;
import org.vividus.mobileapp.action.MobileSequenceActionType;
import org.vividus.mobileapp.action.PositionCachingTouchAction;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.steps.ui.model.SequenceAction;
import org.vividus.steps.ui.validation.IBaseValidations;
import org.vividus.ui.action.search.Locator;

import io.appium.java_client.PerformsTouchActions;

@ExtendWith(MockitoExtension.class)
class ActionStepsTests
{
    private static final String ELEMENT_EXISTS_MESSAGE = "Element for interaction";

    @Mock private IWebDriverProvider webDriverProvider;

    @Mock private IBaseValidations baseValidations;

    @Mock private Locator locator;

    @Mock private WebElement webElement;

    @Mock private PerformsTouchActions performsTouchActions;

    @InjectMocks private ActionSteps actionSteps;

    @BeforeEach
    public void before()
    {
        when(webDriverProvider.getUnwrapped(PerformsTouchActions.class)).thenReturn(performsTouchActions);
    }

    @Test
    void testExecuteSequenceOfActions()
    {
        List<SequenceAction<MobileSequenceActionType>> actions = List.of(
                new SequenceAction<>(MobileSequenceActionType.RELEASE, null)
                );
        actionSteps.executeSequenceOfActions(actions);
        verify(performsTouchActions).performTouchAction(any(PositionCachingTouchAction.class));
    }

    @Test
    void testExecuteSequenceOfActionsWithLocatorArgType()
    {
        when(baseValidations.assertElementExists(ELEMENT_EXISTS_MESSAGE, locator)).thenReturn(Optional.of(webElement));
        when(webElement.getRect()).thenReturn(new Rectangle(0, 0, 0, 0));

        List<SequenceAction<MobileSequenceActionType>> actions = List.of(
                new SequenceAction<>(MobileSequenceActionType.PRESS, locator)
                );
        actionSteps.executeSequenceOfActions(actions);
        verify(performsTouchActions).performTouchAction(any(PositionCachingTouchAction.class));
    }

    @Test
    void testExecuteSequenceOfActionsStoppedSinceElementByLocatorNotFound()
    {
        when(baseValidations.assertElementExists(ELEMENT_EXISTS_MESSAGE, locator)).thenReturn(Optional.empty());

        List<SequenceAction<MobileSequenceActionType>> actions = List.of(
                new SequenceAction<>(MobileSequenceActionType.PRESS, locator)
                );
        actionSteps.executeSequenceOfActions(actions);
        verifyNoInteractions(performsTouchActions);
    }
}
