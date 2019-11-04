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

import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import org.jbehave.core.model.ExamplesTable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WrapsElement;
import org.openqa.selenium.support.ui.Select;
import org.vividus.bdd.steps.ui.web.validation.IBaseValidations;
import org.vividus.bdd.steps.ui.web.validation.IHighlightingSoftAssert;
import org.vividus.ui.web.DropDownState;
import org.vividus.ui.web.action.IFieldActions;
import org.vividus.ui.web.action.IWebElementActions;
import org.vividus.ui.web.action.search.ActionAttributeType;
import org.vividus.ui.web.action.search.SearchAttributes;
import org.vividus.ui.web.action.search.SearchParameters;
import org.vividus.ui.web.action.search.Visibility;
import org.vividus.ui.web.util.LocatorUtil;

@ExtendWith(MockitoExtension.class)
class DropdownStepsTests
{
    private static final String SELECT = "select";
    private static final String DROP_DOWN_WITH_THE_NAME_DROP_DOWN_LIST_NAME =
            "Drop down with the name 'dropDownListName'";
    private static final String SELECTED_OPTION_IN_DROP_DOWN = "Selected option in drop down";
    private static final String ITEM_VALUE = "1";
    private static final String SELECTED_OPTIONS_ARE_PRESENT_IN_DROP_DOWN = "Selected options are present in drop down";
    private static final String SELECT_PATTERN = ".//select[@*='%s']";
    private static final String DROP_DOWN_LIST_NAME = "dropDownListName";
    private static final String TEXT = "text";
    private static final String DROPDOWN_EXAMPLES_TABLE = "|state|item|\n|true|text| ";
    private static final String DROPDOWN_SIZE_ASSERTION_MESSAGE =
            "Expected dropdown is of the same size as actual dropdown: ";
    private static final String TEXT_OF_ITEM_MESSAGE = "Text of actual item at position [%s]";
    private static final String STATE_OF_ITEM_MESSAGE = "State of actual item at position [%s]";
    private static final String A_DROP_DOWN = "A drop down";
    private static final String XPATH = "//xpath";

    @Mock
    private IWebElementActions webElementActions;

    @Mock
    private IBaseValidations baseValidations;

    @Mock
    private IHighlightingSoftAssert softAssert;

    @Mock
    private WebElement webElement;

    @InjectMocks
    private DropdownSteps dropdownSteps;

    @Mock
    private IFieldActions fieldActions;

    @Test
    void ifDropDownWithNameFound()
    {
        dropdownSteps.isDropDownWithNameFound(DROP_DOWN_LIST_NAME);
        verify(baseValidations).assertIfElementExists(DROP_DOWN_WITH_THE_NAME_DROP_DOWN_LIST_NAME,
            new SearchAttributes(ActionAttributeType.XPATH, LocatorUtil.getXPath(SELECT_PATTERN, DROP_DOWN_LIST_NAME)));
    }

    @Test
    void ifDropDownWithNameExists()
    {
        mockDropDownWithName(TEXT);
        verify(softAssert).assertEquals(SELECTED_OPTION_IN_DROP_DOWN, TEXT, TEXT);
    }

    @Test
    void ifDropDownWithNameExistsSpaces()
    {
        mockDropDownWithName(TEXT + " ");
        verify(softAssert).assertEquals(SELECTED_OPTION_IN_DROP_DOWN, TEXT, TEXT);
    }

    @Test
    void ifDropDownWithNameExistsSelectNull()
    {
        Select dropDown = mock(Select.class);
        DropdownSteps spy = Mockito.spy(dropdownSteps);
        Mockito.lenient().doReturn(dropDown).when(spy).isDropDownWithNameFound(null);
        spy.ifDropDownWithNameExists(DROP_DOWN_LIST_NAME, TEXT);
        verify(softAssert, never()).assertEquals(SELECTED_OPTION_IN_DROP_DOWN, TEXT, TEXT);
    }

