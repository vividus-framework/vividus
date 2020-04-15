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
import java.util.Set;
import java.util.function.Supplier;

import com.google.common.base.Suppliers;

import org.apache.commons.lang3.StringUtils;
import org.jbehave.core.model.ExamplesTableProperties;
import org.vividus.crawler.ICrawlControllerFactory;
import org.vividus.crawler.LinkCrawlerData;
import org.vividus.crawler.LinkCrawlerFactory;
import org.vividus.util.UriUtils;

import edu.uci.ics.crawler4j.crawler.CrawlController;

public class HeadlessCrawlerTableTransformer extends AbstractFetchingUrlsTableTransformer
{
    private static final String FORWARD_SLASH = "/";
    private static final int NUMBER_OF_CRAWLERS = 50;

    private ICrawlControllerFactory crawlControllerFactory;

    private Set<String> seedRelativeUrls;

    private final Supplier<Set<String>> urlsProvider = Suppliers.memoize(() ->
    {
        URI mainApplicationPage = getMainApplicationPageUri();
        CrawlController controller = crawlControllerFactory.createCrawlController(mainApplicationPage);

        addSeeds(mainApplicationPage, controller);

        LinkCrawlerData linkCrawlerData = new LinkCrawlerData();
        controller.start(new LinkCrawlerFactory(linkCrawlerData), NUMBER_OF_CRAWLERS);
        Set<String> absoluteUrls = linkCrawlerData.getAbsoluteUrls();
        return filterResults(absoluteUrls.stream());
    });

    private void addSeeds(URI mainApplicationPage, CrawlController controller)
    {
        controller.addSeed(mainApplicationPage.toString());
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
                .forEach(controller::addSeed);
    }

    @Override
    protected Set<String> fetchUrls(ExamplesTableProperties properties)
    {
        return urlsProvider.get();
    }

    public void setCrawlControllerFactory(ICrawlControllerFactory crawlControllerFactory)
    {
        this.crawlControllerFactory = crawlControllerFactory;
    }

    public void setSeedRelativeUrls(Set<String> seedRelativeUrls)
    {
        this.seedRelativeUrls = seedRelativeUrls;
    }
}
