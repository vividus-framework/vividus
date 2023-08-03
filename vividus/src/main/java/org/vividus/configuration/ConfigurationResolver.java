/*
 * Copyright 2019-2023 the original author or authors.
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

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.util.PropertyPlaceholderHelper;
import org.vividus.spring.SpelExpressionResolver;

public final class ConfigurationResolver
{
    static final String CONFIGURATION_PROPERTY_FAMILY = "configuration.";
    static final String CONFIGURATION_SET_PROPERTY_FAMILY = "configuration-set.";

    private static final SpelExpressionResolver SPEL_RESOLVER = new SpelExpressionResolver();
    private static final String VIVIDUS_SYSTEM_PROPERTY_FAMILY = "vividus.";
    private static final String ROOT = "";
    private static final String PROFILES = "profiles";
    private static final String ENVIRONMENTS = "environments";
    private static final String SUITES = "suites";
    private static final String PLACEHOLDER_PREFIX = "${";
    private static final String PLACEHOLDER_SUFFIX = "}";
    private static final String PLACEHOLDER_VALUE_SEPARATOR = "=";

    private static final String[] DEFAULTS_PATHS = {
        "org/vividus/http/client",
        "org/vividus/util"
    };

    private static ConfigurationResolver instance;

    private final Properties properties;

    private ConfigurationResolver(Properties properties)
    {
        this.properties = properties;
    }

    public static ConfigurationResolver getInstance() throws IOException
    {
        if (instance != null)
        {
            return instance;
        }

        PropertiesLoader propertiesLoader = new PropertiesLoader(BeanFactory.getResourcePatternResolver());

        Properties properties = new Properties();

        final Properties configurationProperties = propertiesLoader.loadConfigurationProperties();
        final Properties overridingProperties = propertiesLoader.loadOverridingProperties();
        final Properties deprecatedProperties = propertiesLoader.loadFromResourceTreeRecursively(false, "deprecated");

        propertiesLoader.prohibitConfigurationProperties();

        for (String defaultPath : DEFAULTS_PATHS)
        {
            properties.putAll(propertiesLoader.loadDefaultProperties(defaultPath));
        }

        properties.putAll(propertiesLoader.loadFromResourceTreeRecursively(true, "defaults"));
        properties.putAll(configurationProperties);
        properties.putAll(propertiesLoader.loadFromResourceTreeRecursively(true,
            r -> !r.getFilename().endsWith(PropertiesLoader.CONFIGURATION_FILENAME), ROOT));

        Multimap<String, String> configuration = assembleConfiguration(configurationProperties, overridingProperties);
        for (Entry<String, String> configurationEntry : configuration.entries())
        {
            Properties configurationItem = propertiesLoader.loadFromResourceTreeRecursively(
                    !configurationEntry.getValue().isEmpty(), configurationEntry.getKey(),
                    configurationEntry.getValue());
            properties.putAll(configurationItem);
        }

        DeprecatedPropertiesHandler deprecatedPropertiesHandler = new DeprecatedPropertiesHandler(
                deprecatedProperties, PLACEHOLDER_PREFIX, PLACEHOLDER_SUFFIX);
        deprecatedPropertiesHandler.replaceDeprecated(properties);

        Properties overridingAndSystemProperties = new Properties();
        overridingAndSystemProperties.putAll(overridingProperties);
        overridingAndSystemProperties.putAll(System.getenv());
        overridingAndSystemProperties.putAll(loadFilteredSystemProperties());

        deprecatedPropertiesHandler.replaceDeprecated(overridingAndSystemProperties, properties);

        properties.putAll(overridingAndSystemProperties);

        resolveSpelExpressions(properties, true);

        PropertyPlaceholderHelper propertyPlaceholderHelper = createPropertyPlaceholderHelper(false);

        for (Entry<Object, Object> entry : properties.entrySet())
        {
            Object value = entry.getValue();
            deprecatedPropertiesHandler.warnIfDeprecated((String) entry.getKey(), value);
            if (value instanceof String)
            {
                entry.setValue(propertyPlaceholderHelper.replacePlaceholders((String) value, properties::getProperty));
            }
        }
        deprecatedPropertiesHandler.removeDeprecated(properties);
        resolveSpelExpressions(properties, false);

        try (VaultStoredPropertiesProcessor vaultProcessor = new VaultStoredPropertiesProcessor(properties))
        {
            PropertiesProcessor propertiesProcessor = new DelegatingPropertiesProcessor(List.of(
                    new EncryptedPropertiesProcessor(properties), vaultProcessor
            ));
            new SystemPropertiesInitializer(propertiesProcessor).setSystemProperties(properties);
            properties = propertiesProcessor.processProperties(properties);
        }

        instance = new ConfigurationResolver(properties);
        return instance;
    }

    private static PropertyPlaceholderHelper createPropertyPlaceholderHelper(boolean ignoreUnresolvablePlaceholders)
    {
        return new PropertyPlaceholderHelper(PLACEHOLDER_PREFIX, PLACEHOLDER_SUFFIX, PLACEHOLDER_VALUE_SEPARATOR,
                ignoreUnresolvablePlaceholders);
    }

    private static Multimap<String, String> assembleConfiguration(Properties configurationProperties,
            Properties overridingProperties)
    {
        Map<String, String> configurationSet = resolveConfiguration(configurationProperties, overridingProperties);
        String profiles = configurationSet.get(PROFILES);
        String environments = configurationSet.get(ENVIRONMENTS);
        String suites = configurationSet.get(SUITES);

        Properties mergedProperties = new Properties();
        mergedProperties.putAll(configurationProperties);
        mergedProperties.putAll(overridingProperties);
        PropertyPlaceholderHelper propertyPlaceholderHelper = createPropertyPlaceholderHelper(true);

        profiles = propertyPlaceholderHelper.replacePlaceholders(profiles, mergedProperties::getProperty);
        environments = propertyPlaceholderHelper.replacePlaceholders(environments, mergedProperties::getProperty);
        suites = propertyPlaceholderHelper.replacePlaceholders(suites, mergedProperties::getProperty);

        Multimap<String, String> configuration = LinkedHashMultimap.create();
        configuration.putAll("profile", asPaths(resolveSpel(profiles)));
        configuration.putAll("environment", asPaths(resolveSpel(environments)));
        configuration.putAll("suite", asPaths(resolveSpel(suites)));
        return configuration;
    }

    private static Map<String, String> resolveConfiguration(Properties configurationProperties,
        Properties overridingProperties)
    {
        String configurationSet = getPropertyValue(configurationProperties, overridingProperties,
                "configuration-set.active");

        String profiles;
        String environments;
        String suites;

        if (configurationSet == null)
        {
            profiles = getConfigurationPropertyValue(configurationProperties, overridingProperties,
                    CONFIGURATION_PROPERTY_FAMILY + PROFILES);
            environments = getConfigurationPropertyValue(configurationProperties, overridingProperties,
                    CONFIGURATION_PROPERTY_FAMILY + ENVIRONMENTS);
            suites = getConfigurationPropertyValue(configurationProperties, overridingProperties,
                    CONFIGURATION_PROPERTY_FAMILY + SUITES);
        }
        else
        {
            Validate.validState(!configurationSet.isBlank(), "Property 'configuration-set.active' must be not blank.");
            profiles = getConfigurationSetProperty(configurationProperties, overridingProperties, configurationSet,
                PROFILES);
            environments = getConfigurationSetProperty(configurationProperties, overridingProperties, configurationSet,
                ENVIRONMENTS);
            suites = getConfigurationSetProperty(configurationProperties, overridingProperties, configurationSet,
                SUITES);
            overridingProperties.put(CONFIGURATION_PROPERTY_FAMILY + PROFILES, profiles);
            overridingProperties.put(CONFIGURATION_PROPERTY_FAMILY + ENVIRONMENTS, environments);
            overridingProperties.put(CONFIGURATION_PROPERTY_FAMILY + SUITES, suites);
        }
        return Map.of(PROFILES, profiles, SUITES, suites, ENVIRONMENTS, environments);
    }

    private static String getConfigurationSetProperty(Properties configurationProperties,
            Properties overridingProperties, String configurationSet, String key)
    {
        return getConfigurationPropertyValue(configurationProperties, overridingProperties,
                CONFIGURATION_SET_PROPERTY_FAMILY + configurationSet + "." + key);
    }

    private static String resolveSpel(String property)
    {
        return String.valueOf(SPEL_RESOLVER.resolve(property));
    }

    private static List<String> asPaths(String value)
    {
        if (value.isEmpty())
        {
            return List.of(value);
        }
        // First configuration paths in the sequence have high priority then next ones
        return Stream.of(StringUtils.split(value, ','))
                .collect(Collectors.collectingAndThen(Collectors.toList(), Lists::reverse));
    }

    private static Map<String, String> loadFilteredSystemProperties()
    {
        Properties systemProperties = System.getProperties();
        return systemProperties.stringPropertyNames().stream()
                .filter(p -> p.startsWith(VIVIDUS_SYSTEM_PROPERTY_FAMILY))
                .filter(p -> !p.startsWith(VIVIDUS_SYSTEM_PROPERTY_FAMILY + CONFIGURATION_PROPERTY_FAMILY))
                .collect(Collectors.toMap(
                    p -> StringUtils.removeStart(p, VIVIDUS_SYSTEM_PROPERTY_FAMILY), systemProperties::getProperty));
    }

    private static String getConfigurationPropertyValue(Properties configurationProperties,
            Properties overridingProperties, String key)
    {
        String value =
            getPropertyValue(configurationProperties, overridingProperties, key);
        Validate.validState(value != null, "The '%s' property is not set", key);
        return value;
    }

    private static String getPropertyValue(Properties configurationProperties,
            Properties overridingProperties, String propertyName)
    {
        String value = System.getProperty(VIVIDUS_SYSTEM_PROPERTY_FAMILY + propertyName,
                System.getProperty(propertyName));
        if (value == null)
        {
            value = overridingProperties.getProperty(propertyName);
            if (value == null)
            {
                value = configurationProperties.getProperty(propertyName);
            }
        }
        else
        {
            overridingProperties.put(propertyName, value);
        }
        return value;
    }

    private static void resolveSpelExpressions(Properties properties, boolean ignoreValuesWithPropertyPlaceholders)
    {
        Optional<Set<String>> propertyPlaceholders = ignoreValuesWithPropertyPlaceholders
                ? Optional.of(properties.stringPropertyNames().stream()
                        .map(n -> PLACEHOLDER_PREFIX + n + PLACEHOLDER_SUFFIX)
                        .collect(Collectors.toSet()))
                : Optional.empty();

        for (Entry<Object, Object> entry : properties.entrySet())
        {
            Object value = entry.getValue();
            if (value instanceof String)
            {
                String strValue = (String) value;
                if (propertyPlaceholders.stream().flatMap(Set::stream).noneMatch(strValue::contains))
                {
                    entry.setValue(SPEL_RESOLVER.resolve(strValue));
                }
            }
        }
    }

    public static void reset()
    {
        instance = null;
    }

    public Properties getProperties()
    {
        isReset();
        return (Properties) properties.clone();
    }

    private static void isReset()
    {
        if (instance == null)
        {
            throw new IllegalStateException("ConfigurationResolver has not been initialized after the reset");
        }
    }
}
