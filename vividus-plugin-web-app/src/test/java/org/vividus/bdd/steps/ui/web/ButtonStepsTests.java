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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebElement;
import org.vividus.bdd.steps.ui.web.validation.IBaseValidations;
import org.vividus.bdd.steps.ui.web.validation.IElementValidations;
import org.vividus.ui.web.State;
import org.vividus.ui.web.action.ClickActions;
import org.vividus.ui.web.action.IMouseActions;
import org.vividus.ui.web.action.SearchActions;
import org.vividus.ui.web.action.search.ActionAttributeType;
import org.vividus.ui.web.action.search.SearchAttributes;
import org.vividus.ui.web.action.search.SearchParameters;
import org.vividus.ui.web.context.IWebUiContext;
import org.vividus.ui.web.util.LocatorUtil;

@ExtendWith(MockitoExtension.class)
class ButtonStepsTests
{
    private static final String A_RADIO_BUTTON_LABEL_WITH_TEXT_RADIO_OPTION = "A radio "
            + "button label with text 'radioOption'";
    private static final String A_BUTTON_WITH_IMAGE = "A button with image";
    private static final String RADIO_BUTTON = "Radio button";
    private static final String AN_ELEMENT_WITH_BUTTON_OR_INPUT_TAG = "An element with <button> tag or "
            + "<input> tag with attribute type = button| reset| submit";
    private static final String A_BUTTON_WITH_THE_NAME_BUTTON_NAME = "A button with the name 'buttonName'";
    private static final String BUTTON_NAME = "buttonName";
    private static final String VALUE = "value";
    private static final String FOR = "for";
    private static final String RADIO_OPTION = "radioOption";
    private static final String IMAGE_SRC = "imageSrc";
    private static final SearchAttributes BUTTON_IMAGE_ATTRIBUTES = new SearchAttributes(ActionAttributeType.XPATH,
            LocatorUtil.getXPath("button[./img[@src=\"imageSrc\"]]"));
    private static final String TOOLTIP = "tooltip";
    private static final String A_BUTTON_WITH_THE_NAME = "A button with the name";
    private static final String BUSINESS_DESCRIPTION = "There are "
            + "1 buttons with the name 'buttonName' in the webElement";
    private static final String THE_FOUND_BUTTON_STATE = "The found button is " + State.ENABLED;

    @Mock
    private IBaseValidations baseValidations;

    @Mock
    private IElementValidations elementValidations;

    @Mock
    private ClickActions clickActions;

    @Mock
    private SearchActions searchActions;

    @Mock
    private IWebUiContext webUiContext;

    @Mock
    private IMouseActions mouseActions;

    @InjectMocks
    private ButtonSteps buttonSteps;

    @Mock
    private WebElement webElement;

    @Test
    void ifButtonWithNameExists()
    {
        when(webUiContext.getSearchContext()).thenReturn(webElement);
        SearchAttributes searchAttributes = createSearchAttributes();
        List<WebElement> elements = List.of(webElement);
        when(searchActions.findElements(webElement, searchAttributes)).thenReturn(elements);
        buttonSteps.ifButtonWithNameExists(BUTTON_NAME);
        verify(baseValidations).assertIfElementExists(A_BUTTON_WITH_THE_NAME, BUSINESS_DESCRIPTION, elements);
    }

    @Test
    void ifButtonWithNameExistsState()
    {
        webElement = buttonSteps.ifButtonWithNameExists(BUTTON_NAME);
        buttonSteps.ifButtonWithNameExists(State.ENABLED, BUTTON_NAME);
        verify(baseValidations).assertElementState(THE_FOUND_BUTTON_STATE, State.ENABLED, webElement);
    }

    @Test
    void testDoesNotButtonExist()
    {
        SearchAttributes searchAttributes = createSearchAttributes();
        buttonSteps.doesNotButtonExist(BUTTON_NAME);
        verify(baseValidations).assertIfElementDoesNotExist(A_BUTTON_WITH_THE_NAME_BUTTON_NAME,
                AN_ELEMENT_WITH_BUTTON_OR_INPUT_TAG, searchAttributes);
    }

