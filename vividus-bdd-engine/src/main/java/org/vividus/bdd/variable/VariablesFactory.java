/*
 * Copyright 2019 the original author or authors.
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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.vividus.util.property.IPropertyParser;

public class VariablesFactory implements IVariablesFactory
{
    private static final String VARIABLES_PROPERTY_FAMILY = "bdd.variables.global";
    private IPropertyParser propertyParser;

    private Map<String, String> globalVariables;
    private final Map<String, Object> nextBatchesVariables = new ConcurrentHashMap<>();

    public void init()
    {
        globalVariables = propertyParser.getPropertyValuesByFamily(VARIABLES_PROPERTY_FAMILY);
    }

    @Override
    public Variables createVariables()
    {
        Variables variables = new Variables();
        variables.getVariables(VariableScope.GLOBAL).putAll(globalVariables);
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

    public void setPropertyParser(IPropertyParser propertyParser)
    {
        this.propertyParser = propertyParser;
    }
}
