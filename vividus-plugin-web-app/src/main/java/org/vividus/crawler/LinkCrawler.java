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

import java.net.URI;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.util.UriUtils;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;

public class LinkCrawler extends WebCrawler
{
    private static final Logger LOGGER = LoggerFactory.getLogger(LinkCrawler.class);

    private static final String[] EXCLUDED_EXTENSIONS = { ".css", ".gif", ".gz", ".ico", ".jpeg", ".jpg", ".js",
            ".mp3", ".mp4", ".pdf", ".png", ".zip" };

    private final LinkCrawlerData linkCrawlerData;

    public LinkCrawler(LinkCrawlerData linkCrawlerData)
    {
        this.linkCrawlerData = linkCrawlerData;
    }

    @Override
    public boolean shouldVisit(Page referringPage, WebURL url)
    {
        return isAllowedUrl(referringPage, url);
    }

    @Override
    public void visit(Page page)
    {
        String url = page.getWebURL().getURL();
        LOGGER.info("Crawled: {}", url);

        if (page.getParseData() instanceof HtmlParseData)
        {
            Set<String> absoluteUrls = linkCrawlerData.getAbsoluteUrls();
            page.getParseData().getOutgoingUrls()
                    .stream()
                    .filter(u -> isAllowedUrl(page, u))
                    .map(WebURL::getURL)
                    .map(UriUtils::createUri)
                    .map(UriUtils::removeQuery)
                    .map(URI::toString)
                    .forEach(absoluteUrls::add);
        }
    }

    private static boolean isAllowedUrl(Page referringPage, WebURL url)
    {
        return isNotFile(url) && isFromTheSameSite(referringPage.getWebURL().getURL(), url.getURL());
    }

    private static boolean isNotFile(WebURL url)
    {
        return !StringUtils.endsWithAny(url.getPath().toLowerCase(), EXCLUDED_EXTENSIONS);
    }

    private static boolean isFromTheSameSite(String pageUrl, String urlToCheck)
    {
        return UriUtils.isFromTheSameSite(URI.create(pageUrl), URI.create(urlToCheck));
    }
}