    @Test
    void testIfStateButtonExists()
    {
        ButtonSteps spy = Mockito.spy(buttonSteps);
        webElement = buttonSteps.ifButtonWithNameExists(BUTTON_NAME);
        spy.ifButtonWithNameExists(State.ENABLED, BUTTON_NAME);
        verify(spy).ifButtonWithNameExists(BUTTON_NAME);
        verify(baseValidations).assertElementState(THE_FOUND_BUTTON_STATE, State.ENABLED, webElement);
    }

    @Test
    void testIfStateNullButtonExists()
    {
        ButtonSteps spy = Mockito.spy(buttonSteps);
        spy.ifButtonWithNameExists(State.ENABLED, BUTTON_NAME);
        verify(spy).ifButtonWithNameExists(BUTTON_NAME);
        verify(baseValidations, never()).assertExpectedCondition(THE_FOUND_BUTTON_STATE,
                State.ENABLED.getExpectedCondition(webElement));
    }

    @Test
    void testIfRadioOptionExists()
    {
        stubFindRadiOption();
        when(webElement.getAttribute(FOR)).thenReturn(VALUE);
        buttonSteps.ifRadioOptionExists(RADIO_OPTION);
        verify(baseValidations).assertIfElementExists(RADIO_BUTTON, new SearchAttributes(ActionAttributeType.XPATH,
            LocatorUtil.getXPath(ElementPattern.RADIO_OPTION_INPUT_PATTERN, VALUE)));
    }

    @Test
    void testIfRadioOptionExistsNullLabel()
    {
        stubFindRadiOption();
        when(baseValidations.assertIfElementExists(A_RADIO_BUTTON_LABEL_WITH_TEXT_RADIO_OPTION,
                new SearchAttributes(ActionAttributeType.XPATH,
                        LocatorUtil.getXPath(ElementPattern.LABEL_PATTERN, RADIO_OPTION)))).thenReturn(null);
        buttonSteps.ifRadioOptionExists(RADIO_OPTION);
        verify(baseValidations, never()).assertIfElementExists(RADIO_BUTTON, new SearchAttributes(
                ActionAttributeType.XPATH, LocatorUtil.getXPath(ElementPattern.RADIO_OPTION_INPUT_PATTERN, VALUE)));
    }

    @ParameterizedTest
    @NullAndEmptySource
    void testIfRadioOptionExistsEmptyOrNullForAttribute(String forAttribute)
    {
        when(webElement.getAttribute(FOR)).thenReturn(forAttribute);
        when(baseValidations.assertIfElementExists(A_RADIO_BUTTON_LABEL_WITH_TEXT_RADIO_OPTION,
                new SearchAttributes(ActionAttributeType.XPATH,
                        LocatorUtil.getXPath(ElementPattern.LABEL_PATTERN, RADIO_OPTION)))).thenReturn(webElement);
        buttonSteps.ifRadioOptionExists(RADIO_OPTION);
        verify(baseValidations).assertIfElementExists(RADIO_BUTTON, webElement, new SearchAttributes(
                ActionAttributeType.XPATH, "input[@type='radio']"));
    }

    @Test
    void testIfStateRadioOptionExists()
    {
        stubFindRadiOption();
        when(webElement.getAttribute(FOR)).thenReturn(VALUE);
        when(baseValidations.assertIfElementExists(RADIO_BUTTON, new SearchAttributes(
                ActionAttributeType.XPATH, LocatorUtil.getXPath(ElementPattern.RADIO_OPTION_INPUT_PATTERN,
                        VALUE)))).thenReturn(webElement);
        buttonSteps.ifRadioOptionExists(State.ENABLED, RADIO_OPTION);
        verify(baseValidations).assertElementState("The found radio button is " + State.ENABLED, State.ENABLED,
                webElement);
    }

