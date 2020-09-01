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

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.ui.action.search.IElementFilterAction;
import org.vividus.ui.action.search.IElementSearchAction;
import org.vividus.ui.action.search.SearchParameters;
import org.vividus.ui.web.util.LocatorUtil;
import org.vividus.util.UriUtils;

public class LinkUrlSearch extends AbstractWebElementSearchAction
        implements IElementSearchAction, IElementFilterAction
{
    private static final String LINK_PATTERN = ".//a[@href=%s]";
    private static final String LINK_WITH_CASE_INSENSITIVE_URL = ".//a[" + String.format(TRANSLATE_TO_LOWER_CASE,
            "@href") + "=%s]";
    private static final char SHARP_SYMBOL = '#';

    private boolean caseSensitiveSearch;

    @Inject private IWebDriverProvider webDriverProvider;

    public LinkUrlSearch()
    {
        super(WebLocatorType.LINK_URL);
    }

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
                String currentHref = getCurrentHref(href, expectedLinkUrl, webDriverProvider.get().getCurrentUrl());
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

    private static String getCurrentHref(String href, String url, String currentUrl)
    {
        if (href == null)
        {
            return null;
        }
        boolean startsWithSlash = url.charAt(0) == '/';
        Pattern pattern = Pattern.compile(buildCurrentDomainPattern(startsWithSlash, currentUrl));
        if (startsWithSlash)
        {
            Matcher m = pattern.matcher(href);
            if (m.find())
            {
                return href.substring(m.group(1).length());
            }
        }
        else if (!pattern.matcher(url).find() && areSchemeAndAuthorityEqual(currentUrl, href))
        {
            if (url.charAt(0) == SHARP_SYMBOL)
            {
                return href.substring(href.indexOf(SHARP_SYMBOL));
            }
            Matcher m = pattern.matcher(currentUrl);
            if (m.find())
            {
                return href.substring(m.group(0).length());
            }
        }
        return href;
    }

    private static String buildCurrentDomainPattern(boolean absoluteUrl, String url)
    {
        StringBuilder currentDomainPattern = new StringBuilder("((%1$s://(.*:.*@)?%2$s%3$s){1}");
        try
        {
            URL currentUrl = new URL(url);
            int port = currentUrl.getPort();
            String portAsString = port != -1 ? ":" + port : "";
            currentDomainPattern.append(absoluteUrl ? ")(.[^/]*)" : ".*)(\\w*/)");
            return String.format(currentDomainPattern.toString(), currentUrl.getProtocol(), currentUrl.getHost(),
                    portAsString);
        }
        catch (MalformedURLException e)
        {
            throw new IllegalStateException(e);
        }
    }

    private static boolean areSchemeAndAuthorityEqual(String href, String url)
    {
        URI hrefUri = UriUtils.createUri(href);
        URI uri = UriUtils.createUri(url);
        return hrefUri.getScheme().equals(uri.getScheme()) && hrefUri.getAuthority().equals(uri.getAuthority());
    }

    public void setCaseSensitiveSearch(boolean caseSensitiveSearch)
    {
        this.caseSensitiveSearch = caseSensitiveSearch;
    }
}
