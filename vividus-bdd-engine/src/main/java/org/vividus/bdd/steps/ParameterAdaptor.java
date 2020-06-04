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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import org.vividus.bdd.context.IBddVariableContext;
import org.vividus.bdd.variable.DynamicVariable;

public class ParameterAdaptor
{
    private static final Pattern DYNAMIC_DATA_PATTERN = Pattern.compile("\\$\\{(((?![$#]).)*?)}");

    private IBddVariableContext bddVariableContext;
    private Map<String, DynamicVariable> dynamicVariables = Map.of();

    public Object convert(final String value)
    {
        Object convertedValue = value;
        if (value != null)
        {
            Matcher dynamicDataMatcher = DYNAMIC_DATA_PATTERN.matcher(value);
            if (dynamicDataMatcher.matches())
            {
                convertedValue = updateValue(convertedValue, dynamicDataMatcher, true);
                return convertedValue;
            }
            dynamicDataMatcher.reset(value);
            while (dynamicDataMatcher.find())
            {
                convertedValue = updateValue(convertedValue, dynamicDataMatcher, false);
            }
        }
        return convertedValue;
    }

    private Object updateValue(Object convertedValue, Matcher dynamicDataMatcher, boolean checkType)
    {
        Object variable = getVariableValue(dynamicDataMatcher.group(1));
        if (variable != null)
        {
            if (!checkType)
            {
                return replaceVariable(convertedValue, dynamicDataMatcher, variable);
            }
            return variable instanceof String
                    ? replaceVariable(convertedValue, dynamicDataMatcher, variable) : variable;
        }
        return convertedValue;
    }

    private String replaceVariable(Object convertedValue, Matcher dynamicDataMatcher, Object variableValue)
    {
        return ((String) convertedValue).replace(dynamicDataMatcher.group(0), variableValue.toString());
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

    public void setBddVariableContext(IBddVariableContext bddVariableContext)
    {
        this.bddVariableContext = bddVariableContext;
    }

    @Inject
    public void setDynamicVariables(Map<String, DynamicVariable> dynamicVariables)
    {
        this.dynamicVariables = dynamicVariables;
    }
}
