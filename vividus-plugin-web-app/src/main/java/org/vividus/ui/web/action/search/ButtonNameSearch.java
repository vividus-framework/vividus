/*
 * Copyright 2019-2020 the original author or authors.
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

package org.vividus.ui.web.action.search;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.vividus.ui.web.util.LocatorUtil;

public class ButtonNameSearch extends AbstractElementSearchAction implements IElementSearchAction
{
    private static final String BUTTON_WITH_ANY_ATTRIBUTE_NAME_PATTERN = "*[(local-name()='button' and "
            + "(@*=%1$s or text()=%1$s)) or (local-name()='input' and ((@type='submit' or @type='button') and "
            + "(@*=%1$s or text()=%1$s)))]";

    @Override
    public List<WebElement> search(SearchContext searchContext, SearchParameters parameters)
    {
        String value = parameters.getValue();
        By locator = LocatorUtil.getXPathLocator(".//" + BUTTON_WITH_ANY_ATTRIBUTE_NAME_PATTERN, value);
        List<WebElement> buttons = findElements(searchContext, locator, parameters);
        if (buttons.isEmpty())
        {
            return findElementsByText(searchContext, locator, parameters, "button", "input");
        }
        return buttons;
    }
}
