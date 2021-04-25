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

package org.vividus.bdd.steps;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.steps.ParameterConverters;

public class ParameterConvertersDecorator extends ParameterConverters
{
    private static final int MAX_DEPTH = 16;

    private final VariableResolver variableResolver;

    private final ExpressionAdaptor expressionAdaptor;

    public ParameterConvertersDecorator(Configuration configuration, VariableResolver variableResolver,
            ExpressionAdaptor expressionAdaptor)
    {
        super(configuration.stepMonitor(), configuration.keywords(), configuration.storyLoader(),
                configuration.parameterControls(), configuration.tableTransformers());
        this.variableResolver = variableResolver;
        this.expressionAdaptor = expressionAdaptor;
    }

    @Override
    public Object convert(String value, Type type)
    {
        if (type == SubSteps.class)
        {
            return super.convert(value, type);
        }
        Object adaptedValue = resolvePlaceholders(value, type);
        if (type == String.class || adaptedValue instanceof String)
        {
            adaptedValue = processExpressions(String.valueOf(adaptedValue));
        }
        boolean parametrizedType = type instanceof ParameterizedType;
        if (type != String.class && (type == adaptedValue.getClass()
                || type instanceof Class<?> && ((Class<?>) type).isInstance(adaptedValue)
                || parametrizedType && isAssignableFrom((ParameterizedType) type, adaptedValue.getClass())))
        {
            return adaptedValue;
        }
        String convertedValue = String.valueOf(adaptedValue);
        if (parametrizedType)
        {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            if (parameterizedType.getRawType().equals(Optional.class))
            {
                Type argType = parameterizedType.getActualTypeArguments()[0];
                return StringUtils.isBlank(convertedValue) ? Optional.empty()
                        : Optional.of(super.convert(convertedValue, argType));
            }
        }
        Object result = super.convert(convertedValue, type);
        resolvePlaceholdersAfterConversion(type, result);
        return result;
    }

    private void resolvePlaceholdersAfterConversion(Type type, Object result)
    {
        if (type == ExamplesTable.class)
        {
            ExamplesTable examplesTable = (ExamplesTable) result;
            List<Map<String, String>> rows = examplesTable.getRows();
            rows.forEach(
                row -> row.entrySet().forEach(
                    cell -> cell.setValue((String) resolvePlaceholders(cell.getValue(), String.class))
                )
            );
            examplesTable.withRows(rows);
        }
    }

    private Object resolvePlaceholders(String value, Type type)
    {
        return resolvePlaceholders(value, value, type, 1);
    }

    private Object resolvePlaceholders(String originalValue, String value, Type type, int iteration)
    {
        Object adaptedValue = variableResolver.resolve(value);
        if (type == String.class || String.class.isInstance(adaptedValue))
        {
            adaptedValue = processExpressions(String.valueOf(adaptedValue));
            if (!value.equals(adaptedValue) && adaptedValue instanceof String)
            {
                if (iteration == MAX_DEPTH)
                {
                    // If we reached max depth, then we see circular reference or variables have more than MAX_DEPTH
                    // nested levels, we need to fallback to one cycle of variable resolution and exit early
                    return variableResolver.resolve(originalValue);
                }
                return resolvePlaceholders(originalValue, (String) adaptedValue, type, iteration + 1);
            }
        }
        return adaptedValue;
    }

    private boolean isAssignableFrom(ParameterizedType parameterizedType, Class<?> clazz)
    {
        Type rawType = parameterizedType.getRawType();
        return rawType instanceof Class<?> && ((Class<?>) rawType).isAssignableFrom(clazz);
    }

    private Object processExpressions(String valueToConvert)
    {
        return expressionAdaptor.process(valueToConvert);
    }
}
