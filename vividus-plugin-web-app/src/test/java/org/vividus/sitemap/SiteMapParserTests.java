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

package org.vividus.sitemap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.http.client.protocol.HttpClientContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.http.client.HttpResponse;
import org.vividus.http.client.IHttpClient;
import org.vividus.util.ResourceUtils;
import org.vividus.util.UriUtils;

import crawlercommons.sitemaps.SiteMapURL;

@ExtendWith(MockitoExtension.class)
class SiteMapParserTests
{
    private static final String SITEMAP_XML = "sitemap.xml";
    private static final String SITE_URL = "https://www.vividus.site/";
    private static final String RELATIVE_SITE_MAP_URL = "/sitemap.xml";

    private static final URI SITE_MAP_URL = URI.create(SITE_URL + SITEMAP_XML);
    private static final URI REDIRECTED_SITE_MAP_URL = URI.create("http://www.vividus.site/sitemap.xml");

    private static final int SITEMAP_URL_NUMBER = 6;
    private static final int UNIQUE_SITEMAP_URL_NUMBER = 4;
    private static final int REDIRECTED_SITEMAP_URL_NUMBER = 3;

    private static final URI SITEMAP_ENTRY_URL = URI.create(SITE_URL + "kotlin");
    private static final URI REDIRECTED_SITEMAP_ENTRY_URL = URI.create("http://www.vividus.site/groovy");

    @Mock
    private IHttpClient mockedHttpClient;

    @InjectMocks
    private SiteMapParser siteMapParser;

    @Test
    void testParse() throws IOException, SiteMapParseException
    {
        mockSiteMapParsing(SITEMAP_XML, SITE_MAP_URL, null);
        siteMapParser.setFollowRedirects(false);
        Collection<SiteMapURL> siteMapUrls = siteMapParser.parse(true, SITE_MAP_URL);
        assertSiteMapUrls(SITEMAP_URL_NUMBER, SITEMAP_ENTRY_URL, siteMapUrls);
    }

    @Test
    void testParseFollowingRedirects() throws IOException, SiteMapParseException
    {
        mockSiteMapParsing(SITEMAP_XML, SITE_MAP_URL, List.of(REDIRECTED_SITE_MAP_URL));
        siteMapParser.setFollowRedirects(true);
        Collection<SiteMapURL> siteMapUrls = siteMapParser.parse(true, SITE_MAP_URL);
        assertSiteMapUrls(REDIRECTED_SITEMAP_URL_NUMBER, REDIRECTED_SITEMAP_ENTRY_URL, siteMapUrls);
    }

    @Test
    void testParseFollowingRedirectsWhenNoRedirectHappened() throws IOException, SiteMapParseException
    {
        mockSiteMapParsing(SITEMAP_XML, SITE_MAP_URL, null);
        siteMapParser.setFollowRedirects(true);
        Collection<SiteMapURL> siteMapUrls = siteMapParser.parse(true, SITE_MAP_URL);
        assertSiteMapUrls(SITEMAP_URL_NUMBER, SITEMAP_ENTRY_URL, siteMapUrls);
    }

    @Test
    void testParseSiteMapWithUserInfoInUrl() throws IOException, SiteMapParseException
    {
        URI siteMapUrl = UriUtils.addUserInfo(SITE_MAP_URL, "user:pass");
        mockSiteMapParsing(SITEMAP_XML, siteMapUrl, null);
        siteMapParser.setFollowRedirects(false);
        Collection<SiteMapURL> siteMapUrls = siteMapParser.parse(true, siteMapUrl);
        assertSiteMapUrls(SITEMAP_URL_NUMBER, SITEMAP_ENTRY_URL, siteMapUrls);
    }

    @Test
    void testIOExceptionThrown() throws IOException
    {
        IOException ioException = new IOException();
        when(mockedHttpClient.doHttpGet(eq(SITE_MAP_URL), any())).thenThrow(ioException);
        SiteMapParseException exception = assertThrows(SiteMapParseException.class,
            () -> siteMapParser.parse(false, SITE_MAP_URL));
        assertEquals(ioException, exception.getCause());
    }

