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

package org.vividus.json.transformer;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.common.base.Splitter;

import org.apache.commons.lang3.function.FailableFunction;
import org.jbehave.core.model.ExamplesTable.TableProperties;
import org.jbehave.core.model.TableParsers;
import org.vividus.context.VariableContext;
import org.vividus.transformer.ExtendedTableTransformer;
import org.vividus.util.ExamplesTableProcessor;
import org.vividus.util.ResourceUtils;
import org.vividus.util.json.JsonPathUtils;

public class JsonTableTransformer implements ExtendedTableTransformer
{
    private static final String VARIABLE_NAME_PROPERTY_KEY = "variableName";
    private static final String PATH_PROPERTY_KEY = "path";

    private final VariableContext variableContext;

    public JsonTableTransformer(VariableContext variableContext)
    {
        this.variableContext = variableContext;
    }

    @SuppressWarnings("unchecked")
    @Override
    public String transform(String tableAsString, TableParsers tableParsers, TableProperties tableProperties)
    {
        checkTableEmptiness(tableAsString);

        Map.Entry<String, String> entry = processCompetingMandatoryProperties(tableProperties.getProperties(),
                VARIABLE_NAME_PROPERTY_KEY, PATH_PROPERTY_KEY);
        String sourceKey = entry.getKey();
        String sourceValue = entry.getValue();
        FailableFunction<Collection<String>, List<?>, IOException> jsonDataMapper = VARIABLE_NAME_PROPERTY_KEY.equals(
                sourceKey)
                ? paths -> JsonPathUtils.getData((String) variableContext.getVariable(sourceValue), paths)
                : paths -> JsonPathUtils.getData(ResourceUtils.loadResourceOrFileAsStream(sourceValue), paths);

        String columns = tableProperties.getMandatoryNonBlankProperty("columns", String.class);
        Map<String, String> columnsPerJsonPaths = Splitter.on(';').withKeyValueSeparator(Splitter.on('=').limit(2))
                .split(columns);

        try
        {
            List<List<String>> values = jsonDataMapper.apply(columnsPerJsonPaths.values()).stream().map(e ->
            {
                List<Object> columnValues = e instanceof List ? (List<Object>) e : Collections.singletonList(e);
                return columnValues.stream().map(String::valueOf).toList();
            }).toList();
            return ExamplesTableProcessor.buildExamplesTableFromColumns(columnsPerJsonPaths.keySet(), values,
                    tableProperties);
        }
        catch (IOException e)
        {
            throw new UncheckedIOException(e);
        }
    }
}
