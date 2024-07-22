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

package org.vividus.ui.web.playwright.action;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.List;

import com.microsoft.playwright.Keyboard;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Mouse;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.BoundingBox;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PlaywrightActionsTests
{
    private static final String TEXT = "text";
    private static final String CONTROL_OR_META = "ControlOrMeta";
    private static final String A = "a";
    private static final BoundingBox BOUNDING_BOX = new BoundingBox();

    @Mock private Locator locator;
    @Mock private Page page;
    @Mock private Mouse mouse;
    @Mock private Keyboard keyboard;

    @BeforeEach
    void beforeEach()
    {
        BOUNDING_BOX.width = 200;
        BOUNDING_BOX.height = 100;
        BOUNDING_BOX.x = 300;
        BOUNDING_BOX.y = 500;
        lenient().when(locator.boundingBox()).thenReturn(BOUNDING_BOX);
        lenient().when(page.mouse()).thenReturn(mouse);
        lenient().when(page.keyboard()).thenReturn(keyboard);
    }

    @Test
    void shouldTestClickActionWithLocator()
    {
        PlaywrightActions.Click clickAction = new PlaywrightActions.Click().createAction();
        clickAction.execute(locator, page);
        assertFalse(clickAction.isArgumentRequired());
        verify(locator).click();
        verifyNoInteractions(page);
    }

    @Test
    void shouldTestClickActionWithoutLocator()
    {
        PlaywrightActions.Click clickAction = new PlaywrightActions.Click().createAction();
        clickAction.execute(null, page);
        assertFalse(clickAction.isArgumentRequired());
        InOrder order = inOrder(mouse);
        order.verify(mouse).down();
        order.verify(mouse).up();
        verifyNoInteractions(locator);
    }

    @Test
    void shouldTestDoubleClickActionWithLocator()
    {
        PlaywrightActions.DoubleClick doubleClickAction = new PlaywrightActions.DoubleClick().createAction();
        assertFalse(doubleClickAction.isArgumentRequired());
        doubleClickAction.execute(locator, page);
        verify(locator).dblclick();
        verifyNoInteractions(page);
    }

    @Test
    void shouldTestDoubleClickActionWithoutLocator()
    {
        PlaywrightActions.DoubleClick doubleClickAction = new PlaywrightActions.DoubleClick().createAction();
        var exception = assertThrows(UnsupportedOperationException.class, () -> doubleClickAction.execute(null, page));
        assertEquals("'DOUBLE_CLICK' action can be performed on page elements only", exception.getMessage());
    }

    @Test
    void shouldTestClickAndHoldActionWithLocator()
    {
        PlaywrightActions.ClickAndHold clickAndHoldAction = new PlaywrightActions.ClickAndHold().createAction();
        clickAndHoldAction.execute(locator, page);
        assertFalse(clickAndHoldAction.isArgumentRequired());
        testCursorMovingIntoElementCenter();
        verify(mouse).down();
    }

    @Test
    void shouldTestClickAndHoldActionWithoutLocator()
    {
        PlaywrightActions.ClickAndHold clickAndHoldAction = new PlaywrightActions.ClickAndHold().createAction();
        clickAndHoldAction.execute(null, page);
        assertFalse(clickAndHoldAction.isArgumentRequired());
        verify(mouse).down();
        verifyNoMoreInteractions(mouse);
        verifyNoInteractions(locator);
    }

    @Test
    void shouldTestReleaseActionWithLocator()
    {
        PlaywrightActions.Release releaseAction = new PlaywrightActions.Release().createAction();
        releaseAction.execute(locator, page);
        assertFalse(releaseAction.isArgumentRequired());
        testCursorMovingIntoElementCenter();
        verify(mouse).up();
    }

    @Test
    void shouldTestReleaseActionWithoutLocator()
    {
        PlaywrightActions.Release releaseAction = new PlaywrightActions.Release().createAction();
        releaseAction.execute(null, page);
        assertFalse(releaseAction.isArgumentRequired());
        verify(mouse).up();
        verifyNoMoreInteractions(mouse);
        verifyNoInteractions(locator);
    }

    @Test
    void shouldTestMoveAction()
    {
        PlaywrightActions.MoveTo moveToAction = new PlaywrightActions.MoveTo().createAction();
        moveToAction.execute(locator, page);
        assertTrue(moveToAction.isArgumentRequired());
        testCursorMovingIntoElementCenter();
    }

    @Test
    void shouldTestMoveByOffsetAction()
    {
        PlaywrightActions.MoveByOffset moveByOffset = new PlaywrightActions.MoveByOffset().createAction();
        var exception = assertThrows(UnsupportedOperationException.class, () -> moveByOffset.execute(null, page));
        assertEquals("MOVE_BY_OFFSET action is not supported by Playwright", exception.getMessage());
    }

    @Test
    void shouldTestEnterTextAction()
    {
        PlaywrightActions.EnterText enterText = new PlaywrightActions.EnterText().createAction();
        enterText.setArgument(TEXT);
        enterText.execute(null, page);
        assertTrue(enterText.isArgumentRequired());
        verify(keyboard).type(TEXT);
    }

    @Test
    void shouldTestPressKeysAction()
    {
        PlaywrightActions.PressKeys pressKeys = new PlaywrightActions.PressKeys().createAction();
        pressKeys.setArgument(List.of(CONTROL_OR_META, A));
        pressKeys.execute(null, page);
        assertTrue(pressKeys.isArgumentRequired());
        InOrder order = inOrder(keyboard);
        order.verify(keyboard).press(CONTROL_OR_META);
        order.verify(keyboard).press(A);
    }

    @Test
    void shouldTestKeyDownAction()
    {
        PlaywrightActions.KeyDown keyDown = new PlaywrightActions.KeyDown().createAction();
        keyDown.setArgument(List.of(CONTROL_OR_META, A));
        keyDown.execute(null, page);
        assertTrue(keyDown.isArgumentRequired());
        InOrder order = inOrder(keyboard);
        order.verify(keyboard).down(CONTROL_OR_META);
        order.verify(keyboard).down(A);
    }

    @Test
    void shouldTestKeyUpAction()
    {
        PlaywrightActions.KeyUp keyUp = new PlaywrightActions.KeyUp().createAction();
        keyUp.setArgument(List.of(CONTROL_OR_META, A));
        keyUp.execute(null, page);
        assertTrue(keyUp.isArgumentRequired());
        InOrder order = inOrder(keyboard);
        order.verify(keyboard).up(CONTROL_OR_META);
        order.verify(keyboard).up(A);
    }

    void testCursorMovingIntoElementCenter()
    {
        verify(locator).scrollIntoViewIfNeeded();
        verify(mouse).move(400, 550);
    }
}
