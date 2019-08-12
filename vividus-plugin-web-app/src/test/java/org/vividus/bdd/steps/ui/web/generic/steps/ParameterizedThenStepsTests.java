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

package org.vividus.bdd.steps.ui.web.generic.steps;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.jbehave.core.model.ExamplesTable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.vividus.bdd.steps.ComparisonRule;
import org.vividus.bdd.steps.ui.web.CheckboxSteps;
import org.vividus.bdd.steps.ui.web.ElementPattern;
import org.vividus.bdd.steps.ui.web.validation.IDescriptiveSoftAssert;
import org.vividus.ui.web.action.ISearchActions;
import org.vividus.ui.web.action.search.ActionAttributeType;
import org.vividus.ui.web.action.search.SearchAttributes;
import org.vividus.ui.web.action.search.SearchParameters;
import org.vividus.ui.web.context.IWebUiContext;
import org.vividus.ui.web.util.LocatorUtil;

@SuppressWarnings("checkstyle:methodcount")
@ExtendWith(MockitoExtension.class)
class ParameterizedThenStepsTests
{
    private static final String DROP_DOWNS = "drop downs";
    private static final String DISABLED = "DISABLED";
    private static final String VISIBLE = "VISIBLE";
    private static final String CHECKBOX = "checkbox";
    private static final String IMAGE_SRC_STATE_TOOLTIP_VALUE2_ENABLED_VALUE3 =
            "|imageSrc|state|tooltip|\n|value2|enabled|value3|";
    private static final String NAME_IMAGE_SRC_STATE_TOOLTIP_VALUE1_VALUE2_ENABLED_VALUE3 =
            "|name|imageSrc|state|tooltip|\n|value1|value2|enabled|value3|";
    private static final String NAME_IMAGE_SRC_STATE_VALUE1_VALUE2_ENABLED =
            "|name|imageSrc|state|\n|value1|value2|enabled|";
    private static final String NAME_IMAGE_SRC_VALUE1_VALUE2 = "|name|image_src|\n|value1|value2|";
    private static final String NAME_VALUE = "|name|\n|value|";
    private static final String BUTTONS = "buttons";
    private static final String STATE_VISIBLE = "|state|\n |VISIBLE|";
    private static final String TABLE_LINK = "|text|state|URL|tooltip|\n |textValue|VISIBLE|urlValue|tooltipValue|";
    private static final String NAME = "testName";
    private static final String FRAMES = "frames";
    private static final String RADIO_BUTTONS = "radio buttons";
    private static final String ELEMENTS = "elements";
    private static final String SCRIPTS = "scripts";
    private static final String IMAGES = "images";
    private static final String TEST_ATTRIBUTE_NAME = "testAttributeName";
    private static final String TEST_ATTRIBUTE_VALUE = "testAttributeValue";
    private static final String LINKS = "links";
    private static final ComparisonRule EQUAL_TO = ComparisonRule.EQUAL_TO;
    private static final String INVALID_PARAMETERS = "Invalid parameters:"
            + " '%1$s' attributeName and '%2$s' attributeValue";
    private static final String NULL = "null";
    private static final String XPATH = ".//input";

    @Mock
    private ISearchActions searchActions;

    @Mock
    private IWebUiContext webUiContext;

    @Mock
    private SearchContext searchContext;

    @Mock
    private WebElement element;

    @Mock
    private IDescriptiveSoftAssert descriptiveSoftAssert;

    @Mock
    private ParameterizedValidations parameterizedValidations;

    @Mock
    private ParameterizedChecks parameterizedChecks;

    @Mock
    private IParameterizedSearchActions parameterizedSearchActions;

    @Mock
    private CheckboxSteps checboxSteps;

    @InjectMocks
    private ParameterizedThenSteps parameterizedThenSteps;

    private ExamplesTable parameters;
    private final List<WebElement> elements = new ArrayList<>();

    static Stream<Arguments> testIsRadioButtonWithParametersFound()
    {
        return Stream.of(
                Arguments.of("|name|state|\n |testName|VISIBLE|", VISIBLE),
                Arguments.of("|name|\n |testName|",               null)
            );
    }

