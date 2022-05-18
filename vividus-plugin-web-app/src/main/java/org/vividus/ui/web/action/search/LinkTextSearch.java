/*
 * Copyright 2019-2022 the original author or authors.
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
import org.vividus.ui.action.search.IElementSearchAction;
import org.vividus.ui.action.search.SearchParameters;
import org.vividus.ui.util.XpathLocatorUtils;

public class LinkTextSearch extends AbstractWebElementSearchAction implements IElementSearchAction
{
    private static final String LINK_WITH_ANY_ATTRIBUTE_OR_TEXT = ".//a[text()=%1$s or @*=%1$s or *=%1$s]";

    public LinkTextSearch()
    {
        super(WebLocatorType.LINK_TEXT);
    }

    @Override
    public List<WebElement> search(SearchContext searchContext, SearchParameters parameters)
    {
        String text = parameters.getValue();
        List<WebElement> links = findElements(searchContext, By.linkText(text), parameters);
        if (links.isEmpty())
        {
            By locator = XpathLocatorUtils.getXPathLocator(LINK_WITH_ANY_ATTRIBUTE_OR_TEXT, text);
            return findElementsByText(searchContext, locator, parameters, "a");
        }
        return links;
    }
}
