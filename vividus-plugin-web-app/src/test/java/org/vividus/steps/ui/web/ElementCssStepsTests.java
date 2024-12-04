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

package org.vividus.steps.ui.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebElement;
import org.vividus.context.VariableContext;
import org.vividus.reporter.event.IAttachmentPublisher;
import org.vividus.selenium.locator.Locator;
import org.vividus.softassert.SoftAssert;
import org.vividus.steps.StringComparisonRule;
import org.vividus.steps.ui.validation.IBaseValidations;
import org.vividus.steps.ui.validation.IDescriptiveSoftAssert;
import org.vividus.ui.context.IUiContext;
import org.vividus.ui.web.action.JavascriptActions;
import org.vividus.ui.web.action.WebElementActions;
import org.vividus.ui.web.validation.CssValidations;
import org.vividus.variable.VariableScope;

@ExtendWith(MockitoExtension.class)
class ElementCssStepsTests
{
    private static final String ELEMENT_HAS_CORRECT_CSS_PROPERTY_VALUE = "Element has correct css property value";
    private static final String CSS_PROPERTY_VALUE_PART_IS_CORRECT = "Css property value part is correct";
    private static final String CSS_PART_VALUE = "Value";
    private static final String CSS_VALUE = "cssValue";
    private static final String CSS_NAME = "cssName";
    private static final String ELEMENT_HAS_CSS_PROPERTY_CONTAINING_VALUE =
            "Element has CSS property '" + CSS_NAME + "' containing value '" + CSS_PART_VALUE + "'";
    private static final String VARIABLE_NAME = "variableName";
    private static final String ELEMENT_WITH_CSS_PROPERTY = "The element to get the CSS property value";
    private static final Set<VariableScope> VARIABLE_SCOPE = Set.of(VariableScope.SCENARIO);

    @Mock private IBaseValidations baseValidations;
    @Mock private IUiContext uiContext;
    @Mock private WebElementActions webElementActions;
    @Mock private WebElement webElement;
    @Mock private IDescriptiveSoftAssert descriptiveSoftAssert;
    @Mock private VariableContext variableContext;
    @Mock private JavascriptActions javascriptActions;
    @Mock private SoftAssert softAssert;
    @Mock private CssValidations cssValidations;
    @Mock private IAttachmentPublisher attachmentPublisher;
    @InjectMocks private ElementCssSteps elementCssSteps;

    @Test
    void testIsElementHasRightCss()
    {
        mockWebElementCssValue();
        elementCssSteps.doesElementHaveRightCss(CSS_NAME, CSS_VALUE);
        verify(descriptiveSoftAssert).assertEquals(ELEMENT_HAS_CORRECT_CSS_PROPERTY_VALUE, CSS_VALUE, CSS_VALUE);
    }

    @Test
    void testIsNullElementHasRightCss()
    {
        when(uiContext.getSearchContext(WebElement.class)).thenReturn(Optional.empty());
        elementCssSteps.doesElementHaveRightCss(CSS_NAME, CSS_VALUE);
        verifyNoInteractions(webElementActions, descriptiveSoftAssert);
    }

    @Test
    void testIsElementHasRightCssPart()
    {
        mockWebElementCssValue();
        elementCssSteps.doesElementHaveRightPartOfCssValue(CSS_NAME, CSS_PART_VALUE);
        verify(descriptiveSoftAssert).assertThat(eq(CSS_PROPERTY_VALUE_PART_IS_CORRECT),
                eq(ELEMENT_HAS_CSS_PROPERTY_CONTAINING_VALUE), eq(CSS_VALUE),
                argThat(matcher -> matcher.toString().contains(CSS_PART_VALUE)));
    }

    @Test
    void testDoesElementHasRightCss()
    {
        mockWebElementCssValue();
        elementCssSteps.doesElementHaveRightCss(CSS_NAME, StringComparisonRule.CONTAINS, CSS_PART_VALUE);
        verify(descriptiveSoftAssert).assertThat(eq("Element css property value is"), eq(CSS_VALUE),
                argThat(matcher -> matcher.toString().contains(CSS_PART_VALUE)));
    }

    @Test
    void shouldSaveCssPropertyValue()
    {
        var locator = mock(Locator.class);
        var webElement = mock(WebElement.class);
        when(baseValidations.assertElementExists(ELEMENT_WITH_CSS_PROPERTY, locator))
                .thenReturn(Optional.of(webElement));
        when(webElementActions.getCssValue(webElement, CSS_NAME)).thenReturn(CSS_VALUE);

        elementCssSteps.saveCssPropertyValue(CSS_NAME, locator, VARIABLE_SCOPE, VARIABLE_NAME);
        verify(variableContext).putVariable(VARIABLE_SCOPE, VARIABLE_NAME, CSS_VALUE);
        verifyNoInteractions(descriptiveSoftAssert);
    }