    @Test
    void testParseWithSiteUrlAndRelativeSitemapUrl() throws IOException, SiteMapParseException
    {
        mockSiteMapParsing(SITEMAP_XML, SITE_MAP_URL, null);
        siteMapParser.setFollowRedirects(false);
        siteMapParser.setSiteUrl(Optional.empty());
        Collection<SiteMapURL> siteMapUrls = siteMapParser.parse(true, URI.create(SITE_URL), RELATIVE_SITE_MAP_URL);
        assertSiteMapUrls(SITEMAP_URL_NUMBER, SITEMAP_ENTRY_URL, siteMapUrls);
    }

    @Test
    void testParseToRelativeUrlsRelativeSitemapUrl() throws IOException, SiteMapParseException
    {
        mockSiteMapParsing(SITEMAP_XML, SITE_MAP_URL, null);
        siteMapParser.setFollowRedirects(false);
        siteMapParser.setSiteUrl(Optional.of(URI.create(SITE_URL)));
        Set<String> siteMapRelativeUrls = siteMapParser.parseToRelativeUrls(true, null, RELATIVE_SITE_MAP_URL);
        assertEquals(UNIQUE_SITEMAP_URL_NUMBER, siteMapRelativeUrls.size());
    }

    @Test
    void testParseToRelativeUrls() throws IOException, SiteMapParseException
    {
        mockSiteMapParsing(SITEMAP_XML, SITE_MAP_URL, null);
        siteMapParser.setFollowRedirects(false);
        Set<String> siteMapRelativeUrls = siteMapParser.parseToRelativeUrls(true, SITE_MAP_URL);
        assertEquals(UNIQUE_SITEMAP_URL_NUMBER, siteMapRelativeUrls.size());
    }

    @Test
    void testParseWithSpecifiedBaseUrl() throws IOException, SiteMapParseException
    {
        mockSiteMapParsing("sitemapForBaseDomain.xml", SITE_MAP_URL, null);
        siteMapParser.setBaseUrl(Optional.of(URI.create("http://www.baseDomain.com/")));
        Collection<SiteMapURL> siteMapUrls = siteMapParser.parse(true, SITE_MAP_URL);
        assertSiteMapUrls(3, SITEMAP_ENTRY_URL, siteMapUrls);
    }

    @Test
    void testParseSiteMapIndex() throws IOException, SiteMapParseException
    {
        mockHttpGet("sitemap-index.xml", SITE_MAP_URL, null);
        mockSiteMapParsing(SITEMAP_XML, URI.create(SITE_URL + "sitemap-misc.xml"), null);
        siteMapParser.setFollowRedirects(false);
        Collection<SiteMapURL> siteMapUrls = siteMapParser.parse(true, SITE_MAP_URL);
        assertSiteMapUrls(SITEMAP_URL_NUMBER, SITEMAP_ENTRY_URL, siteMapUrls);
    }

    private void mockSiteMapParsing(String resourceName, URI siteMapUrl, List<URI> redirectLocations) throws IOException
    {
        mockHttpGet(resourceName, siteMapUrl, redirectLocations);
        siteMapParser.setBaseUrl(Optional.empty());
    }

    private void mockHttpGet(String resourceName, URI siteMapUrl, List<URI> redirectLocations) throws IOException
    {
        HttpResponse httpResponse = new HttpResponse();
        httpResponse.setResponseBody(ResourceUtils.loadResourceAsByteArray(getClass(), resourceName));
        when(mockedHttpClient.doHttpGet(eq(siteMapUrl), argThat(context ->
        {
            context.setAttribute(HttpClientContext.REDIRECT_LOCATIONS, redirectLocations);
            return true;
        }))).thenReturn(httpResponse);
    }

    private void assertSiteMapUrls(int siteMapUrlNumber, URI sitemapEntryUrl, Collection<SiteMapURL> siteMapUrls)
            throws MalformedURLException
    {
        assertEquals(siteMapUrlNumber, siteMapUrls.size());
        Iterator<SiteMapURL> iterator = siteMapUrls.iterator();
        iterator.next();
        iterator.next();
        assertEquals(sitemapEntryUrl.toURL().getPath(), iterator.next().getUrl().getPath());
    }
}
