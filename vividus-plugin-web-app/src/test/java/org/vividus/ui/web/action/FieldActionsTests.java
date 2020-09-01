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

package org.vividus.ui.web.action;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import org.vividus.softassert.ISoftAssert;

@ExtendWith(MockitoExtension.class)
class FieldActionsTests
{
    private static final String INDEX = "index";
    private static final String ITEMS_WITH_THE_TEXT_TEXT_ARE_SELECTED_FROM_A_DROP_DOWN =
            "Items with the text 'text' are selected from a drop down";
    private static final String ITEM_WITH_THE_TEXT_TEXT_IS_SELECTED_FROM_A_DROP_DOWN =
            "Item with the text 'text' is selected from a drop down";
    private static final String TEXT = "text";
    private static final String SELECT = "select";
    private static final String MULTIPLE = "multiple";

    @Mock
    private WebElementActions webElementActions;

    @Mock
    private WebElement webElement;

    @Mock
    private ISoftAssert softAssert;

    @Mock
    private IWebWaitActions waitActions;

    @InjectMocks
    private FieldActions fieldActions;

    @Test
    void testSelectItemInDDLSelectPresentOptionExist()
    {
        Select select = findDropDownListWithParameters(false);
        addOptionsToSelect(select, TEXT);
        fieldActions.selectItemInDropDownList(select, TEXT, false);
        verify(webElementActions).getElementText(webElement);
        verify(waitActions).waitForPageLoad();
        verify(softAssert).assertTrue(ITEM_WITH_THE_TEXT_TEXT_IS_SELECTED_FROM_A_DROP_DOWN, true);
    }

    @Test
    void testSelectItemInDDLMultiSelectNotAdditable()
    {
        WebElement selectedElement = mock(WebElement.class);
        when(selectedElement.isSelected()).thenReturn(true);
        when(selectedElement.getAttribute(INDEX)).thenReturn(Integer.toString(1));
        Select select = findDropDownListWithParameters(true);

        List<WebElement> options = List.of(webElement, selectedElement);
        when(select.getOptions()).thenReturn(options);
        when(webElementActions.getElementText(webElement)).thenReturn(TEXT);
        when(webElementActions.getElementText(selectedElement)).thenReturn("not" + TEXT);
        fieldActions.selectItemInDropDownList(select, TEXT, false);
        verify(webElementActions).getElementText(webElement);
        verify(waitActions).waitForPageLoad();
        verify(softAssert).assertTrue(ITEMS_WITH_THE_TEXT_TEXT_ARE_SELECTED_FROM_A_DROP_DOWN,
                true);
    }

    @Test
    void testSelectItemInDDLSelectWithWhiteSpacesBefore()
    {
        Select select = findDropDownListWithParameters(false);
        addOptionsToSelect(select, " text");
        fieldActions.selectItemInDropDownList(select, TEXT, false);
        verify(webElementActions).getElementText(webElement);
        verify(waitActions).waitForPageLoad();
        verify(softAssert).assertTrue(ITEM_WITH_THE_TEXT_TEXT_IS_SELECTED_FROM_A_DROP_DOWN, true);
    }

    @Test
    void testSelectItemInDDLSelectWithWhiteSpacesBeforeAfter()
    {
        Select select = findDropDownListWithParameters(false);
        addOptionsToSelect(select, " text ");
        fieldActions.selectItemInDropDownList(select, TEXT, false);
        verify(webElementActions).getElementText(webElement);
        verify(waitActions).waitForPageLoad();
        verify(softAssert).assertTrue(ITEM_WITH_THE_TEXT_TEXT_IS_SELECTED_FROM_A_DROP_DOWN, true);
    }

    @Test
    void testSelectItemInDDLSelectMultiple()
    {
        Select select = findDropDownListWithParameters(true);
        addOptionsToSelect(select, TEXT);
        fieldActions.selectItemInDropDownList(select, TEXT, false);
        verify(webElementActions).getElementText(webElement);
        verify(waitActions).waitForPageLoad();
        verify(softAssert).assertTrue(ITEMS_WITH_THE_TEXT_TEXT_ARE_SELECTED_FROM_A_DROP_DOWN,
                true);
    }

    @Test
    void testSelectItemInDDLSelectPresentOptionDoesntExist()
    {
        when(webElement.getTagName()).thenReturn(SELECT);
        when(webElement.getAttribute(MULTIPLE)).thenReturn(Boolean.toString(false));
        Select select = new Select(webElement);
        addOptionsToSelect(select, "anotherOne");
        fieldActions.selectItemInDropDownList(select, TEXT, false);
        verify(webElementActions).getElementText(webElement);
        verify(waitActions).waitForPageLoad();
        verify(softAssert).assertTrue(ITEM_WITH_THE_TEXT_TEXT_IS_SELECTED_FROM_A_DROP_DOWN,
                false);
    }

    @Test
    void testSelectItemInDDLSelectNull()
    {
        fieldActions.selectItemInDropDownList(null, TEXT, false);
        verify(webElementActions, never()).getElementText(webElement);
        verify(waitActions, never()).waitForPageLoad();
    }

    @Test
    void testSelectItemInDDLSingleSelectAdditable()
    {
        when(webElement.getTagName()).thenReturn(SELECT);
        when(webElement.getAttribute(MULTIPLE)).thenReturn(Boolean.toString(false));
        Select select = new Select(webElement);
        fieldActions.selectItemInDropDownList(select, TEXT, true);
        verifyNoMoreInteractions(waitActions);
        verify(softAssert)
                .recordFailedAssertion("Multiple selecting is not available to single select drop down");
    }

    @Test
    void testClearFieldUsingKeyboard()
    {
        fieldActions.clearFieldUsingKeyboard(webElement);
        verify(webElement).sendKeys(Keys.chord(Keys.CONTROL, "a") + Keys.BACK_SPACE);
    }

    @Test
    void testClearFieldUsingKeyboardNull()
    {
        fieldActions.clearFieldUsingKeyboard(null);
    }

    private Select findDropDownListWithParameters(boolean isMultiple)
    {
        when(webElement.getTagName()).thenReturn(SELECT);
        when(webElement.getAttribute(INDEX)).thenReturn("0");
        when(webElement.getAttribute(MULTIPLE)).thenReturn(Boolean.toString(isMultiple));
        return new Select(webElement);
    }

    private void addOptionsToSelect(Select select, String selectText)
    {
        List<WebElement> options = List.of(webElement);
        when(select.getOptions()).thenReturn(options);
        when(webElementActions.getElementText(webElement)).thenReturn(selectText);
    }
}
