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

package org.vividus.crawler.transformer;

import static com.github.valfirst.slf4jtest.LoggingEvent.warn;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Set;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;

import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.model.ExamplesTable.TableProperties;
import org.jbehave.core.steps.ParameterConverters;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.crawler.ISiteMapParser;
import org.vividus.crawler.SiteMapParseException;
import org.vividus.http.HttpRedirectsProvider;
import org.vividus.ui.web.configuration.WebApplicationConfiguration;

import crawlercommons.sitemaps.SiteMapURL;

@ExtendWith(MockitoExtension.class)
class SiteMapTableTransformerTests
{
    private static final String SOME_URL = "http://www.some.url";
    private static final String NO_URLS_FOUND_MESSAGE = "No URLs found in sitemap, or all URLs were filtered";
    private static final String SITEMAP = "Sitemap";
    private static final String TRUE = "true";
    private static final String IGNORE_ERRORS_PROPERTY_NAME = "ignoreErrors";
    private static final String SITEMAP_XML = "/org/vividus/sitemap/sitemap.xml";
    private static final URI MAIN_APP_PAGE = URI.create(SOME_URL);
    private static final Set<SiteMapURL> SITEMAP_URLS = Set.of(new SiteMapURL(SOME_URL + "/product", true));
    private static final String OUTGOING_ABSOLUT_URL = "http://www.some.url/product";

    private final TestLogger logger = TestLoggerFactory.getTestLogger(SiteMapTableTransformer.class);

    @Mock private ISiteMapParser siteMapParser;
    @Mock private WebApplicationConfiguration webApplicationConfiguration;
    @Mock private HttpRedirectsProvider redirectsProvider;
    @InjectMocks private SiteMapTableTransformer siteMapTableTransformer;

    private final Keywords keywords = new Keywords();
    private final ParameterConverters parameterConverters =  new ParameterConverters();

    @Test
    void testFetchUrls() throws SiteMapParseException
    {
        when(webApplicationConfiguration.getMainApplicationPageUrl()).thenReturn(MAIN_APP_PAGE);
        when(siteMapParser.parse(true, MAIN_APP_PAGE, SITEMAP_XML)).thenReturn(SITEMAP_URLS);
        var properties = createTableProperties();
        var actual = siteMapTableTransformer.fetchUrls(properties);
        assertEquals(Set.of(OUTGOING_ABSOLUT_URL), actual);

        var actualFromCache = siteMapTableTransformer.fetchUrls(properties);
        assertSame(actual, actualFromCache);

        var ordered = inOrder(webApplicationConfiguration, siteMapParser);
        ordered.verify(webApplicationConfiguration).getMainApplicationPageUrl();
        ordered.verify(siteMapParser).parse(true, MAIN_APP_PAGE, SITEMAP_XML);
        ordered.verify(webApplicationConfiguration).getMainApplicationPageUrl();
        ordered.verifyNoMoreInteractions();
    }

    @Test
    void testFetchUrlsWithoutSiteMapRelativeUrl()
    {
        var tableProperties = new TableProperties("", keywords, parameterConverters);
        var exception = assertThrows(IllegalArgumentException.class,
            () -> siteMapTableTransformer.fetchUrls(tableProperties));
        assertEquals("'siteMapRelativeUrl' is not set in ExamplesTable properties", exception.getMessage());
    }

    @Test
    void testEmptySiteMapUrls() throws SiteMapParseException
    {
        when(webApplicationConfiguration.getMainApplicationPageUrl()).thenReturn(MAIN_APP_PAGE);
        when(siteMapParser.parse(true, MAIN_APP_PAGE, SITEMAP_XML)).thenReturn(Set.of());
        var properties = createTableProperties();
        var exception = assertThrows(SiteMapTableGenerationException.class,
            () -> siteMapTableTransformer.transform("", null, properties));
        assertEquals(NO_URLS_FOUND_MESSAGE, exception.getMessage());
    }

    @Test
    void testFetchUrlsEmptySiteMapUrlsIgnoreErrorsSetViaTablePropertyTrueViaPropertyFalse()
            throws SiteMapParseException
    {
        when(webApplicationConfiguration.getMainApplicationPageUrl()).thenReturn(MAIN_APP_PAGE);
        when(siteMapParser.parse(true, MAIN_APP_PAGE, SITEMAP_XML)).thenReturn(Set.of());
        var properties = createTableProperties();
        properties.getProperties().put(IGNORE_ERRORS_PROPERTY_NAME, TRUE);
        assertEquals(Set.of(), siteMapTableTransformer.fetchUrls(properties));
    }

