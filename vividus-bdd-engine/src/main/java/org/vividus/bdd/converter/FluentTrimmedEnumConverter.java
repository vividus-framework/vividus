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

package org.vividus.bdd.converter;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Type;
import java.util.Optional;

import javax.inject.Named;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import org.jbehave.core.steps.ParameterConverters.FluentEnumConverter;

@Named
public class FluentTrimmedEnumConverter extends FluentEnumConverter
{
    private static final LoadingCache<Class<?>, Optional<MethodHandle>> CONVERTERS = CacheBuilder.newBuilder()
            .build(new CacheLoader<>()
            {
                @Override
                public Optional<MethodHandle> load(Class<?> clazz) throws Exception
                {
                    try
                    {
                        MethodType mt = MethodType.methodType(clazz, String.class);
                        MethodHandle fromString = MethodHandles.publicLookup().findStatic(clazz, "fromString", mt);
                        return Optional.of(fromString);
                    }
                    catch (NoSuchMethodException | SecurityException | IllegalAccessException e)
                    {
                        return Optional.empty();
                    }
                }
            });

    @Override
    public Enum<?> convertValue(String value, Type type)
    {
        return fromString(value, type).orElseGet(() -> super.convertValue(value.trim(), type));
    }

    private Optional<Enum<?>> fromString(String value, Type type)
    {
        return CONVERTERS.getUnchecked((Class<?>) type).map(fromString -> invokeStatic(value, fromString));
    }

    @SuppressWarnings("IllegalCatchExtended")
    private Enum<?> invokeStatic(String value, MethodHandle fromString)
    {
        try
        {
            return (Enum<?>) fromString.invoke(value);
        }
        // deepcode ignore dontCatch~1: API limitation
        catch (Throwable e)
        {
            return null;
        }
    }
}
