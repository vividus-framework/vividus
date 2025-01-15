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

package org.vividus.ui.web.validation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.CaseUtils;
import org.vividus.softassert.ISoftAssert;
import org.vividus.steps.StringComparisonRule;
import org.vividus.steps.ui.web.CssValidationParameters;
import org.vividus.steps.ui.web.CssValidationResult;

public class CssValidations
{
    private ISoftAssert softAssert;

    public CssValidations(ISoftAssert softAssert)
    {
        this.softAssert = softAssert;
    }

    public List<CssValidationResult> validateElementCss(List<CssValidationParameters> parameters,
                                                        Map<String, String> elementCss)
    {
        List<CssValidationResult> cssResults = new ArrayList<>();
        parameters.forEach(param ->
        {
            String cssName = param.getCssProperty();
            String expectedValue = param.getExpectedValue();
            StringComparisonRule comparisonRule = param.getComparisonRule();

            String actualCssValue = getCssValue(elementCss, cssName);
            boolean passed = softAssert.assertThat(String.format("CSS property '%s' value", cssName),
                    actualCssValue, comparisonRule.createMatcher(expectedValue));
            cssResults.add(new CssValidationResult(param, actualCssValue, passed));
        });
        return cssResults;
    }

    private String getCssValue(Map<String, String> cssMap, String cssName)
    {
        return Optional.ofNullable(cssMap.get(cssName)).orElseGet(() -> {
            String cssValueAsCamelCase = CaseUtils.toCamelCase(StringUtils.removeStart(cssName, '-'), false, '-');
            return cssMap.get(cssValueAsCamelCase);
        });
    }
}
