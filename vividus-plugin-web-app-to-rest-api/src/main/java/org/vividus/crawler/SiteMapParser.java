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

import java.net.URI;
import java.net.URL;
import java.util.Collection;

public interface SiteMapParser
{
    /**
     * Parses sitemap.xml to a collection of URLs
     *
     * @param strict whether invalid URLs will be rejected
     * @param siteMapUrl URL of sitemap.xml
     * @return collection of sitemap URLs
     * @throws SiteMapParseException if error happens during sitemap.xml parsing
     */
    Collection<URL> parse(boolean strict, URI siteMapUrl) throws SiteMapParseException;
}
