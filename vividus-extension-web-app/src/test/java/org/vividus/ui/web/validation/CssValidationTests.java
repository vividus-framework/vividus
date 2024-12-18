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

package org.vividus.ui.web.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.softassert.SoftAssert;
import org.vividus.steps.StringComparisonRule;
import org.vividus.steps.ui.web.CssValidationParameters;
import org.vividus.steps.ui.web.CssValidationResult;

@ExtendWith(MockitoExtension.class)
class CssValidationTests
{
    @Mock private SoftAssert softAssert;
    @InjectMocks private CssValidations cssValidations;

    @SuppressWarnings("PMD.NcssCount")
    @Test
    void testCssValidation()
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
                StringComparisonRule.CONTAINS, cssValue3);
        final List<CssValidationParameters> cssValidationParameters = List.of(cssValidationParameter1,
                cssValidationParameter2, cssValidationParameter3);

        Map<String, String> elementCss = Map.of(cssKey1, cssValue1, "cssKey2", cssValue2);

        when(softAssert.assertThat(eq("CSS property 'cssKey1' value"),
                eq(cssValue1), argThat(matcher -> matcher.toString().contains(cssValue1))))
                .thenReturn(true);
        when(softAssert.assertThat(eq("CSS property '-css-key2' value"),
                eq(cssValue2), argThat(matcher -> matcher.toString().contains(cssValue2))))
                .thenReturn(true);
        when(softAssert.assertThat(eq("CSS property 'css-key3' value"),
                eq(null), argThat(matcher -> matcher.toString().contains(cssValue3))))
                .thenReturn(false);

        List<CssValidationResult> actualCssValidationResults =
                cssValidations.validateElementCss(cssValidationParameters, elementCss);
        assertEquals(3, actualCssValidationResults.size());

        CssValidationResult result1 = actualCssValidationResults.get(0);
        assertEquals(cssKey1, result1.getCssProperty());
        assertEquals(cssValue1, result1.getActualValue());
        assertEquals(StringComparisonRule.IS_EQUAL_TO, result1.getComparisonRule());
        assertEquals(cssValue1, result1.getExpectedValue());
        assertTrue(result1.isPassed());

        CssValidationResult result2 = actualCssValidationResults.get(1);
        assertEquals(cssKey2, result2.getCssProperty());
        assertEquals(cssValue2, result2.getActualValue());
        assertEquals(StringComparisonRule.IS_EQUAL_TO, result2.getComparisonRule());
        assertEquals(cssValue2, result2.getExpectedValue());
        assertTrue(result2.isPassed());

        CssValidationResult result3 = actualCssValidationResults.get(2);
        assertEquals(cssKey3, result3.getCssProperty());
        assertNull(result3.getActualValue());
        assertEquals(StringComparisonRule.CONTAINS, result3.getComparisonRule());
        assertEquals(cssValue3, result3.getExpectedValue());
        assertFalse(result3.isPassed());
    }
}
