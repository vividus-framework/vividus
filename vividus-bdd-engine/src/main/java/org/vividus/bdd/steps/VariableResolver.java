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

package org.vividus.bdd.steps;

import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.vividus.bdd.context.IBddVariableContext;
import org.vividus.bdd.variable.DynamicVariable;

public class VariableResolver
{
    private static final String VARIABLE_START_MARKER = "${";

    private final IBddVariableContext bddVariableContext;
    private final Map<String, DynamicVariable> dynamicVariables;

    public VariableResolver(IBddVariableContext bddVariableContext, Map<String, DynamicVariable> dynamicVariables)
    {
        this.bddVariableContext = bddVariableContext;
        this.dynamicVariables = dynamicVariables;
    }

    public Object resolve(final String value)
    {
        if (value == null)
        {
            return null;
        }

        return resolveVariables(0, value);
    }

    private Object resolveVariables(int scanStartPosition, String value)
    {
        int start = -1;
        int end = -1;
        int level = 0;

        for (int i = scanStartPosition; i < value.length(); i++)
        {
            switch (value.charAt(i))
            {
                case '$':
                    if (i + 1 < value.length() && value.charAt(i + 1) == '{')
                    {
                        if (start == -1)
                        {
                            start = i;
                        }
                        level++;
                    }
                    break;
                case '}':
                    if (start != -1)
                    {
                        end = i;
                    }
                    if (level > 0)
                    {
                        level--;
                    }
                    break;
                default:
                    break;
            }
            if (level == 0 && end != -1)
            {
                return resolveVariable(value, start, end);
            }
        }
        return value;
    }

    private Object resolveVariable(String value, int start, int end)
    {
        String variableKey = value.substring(start + 2, end);
        Object resolved = resolveVariable(value, variableKey);
        if (end == value.length() - 1)
        {
            return resolved;
        }
        String resolvedStr = (String) resolved;
        int nextScanStartPosition = resolvedStr.indexOf(value.substring(end + 1), start);
        return resolveVariables(nextScanStartPosition, resolvedStr);
    }

    private Object resolveVariable(String value, String variableKey)
    {
        String updatedVariableKey;
        String target;
        if (variableKey.contains(VARIABLE_START_MARKER))
        {
            updatedVariableKey = (String) resolveVariables(0, variableKey);
            target = StringUtils.replaceOnce(value, variableKey, updatedVariableKey);
        }
        else
        {
            updatedVariableKey = variableKey;
            target = value;
        }
        return replaceVariableKeyWithValue(target, updatedVariableKey);
    }

    private Object replaceVariableKeyWithValue(String target, String variableKey)
    {
        Object variableValue = getVariableValue(variableKey);
        if (variableValue == null)
        {
            return target;
        }
        String variablePlaceholder = VARIABLE_START_MARKER + variableKey + "}";
        if (variablePlaceholder.equals(target))
        {
            return variableValue;
        }
        return StringUtils.replaceOnce(target, variablePlaceholder, variableValue.toString());
    }

    private Object getVariableValue(String variableKey)
    {
        Object variable = bddVariableContext.getVariable(variableKey);
        if (variable == null)
        {
            variable = Optional.ofNullable(dynamicVariables.get(variableKey))
                               .map(DynamicVariable::getValue)
                               .orElse(null);
        }
        return variable;
    }
}
