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

package org.vividus.crawler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.hc.client5.http.protocol.RedirectLocations;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.http.client.HttpResponse;
import org.vividus.http.client.IHttpClient;
import org.vividus.util.ResourceUtils;
import org.vividus.util.UriUtils;

@ExtendWith(MockitoExtension.class)
class CrawlerCommonsSiteMapParserTests
{
    private static final String SITEMAP_XML = "sitemap.xml";
    private static final String SITE_URL = "https://www.vividus.site/";

    private static final URI SITE_MAP_URL = URI.create(SITE_URL + SITEMAP_XML);
    private static final URI REDIRECTED_SITE_MAP_URL = URI.create("http://www.vividus.site/sitemap.xml");

    private static final int SITEMAP_URL_NUMBER = 6;
    private static final int REDIRECTED_SITEMAP_URL_NUMBER = 3;

    private static final URI SITEMAP_ENTRY_URL = URI.create(SITE_URL + "kotlin");
    private static final URI REDIRECTED_SITEMAP_ENTRY_URL = URI.create("http://www.vividus.site/groovy");

    @Mock private IHttpClient mockedHttpClient;
    @InjectMocks private CrawlerCommonsSiteMapParser siteMapParser;

    @Test
    void testParseFollowingRedirects() throws IOException, SiteMapParseException
    {
        RedirectLocations redirectLocations = mockSiteMapParsing(SITEMAP_XML, SITE_MAP_URL);
        when(redirectLocations.getAll()).thenReturn(List.of(REDIRECTED_SITE_MAP_URL));
        siteMapParser.setFollowRedirects(true);
        Collection<URL> siteMapUrls = siteMapParser.parse(true, SITE_MAP_URL);
        assertSiteMapUrls(REDIRECTED_SITEMAP_URL_NUMBER, REDIRECTED_SITEMAP_ENTRY_URL, siteMapUrls);
    }

    static Stream<Arguments> namedArguments()
    {
        return Stream.of(
                arguments(named("HTTP redirects are configured to not be followed", false)),
                arguments(named("HTTP redirects are configured to be followed, but no redirects happened", true))
        );
    }

    @DisplayName("Validate sitemap parsing")
    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("namedArguments")
    void shouldParseSiteMap(boolean followRedirects) throws IOException, SiteMapParseException
    {
        mockSiteMapParsing(SITEMAP_XML, SITE_MAP_URL);
        siteMapParser.setFollowRedirects(followRedirects);
        Collection<URL> siteMapUrls = siteMapParser.parse(true, SITE_MAP_URL);
        assertSiteMapUrls(SITEMAP_URL_NUMBER, SITEMAP_ENTRY_URL, siteMapUrls);
    }

    @Test
    void testParseSiteMapWithUserInfoInUrl() throws IOException, SiteMapParseException
    {
        URI siteMapUrl = UriUtils.addUserInfo(SITE_MAP_URL, "user:p%40ss");
        mockSiteMapParsing(SITEMAP_XML, siteMapUrl);

        Collection<URL> siteMapUrls = siteMapParser.parse(true, siteMapUrl);

        assertSiteMapUrls(SITEMAP_URL_NUMBER, SITEMAP_ENTRY_URL, siteMapUrls);
    }

    @Test
    void testIOExceptionThrown() throws IOException
    {
        var ioException = new IOException();
        when(mockedHttpClient.doHttpGet(SITE_MAP_URL, true)).thenThrow(ioException);
        var exception = assertThrows(SiteMapParseException.class,
                () -> siteMapParser.parse(false, SITE_MAP_URL));
        assertEquals(ioException, exception.getCause());
    }

    @Test
    void testParseWithSpecifiedBaseUrl() throws IOException, SiteMapParseException
    {
        mockSiteMapParsing("sitemapForBaseDomain.xml", SITE_MAP_URL);
        siteMapParser.setBaseUrl(Optional.of(URI.create("http://www.baseDomain.com/")));
        Collection<URL> siteMapUrls = siteMapParser.parse(true, SITE_MAP_URL);
        assertSiteMapUrls(3, SITEMAP_ENTRY_URL, siteMapUrls);
    }

    @Test
    void testParseSiteMapIndex() throws IOException, SiteMapParseException
    {
        mockHttpGet("sitemap-index.xml", SITE_MAP_URL);
        mockSiteMapParsing(SITEMAP_XML, URI.create(SITE_URL + "sitemap-misc.xml"));
        siteMapParser.setFollowRedirects(false);
        Collection<URL> siteMapUrls = siteMapParser.parse(true, SITE_MAP_URL);
        assertSiteMapUrls(SITEMAP_URL_NUMBER, SITEMAP_ENTRY_URL, siteMapUrls);
    }

    private RedirectLocations mockSiteMapParsing(String resourceName, URI siteMapUrl) throws IOException
    {
        siteMapParser.setBaseUrl(Optional.empty());
        return mockHttpGet(resourceName, siteMapUrl);
    }

    private RedirectLocations mockHttpGet(String resourceName, URI siteMapUrl) throws IOException
    {
        var httpResponse = new HttpResponse();
        httpResponse.setResponseBody(ResourceUtils.loadResourceAsByteArray(getClass(), resourceName));
        RedirectLocations locations = mock();
        httpResponse.setRedirectLocations(locations);
        when(mockedHttpClient.doHttpGet(siteMapUrl, true)).thenReturn(httpResponse);
        return locations;
    }

    private void assertSiteMapUrls(int siteMapUrlNumber, URI sitemapEntryUrl, Collection<URL> siteMapUrls)
            throws MalformedURLException
    {
        assertEquals(siteMapUrlNumber, siteMapUrls.size());
        Iterator<URL> iterator = siteMapUrls.iterator();
        iterator.next();
        iterator.next();
        assertEquals(sitemapEntryUrl.toURL().getPath(), iterator.next().getPath());
    }
}
