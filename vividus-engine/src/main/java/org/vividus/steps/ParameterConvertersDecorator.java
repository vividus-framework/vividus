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

package org.vividus.steps;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.steps.ParameterConverters;

public class ParameterConvertersDecorator extends ParameterConverters
{
    private final PlaceholderResolver placeholderResolver;

    public ParameterConvertersDecorator(Configuration configuration, PlaceholderResolver placeholderResolver)
    {
        super(configuration.stepMonitor(), configuration.keywords(), configuration.storyLoader(),
                configuration.parameterControls(), configuration.tableTransformers());
        this.placeholderResolver = placeholderResolver;
    }

    @Override
    public Object convert(String value, Type type)
    {
        if (type == SubSteps.class)
        {
            return super.convert(value, type);
        }
        Object adaptedValue = placeholderResolver.resolvePlaceholders(value, type);
        if (type == String.class || adaptedValue instanceof String)
        {
            adaptedValue = placeholderResolver.processExpressions(String.valueOf(adaptedValue));
        }
        return convertAdaptedValue(adaptedValue, type);
    }

    private Object convertAdaptedValue(Object adaptedValue, Type type)
    {
        if (type == DataWrapper.class)
        {
            return new DataWrapper(adaptedValue);
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
        return super.convert(convertedValue, type);
    }

    private boolean isAssignableFrom(ParameterizedType parameterizedType, Class<?> clazz)
    {
        Type rawType = parameterizedType.getRawType();
        return rawType instanceof Class<?> && ((Class<?>) rawType).isAssignableFrom(clazz);
    }
}
