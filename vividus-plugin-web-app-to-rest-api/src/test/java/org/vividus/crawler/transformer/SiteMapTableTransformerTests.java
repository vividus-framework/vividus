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

package org.vividus.crawler.transformer;

import static com.github.valfirst.slf4jtest.LoggingEvent.warn;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.crawler.SiteMapParseException;
import org.vividus.crawler.SiteMapParser;
import org.vividus.http.HttpRedirectsProvider;
import org.vividus.ui.web.configuration.WebApplicationConfiguration;

@ExtendWith({ MockitoExtension.class, TestLoggerFactoryExtension.class })
class SiteMapTableTransformerTests
{
    private static final String SOME_URL = "http://www.some.url";
    private static final String NO_URLS_FOUND_MESSAGE = "No URLs found in sitemap, or all URLs were filtered";
    private static final String SITEMAP = "Sitemap";
    private static final String IGNORE_ERRORS_PARAMETER_NAME = "ignoreErrors";
    private static final String STRICT_PARAMETER_NAME = "strict";
    private static final URI MAIN_APP_PAGE = URI.create(SOME_URL);
    private static final String SITEMAP_RELATIVE_URL = "/org/vividus/sitemap/sitemap.xml";
    private static final URI SITEMAP_XML = URI.create(SOME_URL + SITEMAP_RELATIVE_URL);

    private static final String OUTGOING_ABSOLUTE_SIMPLE_URL = "http://www.some.url/product";
    private static final String OUTGOING_ABSOLUTE_NOT_ENCODED_URL = "http://www.some.url/Juridisk erklæring";
    private static final String OUTGOING_ABSOLUTE_ENCODED_URL = "http://www.some.url/Juridisk%20erkl%C3%A6ring";

    private static final String PRODUCT_URL = SOME_URL + "/product";
    private static final Set<String> SITEMAP_URLS = Set.of(
            PRODUCT_URL,
            /*
            Some sitemaps may contain invalid URLs: not encoded, it's not allowed according to the standard:
            https://www.sitemaps.org/protocol.html#escaping, but we are trying to handle such cases
            */
            SOME_URL + "/Juridisk erklæring"
        );

    private final TestLogger logger = TestLoggerFactory.getTestLogger(SiteMapTableTransformer.class);

    @Mock private SiteMapParser siteMapParser;
    @Mock private WebApplicationConfiguration webApplicationConfiguration;
    @Mock private HttpRedirectsProvider redirectsProvider;

    private final Keywords keywords = new Keywords();
    private final ParameterConverters parameterConverters =  new ParameterConverters();

    private static List<URL> getSiteMapUrls() throws MalformedURLException
    {
        List<URL> siteMapUrls = new ArrayList<>();
        for (String siteMapUrl : SITEMAP_URLS)
        {
            siteMapUrls.add(new URL(siteMapUrl));
        }
        return siteMapUrls;
    }

    @Test
    void testFetchUrls() throws SiteMapParseException, MalformedURLException
    {
        when(webApplicationConfiguration.getMainApplicationPageUrl()).thenReturn(MAIN_APP_PAGE);
        var strict = true;
        when(siteMapParser.parse(strict, SITEMAP_XML)).thenReturn(getSiteMapUrls());

        var siteMapTableTransformer = new SiteMapTableTransformer(siteMapParser, false, strict);
        siteMapTableTransformer.setWebApplicationConfiguration(webApplicationConfiguration);

        var properties = createTableProperties();
        var actual = siteMapTableTransformer.fetchUrls(properties);
        assertEquals(Set.of(OUTGOING_ABSOLUTE_SIMPLE_URL, OUTGOING_ABSOLUTE_NOT_ENCODED_URL), actual);

        var actualFromCache = siteMapTableTransformer.fetchUrls(properties);
        assertSame(actual, actualFromCache);

        var ordered = inOrder(webApplicationConfiguration, siteMapParser);
        ordered.verify(webApplicationConfiguration).getMainApplicationPageUrl();
        ordered.verify(siteMapParser).parse(strict, SITEMAP_XML);
        ordered.verify(webApplicationConfiguration).getMainApplicationPageUrl();
        ordered.verifyNoMoreInteractions();
    }

