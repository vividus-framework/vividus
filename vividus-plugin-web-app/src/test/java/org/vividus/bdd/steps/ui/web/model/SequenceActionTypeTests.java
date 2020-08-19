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

package org.vividus.bdd.steps.ui.web.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.Keys;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

@ExtendWith(MockitoExtension.class)
public class SequenceActionTypeTests
{
    private static final String VALUE = "value";

    @Mock
    private Actions baseAction;

    @Mock
    private WebElement element;

    @Test
    void testDoubleClick()
    {
        SequenceActionType.DOUBLE_CLICK.addAction(baseAction, element);
        verify(baseAction).doubleClick(element);
        verifyNoMoreInteractions(baseAction, element);
    }

    @Test
    void testDoubleClickNoElement()
    {
        SequenceActionType.DOUBLE_CLICK.addAction(baseAction, null);
        verify(baseAction).doubleClick();
        verifyNoMoreInteractions(baseAction, element);
    }

    @Test
    void testClickAndHold()
    {
        SequenceActionType.CLICK_AND_HOLD.addAction(baseAction, element);
        verify(baseAction).clickAndHold(element);
        verifyNoMoreInteractions(baseAction, element);
    }

    @Test
    void testClickAndHoldNoElement()
    {
        SequenceActionType.CLICK_AND_HOLD.addAction(baseAction, null);
        verify(baseAction).clickAndHold();
        verifyNoMoreInteractions(baseAction, element);
    }

    @Test
    void testMoveTo()
    {
        SequenceActionType.MOVE_TO.addAction(baseAction, element);
        verify(baseAction).moveToElement(element);
        verifyNoMoreInteractions(baseAction, element);
    }

    @Test
    void testMoveByOffset()
    {
        int offset = 10;
        Point point = spy(new Point(offset, offset));
        SequenceActionType.MOVE_BY_OFFSET.addAction(baseAction, point);
        verify(baseAction).moveByOffset(offset, offset);
        verify(point).getX();
        verify(point).getY();
        verifyNoMoreInteractions(baseAction, point);
    }

    @Test
    void testRelease()
    {
        SequenceActionType.RELEASE.addAction(baseAction, element);
        verify(baseAction).release(element);
        verifyNoMoreInteractions(baseAction, element);
    }

    @Test
    void testReleaseNoElement()
    {
        SequenceActionType.RELEASE.addAction(baseAction, null);
        verify(baseAction).release();
        verifyNoMoreInteractions(baseAction, element);
    }

    @Test
    void testEnterText()
    {
        SequenceActionType.ENTER_TEXT.addAction(baseAction, VALUE);
        verify(baseAction).sendKeys(VALUE);
        verifyNoMoreInteractions(baseAction);
    }

    @Test
    void testPressKeys()
    {
        SequenceActionType.PRESS_KEYS.addAction(baseAction, List.of(VALUE));
        verify(baseAction).sendKeys(VALUE);
        verifyNoMoreInteractions(baseAction);
    }

    @Test
    void testPressKeysSimultaneously()
    {
        when(baseAction.keyDown(any(Keys.class))).thenReturn(baseAction);
        when(baseAction.sendKeys(anyString())).thenReturn(baseAction);

        SequenceActionType.PRESS_KEYS_SIMULTANEOUSLY.addAction(baseAction, List.of("CONTROL", anyString()));
        verify(baseAction).keyDown(Keys.CONTROL);
        verify(baseAction).sendKeys(anyString());
        verify(baseAction).keyUp(Keys.CONTROL);
        verifyNoMoreInteractions(baseAction);
    }

    @Test
    void testPressKeysSimultaneouslyWrongKeysOrder()
    {
        String value = "a";
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                SequenceActionType.PRESS_KEYS_SIMULTANEOUSLY.addAction(baseAction, List.of(value)));
        String expectedExceptionMessage = String.format("The '%s' is not allowed as first key and the provided key "
                + "is none of 'SHIFT', 'ALT' or 'CONTROL'/'COMMAND'.", value);
        assertEquals(expectedExceptionMessage, exception.getMessage());
        verifyNoMoreInteractions(baseAction);
    }

    @Test
    void testPressKeysSimultaneouslyWithEmptyList()
    {
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                SequenceActionType.PRESS_KEYS_SIMULTANEOUSLY.addAction(baseAction, List.of()));
        String expectedExceptionMessage = "At least one key should be provided. "
            + "The provided keys should start with of 'SHIFT', 'ALT' or 'CONTROL'/'COMMAND' key.";
        assertEquals(expectedExceptionMessage, exception.getMessage());
        verifyNoMoreInteractions(baseAction);
    }

    @Test
    void testClick()
    {
        SequenceActionType.CLICK.addAction(baseAction, element);
        verify(baseAction).click(element);
        verifyNoMoreInteractions(baseAction, element);
    }

    @Test
    void testClickNoElement()
    {
        SequenceActionType.CLICK.addAction(baseAction, null);
        verify(baseAction).click();
        verifyNoMoreInteractions(baseAction, element);
    }

    @EnumSource(SequenceActionType.class)
    @ParameterizedTest
    void testWrongArgType(SequenceActionType type)
    {
        Object dummy = mock(Object.class);
        Exception excepton = assertThrows(IllegalArgumentException.class, () -> type.addAction(baseAction, dummy));
        assertEquals(String.format("Argument for %s action must be of type %s", type.name(), type.getArgumentType()),
                excepton.getMessage());
        verifyNoMoreInteractions(baseAction, element);
    }
}
