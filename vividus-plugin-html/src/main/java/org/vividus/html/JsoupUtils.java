/*
 * Copyright 2019-2025 the original author or authors.
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

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.jsoup.parser.Tag;

public final class JsoupUtils
{
    private JsoupUtils()
    {
    }

    public static Document getDocument(String html, String baseUri)
    {
        Parser parser = Parser.htmlParser();
        parser.tagSet().onNewTag(tag -> tag.set(Tag.SelfClose));
        return Jsoup.parse(html, baseUri, parser);
    }

    public static Document getDocument(String html)
    {
        return getDocument(html, "");
    }
}
