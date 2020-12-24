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

import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import org.jbehave.core.model.ExamplesTable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import org.vividus.bdd.steps.ui.validation.IBaseValidations;
import org.vividus.bdd.steps.ui.validation.IDescriptiveSoftAssert;
import org.vividus.ui.action.search.Locator;
import org.vividus.ui.web.action.IFieldActions;
import org.vividus.ui.web.action.IWebElementActions;
import org.vividus.ui.web.action.search.WebLocatorType;

@ExtendWith(MockitoExtension.class)
class DropdownStepsTests
{
    private static final String MULTIPLE = "multiple";
    private static final String OPTION = "option";
    private static final String DROPDOWN = "Dropdown";
    private static final String SELECT = "select";
    private static final String SELECTED_OPTION_IN_DROP_DOWN = "Selected option in dropdown";
    private static final String ITEM_VALUE = "1";
    private static final String SELECTED_OPTIONS_ARE_PRESENT_IN_DROP_DOWN = "Selected options are present in dropdown";
    private static final Locator LOCATOR = new Locator(WebLocatorType.XPATH, "//xpath");
    private static final String TEXT = "text";
    private static final String DROPDOWN_EXAMPLES_TABLE = "|state|item|\n|true|text| ";
    private static final String DROPDOWN_SIZE_ASSERTION_MESSAGE =
            "Expected dropdown is of the same size as actual dropdown: ";
    private static final String TEXT_OF_ITEM_MESSAGE = "Text of actual option at position [%s]";
    private static final String STATE_OF_ITEM_MESSAGE = "State of actual option at position [%s]";

    @Mock
    private IWebElementActions webElementActions;

    @Mock
    private IBaseValidations baseValidations;

    @Mock
    private IDescriptiveSoftAssert softAssert;

    @Mock
    private WebElement webElement;

    @InjectMocks
    private DropdownSteps dropdownSteps;

    @Mock
    private IFieldActions fieldActions;

    @ParameterizedTest
    @ValueSource(strings = { TEXT, TEXT + " "})
    void shouldVerifySelectWithSelectedOptionExists(String text)
    {
        when(baseValidations.assertIfElementExists(DROPDOWN, LOCATOR)).thenReturn(webElement);
        when(webElement.getTagName()).thenReturn(SELECT);
        List<WebElement> options = List.of(mock(WebElement.class));
        when(softAssert.assertTrue(SELECTED_OPTIONS_ARE_PRESENT_IN_DROP_DOWN, true)).thenReturn(Boolean.TRUE);
        WebElement firstSelectedOption = options.get(0);
        when(webElement.findElements(By.tagName(OPTION))).thenReturn(singletonList(firstSelectedOption));
        when(firstSelectedOption.isSelected()).thenReturn(true);
        when(firstSelectedOption.getText()).thenReturn(text);
        dropdownSteps.doesDropdownHaveFirstSelectedOption(LOCATOR, TEXT);
        verify(softAssert).assertEquals(SELECTED_OPTION_IN_DROP_DOWN, TEXT, TEXT);
    }

    @Test
    void shouldFailIfNoOptionsSelected()
    {
        when(baseValidations.assertIfElementExists(DROPDOWN, LOCATOR)).thenReturn(webElement);
        when(webElement.getTagName()).thenReturn(SELECT);
        when(softAssert.assertTrue(SELECTED_OPTIONS_ARE_PRESENT_IN_DROP_DOWN, false)).thenReturn(Boolean.FALSE);
        when(webElement.findElements(By.tagName(OPTION))).thenReturn(List.of());
        dropdownSteps.doesDropdownHaveFirstSelectedOption(LOCATOR, TEXT);
        verify(softAssert, never()).assertEquals(SELECTED_OPTION_IN_DROP_DOWN, TEXT, TEXT);
    }

    @Test
    void shouldFailIfNoSelectFound()
    {
        when(baseValidations.assertIfElementExists(DROPDOWN, LOCATOR)).thenReturn(null);
        dropdownSteps.doesDropdownHaveFirstSelectedOption(LOCATOR, TEXT);
        verifyNoInteractions(softAssert);
    }

