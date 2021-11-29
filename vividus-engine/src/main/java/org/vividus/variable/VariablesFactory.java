/*
 * Copyright 2019-2021 the original author or authors.
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

package org.vividus.variable;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.vividus.context.RunContext;
import org.vividus.util.property.IPropertyMapper;
import org.vividus.util.property.IPropertyParser;

public class VariablesFactory implements IVariablesFactory
{
    private static final String VARIABLES_PREFIX = "bdd.variables.";
    private static final String BATCH_PREFIX = "batch-";
    private static final String VARIABLES_PROPERTY_PREFIX = VARIABLES_PREFIX + "global.";

    private final IPropertyParser propertyParser;
    private final IPropertyMapper propertyMapper;
    private final RunContext runContext;

    private Map<String, String> globalVariables;
    private Map<String, Map<String, String>> batchVariables;
    private final Map<String, Object> nextBatchesVariables = new ConcurrentHashMap<>();

    public VariablesFactory(IPropertyParser propertyParser, IPropertyMapper propertyMapper,
            RunContext runContext)
    {
        this.propertyParser = propertyParser;
        this.propertyMapper = propertyMapper;
        this.runContext = runContext;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void init() throws IOException
    {
        globalVariables = propertyParser.getPropertyValuesByPrefix(VARIABLES_PROPERTY_PREFIX);
        batchVariables = propertyMapper.readValues(VARIABLES_PREFIX + BATCH_PREFIX, BATCH_PREFIX::concat, Map.class)
                .getData().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Override
    public Variables createVariables()
    {
        Map<String, Object> merged = new HashMap<>(globalVariables);
        Optional.ofNullable(batchVariables.get(runContext.getRunningBatchKey())).ifPresent(merged::putAll);
        merged.putAll(nextBatchesVariables);
        return new Variables(merged);
    }

    @Override
    public void addNextBatchesVariable(String variableKey, Object variableValue)
    {
        nextBatchesVariables.put(variableKey, variableValue);
    }
}
