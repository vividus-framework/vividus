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

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class Variables
{
    private Map<VariableScope, Map<String, Object>> variablesPerScope = new EnumMap<>(VariableScope.class);

    public Variables()
    {
        Stream.of(VariableScope.values()).forEach(scope -> variablesPerScope.put(scope, new HashMap<>()));
    }

    public Map<String, Object> getVariables(VariableScope variableScope)
    {
        return variablesPerScope.get(variableScope);
    }

    public void setVariablesPerScope(Map<VariableScope, Map<String, Object>> variablesPerScope)
    {
        this.variablesPerScope = variablesPerScope;
    }
}