    @Test
    void testSelectItemInDDLSingleSelectAdditable()
    {
        when(baseValidations.assertIfElementExists(DROPDOWN, LOCATOR)).thenReturn(webElement);
        when(webElement.getTagName()).thenReturn(SELECT);
        when(webElement.getAttribute(MULTIPLE)).thenReturn(Boolean.toString(false));
        WebElement element = webElement;
        dropdownSteps.addOptionInDropdown(TEXT, LOCATOR);
        verify(fieldActions).selectItemInDropDownList(
                argThat((Select select) -> select.getWrappedElement().equals(element)), eq(TEXT), eq(true));
    }

    @Test
    void testDoesDropDownListContainItems()
    {
        ExamplesTable dropDownItems = new ExamplesTable(DROPDOWN_EXAMPLES_TABLE);
        when(baseValidations.assertIfElementExists(DROPDOWN, LOCATOR)).thenReturn(webElement);
        when(webElement.getTagName()).thenReturn(SELECT);
        when(webElement.getAttribute(MULTIPLE)).thenReturn(Boolean.toString(true));
        WebElement element = webElement;
        addOptionsToSelect(element, TEXT);
        when(softAssert.assertEquals(DROPDOWN_SIZE_ASSERTION_MESSAGE,
                dropDownItems.getRowsAsParameters(true).size(), 1)).thenReturn(true);
        when(webElement.isSelected()).thenReturn(true);
        dropdownSteps.doesDropdownContainOptions(LOCATOR, dropDownItems);
        verify(softAssert).assertEquals(String.format(TEXT_OF_ITEM_MESSAGE, ITEM_VALUE), TEXT,
                TEXT);
        verify(softAssert).assertEquals(String.format(STATE_OF_ITEM_MESSAGE, ITEM_VALUE), true,
                true);
    }

    @Test
    void testDoesDropDownListContainItemsNullDropDown()
    {
        ExamplesTable dropDownItems = new ExamplesTable(DROPDOWN_EXAMPLES_TABLE);
        dropdownSteps.doesDropdownContainOptions(LOCATOR, dropDownItems);
        verify(softAssert, never()).assertEquals(String.format(TEXT_OF_ITEM_MESSAGE, ITEM_VALUE),
                TEXT, TEXT);
        verify(softAssert, never())
                .assertEquals(String.format(STATE_OF_ITEM_MESSAGE, ITEM_VALUE), true, true);
    }

    @Test
    void testDoesDropDownListContainItemsListIsEmpty()
    {
        ExamplesTable dropDownItems = new ExamplesTable(DROPDOWN_EXAMPLES_TABLE);
        when(baseValidations.assertIfElementExists(DROPDOWN, LOCATOR)).thenReturn(webElement);
        when(webElement.getTagName()).thenReturn(SELECT);
        when(webElement.getAttribute(MULTIPLE)).thenReturn(Boolean.toString(true));
        WebElement element = webElement;
        addOptionsToSelect(element, "itemValue");
        when(softAssert.assertEquals(DROPDOWN_SIZE_ASSERTION_MESSAGE,
                dropDownItems.getRowsAsParameters(true).size(), 1)).thenReturn(false);
        dropdownSteps.doesDropdownContainOptions(LOCATOR, dropDownItems);
        verify(softAssert, never()).assertEquals(String.format(TEXT_OF_ITEM_MESSAGE, ITEM_VALUE),
                TEXT, TEXT);
        verify(softAssert, never())
                .assertEquals(String.format(STATE_OF_ITEM_MESSAGE, ITEM_VALUE), true, true);
    }

    @Test
    void testSelectTextFromDropDownByLocatorXpath()
    {
        when(baseValidations.assertIfElementExists(DROPDOWN, LOCATOR)).thenReturn(webElement);
        when(webElement.getTagName()).thenReturn(SELECT);
        dropdownSteps.selectOptionInDropdown(TEXT, LOCATOR);
        verify(fieldActions).selectItemInDropDownList(any(Select.class), eq(TEXT), eq(false));
    }

    @Test
    void testSelectTextFromDropDownByLocatorXpathNoElement()
    {
        dropdownSteps.selectOptionInDropdown(TEXT, LOCATOR);
        verifyNoInteractions(fieldActions);
    }

    private void addOptionsToSelect(WebElement element, String selectText)
    {
        when(element.findElements(By.tagName(OPTION))).thenReturn(singletonList(webElement));
        Mockito.lenient().when(webElementActions.getElementText(webElement)).thenReturn(selectText);
    }
}
