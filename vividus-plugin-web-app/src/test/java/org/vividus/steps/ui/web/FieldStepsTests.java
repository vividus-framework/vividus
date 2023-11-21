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

package org.vividus.steps.ui.web;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebElement;
import org.vividus.selenium.locator.Locator;
import org.vividus.selenium.manager.IWebDriverManager;
import org.vividus.softassert.ISoftAssert;
import org.vividus.steps.ui.validation.IBaseValidations;
import org.vividus.ui.web.action.IFieldActions;
import org.vividus.ui.web.action.WebJavascriptActions;
import org.vividus.ui.web.action.search.WebLocatorType;

@ExtendWith(MockitoExtension.class)
class FieldStepsTests
{
    private static final String FIELD_NAME = "fieldName";
    private static final String TEXT = "text";
    private static final String FIELD_TO_CLEAR = "The field to clear";
    private static final String FIELD_TO_ADD_TEXT = "The field to add text";
    private static final String FIELD_TO_ENTER_TEXT = "The field to enter text";
    private static final Locator ELEMENT_LOCATOR = new Locator(WebLocatorType.FIELD_NAME, FIELD_NAME);

    @Mock private IWebDriverManager webDriverManager;
    @Mock private IFieldActions fieldActions;
    @Mock private WebJavascriptActions javascriptActions;
    @Mock private ISoftAssert softAssert;
    @Mock private IBaseValidations baseValidations;
    @InjectMocks private FieldSteps fieldSteps;
    @Mock private WebElement webElement;

    @Test
    void testEnterTextInField()
    {
        when(baseValidations.assertElementExists(FIELD_TO_ENTER_TEXT, ELEMENT_LOCATOR))
                .thenReturn(Optional.of(webElement));
        fieldSteps.enterTextInField(TEXT, ELEMENT_LOCATOR);
        verify(fieldActions).typeText(webElement, TEXT);
    }

    @Test
    void testEnterTextInFieldNotFound()
    {
        when(baseValidations.assertElementExists(FIELD_TO_ENTER_TEXT, ELEMENT_LOCATOR))
                .thenReturn(Optional.empty());
        fieldSteps.enterTextInField(TEXT, ELEMENT_LOCATOR);
        verifyNoInteractions(fieldActions);
    }

    @Test
    void shouldAddText()
    {
        when(baseValidations.assertElementExists(FIELD_TO_ADD_TEXT, ELEMENT_LOCATOR))
                .thenReturn(Optional.of(webElement));
        fieldSteps.addTextToField(TEXT, ELEMENT_LOCATOR);
        verify(fieldActions).addText(webElement, TEXT);
    }

    @Test
    void shouldDoNothingIfFieldToAddTextIsNotFound()
    {
        when(baseValidations.assertElementExists(FIELD_TO_ADD_TEXT, ELEMENT_LOCATOR)).thenReturn(Optional.empty());
        fieldSteps.addTextToField(TEXT, ELEMENT_LOCATOR);
        verifyNoInteractions(fieldActions);
    }

    @Test
    void shouldClearField()
    {
        when(baseValidations.assertElementExists(FIELD_TO_CLEAR, ELEMENT_LOCATOR)).thenReturn(Optional.of(webElement));
        fieldSteps.clearField(ELEMENT_LOCATOR);
        verify(webElement).clear();
    }

    @Test
    void shouldDoNothingIfFieldToClearIsNotFound()
    {
        when(baseValidations.assertElementExists(FIELD_TO_CLEAR, ELEMENT_LOCATOR)).thenReturn(Optional.empty());
        fieldSteps.clearField(ELEMENT_LOCATOR);
    }

    @Test
    void shouldClearFieldUsingKeyboard()
    {
        when(baseValidations.assertElementExists(FIELD_TO_CLEAR, ELEMENT_LOCATOR)).thenReturn(Optional.of(webElement));
        fieldSteps.clearFieldUsingKeyboard(ELEMENT_LOCATOR);
        verify(fieldActions).clearFieldUsingKeyboard(webElement);
    }

    @Test
    void shouldDoNothingIfFieldToClearUsingKeyboardIsNotFound()
    {
        when(baseValidations.assertElementExists(FIELD_TO_CLEAR, ELEMENT_LOCATOR)).thenReturn(Optional.empty());
        fieldSteps.clearFieldUsingKeyboard(ELEMENT_LOCATOR);
        verifyNoInteractions(fieldActions);
    }
}
