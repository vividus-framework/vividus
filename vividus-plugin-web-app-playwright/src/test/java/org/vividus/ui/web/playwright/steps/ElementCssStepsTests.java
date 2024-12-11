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
        final String cssKey2 = "-css-key2";
        final String cssValue2 = "cssValue2";
        final String cssKey3 = "css-key3";
        final String cssValue3 = "cssValue3";

        final CssValidationParameters cssValidationParameter1 = new CssValidationParameters(cssKey1,
                StringComparisonRule.IS_EQUAL_TO, cssValue1);
        final CssValidationParameters cssValidationParameter2 = new CssValidationParameters(cssKey2,
                StringComparisonRule.IS_EQUAL_TO, cssValue2);
        final CssValidationParameters cssValidationParameter3 = new CssValidationParameters(cssKey3,
                StringComparisonRule.IS_EQUAL_TO, cssValue3);
        final List<CssValidationParameters> cssValidationParameters = List.of(cssValidationParameter1,
                cssValidationParameter2, cssValidationParameter3);

        Locator locator = mock();
        ElementHandle elementHandle = mock();
        when(locator.elementHandle()).thenReturn(elementHandle);

        when(uiContext.getCurrentContexOrPageRoot()).thenReturn(locator);
        try (MockedStatic<ResourceUtils> resourceUtils =
                     mockStatic(ResourceUtils.class))
        {
            resourceUtils.when(() -> ResourceUtils
                    .loadResource("org/vividus/ui/web/get-element-computed-css-func.js")).thenReturn("script");
            String getAllCssScript = "([el]) => {scriptreturn getComputedStyleAsMap(el)}";
            Map<String, String> elementCss = Map.of(cssKey1, cssValue1, "cssKey2", cssValue2);
            when(javascriptActions.executeScript(getAllCssScript, elementHandle)).thenReturn(elementCss);

            when(softAssert.assertThat(eq("Element has CSS property 'cssKey1' containing value 'cssValue1'"),
                    eq(cssValue1), argThat(matcher -> matcher.toString().contains(cssValue1))))
                    .thenReturn(true);
            when(softAssert.assertThat(eq("Element has CSS property '-css-key2' containing value 'cssValue2'"),
                    eq(cssValue2), argThat(matcher -> matcher.toString().contains(cssValue2))))
                    .thenReturn(true);
            when(softAssert.assertThat(eq("Element has CSS property 'css-key3' containing value 'cssValue3'"),
                    eq(null), argThat(matcher -> matcher.toString().contains(cssValue3))))
                    .thenReturn(false);

            steps.doesElementHasCssProperties(cssValidationParameters);
        }
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, List<CssValidationResult>>> argumentCaptor = ArgumentCaptor.forClass(Map.class);
        verify(attachmentPublisher).publishAttachment(eq("templates/css_validation_result.ftl"),
                argumentCaptor.capture(), eq("Css validation results"));
        List<CssValidationResult> actualCssValidationResults = argumentCaptor.getValue().get("cssResults");
        assertEquals(3, actualCssValidationResults.size());

        CssValidationResult result2 = actualCssValidationResults.get(1);
        assertEquals(cssKey2, result2.getCssProperty());
        assertEquals(cssValue2, result2.getActualValue());
        assertEquals(StringComparisonRule.IS_EQUAL_TO, result2.getComparisonRule());
        assertEquals(cssValue2, result2.getExpectedValue());
        assertTrue(result2.isPassed());
    }
}
