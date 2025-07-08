/*
 * Copyright 2019-2024 the original author or authors.
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

package org.vividus.html.transfromer;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.substringAfterLast;
import static org.apache.commons.lang3.StringUtils.substringBeforeLast;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.apache.commons.lang3.function.FailableSupplier;
import org.jbehave.core.model.ExamplesTable.TableProperties;
import org.jbehave.core.model.TableParsers;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.context.VariableContext;
import org.vividus.transformer.ExtendedTableTransformer;
import org.vividus.util.ExamplesTableProcessor;
import org.vividus.util.ResourceUtils;

public class HtmlDocumentTableTransformer implements ExtendedTableTransformer
{
    private static final Logger LOGGER = LoggerFactory.getLogger(HtmlDocumentTableTransformer.class);

    private static final String SLASH = "/";
    private static final String PAGE_URL_PROPERTY_KEY = "pageUrl";
    private static final String VARIABLE_NAME_PROPERTY_KEY = "variableName";
    private static final String PATH_PROPERTY_KEY = "path";

    private final Optional<HttpConfiguration> httpConfiguration;
    private final VariableContext variableContext;

    public HtmlDocumentTableTransformer(Optional<HttpConfiguration> httpConfiguration, VariableContext variableContext)
    {
        this.httpConfiguration = httpConfiguration;
        this.variableContext = variableContext;
    }

    @Override
    public String transform(String table, TableParsers parsers, TableProperties tableProperties)
    {
        String baseUri = tableProperties.getProperties().getProperty("baseUri", "");
        Map.Entry<String, String> entry = processCompetingMandatoryProperties(tableProperties.getProperties(),
                PAGE_URL_PROPERTY_KEY, VARIABLE_NAME_PROPERTY_KEY, PATH_PROPERTY_KEY);
        String sourceKey = entry.getKey();
        String sourceValue = entry.getValue();
        FailableSupplier<Document, IOException> documentSuppler;
        if (VARIABLE_NAME_PROPERTY_KEY.equals(sourceKey))
        {
            documentSuppler = () -> Jsoup.parse((String) variableContext.getVariable(sourceValue), baseUri);
        }
        else if (PAGE_URL_PROPERTY_KEY.equals(sourceKey))
        {
            LOGGER.atWarn().log("The 'pageUrl' transformer parameter is deprecated and will be removed VIVIDUS 0.7.0, "
                    + "please use 'variableName' parameter instead.");
            documentSuppler = () -> createDocument(sourceValue);
        }
        else
        {
            documentSuppler = () -> Jsoup.parse(ResourceUtils.loadResourceOrFileAsStream(sourceValue),
                    StandardCharsets.UTF_8.name(), baseUri);
        }

        String column = tableProperties.getMandatoryNonBlankProperty("column", String.class);

        String xpathSelector = tableProperties.getMandatoryNonBlankProperty("xpathSelector", String.class);

        String lastSegment = substringAfterLast(xpathSelector, SLASH);
        Function<Element, String> getter;
        if ("text()".equals(lastSegment))
        {
            getter = Element::text;
            xpathSelector = substringBeforeLast(xpathSelector, SLASH);
        }
        else if (lastSegment.startsWith("@"))
        {
            String attribute = lastSegment.substring(1);
            getter = e -> e.attr(attribute);
            xpathSelector = substringBeforeLast(xpathSelector, SLASH);
        }
        else
        {
            getter = Element::outerHtml;
        }

        try
        {
            return documentSuppler.get().selectXpath(xpathSelector)
                                    .stream()
                                    .map(getter)
                                    .collect(collectingAndThen(toList(), attrs -> ExamplesTableProcessor
                                            .buildExamplesTableFromColumns(List.of(column), List.of(attrs),
                                                    tableProperties)));
        }
        catch (IOException e)
        {
            throw new UncheckedIOException(e);
        }
    }

    private Document createDocument(String pageUrl) throws IOException
    {
        Connection connection = Jsoup.connect(pageUrl);
        httpConfiguration.ifPresent(cfg -> connection.headers(cfg.getHeaders()));
        return connection.get();
    }

    public static final class HttpConfiguration
    {
        private Map<String, String> headers;

        public Map<String, String> getHeaders()
        {
            return headers;
        }

        public void setHeaders(Map<String, String> headers)
        {
            this.headers = headers;
        }
    }
}
