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

package org.vividus.ui.web.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;

class WebXpathLocatorUtilsTests
{
    private static final String FULL_INNER_TEXT_XPATH_FORMAT = ".//%1$s[normalize-space(.)=\"%2$s\" and "
            + "not(.//%1$s[normalize-space(.)=\"%2$s\"])]";
    private static final String INNER_TEXT_XPATH_FORMAT = ".//%1$s[contains(normalize-space(.), \"%2$s\") and "
            + "not(.//%1$s[contains(normalize-space(.), \"%2$s\")])]";
    private static final String ANY_ELEMENT = "*";
    private static final String TEXT_WITH_PERCENT = "text %";

    @Test
    void testGetXPathLocatorByInnerTextWithTagName()
    {
        String tagName = "tagName";
        By expectedLocator = By.xpath(String.format(INNER_TEXT_XPATH_FORMAT, tagName, TEXT_WITH_PERCENT));
        By actualLocator = WebXpathLocatorUtils.getXPathLocatorByInnerTextWithTagName(tagName, TEXT_WITH_PERCENT);
        assertEquals(expectedLocator, actualLocator);
    }

    @Test
    void testGetXPathLocatorByFullInnerText()
    {
        By expectedLocator = By.xpath(String.format(FULL_INNER_TEXT_XPATH_FORMAT, ANY_ELEMENT, TEXT_WITH_PERCENT));
        By actualLocator = WebXpathLocatorUtils.getXPathLocatorByFullInnerText(TEXT_WITH_PERCENT);
        assertEquals(expectedLocator, actualLocator);
    }

    @Test
    void testGetXPathLocatorByFullInnerTextWithBothQuotes()
    {
        By expectedLocator = By.xpath(".//*[normalize-space(.)=concat(\"This 'text' with both \", '\"', \"quotes\", "
                + "'\"', \" %\") and not(.//*[normalize-space(.)=concat(\"This 'text' with both \", '\"', \"quotes\", "
                + "'\"', \" %\")])]");
        String text = "This 'text' with both \"quotes\" %";
        By actualLocator = WebXpathLocatorUtils.getXPathLocatorByFullInnerText(text);
        assertEquals(expectedLocator.toString(), actualLocator.toString());
    }

    @Test
    void testGetXPathLocatorByInnerTextWithoutTagName()
    {
        By expectedLocator = By.xpath(String.format(INNER_TEXT_XPATH_FORMAT, ANY_ELEMENT, TEXT_WITH_PERCENT));
        By actualLocator = WebXpathLocatorUtils.getXPathLocatorByInnerText(TEXT_WITH_PERCENT);
        assertEquals(expectedLocator, actualLocator);
    }

    @Test
    void testGetXPathLocatorByInnerTextWithoutTagNameAndWithSingleQuote()
    {
        By expectedLocator = By.xpath(".//*[contains(normalize-space(.), concat(\"This action rebuilds your site's XML "
                + "sitemap and regenerates the cached files, and may be a lengthy process. If you just installed \", "
                + "'\"', \"XML sitemap\", '\"', \", this can be helpful to import all your site's content into the "
                + "sitemap. Otherwise, this should only be used in emergencies. %\")) and not(.//*[contains("
                + "normalize-space(.), concat(\"This action rebuilds your site's XML sitemap and regenerates the cached"
                + " files, and may be a lengthy process. If you just installed \", '\"', \"XML sitemap\", '\"', \", "
                + "this can be helpful to import all your site's content into the sitemap. Otherwise, this should "
                + "only be used in emergencies. %\"))])]");
        String text = "This action rebuilds your site's XML sitemap and regenerates the cached files, "
                + "and may be a lengthy process. If you just installed \"XML sitemap\", this can be helpful to import "
                + "all your site's content into the sitemap. Otherwise, this should only be used in emergencies. %";
        By actualLocator = WebXpathLocatorUtils.getXPathLocatorByInnerText(text);
        assertEquals(expectedLocator.toString(), actualLocator.toString());
    }
}
