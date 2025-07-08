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

package org.vividus.crawler.transformer;

import static com.github.valfirst.slf4jtest.LoggingEvent.info;
import static com.github.valfirst.slf4jtest.LoggingEvent.warn;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import com.github.valfirst.slf4jtest.LoggingEvent;
import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.apache.hc.client5.http.HttpResponseException;
import org.apache.hc.core5.http.HttpStatus;
import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.model.ExamplesTable.TableProperties;
import org.jbehave.core.steps.ParameterConverters;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.crawler.ICrawlControllerFactory;
import org.vividus.crawler.LinkCrawler;
import org.vividus.crawler.LinkCrawlerFactory;
import org.vividus.http.HttpRedirectsProvider;
import org.vividus.ui.web.configuration.WebApplicationConfiguration;
import org.vividus.util.UriUtils;

import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.crawler.CrawlController.WebCrawlerFactory;
import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.AbstractWebURL;
import edu.uci.ics.crawler4j.url.WebURL;

@ExtendWith({MockitoExtension.class, TestLoggerFactoryExtension.class})
class HeadlessCrawlerTableTransformerTests
{
    private static final String MAIN_APP_PROP = "transformer.from-headless-crawling.main-page-url";

    private static final String SLASH_PATH3 = "/path3";
    private static final String PATH3 = "path3";
    private static final String ROOT = "/";
    private static final String SEED = "/transformer/properties";
    private static final String PATH4 = "/path4";
    private static final String PATH2 = "/path2";
    private static final String MAIN_APP_PAGE = "http://some.url";
    private static final String DEFAULT_RELATIVE_URL = "";

    private static final String CRAWLING_RELATIVE_URL = "/page";

    private static final String OUTGOING_RELATIVE_URL = "/path";
    private static final String OUTGOING_ABSOLUTE_URL = MAIN_APP_PAGE + OUTGOING_RELATIVE_URL;

    private static final String EXCLUDE_EXTENSIONS_REGEX = "js|css";
    private static final String EXCLUDE_URLS_REGEX = ".*broken-link*";

    private static final String REDIRECT_FILTER_LOG = "Filtered redirects chains:{}{}";

    private final TestLogger logger = TestLoggerFactory.getTestLogger(HeadlessCrawlerTableTransformer.class);

    @Mock private ICrawlControllerFactory crawlControllerFactory;
    @Mock private WebApplicationConfiguration webApplicationConfiguration;
    @Mock private HttpRedirectsProvider redirectsProvider;
    @InjectMocks private HeadlessCrawlerTableTransformer transformer;

    private final Keywords keywords = new Keywords();
    private final ParameterConverters parameterConverters =  new ParameterConverters();

    static Stream<Arguments> dataProviderOfFetchingUrls()
    {
        // @formatter:off
        return Stream.of(
                arguments(DEFAULT_RELATIVE_URL, toSet(PATH2, PATH3),      List.of(PATH2, SLASH_PATH3)),
                arguments(ROOT,                 toSet(PATH4, "path5"),    List.of("/path5", PATH4)),
                arguments("/go",                toSet("path6", "/path7"), List.of("/go/path7", "/go/path6")),
                arguments("/go/",               toSet("path8", "/path9"), List.of("/go/path8", "/go/path9")),
                arguments(DEFAULT_RELATIVE_URL, null,                     List.of())
        );
        // @formatter:on
    }

    @ParameterizedTest
    @MethodSource("dataProviderOfFetchingUrls")
    void testFetchUrlsSuccessfully(String mainAppPageRelativeUrl, Set<String> seedRelativeUrlsProperty,
                                  List<String> expectedSeedRelativeUrls) throws IOException, InterruptedException
    {
        transformer.setSeedRelativeUrls(seedRelativeUrlsProperty);
        transformer.setExcludeUrlsRegex(EXCLUDE_URLS_REGEX);
        transformer.setMainPageUrlProperty(MAIN_APP_PROP);
        Set<String> urls = testFetchUrls(mainAppPageRelativeUrl, expectedSeedRelativeUrls);
        assertThat(urls, equalTo(Set.of(OUTGOING_ABSOLUTE_URL)));
        verifyNoInteractions(redirectsProvider);
        assertThat(logger.getLoggingEvents(), is(List.of(getMainAppPageWarn())));
    }

    @Test
    void testFetchUrlsWithDeprecatedExcludeExtensionsRegexProperty() throws IOException, InterruptedException
    {
        transformer.setExcludeExtensionsRegex(EXCLUDE_EXTENSIONS_REGEX);
        transformer.setMainPageUrlProperty(MAIN_APP_PROP);
        testFetchUrls(MAIN_APP_PAGE, List.of());
        assertThat(logger.getLoggingEvents(), is(List.of(warn(
                "Property `transformer.from-headless-crawling.exclude-extensions-regex` is deprecated and will "
                        + "be removed in VIVIDUS 0.7.0. "
                        + "Please use `transformer.from-headless-crawling.exclude-urls-regex` instead."),
                getMainAppPageWarn())));
    }