    @Test
    void shouldNotSaveCssPropertyValueIfTheValueIsNotFound()
    {
        var locator = mock(Locator.class);
        var webElement = mock(WebElement.class);
        when(baseValidations.assertElementExists(ELEMENT_WITH_CSS_PROPERTY, locator))
                .thenReturn(Optional.of(webElement));
        when(webElementActions.getCssValue(webElement, CSS_NAME)).thenReturn(StringUtils.EMPTY);

        elementCssSteps.saveCssPropertyValue(CSS_NAME, locator, VARIABLE_SCOPE, VARIABLE_NAME);
        verify(descriptiveSoftAssert).recordFailedAssertion(
                String.format("The '%s' CSS property does not exist", CSS_NAME));
        verifyNoInteractions(variableContext);
    }

    @Test
    void shouldNotSaveCssPropertyValueIfTheElementIsNotFound()
    {
        var locator = mock(Locator.class);
        when(baseValidations.assertElementExists(ELEMENT_WITH_CSS_PROPERTY, locator)).thenReturn(Optional.empty());

        elementCssSteps.saveCssPropertyValue(CSS_NAME, locator, VARIABLE_SCOPE, VARIABLE_NAME);
        verifyNoInteractions(descriptiveSoftAssert, variableContext);
    }

    @SuppressWarnings("PMD.NcssCount")
    @Test
    void testDoesElementHasCssProperties()
    {
        final String cssKey1 = "cssKey1";
        final String cssValue1 = "cssValue1";

        final CssValidationParameters cssValidationParameter1 = new CssValidationParameters(cssKey1,
                StringComparisonRule.IS_EQUAL_TO, cssValue1);
        final List<CssValidationParameters> cssValidationParameters = List.of(cssValidationParameter1);

        when(uiContext.getSearchContext(WebElement.class)).thenReturn(Optional.of(webElement));
        try (MockedStatic<org.vividus.util.ResourceUtils> resourceUtils =
                     mockStatic(org.vividus.util.ResourceUtils.class))
        {
            final String jsScript = "script";
            resourceUtils.when(() -> org.vividus.util.ResourceUtils
                    .loadResource("org/vividus/ui/web/get-element-computed-css-func.js")).thenReturn(jsScript);
            Map<String, String> elementCss = Map.of(cssKey1, cssValue1);
            when(javascriptActions.executeScript(jsScript, webElement)).thenReturn(elementCss);

            CssValidationResult cssValidationResult = new CssValidationResult(cssValidationParameter1, cssValue1, true);
            List<CssValidationResult> cssValidationResults = List.of(cssValidationResult);
            when(cssValidations.validateElementCss(cssValidationParameters, elementCss))
                    .thenReturn(cssValidationResults);

            elementCssSteps.doesElementHasCssProperties(cssValidationParameters);
        }
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, List<CssValidationResult>>> argumentCaptor = ArgumentCaptor.forClass(Map.class);
        verify(attachmentPublisher).publishAttachment(eq("templates/css_validation_result.ftl"),
                argumentCaptor.capture(), eq("CSS validation results"));
        List<CssValidationResult> actualCssValidationResults = argumentCaptor.getValue().get("cssResults");
        assertEquals(1, actualCssValidationResults.size());

        CssValidationResult result1 = actualCssValidationResults.get(0);
        assertEquals(cssKey1, result1.getCssProperty());
        assertEquals(cssValue1, result1.getActualValue());
        assertEquals(StringComparisonRule.IS_EQUAL_TO, result1.getComparisonRule());
        assertEquals(cssValue1, result1.getExpectedValue());
        assertTrue(result1.isPassed());
    }

    @Test
    void testDoesElementHasCssPropertiesNoElement()
    {
        when(uiContext.getSearchContext(WebElement.class)).thenReturn(Optional.empty());
        elementCssSteps.doesElementHasCssProperties(List.of());
        verifyNoInteractions(javascriptActions);
        verifyNoInteractions(softAssert);
        verifyNoInteractions(attachmentPublisher);
    }

    private void mockWebElementCssValue()
    {
        when(uiContext.getSearchContext(WebElement.class)).thenReturn(Optional.of(webElement));
        when(webElementActions.getCssValue(webElement, CSS_NAME)).thenReturn(CSS_VALUE);
    }
}
