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

package org.vividus.bdd.context;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.bdd.variable.IVariablesFactory;
import org.vividus.bdd.variable.VariableScope;
import org.vividus.bdd.variable.Variables;
import org.vividus.testcontext.TestContext;

public class BddVariableContext implements IBddVariableContext
{
    private static final int VARIABLE_NAME_GROUP = 1;
    private static final int LIST_INDEX_GROUP = 2;
    private static final int MAP_KEY_GROUP = 3;
    private static final Pattern COMPLEX_VARIABLE_PATTERN = Pattern.compile(
            "([^\\[\\]\\.:]+):?(?:\\[(\\d+)\\])?:?(?:\\.([^:]+))?:?");
    private static final char COLON = ':';

    private static final Logger LOGGER = LoggerFactory.getLogger(BddVariableContext.class);
    private static final Class<Variables> VARIABLES_KEY = Variables.class;

    private TestContext testContext;
    private IVariablesFactory variablesFactory;

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getVariable(String variableKey)
    {
        Variables variables = getVariables();
        return (T) Stream.of(VariableScope.STEP, VariableScope.SCENARIO, VariableScope.STORY,
                VariableScope.NEXT_BATCHES, VariableScope.GLOBAL)
                .map(scope -> getVariable(variables.getVariables(scope), variableKey))
                .filter(Objects::nonNull)
                .findFirst()
                .or(() -> getDefault(variableKey))
                .orElseGet(() -> getSystem(variableKey));
    }

    @Override
    public void putVariable(Set<VariableScope> variableScopes, String variableKey, Object variableValue)
    {
        variableScopes.forEach(s -> putVariable(s, variableKey, variableValue));
    }

    private Optional<String> getDefault(String key)
    {
        int colonIndex = key.lastIndexOf(COLON);
        return colonIndex >= 0 ? Optional.of(key.substring(colonIndex + 1)) : Optional.empty();
    }

    private Object getSystem(String variableKey)
    {
        return variableKey.isBlank() ? null : System.getProperty(variableKey);
    }

    @Override
    public void putVariable(VariableScope variableScope, String variableKey, Object variableValue)
    {
        if (variableScope == VariableScope.GLOBAL)
        {
            throw new IllegalArgumentException("Setting of GLOBAL variables is forbidden");
        }
        LOGGER.info("Saving a value '{}' into the '{}' variable '{}'", variableValue, variableScope, variableKey);
        if (variableScope == VariableScope.NEXT_BATCHES)
        {
            variablesFactory.addNextBatchesVariable(variableKey, variableValue);
        }
        else
        {
            getVariables().getVariables(variableScope).put(variableKey, variableValue);
        }
    }

    @Override
    public void initVariables()
    {
        getVariables();
    }

    @Override
    public void clearVariables(VariableScope variableScope)
    {
        getVariables().getVariables(variableScope).clear();
    }

    @Override
    public void clearVariables()
    {
        testContext.remove(VARIABLES_KEY);
    }

    private Object getVariable(Map<String, Object> variables, String key)
    {
        return Optional.ofNullable(variables.get(key))
                       .or(() -> resolveAsComplexType(variables, key))
                       .orElse(null);
    }

    private Optional<Object> resolveAsComplexType(Map<String, Object> variables, String key)
    {
        Matcher variableMatcher = COMPLEX_VARIABLE_PATTERN.matcher(key);
        if (!variableMatcher.find())
        {
            return Optional.empty();
        }
        String variableKey = variableMatcher.group(VARIABLE_NAME_GROUP);
        return Optional.ofNullable(variables.get(variableKey))
                       .map(v -> resolveAsListItem(variableMatcher, v))
                       .map(v -> resolveAsMapItem(variableMatcher, v));
    }

    @SuppressWarnings("unchecked")
    private Object resolveAsMapItem(Matcher variableMatcher, Object variable)
    {
        String mapKey = variableMatcher.group(MAP_KEY_GROUP);
        if (mapKey != null && variable instanceof Map)
        {
            return ((Map<String, ?>) variable).get(mapKey);
        }
        return variable;
    }

    private Object resolveAsListItem(Matcher variableMatcher, Object variable)
    {
        String listIndex = variableMatcher.group(LIST_INDEX_GROUP);
        if (listIndex != null && variable instanceof List)
        {
            List<?> listVariable = (List<?>) variable;
            int elementIndex = Integer.parseInt(listIndex);
            return elementIndex < listVariable.size() ? listVariable.get(elementIndex) : null;
        }
        return variable;
    }

    private Variables getVariables()
    {
        return testContext.get(VARIABLES_KEY, variablesFactory::createVariables);
    }

    public void setTestContext(TestContext testContext)
    {
        this.testContext = testContext;
    }

    public void setVariablesFactory(IVariablesFactory variablesFactory)
    {
        this.variablesFactory = variablesFactory;
    }
}
