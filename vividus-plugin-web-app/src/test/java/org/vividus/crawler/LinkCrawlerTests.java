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

package org.vividus.crawler;

import static com.github.valfirst.slf4jtest.LoggingEvent.info;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.parser.TextParseData;
import edu.uci.ics.crawler4j.url.WebURL;

@ExtendWith(TestLoggerFactoryExtension.class)
class LinkCrawlerTests
{
    private static final String RELATIVE_URL = "/test%25_test";

    private static final String CRAWLED_LOG_MESSAGE = "Crawled: {}";

    private static final String PAGE_URL = "http://some.url";
    private static final String PAGE_URL_HTTPS = "https://some.url";
    private static final String ABSOLUTE_RESOURCE_URL = "https://some.url/page";

    private final TestLogger logger = TestLoggerFactory.getTestLogger(LinkCrawler.class);

    static Stream<Arguments> dataProviderShouldVisit()
    {
        return Stream.of(
            Arguments.of(PAGE_URL,               PAGE_URL,                      true),
            Arguments.of(PAGE_URL,               PAGE_URL_HTTPS,                true),
            Arguments.of(PAGE_URL_HTTPS,         PAGE_URL,                      true),
            Arguments.of(PAGE_URL,               "http://another.url",          false),
            Arguments.of(PAGE_URL,               "http://some.url/image.png",   false),
            Arguments.of("http://some.url/path", "http://some.url/anotherPath", true),
            Arguments.of("http://some.url/",     "http://user:pass@some.url/",  true)
        );
    }

    @ParameterizedTest
    @MethodSource("dataProviderShouldVisit")
    void testShouldVisit(String pageUrl, String url, boolean shouldVisit)
    {
        LinkCrawlerData linkCrawlerData = new LinkCrawlerData();
        LinkCrawler crawler = new LinkCrawler(linkCrawlerData);
        assertEquals(shouldVisit, crawler.shouldVisit(new Page(createWebUrl(pageUrl)), createWebUrl(url)));
    }

    @Test
    void testShouldVisitWhenTimeoutIsNotReached()
    {
        LinkCrawlerData linkCrawlerData = new LinkCrawlerData();
        LinkCrawler crawler = new LinkCrawler(linkCrawlerData);
        assertTrue(crawler.shouldVisit(new Page(createWebUrl(PAGE_URL)), createWebUrl(PAGE_URL)));
    }

    @Test
    void testVisitHtmlPage()
    {
        LinkCrawlerData linkCrawlerData = new LinkCrawlerData();
        LinkCrawler crawler = new LinkCrawler(linkCrawlerData);
        Page page = new Page(createWebUrl(PAGE_URL));
        HtmlParseData htmlParseData = new HtmlParseData();
        htmlParseData.setOutgoingUrls(Set.of(createWebUrl(ABSOLUTE_RESOURCE_URL), createWebUrl(PAGE_URL + "/%2A"),
                createWebUrl(PAGE_URL + RELATIVE_URL)));
        page.setParseData(htmlParseData);
        crawler.visit(page);
        assertEquals(Set.of(ABSOLUTE_RESOURCE_URL, PAGE_URL + "/*", PAGE_URL + RELATIVE_URL),
                linkCrawlerData.getAbsoluteUrls());
        assertThat(logger.getLoggingEvents(), is(List.of(info(CRAWLED_LOG_MESSAGE, PAGE_URL))));
    }

    @Test
    void testVisitTextPage()
    {
        LinkCrawlerData linkCrawlerData = new LinkCrawlerData();
        LinkCrawler crawler = new LinkCrawler(linkCrawlerData);
        Page page = new Page(createWebUrl(PAGE_URL));
        page.setParseData(new TextParseData());
        crawler.visit(page);
        assertEquals(Set.of(), linkCrawlerData.getAbsoluteUrls());
        assertThat(logger.getLoggingEvents(), is(List.of(info(CRAWLED_LOG_MESSAGE, PAGE_URL))));
    }

    private static WebURL createWebUrl(String url)
    {
        WebURL webUrl = new WebURL();
        webUrl.setURL(url);
        return webUrl;
    }
}
