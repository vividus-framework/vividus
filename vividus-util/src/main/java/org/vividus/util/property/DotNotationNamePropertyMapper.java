/*
 * Copyright 2019-2024 the original author or authors.
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

import static java.util.stream.Collectors.toSet;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;

import org.apache.commons.lang3.StringUtils;

public class DotNotationNamePropertyMapper extends PropertyMapper
{
    public DotNotationNamePropertyMapper(String propertyPrefixSeparator, PropertyNamingStrategy namingStrategy,
                                         PropertyParser propertyParser, Set<JsonDeserializer<?>> deserializers)
    {
        super(propertyPrefixSeparator, namingStrategy, propertyParser, deserializers);
    }

    @Override
    protected Set<String> getKeys(Set<String> propertyNames, String propertyPrefix)
    {
        return propertyNames.stream().map(propertyName ->
        {
            String propertyNameWithoutPrefix = StringUtils.removeStart(propertyName, propertyPrefix);
            return StringUtils.substringBeforeLast(propertyNameWithoutPrefix, getPropertyPrefixSeparator());
        }).collect(toSet());
    }

    @Override
    protected Map<String, String> collectObjectProperties(Map<String, String> properties, String propertyFamily)
    {
        Map<String, String> objectProperties = new HashMap<>();
        for (Map.Entry<String, String> entry : properties.entrySet())
        {
            String key = entry.getKey();
            if (key.startsWith(propertyFamily))
            {
                String objectKey = StringUtils.removeStart(key, propertyFamily);
                if (!objectKey.contains(getPropertyPrefixSeparator()))
                {
                    objectProperties.put(objectKey, entry.getValue());
                }
            }
        }
        return objectProperties;
    }
}
