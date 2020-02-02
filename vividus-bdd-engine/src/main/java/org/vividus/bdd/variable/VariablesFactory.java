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

package org.vividus.bdd.variable;

import static java.util.Map.entry;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.vividus.bdd.context.IBddRunContext;
import org.vividus.util.property.IPropertyMapper;
import org.vividus.util.property.IPropertyParser;

public class VariablesFactory implements IVariablesFactory
{
    private static final String BDD_VARIABLES_PREFIX = "bdd.variables.";
    private static final String BATCH_PREFIX = "batch-";
    private static final String VARIABLES_PROPERTY_FAMILY = BDD_VARIABLES_PREFIX + "global";

    private final IPropertyParser propertyParser;
    private final IPropertyMapper propertyMapper;
    private final IBddRunContext bddRunContext;

    private Map<String, String> globalVariables;
    private Map<String, Map<String, String>> batchVariables;
    private final Map<String, Object> nextBatchesVariables = new ConcurrentHashMap<>();

    public VariablesFactory(IPropertyParser propertyParser, IPropertyMapper propertyMapper,
            IBddRunContext bddRunContext)
    {
        this.propertyParser = propertyParser;
        this.propertyMapper = propertyMapper;
        this.bddRunContext = bddRunContext;
    }

    public void init() throws IOException
    {
        globalVariables = propertyParser.getPropertyValuesByFamily(VARIABLES_PROPERTY_FAMILY);
        batchVariables = propertyMapper.readValues(BDD_VARIABLES_PREFIX + BATCH_PREFIX, Map.class).entrySet()
            .stream()
            .map(e -> entry(BATCH_PREFIX + e.getKey(), e.getValue()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Override
    public Variables createVariables()
    {
        Variables variables = new Variables();
        Map<String, Object> globalScoped = variables.getVariables(VariableScope.GLOBAL);
        globalScoped.putAll(globalVariables);
        globalScoped.putAll(batchVariables.getOrDefault(bddRunContext.getRunningBatchKey(), Map.of()));
        variables.getVariables(VariableScope.NEXT_BATCHES).putAll(nextBatchesVariables);
        return variables;
    }

    @Override
    public Map<String, String> getGlobalVariables()
    {
        return globalVariables;
    }

    @Override
    public void addNextBatchesVariable(String variableKey, Object variableValue)
    {
        nextBatchesVariables.put(variableKey, variableValue);
    }
}
