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

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.time.Duration;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Interactive;
import org.openqa.selenium.interactions.Sequence;

@ExtendWith(MockitoExtension.class)
class TouchGesturesTests
{
    private static final String POINTER_MOVE_TO_ELEMENT_ACTION = "{duration=200, x=0, y=0, type=pointerMove, "
            + "origin=Mock for WebElement, hashCode: %d}";
    private static final String POINTER_MOVE_ACTION = "{duration=200, x=%d, y=%d, type=pointerMove, origin=%s}";
    private static final String ACTIONS_OPEN = "{id=finger, type=pointer, parameters={pointerType=touch}, actions=[";
    private static final String ACTION_SEPARATOR = ", ";
    private static final String POINTER_MOVE_TO_ELEMENT = POINTER_MOVE_TO_ELEMENT_ACTION + ACTION_SEPARATOR;
    private static final String TAP_ACTION = "{button=0, type=pointerDown}, {button=0, type=pointerUp}";
    private static final String RELEASE_ACTION = "{button=0, type=pointerUp}";
    private static final String PRESS_ACTION = "{button=0, type=pointerDown}";
    private static final String ACTIONS_CLOSE = "]}";
    private static final String POINTER = "pointer";

    @Mock(extraInterfaces = Interactive.class)
    private WebDriver webDriver;

    @InjectMocks
    private TouchGestures actions;

    @Test
    void testPerformPressOnWebElement()
    {
        var webElement = mock(WebElement.class);
        actions.tapAndHold(webElement).perform();
        var hash = webElement.hashCode();
        var touchSequence = ACTIONS_OPEN + format(POINTER_MOVE_TO_ELEMENT, hash) + PRESS_ACTION + ACTIONS_CLOSE;
        verifyValue(touchSequence);
    }

    @Test
    void testPerformPress()
    {
        actions.tapAndHold().perform();
        var touchSequence = ACTIONS_OPEN + PRESS_ACTION + ACTIONS_CLOSE;
        verifyValue(touchSequence);
    }

    @Test
    void testPerformRelease()
    {
        actions.release().perform();
        var touchSequence = ACTIONS_OPEN + RELEASE_ACTION + ACTIONS_CLOSE;
        verifyValue(touchSequence);
    }

    @Test
    void testPerformMoveTo()
    {
        var webElement = mock(WebElement.class);
        var hash = webElement.hashCode();
        actions.moveToElement(webElement).perform();
        var touchSequence = ACTIONS_OPEN + format(POINTER_MOVE_TO_ELEMENT_ACTION, hash) + ACTIONS_CLOSE;
        verifyValue(touchSequence);
    }

    @Test
    void testPerformTapOnElement()
    {
        var webElement = mock(WebElement.class);
        var hash = webElement.hashCode();
        actions.tap(webElement).perform();
        var touchSequence = ACTIONS_OPEN + format(POINTER_MOVE_TO_ELEMENT, hash) + TAP_ACTION + ACTIONS_CLOSE;
        verifyValue(touchSequence);
    }

    @Test
    void testPerformTap()
    {
        actions.tap().perform();
        var touchSequence = ACTIONS_OPEN + TAP_ACTION + ACTIONS_CLOSE;
        verifyValue(touchSequence);
    }

    @Test
    void testPerformPause()
    {
        var duration = Duration.ofSeconds(1);
        actions.pause(duration).perform();
        var touchSequence = ACTIONS_OPEN + "{duration=1000, type=pause}" + ACTIONS_CLOSE;
        verifyValue(touchSequence);
    }

    @Test
    void testPerformMoveByOffset()
    {
        var offsetX = 15;
        var offsetY = 25;
        var newOffsetX = 35;
        var newOffsetY = 45;
        actions.moveByOffset(offsetX, offsetY).moveByOffset(newOffsetX, newOffsetY).perform();
        var touchSequence = ACTIONS_OPEN + format(POINTER_MOVE_ACTION, offsetX, offsetY, "viewport") + ACTION_SEPARATOR
                + format(POINTER_MOVE_ACTION, newOffsetX, newOffsetY, POINTER) + ACTIONS_CLOSE;
        verifyValue(touchSequence);
    }

    @Test
    void testPerformMoveByOffsetAfterMoveToElement()
    {
        var offsetX = 5;
        var offsetY = 55;
        var webElement = mock(WebElement.class);
        var hash = webElement.hashCode();
        actions.moveToElement(webElement).moveByOffset(offsetX, offsetY).perform();
        var touchSequence = ACTIONS_OPEN + format(POINTER_MOVE_TO_ELEMENT, hash)
                + format(POINTER_MOVE_ACTION, offsetX, offsetY, POINTER) + ACTIONS_CLOSE;
        verifyValue(touchSequence);
    }

    @SuppressWarnings({ "LineLength", "unchecked" })
    private void verifyValue(String touchSequence)
    {
        var actionsCaptor = ArgumentCaptor.forClass(Collection.class);
        verify((Interactive) webDriver).perform(actionsCaptor.capture());
        assertEquals(touchSequence, asString(actionsCaptor.getValue()));
    }

    private static String asString(Collection<Sequence> sequences)
    {
        return sequences.stream()
                .map(Sequence::encode)
                .map(Map::toString)
                .sorted()
                .collect(Collectors.joining());
    }
}
