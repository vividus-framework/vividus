/*
 * Copyright 2019 the original author or authors.
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

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.vividus.bdd.steps.ui.web.model.Action;
import org.vividus.bdd.steps.ui.web.model.ActionType;
import org.vividus.bdd.steps.ui.web.validation.IBaseValidations;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.softassert.ISoftAssert;
import org.vividus.ui.web.action.search.ActionAttributeType;
import org.vividus.ui.web.action.search.SearchAttributes;

@ExtendWith(MockitoExtension.class)
class ActionStepsTests
{
    private static final String SIGNATURE_FIELD_XPATH = "//canvas";
    private static final String ELEMENT_EXISTS_MESSAGE = "Element for interaction";
    private static final String OFFSETS_PRESENT_MESSAGE = "Action offset is present";

    @Mock
    private IWebDriverProvider webDriverProvider;

    @Mock
    private IBaseValidations baseValidations;

    @Mock
    private ISoftAssert softAssert;

    @Mock
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
        SearchAttributes searchAttributes = new SearchAttributes(ActionAttributeType.XPATH, SIGNATURE_FIELD_XPATH);
        List<Action> actions = createActionsList(searchAttributes);
        when(baseValidations.assertIfElementExists(ELEMENT_EXISTS_MESSAGE, searchAttributes)).thenReturn(webElement);
        doReturn(true).when(softAssert).assertTrue(OFFSETS_PRESENT_MESSAGE, true);
        actionSteps.executeActionsSequence(actions);
        verify(baseValidations, times(2)).assertIfElementExists(ELEMENT_EXISTS_MESSAGE, searchAttributes);
        verify(softAssert).assertTrue(OFFSETS_PRESENT_MESSAGE, true);
    }

    @Test
    void testExecuteActionsSequenceNoSearchAttributes()
    {
        List<Action> actions = createActionsList(null);
        actionSteps.executeActionsSequence(actions);
        verifyNoInteractions(baseValidations);
    }

    @Test
    void testExecuteActionsSequenceWrongSearchAttributes()
    {
        SearchAttributes searchAttributes = new SearchAttributes(ActionAttributeType.XPATH, SIGNATURE_FIELD_XPATH);
        List<Action> actions = createActionsList(searchAttributes);
        when(baseValidations.assertIfElementExists(ELEMENT_EXISTS_MESSAGE, searchAttributes)).thenReturn(null);
        actionSteps.executeActionsSequence(actions);
        verify(baseValidations).assertIfElementExists(ELEMENT_EXISTS_MESSAGE, searchAttributes);
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

    private List<Action> createActionsList(SearchAttributes searchAttributes)
    {
        List<Action> actions = new ArrayList<>();
        Action actionFirst = new Action();
        actionFirst.setType(ActionType.CLICK_AND_HOLD);
        actionFirst.setSearchAttributes(Optional.ofNullable(searchAttributes));
        actionFirst.setOffset(Optional.of(new Point(0, 0)));
        actions.add(actionFirst);
        Action actionSecond = new Action();
        actionSecond.setType(ActionType.DOUBLE_CLICK);
        actionSecond.setSearchAttributes(Optional.ofNullable(searchAttributes));
        actions.add(actionSecond);
        Action actionThird = new Action();
        actionThird.setType(ActionType.MOVE_BY_OFFSET);
        actionThird.setOffset(Optional.of(new Point(10, 0)));
        actions.add(actionThird);
        return actions;
    }
}
