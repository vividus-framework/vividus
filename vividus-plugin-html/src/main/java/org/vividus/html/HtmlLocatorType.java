/*
 * Copyright 2019-2024 the original author or authors.
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

package org.vividus.html;

import java.util.function.BiFunction;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public enum HtmlLocatorType
{
    XPATH("XPath", Document::selectXpath),
    CSS_SELECTOR("CSS selector", Document::select);

    private final String description;
    private final BiFunction<Document, String, Elements> finder;

    HtmlLocatorType(String description, BiFunction<Document, String, Elements> finder)
    {
        this.description = description;
        this.finder = finder;
    }

    public Elements findElements(String html, String locator)
    {
        return findElements("", html, locator);
    }

    public Elements findElements(String baseUri, String html, String locator)
    {
        return finder.apply(Jsoup.parse(html, baseUri), locator);
    }

    public String getDescription()
    {
        return description;
    }
}
