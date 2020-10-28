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

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.vividus.ui.action.search.IElementFilterAction;
import org.vividus.ui.action.search.IElementSearchAction;
import org.vividus.ui.action.search.SearchParameters;
import org.vividus.ui.web.util.LocatorUtil;

public class LinkUrlPartSearch extends AbstractWebElementSearchAction
        implements IElementSearchAction, IElementFilterAction
{
    private static final String LINK_WITH_PART_URL_PATTERN = ".//a[contains(@href, %s)]";
    private static final String LINK_WITH_CASE_INSENSITIVE_URL_PART = ".//a[contains (" + String.format(
            TRANSLATE_TO_LOWER_CASE, "@href") + ", %s)]";
    private boolean caseSensitiveSearch;

    public LinkUrlPartSearch()
    {
        super(WebLocatorType.LINK_URL_PART);
    }

    @Override
    public List<WebElement> search(SearchContext searchContext, SearchParameters parameters)
    {
        String searchValue = parameters.getValue();
        By xpathLocator = caseSensitiveSearch ? LocatorUtil.getXPathLocator(LINK_WITH_PART_URL_PATTERN, searchValue)
                : LocatorUtil.getXPathLocator(LINK_WITH_CASE_INSENSITIVE_URL_PART, searchValue.toLowerCase());
        return findElements(searchContext, xpathLocator, parameters);
    }

    @Override
    public List<WebElement> filter(List<WebElement> elements, String urlPart)
    {
        List<WebElement> linksWithUrlPart = new ArrayList<>();
        for (WebElement element : elements)
        {
            String href = element.getAttribute("href");
            if (href != null && (caseSensitiveSearch ? href.contains(urlPart)
                    : href.toLowerCase().contains(urlPart.toLowerCase())))
            {
                linksWithUrlPart.add(element);
            }
        }
        return linksWithUrlPart;
    }

    public void setCaseSensitiveSearch(boolean caseSensitiveSearch)
    {
        this.caseSensitiveSearch = caseSensitiveSearch;
    }

    @Override
    public boolean matches(WebElement element, String value)
    {
        throw new UnsupportedOperationException();
    }
}
