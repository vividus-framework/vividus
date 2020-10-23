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

import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class Variables
{
    private static final int VARIABLE_NAME_GROUP = 1;
    private static final int LIST_INDEX_GROUP = 2;
    private static final int MAP_KEY_GROUP = 3;
    private static final Pattern COMPOUND_VARIABLE_PATTERN = Pattern.compile(
            "([^\\[\\].:]+):?(?:\\[(\\d+)])?:?(?:\\.([^:]+))?:?");

    private final Map<String, Object> batchVariables;
    private final Map<String, Object> storyVariables;
    private final Map<String, Object> scenarioVariables;
    private final Deque<Map<String, Object>> stepVariables;

    public Variables(Map<String, Object> batchVariables)
    {
        this.batchVariables = batchVariables;
        storyVariables = new HashMap<>();
        scenarioVariables = new HashMap<>();
        stepVariables = new LinkedList<>();
    }

    public Object getVariable(String variableKey)
    {
        VariableKey key = new VariableKey(variableKey);
        return concatedVariables()
                .map(scopedVariables -> getVariable(scopedVariables, key))
                .flatMap(Optional::stream)
                .findFirst()
                .or(() -> key.defaultValue)
                .orElseGet(() -> getSystem(variableKey));
    }

    private Stream<Map<String, Object>> concatedVariables()
    {
        return Stream.concat(
                    StreamSupport.stream(stepVariables.spliterator(), false),
                    Stream.of(scenarioVariables, storyVariables, batchVariables));
    }

    public Map<String, Object> getVariables()
    {
        return concatedVariables().map(Map::entrySet)
                                  .flatMap(Set::stream)
                                  .collect(Collectors.toMap(Entry::getKey, Entry::getValue, (k1, k2) -> k2));
    }

    private Optional<Object> getVariable(Map<String, Object> variables, VariableKey variableKey)
    {
        return Optional.ofNullable(variables.get(variableKey.key))
                .or(() -> variableKey.hasDefaultValue()
                        ? Optional.ofNullable(variables.get(variableKey.name))
                        : Optional.empty()
                )
                .or(() -> resolveAsCompound(variables, variableKey.key));
    }

    private Optional<Object> resolveAsCompound(Map<String, Object> variables, String key)
    {
        Matcher variableMatcher = COMPOUND_VARIABLE_PATTERN.matcher(key);
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

    private Object getSystem(String variableKey)
    {
        return variableKey.isBlank() ? null : System.getProperty(variableKey);
    }

    public void putStepVariable(String variableKey, Object variableValue)
    {
        stepVariables.peek().put(variableKey, variableValue);
    }

    public void putScenarioVariable(String variableKey, Object variableValue)
    {
        scenarioVariables.put(variableKey, variableValue);
    }

    public void putStoryVariable(String variableKey, Object variableValue)
    {
        storyVariables.put(variableKey, variableValue);
    }

    public void initStepVariables()
    {
        stepVariables.push(new HashMap<>());
    }

    public void clearStepVariables()
    {
        // Need to check for emptiness because @BeforeScenario/@AfterScenario steps do not report 'beforeStep', but may
        // report 'failed', which will trigger  step variables clean up
        if (!stepVariables.isEmpty())
        {
            stepVariables.pop();
        }
    }

    public void clearScenarioVariables()
    {
        scenarioVariables.clear();
    }

    public void clearStoryVariables()
    {
        storyVariables.clear();
    }

    private static final class VariableKey
    {
        private static final char COLON = ':';

        private final String key;
        private final String name;
        private final Optional<String> defaultValue;

        private VariableKey(String key)
        {
            this.key = key;

            int colonIndex = key.lastIndexOf(COLON);
            if (colonIndex >= 0)
            {
                name = key.substring(0, colonIndex);
                defaultValue = Optional.of(key.substring(colonIndex + 1));
            }
            else
            {
                name = key;
                defaultValue = Optional.empty();
            }
        }

        private boolean hasDefaultValue()
        {
            return defaultValue.isPresent();
        }
    }
}