    @Test
    void testFetchUrlsEmptySiteMapUrlsIgnoreErrorsSetViaPropertyTrueViaTableFalse() throws SiteMapParseException
    {
        when(webApplicationConfiguration.getMainApplicationPageUrl()).thenReturn(MAIN_APP_PAGE);
        when(siteMapParser.parse(true, MAIN_APP_PAGE, SITEMAP_XML)).thenReturn(Set.of());
        var properties = createTableProperties();
        properties.getProperties().put(IGNORE_ERRORS_PROPERTY_NAME, "false");
        siteMapTableTransformer.setIgnoreErrors(true);
        var exception = assertThrows(SiteMapTableGenerationException.class,
            () -> siteMapTableTransformer.transform("", null, properties));
        assertEquals(NO_URLS_FOUND_MESSAGE, exception.getMessage());
    }

    @Test
    void testTransformSitemapIsInvalid() throws SiteMapParseException
    {
        when(webApplicationConfiguration.getMainApplicationPageUrl()).thenReturn(MAIN_APP_PAGE);
        SiteMapParseException exception = new SiteMapParseException(SITEMAP, new IOException());
        when(siteMapParser.parse(true, MAIN_APP_PAGE, SITEMAP_XML)).thenThrow(exception);
        var properties = createTableProperties();
        var actualException = assertThrows(IllegalStateException.class,
            () -> siteMapTableTransformer.transform("", null, properties));
        assertEquals(exception, actualException.getCause());
    }

    @Test
    void testTransformSitemapIsInvalidIgnoreErrorsSetViaProperty() throws SiteMapParseException
    {
        when(webApplicationConfiguration.getMainApplicationPageUrl()).thenReturn(MAIN_APP_PAGE);
        var exception = new SiteMapParseException(SITEMAP, new IOException());
        when(siteMapParser.parse(true, MAIN_APP_PAGE, SITEMAP_XML)).thenThrow(exception);
        siteMapTableTransformer.setIgnoreErrors(true);
        assertEquals(Set.of(), siteMapTableTransformer.fetchUrls(createTableProperties()));
    }

    @Test
    void testTransformSitemapIsInvalidIgnoreErrorsSetViaExamplesTableProperty() throws SiteMapParseException
    {
        when(webApplicationConfiguration.getMainApplicationPageUrl()).thenReturn(MAIN_APP_PAGE);
        var exception = new SiteMapParseException(SITEMAP, new IOException());
        when(siteMapParser.parse(true, MAIN_APP_PAGE, SITEMAP_XML)).thenThrow(exception);
        var properties = createTableProperties();
        properties.getProperties().put(IGNORE_ERRORS_PROPERTY_NAME, TRUE);
        assertEquals(Set.of(), siteMapTableTransformer.fetchUrls(properties));
    }

    @Test
    void testThrowIllegalStateException() throws SiteMapParseException
    {
        when(webApplicationConfiguration.getMainApplicationPageUrl()).thenReturn(MAIN_APP_PAGE);
        when(siteMapParser.parse(true, MAIN_APP_PAGE, SITEMAP_XML)).thenReturn(SITEMAP_URLS);
        siteMapTableTransformer.setFilterRedirects(true);
        var exception = new IllegalStateException();
        when(redirectsProvider.getRedirects(URI.create(OUTGOING_ABSOLUT_URL))).thenThrow(exception);
        var actual = siteMapTableTransformer.fetchUrls(createTableProperties());
        assertEquals(Set.of(OUTGOING_ABSOLUT_URL), actual);
        assertThat(logger.getLoggingEvents(), is(List.of(warn(exception, "Exception during redirects receiving"))));
    }

    @Test
    void testNoRedirects() throws SiteMapParseException
    {
        when(webApplicationConfiguration.getMainApplicationPageUrl()).thenReturn(MAIN_APP_PAGE);
        when(siteMapParser.parse(true, MAIN_APP_PAGE, SITEMAP_XML)).thenReturn(SITEMAP_URLS);
        siteMapTableTransformer.setFilterRedirects(true);
        when(redirectsProvider.getRedirects(URI.create(OUTGOING_ABSOLUT_URL))).thenReturn(List.of());
        var actual = siteMapTableTransformer.fetchUrls(createTableProperties());
        assertThat(actual, equalTo(Set.of(OUTGOING_ABSOLUT_URL)));
    }

    private TableProperties createTableProperties()
    {
        return new TableProperties("siteMapRelativeUrl=" + SITEMAP_XML, keywords, parameterConverters);
    }
}
