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

package org.vividus.crawler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;

import org.junit.jupiter.api.Test;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;

class LinkCrawlerFactoryTests
{
    private static final String PAGE_URL = "http://some.url";
    private static final String ABSOLUTE_RESOURCE_URL = "http://some.url/page";

    @Test
    void testNewInstance()
    {
        LinkCrawlerData linkCrawlerData = new LinkCrawlerData();
        LinkCrawlerFactory factory = new LinkCrawlerFactory(linkCrawlerData);
        LinkCrawler crawler = factory.newInstance();
        Page page = new Page(createWebUrl(PAGE_URL));
        HtmlParseData htmlParseData = new HtmlParseData();
        htmlParseData.setOutgoingUrls(Set.of(createWebUrl(ABSOLUTE_RESOURCE_URL)));
        page.setParseData(htmlParseData);
        crawler.visit(page);
        assertEquals(Set.of(ABSOLUTE_RESOURCE_URL), linkCrawlerData.getAbsoluteUrls());
    }

    private static WebURL createWebUrl(String url)
    {
        WebURL webUrl = new WebURL();
        webUrl.setURL(url);
        return webUrl;
    }
}
