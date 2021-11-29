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

package org.vividus.mobileapp.action;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.Point;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.WebElement;

import io.appium.java_client.touch.WaitOptions;
import io.appium.java_client.touch.offset.PointOption;

@ExtendWith(MockitoExtension.class)
public class SequenceTouchActionTypeTests
{
    private Rectangle defaultRectangle = new Rectangle(0, 0, 0, 0);
    private Point defaultPoint = new Point(0, 0);

    @Mock private PositionCachingTouchAction baseAction;

    @Mock private WebElement element;

    @Test
    void testPressByElement()
    {
        when(element.getRect()).thenReturn(defaultRectangle);
        ArgumentCaptor<PointOption<?>> captor = ArgumentCaptor.forClass(PointOption.class);
        when(baseAction.press(captor.capture())).thenReturn(baseAction);

        MobileSequenceActionType.PRESS.addAction(baseAction, element);

        verifyPointOption(captor.getValue(), 0, 0);
        verifyNoMoreInteractions(baseAction, element);
    }

    @Test
    void testPressByCoords()
    {
        ArgumentCaptor<PointOption<?>> captor = ArgumentCaptor.forClass(PointOption.class);
        when(baseAction.press(captor.capture())).thenReturn(baseAction);

        MobileSequenceActionType.PRESS_BY_COORDS.addAction(baseAction, defaultPoint);

        verifyPointOption(captor.getValue(), 0, 0);
        verifyNoMoreInteractions(baseAction);
    }

    @Test
    void testMoveTo()
    {
        when(element.getRect()).thenReturn(defaultRectangle);
        ArgumentCaptor<PointOption<?>> captor = ArgumentCaptor.forClass(PointOption.class);
        when(baseAction.moveTo(captor.capture())).thenReturn(baseAction);

        MobileSequenceActionType.MOVE_TO.addAction(baseAction, element);

        verifyPointOption(captor.getValue(), 0, 0);
        verifyNoMoreInteractions(baseAction, element);
    }

    @Test
    void testMoveByOffset()
    {
        ArgumentCaptor<PointOption<?>> captor = ArgumentCaptor.forClass(PointOption.class);
        when(baseAction.moveTo(captor.capture())).thenReturn(baseAction);
        when(baseAction.getPosition()).thenReturn(null);

        MobileSequenceActionType.MOVE_BY_OFFSET.addAction(baseAction, defaultPoint);

        verifyPointOption(captor.getValue(), 0, 0);
        verifyNoMoreInteractions(baseAction);
    }

    @Test
    void testMoveByOffsetCached()
    {
        ArgumentCaptor<PointOption<?>> captor = ArgumentCaptor.forClass(PointOption.class);
        when(baseAction.moveTo(captor.capture())).thenReturn(baseAction);
        when(baseAction.getPosition()).thenReturn(new RetrievablePointOption().withCoordinates(1, 1));

        MobileSequenceActionType.MOVE_BY_OFFSET.addAction(baseAction, defaultPoint);

        verifyPointOption(captor.getValue(), 1, 1);
        verifyNoMoreInteractions(baseAction);
    }

    @Test
    void testRelease()
    {
        MobileSequenceActionType.RELEASE.addAction(baseAction, null);
        verify(baseAction).release();
        verifyNoMoreInteractions(baseAction);
    }

    @Test
    void testWait()
    {
        Duration duration = mock(Duration.class);
        when(baseAction.waitAction(any(WaitOptions.class))).thenReturn(baseAction);
        MobileSequenceActionType.WAIT.addAction(baseAction, duration);
        verifyNoMoreInteractions(baseAction);
    }

    private void verifyPointOption(PointOption<?> actualPointOption, int expectedX, int expectedY)
    {
        Map<String, Object> capturedPointData = actualPointOption.build();
        assertEquals(expectedX, capturedPointData.get("x"));
        assertEquals(expectedY, capturedPointData.get("y"));
    }
}