    @Test
    void testFetchUrlsWithoutSiteMapRelativeUrl()
    {
        var tableProperties = new TableProperties("", keywords, parameterConverters);
        var siteMapTableTransformer = new SiteMapTableTransformer(siteMapParser, false, false);
        var exception = assertThrows(IllegalArgumentException.class,
            () -> siteMapTableTransformer.fetchUrls(tableProperties));
        assertEquals("'siteMapRelativeUrl' is not set in ExamplesTable properties", exception.getMessage());
    }

    @Test
    void testEmptySiteMapUrls() throws SiteMapParseException
    {
        when(webApplicationConfiguration.getMainApplicationPageUrl()).thenReturn(MAIN_APP_PAGE);
        var strict = false;
        when(siteMapParser.parse(strict, SITEMAP_XML)).thenReturn(Set.of());

        var siteMapTableTransformer = new SiteMapTableTransformer(siteMapParser, false, strict);
        siteMapTableTransformer.setWebApplicationConfiguration(webApplicationConfiguration);

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
        when(siteMapParser.parse(true, SITEMAP_XML)).thenReturn(Set.of());

        var siteMapTableTransformer = new SiteMapTableTransformer(siteMapParser, false, false);
        siteMapTableTransformer.setWebApplicationConfiguration(webApplicationConfiguration);

        var properties = createTableProperties();
        properties.getProperties().put(IGNORE_ERRORS_PARAMETER_NAME, Boolean.TRUE.toString());
        properties.getProperties().put(STRICT_PARAMETER_NAME, Boolean.TRUE.toString());
        assertEquals(Set.of(), siteMapTableTransformer.fetchUrls(properties));
    }

    @Test
    void testFetchUrlsEmptySiteMapUrlsIgnoreErrorsSetViaPropertyTrueViaTableFalse() throws SiteMapParseException
    {
        when(webApplicationConfiguration.getMainApplicationPageUrl()).thenReturn(MAIN_APP_PAGE);
        when(siteMapParser.parse(false, SITEMAP_XML)).thenReturn(Set.of());

        var siteMapTableTransformer = new SiteMapTableTransformer(siteMapParser, true, true);
        siteMapTableTransformer.setWebApplicationConfiguration(webApplicationConfiguration);

        var properties = createTableProperties();
        properties.getProperties().put(IGNORE_ERRORS_PARAMETER_NAME, Boolean.FALSE.toString());
        properties.getProperties().put(STRICT_PARAMETER_NAME, Boolean.FALSE.toString());
        var exception = assertThrows(SiteMapTableGenerationException.class,
            () -> siteMapTableTransformer.transform("", null, properties));
        assertEquals(NO_URLS_FOUND_MESSAGE, exception.getMessage());
    }

    @Test
    void testTransformSitemapIsInvalid() throws SiteMapParseException
    {
        when(webApplicationConfiguration.getMainApplicationPageUrl()).thenReturn(MAIN_APP_PAGE);
        var exception = new SiteMapParseException(SITEMAP, new IOException());
        var strict = true;
        when(siteMapParser.parse(strict, SITEMAP_XML)).thenThrow(exception);

        var siteMapTableTransformer = new SiteMapTableTransformer(siteMapParser, false, strict);
        siteMapTableTransformer.setWebApplicationConfiguration(webApplicationConfiguration);

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
        var strict = true;
        when(siteMapParser.parse(strict, SITEMAP_XML)).thenThrow(exception);

        var siteMapTableTransformer = new SiteMapTableTransformer(siteMapParser, true, strict);
        siteMapTableTransformer.setWebApplicationConfiguration(webApplicationConfiguration);

        assertEquals(Set.of(), siteMapTableTransformer.fetchUrls(createTableProperties()));
    }

