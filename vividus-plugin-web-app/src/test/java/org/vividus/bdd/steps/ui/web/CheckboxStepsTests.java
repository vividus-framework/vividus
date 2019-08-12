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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WrapsElement;
import org.vividus.bdd.steps.ui.web.validation.IBaseValidations;
import org.vividus.selenium.element.Checkbox;
import org.vividus.ui.web.State;
import org.vividus.ui.web.action.CheckboxAction;
import org.vividus.ui.web.action.IClickActions;
import org.vividus.ui.web.action.search.ActionAttributeType;
import org.vividus.ui.web.action.search.SearchAttributes;
import org.vividus.ui.web.action.search.SearchParameters;
import org.vividus.ui.web.context.IWebUiContext;
import org.vividus.ui.web.util.LocatorUtil;

@ExtendWith(MockitoExtension.class)
class CheckboxStepsTests
{
    private static final String CHECKBOXES_NUMBER = "Checkboxes number";
    private static final String CHECKBOX_WITH_ATTR_VALUE = "Checkbox with the attribute '%1$s'='%2$s'";
    private static final String CHECKBOX = "Checkbox";
    private static final String XPATH = "xpath";
    private static final String CHECKBOX_NAME = "checkboxName";
    private static final String CHECKBOX_ATTRIBUTE_TYPE = "attributeType";
    private static final String CHECKBOX_ATTRIBUTE_VALUE = "attributeValue";
    private static final String CHECKBOX_LOCATOR = "input[@type='checkbox']";
    private static final String THE_FOUND_CHECKBOX_IS = "The found checkbox is ";
    private static final String CHECKBOX_WITH_NAME = "Checkbox with name '%s'";
    private static final String CHECKBOX_XPATH = LocatorUtil.getXPath(
            "input[@type=\"checkbox\" and @" + CHECKBOX_ATTRIBUTE_TYPE + "=%s]", CHECKBOX_ATTRIBUTE_VALUE);

    @Mock
    private IBaseValidations baseValidations;

    @Mock
    private IWebUiContext webUiContext;

    @Mock
    private WebElement webElement;

    @Mock
    private IClickActions clickActions;

    @InjectMocks
    private CheckboxSteps checkboxSteps;

    @Test
    void checkCheckBoxUnchecked()
    {
        when(baseValidations.assertIfElementExists(CHECKBOX, new SearchAttributes(
                ActionAttributeType.XPATH, LocatorUtil.getXPath(CHECKBOX_LOCATOR)))).thenReturn(webElement);
        when(webElement.isSelected()).thenReturn(false);
        when(webElement.isDisplayed()).thenReturn(true);
        checkboxSteps.checkCheckBox();
        verify(clickActions).click(argThatCheckbox());
    }

    @Test
    void checkCheckBoxChecked()
    {
        Mockito.lenient().when(baseValidations.assertIfElementExists(CHECKBOX, new SearchAttributes(
                ActionAttributeType.XPATH, CHECKBOX_LOCATOR))).thenReturn(webElement);
        checkboxSteps.checkCheckBox();
        verify(clickActions, never()).click(argThatCheckbox());
    }

    @Test
    void checkCheckBoxCheckedNullCheckbox()
    {
        Mockito.lenient().when(baseValidations.assertIfElementExists(CHECKBOX, new SearchAttributes(
                ActionAttributeType.XPATH, CHECKBOX_LOCATOR))).thenReturn(null);
        checkboxSteps.checkCheckBox();
        Mockito.verifyZeroInteractions(clickActions);
    }

    @Test
    void checkAllCheckBoxes()
    {
        when(baseValidations.assertIfElementsExist(CHECKBOXES_NUMBER,
                new SearchAttributes(ActionAttributeType.XPATH, LocatorUtil.getXPath(CHECKBOX_LOCATOR))))
                        .thenReturn(Arrays.asList(webElement, webElement));
        when(webElement.isDisplayed()).thenReturn(true);
        checkboxSteps.checkAllCheckboxes();
        verify(clickActions, times(2)).click(argThatCheckbox());
    }

    @Test
    void checkAllCheckBoxesEmpty()
    {
        when(baseValidations.assertIfElementsExist(CHECKBOXES_NUMBER,
                new SearchAttributes(ActionAttributeType.XPATH, LocatorUtil.getXPath(CHECKBOX_LOCATOR))))
                        .thenReturn(List.of());
        checkboxSteps.checkAllCheckboxes();
        verify(clickActions, never()).click(argThatCheckbox());
    }

