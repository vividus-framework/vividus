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
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.Keys;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.vividus.selenium.KeysManager;
import org.vividus.ui.action.AtomicActionFactory;

@ExtendWith(MockitoExtension.class)
class WebAtomicActionFactoriesTests
{
    private static final String VALUE = "value";
    private static final String CONTROL = "CONTROL";
    private static final String COPY_SHORTCUT_KEY = "c";
    private static final List<String> KEY_LIST = List.of(CONTROL, COPY_SHORTCUT_KEY);
    private static final String EMPTY_KEY_LIST_EXCEPTION = "At least one key should be provided";

    @Mock private Actions baseAction;
    @Mock private WebElement element;
    @Mock private KeysManager keysManager;

    @Test
    void testDoubleClick()
    {
        new WebAtomicActionFactories.DoubleClick().addAction(baseAction, element);
        verify(baseAction).doubleClick(element);
        verifyNoMoreInteractions(baseAction, element);
    }

    @Test
    void testDoubleClickNoElement()
    {
        new WebAtomicActionFactories.DoubleClick().addAction(baseAction, null);
        verify(baseAction).doubleClick();
        verifyNoMoreInteractions(baseAction, element);
    }

    @Test
    void testClickAndHold()
    {
        new WebAtomicActionFactories.ClickAndHold().addAction(baseAction, element);
        verify(baseAction).clickAndHold(element);
        verifyNoMoreInteractions(baseAction, element);
    }

    @Test
    void testClickAndHoldNoElement()
    {
        new WebAtomicActionFactories.ClickAndHold().addAction(baseAction, null);
        verify(baseAction).clickAndHold();
        verifyNoMoreInteractions(baseAction, element);
    }

    @Test
    void testMoveTo()
    {
        new WebAtomicActionFactories.MoveTo().addAction(baseAction, element);
        verify(baseAction).moveToElement(element);
        verifyNoMoreInteractions(baseAction, element);
    }

    @Test
    void testMoveByOffset()
    {
        var offset = 10;
        var point = spy(new Point(offset, offset));
        new WebAtomicActionFactories.MoveByOffset().addAction(baseAction, point);
        verify(baseAction).moveByOffset(offset, offset);
        verify(point).getX();
        verify(point).getY();
        verifyNoMoreInteractions(baseAction, point);
    }

    @Test
    void testRelease()
    {
        new WebAtomicActionFactories.Release().addAction(baseAction, element);
        verify(baseAction).release(element);
        verifyNoMoreInteractions(baseAction, element);
    }

    @Test
    void testReleaseNoElement()
    {
        new WebAtomicActionFactories.Release().addAction(baseAction, null);
        verify(baseAction).release();
        verifyNoMoreInteractions(baseAction, element);
    }

    @Test
    void testEnterText()
    {
        new WebAtomicActionFactories.EnterText().addAction(baseAction, VALUE);
        verify(baseAction).sendKeys(VALUE);
        verifyNoMoreInteractions(baseAction);
    }

    @Test
    void testPressKeys()
    {
        KeysManager keysManager = mock();
        var argument = List.of(VALUE);
        CharSequence[] keys = { "v", "a", "l", "u", "e" };
        when(keysManager.convertToKeys(argument)).thenReturn(keys);
        new WebAtomicActionFactories.PressKeys(keysManager).addAction(baseAction, argument);
        verify(baseAction).sendKeys(keys);
        verifyNoMoreInteractions(baseAction);
    }

    @Test
    void testKeyDown()
    {
        when(keysManager.convertToKey(true, CONTROL)).thenReturn(Keys.CONTROL);
        new WebAtomicActionFactories.KeyDown(keysManager).addAction(baseAction, KEY_LIST);
        verify(baseAction).keyDown(Keys.CONTROL);
        verify(baseAction).keyDown(COPY_SHORTCUT_KEY);
        verifyNoMoreInteractions(baseAction);
    }

    @Test
    void testKeyDownWithEmptyList()
    {
        var argument = List.of();
        var keyDown = new WebAtomicActionFactories.KeyDown(keysManager);
        var exception = assertThrows(IllegalArgumentException.class, () -> keyDown.addAction(baseAction, argument));
        assertEquals(EMPTY_KEY_LIST_EXCEPTION, exception.getMessage());
        verifyNoMoreInteractions(baseAction);
        verifyNoInteractions(keysManager);
    }

    @Test
    void testKeyUp()
    {
        when(keysManager.convertToKey(true, CONTROL)).thenReturn(Keys.CONTROL);
        new WebAtomicActionFactories.KeyUp(keysManager).addAction(baseAction, KEY_LIST);
        verify(baseAction).keyUp(Keys.CONTROL);
        verify(baseAction).keyUp(COPY_SHORTCUT_KEY);
        verifyNoMoreInteractions(baseAction);
    }

    @Test
    void testKeyUpWithEmptyList()
    {
        var argument = List.of();
        var keyUp = new WebAtomicActionFactories.KeyUp(keysManager);
        var exception = assertThrows(IllegalArgumentException.class, () -> keyUp.addAction(baseAction, argument));
        assertEquals(EMPTY_KEY_LIST_EXCEPTION, exception.getMessage());
        verifyNoMoreInteractions(baseAction);
        verifyNoInteractions(keysManager);
    }

    @Test
    void testClick()
    {
        new WebAtomicActionFactories.Click().addAction(baseAction, element);
        verify(baseAction).click(element);
        verifyNoMoreInteractions(baseAction, element);
    }

    @Test
    void testClickNoElement()
    {
        new WebAtomicActionFactories.Click().addAction(baseAction, null);
        verify(baseAction).click();
        verifyNoMoreInteractions(baseAction, element);
    }

    static Stream<AtomicActionFactory<Actions, ?>> webActions()
    {
        return Stream.of(
                new WebAtomicActionFactories.Click(),
                new WebAtomicActionFactories.DoubleClick(),
                new WebAtomicActionFactories.ClickAndHold(),
                new WebAtomicActionFactories.Release(),
                new WebAtomicActionFactories.MoveTo(),
                new WebAtomicActionFactories.MoveByOffset(),
                new WebAtomicActionFactories.EnterText(),
                new WebAtomicActionFactories.PressKeys(mock(KeysManager.class)),
                new WebAtomicActionFactories.KeyDown(mock(KeysManager.class)),
                new WebAtomicActionFactories.KeyUp(mock(KeysManager.class))
        );
    }

    @ParameterizedTest
    @MethodSource("webActions")
    void testWrongArgType(AtomicActionFactory<Actions, ?> factory)
    {
        var dummy = mock(Object.class);
        var exception = assertThrows(IllegalArgumentException.class, () -> factory.addAction(baseAction, dummy));
        assertEquals(
                String.format("Argument for %s action must be of %s", factory.getName(), factory.getArgumentType()),
                exception.getMessage());
        verifyNoMoreInteractions(baseAction, element);
    }
}
