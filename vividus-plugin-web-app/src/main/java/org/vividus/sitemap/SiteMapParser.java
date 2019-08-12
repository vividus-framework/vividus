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

package org.vividus.sitemap;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.http.client.protocol.HttpClientContext;
import org.vividus.http.client.HttpResponse;
import org.vividus.http.client.IHttpClient;
import org.vividus.util.UriUtils;

import crawlercommons.sitemaps.AbstractSiteMap;
import crawlercommons.sitemaps.AbstractSiteMap.SitemapType;
import crawlercommons.sitemaps.SiteMap;
import crawlercommons.sitemaps.SiteMapIndex;
import crawlercommons.sitemaps.SiteMapURL;
import crawlercommons.sitemaps.UnknownFormatException;

public class SiteMapParser implements ISiteMapParser
{
    private IHttpClient httpClient;

    private Optional<URI> siteUrl;
    private Optional<URI> baseUrl;
    private boolean followRedirects;

    @Override
    public Collection<SiteMapURL> parse(boolean strict, URI siteMapUrl) throws SiteMapParseException
    {
        return parse(siteMapUrl, new crawlercommons.sitemaps.SiteMapParser(strict));
    }

    private Collection<SiteMapURL> parse(URI siteMapUrl, crawlercommons.sitemaps.SiteMapParser siteMapParser)
            throws SiteMapParseException
    {
        try
        {
            HttpClientContext context = new HttpClientContext();
            HttpResponse response = httpClient.doHttpGet(siteMapUrl, context);
            URI cleanSiteMapUrl = UriUtils.removeUserInfo(getBaseUri(context, siteMapUrl));
            AbstractSiteMap siteMap = siteMapParser.parseSiteMap(response.getResponseBody(), cleanSiteMapUrl.toURL());
            if (siteMap.getType() == SitemapType.INDEX)
            {
                List<SiteMapURL> siteMapUrls = new LinkedList<>();
                for (AbstractSiteMap siteMapFromIndex : ((SiteMapIndex) siteMap).getSitemaps())
                {
                    siteMapUrls.addAll(parse(siteMapFromIndex.getUrl().toURI(), siteMapParser));
                }
                return siteMapUrls;
            }
            return ((SiteMap) siteMap).getSiteMapUrls();
        }
        catch (IOException | UnknownFormatException | URISyntaxException e)
        {
            throw new SiteMapParseException(e.getMessage(), e);
        }
    }

    private URI getBaseUri(HttpClientContext context, URI siteMapUrl)
    {
        return this.baseUrl.orElseGet(() -> {
            if (followRedirects)
            {
                List<URI> redirectLocations = context.getRedirectLocations();
                if (redirectLocations != null)
                {
                    return redirectLocations.get(redirectLocations.size() - 1);
                }
            }
            return siteMapUrl;
        });
    }

    @Override
    public Set<String> parseToRelativeUrls(boolean strict, URI siteMapUrl) throws SiteMapParseException
    {
        return parse(strict, siteMapUrl).stream().map(SiteMapURL::getUrl).map(URL::getPath).collect(Collectors.toSet());
    }

    @Override
    public Collection<SiteMapURL> parse(boolean strict, URI siteUrl, String siteMapRelativeUrl)
            throws SiteMapParseException
    {
        URI siteMapUrl = buildSiteMapUrl(siteUrl, siteMapRelativeUrl);
        return parse(strict, siteMapUrl);
    }

    @Override
    public Set<String> parseToRelativeUrls(boolean strict, URI siteUrl, String siteMapRelativeUrl)
            throws SiteMapParseException
    {
        URI siteMapUrl = buildSiteMapUrl(siteUrl, siteMapRelativeUrl);
        return parseToRelativeUrls(strict, siteMapUrl);
    }

    private URI buildSiteMapUrl(URI siteUrl, String siteMapRelativeUrl)
    {
        return UriUtils.buildNewRelativeUrl(this.siteUrl.orElse(siteUrl), siteMapRelativeUrl);
    }

    public void setHttpClient(IHttpClient httpClient)
    {
        this.httpClient = httpClient;
    }

    public void setSiteUrl(Optional<URI> siteUrl)
    {
        this.siteUrl = siteUrl;
    }

    public void setBaseUrl(Optional<URI> baseUrl)
    {
        this.baseUrl = baseUrl;
    }

    public void setFollowRedirects(boolean followRedirects)
    {
        this.followRedirects = followRedirects;
    }
}
