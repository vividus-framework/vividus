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

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebElement;
import org.vividus.bdd.steps.ui.web.validation.IBaseValidations;
import org.vividus.selenium.element.Checkbox;
import org.vividus.ui.web.action.CheckboxAction;
import org.vividus.ui.web.action.IMouseActions;
import org.vividus.ui.web.action.search.ActionAttributeType;
import org.vividus.ui.web.action.search.SearchAttributes;

@ExtendWith(MockitoExtension.class)
class CheckboxStepsTests
{
    private static final String CHECKBOX = "Checkbox";
    private static final SearchAttributes SEARCH_ATTRIBUTES = new SearchAttributes(
            ActionAttributeType.XPATH, "input[@type='checkbox']");

    @Mock
    private IBaseValidations baseValidations;

    @Mock
    private WebElement checkbox;

    @Mock
    private IMouseActions mouseActions;

    @InjectMocks
    private CheckboxSteps checkboxSteps;

    @Test
    void shouldChangeCheckboxState()
    {
        when(baseValidations.assertIfElementExists(CHECKBOX, SEARCH_ATTRIBUTES)).thenReturn(checkbox);
        when(checkbox.isDisplayed()).thenReturn(true);
        checkboxSteps.changeStateOfCheckbox(CheckboxAction.CHECK, SEARCH_ATTRIBUTES);
        verify(mouseActions).click(verifyWrappedCheckbox());
    }

    @Test
    void shouldNotCheckWhenNoElementFound()
    {
        checkboxSteps.changeStateOfCheckbox(CheckboxAction.CHECK, SEARCH_ATTRIBUTES);
        verifyNoInteractions(mouseActions);
    }

    @Test
    void shouldNotChangeCheckboxStateWhenWrappedElementNull()
    {
        when(baseValidations.assertIfElementExists(CHECKBOX, SEARCH_ATTRIBUTES)).thenReturn(new Checkbox(null));
        checkboxSteps.changeStateOfCheckbox(CheckboxAction.CHECK, SEARCH_ATTRIBUTES);
        verifyNoInteractions(mouseActions);
    }

    @Test
    void shouldNotClickCheckboxIfItIsInDesiredState()
    {
        when(baseValidations.assertIfElementExists(CHECKBOX, SEARCH_ATTRIBUTES)).thenReturn(checkbox);
        CheckboxAction action = CheckboxAction.CHECK;
        when(checkbox.isSelected()).thenReturn(action.isSelected());
        checkboxSteps.changeStateOfCheckbox(action, SEARCH_ATTRIBUTES);
        verifyNoInteractions(mouseActions);
    }

    @Test
    void shouldClickLabelIfCheckboxNotDisplayed()
    {
        WebElement label = mock(WebElement.class);
        Checkbox wrappedCheckbox = new Checkbox(checkbox, label);
        when(baseValidations.assertIfElementExists(CHECKBOX, SEARCH_ATTRIBUTES)).thenReturn(wrappedCheckbox);
        CheckboxAction action = CheckboxAction.CHECK;
        checkboxSteps.changeStateOfCheckbox(action, SEARCH_ATTRIBUTES);
        verify(mouseActions).click(label);
    }

    @Test
    void shouldCheckAllFoundCheckboxes()
    {
        when(baseValidations.assertIfElementsExist("Checkboxes", SEARCH_ATTRIBUTES))
            .thenReturn(List.of(checkbox, checkbox, checkbox));
        when(checkbox.isDisplayed()).thenReturn(true);
        checkboxSteps.changeStateOfAllCheckboxes(CheckboxAction.CHECK, SEARCH_ATTRIBUTES);
        verify(mouseActions, times(3)).click(verifyWrappedCheckbox());
    }

    private WebElement verifyWrappedCheckbox()
    {
        return argThat(e -> checkbox.equals(((Checkbox) e).getWrappedElement()));
    }
}
