/*
 * Copyright 2019-2024 the original author or authors.
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

package org.vividus.ui.web.playwright.steps;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Set;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.BoundingBox;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.context.VariableContext;
import org.vividus.softassert.ISoftAssert;
import org.vividus.steps.ComparisonRule;
import org.vividus.ui.web.playwright.UiContext;
import org.vividus.ui.web.playwright.locator.PlaywrightLocator;
import org.vividus.variable.VariableScope;

@ExtendWith(MockitoExtension.class)
class ElementStepsTests
{
    private static final String XPATH = "xpath";
    private static final String VARIABLE_NAME = "variableName";
    private static final String LOCATOR_VALUE = "div";
    private static final String ATTRIBUTE_NAME = "attributeName";
    private static final String ATTRIBUTE_VALUE = "attributeValue";
    private static final Set<VariableScope> VARIABLE_SCOPE = Set.of(VariableScope.STORY);

    @Mock private UiContext uiContext;
    @Mock private ISoftAssert softAssert;
    @Mock private VariableContext variableContext;
    @InjectMocks private ElementSteps steps;

    @ParameterizedTest
    // CHECKSTYLE:OFF
    @CsvSource(quoteCharacter = '`', value = {
            ".//a,   3, 3, The number of elements found by 'xpath(.//a) with visibility: visible' is 3",
            ".//div, 4, 3, `The number of elements found by 'xpath(.//div) with visibility: visible' is 4, but 3 were expected`",
            "/h1,    3, 1, `The number of elements found by 'xpath(/h1) with visibility: visible' is 3, but 1 was expected`",
            "/h2,    1, 0, `The number of elements found by 'xpath(/h2) with visibility: visible' is 1, but 0 were expected`"
    })
    // CHECKSTYLE:ON
    void shouldRecordAssertionOnValidationIfNumberOfElementIsEqualToExpected(String locatorValue, int actualNumber,
            int expectedNumber, String assertionMessage)
    {
        var playwrightLocator = new PlaywrightLocator(XPATH, locatorValue);
        Locator locator = mock();
        when(uiContext.locateElement(playwrightLocator)).thenReturn(locator);
        when(locator.count()).thenReturn(actualNumber);
        steps.assertElementsNumber(playwrightLocator, ComparisonRule.EQUAL_TO, expectedNumber);
        verify(softAssert).recordAssertion(actualNumber == expectedNumber, assertionMessage);
    }

    @ParameterizedTest
    // CHECKSTYLE:OFF
    @CsvSource(quoteCharacter = '`', value = {
            "a,   3, 2, true, `The number of elements found by 'css(a) with visibility: visible' is 3, it is greater than 2`",
            "div, 3, 4, false, `The number of elements found by 'css(div) with visibility: visible' is 3, but it is not greater than 4`"
    })
    // CHECKSTYLE:ON
    void shouldRecordAssertionOnValidationIfNumberOfElementMatchesToExpected(String locatorValue, int actualNumber,
            int expectedNumber, boolean passed, String assertionMessage)
    {
        var playwrightLocator = new PlaywrightLocator("css", locatorValue);
        Locator locator = mock();
        when(uiContext.locateElement(playwrightLocator)).thenReturn(locator);
        when(locator.count()).thenReturn(actualNumber);
        steps.assertElementsNumber(playwrightLocator, ComparisonRule.GREATER_THAN, expectedNumber);
        verify(softAssert).recordAssertion(passed, assertionMessage);
    }

    @Test
    void shouldSaveElementCoordinatesAndSize()
    {
        var playwrightLocator = new PlaywrightLocator(XPATH, LOCATOR_VALUE);
        Locator locator = mock();
        when(uiContext.locateElement(playwrightLocator)).thenReturn(locator);
        BoundingBox box = new BoundingBox();
        box.x = 1;
        box.y = 2;
        box.height = 3;
        box.width = 4;
        when(locator.boundingBox()).thenReturn(box);

        steps.saveElementCoordinatesAndSize(playwrightLocator, VARIABLE_SCOPE, VARIABLE_NAME);

        verify(variableContext).putVariable(VARIABLE_SCOPE, VARIABLE_NAME, Map.of(
            "x", box.x,
            "y", box.y,
            "height", box.height,
            "width", box.width
        ));
    }

    @Test
    void shouldSaveNumberOfElementsToVariable()
    {
        int elementCount = 5;
        var playwrightLocator = new PlaywrightLocator(XPATH, LOCATOR_VALUE);
        Locator locator = mock();
        when(uiContext.locateElement(playwrightLocator)).thenReturn(locator);
        when(locator.count()).thenReturn(elementCount);
        steps.saveNumberOfElementsToVariable(playwrightLocator, VARIABLE_SCOPE, VARIABLE_NAME);
        verify(variableContext).putVariable(VARIABLE_SCOPE, VARIABLE_NAME, elementCount);
    }

    @Test
    void shouldSaveContextElementAttributeValueToVariable()
    {
        Locator locator = mock();
        when(uiContext.getCurrentContexOrPageRoot()).thenReturn(locator);
        when(locator.getAttribute(ATTRIBUTE_NAME)).thenReturn(ATTRIBUTE_VALUE);
        steps.saveContextElementAttributeValueToVariable(ATTRIBUTE_NAME, VARIABLE_SCOPE, VARIABLE_NAME);
        verify(variableContext).putVariable(VARIABLE_SCOPE, VARIABLE_NAME, ATTRIBUTE_VALUE);
        verifyNoInteractions(softAssert);
    }

    @Test
    void shouldSaveAttributeValueOfElement()
    {
        var playwrightLocator = new PlaywrightLocator(XPATH, LOCATOR_VALUE);
        Locator locator = mock();
        when(uiContext.locateElement(playwrightLocator)).thenReturn(locator);
        when(locator.getAttribute(ATTRIBUTE_NAME)).thenReturn(ATTRIBUTE_VALUE);
        steps.saveAttributeValueOfElement(ATTRIBUTE_NAME, playwrightLocator, VARIABLE_SCOPE, VARIABLE_NAME);
        verify(variableContext).putVariable(VARIABLE_SCOPE, VARIABLE_NAME, ATTRIBUTE_VALUE);
        verifyNoInteractions(softAssert);
    }

    @Test
    void shouldNotSaveAttributeValueOfContextIfAttributeIsNotPresent()
    {
        Locator locator = mock();
        when(uiContext.getCurrentContexOrPageRoot()).thenReturn(locator);
        when(locator.getAttribute(ATTRIBUTE_NAME)).thenReturn(null);
        steps.saveContextElementAttributeValueToVariable(ATTRIBUTE_NAME, VARIABLE_SCOPE, VARIABLE_NAME);
        verifyIfAttributeIsNotPresent();
    }

    @Test
    void shouldNotSaveAttributeValueOfElementIfAttributeIsNotPresent()
    {
        var playwrightLocator = new PlaywrightLocator(XPATH, LOCATOR_VALUE);
        Locator locator = mock();
        when(uiContext.locateElement(playwrightLocator)).thenReturn(locator);
        when(locator.getAttribute(ATTRIBUTE_NAME)).thenReturn(null);
        steps.saveAttributeValueOfElement(ATTRIBUTE_NAME, playwrightLocator, VARIABLE_SCOPE, VARIABLE_NAME);
        verifyIfAttributeIsNotPresent();
    }

    private void verifyIfAttributeIsNotPresent()
    {
        verify(softAssert).recordFailedAssertion(String.format("The '%s' attribute does not exist", ATTRIBUTE_NAME));
        verifyNoInteractions(variableContext);
    }
}
