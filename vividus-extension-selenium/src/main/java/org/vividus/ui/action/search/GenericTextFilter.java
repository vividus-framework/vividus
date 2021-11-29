/*
 * Copyright 2019-2021 the original author or authors.
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

package org.vividus.ui.action.search;

import org.openqa.selenium.WebElement;
import org.vividus.steps.StringComparisonRule;
import org.vividus.ui.action.ElementActions;

public class GenericTextFilter implements IElementFilterAction
{
    private final LocatorType locatorType;
    private final StringComparisonRule comparisonRule;
    private final ElementActions elementActions;

    public GenericTextFilter(LocatorType locatorType, StringComparisonRule comparisonRule,
            ElementActions elementActions)
    {
        this.locatorType = locatorType;
        this.elementActions = elementActions;
        this.comparisonRule = comparisonRule;
    }

    @Override
    public boolean matches(WebElement element, String text)
    {
        return comparisonRule.createMatcher(text).matches(elementActions.getElementText(element));
    }

    @Override
    public LocatorType getType()
    {
        return locatorType;
    }
}
