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

package org.vividus.bdd.steps.ui.web;

import static java.lang.String.format;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Interactive;
import org.openqa.selenium.interactions.Sequence;
import org.vividus.bdd.steps.ui.validation.IBaseValidations;
import org.vividus.bdd.steps.ui.web.model.Action;
import org.vividus.bdd.steps.ui.web.model.ActionType;
import org.vividus.bdd.steps.ui.web.model.SequenceAction;
import org.vividus.bdd.steps.ui.web.model.SequenceActionType;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.softassert.ISoftAssert;
import org.vividus.ui.action.search.Locator;
import org.vividus.ui.web.action.search.WebLocatorType;

@ExtendWith(MockitoExtension.class)
class ActionStepsTests
{
    private static final String SIGNATURE_FIELD_XPATH = "//canvas";
    private static final String ELEMENT_EXISTS_MESSAGE = "Element for interaction";
    private static final String OFFSETS_PRESENT_MESSAGE = "Action offset is present";
    private static final String POINTER_MOVE_ACTION = "{duration=100, x=0, y=0, type=pointerMove, origin=Mock for "
            + "WebElement, hashCode: %d}";
    private static final String SEPARATOR = ", ";
    private static final String POINTER_MOVE = POINTER_MOVE_ACTION + SEPARATOR;
    private static final String DURATION_PART = "{duration=0, type=pause}, {duration=0, type=pause}, {duration=0, "
            + "type=pause}, {duration=0, type=pause}, ";
    private static final String TEXT = "text";
    private static final String CLICK_ACTION = "{button=0, type=pointerDown}, {button=0, type=pointerUp}";
    private static final String DOUBLE_CLICK_ACTION = CLICK_ACTION + SEPARATOR + CLICK_ACTION;
    private static final String RELEASE_ACTION = "{button=0, type=pointerUp}";
    private static final String CLICK_AND_HOLD_ACTION = "{button=0, type=pointerDown}";

    @Mock
    private IWebDriverProvider webDriverProvider;

    @Mock
    private IBaseValidations baseValidations;

    @Mock
    private ISoftAssert softAssert;

    @Mock(extraInterfaces = Interactive.class)
    private WebDriver webDriver;

    @Mock
    private WebElement webElement;

    @InjectMocks
    private ActionSteps actionSteps;

    @BeforeEach
    public void before()
    {
        when(webDriverProvider.get()).thenReturn(webDriver);
    }

    @Test
    void testExecuteActionsSequence()
    {
        Locator locator = new Locator(WebLocatorType.XPATH, SIGNATURE_FIELD_XPATH);
        List<Action> actions = createActionsList(locator);
        when(baseValidations.assertIfElementExists(ELEMENT_EXISTS_MESSAGE, locator)).thenReturn(webElement);
        doReturn(true).when(softAssert).assertTrue(OFFSETS_PRESENT_MESSAGE, true);
        actionSteps.executeActionsSequence(actions);
        verify(baseValidations, times(2)).assertIfElementExists(ELEMENT_EXISTS_MESSAGE, locator);
        verify(softAssert).assertTrue(OFFSETS_PRESENT_MESSAGE, true);
    }

    @Test
    void testExecuteActionsSequenceNoLocator()
    {
        List<Action> actions = createActionsList(null);
        actionSteps.executeActionsSequence(actions);
        verifyNoInteractions(baseValidations);
    }

    @Test
    void testExecuteActionsSequenceWrongLocator()
    {
        Locator locator = new Locator(WebLocatorType.XPATH, SIGNATURE_FIELD_XPATH);
        List<Action> actions = createActionsList(locator);
        when(baseValidations.assertIfElementExists(ELEMENT_EXISTS_MESSAGE, locator)).thenReturn(null);
        actionSteps.executeActionsSequence(actions);
        verify(baseValidations).assertIfElementExists(ELEMENT_EXISTS_MESSAGE, locator);
    }

    @Test
    void testExecuteActionMissingOffset()
    {
        List<Action> actions = new ArrayList<>();
        Action action = new Action();
        action.setType(ActionType.MOVE_BY_OFFSET);
        action.setOffset(Optional.empty());
        actions.add(action);
        when(softAssert.assertTrue(OFFSETS_PRESENT_MESSAGE, false)).thenReturn(false);
        actionSteps.executeActionsSequence(actions);
        verify(softAssert).assertTrue(OFFSETS_PRESENT_MESSAGE, false);
    }