    @Test
    void testIsButtonWithImageURLAndTooltipFound()
    {
        when(baseValidations.assertIfElementExists(A_BUTTON_WITH_IMAGE, BUTTON_IMAGE_ATTRIBUTES))
                .thenReturn(webElement);
        buttonSteps.isButtonWithImageURLAndTooltipFound(TOOLTIP, IMAGE_SRC);
        verify(elementValidations).assertIfElementContainsTooltip(webElement, TOOLTIP);
    }

    @Test
    void testIsNullButtonWithImageURLAndTooltipFound()
    {
        buttonSteps.isButtonWithImageURLAndTooltipFound(TOOLTIP, IMAGE_SRC);
        verify(elementValidations).assertIfElementContainsTooltip(null, TOOLTIP);
    }

    @Test
    void testIsButtonWithImageSrcFound()
    {
        buttonSteps.isButtonWithImageSrcFound(IMAGE_SRC);
        verify(baseValidations).assertIfElementExists(A_BUTTON_WITH_IMAGE, BUTTON_IMAGE_ATTRIBUTES);
    }

    @Test
    void testClickButtonWithName()
    {
        when(webUiContext.getSearchContext()).thenReturn(webElement);
        SearchAttributes searchAttributes = createSearchAttributes();
        List<WebElement> elements = List.of(webElement);
        when(searchActions.findElements(webElement, searchAttributes)).thenReturn(elements);
        when(baseValidations.assertIfElementExists(A_BUTTON_WITH_THE_NAME, BUSINESS_DESCRIPTION, elements))
                .thenReturn(webElement);
        buttonSteps.clickButtonWithName(BUTTON_NAME);
        verify(clickActions).click(webElement);
    }

    @Test
    void testClickButtonWithImageSrc()
    {
        when(baseValidations.assertIfElementExists(A_BUTTON_WITH_IMAGE, BUTTON_IMAGE_ATTRIBUTES))
                .thenReturn(webElement);
        buttonSteps.clickButtonWithImageSrc(IMAGE_SRC);
        verify(clickActions).click(webElement);
    }

    @Test
    void testClickNullButtonWithImageSrc()
    {
        buttonSteps.clickButtonWithImageSrc(IMAGE_SRC);
        verify(clickActions).click((WebElement) null);
    }

    @Test
    void testMouseOverButton()
    {
        ButtonSteps spy = Mockito.spy(buttonSteps);
        doReturn(webElement).when(spy).ifButtonWithNameExists(BUTTON_NAME);
        spy.mouseOverButton(BUTTON_NAME);
        verify(mouseActions).moveToElement(webElement);
    }

    @Test
    void testCheckRadioOptionInGroup()
    {
        stubFindRadiOption();
        when(webElement.getAttribute(FOR)).thenReturn(VALUE);
        when(baseValidations.assertIfElementExists(RADIO_BUTTON, new SearchAttributes(ActionAttributeType.XPATH,
            LocatorUtil.getXPath(ElementPattern.RADIO_OPTION_INPUT_PATTERN, VALUE)))).thenReturn(webElement);
        buttonSteps.checkRadioOption(RADIO_OPTION);
        verify(clickActions).click(webElement);
    }

    private void stubFindRadiOption()
    {
        when(baseValidations.assertIfElementExists(A_RADIO_BUTTON_LABEL_WITH_TEXT_RADIO_OPTION,
                new SearchAttributes(ActionAttributeType.XPATH,
                        LocatorUtil.getXPath(ElementPattern.LABEL_PATTERN, RADIO_OPTION)))).thenReturn(webElement);
    }

    private SearchAttributes createSearchAttributes()
    {
        SearchParameters parameters = new SearchParameters(BUTTON_NAME);
        return new SearchAttributes(ActionAttributeType.BUTTON_NAME, parameters);
    }
}
