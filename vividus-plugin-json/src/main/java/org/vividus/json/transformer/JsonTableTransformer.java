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

package org.vividus.json.transformer;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.base.Splitter;

import org.jbehave.core.model.ExamplesTable.TableProperties;
import org.jbehave.core.model.TableParsers;
import org.vividus.context.VariableContext;
import org.vividus.transformer.ExtendedTableTransformer;
import org.vividus.util.ExamplesTableProcessor;
import org.vividus.util.json.JsonPathUtils;

public class JsonTableTransformer implements ExtendedTableTransformer
{
    private final VariableContext variableContext;

    public JsonTableTransformer(VariableContext variableContext)
    {
        this.variableContext = variableContext;
    }

    @SuppressWarnings("unchecked")
    @Override
    public String transform(String tableAsString, TableParsers tableParsers, TableProperties properties)
    {
        checkTableEmptiness(tableAsString);

        String variableName = properties.getMandatoryNonBlankProperty("variableName", String.class);
        String columns = properties.getMandatoryNonBlankProperty("columns", String.class);

        String jsonData = variableContext.getVariable(variableName);

        Map<String, String> columnsPerJsonPaths = Splitter.on(';').withKeyValueSeparator(Splitter.on('=').limit(2))
                .split(columns);

        List<List<String>> values = JsonPathUtils.getData(jsonData, columnsPerJsonPaths.values()).stream().map(e ->
        {
            List<Object> columnValues = e instanceof List ? (List<Object>) e : Collections.singletonList(e);
            return columnValues.stream().map(String::valueOf).collect(Collectors.toList());
        }).collect(Collectors.toList());

        return ExamplesTableProcessor.buildExamplesTableFromColumns(columnsPerJsonPaths.keySet(), values, properties);
    }
}
