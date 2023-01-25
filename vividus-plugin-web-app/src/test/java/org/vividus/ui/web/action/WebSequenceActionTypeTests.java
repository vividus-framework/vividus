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

package org.vividus.ui.web.action;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

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
class WebSequenceActionTypeTests
{
    private static final String VALUE = "value";
    private static final String STRING = "str";
    private static final String COPY_SHORTCUT_KEY = "c";
    private static final List<String> KEY_LIST = List.of("CONTROL", COPY_SHORTCUT_KEY);
    private static final String EMPTY_KEY_LIST_EXCEPTION = "At least one key should be provided";
    private static final String WRONG_KEY_LIST_VALUE_EXCEPTION_FORMAT = "The '%s' is not allowed as a key";

    @Mock private Actions baseAction;
    @Mock private WebElement element;

    @Test
    void testDoubleClick()
    {
        WebSequenceActionType.DOUBLE_CLICK.addAction(baseAction, element);
        verify(baseAction).doubleClick(element);
        verifyNoMoreInteractions(baseAction, element);
    }

    @Test
    void testDoubleClickNoElement()
    {
        WebSequenceActionType.DOUBLE_CLICK.addAction(baseAction, null);
        verify(baseAction).doubleClick();
        verifyNoMoreInteractions(baseAction, element);
    }

    @Test
    void testClickAndHold()
    {
        WebSequenceActionType.CLICK_AND_HOLD.addAction(baseAction, element);
        verify(baseAction).clickAndHold(element);
        verifyNoMoreInteractions(baseAction, element);
    }

    @Test
    void testClickAndHoldNoElement()
    {
        WebSequenceActionType.CLICK_AND_HOLD.addAction(baseAction, null);
        verify(baseAction).clickAndHold();
        verifyNoMoreInteractions(baseAction, element);
    }

    @Test
    void testMoveTo()
    {
        WebSequenceActionType.MOVE_TO.addAction(baseAction, element);
        verify(baseAction).moveToElement(element);
        verifyNoMoreInteractions(baseAction, element);
    }

    @Test
    void testMoveByOffset()
    {
        var offset = 10;
        var point = spy(new Point(offset, offset));
        WebSequenceActionType.MOVE_BY_OFFSET.addAction(baseAction, point);
        verify(baseAction).moveByOffset(offset, offset);
        verify(point).getX();
        verify(point).getY();
        verifyNoMoreInteractions(baseAction, point);
    }

    @Test
    void testRelease()
    {
        WebSequenceActionType.RELEASE.addAction(baseAction, element);
        verify(baseAction).release(element);
        verifyNoMoreInteractions(baseAction, element);
    }

    @Test
    void testReleaseNoElement()
    {
        WebSequenceActionType.RELEASE.addAction(baseAction, null);
        verify(baseAction).release();
        verifyNoMoreInteractions(baseAction, element);
    }

    @Test
    void testEnterText()
    {
        WebSequenceActionType.ENTER_TEXT.addAction(baseAction, VALUE);
        verify(baseAction).sendKeys(VALUE);
        verifyNoMoreInteractions(baseAction);
    }

    @Test
    void testPressKeys()
    {
        WebSequenceActionType.PRESS_KEYS.addAction(baseAction, List.of(VALUE));
        verify(baseAction).sendKeys(VALUE);
        verifyNoMoreInteractions(baseAction);
    }

    @Test
    void testKeyDown()
    {
        WebSequenceActionType.KEY_DOWN.addAction(baseAction, KEY_LIST);
        verify(baseAction).keyDown(Keys.CONTROL);
        verify(baseAction).keyDown(COPY_SHORTCUT_KEY);
        verifyNoMoreInteractions(baseAction);
    }

    @Test
    void testKeyDownWrongKey()
    {
        var argument = List.of(STRING);
        var exception = assertThrows(IllegalArgumentException.class,
                () -> WebSequenceActionType.KEY_DOWN.addAction(baseAction, argument));
        var expectedExceptionMessage = String.format(WRONG_KEY_LIST_VALUE_EXCEPTION_FORMAT, STRING);
        assertEquals(expectedExceptionMessage, exception.getMessage());
        verifyNoMoreInteractions(baseAction);
    }

    @Test
    void testKeyDownWithEmptyList()
    {
        var argument = List.of();
        var exception = assertThrows(IllegalArgumentException.class,
                () -> WebSequenceActionType.KEY_DOWN.addAction(baseAction, argument));
        assertEquals(EMPTY_KEY_LIST_EXCEPTION, exception.getMessage());
        verifyNoMoreInteractions(baseAction);
    }

    @Test
    void testKeyUp()
    {
        WebSequenceActionType.KEY_UP.addAction(baseAction, KEY_LIST);
        verify(baseAction).keyUp(Keys.CONTROL);
        verify(baseAction).keyUp(COPY_SHORTCUT_KEY);
        verifyNoMoreInteractions(baseAction);
    }

    @Test
    void testKeyUpWrongKey()
    {
        var argument = List.of(STRING);
        var exception = assertThrows(IllegalArgumentException.class,
                () -> WebSequenceActionType.KEY_UP.addAction(baseAction, argument));
        var expectedExceptionMessage = String.format(WRONG_KEY_LIST_VALUE_EXCEPTION_FORMAT, STRING);
        assertEquals(expectedExceptionMessage, exception.getMessage());
        verifyNoMoreInteractions(baseAction);
    }

    @Test
    void testKeyUpWithEmptyList()
    {
        var argument = List.of();
        var exception = assertThrows(IllegalArgumentException.class,
                () -> WebSequenceActionType.KEY_UP.addAction(baseAction, argument));
        assertEquals(EMPTY_KEY_LIST_EXCEPTION, exception.getMessage());
        verifyNoMoreInteractions(baseAction);
    }

    @Test
    void testClick()
    {
        WebSequenceActionType.CLICK.addAction(baseAction, element);
        verify(baseAction).click(element);
        verifyNoMoreInteractions(baseAction, element);
    }

    @Test
    void testClickNoElement()
    {
        WebSequenceActionType.CLICK.addAction(baseAction, null);
        verify(baseAction).click();
        verifyNoMoreInteractions(baseAction, element);
    }

    @EnumSource(WebSequenceActionType.class)
    @ParameterizedTest
    void testWrongArgType(WebSequenceActionType type)
    {
        var dummy = mock(Object.class);
        var exception = assertThrows(IllegalArgumentException.class, () -> type.addAction(baseAction, dummy));
        assertEquals(String.format("Argument for %s action must be of type %s", type.name(), type.getArgumentType()),
                exception.getMessage());
        verifyNoMoreInteractions(baseAction, element);
    }
}
