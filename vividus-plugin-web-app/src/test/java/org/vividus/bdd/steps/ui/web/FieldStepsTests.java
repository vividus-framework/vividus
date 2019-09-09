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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.vividus.bdd.steps.ui.web.validation.IBaseValidations;
import org.vividus.ui.web.action.IFieldActions;
import org.vividus.ui.web.action.WebElementActions;
import org.vividus.ui.web.action.search.ActionAttributeType;
import org.vividus.ui.web.action.search.SearchAttributes;
import org.vividus.ui.web.action.search.SearchParameters;
import org.vividus.ui.web.action.search.Visibility;

@ExtendWith(MockitoExtension.class)
class FieldStepsTests
{
    private static final String A_FIELD_WITH_NAME_FIELD_NAME =
            "A field with attributes Field name: 'fieldName'; Visibility: VISIBLE;";
    private static final String FIELD_NAME = "fieldName";
    private static final String TEXT = "text";

    @Mock
    private IBaseValidations baseValidations;

    @Mock
    private WebElementActions webElementActions;

    @Mock
    private WebElement webElement;

    @Mock
    private IFieldActions fieldActions;

    @InjectMocks
    private FieldSteps fieldSteps;

    @Test
    void testDoesNotFieldExist()
    {
        SearchAttributes searchAttributes = new SearchAttributes(ActionAttributeType.FIELD_NAME,
                new SearchParameters(FIELD_NAME).setVisibility(Visibility.ALL));
        fieldSteps.doesNotFieldExist(searchAttributes);
        verify(baseValidations).assertIfElementDoesNotExist(
                "A field with attributes Field name: 'fieldName'; Visibility: ALL;", searchAttributes);
    }

    @Test
    void isFieldFound()
    {
        SearchAttributes searchAttributes = new SearchAttributes(ActionAttributeType.FIELD_NAME, FIELD_NAME);
        fieldSteps.findFieldBy(searchAttributes);
        verify(baseValidations).assertIfElementExists(A_FIELD_WITH_NAME_FIELD_NAME,
                searchAttributes);
    }

    @Test
    void testEnterTextInFieldWithName()
    {
        SearchAttributes searchAttributes = new SearchAttributes(ActionAttributeType.FIELD_NAME, FIELD_NAME);
        fieldSteps.enterTextInField(TEXT, searchAttributes);
        verify(webElementActions).typeText(searchAttributes, TEXT);
    }

    @Test
    void testAddText()
    {
        SearchAttributes searchAttributes = new SearchAttributes(ActionAttributeType.FIELD_NAME, FIELD_NAME);
        when(baseValidations.assertIfElementExists(A_FIELD_WITH_NAME_FIELD_NAME, searchAttributes))
                .thenReturn(webElement);
        fieldSteps.addTextToField(TEXT, searchAttributes);
        verify(webElementActions).addText(webElement, TEXT);
    }

    @Test
    void testAddTextNullField()
    {
        fieldSteps.addTextToField(TEXT, mock(SearchAttributes.class));
        verify(webElement, never()).sendKeys(TEXT);
    }

    @Test
    void testClearFieldWithName()
    {
        SearchAttributes searchAttributes = new SearchAttributes(ActionAttributeType.FIELD_NAME, FIELD_NAME);
        when(baseValidations.assertIfElementExists(A_FIELD_WITH_NAME_FIELD_NAME,
                searchAttributes)).thenReturn(webElement);
        fieldSteps.clearFieldLocatedBy(searchAttributes);
        verify(webElement).clear();
    }

    @Test
    void testClearFieldWithNameNull()
    {
        fieldSteps.clearFieldLocatedBy(mock(SearchAttributes.class));
        verify(webElement, never()).clear();
    }

    @Test
    void testClearFieldWithNameUsingKeyboard()
    {
        SearchAttributes searchAttributes = new SearchAttributes(ActionAttributeType.FIELD_NAME, FIELD_NAME);
        when(baseValidations.assertIfElementExists(A_FIELD_WITH_NAME_FIELD_NAME,
                searchAttributes)).thenReturn(webElement);
        fieldSteps.clearFieldLocatedByUsingKeyboard(searchAttributes);
        verify(fieldActions).clearFieldUsingKeyboard(webElement);
    }

    @Test
    void testClearFieldWithNameUsingKeyboardNull()
    {
        fieldSteps.clearFieldLocatedByUsingKeyboard(mock(SearchAttributes.class));
        verify(webElement, never()).sendKeys(Keys.chord(Keys.CONTROL, "a") + Keys.BACK_SPACE);
    }
}
