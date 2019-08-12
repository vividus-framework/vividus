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

import java.net.URI;
import java.util.Collection;
import java.util.Set;

import crawlercommons.sitemaps.SiteMapURL;

public interface ISiteMapParser
{
    /**
     * Parses sitemap.xml to a collection of URLs
     *
     * @param strict whether invalid URLs will be rejected
     * @param siteMapUrl URL of sitemap.xml
     * @return collection of sitemap URLs
     * @throws SiteMapParseException if error happens during sitemap.xml parsing
     */
    Collection<SiteMapURL> parse(boolean strict, URI siteMapUrl) throws SiteMapParseException;

    /**
     * Parses sitemap.xml to a collection of URLs
     *
     * @param strict whether invalid URLs will be rejected
     * @param siteUrl URL of site
     * @param siteMapRelativeUrl Relative sitemap URL, e.g. "/sitemap.xml"
     * @return collection of sitemap URLs
     * @throws SiteMapParseException if error happens during sitemap.xml parsing
     */
    Collection<SiteMapURL> parse(boolean strict, URI siteUrl, String siteMapRelativeUrl) throws SiteMapParseException;

    /**
     * Parses sitemap.xml to a set of unique relative URLs
     *
     * @param strict whether invalid URLs will be rejected
     * @param siteUrl URL of site
     * @param siteMapRelativeUrl Relative sitemap URL, e.g. "/sitemap.xml"
     * @return set of unique relative URLs
     * @throws SiteMapParseException if error happens during sitemap.xml parsing
     */
    Set<String> parseToRelativeUrls(boolean strict, URI siteUrl, String siteMapRelativeUrl)
            throws SiteMapParseException;

    /**
     * Parses sitemap.xml to a set of unique relative URLs
     *
     * @param strict whether invalid URLs will be rejected
     * @param siteMapUrl URL of sitemap.xml
     * @return set of unique relative URLs
     * @throws SiteMapParseException if error happens during sitemap.xml parsing
     */
    Set<String> parseToRelativeUrls(boolean strict, URI siteMapUrl) throws SiteMapParseException;
}
