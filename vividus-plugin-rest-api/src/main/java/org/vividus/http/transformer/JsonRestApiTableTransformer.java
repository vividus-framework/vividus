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

package org.vividus.http.transformer;

import static java.util.Map.entry;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;

import com.google.common.base.Splitter;

import org.jbehave.core.model.ExamplesTable.TableProperties;
import org.jbehave.core.model.TableParsers;
import org.vividus.context.VariableContext;
import org.vividus.http.client.IHttpClient;
import org.vividus.transformer.ExtendedTableTransformer;
import org.vividus.util.ExamplesTableProcessor;
import org.vividus.util.UriUtils;
import org.vividus.util.json.JsonPathUtils;

public class JsonRestApiTableTransformer implements ExtendedTableTransformer
{
    @Inject private VariableContext variableContext;
    private IHttpClient httpClient;

    @SuppressWarnings("unchecked")
    @Override
    public String transform(String tableAsString, TableParsers tableParsers, TableProperties properties)
    {
        checkTableEmptiness(tableAsString);

        String columns = properties.getMandatoryNonBlankProperty("columns", String.class);

        String jsonData = processCompetingMandatoryProperties(properties,
                entry("url", this::getJsonByUrl),
                entry("variable", variableContext::getVariable));

        Map<String, String> columnsPerJsonPaths = Splitter.on(';').withKeyValueSeparator(Splitter.on('=').limit(2))
                .split(columns);

        List<List<String>> values = JsonPathUtils.getData(jsonData, columnsPerJsonPaths.values()).stream().map(e ->
        {
            List<Object> columnValues = e instanceof List ? (List<Object>) e : Collections.singletonList(e);
            return columnValues.stream().map(String::valueOf).collect(Collectors.toList());
        }).collect(Collectors.toList());

        return ExamplesTableProcessor.buildExamplesTableFromColumns(columnsPerJsonPaths.keySet(), values, properties);
    }

    private String getJsonByUrl(String url)
    {
        try
        {
            return httpClient.doHttpGet(UriUtils.createUri(url)).getResponseBodyAsString();
        }
        catch (IOException e)
        {
            throw new UncheckedIOException(e);
        }
    }

    public void setHttpClient(IHttpClient httpClient)
    {
        this.httpClient = httpClient;
    }
}
