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

package org.vividus.configuration;

import java.util.Map.Entry;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeprecatedPropertiesHandler
{
    private static final Logger LOGGER = LoggerFactory.getLogger(DeprecatedPropertiesHandler.class);

    private static final String PATTERN_FORMAT = "%1$s(.[^%1$s%2$s]*)%2$s";

    private static final String DEPRECATED_WARN_MSG = "Deprecated property found: '{}'. Use '{}' instead";

    private static final String PLACEHOLDER_WARN_MSG_FORMAT =
            "Property '{}' uses deprecated placeholder '%1$s{}%2$s'. Use '%1$s{}%2$s' instead";

    private final Properties deprecatedProperties;
    private final String placeholderWarnMsg;
    private final Pattern placeholderPattern;

    public DeprecatedPropertiesHandler(Properties deprecatedProperties, String placeholderPrefix,
            String placeholderSuffix)
    {
        this.deprecatedProperties = deprecatedProperties;

        placeholderPattern = Pattern.compile(String.format(PATTERN_FORMAT, Pattern.quote(placeholderPrefix),
                Pattern.quote(placeholderSuffix)));
        placeholderWarnMsg = String.format(PLACEHOLDER_WARN_MSG_FORMAT, placeholderPrefix, placeholderSuffix);
    }

    public void warnIfDeprecated(String key, String value)
    {
        if (deprecatedProperties.containsKey(key))
        {
            warnDeprecated(DEPRECATED_WARN_MSG, key, deprecatedProperties.get(key));
        }

        Matcher placeholderMatcher = placeholderPattern.matcher(value);
        while (placeholderMatcher.find())
        {
            String placeholderKey = placeholderMatcher.group(1);
            if (deprecatedProperties.containsKey(placeholderKey))
            {
                warnDeprecated(placeholderWarnMsg, key, placeholderKey, deprecatedProperties.get(placeholderKey));
            }
        }
    }

    public void removeDeprecated(Properties properties)
    {
        deprecatedProperties.keySet().forEach(properties::remove);
    }

    public void replaceDeprecated(Properties properties)
    {
        replaceDeprecated(properties, properties);
    }

    public void replaceDeprecated(Properties checkProperties, Properties replaceInProperties)
    {
        for (Entry<Object, Object> entry : checkProperties.entrySet())
        {
            String key = (String) entry.getKey();
            String value = (String) entry.getValue();
            String newPropertyKey = deprecatedProperties.getProperty(key);
            if (newPropertyKey != null)
            {
                String newPropertyValue = replaceInProperties.getProperty(newPropertyKey);
                if (newPropertyValue == null || !placeholderPattern.matcher(newPropertyValue).find())
                {
                    replaceInProperties.put(newPropertyKey, value);
                }
            }
        }
    }

    protected void warnDeprecated(String format, Object... arguments)
    {
        LOGGER.warn(format, arguments);
    }
}
