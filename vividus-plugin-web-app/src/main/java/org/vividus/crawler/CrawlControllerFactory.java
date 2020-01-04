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

package org.vividus.crawler;

import java.net.MalformedURLException;
import java.net.URI;

import org.vividus.util.UriUtils;
import org.vividus.util.UriUtils.UserInfo;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.crawler.authentication.BasicAuthInfo;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;

public class CrawlControllerFactory implements ICrawlControllerFactory
{
    private static final int SOCKET_TIMEOUT = 40_000;

    private String crawlStorageFolder;

    @Override
    public CrawlController createCrawlController(URI mainApplicationPage)
    {
        CrawlConfig crawlConfig = createCrawlConfig(mainApplicationPage);

        RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
        robotstxtConfig.setEnabled(false);
        PageFetcher pageFetcher = new PageFetcher(crawlConfig);
        RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);

        try
        {
            return new CrawlController(crawlConfig, pageFetcher, robotstxtServer);
        }
        catch (Exception e)
        {
            throw new IllegalArgumentException(e);
        }
    }

    private CrawlConfig createCrawlConfig(URI mainApplicationPage)
    {
        CrawlConfig crawlConfig = new CrawlConfig();
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
