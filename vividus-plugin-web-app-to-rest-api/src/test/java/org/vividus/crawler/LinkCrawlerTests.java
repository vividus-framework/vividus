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

package org.vividus.crawler;

import static com.github.valfirst.slf4jtest.LoggingEvent.info;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.parser.TextParseData;
import edu.uci.ics.crawler4j.url.WebURL;
import edu.uci.ics.crawler4j.url.WebURLImpl;

@ExtendWith(TestLoggerFactoryExtension.class)
class LinkCrawlerTests
{
    private static final String RELATIVE_URL = "/test%25_test";

    private static final String CRAWLED_LOG_MESSAGE = "Crawled: {}";

    private static final String PAGE_URL = "http://some.url";
    private static final String PAGE_URL_HTTPS = "https://some.url";
    private static final String ABSOLUTE_RESOURCE_URL = "https://some.url/page";

    private static final String RESOURCE_URL_WITH_EXTENSION = "https://some.url/resources/manual.PDF?param=value";

    private static final String EXCLUDE_EXTENSIONS_REGEX = "(js|css|png|pdf)";

    private final TestLogger logger = TestLoggerFactory.getTestLogger(LinkCrawler.class);

    static Stream<Arguments> dataProviderShouldVisit()
    {
        return Stream.of(
            Arguments.of(PAGE_URL,               PAGE_URL,                               true),
            Arguments.of(PAGE_URL,               PAGE_URL_HTTPS,                         true),
            Arguments.of(PAGE_URL_HTTPS,         PAGE_URL,                               true),
            Arguments.of(PAGE_URL,               "http://another.url",                   false),
            Arguments.of(PAGE_URL,               "http://some.url/image.png",            false),
            Arguments.of(PAGE_URL,               "http://some.url/path.html",            true),
            Arguments.of(PAGE_URL,               "http://some.url/path.pdf?param=value", false),
            Arguments.of(PAGE_URL,               "http://some.url/path/path.pdf",        false),
            Arguments.of(PAGE_URL,               "http://some.url/path.path/path.PDF",   false),
            Arguments.of("http://some.url/path", "http://some.url/anotherPath",          true),
            Arguments.of("http://some.url/",     "http://user:pass@some.url/",           true)
        );
    }

    @ParameterizedTest
    @MethodSource("dataProviderShouldVisit")
    void testShouldVisit(String pageUrl, String url, boolean shouldVisit)
    {
        LinkCrawlerData linkCrawlerData = new LinkCrawlerData();
        LinkCrawler crawler = new LinkCrawler(linkCrawlerData, EXCLUDE_EXTENSIONS_REGEX);
        assertEquals(shouldVisit, crawler.shouldVisit(new Page(createWebUrl(pageUrl)), createWebUrl(url)));
    }

    @ParameterizedTest
    @CsvSource({ RESOURCE_URL_WITH_EXTENSION,
                 ABSOLUTE_RESOURCE_URL })
    void testShouldVisitIfRegexEmpty(String url)
    {
        LinkCrawlerData linkCrawlerData = new LinkCrawlerData();
        LinkCrawler crawler = new LinkCrawler(linkCrawlerData, null);
        assertTrue(crawler.shouldVisit(new Page(createWebUrl(PAGE_URL)), createWebUrl(url)));
    }

    @Test
    void testOnRedirectedStatusCode()
    {
        LinkCrawlerData linkCrawlerData = new LinkCrawlerData();
        LinkCrawler crawler = new LinkCrawler(linkCrawlerData, EXCLUDE_EXTENSIONS_REGEX);
        crawler.onRedirectedStatusCode(createPageWithRedirect(PAGE_URL, ABSOLUTE_RESOURCE_URL));
        assertThat(logger.getLoggingEvents(), is(List.of()));
    }