    @Test
    void ifDropDownWithNameExistsSelectedOptionsAreNotPresent()
    {
        Select dropDown = mock(Select.class);
        DropdownSteps spy = Mockito.spy(dropdownSteps);
        doReturn(dropDown).when(spy).isDropDownWithNameFound(DROP_DOWN_LIST_NAME);
        when(dropDown.getAllSelectedOptions()).thenReturn(List.of());
        Mockito.lenient().when(softAssert.assertTrue(SELECTED_OPTIONS_ARE_PRESENT_IN_DROP_DOWN, true))
                .thenReturn(Boolean.FALSE);
        spy.ifDropDownWithNameExists(DROP_DOWN_LIST_NAME, TEXT);
        verify(softAssert, never()).assertEquals(SELECTED_OPTION_IN_DROP_DOWN, TEXT, TEXT);
    }

    @Test
    void ifDropDownWithNameFoundState()
    {
        WebElement element = findDropDownListWithParameters(false);
        dropdownSteps.isDropDownWithNameFound(DropDownState.ENABLED, DROP_DOWN_LIST_NAME);
        verify(baseValidations).assertElementState(eq("The found drop down is ENABLED"), eq(DropDownState.ENABLED),
                argThat((WrapsElement select) -> select.getWrappedElement().equals(element)));
    }

    @Test
    void testDoesNotDropDownExist()
    {
        dropdownSteps.doesNotDropDownExist(DROP_DOWN_LIST_NAME);
        verify(baseValidations).assertIfElementDoesNotExist(DROP_DOWN_WITH_THE_NAME_DROP_DOWN_LIST_NAME,
                new SearchAttributes(ActionAttributeType.XPATH,
                        new SearchParameters(LocatorUtil.getXPath(ElementPattern.SELECT_PATTERN, DROP_DOWN_LIST_NAME))
                                .setVisibility(Visibility.ALL)));
    }

    @Test
    void testSelectItemInDDLSelectPresentOptionExist()
    {
        WebElement element = findDropDownListWithParameters(false);
        dropdownSteps.selectItemInDDL(TEXT, DROP_DOWN_LIST_NAME);
        verify(fieldActions).selectItemInDropDownList(
                argThat((Select select) -> select.getWrappedElement().equals(element)), eq(TEXT), eq(false));
    }

    @Test
    void testSelectItemInDDLSingleSelectAdditable()
    {
        WebElement element = findDropDownListWithParameters(false);
        dropdownSteps.addItemInDDL(TEXT, DROP_DOWN_LIST_NAME);
        verify(fieldActions).selectItemInDropDownList(
                argThat((Select select) -> select.getWrappedElement().equals(element)), eq(TEXT), eq(true));
    }

    @Test
    void testDoesDropDownListContainItems()
    {
        ExamplesTable dropDownItems = new ExamplesTable(DROPDOWN_EXAMPLES_TABLE);
        WebElement element = findDropDownListWithParameters(true);
        addOptionsToSelect(element, TEXT);
        when(softAssert.assertEquals(DROPDOWN_SIZE_ASSERTION_MESSAGE,
                dropDownItems.getRowsAsParameters(true).size(), 1)).thenReturn(true);
        when(webElement.isSelected()).thenReturn(true);
        dropdownSteps.doesDropDownListContainItems(DROP_DOWN_LIST_NAME, dropDownItems);
        verify(softAssert).assertEquals(String.format(TEXT_OF_ITEM_MESSAGE, ITEM_VALUE), TEXT,
                TEXT);
        verify(softAssert).assertEquals(String.format(STATE_OF_ITEM_MESSAGE, ITEM_VALUE), true,
                true);
    }

    @Test
    void testDoesDropDownListContainItemsNullDropDown()
    {
        ExamplesTable dropDownItems = new ExamplesTable(DROPDOWN_EXAMPLES_TABLE);
        dropdownSteps.doesDropDownListContainItems(DROP_DOWN_LIST_NAME, dropDownItems);
        verify(softAssert, never()).assertEquals(String.format(TEXT_OF_ITEM_MESSAGE, ITEM_VALUE),
                TEXT, TEXT);
        verify(softAssert, never())
                .assertEquals(String.format(STATE_OF_ITEM_MESSAGE, ITEM_VALUE), true, true);
    }

