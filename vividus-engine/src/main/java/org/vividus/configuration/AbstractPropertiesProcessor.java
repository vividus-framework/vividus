/*
 * Copyright 2019-2025 the original author or authors.
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

package org.vividus.configuration;

import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AbstractPropertiesProcessor implements PropertiesProcessor
{
    private final Pattern propertyPattern;

    protected AbstractPropertiesProcessor(String processorRegexMarker)
    {
        this.propertyPattern = Pattern.compile("(" + processorRegexMarker + "\\((.+?)\\)" + ")");
    }

    @Override
    public Properties processProperties(Properties properties)
    {
        for (Map.Entry<Object, Object> entry : properties.entrySet())
        {
            Object propertyValue = entry.getValue();
            if (propertyValue instanceof String propertyValueAsString)
            {
                String newValue = processProperty((String) entry.getKey(), propertyValueAsString);
                entry.setValue(newValue);
            }
        }
        return properties;
    }

    @Override
    public String processProperty(String propertyName, String propertyValue)
    {
        String processedValue = propertyValue;
        Matcher matcher = propertyPattern.matcher(propertyValue);
        while (matcher.find())
        {
            String expressionForProcessing = matcher.group(1);
            String processedPartOfValue = processValue(propertyName, matcher.group(2));
            processedValue = processedValue.replace(expressionForProcessing, processedPartOfValue);
        }
        return processedValue;
    }

    protected abstract String processValue(String propertyName, String partOfPropertyValueToProcess);
}
