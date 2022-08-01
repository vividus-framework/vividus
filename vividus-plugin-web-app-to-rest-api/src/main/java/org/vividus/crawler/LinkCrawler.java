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

import static org.apache.commons.io.FilenameUtils.getExtension;

import java.net.URI;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.util.UriUtils;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;

public class LinkCrawler extends WebCrawler
{
    private static final Logger LINK_CRAWLER_LOGGER = LoggerFactory.getLogger(LinkCrawler.class);

    private final LinkCrawlerData linkCrawlerData;

    private final Optional<Pattern> excludeExtensionsPattern;

    private final Set<String> urlsWithInvalidRedirects = new HashSet<>();

    public LinkCrawler(LinkCrawlerData linkCrawlerData, String excludeExtensionsRegex)
    {
        this.linkCrawlerData = linkCrawlerData;
        this.excludeExtensionsPattern = Optional.ofNullable(excludeExtensionsRegex).map(Pattern::compile);
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
        LINK_CRAWLER_LOGGER.info("Crawled: {}", url);

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

    private boolean isAllowedUrl(Page referringPage, WebURL url)
    {
        return isNotFileWithForbiddenExtension(url)
                && isFromTheSameSite(referringPage.getWebURL().getURL(), url.getURL());
    }

    private boolean isNotFileWithForbiddenExtension(WebURL url)
    {
        boolean excluded = isExcluded(url.getPath());
        if (excluded)
        {
            LINK_CRAWLER_LOGGER.info("Skip crawling for URL {}", url.getURL());
        }
        return !excluded;
    }

    @Override
    protected void onRedirectedStatusCode(Page page)
    {
        String redirectedPage = page.getRedirectedToUrl();
        URI redirectedUri = UriUtils.createUri(redirectedPage);
        redirectedUri = UriUtils.removeQuery(redirectedUri);

        if (isExcluded(redirectedUri.getPath()))
        {
            String url = page.getWebURL().getURL();
            urlsWithInvalidRedirects.add(url);
            LINK_CRAWLER_LOGGER.info(
                    "URL {} redirects to the URL with a forbidden extension ({}) and will be excluded from the result.",
                    url, redirectedPage);
        }
    }

    @Override
    public void onBeforeExit()
    {
        linkCrawlerData.getAbsoluteUrls().removeAll(urlsWithInvalidRedirects);
    }

    private boolean isExcluded(String path)
    {
        String fileExtensionFromPath = getExtension(path).toLowerCase();

        return !fileExtensionFromPath.isEmpty()
                && excludeExtensionsPattern.map(p -> p.matcher(fileExtensionFromPath))
                .map(Matcher::matches)
                .orElse(false);
    }

    private static boolean isFromTheSameSite(String pageUrl, String urlToCheck)
    {
        return UriUtils.isFromTheSameSite(URI.create(pageUrl), URI.create(urlToCheck));
    }
}
