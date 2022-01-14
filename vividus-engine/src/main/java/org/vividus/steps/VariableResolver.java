/*
 * Copyright 2019-2022 the original author or authors.
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

package org.vividus.steps;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import com.google.common.base.CaseFormat;

import org.apache.commons.lang3.StringUtils;
import org.jbehave.core.embedder.StoryControls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.DryRunAwareExecutor;
import org.vividus.context.VariableContext;
import org.vividus.variable.DynamicVariable;

public class VariableResolver implements DryRunAwareExecutor
{
    private static final Logger LOGGER = LoggerFactory.getLogger(VariableResolver.class);

    private static final String VARIABLE_START_MARKER = "${";
    private static final String LINE_BREAKS = "[\r\n]*";

    private final VariableContext variableContext;
    private final Map<String, DynamicVariable> dynamicVariables;
    private final StoryControls storyControls;

    public VariableResolver(VariableContext variableContext, Map<String, DynamicVariable> dynamicVariables,
            StoryControls storyControls)
    {
        this.variableContext = variableContext;
        this.dynamicVariables = new HashMap<>();
        this.storyControls = storyControls;
        dynamicVariables.forEach((key, variable) ->
        {
            this.dynamicVariables.put(key, variable);
            if (key.indexOf('-') > -1)
            {
                String camelKey = CaseFormat.LOWER_HYPHEN.to(CaseFormat.LOWER_CAMEL, key);
                this.dynamicVariables.put(camelKey, variable);
            }
        });
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
        if (target.matches(LINE_BREAKS + Pattern.quote(variablePlaceholder) + LINE_BREAKS))
        {
            return variableValue;
        }
        return StringUtils.replaceOnce(target, variablePlaceholder, variableValue.toString());
    }

    private Object getVariableValue(String variableKey)
    {
        Object variable = variableContext.getVariable(variableKey);
        if (variable == null)
        {
            variable = execute(() -> calculateDynamicVariableValue(variableKey), null);
        }
        return variable;
    }

    private String calculateDynamicVariableValue(String variableKey)
    {
        return Optional.ofNullable(dynamicVariables.get(variableKey))
                .map(DynamicVariable::calculateValue)
                .flatMap(r -> r.getValueOrHandleError(
                        error -> LOGGER.error("Unable to resolve dynamic variable ${{}}: {}", variableKey, error)
                ))
                .orElse(null);
    }

    @Override
    public StoryControls getStoryControls()
    {
        return storyControls;
    }
}
