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

package org.vividus.bdd.steps.ui.web.validation;

import java.net.URI;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.softassert.ISoftAssert;
import org.vividus.ui.web.action.search.ActionAttributeType;
import org.vividus.ui.web.action.search.SearchAttributes;
import org.vividus.ui.web.util.LinkUrlSearchUtils;
import org.vividus.ui.web.util.LocatorUtil;
import org.vividus.util.UriUtils;

public class LinkValidations implements ILinkValidations
{
    private static final String LINK_WITH_ATTRIBUTES = "Link with attributes: %s";
    private static final String LINK_WITH_TEXT = "Link with text '%s'";

    @Inject private IHighlightingSoftAssert softAssert;
    @Inject private IBaseValidations baseValidations;
    @Inject private IWebDriverProvider webDriverProvider;

    @Override
    public WebElement assertIfLinkWithTextExists(SearchContext searchContext, String text)
    {
        SearchAttributes attributes = new SearchAttributes(ActionAttributeType.LINK_TEXT, text);
        return baseValidations.assertIfElementExists(String.format(LINK_WITH_TEXT, text), searchContext, attributes);
    }

    @Override
    public boolean assertIfLinkWithTextNotExists(SearchContext searchContext, String text)
    {
        return baseValidations.assertIfElementDoesNotExist(String.format(LINK_WITH_TEXT, text), searchContext,
                new SearchAttributes(ActionAttributeType.LINK_TEXT, text));
    }

    @Override
    public WebElement assertIfLinkExists(SearchContext searchContext, SearchAttributes attributes)
    {
        return baseValidations.assertIfElementExists(String.format(LINK_WITH_ATTRIBUTES, attributes),
                searchContext, attributes);
    }

    @Override
    public boolean assertIfLinkDoesNotExist(SearchContext searchContext, SearchAttributes attributes)
    {
        return baseValidations.assertIfElementDoesNotExist(String.format(LINK_WITH_ATTRIBUTES, attributes),
                searchContext, attributes);
    }

    @Override
    public WebElement assertIfLinkWithTooltipExists(SearchContext searchContext, String tooltip)
    {
        return baseValidations.assertIfElementExists(String.format("Link with tooltip '%s'", tooltip), searchContext,
                new SearchAttributes(ActionAttributeType.XPATH, LocatorUtil.getXPath(".//a[@title='%s']", tooltip)));
    }

    @Override
    public boolean assertIfLinkHrefMatchesURL(WebElement link, String url, boolean equals)
    {
        if (link != null)
        {
            ISoftAssert softAssert = this.softAssert.withHighlightedElement(link);
            boolean result = true;
            String href = link.getAttribute("href");
            if (href != null)
            {
                href = LinkUrlSearchUtils.getCurrentHref(href, url, webDriverProvider.get());
                result = assertUrlsAreEqual(url, href, softAssert, equals);
            }
            else if (equals)
            {
                result = softAssert.recordFailedAssertion("Href attribute was not found in element");
            }
            return result;
        }
        return false;
    }


    private static boolean assertUrlsAreEqual(String expectedStringUrl, String actualStringUrl,
            ISoftAssert softAssert, boolean equals)
    {
        boolean result;
        if (expectedStringUrl == null || actualStringUrl == null)
        {
            result = StringUtils.equals(expectedStringUrl, actualStringUrl);
        }
        else
        {
            try
            {
                URI expectedUrl = UriUtils.createUri(expectedStringUrl);
                URI actualUrl = UriUtils.createUri(actualStringUrl);
                result = expectedUrl.equals(actualUrl);
            }
            catch (IllegalArgumentException e)
            {
                result = StringUtils.equals(expectedStringUrl, actualStringUrl);
            }
        }
        String message = equals ? "Link has correct URL" : "Link does not have specified URL";
        return softAssert.assertEquals(
                String.format("%1$s, actual was: %2$s, expected: %3$s", message, actualStringUrl, expectedStringUrl),
                equals, result);
    }
}
