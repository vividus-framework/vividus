/*
 * Copyright 2019-2021 the original author or authors.
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

package org.vividus.transformer;

import static com.github.valfirst.slf4jtest.LoggingEvent.warn;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Stream;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

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
import edu.uci.ics.crawler4j.url.WebURL;

@ExtendWith({MockitoExtension.class, TestLoggerFactoryExtension.class})
class HeadlessCrawlerTableTransformerTests
{
    private static final String SLASH_PATH3 = "/path3";
    private static final String PATH3 = "path3";
    private static final String ROOT = "/";
    private static final String SEED = "/transformer/properties";
    private static final String PATH4 = "/path4";
    private static final String PATH2 = "/path2";
    private static final String MAIN_APP_PAGE = "http://some.url";
    private static final String DEFAULT_RELATIVE_URL = "";

    private static final String CRAWLING_RELATIVE_URL = "/page";

    private static final String OUTGOING_ABSOLUT_URL = "http://some.url/path";

    private final TestLogger logger = TestLoggerFactory.getTestLogger(HeadlessCrawlerTableTransformer.class);

    @Mock private ICrawlControllerFactory crawlControllerFactory;
    @Mock private WebApplicationConfiguration webApplicationConfiguration;
    @Mock private HttpRedirectsProvider redirectsProvider;
    @InjectMocks private HeadlessCrawlerTableTransformer transformer;

    private final ParameterConverters parameterConverters =  new ParameterConverters();

    static Stream<Arguments> dataProviderOfFechingUrls()
    {
        // @formatter:off
        return Stream.of(
                Arguments.of(DEFAULT_RELATIVE_URL, toSet(PATH2, PATH3),      List.of(PATH2, SLASH_PATH3)),
                Arguments.of(ROOT,                 toSet(PATH4, "path5"),    List.of("/path5", PATH4)),
                Arguments.of("/go",                toSet("path6", "/path7"), List.of("/go/path7", "/go/path6")),
                Arguments.of("/go/",               toSet("path8", "/path9"), List.of("/go/path8", "/go/path9")),
                Arguments.of(DEFAULT_RELATIVE_URL, null,                     List.of())
        );
        // @formatter:on
    }

    @ParameterizedTest
    @MethodSource("dataProviderOfFechingUrls")
    void testFetchUrlsSucessfully(String mainAppPageRelativeUrl, Set<String> seedRelativeUrlsProperty,
                                  List<String> expectedSeedRelativeUrls)
    {
        transformer.setSeedRelativeUrls(seedRelativeUrlsProperty);
        Set<String> urls = testFetchUrls(mainAppPageRelativeUrl, expectedSeedRelativeUrls);
        assertThat(urls, equalTo(Set.of(OUTGOING_ABSOLUT_URL)));
        verifyNoInteractions(redirectsProvider);
    }

    @Test
    void shouldFilterUrlsWhenLastRedirectUrlAlreadyInTheSet()
    {
        transformer.setFilterRedirects(true);
        transformer.setSeedRelativeUrls(toSet(PATH2, PATH3));
        URI outgoingURI = URI.create(OUTGOING_ABSOLUT_URL);
        when(redirectsProvider.getRedirects(outgoingURI)).thenReturn(List.of(outgoingURI));
        Set<String> urls = testFetchUrls(ROOT, asList(PATH2, SLASH_PATH3));
        assertThat(urls, equalTo(Set.of()));
    }

    @Test
    void shouldTreatInvalidStatusCodeAsNoRedirects()
    {
        transformer.setFilterRedirects(true);
        transformer.setSeedRelativeUrls(toSet(PATH2, PATH3));
        URI outgoingURI = URI.create(OUTGOING_ABSOLUT_URL);
        IllegalStateException illegalStateException = new IllegalStateException();
        when(redirectsProvider.getRedirects(outgoingURI)).thenThrow(illegalStateException);
        Set<String> urls = testFetchUrls(ROOT, List.of(PATH2, SLASH_PATH3));
        assertThat(urls, equalTo(Set.of(OUTGOING_ABSOLUT_URL)));
        assertThat(logger.getLoggingEvents(), is(List.of(warn(illegalStateException,
                "Exception during redirects receiving"))));
    }

    @Test
    void shouldNotFilterUrlsWhenLastRedirectUrlNotInTheSet()
    {
        transformer.setFilterRedirects(true);
        transformer.setSeedRelativeUrls(toSet(PATH2, PATH3));
        URI outgoingURI = URI.create(OUTGOING_ABSOLUT_URL);
        when(redirectsProvider.getRedirects(outgoingURI)).thenReturn(List.of(URI.create("http://some.url/other")));
        Set<String> urls = testFetchUrls(ROOT, asList(PATH2, SLASH_PATH3));
        assertThat(urls, equalTo(Set.of(OUTGOING_ABSOLUT_URL)));
    }

    @Test
    void testFetchUrlsTwice()
    {
        transformer.setSeedRelativeUrls(toSet(SEED));
        Set<String> urls = testFetchUrls(DEFAULT_RELATIVE_URL, List.of(SEED));
        assertThat(urls, equalTo(Set.of(OUTGOING_ABSOLUT_URL)));
        TableProperties tableProperties = buildTableProperties();
        Set<String> urls2 = transformer.fetchUrls(tableProperties);
        verifyNoMoreInteractions(crawlControllerFactory);
        assertSame(urls, urls2);
        verifyNoInteractions(redirectsProvider);
    }

    @Test
    void testFetchUrlsTwiceWithSameProperties()
    {
        String seedRelativeUrlsProperty = "/seed1";
        String mainAppPage = buildAppPageUrl(DEFAULT_RELATIVE_URL);
        CrawlController crawlController = mockCrawlerControllerFactory(mainAppPage);
        InOrder ordered = inOrder(crawlControllerFactory, crawlController);
        TableProperties tableProperties = buildTableProperties();
        transformer.setSeedRelativeUrls(toSet(seedRelativeUrlsProperty));
        Set<String> urls = runUrlFetching(mainAppPage, tableProperties,
                List.of(seedRelativeUrlsProperty), crawlController, ordered);
        assertThat(urls, equalTo(Set.of(OUTGOING_ABSOLUT_URL)));
        Set<String> urls2 = transformer.fetchUrls(tableProperties);
        verifyNoMoreInteractions(crawlControllerFactory, crawlController);
        assertThat(urls2, equalTo(Set.of(OUTGOING_ABSOLUT_URL)));
        assertSame(urls, urls2);
        verifyNoInteractions(redirectsProvider);
    }

    @Test
    void testFetchUrlsWhenSeedRelativeUrlsAreSetViaConfiguration()
    {
        String seedRelativeUrl = "/fromConfig";
        transformer.setSeedRelativeUrls(Set.of(seedRelativeUrl));
        Set<String> urls = testFetchUrls(DEFAULT_RELATIVE_URL, List.of(seedRelativeUrl));
        assertThat(urls, equalTo(Set.of(OUTGOING_ABSOLUT_URL)));
        verifyNoInteractions(redirectsProvider);
    }

    private Set<String> testFetchUrls(String mainAppPageRelativeUrl, List<String> expectedSeedRelativeUrls)
    {
        String mainAppPage = buildAppPageUrl(mainAppPageRelativeUrl);
        CrawlController crawlController = mockCrawlerControllerFactory(mainAppPage);
        TableProperties tableProperties = buildTableProperties();
        return runUrlFetching(mainAppPage, tableProperties, expectedSeedRelativeUrls, crawlController);
    }

    private Set<String> runUrlFetching(String mainAppPage, TableProperties tableProperties,
                                       List<String> expectedSeedRelativeUrls, CrawlController crawlController)
    {
        InOrder ordered = inOrder(crawlControllerFactory, crawlController);
        return runUrlFetching(mainAppPage, tableProperties, expectedSeedRelativeUrls,
                crawlController, ordered);
    }

    private Set<String> runUrlFetching(String mainAppPage, TableProperties tableProperties,
                                       List<String> expectedSeedRelativeUrls, CrawlController crawlController,
                                       InOrder ordered)
    {
        URI mainAppPageUri = URI.create(mainAppPage);
        doNothing().when(crawlController).start((WebCrawlerFactory<?>) argThat(factory ->
        {
            if (factory instanceof LinkCrawlerFactory)
            {
                LinkCrawler linkCrawler = ((LinkCrawlerFactory) factory).newInstance();
                HtmlParseData htmlParseData = new HtmlParseData();
                String outgoingUrl = UriUtils.buildNewUrl(mainAppPage, OUTGOING_ABSOLUT_URL).toString();
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
        Stream.concat(Stream.of(mainAppPage),
                expectedSeedRelativeUrls.stream().map(HeadlessCrawlerTableTransformerTests::buildAppPageUrl))
                .forEach(url -> ordered.verify(crawlController).addSeed(url));
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

    private TableProperties buildTableProperties()
    {
        return new TableProperties(parameterConverters, new Properties());
    }

    private static WebURL createWebUrl(String url)
    {
        WebURL webUrl = new WebURL();
        webUrl.setURL(url);
        return webUrl;
    }

    private static Set<String> toSet(String... strings)
    {
        return new HashSet<>(asList(strings));
    }
}
