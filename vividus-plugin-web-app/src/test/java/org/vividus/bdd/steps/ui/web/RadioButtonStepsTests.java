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
import org.vividus.ui.web.util.LocatorUtil;

@ExtendWith(MockitoExtension.class)
class RadioButtonStepsTests
{
    private static final String A_RADIO_BUTTON_LABEL_WITH_TEXT_RADIO_OPTION =
            "A radio " + "button label with text 'radioOptionValue'";
    private static final String RADIO_BUTTON = "Radio button";
    private static final String VALUE = "value";
    private static final String FOR = "for";
    private static final String RADIO_OPTION = "radioOptionValue";
    private static final String PATTERN_RADIO_XPATH = "input[@type='radio' and @id='%s']";
    private static final String TABLE = "|radioOption|\n |radioOptionValue|";

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
        mockRadioOptionLabelSearch();
        when(webElement.getAttribute(FOR)).thenReturn(VALUE);
        radioButtonSteps.assertIfRadioOptionExists(RADIO_OPTION);
        verify(baseValidations).assertIfElementExists(RADIO_BUTTON, new SearchAttributes(ActionAttributeType.XPATH,
                LocatorUtil.getXPath(ElementPattern.RADIO_OPTION_INPUT_PATTERN, VALUE)));
    }

    @Test
    void testIfRadioOptionExistsNullLabel()
    {
        mockRadioOptionLabelSearch();
        when(baseValidations.assertIfElementExists(A_RADIO_BUTTON_LABEL_WITH_TEXT_RADIO_OPTION,
                new SearchAttributes(ActionAttributeType.XPATH,
                        LocatorUtil.getXPath(ElementPattern.LABEL_PATTERN, RADIO_OPTION)))).thenReturn(null);
        radioButtonSteps.assertIfRadioOptionExists(RADIO_OPTION);
        verify(baseValidations, never()).assertIfElementExists(RADIO_BUTTON,
                new SearchAttributes(ActionAttributeType.XPATH,
                        LocatorUtil.getXPath(ElementPattern.RADIO_OPTION_INPUT_PATTERN, VALUE)));
    }

    @ParameterizedTest
    @NullAndEmptySource
    void testIfRadioOptionExistsEmptyOrNullForAttribute(String forAttribute)
    {
        mockRadioOptionLabelSearch();
        when(webElement.getAttribute(FOR)).thenReturn(forAttribute);
        radioButtonSteps.assertIfRadioOptionExists(RADIO_OPTION);
        verify(baseValidations).assertIfElementExists(RADIO_BUTTON, webElement,
                new SearchAttributes(ActionAttributeType.XPATH, "input[@type='radio']"));
    }

    @Test
    void testIfStateRadioOptionExists()
    {
        mockRadioOptionLabelSearch();
        when(webElement.getAttribute(FOR)).thenReturn(VALUE);
        when(baseValidations.assertIfElementExists(RADIO_BUTTON, new SearchAttributes(ActionAttributeType.XPATH,
                LocatorUtil.getXPath(ElementPattern.RADIO_OPTION_INPUT_PATTERN, VALUE)))).thenReturn(webElement);
        radioButtonSteps.assertIfRadioOptionExists(State.ENABLED, RADIO_OPTION);
        verify(baseValidations).assertElementState("The found radio button is " + State.ENABLED, State.ENABLED,
                webElement);
    }

    @Test
    void testCheckRadioOptionInGroup()
    {
        mockRadioOptionLabelSearch();
        when(webElement.getAttribute(FOR)).thenReturn(VALUE);
        when(baseValidations.assertIfElementExists(RADIO_BUTTON, new SearchAttributes(ActionAttributeType.XPATH,
                LocatorUtil.getXPath(ElementPattern.RADIO_OPTION_INPUT_PATTERN, VALUE)))).thenReturn(webElement);
        radioButtonSteps.checkRadioOption(RADIO_OPTION);
        verify(mouseActions).click(webElement);
    }

    @Test
    void testDoesElementContainRadioOptions()
    {
        mockRadioOptionLabelSearch();
        when(webElement.getAttribute(FOR)).thenReturn(RADIO_OPTION);
        ExamplesTable radioOptions = new ExamplesTable(TABLE);
        radioButtonSteps.doesElementContainRadioOptions(radioOptions);
        verify(baseValidations).assertIfElementExists(RADIO_BUTTON, new SearchAttributes(ActionAttributeType.XPATH,
                LocatorUtil.getXPath(ElementPattern.RADIO_OPTION_INPUT_PATTERN, RADIO_OPTION)));
    }

    @Test
    void testElementDoesNotContainRadioOptions()
    {
        mockRadioOptionLabelSearch();
        ExamplesTable radioOptions = new ExamplesTable(TABLE);
        radioButtonSteps.doesElementContainRadioOptions(radioOptions);
        verify(baseValidations, never()).assertIfElementExists(RADIO_BUTTON,
                new SearchAttributes(ActionAttributeType.XPATH,
                        LocatorUtil.getXPath(PATTERN_RADIO_XPATH, RADIO_OPTION)));
    }

    private void mockRadioOptionLabelSearch()
    {
        when(baseValidations.assertIfElementExists(A_RADIO_BUTTON_LABEL_WITH_TEXT_RADIO_OPTION,
                new SearchAttributes(ActionAttributeType.XPATH,
                        LocatorUtil.getXPath(ElementPattern.LABEL_PATTERN, RADIO_OPTION)))).thenReturn(webElement);
    }
}
