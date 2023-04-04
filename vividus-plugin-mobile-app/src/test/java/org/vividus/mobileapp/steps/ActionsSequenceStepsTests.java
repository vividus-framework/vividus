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

package org.vividus.mobileapp.steps;

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Interactive;
import org.openqa.selenium.interactions.Sequence;
import org.vividus.mobileapp.action.MobileAtomicActionFactories;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.steps.ui.validation.IBaseValidations;
import org.vividus.ui.action.AtomicAction;
import org.vividus.ui.action.search.Locator;
import org.vividus.ui.mobile.action.search.AppiumLocatorType;

@ExtendWith(MockitoExtension.class)
class ActionsSequenceStepsTests
{
    private static final Locator LOCATOR = new Locator(AppiumLocatorType.ID, "element");
    private static final String ELEMENT_EXISTS_MESSAGE = "Element for interaction";
    private static final String POINTER_MOVE_ACTION = "{duration=200, x=0, y=0, type=pointerMove, origin=Mock for "
                                                      + "WebElement, hashCode: %d}";
    private static final String SEPARATOR = ", ";
    private static final String POINTER_MOVE = POINTER_MOVE_ACTION + SEPARATOR;
    private static final String TAP_ACTION = "{button=0, type=pointerDown}, {button=0, type=pointerUp}";
    private static final String RELEASE_ACTION = "{button=0, type=pointerUp}";
    private static final String PRESS_ACTION = "{button=0, type=pointerDown}";
    private static final String ACTIONS_CLOSE = "]}";

    private static final MobileAtomicActionFactories.TapAndHold TAP_AND_HOLD =
            new MobileAtomicActionFactories.TapAndHold();
    private static final MobileAtomicActionFactories.Tap TAP = new MobileAtomicActionFactories.Tap();
    private static final MobileAtomicActionFactories.MoveTo MOVE_TO = new MobileAtomicActionFactories.MoveTo();
    private static final MobileAtomicActionFactories.MoveByOffset MOVE_BY_OFFSET =
            new MobileAtomicActionFactories.MoveByOffset();
    private static final MobileAtomicActionFactories.Release RELEASE = new MobileAtomicActionFactories.Release();

    @Mock private IBaseValidations baseValidations;
    @Mock private IWebDriverProvider webDriverProvider;
    @InjectMocks private ActionsSequenceSteps sequenceActionSteps;

    @SuppressWarnings("unchecked")
    @Test
    void testExecuteSequenceOfActions()
    {
        var webDriver = mock(WebDriver.class, withSettings().extraInterfaces(Interactive.class));
        var point = mock(Point.class);
        var webElement = mock(WebElement.class);
        var offset = 15;

        when(webDriverProvider.get()).thenReturn(webDriver);
        when(baseValidations.assertElementExists(ELEMENT_EXISTS_MESSAGE, LOCATOR)).thenReturn(Optional.of(webElement));
        when(point.getX()).thenReturn(offset);
        when(point.getY()).thenReturn(offset);

        var actions = List.of(
                new AtomicAction<>(TAP_AND_HOLD, LOCATOR),
                new AtomicAction<>(MOVE_BY_OFFSET, point),
                new AtomicAction<>(RELEASE, null),
                new AtomicAction<>(TAP, LOCATOR),
                new AtomicAction<>(MOVE_TO, LOCATOR),
                new AtomicAction<>(TAP_AND_HOLD, null),
                new AtomicAction<>(RELEASE, null),
                new AtomicAction<>(TAP, null)
        );
        sequenceActionSteps.executeSequenceOfActions(actions);
        var hash = webElement.hashCode();
        var touchSequence = "{id=finger, type=pointer, parameters={pointerType=touch}, actions=["
                            + format(POINTER_MOVE, hash)
                            + PRESS_ACTION + SEPARATOR
                            + "{duration=200, x=15, y=15, type=pointerMove, origin=pointer}" + SEPARATOR
                            + RELEASE_ACTION + SEPARATOR
                            + format(POINTER_MOVE, hash)
                            + TAP_ACTION + SEPARATOR
                            + format(POINTER_MOVE_ACTION, hash) + SEPARATOR
                            + PRESS_ACTION + SEPARATOR
                            + RELEASE_ACTION + SEPARATOR
                            + TAP_ACTION
                            + ACTIONS_CLOSE;
        var actionsCaptor = ArgumentCaptor.forClass(Collection.class);
        verify((Interactive) webDriver).perform(actionsCaptor.capture());
        assertEquals(touchSequence, asString(actionsCaptor.getValue()));
    }

    @Test
    void testExecuteActionsSequenceElementIsNull()
    {
        var webDriver = mock(WebDriver.class, withSettings().extraInterfaces(Interactive.class));

        when(webDriverProvider.get()).thenReturn(webDriver);
        when(baseValidations.assertElementExists(ELEMENT_EXISTS_MESSAGE, LOCATOR)).thenReturn(Optional.empty());

        var actions = List.of(new AtomicAction<>(TAP_AND_HOLD, LOCATOR));
        sequenceActionSteps.executeSequenceOfActions(actions);
        verify((Interactive) webDriver, never()).perform(any());
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