    @ParameterizedTest
    @MethodSource
    void testIsRadioButtonWithParametersFound(String inputExample, String state)
    {
        when(webUiContext.getSearchContext()).thenReturn(searchContext);
        parameters = new ExamplesTable(inputExample);
        when(parameterizedChecks.checkIfParametersAreSet(parameters)).thenReturn(true);
        stubRadioButton(state);
        parameterizedThenSteps.isRadioButtonWithParametersFound(EQUAL_TO, 1, parameters);
        verifyAssert(RADIO_BUTTONS, elements);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "|name|\n |testName|",
            "|state|\n |VISIBLE|"
    })
    void testIsRadioButtonWithParametersNotFound(String inputExamples)
    {
        parameters = new ExamplesTable(inputExamples);
        when(parameterizedChecks.checkIfParametersAreSet(parameters)).thenReturn(true);
        parameterizedThenSteps.isRadioButtonWithParametersFound(EQUAL_TO, 1, parameters);
        verifyAssert(RADIO_BUTTONS, elements);
    }

    @Test
    void testIsRadioButtonWithParametersFoundEmptyParameters()
    {
        parameters = new ExamplesTable("|name|state|");
        when(parameterizedChecks.checkIfParametersAreSet(parameters)).thenReturn(false);
        parameterizedThenSteps.isRadioButtonWithParametersFound(EQUAL_TO, 1, parameters);
        verifyZeroInteractions(descriptiveSoftAssert);
    }

    @Test
    void testIsRadioButtonWithParametersFoundEmptyName()
    {
        parameters = new ExamplesTable("|name|state|\n ||VISIBLE|");
        when(parameterizedChecks.checkIfParametersAreSet(parameters)).thenReturn(false);
        parameterizedThenSteps.isRadioButtonWithParametersFound(EQUAL_TO, 1, parameters);
        verifyZeroInteractions(descriptiveSoftAssert);
    }

    @Test
    void testIsElementWithParametersSingleParameterNotSet()
    {
        when(parameterizedChecks.checkIfParametersAreSet(parameters)).thenReturn(false);
        parameterizedThenSteps.isElementWithParametersFound(EQUAL_TO, 1, parameters);
        verifyZeroInteractions(parameterizedValidations);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            NAME_VALUE,
            NAME_IMAGE_SRC_VALUE1_VALUE2,
            NAME_IMAGE_SRC_STATE_VALUE1_VALUE2_ENABLED,
            NAME_IMAGE_SRC_STATE_TOOLTIP_VALUE1_VALUE2_ENABLED_VALUE3,
            IMAGE_SRC_STATE_TOOLTIP_VALUE2_ENABLED_VALUE3
    })
    void testIsButtonWithParametersFoundName(String parameters)
    {
        when(webUiContext.getSearchContext()).thenReturn(searchContext);
        ExamplesTable table = new ExamplesTable(parameters);
        when(parameterizedChecks.checkIfParametersAreSet(table)).thenReturn(true);
        parameterizedThenSteps.isButtonWithParametersFound(EQUAL_TO, 1, table);
        verifyAssert(BUTTONS, elements);
    }

    @Test
    void testIsButtonWithParametersFoundNameAndImage()
    {
        when(webUiContext.getSearchContext()).thenReturn(searchContext);
        ExamplesTable table = new ExamplesTable(NAME_IMAGE_SRC_VALUE1_VALUE2);
        when(parameterizedChecks.checkIfParametersAreSet(table)).thenReturn(true);
        parameterizedThenSteps.isButtonWithParametersFound(EQUAL_TO, 1, table);
        verifyAssert(BUTTONS, elements);
    }

    @Test
    void testIsButtonWithParametersFoundNameAndImageAndState()
    {
        when(webUiContext.getSearchContext()).thenReturn(searchContext);
        ExamplesTable table = new ExamplesTable(NAME_IMAGE_SRC_STATE_VALUE1_VALUE2_ENABLED);
        when(parameterizedChecks.checkIfParametersAreSet(table)).thenReturn(true);
        parameterizedThenSteps.isButtonWithParametersFound(EQUAL_TO, 1, table);
        verifyAssert(BUTTONS, elements);
    }

    @Test
    void testIsButtonWithParametersFoundNameAndImageAndStateAndTooltip()
    {
        when(webUiContext.getSearchContext()).thenReturn(searchContext);
        ExamplesTable table = new ExamplesTable(NAME_IMAGE_SRC_STATE_TOOLTIP_VALUE1_VALUE2_ENABLED_VALUE3);
        when(parameterizedChecks.checkIfParametersAreSet(table)).thenReturn(true);
        parameterizedThenSteps.isButtonWithParametersFound(EQUAL_TO, 1, table);
        verifyAssert(BUTTONS, elements);
    }

    @Test
    void testIsButtonWithParametersFoundImageAndStateAndTooltip()
    {
        when(webUiContext.getSearchContext()).thenReturn(searchContext);
        SearchAttributes attributes = new SearchAttributes(ActionAttributeType.BUTTON_NAME, "")
                        .addFilter(ActionAttributeType.STATE, "enabled")
                        .addFilter(ActionAttributeType.TOOLTIP, "value3");
        attributes.addChildSearchAttributes(
                new SearchAttributes(ActionAttributeType.IMAGE_SRC, new SearchParameters("value2")));
        mockSearchResults(attributes);
        ExamplesTable table = new ExamplesTable(IMAGE_SRC_STATE_TOOLTIP_VALUE2_ENABLED_VALUE3);
        when(parameterizedChecks.checkIfParametersAreSet(table)).thenReturn(true);
        parameterizedThenSteps.isButtonWithParametersFound(EQUAL_TO, 1, table);
        verifyAssert(BUTTONS, elements);
    }

    @Test
    void testIsButtonWithParametersFoundNoParameters()
    {
        parameters = new ExamplesTable("");
        when(parameterizedChecks.checkIfParametersAreSet(parameters)).thenReturn(false);
        parameterizedThenSteps.isButtonWithParametersFound(EQUAL_TO, 1, parameters);
        verifyZeroInteractions(descriptiveSoftAssert);
    }

    @Test
    void testIsButtonWithParametersFoundNoRequiredParams()
    {
        ExamplesTable table = new ExamplesTable("|state|tooltip|\n|enabled|value3|");
        when(parameterizedChecks.checkIfParametersAreSet(table)).thenReturn(true);
        parameterizedThenSteps.isButtonWithParametersFound(EQUAL_TO, 1, table);
        verifyZeroInteractions(searchActions);
        verify(descriptiveSoftAssert).recordFailedAssertion("Both name and imageSrc were not specified");
    }

    @Test
    void testIsButtonWithParametersFoundNotFoundName()
    {
        when(webUiContext.getSearchContext()).thenReturn(searchContext);
        ExamplesTable table = new ExamplesTable("|name|state|tooltip|\n|value2|enabled|value3|");
        when(parameterizedChecks.checkIfParametersAreSet(table)).thenReturn(true);
        when(searchActions.findElements(eq(searchContext), any(SearchAttributes.class))).thenReturn(List.of());
        parameterizedThenSteps.isButtonWithParametersFound(EQUAL_TO, 1, table);
        verifyAssert(BUTTONS, List.of());
    }

    @Test
    void testIsFiledWithParametersFoundNullName()
    {
        ExamplesTable table = new ExamplesTable("|state|\n|DISABLED|");
        when(parameterizedChecks.checkIfParametersAreSet(any(ExamplesTable.class))).thenReturn(true);
        assertThrows(UnsupportedOperationException.class,
            () -> parameterizedThenSteps.isFieldsWithParametersFound(EQUAL_TO, 1, table));
    }

    @Test
    void testIsFiledWithParametersFoundSingleParameterNotSet()
    {
        ExamplesTable parameters = new ExamplesTable("|fieldName|state|\n|testName||");
        when(parameterizedChecks.checkIfParametersAreSet(parameters)).thenReturn(false);
        parameterizedThenSteps.isFieldsWithParametersFound(EQUAL_TO, 1, parameters);
        verifyZeroInteractions(descriptiveSoftAssert);
        verifyZeroInteractions(parameterizedValidations);
    }

    @Test
    void testIsFiledWithParametersFoundText()
    {
        when(webUiContext.getSearchContext()).thenReturn(searchContext);
        ExamplesTable parameters = new ExamplesTable("|fieldName|text|\n|testName|text|");
        when(parameterizedChecks.checkIfParametersAreSet(parameters)).thenReturn(true);
        parameterizedThenSteps.isFieldsWithParametersFound(EQUAL_TO, 1, parameters);
        verifyAssert("fields", elements);
    }

    @Test
    void testIsImageWithParametersFoundSrc()
    {
        when(webUiContext.getSearchContext()).thenReturn(searchContext);
        parameters = new ExamplesTable("|imageSrc|\n|value|");
        when(parameterizedChecks.checkIfParametersAreSet(parameters)).thenReturn(true);
        parameterizedThenSteps.isImageWithParametersFound(EQUAL_TO, 1, parameters);
        verifyAssert(IMAGES, elements);
    }

    @Test
    void testIsImageWithParametersFoundSrcAndState()
    {
        when(webUiContext.getSearchContext()).thenReturn(searchContext);
        parameters = new ExamplesTable("|imageSrc|state|\n|value|enabled|");
        when(parameterizedChecks.checkIfParametersAreSet(parameters)).thenReturn(true);
        parameterizedThenSteps.isImageWithParametersFound(EQUAL_TO, 1, parameters);
        verifyAssert(IMAGES, elements);
    }

    @Test
    void testIsImageWithParametersFoundSrcAndTooltipAndState()
    {
        when(webUiContext.getSearchContext()).thenReturn(searchContext);
        parameters = new ExamplesTable("|imageSrc|state|tooltip|\n|value1|enabled|value3|");
        when(parameterizedChecks.checkIfParametersAreSet(parameters)).thenReturn(true);
        parameterizedThenSteps.isImageWithParametersFound(EQUAL_TO, 1, parameters);
        verifyAssert(IMAGES, elements);
    }

    @Test
    void testIsImageWithParametersFoundNoParameters()
    {
        parameters = new ExamplesTable("");
        when(parameterizedChecks.checkIfParametersAreSet(parameters)).thenReturn(false);
        parameterizedThenSteps.isImageWithParametersFound(EQUAL_TO, 1, parameters);
        verifyZeroInteractions(descriptiveSoftAssert);
    }

    @Test
    void testIsImageWithParametersFoundEmptyParameter()
    {
        parameters = new ExamplesTable("|imageSrc|\n||");
        when(parameterizedChecks.checkIfParametersAreSet(parameters)).thenReturn(false);
        parameterizedThenSteps.isImageWithParametersFound(EQUAL_TO, 1, parameters);
        verifyZeroInteractions(descriptiveSoftAssert);
        verifyZeroInteractions(parameterizedValidations);
    }

    private void verifyAssert(String elementName, List<WebElement> elementsList)
    {
        verify(parameterizedValidations).assertNumber(elementName, EQUAL_TO, 1, elementsList);
    }

    @Test
    void isFrameWithParametersFoundCorrectParameters()
    {
        when(webUiContext.getSearchContext()).thenReturn(searchContext);
        parameters = new ExamplesTable("|state|attributeName|attributeValue|\n"
                + "|VISIBLE|testAttributeName|testAttributeValue|");
        when(parameterizedChecks.checkIfParametersAreSet(parameters)).thenReturn(true);
        SearchAttributes attributes = ElementPattern.getFrameSearchAttributes(TEST_ATTRIBUTE_NAME, TEST_ATTRIBUTE_VALUE)
                .addFilter(ActionAttributeType.STATE, VISIBLE);
        mockSearchResults(attributes);
        parameterizedThenSteps.isFrameWithParametersFound(EQUAL_TO, 1, parameters);
        verifyAssert(FRAMES, elements);
    }

    @Test
    void isFrameWithParametersFoundIncorrectParameters()
    {
        String table = "|state|attributeName|attributeValue|\n |VISIBLE|testAttributeName|incorrectTestAttributeValue|";
        parameters = new ExamplesTable(table);
        when(parameterizedChecks.checkIfParametersAreSet(parameters)).thenReturn(true);
        parameterizedThenSteps.isFrameWithParametersFound(EQUAL_TO, 1, parameters);
        verifyAssert(FRAMES, List.of());
    }

    @Test
    void isFrameWithParametersFoundEmptyParameters()
    {
        parameters = new ExamplesTable("");
        when(parameterizedChecks.checkIfParametersAreSet(parameters)).thenReturn(false);
        parameterizedThenSteps.isFrameWithParametersFound(EQUAL_TO, 1, parameters);
        verifyZeroInteractions(descriptiveSoftAssert);
        verifyZeroInteractions(searchActions);
    }

    @Test
    void isFrameWithParametersFoundAttributeValueIsNotSetAttributeNameIsSet()
    {
        String table = "|state|attributeName|\n |VISIBLE|testAttributeName|";
        parameters = new ExamplesTable(table);
        when(parameterizedChecks.checkIfParametersAreSet(parameters)).thenReturn(true);
        parameterizedThenSteps.isFrameWithParametersFound(EQUAL_TO, 1, parameters);
        verify(descriptiveSoftAssert)
                .recordFailedAssertion(String.format(INVALID_PARAMETERS, TEST_ATTRIBUTE_NAME, NULL));
        verifyZeroInteractions(searchActions);
    }

    @Test
    void isFrameWithParametersFoundAttributeValueIsSetAttributeNameIsNot()
    {
        String table = "|state|attributeValue|\n |VISIBLE|testAttributeValue|";
        parameters = new ExamplesTable(table);
        when(parameterizedChecks.checkIfParametersAreSet(parameters)).thenReturn(true);
        parameterizedThenSteps.isFrameWithParametersFound(EQUAL_TO, 1, parameters);
        verify(descriptiveSoftAssert)
                .recordFailedAssertion(String.format(INVALID_PARAMETERS, NULL, TEST_ATTRIBUTE_VALUE));
        verifyZeroInteractions(searchActions);
    }

    @Test
    void isFrameWithParametersFoundAttributeValueIsNotSetAttributeNameIsNotSet()
    {
        parameters = new ExamplesTable(STATE_VISIBLE);
        when(parameterizedChecks.checkIfParametersAreSet(parameters)).thenReturn(true);
        parameterizedThenSteps.isFrameWithParametersFound(EQUAL_TO, 1, parameters);
        verify(descriptiveSoftAssert).recordFailedAssertion(String.format(INVALID_PARAMETERS, NULL, NULL));
        verifyZeroInteractions(searchActions);
    }

    @Test
    void testIsScrollbarsFound()
    {
        when(webUiContext.getSearchContext()).thenReturn(searchContext);
        SearchAttributes attributes = new SearchAttributes(ActionAttributeType.XPATH,
                LocatorUtil.getXPath("div[@*[contains(.,'scrollbar')]]"));
        elements.add(element);
        when(searchActions.findElements(eq(searchContext), eq(attributes))).thenReturn(elements);
        parameterizedThenSteps.isScrollbarFound(EQUAL_TO, 1);
        verifyAssert("scrollbars", elements);
    }

    @Test
    void testIsDDWithParametersFound()
    {
        when(webUiContext.getSearchContext()).thenReturn(searchContext);
        String xpath = LocatorUtil.getXPath(ElementPattern.SELECT_PATTERN, NAME);
        SearchAttributes attributes = new SearchAttributes(ActionAttributeType.XPATH, xpath)
                .addFilter(ActionAttributeType.DROP_DOWN_STATE, DISABLED);
        when(searchActions.findElements(eq(searchContext), eq(attributes))).thenReturn(elements);
        ExamplesTable parameters = new ExamplesTable("|dropDownName|state|\n|testName|DISABLED|");
        when(parameterizedChecks.checkIfParametersAreSet(parameters)).thenReturn(true);
        parameterizedThenSteps.isDropDownWithParametrsFound(EQUAL_TO, 1, parameters);
        verifyAssert(DROP_DOWNS, elements);
    }

    @Test
    void testIsDDWithParametersFoundMultySelect()
    {
        when(webUiContext.getSearchContext()).thenReturn(searchContext);
        String xpath = LocatorUtil.getXPath(ElementPattern.SELECT_PATTERN, NAME);
        SearchAttributes attributes = new SearchAttributes(ActionAttributeType.XPATH, xpath)
                .addFilter(ActionAttributeType.DROP_DOWN_STATE, DISABLED)
                .addFilter(ActionAttributeType.DROP_DOWN_TEXT, "testText1")
                .addFilter(ActionAttributeType.DROP_DOWN_TEXT, "testText2");
        when(searchActions.findElements(eq(searchContext), eq(attributes))).thenReturn(elements);
        ExamplesTable parameters = new ExamplesTable(
                "|dropDownName|state|selectedText1|selectedText2|\n|testName|DISABLED|testText1|testText2|");
        when(parameterizedChecks.checkIfParametersAreSet(parameters)).thenReturn(true);
        parameterizedThenSteps.isDropDownWithParametrsFound(EQUAL_TO, 1, parameters);
        verifyAssert(DROP_DOWNS, elements);
    }

    @Test
    void testIsDDWithParametersFoundNullName()
    {
        ExamplesTable parameters = new ExamplesTable("|selectedText1|\n|testText|");
        when(parameterizedChecks.checkIfParametersAreSet(parameters)).thenReturn(true);
        parameterizedThenSteps.isDropDownWithParametrsFound(EQUAL_TO, 1, parameters);
        verify(descriptiveSoftAssert).recordFailedAssertion("No drop down name was specified");
    }

    @Test
    void testIsDDWithParametersFoundEmptyParameter()
    {
        ExamplesTable parameters = new ExamplesTable("|dropDownName|selectedText1|\n|||");
        when(parameterizedChecks.checkIfParametersAreSet(parameters)).thenReturn(false);
        parameterizedThenSteps.isDropDownWithParametrsFound(EQUAL_TO, 1, parameters);
        verifyZeroInteractions(descriptiveSoftAssert);
        verifyZeroInteractions(parameterizedValidations);
    }

    @Test
    void testIsJSFoundEmptyParameters()
    {
        parameters = new ExamplesTable("|srcPart|\n ||");
        when(parameterizedChecks.checkIfParametersAreSet(parameters)).thenReturn(false);
        parameterizedThenSteps.isJSFound(EQUAL_TO, 1, parameters);
        verifyZeroInteractions(descriptiveSoftAssert);
        verifyZeroInteractions(parameterizedValidations);
    }

    @Test
    void testIsJSFound()
    {
        parameters = new ExamplesTable("|srcPart|\n |https://someSrc.js|");
        when(parameterizedChecks.checkIfParametersAreSet(parameters)).thenReturn(true);
        parameterizedThenSteps.isJSFound(EQUAL_TO, 1, parameters);
        verifyAssert(SCRIPTS, elements);
    }

    @Test
    void testIsJSFoundUnknownParameters()
    {
        parameters = new ExamplesTable("|src|\n |https://someSrc.js|");
        when(parameterizedChecks.checkIfParametersAreSet(parameters)).thenReturn(true);
        parameterizedThenSteps.isJSFound(EQUAL_TO, 1, parameters);
        verifyAssert(SCRIPTS, List.of());
    }

    @Test
    void isLinkWithParametersFoundCorrectParameters()
    {
        parameters = new ExamplesTable(TABLE_LINK);
        when(parameterizedChecks.checkIfParametersAreSet(parameters)).thenReturn(true);
        parameterizedThenSteps.isLinksWithParametersFound(EQUAL_TO, 1, parameters);
        verifyAssert(LINKS, elements);
    }

    @Test
    void isLinkWithParametersFoundParametersAreNotSet()
    {
        when(parameterizedChecks.checkIfParametersAreSet(parameters)).thenReturn(false);
        parameterizedThenSteps.isLinksWithParametersFound(EQUAL_TO, 1, parameters);
        verifyZeroInteractions(searchActions);
    }

    @Test
    void isLinkWithParametersFoundLinksNotFound()
    {
        parameters = new ExamplesTable(TABLE_LINK);
        when(parameterizedChecks.checkIfParametersAreSet(parameters)).thenReturn(true);
        parameterizedThenSteps.isLinksWithParametersFound(EQUAL_TO, 1, parameters);
        verifyAssert(LINKS, List.of());
    }

    private void stubRadioButton(String state)
    {
        String label = "label";
        elements.add(element);
        when(searchActions.findElements(searchContext, LocatorUtil.getXPathLocator(ElementPattern.LABEL_PATTERN, NAME)))
                .thenReturn(elements);
        when(element.getAttribute("for")).thenReturn(label);
        SearchAttributes attributes = new SearchAttributes(ActionAttributeType.XPATH,
                LocatorUtil.getXPath(ElementPattern.RADIO_OPTION_INPUT_PATTERN, label))
                        .addFilter(ActionAttributeType.STATE, state);
        when(searchActions.findElements(searchContext, attributes)).thenReturn(elements);
    }

    private void mockSearchResults(SearchAttributes attributes)
    {
        elements.add(element);
        when(searchActions.findElements(searchContext, attributes)).thenReturn(elements);
    }

    @Test
    void testIsCheckboxWithParametersFoundByName()
    {
        when(webUiContext.getSearchContext()).thenReturn(searchContext);
        parameters = new ExamplesTable(NAME_VALUE);
        when(parameterizedChecks.checkIfParametersAreSet(parameters)).thenReturn(true);
        SearchAttributes attributes = new SearchAttributes(ActionAttributeType.CHECKBOX_NAME, "value")
                .addFilter(ActionAttributeType.STATE, null);
        mockSearchResults(attributes);
        parameterizedThenSteps.isCheckboxWithParametersFound(EQUAL_TO, 1, parameters);
        verifyAssert(CHECKBOX, elements);
    }

    @Test
    void testIsCheckboxWithParametersFoundByAttribute()
    {
        when(webUiContext.getSearchContext()).thenReturn(searchContext);
        parameters = new ExamplesTable("|attributeType|attributeValue|\n|type1|value1|");
        when(parameterizedChecks.checkIfParametersAreSet(parameters)).thenReturn(true);
        when(checboxSteps.getCheckboxXpathByAttributeAndValue("type1", "value1"))
            .thenReturn(XPATH);
        SearchAttributes attributes = new SearchAttributes(ActionAttributeType.XPATH, XPATH)
                .addFilter(ActionAttributeType.STATE, null);
        mockSearchResults(attributes);
        parameterizedThenSteps.isCheckboxWithParametersFound(EQUAL_TO, 1, parameters);
        verifyAssert(CHECKBOX, elements);
    }

    @Test
    void testIsCheckboxWithParametersFoundBothRequiredParams()
    {
        parameters = new ExamplesTable("|name|attributeType|attributeValue|\n|value1|type1|value1|");
        when(parameterizedChecks.checkIfParametersAreSet(parameters)).thenReturn(true);
        parameterizedThenSteps.isCheckboxWithParametersFound(EQUAL_TO, 1, parameters);
        verify(descriptiveSoftAssert).recordFailedAssertion(anyString());
        verifyZeroInteractions(searchActions);
    }

    @Test
    void testIsCheckboxWithParametersFoundIncompleteAttribute()
    {
        parameters = new ExamplesTable("|attributeType|\n|value1|");
        when(parameterizedChecks.checkIfParametersAreSet(parameters)).thenReturn(true);
        parameterizedThenSteps.isCheckboxWithParametersFound(EQUAL_TO, 1, parameters);
        String noSearchParamsProvided = "No search parameters provided: either name or both attributeType "
                + "with attributeValue should be specified";
        verify(descriptiveSoftAssert).recordFailedAssertion(noSearchParamsProvided);
        verifyZeroInteractions(searchActions);
    }

    @Test
    void testIsCheckboxWithParametersFoundEmptyParams()
    {
        when(parameterizedChecks.checkIfParametersAreSet(parameters)).thenReturn(false);
        parameterizedThenSteps.isCheckboxWithParametersFound(EQUAL_TO, 1, parameters);
        verifyZeroInteractions(searchActions);
    }

    @Test
    void testIsElementWithParametersFound()
    {
        when(webUiContext.getSearchContext()).thenReturn(searchContext);
        parameters = new ExamplesTable("|xpath|\n|//body|");
        when(parameterizedChecks.checkIfParametersAreSet(parameters)).thenReturn(true);
        elements.add(element);
        when(parameterizedSearchActions.findElements(eq(searchContext),
                any(SearchInputData.class))).thenReturn(elements);
        when(parameterizedSearchActions.findElementsWithAttributes(eq(searchContext),
                any(SearchInputData.class), eq(elements))).thenReturn(elements);
        when(parameterizedSearchActions.filterElementsWithCssProperties(any(SearchInputData.class),
                eq(elements))).thenReturn(elements);
        parameterizedThenSteps.isElementWithParametersFound(EQUAL_TO, 1, parameters);
        verify(parameterizedValidations).assertNumberWithLocationReporting(ELEMENTS, EQUAL_TO, 1, elements);
    }

    @Test
    void testIsElementWithParametersFoundNoAttribute()
    {
        when(webUiContext.getSearchContext()).thenReturn(searchContext);
        parameters = new ExamplesTable("|xpath|attributeName|\n|//body|testName|");
        when(parameterizedChecks.checkIfParametersAreSet(parameters)).thenReturn(true);
        elements.add(element);
        when(parameterizedSearchActions.findElements(eq(searchContext),
                any(SearchInputData.class))).thenReturn(elements);
        when(parameterizedSearchActions.findElementsWithAttributes(eq(searchContext),
                any(SearchInputData.class), eq(elements))).thenReturn(List.of());
        parameterizedThenSteps.isElementWithParametersFound(EQUAL_TO, 1, parameters);
        verifyAssert(ELEMENTS, List.of());
    }
}