    @Test
    void checkCheckBoxElementNull()
    {
        when(baseValidations.assertIfElementExists(CHECKBOX, new SearchAttributes(ActionAttributeType.XPATH,
                LocatorUtil.getXPath(CHECKBOX_LOCATOR)))).thenReturn(null);
        checkboxSteps.checkCheckBox();
        verify(clickActions, never()).click(argThatCheckbox());
    }

    @Test
    void testUncheckCheckboxByXpath()
    {
        when(baseValidations.assertIfElementExists(CHECKBOX, new SearchAttributes(
                ActionAttributeType.XPATH, LocatorUtil.getXPath(XPATH)))).thenReturn(webElement);
        when(webElement.isSelected()).thenReturn(true);
        when(webElement.isDisplayed()).thenReturn(true);
        checkboxSteps.processCheckboxByXpath(CheckboxAction.UNCHECK, XPATH);
        verify(clickActions).click(argThatCheckbox());
    }

    @Test
    void testUncheckCheckboxItem()
    {
        when(baseValidations.assertIfElementExists(String.format(CHECKBOX_WITH_NAME, CHECKBOX_NAME),
                new SearchAttributes(ActionAttributeType.CHECKBOX_NAME, CHECKBOX_NAME)))
                        .thenReturn(new Checkbox(webElement));
        when(webElement.isSelected()).thenReturn(true);
        when(webElement.isDisplayed()).thenReturn(true);
        checkboxSteps.processCheckboxItem(CheckboxAction.UNCHECK, CHECKBOX_NAME);
        verify(clickActions).click(argThatCheckbox());
    }

    @Test
    void testUncheckCheckboxItemWithAttribute()
    {
        when(baseValidations.assertIfElementExists(CHECKBOX, new SearchAttributes(ActionAttributeType.XPATH,
                LocatorUtil.getXPath("input[@type=\"checkbox\" and @type=\"value\"]")))).thenReturn(webElement);
        when(webElement.isSelected()).thenReturn(true);
        when(webElement.isDisplayed()).thenReturn(true);
        checkboxSteps.uncheckCheckboxItem("type", "value");
        verify(clickActions).click(argThatCheckbox());
    }

    @Test
    void testIfCheckboxWithAttributeExist()
    {
        checkboxSteps.ifCheckboxWithAttributeExists(CHECKBOX_ATTRIBUTE_TYPE, CHECKBOX_ATTRIBUTE_VALUE);
        verify(baseValidations).assertIfElementExists(
                String.format(CHECKBOX_WITH_ATTR_VALUE, CHECKBOX_ATTRIBUTE_TYPE, CHECKBOX_ATTRIBUTE_VALUE),
                new SearchAttributes(ActionAttributeType.XPATH, CHECKBOX_XPATH));
    }

    @Test
    void testIfCheckboxWithAttributeAndStateExist()
    {
        when(baseValidations.assertIfElementExists(
                String.format(CHECKBOX_WITH_ATTR_VALUE, CHECKBOX_ATTRIBUTE_TYPE, CHECKBOX_ATTRIBUTE_VALUE),
                new SearchAttributes(ActionAttributeType.XPATH, CHECKBOX_XPATH))).thenReturn(webElement);
        checkboxSteps.ifCheckboxWithAttributeExists(State.ENABLED, CHECKBOX_ATTRIBUTE_TYPE, CHECKBOX_ATTRIBUTE_VALUE);
        verify(baseValidations).assertElementState(THE_FOUND_CHECKBOX_IS + State.ENABLED, State.ENABLED,
                (WrapsElement) new Checkbox(webElement));
    }

    @Test
    void testIfCheckboxExist()
    {
        checkboxSteps.ifCheckboxExists(CHECKBOX_NAME);
        verify(baseValidations).assertIfElementExists(String.format(CHECKBOX_WITH_NAME, CHECKBOX_NAME),
                new SearchAttributes(ActionAttributeType.CHECKBOX_NAME, CHECKBOX_NAME));
    }

