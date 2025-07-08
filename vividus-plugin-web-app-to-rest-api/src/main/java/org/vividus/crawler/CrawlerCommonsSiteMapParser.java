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

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.vividus.http.client.HttpResponse;
import org.vividus.http.client.IHttpClient;
import org.vividus.util.UriUtils;

import crawlercommons.sitemaps.AbstractSiteMap;
import crawlercommons.sitemaps.AbstractSiteMap.SitemapType;
import crawlercommons.sitemaps.SiteMap;
import crawlercommons.sitemaps.SiteMapIndex;
import crawlercommons.sitemaps.SiteMapURL;
import crawlercommons.sitemaps.UnknownFormatException;

public class CrawlerCommonsSiteMapParser implements SiteMapParser
{
    private IHttpClient httpClient;

    private Optional<URI> baseUrl;
    private boolean followRedirects;

    @Override
    public Collection<URL> parse(boolean strict, URI siteMapUrl) throws SiteMapParseException
    {
        return parse(siteMapUrl, new crawlercommons.sitemaps.SiteMapParser(strict)).stream()
                .map(SiteMapURL::getUrl)
                .toList();
    }

    private Collection<SiteMapURL> parse(URI siteMapUrl, crawlercommons.sitemaps.SiteMapParser siteMapParser)
            throws SiteMapParseException
    {
        try
        {
            HttpResponse response = httpClient.doHttpGet(siteMapUrl, true);
            URI cleanSiteMapUrl = UriUtils.removeUserInfo(getBaseUri(response, siteMapUrl));
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

    private URI getBaseUri(HttpResponse httpResponse, URI siteMapUrl)
    {
        return this.baseUrl.orElseGet(() -> {
            if (followRedirects)
            {
                List<URI> redirectLocations = httpResponse.getRedirectLocations().getAll();
                if (!redirectLocations.isEmpty())
                {
                    return redirectLocations.get(redirectLocations.size() - 1);
                }
            }
            return siteMapUrl;
        });
    }

    public void setHttpClient(IHttpClient httpClient)
    {
        this.httpClient = httpClient;
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