    @Test
    void shouldFilterUrlsWhenLastRedirectUrlAlreadyInTheSet() throws IOException, InterruptedException
    {
        transformer.setFilterRedirects(true);
        transformer.setSeedRelativeUrls(toSet(PATH2, PATH3));
        transformer.setMainPageUrlProperty(MAIN_APP_PROP);
        URI outgoingURI = URI.create(OUTGOING_ABSOLUTE_URL);
        when(redirectsProvider.getRedirects(outgoingURI)).thenReturn(List.of(outgoingURI));
        Set<String> urls = testFetchUrls(ROOT, asList(PATH2, SLASH_PATH3));
        assertThat(urls, equalTo(Set.of()));
        assertThat(logger.getLoggingEvents(), is(List.of(getMainAppPageWarn(),
                info(REDIRECT_FILTER_LOG, System.lineSeparator(), "http://some.url/path -> http://some.url/path"))));
    }

    @Test
    void shouldTreatInvalidStatusCodeAsNoRedirects() throws IOException, InterruptedException
    {
        transformer.setFilterRedirects(true);
        transformer.setSeedRelativeUrls(toSet(PATH2, PATH3));
        transformer.setMainPageUrlProperty(MAIN_APP_PROP);
        URI outgoingURI = URI.create(OUTGOING_ABSOLUTE_URL);
        var httpResponseException = new HttpResponseException(HttpStatus.SC_NOT_FOUND, "");
        when(redirectsProvider.getRedirects(outgoingURI)).thenThrow(httpResponseException);
        Set<String> urls = testFetchUrls(ROOT, List.of(PATH2, SLASH_PATH3));
        assertThat(urls, equalTo(Set.of(OUTGOING_ABSOLUTE_URL)));
        assertThat(logger.getLoggingEvents(), is(List.of(getMainAppPageWarn(), warn(httpResponseException,
                "Exception during redirects receiving"))));
    }

    @Test
    void shouldNotFilterUrlsWhenLastRedirectUrlNotInTheSet() throws IOException, InterruptedException
    {
        transformer.setFilterRedirects(true);
        transformer.setSeedRelativeUrls(toSet(PATH2, PATH3));
        transformer.setMainPageUrlProperty(MAIN_APP_PROP);
        URI outgoingURI = URI.create(OUTGOING_ABSOLUTE_URL);
        when(redirectsProvider.getRedirects(outgoingURI)).thenReturn(List.of(URI.create("http://some.url/other")));
        Set<String> urls = testFetchUrls(ROOT, asList(PATH2, SLASH_PATH3));
        assertThat(urls, equalTo(Set.of(OUTGOING_ABSOLUTE_URL)));
        assertThat(logger.getLoggingEvents(), is(List.of(getMainAppPageWarn(),
                info(REDIRECT_FILTER_LOG, System.lineSeparator(), "http://some.url/path -> http://some.url/other"))));
    }

    @Test
    void testFetchUrlsTwice() throws IOException, InterruptedException
    {
        transformer.setSeedRelativeUrls(toSet(SEED));
        Set<String> urls = testFetchUrls(DEFAULT_RELATIVE_URL, List.of(SEED));
        assertThat(urls, equalTo(Set.of(OUTGOING_ABSOLUTE_URL)));
        TableProperties tableProperties = buildTableProperties();
        Set<String> urls2 = transformer.fetchUrls(tableProperties);
        verifyNoMoreInteractions(crawlControllerFactory);
        assertSame(urls, urls2);
        verifyNoInteractions(redirectsProvider);
    }

    @Test
    void testFetchUrlsTwiceWithSameProperties() throws IOException, InterruptedException
    {
        String seedRelativeUrlsProperty = "/seed1";
        String mainAppPage = buildAppPageUrl(DEFAULT_RELATIVE_URL);
        CrawlController crawlController = mockCrawlerControllerFactory(mainAppPage);
        InOrder ordered = inOrder(crawlControllerFactory, crawlController);
        TableProperties tableProperties = buildTableProperties();
        transformer.setSeedRelativeUrls(toSet(seedRelativeUrlsProperty));
        Set<String> urls = runUrlFetching(mainAppPage, tableProperties,
                List.of(seedRelativeUrlsProperty), crawlController, ordered);
        assertThat(urls, equalTo(Set.of(OUTGOING_ABSOLUTE_URL)));
        Set<String> urls2 = transformer.fetchUrls(tableProperties);
        verifyNoMoreInteractions(crawlControllerFactory, crawlController);
        assertThat(urls2, equalTo(Set.of(OUTGOING_ABSOLUTE_URL)));
        assertSame(urls, urls2);
        verifyNoInteractions(redirectsProvider);
    }

