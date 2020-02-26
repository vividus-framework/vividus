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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.jbehave.core.model.ExamplesTable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebElement;
import org.vividus.bdd.steps.ui.web.validation.IBaseValidations;
import org.vividus.ui.web.State;
import org.vividus.ui.web.action.IMouseActions;
import org.vividus.ui.web.action.search.ActionAttributeType;
import org.vividus.ui.web.action.search.SearchAttributes;

@ExtendWith(MockitoExtension.class)
class RadioButtonStepsTests
{
    private static final String RADIO_BUTTON = "Radio button";
    private static final String VALUE = "value";
    private static final String FOR = "for";
    private static final String RADIO_OPTION = "radioOptionValue";
    private static final ExamplesTable RADIO_OPTIONS_TABLE = new ExamplesTable("|radioOption|\n |radioOptionValue|");
    private static final String RADIO_OPTION_INPUT_XPATH = ".//input[normalize-space(@type)='radio'"
            + " and normalize-space(@id)=\"value\"]";
    private static final String LABEL_XPATH = ".//label[text()[normalize-space()=\"radioOptionValue\"]"
            + " or *[normalize-space()=\"radioOptionValue\"]"
            + " or @*[normalize-space()=\"radioOptionValue\"]]";

    @Mock
    private IBaseValidations baseValidations;

    @Mock
    private IMouseActions mouseActions;

    @InjectMocks
    private RadioButtonSteps radioButtonSteps;

    @Mock
    private WebElement webElement;

    @Test
    void testIfRadioOptionExists()
    {
        mockRadioOptionLabelSearch(webElement);
        when(webElement.getAttribute(FOR)).thenReturn(VALUE);
        radioButtonSteps.assertIfRadioOptionExists(RADIO_OPTION);
        verify(baseValidations).assertIfElementExists(RADIO_BUTTON,
                new SearchAttributes(ActionAttributeType.XPATH, RADIO_OPTION_INPUT_XPATH));
    }

    @Test
    void testIfRadioOptionExistsNullLabel()
    {
        mockRadioOptionLabelSearch(null);
        radioButtonSteps.assertIfRadioOptionExists(RADIO_OPTION);
        verify(baseValidations, never()).assertIfElementExists(eq(RADIO_BUTTON), any(SearchAttributes.class));
    }

    @ParameterizedTest
    @NullAndEmptySource
    void testIfRadioOptionExistsEmptyOrNullForAttribute(String forAttribute)
    {
        mockRadioOptionLabelSearch(webElement);
        when(webElement.getAttribute(FOR)).thenReturn(forAttribute);
        radioButtonSteps.assertIfRadioOptionExists(RADIO_OPTION);
        verify(baseValidations).assertIfElementExists(RADIO_BUTTON, webElement,
                new SearchAttributes(ActionAttributeType.XPATH, "input[@type='radio']"));
    }

    @Test
    void testIfStateRadioOptionExists()
    {
        mockRadioOptionLabelSearch(webElement);
        when(webElement.getAttribute(FOR)).thenReturn(VALUE);
        when(baseValidations.assertIfElementExists(RADIO_BUTTON,
                new SearchAttributes(ActionAttributeType.XPATH, RADIO_OPTION_INPUT_XPATH))).thenReturn(webElement);
        radioButtonSteps.assertIfRadioOptionExists(State.ENABLED, RADIO_OPTION);
        verify(baseValidations).assertElementState("The found radio button is " + State.ENABLED, State.ENABLED,
                webElement);
    }

    @Test
    void testCheckRadioOptionInGroup()
    {
        mockRadioOptionLabelSearch(webElement);
        when(webElement.getAttribute(FOR)).thenReturn(VALUE);
        when(baseValidations.assertIfElementExists(RADIO_BUTTON,
                new SearchAttributes(ActionAttributeType.XPATH, RADIO_OPTION_INPUT_XPATH))).thenReturn(webElement);
        radioButtonSteps.checkRadioOption(RADIO_OPTION);
        verify(mouseActions).click(webElement);
    }

    @Test
    void testDoesElementContainRadioOptions()
    {
        mockRadioOptionLabelSearch(webElement);
        when(webElement.getAttribute(FOR)).thenReturn(VALUE);
        radioButtonSteps.doesElementContainRadioOptions(RADIO_OPTIONS_TABLE);
        verify(baseValidations).assertIfElementExists(RADIO_BUTTON,
                new SearchAttributes(ActionAttributeType.XPATH, RADIO_OPTION_INPUT_XPATH));
    }

    @Test
    void testElementDoesNotContainRadioOptions()
    {
        mockRadioOptionLabelSearch(webElement);
        radioButtonSteps.doesElementContainRadioOptions(RADIO_OPTIONS_TABLE);
        verify(baseValidations, never()).assertIfElementExists(eq(RADIO_BUTTON), any(SearchAttributes.class));
    }

    private void mockRadioOptionLabelSearch(WebElement label)
    {
        when(baseValidations.assertIfElementExists("A radio button label with text 'radioOptionValue'",
                new SearchAttributes(ActionAttributeType.XPATH, LABEL_XPATH))).thenReturn(label);
    }
}
