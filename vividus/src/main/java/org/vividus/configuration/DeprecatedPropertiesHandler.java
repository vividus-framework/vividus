/*
 * Copyright 2019-2022 the original author or authors.
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeprecatedPropertiesHandler
{
    private static final String FALSE = "false";

    private static final Pattern BOOLEAN = Pattern.compile("false|true");

    private static final Logger LOGGER = LoggerFactory.getLogger(DeprecatedPropertiesHandler.class);

    private static final String PATTERN_FORMAT = "%1$s(.[^%1$s%2$s]*)%2$s";

    private static final String DEPRECATED_WARN_MSG = "Deprecated property found: '{}'. Use '{}' instead";

    private static final String PLACEHOLDER_WARN_MSG_FORMAT =
            "Property '{}' uses deprecated placeholder '%1$s{}%2$s'. Use '%1$s{}%2$s' instead";

    private final String placeholderWarnMsg;
    private final Pattern placeholderPattern;
    private final List<DeprecatedProperty> dynamicDeprecatedProperties = new ArrayList<>();
    private final Map<String, DeprecatedProperty> deprecatedProperties = new HashMap<>();

    public DeprecatedPropertiesHandler(Properties propertiesToDeprecate, String placeholderPrefix,
            String placeholderSuffix)
    {
        for (Entry<Object, Object> entry : propertiesToDeprecate.entrySet())
        {
            String key = (String) entry.getKey();
            String value = (String) entry.getValue();
            if (key.indexOf('(') != -1)
            {
                this.dynamicDeprecatedProperties.add(new DynamicDeprecatedProperty(key, value));
            }
            else
            {
                DeprecatedProperty deprecatedProperty = new DeprecatedProperty(key, value);
                deprecatedProperties.put(deprecatedProperty.oldKey, deprecatedProperty);
            }
        }

        placeholderPattern = Pattern.compile(String.format(PATTERN_FORMAT, Pattern.quote(placeholderPrefix),
                Pattern.quote(placeholderSuffix)));
        placeholderWarnMsg = String.format(PLACEHOLDER_WARN_MSG_FORMAT, placeholderPrefix, placeholderSuffix);
    }

    public void warnIfDeprecated(String key, Object value)
    {
        if (isDeprecated(key))
        {
            warnDeprecated(DEPRECATED_WARN_MSG, key, getDeprecatedProperty(key).getNewKey(key));
        }

        if (value instanceof String)
        {
            Matcher placeholderMatcher = placeholderPattern.matcher((String) value);
            while (placeholderMatcher.find())
            {
                String placeholderKey = placeholderMatcher.group(1);
                if (isDeprecated(placeholderKey))
                {
                    warnDeprecated(placeholderWarnMsg, key, placeholderKey,
                            getDeprecatedProperty(placeholderKey).getNewKey(placeholderKey));
                }
            }
        }
    }

    private boolean isDeprecated(String key)
    {
        return null != getDeprecatedProperty(key);
    }

    private DeprecatedProperty getDeprecatedProperty(String key)
    {
        return deprecatedProperties.computeIfAbsent(key, oldKey ->
            dynamicDeprecatedProperties.stream()
                                       .filter(p -> p.isDeprecated(oldKey))
                                       .findFirst()
                                       .orElse(null));
    }

    public void removeDeprecated(Properties properties)
    {
        properties.keySet()
                  .stream()
                  .map(String.class::cast)
                  .filter(this::isDeprecated)
                  .distinct()
                  .forEach(properties::remove);
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
            DeprecatedProperty deprecatedProperty = getDeprecatedProperty(key);
            if (deprecatedProperty != null)
            {
                String newPropertyKey = deprecatedProperty.getNewKey(key);
                String newPropertyValue = replaceInProperties.getProperty(newPropertyKey);
                if (newPropertyValue == null || !placeholderPattern.matcher(newPropertyValue).find())
                {
                    replaceKey(replaceInProperties, value, newPropertyKey, deprecatedProperty.isInvertValue());
                }
            }
        }
    }

    private void replaceKey(Properties replaceInProperties, String value, String newPropertyKey,
            boolean invert)
    {
        String propertyValue = value;
        if (invert && BOOLEAN.matcher(propertyValue).matches())
        {
            propertyValue = FALSE.equals(propertyValue) ? "true" : FALSE;
        }
        replaceInProperties.put(newPropertyKey, propertyValue);
    }

    protected void warnDeprecated(String format, Object... arguments)
    {
        LOGGER.warn(format, arguments);
    }

    private final class DynamicDeprecatedProperty extends DeprecatedProperty
    {
        private final Pattern regex;
        private final String replacement;

        private DynamicDeprecatedProperty(String oldKey, String newKey)
        {
            super(oldKey, newKey);
            this.regex = Pattern.compile(super.oldKey);
            this.replacement = newKey;
        }

        @Override
        protected boolean isDeprecated(String key)
        {
            return regex.matcher(key).matches();
        }

        @Override
        protected String getNewKey(String oldKey)
        {
            return regex.matcher(oldKey).replaceFirst(replacement);
        }
    }

    @SuppressWarnings("FinalClass")
    private static class DeprecatedProperty
    {
        private final String oldKey;
        private final String newKey;
        private final boolean invertValue;

        private DeprecatedProperty(String oldKey, String newKey)
        {
            this.invertValue = oldKey.charAt(0) == '!';
            this.oldKey = invertValue ? oldKey.substring(1) : oldKey;
            this.newKey = newKey;
        }

        protected boolean isDeprecated(String key)
        {
            return oldKey.equals(key);
        }

        protected String getNewKey(String oldKey)
        {
            return newKey;
        }

        private boolean isInvertValue()
        {
            return invertValue;
        }
    }
}