    @Test
    void testFetchUrlsWhenSeedRelativeUrlsAreSetViaConfiguration() throws IOException, InterruptedException
    {
        String seedRelativeUrl = "/fromConfig";
        transformer.setSeedRelativeUrls(Set.of(seedRelativeUrl));
        Set<String> urls = testFetchUrls(DEFAULT_RELATIVE_URL, List.of(seedRelativeUrl));
        assertThat(urls, equalTo(Set.of(OUTGOING_ABSOLUTE_URL)));
        verifyNoInteractions(redirectsProvider);
    }

    private Set<String> testFetchUrls(String mainAppPageRelativeUrl, List<String> expectedSeedRelativeUrls)
            throws IOException, InterruptedException
    {
        String mainAppPage = buildAppPageUrl(mainAppPageRelativeUrl);
        CrawlController crawlController = mockCrawlerControllerFactory(mainAppPage);
        TableProperties tableProperties = buildTableProperties();
        return runUrlFetching(mainAppPage, tableProperties, expectedSeedRelativeUrls, crawlController);
    }

    private Set<String> runUrlFetching(String mainAppPage, TableProperties tableProperties,
                                       List<String> expectedSeedRelativeUrls, CrawlController crawlController)
                                       throws IOException, InterruptedException
    {
        InOrder ordered = inOrder(crawlControllerFactory, crawlController);
        return runUrlFetching(mainAppPage, tableProperties, expectedSeedRelativeUrls,
                crawlController, ordered);
    }

    private Set<String> runUrlFetching(String mainAppPage, TableProperties tableProperties,
                                       List<String> expectedSeedRelativeUrls, CrawlController crawlController,
                                       InOrder ordered) throws IOException, InterruptedException
    {
        URI mainAppPageUri = URI.create(mainAppPage);
        doNothing().when(crawlController).start((WebCrawlerFactory<?>) argThat(factory ->
        {
            if (factory instanceof LinkCrawlerFactory)
            {
                LinkCrawler linkCrawler = ((LinkCrawlerFactory) factory).newInstance();
                HtmlParseData htmlParseData = new HtmlParseData();
                String outgoingUrl = UriUtils.buildNewUrl(mainAppPage, OUTGOING_RELATIVE_URL).toString();
                htmlParseData.setOutgoingUrls(Set.of(createWebUrl(outgoingUrl)));
                String crawlingPageUrl = UriUtils.buildNewUrl(mainAppPage, CRAWLING_RELATIVE_URL).toString();
                WebURL crawlingPageWebUrl = createWebUrl(crawlingPageUrl);
                Page page = new Page(crawlingPageWebUrl);
                page.setParseData(htmlParseData);
                if (linkCrawler.shouldVisit(page, crawlingPageWebUrl))
                {
                    linkCrawler.visit(page);
                }
                return true;
            }
            return false;
        }), eq(50));
        Set<String> urls = transformer.fetchUrls(tableProperties);
        ordered.verify(crawlControllerFactory).createCrawlController(mainAppPageUri);
        List<String> urlsToVerify = Stream.concat(Stream.of(mainAppPage),
                expectedSeedRelativeUrls.stream().map(HeadlessCrawlerTableTransformerTests::buildAppPageUrl))
                .toList();
        for (String url : urlsToVerify)
        {
            ordered.verify(crawlController).addSeed(url);
        }
        ordered.verify(crawlController).start(any(LinkCrawlerFactory.class), eq(50));
        verifyNoMoreInteractions(crawlController);
        return urls;
    }

    private static String buildAppPageUrl(String mainAppPageRelativeUrl)
    {
        return MAIN_APP_PAGE + mainAppPageRelativeUrl;
    }

    private CrawlController mockCrawlerControllerFactory(String mainAppPage)
    {
        CrawlController crawlController = mock(CrawlController.class);
        URI mainPageURI = URI.create(mainAppPage);
        when(webApplicationConfiguration.getMainApplicationPageUrl()).thenReturn(mainPageURI);
        when(crawlControllerFactory.createCrawlController(mainPageURI))
                .thenReturn(crawlController);
        return crawlController;
    }

    private LoggingEvent getMainAppPageWarn()
    {
        return warn("The use of {} property for setting of main page for crawling is deprecated and will "
                + "be removed in VIVIDUS 0.7.0, please see use either {} transformer parameter or "
                + "{} property.",
                "web-application.main-page-url", "mainPageUrl", MAIN_APP_PROP);
    }

    private TableProperties buildTableProperties()
    {
        return new TableProperties("", keywords, parameterConverters);
    }

    private static WebURL createWebUrl(String url)
    {
        WebURL webUrl = new AbstractWebURL() { };
        webUrl.setURL(url);
        return webUrl;
    }

    private static Set<String> toSet(String... strings)
    {
        return new HashSet<>(List.of(strings));
    }
}
