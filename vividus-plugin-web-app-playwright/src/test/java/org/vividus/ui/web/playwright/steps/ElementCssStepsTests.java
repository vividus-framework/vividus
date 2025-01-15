/*
 * Copyright 2019-2025 the original author or authors.
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Locator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.reporter.event.IAttachmentPublisher;
import org.vividus.softassert.ISoftAssert;
import org.vividus.steps.StringComparisonRule;
import org.vividus.steps.ui.web.CssValidationParameters;
import org.vividus.steps.ui.web.CssValidationResult;
import org.vividus.ui.web.action.JavascriptActions;
import org.vividus.ui.web.playwright.UiContext;
import org.vividus.ui.web.playwright.action.ElementActions;
import org.vividus.ui.web.validation.CssValidations;
import org.vividus.util.ResourceUtils;

@ExtendWith(MockitoExtension.class)
class ElementCssStepsTests
{
    private static final String CSS_NAME = "cssName";
    private static final String CSS_VALUE = "cssValue";

    @Mock private UiContext uiContext;
    @Mock private ISoftAssert softAssert;
    @Mock private ElementActions elementActions;
    @Mock private JavascriptActions javascriptActions;
    @Mock private CssValidations cssValidations;
    @Mock private IAttachmentPublisher attachmentPublisher;
    @InjectMocks private ElementCssSteps steps;

    @Test
    void shouldAssertElementCssProperty()
    {
        Locator locator = mock();
        when(uiContext.getCurrentContexOrPageRoot()).thenReturn(locator);
        when(elementActions.getCssValue(locator, CSS_NAME)).thenReturn(CSS_VALUE);
        steps.assertElementCssProperty(CSS_NAME, StringComparisonRule.IS_EQUAL_TO, CSS_VALUE);
        verify(softAssert).assertThat(eq("Element css property value is"), eq(CSS_VALUE),
                argThat(matcher -> matcher.matches(CSS_VALUE)));
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

        Locator locator = mock();
        ElementHandle elementHandle = mock();
        when(locator.elementHandle()).thenReturn(elementHandle);

        when(uiContext.getCurrentContexOrPageRoot()).thenReturn(locator);
        try (MockedStatic<ResourceUtils> resourceUtils = mockStatic(ResourceUtils.class))
        {
            resourceUtils.when(() -> ResourceUtils
                    .loadResource("org/vividus/ui/web/get-element-computed-css-func.js")).thenReturn("script");
            String getAllCssScript = "arguments => {script}";
            Map<String, String> elementCss = Map.of(cssKey1, cssValue1);
            when(javascriptActions.executeScript(getAllCssScript, elementHandle)).thenReturn(elementCss);

            CssValidationResult cssValidationResult = new CssValidationResult(cssValidationParameter1, cssValue1, true);
            List<CssValidationResult> cssValidationResults = List.of(cssValidationResult);
            when(cssValidations.validateElementCss(cssValidationParameters, elementCss))
                    .thenReturn(cssValidationResults);

            steps.doesElementHasCssProperties(cssValidationParameters);
        }
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, List<CssValidationResult>>> argumentCaptor = ArgumentCaptor.forClass(Map.class);
        verify(attachmentPublisher).publishAttachment(eq("templates/css_validation_result.ftl"),
                argumentCaptor.capture(), eq("CSS validation results"));
        List<CssValidationResult> actualCssValidationResults = argumentCaptor.getValue().get("cssResults");
        assertEquals(1, actualCssValidationResults.size());

        CssValidationResult result = actualCssValidationResults.get(0);
        assertEquals(cssKey1, result.getCssProperty());
        assertEquals(cssValue1, result.getActualValue());
        assertEquals(StringComparisonRule.IS_EQUAL_TO, result.getComparisonRule());
        assertEquals(cssValue1, result.getExpectedValue());
        assertTrue(result.isPassed());
    }
}