    @Test
    void testTransformSitemapIsInvalidIgnoreErrorsSetViaExamplesTableProperty() throws SiteMapParseException
    {
        when(webApplicationConfiguration.getMainApplicationPageUrl()).thenReturn(MAIN_APP_PAGE);
        var exception = new SiteMapParseException(SITEMAP, new IOException());
        var strict = true;
        when(siteMapParser.parse(strict, SITEMAP_XML)).thenThrow(exception);

        var siteMapTableTransformer = new SiteMapTableTransformer(siteMapParser, false, strict);
        siteMapTableTransformer.setWebApplicationConfiguration(webApplicationConfiguration);

        var properties = createTableProperties();
        properties.getProperties().put(IGNORE_ERRORS_PARAMETER_NAME, Boolean.TRUE.toString());
        assertEquals(Set.of(), siteMapTableTransformer.fetchUrls(properties));
    }

    @Test
    void testThrowHttpResponseException() throws SiteMapParseException, IOException
    {
        when(webApplicationConfiguration.getMainApplicationPageUrl()).thenReturn(MAIN_APP_PAGE);
        var strict = true;
        when(siteMapParser.parse(strict, SITEMAP_XML)).thenReturn(Set.of(new URL(PRODUCT_URL)));

        var siteMapTableTransformer = new SiteMapTableTransformer(siteMapParser, false, strict);
        siteMapTableTransformer.setWebApplicationConfiguration(webApplicationConfiguration);
        siteMapTableTransformer.setHttpRedirectsProvider(redirectsProvider);
        var prop = "transformer.from-sitemap.main-page-url";
        siteMapTableTransformer.setMainPageUrlProperty(prop);
        siteMapTableTransformer.setFilterRedirects(true);

        var httpResponseException = new HttpResponseException(HttpStatus.SC_NOT_FOUND, "");
        when(redirectsProvider.getRedirects(URI.create(OUTGOING_ABSOLUTE_SIMPLE_URL))).thenThrow(httpResponseException);
        var actual = siteMapTableTransformer.fetchUrls(createTableProperties());
        assertEquals(Set.of(OUTGOING_ABSOLUTE_SIMPLE_URL), actual);
        assertThat(logger.getLoggingEvents(),
                is(List.of(
                        warn("The use of {} property for setting of main page for crawling is deprecated and will "
                                + "be removed in VIVIDUS 0.7.0, please see use either {} transformer parameter or "
                                + "{} property.", "web-application.main-page-url", "mainPageUrl", prop),
                        warn(httpResponseException, "Exception during redirects receiving"))));
    }

    @Test
    void testNoRedirects() throws SiteMapParseException, IOException
    {
        when(webApplicationConfiguration.getMainApplicationPageUrl()).thenReturn(MAIN_APP_PAGE);
        var strict = true;
        when(siteMapParser.parse(strict, SITEMAP_XML)).thenReturn(getSiteMapUrls());
        when(redirectsProvider.getRedirects(URI.create(OUTGOING_ABSOLUTE_SIMPLE_URL))).thenReturn(List.of());
        when(redirectsProvider.getRedirects(URI.create(OUTGOING_ABSOLUTE_ENCODED_URL))).thenReturn(List.of());

        var siteMapTableTransformer = new SiteMapTableTransformer(siteMapParser, false, strict);
        siteMapTableTransformer.setFilterRedirects(true);
        siteMapTableTransformer.setWebApplicationConfiguration(webApplicationConfiguration);
        siteMapTableTransformer.setHttpRedirectsProvider(redirectsProvider);

        var actual = siteMapTableTransformer.fetchUrls(createTableProperties());
        assertEquals(Set.of(OUTGOING_ABSOLUTE_SIMPLE_URL, OUTGOING_ABSOLUTE_NOT_ENCODED_URL), actual);
    }

    private TableProperties createTableProperties()
    {
        return new TableProperties("siteMapRelativeUrl=" + SITEMAP_RELATIVE_URL, keywords, parameterConverters);
    }
}