    @Test
    void testIfStateCheckboxExist()
    {
        WebElement checkbox = mock(Checkbox.class);
        when(baseValidations.assertIfElementExists(String.format(CHECKBOX_WITH_NAME, CHECKBOX_NAME),
                new SearchAttributes(ActionAttributeType.CHECKBOX_NAME, CHECKBOX_NAME))).thenReturn(checkbox);
        checkboxSteps.ifCheckboxExists(State.ENABLED, CHECKBOX_NAME);
        verify(baseValidations).assertElementState(THE_FOUND_CHECKBOX_IS + State.ENABLED, State.ENABLED, checkbox);
    }

    @Test
    void testDoesNotCheckboxExist()
    {
        checkboxSteps.doesNotCheckboxExist(CHECKBOX_NAME);
        verify(baseValidations).assertIfElementDoesNotExist(String.format(CHECKBOX_WITH_NAME, CHECKBOX_NAME),
                new SearchAttributes(ActionAttributeType.CHECKBOX_NAME,
                        new SearchParameters(CHECKBOX_NAME).setWaitForElement(false)));
    }

    @Test
    void testCheckCheckboxByXpath()
    {
        when(baseValidations.assertIfElementExists(CHECKBOX, new SearchAttributes(ActionAttributeType.XPATH,
                LocatorUtil.getXPath(XPATH)))).thenReturn(webElement);
        when(webElement.isDisplayed()).thenReturn(true);
        checkboxSteps.processCheckboxByXpath(CheckboxAction.CHECK, XPATH);
        verify(clickActions).click(argThatCheckbox());
    }

    @Test
    void testCheckCheckboxItem()
    {
        when(baseValidations.assertIfElementExists(String.format(CHECKBOX_WITH_NAME, CHECKBOX_NAME),
                new SearchAttributes(ActionAttributeType.CHECKBOX_NAME, CHECKBOX_NAME)))
                        .thenReturn(new Checkbox(webElement));
        when(webElement.isDisplayed()).thenReturn(true);
        checkboxSteps.processCheckboxItem(CheckboxAction.CHECK, CHECKBOX_NAME);
        verify(clickActions).click(argThatCheckbox());
    }

    @Test
    void testCheckCheckboxItemInvisibleCheckbox()
    {
        WebElement labelElement = mock(WebElement.class);
        when(baseValidations.assertIfElementExists(String.format(CHECKBOX_WITH_NAME, CHECKBOX_NAME),
                new SearchAttributes(ActionAttributeType.CHECKBOX_NAME, CHECKBOX_NAME)))
                        .thenReturn(new Checkbox(webElement, labelElement));
        when(webElement.isDisplayed()).thenReturn(false);
        checkboxSteps.processCheckboxItem(CheckboxAction.CHECK, CHECKBOX_NAME);
        verify(clickActions).click(labelElement);
    }

    @Test
    void testCheckCheckboxByXpath2()
    {
        String xpath = ".//*[contains(@style, 'width: 100%')]";
        when(baseValidations.assertIfElementExists(CHECKBOX, new SearchAttributes(ActionAttributeType.XPATH,
                LocatorUtil.getXPath(xpath)))).thenReturn(webElement);
        when(webElement.isDisplayed()).thenReturn(true);
        checkboxSteps.processCheckboxByXpath(CheckboxAction.CHECK, xpath);
        verify(clickActions).click(argThatCheckbox());
    }

    @Test
    void testCheckCheckboxItemWithAttribute()
    {
        when(baseValidations.assertIfElementExists(CHECKBOX, new SearchAttributes(ActionAttributeType.XPATH,
                CHECKBOX_XPATH))).thenReturn(webElement);
        when(webElement.isDisplayed()).thenReturn(true);
        checkboxSteps.checkCheckboxItem(CHECKBOX_ATTRIBUTE_TYPE, CHECKBOX_ATTRIBUTE_VALUE);
        verify(clickActions).click(argThatCheckbox());
    }

    @Test
    void testGetCheckboxXpathByAttributeAndValue()
    {
        assertEquals(CHECKBOX_XPATH,
                checkboxSteps.getCheckboxXpathByAttributeAndValue(CHECKBOX_ATTRIBUTE_TYPE, CHECKBOX_ATTRIBUTE_VALUE));
    }

    private WebElement argThatCheckbox()
    {
        return argThat(e -> ((Checkbox) e).getWrappedElement().equals(webElement));
    }
}