    @Test
    void testDoesDropDownListContainItemsListIsEmpty()
    {
        ExamplesTable dropDownItems = new ExamplesTable(DROPDOWN_EXAMPLES_TABLE);
        WebElement element = findDropDownListWithParameters(true);
        addOptionsToSelect(element, "itemValue");
        when(softAssert.assertEquals(DROPDOWN_SIZE_ASSERTION_MESSAGE,
                dropDownItems.getRowsAsParameters(true).size(), 1)).thenReturn(false);
        dropdownSteps.doesDropDownListContainItems(DROP_DOWN_LIST_NAME, dropDownItems);
        verify(softAssert, never()).assertEquals(String.format(TEXT_OF_ITEM_MESSAGE, ITEM_VALUE),
                TEXT, TEXT);
        verify(softAssert, never())
                .assertEquals(String.format(STATE_OF_ITEM_MESSAGE, ITEM_VALUE), true, true);
    }

    @Test
    void testSelectTextFromDropDownByLocatorXpath()
    {
        SearchAttributes searchAttributes = new SearchAttributes(ActionAttributeType.XPATH, XPATH);
        when(baseValidations.assertIfElementExists(A_DROP_DOWN, searchAttributes)).thenReturn(webElement);
        when(webElement.getTagName()).thenReturn(SELECT);
        dropdownSteps.selectTextFromDropDownByLocator(TEXT, searchAttributes);
        verify(fieldActions).selectItemInDropDownList(any(Select.class), eq(TEXT), eq(false));
    }

    @Test
    void testSelectTextFromDropDownByLocatorXpathNoElement()
    {
        SearchAttributes searchAttributes = new SearchAttributes(ActionAttributeType.XPATH, XPATH);
        when(baseValidations.assertIfElementExists(A_DROP_DOWN, searchAttributes)).thenReturn(null);
        dropdownSteps.selectTextFromDropDownByLocator(TEXT, searchAttributes);
        verifyNoInteractions(fieldActions);
    }

    private WebElement findDropDownListWithParameters(boolean isMultiple)
    {
        when(baseValidations.assertIfElementExists(DROP_DOWN_WITH_THE_NAME_DROP_DOWN_LIST_NAME,
            new SearchAttributes(ActionAttributeType.XPATH, LocatorUtil.getXPath(ElementPattern.SELECT_PATTERN,
                DROP_DOWN_LIST_NAME)))).thenReturn(webElement);
        when(webElement.getTagName()).thenReturn(SELECT);
        when(webElement.getAttribute("multiple")).thenReturn(Boolean.toString(isMultiple));
        return webElement;
    }

    private void addOptionsToSelect(WebElement element, String selectText)
    {
        when(element.findElements(By.tagName("option"))).thenReturn(singletonList(webElement));
        Mockito.lenient().when(webElementActions.getElementText(webElement)).thenReturn(selectText);
    }

    private void mockDropDownWithName(String firstOptionText)
    {
        Select dropDown = mock(Select.class);
        DropdownSteps spy = Mockito.spy(dropdownSteps);
        doReturn(dropDown).when(spy).isDropDownWithNameFound(DROP_DOWN_LIST_NAME);

        List<WebElement> options = List.of(mock(WebElement.class));
        when(dropDown.getAllSelectedOptions()).thenReturn(options);
        when(softAssert.assertTrue(SELECTED_OPTIONS_ARE_PRESENT_IN_DROP_DOWN, true))
                .thenReturn(Boolean.TRUE);

        WebElement firstSelectedOption = options.get(0);
        when(dropDown.getFirstSelectedOption()).thenReturn(firstSelectedOption);
        when(firstSelectedOption.getText()).thenReturn(firstOptionText);
        spy.ifDropDownWithNameExists(DROP_DOWN_LIST_NAME, TEXT);
    }
}
