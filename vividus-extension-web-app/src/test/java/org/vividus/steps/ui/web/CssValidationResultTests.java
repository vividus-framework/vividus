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
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;
import org.vividus.steps.StringComparisonRule;

class CssValidationResultTests
{
    @Test
    void testToCoverage()
    {
        final String key = "key";
        final String value = "value";
        final String expected = "expected";
        CssValidationParameters parameters = new CssValidationParameters(key, StringComparisonRule.CONTAINS, expected);
        CssValidationParameters newParameters = new CssValidationParameters();
        newParameters.setCssProperty(parameters.getCssProperty());
        newParameters.setComparisonRule(parameters.getComparisonRule());
        newParameters.setExpectedValue(parameters.getExpectedValue());

        CssValidationResult result = new CssValidationResult(newParameters, expected, true);
        result.setActualValue(value);
        result.setPassed(false);

        assertEquals(key, result.getCssProperty());
        assertEquals(value, result.getActualValue());
        assertEquals(StringComparisonRule.CONTAINS, result.getComparisonRule());
        assertEquals(expected, result.getExpectedValue());
        assertFalse(result.isPassed());
    }
}
