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

package org.vividus.report;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.regex.Pattern;

import com.google.common.collect.Maps;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.reporter.environment.EnvironmentConfigurer;
import org.vividus.reporter.environment.PropertyCategory;

public final class MetadataLogger
{
    private static final Logger LOGGER = LoggerFactory.getLogger(MetadataLogger.class);
    private static final Pattern SECURE_KEY_PATTERN = Pattern
            .compile(".*(password|access-?key|api-?key|secret|token).*", Pattern.CASE_INSENSITIVE);

    private MetadataLogger()
    {
    }

    public static void logEnvironmentMetadata()
    {
        EnvironmentConfigurer.ENVIRONMENT_CONFIGURATION.forEach(MetadataLogger::logMetadata);
    }

    public static void logPropertiesSecurely(Properties properties)
    {
        Maps.fromProperties(properties).entrySet().stream()
            .sorted(Entry.comparingByKey())
            .forEach(MetadataLogger::logPropertySecurely);
    }

    private static void logMetadata(PropertyCategory propertyCategory, Map<String, String> properties)
    {
        LOGGER.info("--------------------------------");
        LOGGER.info("{}: ", propertyCategory.getCategoryName());
        properties.forEach(MetadataLogger::logProperty);
    }

    private static void logProperty(String propertyName, String propetyValue)
    {
        logProperty("{}: {}", propertyName, propetyValue);
    }

    private static void logPropertySecurely(Entry<String, String> entry)
    {
        String key = entry.getKey();
        String value = SECURE_KEY_PATTERN.matcher(key).matches() ? "****" : entry.getValue();
        logProperty("{}={}", key, value);
    }

    private static void logProperty(String format, String key, String value)
    {
        LOGGER.info(format, key, value);
    }
}
