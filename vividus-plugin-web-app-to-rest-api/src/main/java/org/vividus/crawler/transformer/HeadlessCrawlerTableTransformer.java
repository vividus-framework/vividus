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
import java.util.Set;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import org.apache.commons.lang3.StringUtils;
import org.jbehave.core.model.ExamplesTable.TableProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.crawler.ICrawlControllerFactory;
import org.vividus.crawler.LinkCrawlerData;
import org.vividus.crawler.LinkCrawlerFactory;
import org.vividus.util.UriUtils;

import edu.uci.ics.crawler4j.crawler.CrawlController;

public class HeadlessCrawlerTableTransformer extends AbstractFetchingUrlsTableTransformer
{
    private static final Logger LOGGER = LoggerFactory.getLogger(HeadlessCrawlerTableTransformer.class);

    private static final String DEPRECATION_MESSAGE =
           "Property `transformer.from-headless-crawling.exclude-extensions-regex` is deprecated and will be removed in"
                    + " VIVIDUS 0.7.0. Please use `transformer.from-headless-crawling.exclude-urls-regex` instead.";
    private static final String FORWARD_SLASH = "/";
    private static final int NUMBER_OF_CRAWLERS = 50;

    private ICrawlControllerFactory crawlControllerFactory;

    private Set<String> seedRelativeUrls;

    private String excludeUrlsRegex;
    private String excludeExtensionsRegex;

    private final LoadingCache<URI, Set<String>> crawledUrlsCache = CacheBuilder.newBuilder()
            .build(new CacheLoader<>()
            {
                @Override
                public Set<String> load(URI mainApplicationPage)
                {
                    CrawlController controller = crawlControllerFactory.createCrawlController(mainApplicationPage);

                    addSeeds(mainApplicationPage, controller);

                    LinkCrawlerData linkCrawlerData = new LinkCrawlerData();
                    controller.start(new LinkCrawlerFactory(linkCrawlerData, excludeUrlsRegex), NUMBER_OF_CRAWLERS);
                    Set<String> absoluteUrls = linkCrawlerData.getAbsoluteUrls();
                    return filterResults(absoluteUrls.stream());
                }
            });

    private void addSeeds(URI mainApplicationPage, CrawlController controller)
    {
        addSeed(controller, mainApplicationPage.toString());
        if (this.seedRelativeUrls == null)
        {
            return;
        }
        String mainApplicationPagePath = StringUtils.appendIfMissing(mainApplicationPage.getPath(), FORWARD_SLASH);
        this.seedRelativeUrls.stream()
                .map(seedRelativeUrl -> StringUtils.removeStart(seedRelativeUrl, FORWARD_SLASH))
                .map(mainApplicationPagePath::concat)
                .map(relativeUrl -> UriUtils.buildNewUrl(mainApplicationPage, relativeUrl))
                .map(URI::toString)
                .forEach(pageUrl -> addSeed(controller, pageUrl));
    }

    private void addSeed(CrawlController controller, String pageUrl)
    {
        try
        {
            controller.addSeed(pageUrl);
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }

    @Override
    protected Set<String> fetchUrls(TableProperties properties)
    {
        if (!StringUtils.isEmpty(excludeExtensionsRegex))
        {
            LOGGER.warn(DEPRECATION_MESSAGE);
        }

        URI mainApplicationPage = getMainApplicationPageUri(properties);
        return crawledUrlsCache.getUnchecked(mainApplicationPage);
    }

    @Override
    protected URI parseUri(String uri)
    {
        return URI.create(uri);
    }

    public void setCrawlControllerFactory(ICrawlControllerFactory crawlControllerFactory)
    {
        this.crawlControllerFactory = crawlControllerFactory;
    }

    public void setSeedRelativeUrls(Set<String> seedRelativeUrls)
    {
        this.seedRelativeUrls = seedRelativeUrls;
    }

    public void setExcludeUrlsRegex(String excludeUrlsRegex)
    {
        this.excludeUrlsRegex = excludeUrlsRegex;
    }

    public void setExcludeExtensionsRegex(String excludeExtensionsRegex)
    {
        this.excludeExtensionsRegex = excludeExtensionsRegex;
    }
}
