/*
 * Copyright 2019-2023 the original author or authors.
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

package org.vividus.util;

import java.util.function.Function;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.vividus.html.LocatorType;

public final class HtmlUtils
{
    private HtmlUtils()
    {
    }

    public static Elements getElements(String html, LocatorType locatorType, String locator)
    {
        return getElements("", html, locatorType, locator);
    }

    public static Elements getElements(String baseUri, String html, LocatorType locatorType, String locator)
    {
        if (locatorType == LocatorType.CSS_SELECTOR)
        {
            return getElements(baseUri, html, d -> d.select(locator));
        }
        return getElements(baseUri, html, d -> d.selectXpath(locator));
    }

    private static Elements getElements(String baseUri, String html, Function<Document, Elements> finder)
    {
        Document document = Jsoup.parse(html, baseUri);
        return finder.apply(document);
    }
}