    @SuppressWarnings("LineLength")
    @Test
    void testExecuteSequenceOfActions()
    {
        Locator locator = new Locator(WebLocatorType.XPATH, SIGNATURE_FIELD_XPATH);
        Point point = mock(Point.class);
        WebElement webElement = mock(WebElement.class);
        int offset = 15;

        when(baseValidations.assertIfElementExists(ELEMENT_EXISTS_MESSAGE, locator)).thenReturn(webElement);
        when(point.getX()).thenReturn(offset);
        when(point.getY()).thenReturn(offset);

        List<SequenceAction> actions = List.of(
                new SequenceAction(SequenceActionType.DOUBLE_CLICK, locator),
                new SequenceAction(SequenceActionType.CLICK_AND_HOLD, locator),
                new SequenceAction(SequenceActionType.MOVE_BY_OFFSET, point),
                new SequenceAction(SequenceActionType.RELEASE, locator),
                new SequenceAction(SequenceActionType.ENTER_TEXT, TEXT),
                new SequenceAction(SequenceActionType.CLICK, locator),
                new SequenceAction(SequenceActionType.MOVE_TO, locator),
                new SequenceAction(SequenceActionType.DOUBLE_CLICK, null),
                new SequenceAction(SequenceActionType.CLICK_AND_HOLD, null),
                new SequenceAction(SequenceActionType.RELEASE, null),
                new SequenceAction(SequenceActionType.CLICK, null)
                );
        actionSteps.executeSequenceOfActions(actions);
        verify((Interactive) webDriver).perform(argThat(arg -> {
            int hash = webElement.hashCode();
            String mouseSequence = "{id=default mouse, type=pointer, parameters={pointerType=mouse}, actions=["
                    + format(POINTER_MOVE, hash)
                    + DOUBLE_CLICK_ACTION + SEPARATOR
                    + format(POINTER_MOVE, hash)
                    + CLICK_AND_HOLD_ACTION + SEPARATOR
                    + "{duration=200, x=15, y=15, type=pointerMove, origin=pointer}" + SEPARATOR
                    + format(POINTER_MOVE, hash)
                    + RELEASE_ACTION + SEPARATOR
                    + DURATION_PART + DURATION_PART
                    + format(POINTER_MOVE, hash)
                    + CLICK_ACTION + SEPARATOR
                    + format(POINTER_MOVE_ACTION, hash) + SEPARATOR
                    + DOUBLE_CLICK_ACTION + SEPARATOR
                    + CLICK_AND_HOLD_ACTION + SEPARATOR
                    + RELEASE_ACTION + SEPARATOR
                    + CLICK_ACTION
                    + "]}";
            String keyboardSequence = "{type=key, actions=["
                    + DURATION_PART + DURATION_PART
                    + "{duration=0, type=pause}, {duration=0, type=pause}, "
                    + "{type=keyDown, value=t}, {type=keyUp, value=t}, {type=keyDown, value=e}, {type=keyUp, value=e}, "
                    + "{type=keyDown, value=x}, {type=keyUp, value=x}, {type=keyDown, value=t}, {type=keyUp, value=t}, "
                    + "{duration=0, type=pause}, {duration=0, type=pause}, {duration=0, type=pause}, {duration=0, type=pause},"
                    + " {duration=0, type=pause}, {duration=0, type=pause}, {duration=0, type=pause}, {duration=0, type=pause},"
                    + " {duration=0, type=pause}, {duration=0, type=pause}, {duration=0, type=pause}, {duration=0, type=pause}], "
                    + "id=default keyboard}";
            return asString(arg).equals(mouseSequence + keyboardSequence);
        }));
    }

    @Test
    void testExecuteActionsSequenceElementIsNull()
    {
        Locator locator = new Locator(WebLocatorType.XPATH, SIGNATURE_FIELD_XPATH);

        when(baseValidations.assertIfElementExists(ELEMENT_EXISTS_MESSAGE, locator)).thenReturn(null);

        List<SequenceAction> actions = List.of(
                new SequenceAction(SequenceActionType.DOUBLE_CLICK, locator),
                new SequenceAction(SequenceActionType.ENTER_TEXT, TEXT)
                );
        actionSteps.executeSequenceOfActions(actions);
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

    private List<Action> createActionsList(Locator locator)
    {
        List<Action> actions = new ArrayList<>();
        Action actionFirst = new Action();
        actionFirst.setType(ActionType.CLICK_AND_HOLD);
        actionFirst.setSearchAttributes(Optional.ofNullable(locator));
        actionFirst.setOffset(Optional.of(new Point(0, 0)));
        actions.add(actionFirst);
        Action actionSecond = new Action();
        actionSecond.setType(ActionType.DOUBLE_CLICK);
        actionSecond.setSearchAttributes(Optional.ofNullable(locator));
        actions.add(actionSecond);
        Action actionThird = new Action();
        actionThird.setType(ActionType.MOVE_BY_OFFSET);
        actionThird.setOffset(Optional.of(new Point(10, 0)));
        actions.add(actionThird);
        return actions;
    }
}
