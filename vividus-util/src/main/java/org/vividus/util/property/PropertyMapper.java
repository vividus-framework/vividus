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

package org.vividus.util.property;

import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.IntFunction;
import java.util.function.UnaryOperator;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.javaprop.JavaPropsMapper;

import org.apache.commons.lang3.StringUtils;

public class PropertyMapper implements IPropertyMapper
{
    private static final String PROPERTY_PREFIX_SEPARATOR = ".";
    private final JavaPropsMapper javaPropsMapper;
    private final PropertyParser propertyParser;

    public PropertyMapper(PropertyParser propertyParser, Set<JsonDeserializer<?>> deserializers)
    {
        this.propertyParser = propertyParser;
        SimpleModule module = new SimpleModule();
        deserializers.forEach(deserializer -> module.addDeserializer(getType(deserializer), deserializer));
        javaPropsMapper = new JavaPropsMapper();
        javaPropsMapper.setPropertyNamingStrategy(PropertyNamingStrategies.KEBAB_CASE).registerModule(module);
        javaPropsMapper.findAndRegisterModules();
    }

    @Override
    public <T> T readValue(String propertyPrefix, Class<T> resultType) throws IOException
    {
        Map<String, String> propertyValuesByPrefix = propertyParser.getPropertyValuesByPrefix(propertyPrefix);
        return javaPropsMapper.readMapAs(propertyValuesByPrefix, resultType);
    }

    @Override
    public <T> PropertyMappedCollection<T> readValues(String propertyPrefix, Class<T> valueType) throws IOException
    {
        return readValues(propertyPrefix, UnaryOperator.identity(), valueType);
    }

    @Override
    public <T> PropertyMappedCollection<T> readValues(String propertyPrefix, UnaryOperator<String> keyMapper,
            Class<T> valueType) throws IOException
    {
        return readValues(propertyPrefix, keyMapper, valueType,  HashMap::new);
    }

    @Override
    public <T> PropertyMappedCollection<T> readValues(String propertyPrefix, UnaryOperator<String> keyMapper,
            Comparator<String> keyComparator, Class<T> valueType) throws IOException
    {
        return readValues(propertyPrefix, keyMapper, valueType, keysSize -> new TreeMap<>(keyComparator));
    }

    private <T> PropertyMappedCollection<T> readValues(String propertyPrefix, UnaryOperator<String> keyMapper,
            Class<T> valueType, IntFunction<Map<String, T>> mapProducer) throws IOException
    {
        Map<String, String> properties = propertyParser.getPropertiesByPrefix(propertyPrefix);
        Set<String> keys = getKeys(properties.keySet(), propertyPrefix);
        Map<String, T> result = mapProducer.apply(keys.size());
        for (String key : keys)
        {
            String propertyFamily = propertyPrefix + key + PROPERTY_PREFIX_SEPARATOR;
            Map<String, String> objectProps = properties.entrySet().stream()
                    .filter(e -> e.getKey().startsWith(propertyFamily))
                    .collect(toMap(e -> StringUtils.removeStart(e.getKey(), propertyFamily), Entry::getValue));
            T value = javaPropsMapper.readMapAs(objectProps, valueType);
            result.put(keyMapper.apply(key), value);
        }
        return new PropertyMappedCollection<>(result);
    }

    @SuppressWarnings("unchecked")
    private <T> Class<T> getType(JsonDeserializer<? extends T> deserializer)
    {
        Type type = ((ParameterizedType) deserializer.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        return (Class<T>) (type instanceof ParameterizedType ? ((ParameterizedType) type).getRawType() : type);
    }

    private static Set<String> getKeys(Set<String> propertyNames, String propertyPrefix)
    {
        return propertyNames.stream().map(propertyName ->
        {
            String propertyNameWithoutPrefix = StringUtils.removeStart(propertyName, propertyPrefix);
            return StringUtils.substringBefore(propertyNameWithoutPrefix, PROPERTY_PREFIX_SEPARATOR);
        }).collect(toSet());
    }
}
