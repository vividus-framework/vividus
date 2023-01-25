/*
 * Copyright 2019-2023 the original author or authors.
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

package org.vividus.mobileapp.action;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;

@ExtendWith(MockitoExtension.class)
class MobileSequenceActionTypeTests
{
    @Mock private TouchGestures baseAction;

    @Test
    void testPress()
    {
        var element = mock(WebElement.class);
        MobileSequenceActionType.TAP_AND_HOLD.addAction(baseAction, element);
        verify(baseAction).tapAndHold(element);
        verifyNoMoreInteractions(baseAction, element);
    }

    @Test
    void testPressNoElement()
    {
        var element = mock(WebElement.class);
        MobileSequenceActionType.TAP_AND_HOLD.addAction(baseAction, null);
        verify(baseAction).tapAndHold();
        verifyNoMoreInteractions(baseAction, element);
    }

    @Test
    void testMoveTo()
    {
        var element = mock(WebElement.class);
        MobileSequenceActionType.MOVE_TO.addAction(baseAction, element);
        verify(baseAction).moveToElement(element);
        verifyNoMoreInteractions(baseAction, element);
    }

    @Test
    void testMoveByOffset()
    {
        var offset = 10;
        var point = spy(new Point(offset, offset));
        MobileSequenceActionType.MOVE_BY_OFFSET.addAction(baseAction, point);
        verify(baseAction).moveByOffset(offset, offset);
        verify(point).getX();
        verify(point).getY();
        verifyNoMoreInteractions(baseAction, point);
    }

    @Test
    void testRelease()
    {
        var element = mock(WebElement.class);
        MobileSequenceActionType.RELEASE.addAction(baseAction, null);
        verify(baseAction).release();
        verifyNoMoreInteractions(baseAction, element);
    }

    @Test
    void testTap()
    {
        var element = mock(WebElement.class);
        MobileSequenceActionType.TAP.addAction(baseAction, element);
        verify(baseAction).tap(element);
        verifyNoMoreInteractions(baseAction, element);
    }

    @Test
    void testTapNoElement()
    {
        var element = mock(WebElement.class);
        MobileSequenceActionType.TAP.addAction(baseAction, null);
        verify(baseAction).tap();
        verifyNoMoreInteractions(baseAction, element);
    }

    @Test
    void testWait()
    {
        Duration duration = mock(Duration.class);
        MobileSequenceActionType.WAIT.addAction(baseAction, duration);
        verify(baseAction).pause(duration);
        verifyNoMoreInteractions(baseAction);
    }

    @EnumSource(MobileSequenceActionType.class)
    @ParameterizedTest
    void testWrongArgType(MobileSequenceActionType type)
    {
        var element = mock(WebElement.class);
        var dummy = mock(Object.class);
        var exception = assertThrows(IllegalArgumentException.class, () -> type.addAction(baseAction, dummy));
        assertEquals(String.format("Argument for %s action must be of type %s", type.name(), type.getArgumentType()),
                exception.getMessage());
        verifyNoMoreInteractions(baseAction, element);
    }
}
