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

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jbehave.core.model.ExamplesTable.TableProperties;
import org.jbehave.core.model.TableParsers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.http.HttpRedirectsProvider;
import org.vividus.transformer.ExtendedTableTransformer;
import org.vividus.ui.web.configuration.WebApplicationConfiguration;
import org.vividus.util.ExamplesTableProcessor;
import org.vividus.util.UriUtils;

public abstract class AbstractFetchingUrlsTableTransformer implements ExtendedTableTransformer
{
    private static final String COLUMN_KEY = "column";
    private static final String REDIRECT_JOINER = " -> ";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private WebApplicationConfiguration webApplicationConfiguration;
    private HttpRedirectsProvider httpRedirectsProvider;
    private boolean filterRedirects;
    private URI mainPageUrl;
    @Deprecated(forRemoval = true, since = "0.6.6")
    private String mainPageUrlProperty;

    @Override
    public String transform(String tableAsString, TableParsers tableParsers, TableProperties properties)
    {
        checkTableEmptiness(tableAsString);
        Set<String> urls = fetchUrls(properties).stream()
                .map(this::parseUri)
                .map(URI::getRawPath)
                .collect(Collectors.toSet());
        return build(urls, properties);
    }

    protected abstract Set<String> fetchUrls(TableProperties properties);

    protected abstract URI parseUri(String uri);

    protected Set<String> filterResults(Stream<String> urls)
    {
        Set<String> uniqueUrls = urls.collect(Collectors.toCollection(LinkedHashSet::new));
        if (filterRedirects)
        {
            Set<String> result = new LinkedHashSet<>();
            Map<String, List<String>> redirectChains = new HashMap<>();

            for (String url : uniqueUrls)
            {
                List<String> redirects = getRedirects(url);
                if (redirects.isEmpty())
                {
                    result.add(url);
                    continue;
                }

                String targetLocation = redirects.get(redirects.size() - 1);
                if (!uniqueUrls.contains(targetLocation))
                {
                    result.add(url);
                }

                redirectChains.put(url, redirects);
            }

            if (!redirectChains.isEmpty())
            {
                logger.atInfo().addArgument(System.lineSeparator())
                               .addArgument(() -> redirectChains.entrySet().stream()
                                   .map(e -> e.getKey() + REDIRECT_JOINER + String.join(REDIRECT_JOINER, e.getValue()))
                                   .collect(Collectors.joining(System.lineSeparator())))
                               .log("Filtered redirects chains:{}{}");
            }

            return result;
        }

        return uniqueUrls;
    }

    private List<String> getRedirects(String urlAsString)
    {
        try
        {
            return httpRedirectsProvider.getRedirects(parseUri(urlAsString)).stream()
                    .map(URI::toString)
                    .toList();
        }
        catch (IOException e)
        {
            logger.warn("Exception during redirects receiving", e);
        }
        return List.of();
    }

    private String build(Set<String> urls, TableProperties properties)
    {
        String columnName = properties.getMandatoryNonBlankProperty(COLUMN_KEY, String.class);
        List<String> urlsList = new ArrayList<>(urls);
        return ExamplesTableProcessor
                .buildExamplesTableFromColumns(List.of(columnName), List.of(urlsList), properties);
    }

    protected URI getMainApplicationPageUri(TableProperties properties)
    {
        String mainPageParam = "mainPageUrl";
        URI uri = Optional.ofNullable(properties.getProperties().getProperty(mainPageParam))
                          .map(UriUtils::createUri)
                          .orElse(mainPageUrl);

        if (uri == null)
        {
            uri = webApplicationConfiguration.getMainApplicationPageUrl();
            logger.atWarn().addArgument("web-application.main-page-url")
                           .addArgument(mainPageParam)
                           .addArgument(mainPageUrlProperty)
                           .log("The use of {} property for setting of main page for crawling is deprecated and will "
                                   + "be removed in VIVIDUS 0.7.0, please see use either {} transformer parameter or "
                                   + "{} property.");
        }

        return uri;
    }

    public void setWebApplicationConfiguration(WebApplicationConfiguration webApplicationConfiguration)
    {
        this.webApplicationConfiguration = webApplicationConfiguration;
    }

    public void setHttpRedirectsProvider(HttpRedirectsProvider httpRedirectsProvider)
    {
        this.httpRedirectsProvider = httpRedirectsProvider;
    }

    public void setFilterRedirects(boolean filterRedirects)
    {
        this.filterRedirects = filterRedirects;
    }

    public void setMainPageUrl(URI mainPageUrl)
    {
        this.mainPageUrl = mainPageUrl;
    }

    @Deprecated(forRemoval = true, since = "0.6.6")
    public void setMainPageUrlProperty(String mainPageUrlProperty)
    {
        this.mainPageUrlProperty = mainPageUrlProperty;
    }
}
