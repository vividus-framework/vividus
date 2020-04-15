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

package org.vividus.transformer;

import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;

import org.jbehave.core.model.ExamplesTable.ExamplesTableProperties;
import org.vividus.bdd.transformer.ExtendedTableTransformer;
import org.vividus.model.SiteMap;
import org.vividus.sitemap.ISiteMapParser;
import org.vividus.sitemap.SiteMapParseException;

import crawlercommons.sitemaps.SiteMapURL;

public class SiteMapTableTransformer extends AbstractFetchingUrlsTableTransformer
{
    private ISiteMapParser siteMapParser;
    private boolean ignoreErrors;

    private final List<SiteMap> siteMaps = Collections.synchronizedList(new LinkedList<>());

    @Override
    public Set<String> fetchUrls(ExamplesTableProperties properties)
    {
        Properties tableProperties = properties.getProperties();
        boolean throwException = !Optional.ofNullable(tableProperties.getProperty("ignoreErrors"))
                .map(Boolean::valueOf)
                .orElse(ignoreErrors);
        String siteMapRelativeUrl = ExtendedTableTransformer.getMandatoryNonBlankProperty(properties,
                "siteMapRelativeUrl");

        URI mainApplicationPage = getMainApplicationPageUri();
        Set<String> urls = siteMaps.stream().filter(
            s -> s.getMainAppPage().equals(mainApplicationPage) && s.getSiteMapRelativeUrl().equals(siteMapRelativeUrl))
            .findFirst().orElseGet(() ->
            {
                Set<String> siteMapRelativeUrls;
                try
                {
                    siteMapRelativeUrls = filterResults(siteMapParser.parse(true, mainApplicationPage,
                            siteMapRelativeUrl).stream()
                            .map(SiteMapURL::getUrl)
                            .map(URL::toString));
                }
                catch (SiteMapParseException e)
                {
                    if (throwException)
                    {
                        throw new IllegalStateException(e);
                    }
                    siteMapRelativeUrls = Set.of();
                }
                SiteMap siteMap = new SiteMap(mainApplicationPage, siteMapRelativeUrl, siteMapRelativeUrls);
                siteMaps.add(siteMap);
                return siteMap;
            }).getUrls();
        if (urls.isEmpty() && throwException)
        {
            throw new SiteMapTableGenerationException("No URLs found in sitemap, or all URLs were filtered");
        }
        return urls;
    }

    public void setSiteMapParser(ISiteMapParser siteMapParser)
    {
        this.siteMapParser = siteMapParser;
    }

    public void setIgnoreErrors(boolean ignoreErrors)
    {
        this.ignoreErrors = ignoreErrors;
    }
}
