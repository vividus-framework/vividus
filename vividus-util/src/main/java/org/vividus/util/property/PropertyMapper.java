/*
 * Copyright 2019 the original author or authors.
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
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import javax.inject.Inject;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.javaprop.JavaPropsMapper;

import org.apache.commons.lang3.StringUtils;

public class PropertyMapper implements IPropertyMapper
{
    private static final String PROPERTY_FAMILY_SEPARATOR = ".";
    private static final JavaPropsMapper PROPS_MAPPER = new JavaPropsMapper();
    @Inject
    private Set<JsonDeserializer<?>> deserializers;
    private PropertyParser propertyParser;

    @SuppressWarnings("unchecked")
    public void init()
    {
        PROPS_MAPPER.setPropertyNamingStrategy(PropertyNamingStrategy.KEBAB_CASE);
        SimpleModule module = new SimpleModule();
        deserializers.forEach(deserializer -> module.addDeserializer(getType(deserializer), deserializer));
        PROPS_MAPPER.registerModule(module);
        PROPS_MAPPER.findAndRegisterModules();
    }

    @Override
    public <T> Map<String, T> readValues(String propertyPrefix, Class<T> valueType) throws IOException
    {
        Map<String, String> properties = propertyParser.getPropertiesByPrefix(propertyPrefix);
        Set<String> keys = getKeys(properties.keySet(), propertyPrefix);
        Map<String, T> result = new HashMap<>(keys.size());
        for (String key : keys)
        {
            String propertyFamily = propertyPrefix + key + PROPERTY_FAMILY_SEPARATOR;
            Properties objectProps = properties.entrySet().stream().filter(e -> e.getKey().startsWith(propertyFamily))
                    .collect(toMap(e -> StringUtils.removeStart(e.getKey(), propertyFamily), Entry::getValue,
                        (v1, v2) -> null, Properties::new));
            T value = PROPS_MAPPER.readPropertiesAs(objectProps, valueType);
            result.put(key, value);
        }
        return result;
    }

    @SuppressWarnings("rawtypes")
    private Class getType(JsonDeserializer<?> deserializer)
    {
        Type type = ((ParameterizedType) deserializer.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        return (Class<?>) (type instanceof ParameterizedType ? ((ParameterizedType) type).getRawType() : type);
    }

    private static Set<String> getKeys(Set<String> propertyNames, String propertyPrefix)
    {
        return propertyNames.stream().map(propertyName ->
        {
            String propertyNameWithoutPrefix = StringUtils.removeStart(propertyName, propertyPrefix);
            return StringUtils.substringBefore(propertyNameWithoutPrefix, PROPERTY_FAMILY_SEPARATOR);
        }).collect(toSet());
    }

    public void setPropertyParser(PropertyParser propertyParser)
    {
        this.propertyParser = propertyParser;
    }
}
