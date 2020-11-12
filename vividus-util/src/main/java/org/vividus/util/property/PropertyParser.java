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
import static org.apache.commons.lang3.StringUtils.removeStart;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

public class PropertyParser implements IPropertyParser
{
    private Properties properties;

    @Override
    public Map<String, String> getPropertiesByPrefix(String propertyPrefix)
    {
        return filterProperties(p -> ((String) p.getKey()).startsWith(propertyPrefix));
    }

    @Override
    public Map<String, String> getPropertyValuesByPrefix(String propertyPrefix)
    {
        return getPropertiesByPrefix(propertyPrefix).entrySet()
                .stream()
                .collect(toMap(e -> removeStart(e.getKey(), propertyPrefix), Entry::getValue));
    }

    @Override
    public Map<String, Object> getPropertyValuesTreeByPrefix(String propertyPrefix)
    {
        Map<String, Object> container = new HashMap<>();
        getPropertyValuesByPrefix(propertyPrefix).forEach((k, v) -> putByPath(container, k, v));
        return container;
    }

    @SuppressWarnings("unchecked")
    public static void putByPath(Map<String, Object> container, String path, Object value)
    {
        String[] paths = StringUtils.split(path, '.');
        int limit = paths.length - 1;
        String key = paths[limit];
        Map<String, Object> target = Stream.of(paths)
                .limit(limit)
                .reduce(container, (map, pathKey) ->
                {
                    Map<String, Object> nested = (Map<String, Object>) map.compute(pathKey, (k, v) ->
                    {
                        if (v == null)
                        {
                            return new HashMap<>();
                        }
                        else if (v instanceof Map)
                        {
                            return v;
                        }
                        throw new IllegalArgumentException(String.format(
                                "Path key '%s' from path '%s' is already used as a property key", pathKey, path));
                    });
                    map.put(pathKey, nested);
                    return nested;
                }, (l, r) -> l);
        target.put(key, value);
    }

    @Override
    public Map<String, String> getPropertiesByRegex(Pattern regex)
    {
        return filterProperties(p -> regex.matcher((String) p.getKey()).matches());
    }

    @Override
    public String getPropertyValue(String propertyNameFormat, Object... args)
    {
        return properties.getProperty(String.format(propertyNameFormat, args));
    }

    private Map<String, String> filterProperties(Predicate<? super Entry<Object, Object>> filter)
    {
        return properties.entrySet().stream().filter(filter)
                .collect(toMap(p -> (String) p.getKey(), p -> (String) p.getValue()));
    }

    public void setProperties(Properties properties)
    {
        this.properties = (Properties) properties.clone();
    }
}
