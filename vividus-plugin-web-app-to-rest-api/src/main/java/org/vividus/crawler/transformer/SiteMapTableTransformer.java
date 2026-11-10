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

import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;

import org.jbehave.core.model.ExamplesTable.TableProperties;
import org.vividus.crawler.SiteMapParseException;
import org.vividus.crawler.SiteMapParser;
import org.vividus.util.UriUtils;

public class SiteMapTableTransformer extends AbstractFetchingUrlsTableTransformer
{
    private final SiteMapParser siteMapParser;
    private final boolean ignoreErrors;
    private final boolean strict;

    private final List<SiteMap> siteMaps = Collections.synchronizedList(new LinkedList<>());

    public SiteMapTableTransformer(SiteMapParser siteMapParser, boolean ignoreErrors, boolean strict)
    {
        this.siteMapParser = siteMapParser;
        this.ignoreErrors = ignoreErrors;
        this.strict = strict;
    }

    @Override
    public Set<String> fetchUrls(TableProperties properties)
    {
        Properties tableProperties = properties.getProperties();
        boolean throwException = !Optional.ofNullable(tableProperties.getProperty("ignoreErrors"))
                .map(Boolean::valueOf)
                .orElse(ignoreErrors);
        String siteMapRelativeUrl = properties.getMandatoryNonBlankProperty("siteMapRelativeUrl", String.class);
        boolean strictParameter = Optional.ofNullable(properties.getProperties().getProperty("strict"))
                .map(Boolean::valueOf)
                .orElse(strict);


        URI mainApplicationPage = getMainApplicationPageUri(properties);
        URI siteMapUrl = UriUtils.buildNewRelativeUrl(mainApplicationPage, siteMapRelativeUrl);
        Set<String> urls = siteMaps.stream().filter(s -> s.siteMapUrl.equals(siteMapUrl)).findFirst()
            .orElseGet(() ->
            {
                Set<String> siteMapUrls;
                try
                {
                    siteMapUrls = filterResults(siteMapParser.parse(strictParameter, siteMapUrl)
                            .stream()
                            .map(URL::toString)
                    );
                }
                catch (SiteMapParseException e)
                {
                    if (throwException)
                    {
                        throw new IllegalStateException(e);
                    }
                    siteMapUrls = Set.of();
                }
                SiteMap siteMap = new SiteMap(siteMapUrl, siteMapUrls);
                siteMaps.add(siteMap);
                return siteMap;
            }).urls();
        if (urls.isEmpty() && throwException)
        {
            throw new SiteMapTableGenerationException("No URLs found in sitemap, or all URLs were filtered");
        }
        return urls;
    }

    @Override
    protected URI parseUri(String uri)
    {
        return UriUtils.createUri(uri);
    }

    private record SiteMap(URI siteMapUrl, Set<String> urls)
    {
    }
}
