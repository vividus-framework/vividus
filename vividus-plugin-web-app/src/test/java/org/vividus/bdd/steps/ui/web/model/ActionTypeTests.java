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
import static org.mockito.Mockito.verify;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

@ExtendWith(MockitoExtension.class)
class ActionTypeTests
{
    @Mock
    private Actions baseAction;

    @Mock
    private WebElement element;

    @Test
    void testClickAndHold()
    {
        ActionType.CLICK_AND_HOLD.addAction(baseAction, Optional.of(element));
        verify(baseAction).clickAndHold(element);
    }

    @Test
    void testClickAndHoldElementNotPresent()
    {
        ActionType.CLICK_AND_HOLD.addAction(baseAction, Optional.empty());
        verify(baseAction).clickAndHold();
    }

    @Test
    void testMoveByOffset()
    {
        Point offset = new Point(10, 10);
        ActionType.MOVE_BY_OFFSET.addAction(baseAction, offset);
        verify(baseAction).moveByOffset(offset.getX(), offset.getY());
    }

    @Test
    void testRelease()
    {
        ActionType.RELEASE.addAction(baseAction, Optional.of(element));
        verify(baseAction).release(element);
    }

    @Test
    void testReleaseElementNotPresent()
    {
        ActionType.RELEASE.addAction(baseAction, Optional.empty());
        verify(baseAction).release();
    }

    @ParameterizedTest
    @CsvSource({
        "CLICK_AND_HOLD, false",
        "MOVE_BY_OFFSET, true",
        "RELEASE, false"
        })
    void testIsElementRequired(ActionType type, boolean value)
    {
        assertEquals(value, type.isCoordinatesRequired());
    }

    @ParameterizedTest
    @CsvSource({
        "CLICK_AND_HOLD",
        "RELEASE"
        })
    void testUnsupportedWithPoint(ActionType type)
    {
        assertThrows(UnsupportedOperationException.class, () -> type.addAction(baseAction, new Point(0, 0)));
    }

    @Test
    void testUnsupportedWithElement()
    {
        assertThrows(UnsupportedOperationException.class,
            () -> ActionType.MOVE_BY_OFFSET.addAction(baseAction, Optional.of(element)));
    }
}