    @Test
    void testOnRedirectedStatusCodeShouldLogMessage()
    {
        LinkCrawlerData linkCrawlerData = new LinkCrawlerData();
        LinkCrawler crawler = new LinkCrawler(linkCrawlerData, EXCLUDE_EXTENSIONS_REGEX);
        Page page = createPageWithRedirect(PAGE_URL, RESOURCE_URL_WITH_EXTENSION);
        crawler.onRedirectedStatusCode(page);
        assertThat(logger.getLoggingEvents(), is(List.of(info(
                "URL {} redirects to the URL with a forbidden extension ({}) and will be excluded from the result.",
                page.getWebURL().getURL(), RESOURCE_URL_WITH_EXTENSION))));
    }

    @Test
    void testOnBeforeExit()
    {
        String urlWithRedirectOne = "https://some.url/redirect.html";
        String urlWithRedirectTwo = "https://some.url/redirect2.html";
        LinkCrawlerData linkCrawlerData = new LinkCrawlerData();
        linkCrawlerData.getAbsoluteUrls().add(urlWithRedirectOne);
        linkCrawlerData.getAbsoluteUrls().add(ABSOLUTE_RESOURCE_URL);
        linkCrawlerData.getAbsoluteUrls().add(urlWithRedirectTwo);
        Page pageOne = createPageWithRedirect(urlWithRedirectOne, RESOURCE_URL_WITH_EXTENSION);
        Page pageTwo = createPageWithRedirect(urlWithRedirectTwo, RESOURCE_URL_WITH_EXTENSION);
        LinkCrawler crawler = new LinkCrawler(linkCrawlerData, EXCLUDE_EXTENSIONS_REGEX);

        crawler.onRedirectedStatusCode(pageOne);
        crawler.onRedirectedStatusCode(pageTwo);
        crawler.onBeforeExit();
        assertEquals(Set.of(ABSOLUTE_RESOURCE_URL), linkCrawlerData.getAbsoluteUrls());
    }

    @Test
    void testShouldVisitWhenTimeoutIsNotReached()
    {
        LinkCrawlerData linkCrawlerData = new LinkCrawlerData();
        LinkCrawler crawler = new LinkCrawler(linkCrawlerData, EXCLUDE_EXTENSIONS_REGEX);
        assertTrue(crawler.shouldVisit(new Page(createWebUrl(PAGE_URL)), createWebUrl(PAGE_URL)));
    }

    @Test
    void testVisitHtmlPage()
    {
        LinkCrawlerData linkCrawlerData = new LinkCrawlerData();
        LinkCrawler crawler = new LinkCrawler(linkCrawlerData, EXCLUDE_EXTENSIONS_REGEX);
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
    void testVisitPageWithNotAllowedExtension()
    {
        LinkCrawlerData linkCrawlerData = new LinkCrawlerData();
        LinkCrawler crawler = new LinkCrawler(linkCrawlerData, EXCLUDE_EXTENSIONS_REGEX);
        assertFalse(crawler.shouldVisit(new Page(createWebUrl(RESOURCE_URL_WITH_EXTENSION)),
                createWebUrl(RESOURCE_URL_WITH_EXTENSION)));
        assertThat(logger.getLoggingEvents(),
                is(List.of(info("Skip crawling for URL {}", RESOURCE_URL_WITH_EXTENSION))));
    }

    @Test
    void testVisitTextPage()
    {
        LinkCrawlerData linkCrawlerData = new LinkCrawlerData();
        LinkCrawler crawler = new LinkCrawler(linkCrawlerData, EXCLUDE_EXTENSIONS_REGEX);
        Page page = new Page(createWebUrl(PAGE_URL));
        page.setParseData(new TextParseData());
        crawler.visit(page);
        assertEquals(Set.of(), linkCrawlerData.getAbsoluteUrls());
        assertThat(logger.getLoggingEvents(), is(List.of(info(CRAWLED_LOG_MESSAGE, PAGE_URL))));
    }

    private static Page createPageWithRedirect(String pageUrl, String redirectPage)
    {
        Page page = new Page(createWebUrl(pageUrl));
        page.setRedirectedToUrl(redirectPage);
        return page;
    }

    private static WebURL createWebUrl(String url)
    {
        WebURL webUrl = new WebURLImpl();
        webUrl.setURL(url);
        return webUrl;
    }
}
