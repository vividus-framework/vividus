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

import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

public class PropertyParser implements IPropertyParser
{
    private static final String PROPERTY_FAMILY_SPLITTER = ".";

    private Properties properties;

    @Override
    public Map<String, String> getPropertiesByPrefix(String propertyPrefix)
    {
        return filterProperties(p -> ((String) p.getKey()).startsWith(propertyPrefix));
    }

    @Override
    public Map<String, String> getPropertyValuesByFamily(String family)
    {
        return getPropertiesByPrefix(family + PROPERTY_FAMILY_SPLITTER).entrySet()
                .stream()
                .filter(p -> family.equals(extractPropertyFamily(p)))
                .collect(toMap(PropertyParser::extractPropertyKey, Entry::getValue));
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

    private static String extractPropertyFamily(Entry<String, String> p)
    {
        return StringUtils.substringBeforeLast(p.getKey(), PROPERTY_FAMILY_SPLITTER);
    }

    private static String extractPropertyKey(Entry<String, String> p)
    {
        return StringUtils.substringAfterLast(p.getKey(), PROPERTY_FAMILY_SPLITTER);
    }

    public void setProperties(Properties properties)
    {
        this.properties = (Properties) properties.clone();
    }
}
