/*
 * Copyright 2019-2023 the original author or authors.
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
import java.util.ArrayList;
import java.util.List;
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

public abstract class AbstractFetchingUrlsTableTransformer implements ExtendedTableTransformer
{
    private static final String COLUMN_KEY = "column";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private WebApplicationConfiguration webApplicationConfiguration;
    private HttpRedirectsProvider httpRedirectsProvider;
    private boolean filterRedirects;

    @Override
    public String transform(String tableAsString, TableParsers tableParsers, TableProperties properties)
    {
        checkTableEmptiness(tableAsString);
        Set<String> urls = fetchUrls(properties).stream()
                .map(URI::create)
                .map(URI::getRawPath)
                .collect(Collectors.toSet());
        return build(urls, properties);
    }

    protected abstract Set<String> fetchUrls(TableProperties properties);

    protected Set<String> filterResults(Stream<String> urls)
    {
        Stream<String> results = urls;
        if (filterRedirects)
        {
            Set<String> uniqueUrls = urls.collect(Collectors.toSet());
            results = uniqueUrls.stream().filter(url -> isNotExistingRedirect(url, uniqueUrls));
        }
        return results.collect(Collectors.toSet());
    }

    private boolean isNotExistingRedirect(String urlToCheck, Set<String> allUrls)
    {
        return getLastRedirect(urlToCheck)
                .map(URI::toString)
                .map(lastRedirect -> !allUrls.contains(lastRedirect))
                .orElse(true);
    }

    private Optional<URI> getLastRedirect(String urlAsString)
    {
        try
        {
            List<URI> redirects = httpRedirectsProvider.getRedirects(URI.create(urlAsString));
            if (!redirects.isEmpty())
            {
                return Optional.of(redirects.get(redirects.size() - 1));
            }
        }
        catch (IllegalStateException e)
        {
            logger.warn("Exception during redirects receiving", e);
        }
        return Optional.empty();
    }

    private String build(Set<String> urls, TableProperties properties)
    {
        String columnName = properties.getMandatoryNonBlankProperty(COLUMN_KEY, String.class);
        List<String> urlsList = new ArrayList<>(urls);
        return ExamplesTableProcessor
                .buildExamplesTableFromColumns(List.of(columnName), List.of(urlsList), properties);
    }

    public void setWebApplicationConfiguration(WebApplicationConfiguration webApplicationConfiguration)
    {
        this.webApplicationConfiguration = webApplicationConfiguration;
    }

    public void setHttpRedirectsProvider(HttpRedirectsProvider httpRedirectsProvider)
    {
        this.httpRedirectsProvider = httpRedirectsProvider;
    }

    protected URI getMainApplicationPageUri()
    {
        return webApplicationConfiguration.getMainApplicationPageUrl();
    }

    public void setFilterRedirects(boolean filterRedirects)
    {
        this.filterRedirects = filterRedirects;
    }
}
