/*
 * Copyright 2019-2022 the original author or authors.
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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;

import org.apache.commons.io.FileUtils;
import org.vividus.util.UriUtils;
import org.vividus.util.UriUtils.UserInfo;

import crawlercommons.filters.basic.BasicURLNormalizer;
import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.crawler.authentication.BasicAuthInfo;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.frontier.SleepycatFrontierConfiguration;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;
import edu.uci.ics.crawler4j.url.SleepycatWebURLFactory;

public class CrawlControllerFactory implements ICrawlControllerFactory
{
    private static final int SOCKET_TIMEOUT = 40_000;

    private String crawlStorageFolder;

    @Override
    public CrawlController createCrawlController(URI mainApplicationPage)
    {
        try
        {
            CrawlConfig crawlConfig = createCrawlConfig(mainApplicationPage);

            BasicURLNormalizer normalizer = BasicURLNormalizer.newBuilder()
                                                          .idnNormalization(BasicURLNormalizer.IdnNormalization.NONE)
                                                          .build();

            RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
            robotstxtConfig.setEnabled(false);
            PageFetcher pageFetcher = new PageFetcher(crawlConfig, normalizer);
            RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher,
                    new SleepycatWebURLFactory());

            return new CrawlController(crawlConfig, normalizer, pageFetcher, robotstxtServer,
                    new SleepycatFrontierConfiguration(crawlConfig));
        }
        catch (Exception e)
        {
            throw new IllegalArgumentException(e);
        }
    }

    private CrawlConfig createCrawlConfig(URI mainApplicationPage) throws IOException
    {
        CrawlConfig crawlConfig = new CrawlConfig();

        /**
         * The crawler4j requires the crawl storage folder to exist, so it should be updated to create
         * folders at any depth
         */
        File crawlStorage = new File(crawlStorageFolder);
        FileUtils.forceMkdir(crawlStorage);

        crawlConfig.setCrawlStorageFolder(crawlStorageFolder);
        crawlConfig.setPolitenessDelay(0);
        crawlConfig.setSocketTimeout(SOCKET_TIMEOUT);
        crawlConfig.setRespectNoFollow(false);
        crawlConfig.setRespectNoIndex(false);

        UserInfo userInfo = UriUtils.getUserInfo(mainApplicationPage);
        if (userInfo != null)
        {
            try
            {
                BasicAuthInfo authInfo = new BasicAuthInfo(userInfo.getUser(), userInfo.getPassword(),
                        UriUtils.removeUserInfo(mainApplicationPage).toString());
                crawlConfig.addAuthInfo(authInfo);
            }
            catch (MalformedURLException e)
            {
                throw new IllegalArgumentException(e);
            }
        }
        return crawlConfig;
    }

    public void setCrawlStorageFolder(String crawlStorageFolder)
    {
        this.crawlStorageFolder = crawlStorageFolder;
    }
}
