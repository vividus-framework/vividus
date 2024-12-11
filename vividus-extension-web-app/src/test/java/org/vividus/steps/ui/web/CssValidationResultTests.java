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
        final String newKey = "new_key";
        final String newValue = "new_value";
        final String newExpected = "new_expected";
        CssValidationResult result = new CssValidationResult("key", "value", StringComparisonRule.CONTAINS,
                "expected", true);
        result.setCssProperty(newKey);
        result.setActualValue(newValue);
        result.setComparisonRule(StringComparisonRule.IS_EQUAL_TO);
        result.setExpectedValue(newExpected);
        result.setPassed(false);
        assertEquals(newKey, result.getCssProperty());
        assertEquals(newValue, result.getActualValue());
        assertEquals(StringComparisonRule.IS_EQUAL_TO, result.getComparisonRule());
        assertEquals(newExpected, result.getExpectedValue());
        assertFalse(result.isPassed());
    }
}
