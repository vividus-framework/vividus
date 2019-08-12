/*
 * Copyright 2019 the original author or authors.
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

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.ui.web.util.LinkUrlSearchUtils;
import org.vividus.ui.web.util.LocatorUtil;
import org.vividus.util.UriUtils;

public class LinkUrlSearch extends AbstractElementSearchAction implements IElementSearchAction, IElementFilterAction
{
    private static final String LINK_PATTERN = ".//a[@href=%s]";
    private static final String LINK_WITH_CASE_INSENSITIVE_URL = ".//a[" + String.format(TRANSLATE_TO_LOWER_CASE,
            "@href") + "=%s]";

    private boolean caseSensitiveSearch;

    @Inject private IWebDriverProvider webDriverProvider;

    @Override
    public List<WebElement> search(SearchContext searchContext, SearchParameters parameters)
    {
        String searchValue = parameters.getValue();
        By xpathLocator = caseSensitiveSearch ? LocatorUtil.getXPathLocator(LINK_PATTERN, searchValue)
                : LocatorUtil.getXPathLocator(LINK_WITH_CASE_INSENSITIVE_URL, searchValue.toLowerCase());
        return findElements(searchContext, xpathLocator, parameters);
    }

    @Override
    public List<WebElement> filter(List<WebElement> elements, String linkUrl)
    {
        List<WebElement> linksWithUrl = new ArrayList<>();
        if (linkUrl != null)
        {
            for (WebElement link : elements)
            {
                String expectedLinkUrl = buildExpectedLinkUrl(linkUrl);
                String href = link.getAttribute("href");
                String currentHref = LinkUrlSearchUtils.getCurrentHref(href, expectedLinkUrl, webDriverProvider.get());
                if (caseSensitiveSearch ? expectedLinkUrl.equals(currentHref)
                        : expectedLinkUrl.equalsIgnoreCase(currentHref))
                {
                    linksWithUrl.add(link);
                }
            }
        }
        return linksWithUrl;
    }

    private static String buildExpectedLinkUrl(String linkUrl)
    {
        URI url = URI.create(linkUrl);
        return url.isAbsolute() && !url.isOpaque() && linkUrl.equals(UriUtils.buildNewUrl(url, "").toString())
                ? linkUrl + "/" : linkUrl;
    }

    public void setCaseSensitiveSearch(boolean caseSensitiveSearch)
    {
        this.caseSensitiveSearch = caseSensitiveSearch;
    }
}
